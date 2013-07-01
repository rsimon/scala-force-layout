package at.ait.dme.forcelayout

import at.ait.dme.forcelayout.quadtree.QuadTree
import at.ait.dme.forcelayout.quadtree.Quad
import at.ait.dme.forcelayout.quadtree.Body
import scala.concurrent._
import scala.collection.parallel.mutable.ParArray

/**
 * A graph layout implementaimport scala.concurrent._
import ExecutionContext.Implicits.global
import at.ait.dme.forcelayout.Springhis code is a port of the Springy JavaScript library (http://getspringy.com/) 
 * by Dennis Hotson. But it also mixes in some ideas from Andrei Kashcha's JavaScript 
 * library VivaGraphJS (https://github.com/anvaka/VivaGraphJS).
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class SpringGraph(val nodes: Seq[Node], val edges: Seq[Edge]) {
  
  /** Parallelizable version of nodes collection**/  
  private val nodes_parallel = nodes.toParArray  
  
  /** Repulsion constant **/
  private var REPULSION = -1.2
  
  /** 'Gravity' constant pulling towards origin **/
  private var CENTER_GRAVITY = -1e-4
  
  /** Default spring length **/
  private var SPRING_LENGTH = 50.0
  
  /** Spring stiffness constant **/
  private var SPRING_COEFFICIENT = 0.0002
      
  /** Drag coefficient **/
  private var DRAG = -0.02
  
  /** Time-step increment **/
  private val TIMESTEP = 20
  
  /** Node velocity limit **/
  private val MAX_VELOCITY = 1.0
  
  /** Barnes-Hut Theta Threshold **/
  private val THETA = 0.8
  
  adjustGraph(nodes_parallel, edges)
  
  def repulsion = REPULSION
  def repulsion_=(value: Double) = REPULSION = value

  def centerGravity = CENTER_GRAVITY
  def centerGravity_=(value: Double) = CENTER_GRAVITY = value
  
  def springCoefficient = SPRING_COEFFICIENT
  def springCoefficient_=(value: Double) = SPRING_COEFFICIENT = value
  
  def springLength = SPRING_LENGTH
  def springLength_=(value: Double) = SPRING_LENGTH = value

  def dragCoefficient = DRAG
  def dragCoefficient_=(value: Double) = DRAG = value

  private def adjustGraph(nodes: ParArray[Node], edges: Seq[Edge]) = {    
    val inLinks = edges.groupBy(_.to.id)
    val outLinks = edges.groupBy(_.from.id)
    
    nodes.foreach(n => {
      val in = inLinks.get(n.id).map(_.foldLeft(0.0)(_ + _.weight)).getOrElse(0.0)
      val out = outLinks.get(n.id).map(_.foldLeft(0.0)(_ + _.weight)).getOrElse(0.0)
      n.mass = 1 + (in + out).toDouble / 3
    })
  }
      
  private def step = {    
    computeHookesLaw(edges)
    computeBarnesHut(nodes)
    computeDrag(nodes)
    computeGravity(nodes)

    nodes_parallel.foreach(node => { 
      val acceleration = node.state.force / node.mass
      node.state.force = Vector2D(0, 0)
            
      node.state.velocity += acceleration * TIMESTEP
      if (node.state.velocity.magnitude > MAX_VELOCITY)
        node.state.velocity = node.state.velocity.normalize * MAX_VELOCITY
       
      node.state.pos += node.state.velocity * TIMESTEP 
    })
  }

  private def computeBarnesHut(nodes: Seq[Node]) = {
    
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
      
    nodes_parallel.foreach(node => apply(node, quadtree.root))
  }
  
  private def computeDrag(nodes: Seq[Node]) = nodes_parallel.foreach(node => node.state.force += node.state.velocity * DRAG)
  
  private def computeGravity(nodes: Seq[Node]) = nodes_parallel.foreach(node => node.state.force += node.state.pos.normalize * CENTER_GRAVITY * node.mass)
  
  private def computeHookesLaw(edges: Seq[Edge]) = edges.foreach(edge => {
    val d = if (edge.to.state.pos == edge.from.state.pos)
        Vector2D.random(0.1, edge.from.state.pos)
      else
        edge.to.state.pos - edge.from.state.pos

    val displacement = d.magnitude - SPRING_LENGTH / edge.weight
    val coeff = SPRING_COEFFICIENT * displacement / d.magnitude   
    val force = d * coeff * 0.5
      
    edge.from.state.force += force
    edge.to.state.force -= force
  })
  
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
  
  def countEdges(node: Node) = {
    // TODO optimize!
    edges.count(edge => edge.from == node || edge.to == node)
  }
  
  def getNearestNode(pt: Vector2D) = nodes.map(node => (node, (node.state.pos - pt).magnitude)).sortBy(_._2).head._1

  def doLayout(onComplete: (Int => Unit) = null, onIteration: (Int => Unit) = null, maxIterations: Int = 1000): Unit = {
    var it = 0
    do { 
      step
      
      if (onIteration != null)
        onIteration(it)
      it += 1
    } while (totalEnergy > 0.001 && it < maxIterations)
      
    if (onComplete != null)
      onComplete(it)
  }
  
}





