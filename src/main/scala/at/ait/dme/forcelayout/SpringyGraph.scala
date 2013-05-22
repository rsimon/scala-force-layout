package at.ait.dme.forcelayout

import scala.util.Random

/**
 * A graph layout implementation based on a basic spring physics model. To a wide 
 * extent, this code is a port of the Springy JavaScript library (http://getspringy.com/) 
 * by Dennis Hotson. But it also mixes in some ideas from Andrei Kashcha's JavaScript 
 * library VivaGraphJS (https://github.com/anvaka/VivaGraphJS).
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
class SpringyGraph(val nodes: Seq[Node], val edges: Seq[Edge]) {

  /** Repulsion constant **/
  private val REPULSION = 50.0
  
  /** Spring stiffness constant **/
  private val STIFFNESS = 200.0
    
  /** Drag coefficient **/
  private val DRAG = 10.0
  
  /** Time-step increment **/
  private val TIMESTEP = 0.01
  
  // TODO how can we change that to an immutable val?
  private var onComplete: Option[Int => Unit] = None
  
  // TODO how can we change that to an immutable val?
  private var onIteration: Option[Int => Unit] = None
  
  // TODO state! How can we change that to an immutable val?
  private var (minX, minY, maxX, maxY) = (0.0, 0.0, 0.0, 0.0)
  
  /**
   * Adds an onComplete event handler.
   */
  def onComplete(callback: Int => Unit): SpringyGraph = {
    onComplete = Some(callback)
    this
  }
  
  /**
   * Adds an onIteration event handler
   */
  def onIteration(callback: Int => Unit): SpringyGraph = {
    onIteration = Some(callback) 
    this
  } 
 
  def doLayout(maxIterations: Int = 10000) = {
    var it = 0
    do { 
      iterate
      if (onIteration.isDefined)
        onIteration.get.apply(it)
      it += 1
    } while (getTotalEnergy() > 1 && it < maxIterations)
      
    if (onComplete.isDefined)
      onComplete.get.apply(it)
  }
    
  private def iterate = {
    // Compute forces
    applyCoulombsLaw
    applyHookesLaw
    applyDrag
    
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
  
  private def applyHookesLaw = {
    edges.foreach(spring => {  
      val d = spring.to.pos - spring.from.pos
      val displacement = d.magnitude - spring.length
      spring.from.acceleration += d.normalize * STIFFNESS * displacement * 0.5 / spring.from.mass
      spring.to.acceleration -= d.normalize * STIFFNESS * displacement * 0.5 / spring.to.mass
    })
  }
  
  private def applyDrag = nodes.foreach(node => node.acceleration -= node.velocity * DRAG)
  
  private def getTotalEnergy() = {
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
