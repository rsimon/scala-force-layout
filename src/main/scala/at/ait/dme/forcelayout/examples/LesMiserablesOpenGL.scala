package at.ait.dme.forcelayout.examples

import rapture.io._
import scala.io.Source

import java.awt.Dimension
import javax.swing.JFrame

import at.ait.dme.forcelayout.{ Edge, Node, SpringGraph }
import at.ait.dme.forcelayout.renderer.OpenGLInteractiveGraphRenderer

object LesMiserablesOpenGL extends App {
  
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
  
  val vis = new OpenGLInteractiveGraphRenderer(graph)
  
  val frame = new JFrame("Les Miserables")
  frame.setPreferredSize(new Dimension(920,720))
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.getContentPane().add(vis) 
  frame.pack()
  frame.setVisible(true)
  
  vis.start
  
}