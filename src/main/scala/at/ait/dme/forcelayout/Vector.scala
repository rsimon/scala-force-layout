package at.ait.dme.forcelayout

import scala.util.Random

/**
 * A basic 2D vector, plus some convenience methods.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Vector(val x: Double, val y: Double ) {
  
  def add(v: Vector) = Vector(x + v.x, y + v.y)
  
  def +(v: Vector) = this.add(v)
  
  def substract(v: Vector) = Vector(x - v.x, y - v.y)
  
  def -(v: Vector) = this.substract(v)

  def multiply(n: Double) = Vector(x * n, y * n)
  
  def *(n: Double) = this.multiply(n)
  
  def divide(n: Double) = Vector(x / n, y /n)
  
  def /(n: Double) = this.divide(n)

  lazy val magnitude = Math.sqrt(x * x + y * y)  
  
  lazy val normalize = this.divide(this.magnitude)
  
}

object Vector {
  
  def random(r: Double = 1.0, center: Vector = Vector(0, 0)) = Vector(center.x + Random.nextDouble * r - r / 2, center.y + Random.nextDouble * r - r / 2)

}