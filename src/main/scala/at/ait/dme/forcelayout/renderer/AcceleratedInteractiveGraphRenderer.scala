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
import at.ait.dme.forcelayout.Vector
import java.awt.Graphics2D
import java.awt.BasicStroke
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D

class AcceleratedInteractiveGraphRenderer(graph: SpringGraph) extends Canvas {

  private var offscreenImage: Image = null
  private var offscreenGraphics: Graphics2D = null
  private var offscreenDimension: Dimension = null

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
  
  private var currentZoom = 1.0
  private var currentXOffset = 0.0
  private var currentYOffset = 0.0
  private var lastMousePos = new Point(0, 0)
  
  private var selectedNode: Option[Node] = None
  
  graph.onIteration(it => repaint())
  graph.onComplete(it => { println("completed in " + it + " iterations"); repaint() })
  
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
      val coords = toGraphCoords(graph, size.getWidth.toInt, size.getHeight.toInt, e.getPoint)
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
    
    if(offscreenImage == null || !currentSize.equals(offscreenDimension)) {
      offscreenImage = createImage(currentSize.width, currentSize.height)
      offscreenGraphics = offscreenImage.getGraphics().asInstanceOf[Graphics2D]
      offscreenDimension = currentSize
    }
  
    offscreenGraphics.setPaint(Color.WHITE)
    offscreenGraphics.fill(g.getClipBounds)
    
    val c = computeScale(graph, bounds.getWidth.toInt, bounds.getHeight.toInt) * currentZoom
    val (dx, dy) = (bounds.getWidth / 2 + currentXOffset, bounds.getHeight / 2 + currentYOffset)
    
    graph.edges.foreach(e => {
      val from = (c * e.from.pos.x + dx, c * e.from.pos.y + dy)
      val to = (c * e.to.pos.x + dx, c * e.to.pos.y + dy)
      val width = Math.max(2, Math.min(8, e.weight)).toInt / 2
      
      offscreenGraphics.setStroke(new BasicStroke(width));
      offscreenGraphics.setColor(new Color(128, 128, 128, 128))     
      offscreenGraphics.draw(new Line2D.Double(from._1, from._2, to._1, to._2))
    })
  
    graph.nodes.foreach(v => {
      val size = Math.log(v.mass * 2) + 1
      val px = c * v.pos.x + dx - size / 2
      val py = c * v.pos.y + dy - size / 2
      
      offscreenGraphics.setPaint(palette(v.group % palette.size))
      offscreenGraphics.fill(new Ellipse2D.Double(px, py, size, size))
    })
    
    if (selectedNode.isDefined) {
      val n = selectedNode.get
      val size = Math.log(n.mass) + 7
      val px = c * n.pos.x + dx - size / 2
      val py = c * n.pos.y + dy - size / 2
      
      offscreenGraphics.setColor(Color.BLACK);
      offscreenGraphics.draw(new Ellipse2D.Double(px, py, size, size))  
      offscreenGraphics.drawString(n.label, px.toInt + 5, py.toInt - 2)
    }
  
    // paint back buffer to main graphics
    g.drawImage(offscreenImage, 0, 0, this);
  }
 
  override def update(g: Graphics) {
    paint(g)
  }
  
  private def computeScale(graph: SpringGraph, width: Int, height: Int) = {
    val (minX, minY, maxX, maxY) = graph.getBounds
    Math.min(width / 2 * 0.9 / Math.max(maxX, Math.abs(minX)), height / 2 * 0.9 / Math.max(maxY, Math.abs(minY)))    
  }
        
  def toGraphCoords(graph: SpringGraph, width: Int, height: Int, pt: Point): Vector = {
    val c = computeScale(graph, width, height)
    val gx = (pt.x - width / 2 - currentXOffset) / (c * currentZoom)
    val gy = (pt.y - height / 2 - currentYOffset) / (c * currentZoom) 
    Vector(gx, gy)
  }
  
  def start = graph.doLayout()
  
}