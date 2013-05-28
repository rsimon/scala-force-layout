package at.ait.dme.forcelayout.examples

import rapture.io._
import at.ait.dme.forcelayout._
import scala.io.Source
import javax.swing.JFrame
import javax.swing.ImageIcon
import java.awt.Dimension
import javax.swing.JLabel
import at.ait.dme.forcelayout.renderer.GraphRenderer
import at.ait.dme.forcelayout.Node
import at.ait.dme.forcelayout.SpringGraph
import rapture.io.JsonExtractor.intJsonExtractor
import rapture.io.JsonExtractor.listJsonExtractor
import rapture.io.JsonExtractor.stringJsonExtractor
import at.ait.dme.forcelayout.renderer.GraphRenderer

object LesMiserables extends App {

  // Read JSON 
  println("Loading data")
  val json = Json.parse(Source.fromFile("src/test/resources/examples/miserables.json").mkString)
  
  val nodes: Seq[Node] = json.nodes.get[List[Json]].map(json => {
      val name = json.name.get[String].toString
      val group = json.group.get[Int]
      Node(name, name, 1.0, group)
    })
    
  val edges = json.links.get[List[Json]].map(json => {
    val value = json.value.get[Int]
    Edge(nodes(json.source.get[Int]), nodes(json.target.get[Int]), value.toDouble)
  })
    
  val graph = new SpringGraph(nodes, edges) 
   
  val frame = new JFrame()
  frame.setPreferredSize(new Dimension(800,800))
  val imgIcon = new ImageIcon()
  val imgLabel = new JLabel(imgIcon)
  frame.add(imgLabel)
  frame.pack();
  frame.setVisible(true);
  
  graph
    .onIteration(it => imgLabel.setIcon(new ImageIcon(GraphRenderer.drawGraph(graph, 800, 800))))
    .onComplete(it => { 
      println("completed in " + it + " iterations")
    })
    .doLayout()
  
}