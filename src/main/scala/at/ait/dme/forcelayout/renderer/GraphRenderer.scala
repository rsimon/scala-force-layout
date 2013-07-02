package at.ait.dme.forcelayout.renderer

import java.awt._
import java.awt.geom.Ellipse2D
import at.ait.dme.forcelayout.{ Node, Edge, SpringGraph, Vector2D }

class Node2D(val x: Int, val y: Int, val node: Node)

class Edge2D(val from: Node2D, val to: Node2D, val edge: Edge)

private[renderer] trait GraphRenderer {

  private var lastCompletion: Long = System.currentTimeMillis
  
  private var nodePainter = (nodes: Seq[Node2D], g2d: Graphics2D) => {
    nodes.foreach(n2d => {
      val (x, y, n) = (n2d.x, n2d.y, n2d.node)
      val size = Math.max(6, Math.min(30, Math.log(n.mass) + 1))
      g2d.setColor(ColorPalette.getColor(n.group))
      g2d.fill(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size))
    })
  }
  
  def setNodePainter(painter: (Seq[Node2D], Graphics2D) => Unit) =
    nodePainter = painter
  
  private var edgePainter = (edges: Seq[Edge2D], g2d: Graphics2D) => {
    edges.foreach(e2d => {
      val width = Math.min(4, Math.max(2, Math.min(8, e2d.edge.weight)).toInt / 2)   
      g2d.setStroke(new BasicStroke(width));
      g2d.setColor(new Color(198, 198, 198, 198))  
      g2d.drawLine(e2d.from.x, e2d.from.y, e2d.to.x, e2d.to.y)
    })
  } 
  
  def setEdgePainter(painter: (Seq[Edge2D], Graphics2D) => Unit) =
    edgePainter = painter
    
  def render(g2d: Graphics2D, graph: SpringGraph, width: Int, height: Int, selectedNode: Option[Node] = None, offsetX: Double = 0.0, offsetY: Double = 0.0, zoom: Double = 1.0, showLabels: Boolean = false): Unit = {
    g2d.setColor(Color.WHITE)
    g2d.fillRect(0, 0, width, height)

    val c = computeScale(graph, width, height) * zoom
    val (dx, dy) = (width / 2 + offsetX, height / 2 + offsetY)
    
    val edges2D = graph.edges.map(e => {
      val from = new Node2D((c * e.from.state.pos.x + dx).toInt, (c * e.from.state.pos.y + dy).toInt, e.from)
      val to = new Node2D((c * e.to.state.pos.x + dx).toInt, (c * e.to.state.pos.y + dy).toInt, e.to)
      new Edge2D(from, to, e)
    })
    edgePainter(edges2D, g2d)
    
    val nodes2D = graph.nodes.map(n => new Node2D((c * n.state.pos.x + dx).toInt, (c * n.state.pos.y + dy).toInt, n))
      .filter(n2d => n2d.x > 0 && n2d.y > 0)
      .filter(n2d => n2d.x <= width && n2d.y <= height)
    nodePainter(nodes2D, g2d)
    
    if (showLabels) {
      g2d.setColor(Color.BLACK)
      nodes2D.foreach(n2d => g2d.drawString(n2d.node.label, n2d.x + 5, n2d.y - 2))
    }  
          
    if (selectedNode.isDefined) {
      val n = selectedNode.get
      val size = Math.log(n.mass) + 7
      val px = c * n.state.pos.x + dx 
      val py = c * n.state.pos.y + dy
      
      // Highlight in-links
      graph.edges.filter(_.to.id.equals(n.id)).foreach(e => {
        val from = (c * e.from.state.pos.x + dx, c * e.from.state.pos.y + dy)
        val width = Math.min(4, Math.max(2, Math.min(8, e.weight)).toInt / 2)
    
        g2d.setStroke(new BasicStroke(width));
        g2d.setColor(Color.GREEN)
        g2d.drawLine(from._1.toInt, from._2.toInt, px.toInt, py.toInt)
        g2d.setColor(Color.BLACK)
        g2d.drawString(e.from.label, from._1.toInt + 5, from._2.toInt - 2)
      })
      
      // Highlight out-links
      graph.edges.filter(_.from.id.equals(n.id)).foreach(e => {
        val to = (c * e.to.state.pos.x + dx, c * e.to.state.pos.y + dy)
        val width = Math.min(4, Math.max(2, Math.min(8, e.weight)).toInt / 2)
    
        g2d.setStroke(new BasicStroke(width));
        g2d.setColor(Color.RED)
        g2d.drawLine(px.toInt, py.toInt, to._1.toInt, to._2.toInt)
        g2d.setColor(Color.BLACK)
        g2d.drawString(e.to.label, to._1.toInt + 5, to._2.toInt - 2)
      })
      
      g2d.setColor(Color.BLACK);
      g2d.draw(new Ellipse2D.Double(px - size / 2, py - size / 2, size, size))  
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


