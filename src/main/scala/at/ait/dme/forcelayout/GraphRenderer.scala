package at.ait.dme.forcelayout

import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Color
import java.awt.BasicStroke
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D
import scala.util.Random
import java.awt.Point

/**
 * A graph drawing utility. 
 */
object GraphRenderer {
      
  private val colorScale = Seq(
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
      
  private lazy val palette = colorScale ++ colorScale ++ colorScale    
  
  def toGraphCoords(graph: SpringGraph, width: Int, height: Int, pt: Point): Vector = {
    // Scale conversion factors
    // TODO eliminate code duplication!
    val (dx, dy) = (width / 2, height / 2)
    val (minX, minY, maxX, maxY) = graph.getBounds
    val c = Math.min(dx * 0.9 / Math.max(maxX, Math.abs(minX)), dy * 0.9 / Math.max(maxY, Math.abs(minY)))
    
    val gx = (pt.x - dx) / c
    val gy = (pt.y - dy) / c   
    Vector(gx, gy)
  }
  
  def drawGraph(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean = false): BufferedImage = {
    // Set up image canvas
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    g.setPaint(Color.WHITE)
    image.getGraphics.fillRect(0, 0, width, height)
    
    // Scale conversion factors
    val (dx, dy) = (width / 2, height / 2)
    val (minX, minY, maxX, maxY) = graph.getBounds
    val c = Math.min(dx * 0.9 / Math.max(maxX, Math.abs(minX)), dy * 0.9 / Math.max(maxY, Math.abs(minY)))
    
    // Draw edges
    graph.edges.foreach(e => {
      val from = (c * e.from.pos.x + dx, c * e.from.pos.y + dy)
      val to = (c * e.to.pos.x + dx, c * e.to.pos.y + dy)
 
      // TODO clean up
      val width = Math.max(2, Math.min(8, e.weight)).toInt / 2
      
      g.setStroke(new BasicStroke(width));
      g.setColor(new Color(128, 128, 128, 128))     
      g.draw(new Line2D.Double(from._1, from._2, to._1, to._2))
    })
  
    graph.nodes.foreach(v => {
      val size = (v.weight + 3) * 2
      val px = c * v.pos.x + dx - size / 2
      val py = c * v.pos.y + dy - size / 2
      
      g.setPaint(palette(v.group))
      g.fill(new Ellipse2D.Double(px, py, size, size))
      g.setColor(Color.WHITE);
      g.draw(new Ellipse2D.Double(px, py, size, size))
      
      if (showLabels) {
        g.setColor(Color.BLACK);
        g.drawString(v.label, px.toInt + 8, py.toInt)
      }
    })  
    
    image
  }

}