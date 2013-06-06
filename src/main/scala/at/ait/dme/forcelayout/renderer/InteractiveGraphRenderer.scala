package at.ait.dme.forcelayout.renderer

import java.awt._
import java.awt.event._
import at.ait.dme.forcelayout.{ Node, SpringGraph, Vector2D }
import javax.swing.JLabel

class InteractiveGraphRenderer(graph: SpringGraph) extends JLabel with GraphRenderer {
  
  private var currentZoom = 1.0
  private var currentXOffset = 0.0
  private var currentYOffset = 0.0
  private var lastMousePos = new Point(0, 0)
  
  private var selectedNode: Option[Node] = None
  
  addMouseMotionListener(new MouseAdapter() {
    override def mouseDragged(e: MouseEvent) {
      currentXOffset += e.getX - lastMousePos.getX
      currentYOffset += e.getY - lastMousePos.getY
      lastMousePos = e.getPoint
      repaint()
    }
  })
  
  addMouseListener(new MouseAdapter() {
    override def mouseClicked(e: MouseEvent) {
      val size = getSize()
      val coords = toGraphCoords(graph, e.getPoint, size.getWidth.toInt, size.getHeight.toInt, currentXOffset, currentYOffset, currentZoom)
      selectedNode = Some(graph.getNearestNode(coords))
      repaint() 
    }
    
    override def mousePressed(e: MouseEvent) = lastMousePos = e.getPoint 
  })
  
  addMouseWheelListener(new MouseWheelListener() {
    override def mouseWheelMoved(e: MouseWheelEvent) {
      // TODO make zooming sensitive to mouse position
      if (e.getWheelRotation() > 0)
        currentZoom /= 1.1
      else
        currentZoom *= 1.1
        
      repaint()
    }
  })
  
  override def paintComponent(g: Graphics) = {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    
    val bounds = g.getClipBounds
    // render(g2d, graph, bounds.getWidth.toInt, bounds.getHeight.toInt, selectedNode, currentXOffset, currentYOffset, currentZoom)
  }
  
  def start = graph.doLayout(onComplete = ((it, nodes, edges) => { println("completed in " + it + " iterations"); repaint() }),
                             onIteration = ((it, nodes, edges) => repaint()))  

}

