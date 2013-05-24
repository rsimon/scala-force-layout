package at.ait.dme.forcelayout

case class Bounds(minX: Double, minY: Double, maxX: Double, maxY: Double) {
  
  lazy val width = maxX - minX
  
  lazy val height = maxY - minY
  
  lazy val center = Vector((minX + maxX) / 2, (minY + maxY) / 2) 
  
  def contains(pt: Vector) = {
    if (pt.x < minX)
      false
    else if (pt.x > maxX)
      false
    else if (pt.y < minY)
      false
    else if (pt.y > maxY)
      false
    else
      true
  }
  
}