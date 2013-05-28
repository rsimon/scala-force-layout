package at.ait.dme.forcelayout

import javax.swing.JLabel
import java.awt.Graphics
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import java.awt.Graphics2D
import java.awt.event.MouseAdapter
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelListener
import java.awt.RenderingHints
import java.awt.Point
import java.awt.geom.Point2D
import java.awt.BasicStroke
import java.awt.geom.Line2D
import java.awt.geom.Ellipse2D
import java.awt.GraphicsEnvironment
import java.awt.image.VolatileImage
import javax.swing.JFrame

class InteractiveGraphRenderer(graph: SpringGraph) extends JLabel {
  
  private val USE_BUFFER = false
  
  private val backBuffer = new BufferedImage(4096, 4096, BufferedImage.TYPE_INT_RGB)
  private val buffer = backBuffer.getGraphics.asInstanceOf[Graphics2D]
  buffer.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON)
  buffer.setRenderingHint(
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON)
  
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
  
  override def paintComponent(g: Graphics) = {  
    val g2d = if (USE_BUFFER) {
                  buffer
               } else {
                  val g2d = g.asInstanceOf[Graphics2D]
                  g2d.setRenderingHint(
                      RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON)
                  g2d.setRenderingHint(
                      RenderingHints.KEY_FRACTIONALMETRICS,
                      RenderingHints.VALUE_FRACTIONALMETRICS_ON)
                  g2d
               }

    val bounds = g.getClipBounds
    g2d.setPaint(Color.WHITE)
    g2d.fill(bounds)
    
    val c = computeScale(graph, bounds.getWidth.toInt, bounds.getHeight.toInt) * currentZoom
    val (dx, dy) = (bounds.getWidth / 2 + currentXOffset, bounds.getHeight / 2 + currentYOffset)
    
    graph.edges.foreach(e => {
      val from = (c * e.from.pos.x + dx, c * e.from.pos.y + dy)
      val to = (c * e.to.pos.x + dx, c * e.to.pos.y + dy)
      val width = Math.max(2, Math.min(8, e.weight)).toInt / 2
      
      g2d.setStroke(new BasicStroke(width));
      g2d.setColor(new Color(128, 128, 128, 128))     
      g2d.draw(new Line2D.Double(from._1, from._2, to._1, to._2))
    })
  
    /*
    graph.nodes.foreach(v => {
      val size = Math.log(v.mass) * 1.5 + 4
      // val size = (graph.countEdges(v) / 3) + 6
      val px = c * v.pos.x + dx - size / 2
      val py = c * v.pos.y + dy - size / 2
      
      g2d.setPaint(palette(v.group % palette.size))
      g2d.fill(new Ellipse2D.Double(px, py, size, size))
      // g2d.setColor(Color.WHITE);
      // g2d.draw(new Ellipse2D.Double(px, py, size, size))
    })
    */
    
    if (selectedNode.isDefined) {
      val n = selectedNode.get
      val size = (n.weight + 3) * 2
      // val size = (graph.countEdges(n) / 3) + 6
      val px = c * n.pos.x + dx - size / 2
      val py = c * n.pos.y + dy - size / 2
      
      g2d.setColor(Color.BLACK);
      g2d.draw(new Ellipse2D.Double(px, py, size, size))  
      g2d.drawString(n.label, px.toInt + 5, py.toInt - 2)
    }
    
    if (USE_BUFFER)
      g.drawImage(backBuffer, 0, 0, null)    
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
  
  /*
  override def createVolatileImage(width: Int, height: Int) = {	
	val gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment
	val gConfiguration = gEnvironment.getDefaultScreenDevice.getDefaultConfiguration
	gConfiguration.createCompatibleVolatileImage(width, height)
  }
  */
  
}