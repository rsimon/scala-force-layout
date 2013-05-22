package at.ait.dme.forcelayout.examples

import at.ait.dme.forcelayout.{Node, Edge, SpringGraph}
import javax.swing.JFrame
import javax.swing.ImageIcon
import java.awt.Dimension
import javax.swing.JLabel
import at.ait.dme.forcelayout.GraphRenderer
import javax.imageio.ImageIO
import java.io.File

object HelloWorld extends App {
  
  val nodes = Seq(
      new Node("A", "Node A"),
      new Node("B", "Node B"),
      new Node("C", "Node C"),
      new Node("D", "Node D"))
      
  val edges = Seq(
      new Edge(nodes(0), nodes(1)),
      new Edge(nodes(1), nodes(2)),
      new Edge(nodes(2), nodes(3)),
      new Edge(nodes(0), nodes(3)))
      
  val graph = new SpringGraph(nodes, edges) 
   
  val frame = new JFrame()
  frame.setPreferredSize(new Dimension(500,500))
  val imgIcon = new ImageIcon()
  val imgLabel = new JLabel(imgIcon)
  frame.add(imgLabel)
  frame.pack();
  frame.setVisible(true);
  
  graph
    .onIteration(it => imgLabel.setIcon(new ImageIcon(GraphRenderer.drawGraph(graph, 500, 500))))
    .onComplete(it => { 
      println("completed in " + it + " iterations")
    })
    .doLayout()

}