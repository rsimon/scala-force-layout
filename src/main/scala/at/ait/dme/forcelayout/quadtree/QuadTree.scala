package at.ait.dme.forcelayout.quadtree

import at.ait.dme.forcelayout.{ Bounds, Vector }

class QuadTree(bounds: Bounds, bodies: Seq[Body]) {

  import QuadTree._
  
  val root = build(bounds, bodies)
  
  def build(bounds: Bounds, bodies: Seq[Body]): Quad = {
    if (bodies.isEmpty) {
      Quad(bounds, bounds.center, 0)
    } else if (bodies.size == 1) {
      val body = bodies.head
      Quad(bounds, body.pos, 1, Some(bodies.head))
    } else {
      val children = subdivideBounds(bounds)
        .map(subbounds => build(subbounds, clipBodies(bodies, subbounds)))
      Quad(bounds, computeCenter(bodies), bodies.size, None, Some(children))
    }
  }  

}

object QuadTree {
  
  def subdivideBounds(bounds: Bounds) = Seq(
    Bounds(bounds.minX, bounds.minY + bounds.height / 2, bounds.minX + bounds.width / 2, bounds.maxY),
    Bounds(bounds.minX + bounds.width / 2, bounds.minY + bounds.height / 2, bounds.maxX, bounds.maxY),
    Bounds(bounds.minX + bounds.width / 2, bounds.minY, bounds.maxX, bounds.minY + bounds.height / 2),
    Bounds(bounds.minX, bounds.minY, bounds.minX + bounds.width / 2, bounds.minY + bounds.height / 2))
  
  def clipBodies(bodies: Seq[Body], bounds: Bounds) = bodies.filter(b => bounds.contains(b.pos))
  
  def computeCenter(bodies: Seq[Body]) = Vector(0,0)
  
}