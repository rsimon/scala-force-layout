package at.ait.dme.forcelayout.renderer

import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Color
import java.awt.BasicStroke
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D
import java.awt.Point
import java.awt.geom.Point2D
import at.ait.dme.forcelayout.SpringGraph
import at.ait.dme.forcelayout.Vector2D

/**
 * A graph drawing utility. 
 */
object ImageRenderer extends GraphRenderer {
      
  def drawGraph(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean = false): BufferedImage = {
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    // render(g, graph, width, height)
    
    image
  }

}