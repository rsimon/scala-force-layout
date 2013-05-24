package at.ait.dme.forcelayout

import scala.util.Random
import at.ait.dme.forcelayout.quadtree.{ Body, QuadTree }
import at.ait.dme.forcelayout.quadtree.Quad

/**
 * A graph layout implementation based on a basic spring physics model. To a wide 
 * extent, this code is a port of the Springy JavaScript library (http://getspringy.com/) 
 * by Dennis Hotson. But it also mixes in some ideas from Andrei Kashcha's JavaScript 
 * library VivaGraphJS (https://github.com/anvaka/VivaGraphJS).
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class SpringGraph(val nodes: Seq[Node], val edges: Seq[Edge]) {

  /** Repulsion constant **/
  private val REPULSION = 100.0
  
  /** Spring stiffness constant **/
  private val STIFFNESS = 300.0
    
  /** Drag coefficient **/
  private val DRAG = 20.0
  
  /** Time-step increment **/
  private val TIMESTEP = 0.02 / Math.log(nodes.size)
  
  // TODO how can we change that to an immutable val?
  private var onComplete: Option[Int => Unit] = None
  
  // TODO how can we change that to an immutable val?
  private var onIteration: Option[Int => Unit] = None
  
  // TODO state! How can we change that to an immutable val?
  private var (minX, minY, maxX, maxY) = (-1.0, -1.0, 1.0, 1.0)
  
  /**
   * Adds an onComplete event handler.
   */
  def onComplete(callback: Int => Unit): SpringGraph = {
    onComplete = Some(callback)
    SpringGraph.this
  }
  
  /**
   * Adds an onIteration event handler
   */
  def onIteration(callback: Int => Unit): SpringGraph = {
    onIteration = Some(callback) 
    SpringGraph.this
  } 
 
  def doLayout(maxIterations: Int = 10000) = {
    var it = 0
    do { 
      iterate
      if (onIteration.isDefined)
        onIteration.get.apply(it)
      it += 1
    } while (getTotalEnergy() > nodes.size / 4 && it < maxIterations)
      
    if (onComplete.isDefined)
      onComplete.get.apply(it)
  }
  
  def getNearestNode(pt: Vector) = nodes.map(node => (node, (node.pos - pt).magnitude)).sortBy(_._2).head._1
    
  private def iterate = {
    // Compute forces
    applyBarnesHut
    // applyCoulombsLaw
    applyHookesLaw
    applyDrag
    attractToCenter
    
    // Reset bounds
    minX = Int.MaxValue
    minY = Int.MaxValue
    maxX = Int.MinValue
    maxY = Int.MinValue
    
    // Apply forces
    nodes.foreach(node => {
      node.velocity += node.acceleration * TIMESTEP
      node.acceleration = Vector(0, 0)   
      node.pos += node.velocity * TIMESTEP
      
      // Update bounds
      minX = Math.min(minX, node.pos.x)
      minY = Math.min(minY, node.pos.y)
      maxX = Math.max(maxX, node.pos.x)
      maxY = Math.max(maxY, node.pos.y)
    })
  }
  
  private def applyCoulombsLaw = {
    nodes.foreach(nodeA => {
      nodes.filter(_ != nodeA).foreach(nodeB => {
        val d = nodeB.pos - nodeA.pos
        val distance = d.magnitude + 0.1 // avoid massive forces at small distances (and divide by zero)
        val direction = d.normalize  
        nodeA.acceleration -= direction * REPULSION / (distance * distance * 0.5 * nodeA.mass) 
        nodeB.acceleration += direction * REPULSION / (distance * distance * 0.5 * nodeB.mass)
      })
    })
  }
  
  private def applyBarnesHut = {
    val THETA = 0.5
    val quadtree = new QuadTree(Bounds(minX, minY, maxX, maxY), nodes.map(n => Body(n.pos, Some(n))))
    
    def apply(node: Node, quad: Quad): Unit = {
      val s = (quad.bounds.width + quad.bounds.height) / 2
      val d = (quad.center - node.pos).magnitude
      if (s/d > THETA) {
        // Nearby quad
        if (quad.children.isDefined) {
          quad.children.get.foreach(child => apply(node, child))
        } else if (quad.body.isDefined) {
          val d = quad.body.get.pos - node.pos
          val distance = d.magnitude + 0.1 // avoid massive forces at small distances (and divide by zero)
          val direction = d.normalize
          
          if (quad.body.get.data.get.asInstanceOf[Node] != node)
            node.acceleration -= direction * REPULSION / (distance * distance * 0.5 * node.mass)
        }
      } else {
        // Far-away quad
        val d = quad.center - node.pos
        val distance = d.magnitude + 0.1 // avoid massive forces at small distances (and divide by zero)
        val direction = d.normalize          
        node.acceleration -= direction * REPULSION * quad.bodies / (distance * distance * 0.5 * node.mass)
      }
    }
    
    nodes.foreach(node => apply(node, quadtree.root))
  }
  
  private def applyHookesLaw = {
    edges.foreach(spring => {  
      val d = if (spring.to.pos == spring.from.pos)
          Vector.random(0.1, spring.from.pos)
        else
          spring.to.pos - spring.from.pos

      val displacement = d.magnitude - spring.length
      spring.from.acceleration += d.normalize * STIFFNESS * displacement * 0.5 / spring.from.mass
      spring.to.acceleration -= d.normalize * STIFFNESS * displacement * 0.5 / spring.to.mass
    })
  }
  
  private def applyDrag = nodes.foreach(node => node.acceleration -= node.velocity * DRAG)
  
  private def attractToCenter = {
    nodes.foreach(node => node.acceleration -= node.pos * REPULSION / (10.0 * node.mass))
  }
  
  def getTotalEnergy() = {
	nodes.map(node => {
	  val v = node.velocity.magnitude
	  0.5 * node.mass * v * v
	}).foldLeft(0.0)(_ + _) 
  }
  
  def getBounds() = (minX, minY, maxX, maxY)
  
}

/** A node in the graph **/
case class Node(id: String, label: String, weight: Double = 1.0, group: Int = 0) {
  
  val mass = weight
  
  // TODO I'd really like to find a way around maintaining mutable state...
  var pos = Vector.random()
  var acceleration = Vector(0, 0)
  var velocity = Vector(0, 0)
  
}

/** An edge in the graph **/
case class Edge(val from: Node, val to: Node, weight: Double = 1.0) {
  
  val length = 1 / weight
  
}
