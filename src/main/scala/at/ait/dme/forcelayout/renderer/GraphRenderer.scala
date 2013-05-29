package at.ait.dme.forcelayout.renderer

import java.awt._
import at.ait.dme.forcelayout.{ Node, Edge, SpringGraph, Vector }
import java.awt.geom.Ellipse2D

private[renderer] trait GraphRenderer {

  private val palette = Seq(
      new Color(31, 119, 180),      new Color(255, 127, 14),      new Color(44, 160, 44),      new Color(214, 39, 40),      new Color(148, 103, 189),      new Color(140, 86, 75),      new Color(227, 119, 194),      new Color(127, 127, 127),      new Color(188, 189, 34),      new Color(23, 190, 207))

  private var lastCompletion: Long = System.currentTimeMillis
  
  def render(g2d: Graphics2D, graph: SpringGraph, width: Int, height: Int, selectedNode: Option[Node] = None, offsetX: Double = 0.0, offsetY: Double = 0.0, zoom: Double = 1.0): Unit = {
    g2d.setColor(Color.WHITE)
    g2d.fillRect(0, 0, width, height)

    val c = computeScale(graph, width, height) * zoom
    val (dx, dy) = (width / 2 + offsetX, height / 2 + offsetY)
    
    graph.edges.foreach(e => {
      val from = (c * e.from.pos.x + dx, c * e.from.pos.y + dy)
      val to = (c * e.to.pos.x + dx, c * e.to.pos.y + dy)
      val width = Math.max(2, Math.min(8, e.weight)).toInt / 2
    
      g2d.setStroke(new BasicStroke(width));
      g2d.setColor(new Color(198, 198, 198, 198))  
      g2d.drawLine(from._1.toInt, from._2.toInt, to._1.toInt, to._2.toInt)
    })

    graph.nodes.foreach(v => {
      val size = 7
      val px = c * v.pos.x + dx - size / 2
      val py = c * v.pos.y + dy - size / 2
      
      g2d.setColor(palette(v.group % palette.size))
      g2d.fill(new Ellipse2D.Double(px, py, size, size))
    })
    
    if (selectedNode.isDefined) {
      val n = selectedNode.get
      val size = Math.log(n.mass) + 7
      val px = c * n.pos.x + dx - size / 2
      val py = c * n.pos.y + dy - size / 2
      
      g2d.setColor(Color.BLACK);
      g2d.draw(new Ellipse2D.Double(px, py, size, size))  
      g2d.drawString(n.label, px.toInt + 5, py.toInt - 2)
    }
    
    g2d.setColor(Color.BLACK)
    g2d.drawString("%.1f".format(1000.0 / (System.currentTimeMillis - lastCompletion)) + "FPS", 2, 12)
    
    lastCompletion = System.currentTimeMillis
  }
  
  private def computeScale(graph: SpringGraph, width: Int, height: Int) = {
    val (minX, minY, maxX, maxY) = graph.getBounds
    Math.min(width / 2 * 0.9 / Math.max(maxX, Math.abs(minX)), height / 2 * 0.9 / Math.max(maxY, Math.abs(minY)))    
  }
        
  def toGraphCoords(graph: SpringGraph, pt: Point, width: Int, height: Int, offsetX: Double = 0.0, offsetY: Double = 0.0, zoom: Double = 1.0): Vector = {
    val c = computeScale(graph, width, height)
    val gx = (pt.x - width / 2 - offsetX) / (c * zoom)
    val gy = (pt.y - height / 2 - offsetY) / (c * zoom) 
    Vector(gx, gy)
  }
  
}