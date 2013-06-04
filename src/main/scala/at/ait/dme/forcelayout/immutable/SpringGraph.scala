package at.ait.dme.forcelayout.immutable

import scala.util.Random
import at.ait.dme.forcelayout.quadtree.QuadTree
import at.ait.dme.forcelayout.Vector
import at.ait.dme.forcelayout.Bounds
import at.ait.dme.forcelayout.Node
import at.ait.dme.forcelayout.quadtree.Quad
import at.ait.dme.forcelayout.quadtree.Body

/**
 * A graph layout implementation based on a basic spring physics model. To a wide 
 * extent, this code is a port of the Springy JavaScript library (http://getspringy.com/) 
 * by Dennis Hotson. But it also mixes in some ideas from Andrei Kashcha's JavaScript 
 * library VivaGraphJS (https://github.com/anvaka/VivaGraphJS).
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class SpringGraph(val nodes: Seq[SpringGraphNode], val edges: Seq[Spring], val bounds: Bounds = Bounds(-1e4, -1e4, 1e4, 1e4)) {
    
  /** Repulsion constant **/
  private val REPULSION = 1.2
  
  /** 'Gravity' constant pulling towards origin **/
  private val CENTER_GRAVITY = 1e-4
  
  /** Spring stiffness constant **/
  private val SPRING_COEFFICIENT = 0.0002
      
  /** Drag coefficient **/
  private val DRAG = 0.02
  
  /** Time-step increment **/
  private val TIMESTEP = 20
  
  /** Node velocity limit **/
  private val MAX_VELOCITY = 1.0
  
  /** Barnes-Hut Theta Threshold **/
  private val THETA = 0.8
    
  private def step(graph: SpringGraph): SpringGraph = {
    // Compute quad tree
    val quadtree = new QuadTree(graph.bounds, nodes.map(n => Body(n.state.pos, Some(n))))
 
    // Keep track of changing bounds
    var (minX, minY, maxX, maxY) = (Double.PositiveInfinity, Double.PositiveInfinity, Double.NegativeInfinity, Double.NegativeInfinity)
    
    // Compute node-based forces
    val updatedNodes = nodes.map(node => { 
      val force =
        applyBarnesHut(node, quadtree) +
        applyDrag(node) +
        attractToCenter(node)
        
      val acceleration = force / node.mass
      val velocity = node.state.velocity + acceleration * TIMESTEP
      val position = if (velocity.magnitude > MAX_VELOCITY)
                       node.state.pos + velocity.normalize * MAX_VELOCITY * TIMESTEP 
                     else
                       node.state.pos + velocity * TIMESTEP
                       
      minX = Math.min(minX, position.x)
      minY = Math.min(minY, position.y)
      maxX = Math.max(maxX, position.x)
      maxY = Math.max(maxY, position.y)
                       
      SpringGraphNode(node.id, node.label, node.mass, node.group, SpringGraphNodeState(position, velocity))
    })
    
    new SpringGraph(updatedNodes, edges, Bounds(minX, minY, maxX, maxY))
  }

  /**
   * Computes N-body repulsion for a node using the Barnes-Hut algorithm.
   */
  private def applyBarnesHut(node: SpringGraphNode, quadtree: QuadTree): Vector = {
  
    def apply(node: SpringGraphNode, quad: Quad): Vector = {
      val s = (quad.bounds.width + quad.bounds.height) / 2
      val d = (quad.center - node.state.pos).magnitude
      if (s/d > THETA) {
        // Nearby quad
        if (quad.children.isDefined) {
          quad.children.get.map(child => apply(node, child)).foldLeft(Vector(0,0))(_ + _)
        } else if (quad.body.isDefined) {
          val d = quad.body.get.pos - node.state.pos
          val distance = d.magnitude //+ 0.1 // avoid massive forces at small distances (and divide by zero)
          val direction = d.normalize
          
          if (quad.body.get.data.get.asInstanceOf[Node] != node) {
            direction * -1 * REPULSION / (distance * distance * 0.5)
          } else {
            Vector(0, 0)
          }
        } else {
          Vector(0,0)
        }
      } else {
        // Far-away quad
        val d = quad.center - node.state.pos
        val distance = d.magnitude
        val direction = d.normalize
        direction * -1 * REPULSION * quad.bodies / (distance * distance * 0.5)
      }
    }
    
    apply(node, quadtree.root)
  }
  
  /**
   * Computes drag force on a node.
   */
  private def applyDrag(node: SpringGraphNode): Vector = node.state.velocity * -1 * DRAG
  
  /**
   * Computes center gravity for a node.
   */
  private def attractToCenter(node: SpringGraphNode): Vector = node.state.pos.normalize * -1 * CENTER_GRAVITY
  
  /*
  private def applyHookesLaw(node: SpringGraphNode): Vector = {
    edges.foreach(spring => {  
      val d = if (spring.to.pos == spring.from.pos)
          Vector.random(0.1, spring.from.pos)
        else
          spring.to.pos - spring.from.pos

      val displacement = d.magnitude - spring.length
      val coeff = SPRING_COEFFICIENT * displacement / d.magnitude

      spring.from.acceleration += d * coeff * 0.5 / spring.from.mass
      spring.to.acceleration -= d * coeff * 0.5 / spring.to.mass
    })
  }
  */
  
  /**
   * Total energy of the graph.
   */
  lazy val totalEnergy = {
	nodes.map(node => {
	  val v = node.state.velocity.magnitude
	  0.5 * node.mass * v * v
	}).fold(0.0)(_ + _) 
  }
  
  def countEdges(node: Node) = {
    // TODO optimize!
    edges.count(edge => edge.from == node || edge.to == node)
  }
  
}




