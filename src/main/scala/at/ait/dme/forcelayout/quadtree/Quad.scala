package at.ait.dme.forcelayout.quadtree

import at.ait.dme.forcelayout.{ Bounds, Vector }

case class Quad(
    bounds: Bounds, 
    center: Vector,
    bodies: Int,
    body: Option[Body] = None, 
    children: Option[Seq[Quad]] = None)