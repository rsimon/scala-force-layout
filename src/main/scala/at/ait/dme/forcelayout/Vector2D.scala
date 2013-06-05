package at.ait.dme.forcelayout

import scala.util.Random

/**
 * A basic 2D vector, plus some convenience methods.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Vector2D(val x: Double, val y: Double ) {
  
  def add(v: Vector2D) = Vector2D(x + v.x, y + v.y)
  
  def +(v: Vector2D) = Vector2D.this.add(v)
  
  def substract(v: Vector2D) = Vector2D(x - v.x, y - v.y)
  
  def -(v: Vector2D) = Vector2D.this.substract(v)

  def multiply(n: Double) = Vector2D(x * n, y * n)
  
  def *(n: Double) = Vector2D.this.multiply(n)
  
  def divide(n: Double) = Vector2D(x / n, y /n)
  
  def /(n: Double) = Vector2D.this.divide(n)

  lazy val magnitude = Math.sqrt(x * x + y * y)  
  
  lazy val normalize = Vector2D.this.divide(Vector2D.this.magnitude)
  
}

object Vector2D {
  
  def random(r: Double = 1.0, center: Vector2D = Vector2D(0, 0)) = Vector2D(center.x + Random.nextDouble * r - r / 2, center.y + Random.nextDouble * r - r / 2)

}