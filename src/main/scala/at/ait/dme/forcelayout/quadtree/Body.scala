package at.ait.dme.forcelayout.quadtree

import at.ait.dme.forcelayout.Vector2D

/**
 * A body in the quadtree.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Body[T](pos: Vector2D, data: Option[T] = None)