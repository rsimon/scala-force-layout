package at.ait.dme.forcelayout.renderer

import java.awt.{ Graphics2D, RenderingHints }
import java.awt.image.BufferedImage

import at.ait.dme.forcelayout.SpringGraph

/**
 * A graph drawing utility. 
 */
object ImageRenderer extends GraphRenderer {
  
  def drawGraph(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean = false) =
    draw(graph, width, height, showLabels, None, None)
  
  def drawGraph(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean, nodePainter: (Seq[Node2D], Graphics2D) => Unit) = 
    draw(graph, width, height, showLabels, Some(nodePainter), None)
  
  def drawGraph(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean, nodePainter: (Seq[Node2D], Graphics2D) => Unit, edgePainter: (Seq[Edge2D], Graphics2D) => Unit) =
    draw(graph, width, height, showLabels, Some(nodePainter), Some(edgePainter))
      
  private def draw(graph: SpringGraph, width: Int, height: Int, showLabels: Boolean, 
      nodePainter: Option[(Seq[Node2D], Graphics2D) => Unit] = None,
      edgePainter: Option[(Seq[Edge2D], Graphics2D) => Unit]): BufferedImage = {
    
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = image.getGraphics.asInstanceOf[Graphics2D]
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)

    if (nodePainter.isDefined)
      setNodePainter(nodePainter.get)
      
    if (edgePainter.isDefined)
      setEdgePainter(edgePainter.get)
      
    render(g, graph, width, height)
    
    image
  }

}