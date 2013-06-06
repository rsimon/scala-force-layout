package at.ait.dme.forcelayout.renderer

import java.awt._
import java.awt.geom.Ellipse2D
import at.ait.dme.forcelayout.SpringGraph
import at.ait.dme.forcelayout.Vector2D
import at.ait.dme.forcelayout.Node
import at.ait.dme.forcelayout.ImmutableNode
import at.ait.dme.forcelayout.Edge
import at.ait.dme.forcelayout.ImmutableEdge

private[renderer] trait GraphRenderer {

  private val palette = Seq(
      new Color(31, 119, 180),
      new Color(255, 127, 14),
      new Color(44, 160, 44),
      new Color(214, 39, 40),
      new Color(148, 103, 189),
      new Color(140, 86, 75),
      new Color(227, 119, 194),
      new Color(127, 127, 127),
      new Color(188, 189, 34),
      new Color(23, 190, 207))

  private var lastCompletion: Long = System.currentTimeMillis
  
  def render(g2d: Graphics2D, graph: SpringGraph, nodes: Seq[ImmutableNode], edges: Seq[ImmutableEdge], width: Int, height: Int, selectedNode: Option[Node] = None, offsetX: Double = 0.0, offsetY: Double = 0.0, zoom: Double = 1.0): Unit = {
    g2d.setColor(Color.WHITE)
    g2d.fillRect(0, 0, width, height)

    val c = computeScale(graph, width, height) * zoom
    val (dx, dy) = (width / 2 + offsetX, height / 2 + offsetY)
    
    edges.foreach(e => {
      val from = (c * e.from.state.pos.x + dx, c * e.from.state.pos.y + dy)
      val to = (c * e.to.state.pos.x + dx, c * e.to.state.pos.y + dy)
      val width = Math.min(4, Math.max(2, Math.min(8, e.weight)).toInt / 2)
    
      g2d.setStroke(new BasicStroke(width));
      g2d.setColor(new Color(198, 198, 198, 198))  
      g2d.drawLine(from._1.toInt, from._2.toInt, to._1.toInt, to._2.toInt)
    })
    
    nodes.map(n => (c * n.state.pos.x + dx, c * n.state.pos.y + dy, n.mass, n.group))
      .filter(pt => pt._1 > 0 && pt._2 > 0)
      .filter(pt => pt._1 <= width && pt._2 <= height)
      .foreach(pt => {      
        val size = Math.max(3, Math.min(10, Math.log(pt._3) + 1))
        g2d.setColor(palette(pt._4 % palette.size))
        g2d.fill(new Ellipse2D.Double(pt._1 - size / 2, pt._2 - size / 2, size, size))
      })
      
    if (selectedNode.isDefined) {
      val n = selectedNode.get
      val size = Math.log(n.mass) + 7
      val px = c * n.state.pos.x + dx - size / 2
      val py = c * n.state.pos.y + dy - size / 2
      
      g2d.setColor(Color.BLACK);
      g2d.draw(new Ellipse2D.Double(px, py, size, size))  
      g2d.drawString(n.label, px.toInt + 5, py.toInt - 2)
    }
    
    g2d.setColor(Color.BLACK)
    g2d.drawString("%.1f".format(1000.0 / (System.currentTimeMillis - lastCompletion)) + "FPS", 2, 12)
    
    lastCompletion = System.currentTimeMillis
  }
  
  private def computeScale(graph: SpringGraph, width: Int, height: Int) = {
    val bounds = graph.bounds
    Math.min(width / 2 * 0.9 / Math.max(bounds.maxX, Math.abs(bounds.minX)), height / 2 * 0.9 / Math.max(bounds.maxY, Math.abs(bounds.minY)))    
  }
        
  def toGraphCoords(graph: SpringGraph, pt: Point, width: Int, height: Int, offsetX: Double = 0.0, offsetY: Double = 0.0, zoom: Double = 1.0): Vector2D = {
    val c = computeScale(graph, width, height)
    val gx = (pt.x - width / 2 - offsetX) / (c * zoom)
    val gy = (pt.y - height / 2 - offsetY) / (c * zoom) 
    Vector2D(gx, gy)
  }
  
}