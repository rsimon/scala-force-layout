package at.ait.dme.forcelayout.examples

import rapture.io._
import scala.io.Source
import at.ait.dme.forcelayout.Node
import at.ait.dme.forcelayout.Edge
import at.ait.dme.forcelayout.SpringGraph
import at.ait.dme.forcelayout.renderer.InteractiveGraphRenderer
import javax.swing.JFrame
import at.ait.dme.forcelayout.renderer.AcceleratedInteractiveGraphRenderer

object LesMiserablesInteractive extends App {
  
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
  
  val vis = new AcceleratedInteractiveGraphRenderer(graph)
  
  val frame = new JFrame("Les Miserables")
  frame.setSize(920, 720)
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  frame.getContentPane().add(vis) 
  frame.pack()
  frame.setVisible(true)
  
  vis.start
  
}