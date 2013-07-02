package at.ait.dme.forcelayout.quadtree

import at.ait.dme.forcelayout.{ Bounds, Vector2D }

/**
 * A quad in the quadtree.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Quad[T](
    bounds: Bounds, 
    center: Vector2D,
    bodies: Int,
    body: Option[Body[T]] = None, 
    children: Option[Seq[Quad[T]]] = None)