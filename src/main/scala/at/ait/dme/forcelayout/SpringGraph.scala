package at.ait.dme.forcelayout

import at.ait.dme.forcelayout.quadtree.QuadTree
import at.ait.dme.forcelayout.quadtree.Quad
import at.ait.dme.forcelayout.quadtree.Body
import scala.concurrent._
import scala.collection.parallel.mutable.ParArray
import scala.collection.parallel.ParSeq

/**
 * A graph layout implementaimport scala.concurrent._
import ExecutionContext.Implicits.global
import at.ait.dme.forcelayout.Springhis code is a port of the Springy JavaScript library (http://getspringy.com/) 
 * by Dennis Hotson. But it also mixes in some ideas from Andrei Kashcha's JavaScript 
 * library VivaGraphJS (https://github.com/anvaka/VivaGraphJS).
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class SpringGraph(val nodes: Seq[Node], val edges: Seq[Edge]) {
  
  val nodes_parallel = buildGraph(nodes, edges)
  
  private def buildGraph(nodes: Seq[Node], edges: Seq[Edge]) = {
    print("Building in-memory graph...")
    
    val inLinks = edges.groupBy(_.to.id)
    val outLinks = edges.groupBy(_.from.id)
    
    nodes.par.map(n => {
      val in = inLinks.get(n.id).getOrElse(Seq.empty[Edge])
      val out = outLinks.get(n.id).getOrElse(Seq.empty[Edge])
      
      // Adjust node mass
      n.mass = 1 + (in.size + out.size).toDouble / 3
      
      // Tuple (node: Node, inlinks: Seq[Edge], outlinks: Seq[Edge])
      (n, in, out)
    })    
  }
  
  /** Repulsion constant **/
  private val REPULSION = -1.2
  
  /** 'Gravity' constant pulling towards origin **/
  private val CENTER_GRAVITY = -1e-4
  
  /** Default spring length **/
  private val SPRING_LENGTH = 50.0
  
  /** Spring stiffness constant **/
  private val SPRING_COEFFICIENT = 0.0002
      
  /** Drag coefficient **/
  private val DRAG = -0.02
  
  /** Time-step increment **/
  private val TIMESTEP = 20
  
  /** Node velocity limit **/
  private val MAX_VELOCITY = 1.0
  
  /** Barnes-Hut Theta Threshold **/
  private val THETA = 0.8
      
  private def step = {    
    computeHookesLaw()
    computeBarnesHut()
    computeDrag()
    computeGravity()

    nodes_parallel.foreach{ case(node, in, out) => { 
      val acceleration = node.state.force / node.mass
      node.state.force = Vector2D(0, 0)
            
      node.state.velocity += acceleration * TIMESTEP
      if (node.state.velocity.magnitude > MAX_VELOCITY)
        node.state.velocity = node.state.velocity.normalize * MAX_VELOCITY
       
      node.state.pos += node.state.velocity * TIMESTEP 
    }}
  }

  private def computeBarnesHut() = {
    
    val quadtree = new QuadTree(bounds, nodes.map(n => Body(n.state.pos, Some(n))))
        
    def apply(node: Node, quad: Quad): Unit = {
      val s = (quad.bounds.width + quad.bounds.height) / 2
      val d = (quad.center - node.state.pos).magnitude
      if (s/d > THETA) {
        // Nearby quad
        if (quad.children.isDefined) {
          quad.children.get.foreach(child => apply(node, child))
        } else if (quad.body.isDefined) {
          val d = quad.body.get.pos - node.state.pos
          val distance = d.magnitude
          val direction = d.normalize
          
          if (quad.body.get.data.get.asInstanceOf[Node] != node) {
            node.state.force += direction * REPULSION / (distance * distance * 0.5)
          } 
        } else {
          Vector2D(0,0)
        }
      } else {
        // Far-away quad
        val d = quad.center - node.state.pos
        val distance = d.magnitude
        val direction = d.normalize
        node.state.force += direction * REPULSION * quad.bodies / (distance * distance * 0.5)
      }
    }
      
    nodes_parallel.foreach(node => apply(node._1, quadtree.root))
  }
  
  private def computeDrag() = nodes_parallel.foreach(node => node._1.state.force += node._1.state.velocity * DRAG)
  
  private def computeGravity() = nodes_parallel.foreach(node => node._1.state.force += node._1.state.pos.normalize * CENTER_GRAVITY * node._1.mass)
  
  private def computeHookesLaw() = {    
    def computeForce(edge: Edge) = {
      if (edge.to.state.pos == edge.from.state.pos)
        edge.to.state.pos = edge.from.state.pos + Vector2D.random(0.01)
      
      val d = edge.to.state.pos - edge.from.state.pos      
      val displacement = d.magnitude - SPRING_LENGTH / edge.weight
      val coeff = SPRING_COEFFICIENT * displacement / d.magnitude   
      d * coeff * 0.5
    }
    
    nodes_parallel.foreach{ case (node, in, out) => {
      in.foreach(inEdge => node.state.force -= computeForce(inEdge))
      out.foreach(outEdge => node.state.force += computeForce(outEdge))
    }}  
  }
  
  def bounds = {
    val positions = nodes.map(n => (n.state.pos.x, n.state.pos.y))
    val minX = positions.minBy(_._1)._1
    val minY = positions.minBy(_._2)._2
    val maxX = positions.maxBy(_._1)._1
    val maxY = positions.maxBy(_._2)._2

    Bounds(minX, minY, maxX, maxY)
  } 
  
  def totalEnergy = {
	nodes.map(node => {
	  val v = node.state.velocity.magnitude
	  0.5 * node.mass * v * v
	}).fold(0.0)(_ + _) 
  }
  
  def getNearestNode(pt: Vector2D) = nodes.map(node => (node, (node.state.pos - pt).magnitude)).sortBy(_._2).head._1

  def doLayout(onComplete: ((Int, Seq[ImmutableNode], Seq[ImmutableEdge]) => Unit) = null, onIteration: ((Int, Seq[ImmutableNode], Seq[ImmutableEdge]) => Unit) = null, maxIterations: Int = 1000): Unit = {
    var it = 0
    do { 
      step
      
      if (onIteration != null)
        onIteration(it, nodes.map(_.immutable), edges.map(e => new ImmutableEdge(e.from.immutable, e.to.immutable, e.weight)))
      it += 1
    } while (totalEnergy > 0.001 && it < maxIterations)
      
    if (onComplete != null)
      onComplete(it, nodes.map(_.immutable), edges.map(e => new ImmutableEdge(e.from.immutable, e.to.immutable, e.weight)))
  }
  
}





