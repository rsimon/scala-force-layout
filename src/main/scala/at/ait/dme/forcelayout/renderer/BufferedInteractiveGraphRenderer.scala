package at.ait.dme.forcelayout.renderer

import java.awt.Canvas
import java.awt.Image
import java.awt.Graphics
import java.awt.Dimension
import java.awt.Color
import at.ait.dme.forcelayout.SpringGraph
import java.awt.Point
import at.ait.dme.forcelayout.Node
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelListener
import java.awt.event.MouseWheelEvent
import at.ait.dme.forcelayout.Vector2D
import java.awt.Graphics2D
import java.awt.BasicStroke
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D
import java.awt.RenderingHints
import java.awt.GraphicsEnvironment
import at.ait.dme.forcelayout.ImmutableNode
import at.ait.dme.forcelayout.Edge
import at.ait.dme.forcelayout.ImmutableEdge

class BufferedInteractiveGraphRenderer(graph: SpringGraph) extends Canvas with GraphRenderer {

  private var offscreenImage: Image = null
  private var offscreenGraphics: Graphics2D = null
  private var offscreenDimension: Dimension = null
  
  private var currentZoom = 1.0
  private var currentXOffset = 0.0
  private var currentYOffset = 0.0
  private var lastMousePos = new Point(0, 0)
  
  private var selectedNode: Option[Node] = None
  
  private var currentGraph: (Seq[ImmutableNode], Seq[ImmutableEdge]) = null 
  
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
  
  override def paint(g : Graphics) {
    val currentSize = getSize
    val (width, height) = (currentSize.getWidth.toInt, currentSize.getWidth.toInt)    
    val gfxConfig = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration
    
    if(offscreenImage == null || !currentSize.equals(offscreenDimension)) {
      if (offscreenImage != null)
        offscreenGraphics.dispose
        
      offscreenImage = gfxConfig.createCompatibleImage(currentSize.width, currentSize.height) 
      offscreenGraphics = offscreenImage.getGraphics.asInstanceOf[Graphics2D]
      offscreenDimension = currentSize
      offscreenGraphics.setRenderingHint(
                      RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON)
      offscreenGraphics.setRenderingHint(
                      RenderingHints.KEY_FRACTIONALMETRICS,
                      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
    }
  
    if (currentGraph != null)
      render(offscreenGraphics, graph, currentGraph._1, currentGraph._2, currentSize.getWidth.toInt, currentSize.getHeight.toInt, selectedNode, currentXOffset, currentYOffset, currentZoom)
    g.drawImage(offscreenImage, 0, 0, this)
  }
 
  override def update(g: Graphics) = paint(g)

  def start = graph.doLayout(onComplete = ((it, nodes, edges) => { println("completed in " + it + " iterations"); repaint() }),
                             onIteration = ((it, nodes, edges) => { currentGraph = (nodes, edges); repaint() }))  
}