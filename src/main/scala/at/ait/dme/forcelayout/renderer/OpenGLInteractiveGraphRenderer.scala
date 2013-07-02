package at.ait.dme.forcelayout.renderer

import java.awt.{ Canvas, Graphics2D, Point, RenderingHints }
import at.ait.dme.forcelayout.{ Node, SpringGraph }
import java.awt.image.BufferStrategy
import java.awt.event.{ MouseAdapter, MouseEvent, MouseWheelListener, MouseWheelEvent }

class OpenGLInteractiveGraphRenderer(graph: SpringGraph) extends Canvas with GraphRenderer {
  
  System.setProperty("sun.java2d.opengl", "True")
  System.setProperty("sun.java2d.ddscale", "True")
  System.setProperty("sun.java2d.translaccel", "True")  
    
  private var currentZoom = 1.0
  private var currentXOffset = 0.0
  private var currentYOffset = 0.0
  private var lastMousePos = new Point(0, 0)
  
  private var selectedNode: Option[Node] = None
  
  private var strategy: BufferStrategy = null
  
  addMouseMotionListener(new MouseAdapter() {
    override def mouseDragged(e: MouseEvent) {
      currentXOffset += e.getX - lastMousePos.getX
      currentYOffset += e.getY - lastMousePos.getY
      lastMousePos = e.getPoint
      doPaint(strategy)
    }
  })
  
  addMouseListener(new MouseAdapter() {
    override def mouseClicked(e: MouseEvent) {
      val size = getSize()
      val coords = toGraphCoords(graph, e.getPoint, size.getWidth.toInt, size.getHeight.toInt, currentXOffset, currentYOffset, currentZoom)
      selectedNode = Some(graph.getNearestNode(coords))
      doPaint(strategy)
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
        
      doPaint(strategy)
    }
  })
  
  def start = {
    createBufferStrategy(2)
    strategy = getBufferStrategy
    graph.doLayout(onComplete = (it => { println("completed in " + it + " iterations"); doPaint(strategy) }),
                   onIteration = (it => doPaint(strategy))) 
  }

  def doPaint(strategy: BufferStrategy): Unit = {
    val g2d = strategy.getDrawGraphics.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    
    val bounds = getSize
    render(g2d, graph, bounds.getWidth.toInt, bounds.getHeight.toInt, selectedNode, currentXOffset, currentYOffset, currentZoom)
    g2d.dispose
    strategy.show
  }

}

