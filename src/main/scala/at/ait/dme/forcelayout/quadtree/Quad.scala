package at.ait.dme.forcelayout.quadtree

import at.ait.dme.forcelayout.{ Bounds, Vector2D }

case class Quad(
    bounds: Bounds, 
    center: Vector2D,
    bodies: Int,
    body: Option[Body] = None, 
    children: Option[Seq[Quad]] = None)