# Scala Force Layout

_Scala Force Layout_ is a force-directed graph layout implementation in Scala. The project originally started
out as a a port of the [Springy](http://getspringy.com/) JavaScript graph layout code by Dennis Hotson. In
addition, I added [Barnes-Hut simulation](http://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation) to 
improve performance on bigger graphs (here's [a video](http://www.screenr.com/7F7H)),
and based my physics model parameters on those used in [VivaGraphJS](http://github.com/anvaka/VivaGraphJS) by
Andrei Kashcha.  

![Scala Force Layout Example](http://github.com/rsimon/scala-force-layout/raw/master/scala-force-layout.png)

## Getting Started

Create a graph from collections of __nodes__ and __edges__.

    val nodes = Seq(
        Node("id_a", "Node A"),
        Node("id_b", "Node B"),
        Node("id_c", "Node C"),
        Node("id_d", "Node D"))
      
    val edges = Seq(
        Edge(nodes(0), nodes(1)),
        Edge(nodes(1), nodes(2)),
        Edge(nodes(2), nodes(3)),
        Edge(nodes(0), nodes(3)))
      
    val graph = new SpringGraph(nodes, edges)
    
Run the layout algorithm using the ``graph.doLayout()`` method. Attach ``onIteration`` and
``onComplete`` handlers to capture intermediate and final results of the layout process.

    graph
      .onIteration(it => { ... do something on every layout iteration ... })
      .onComplete(it => { println("completed in " + it + " iterations") })
      .doLayout()

### Rendering an Image
            
The ``ImageRenderer`` is a simple utility for rendering an image of your graph. If all you
want is to store an image of the final layout, this is what you're looking for:

    graph
      .onComplete(it => {
        // Renders a 500x500 pixel image of the final graph layout  
        val image = ImageRenderer.drawGraph(graph, 500, 500)
        
        // Writes the image to a PNG file
        ImageIO.write(image, "png", new File("my-graph.png"))
      })
      .doLayout()
      
### Opening a Viewer
      
If you want to open your graph in a window on the screen (with mouse pan and zoom included),
use this code:

    // Creates a zoom- and pan-able view of the graph
    val vis = new BufferedInteractiveGraphRenderer(graph)
  
    // Creates a JFrame, with the graph renderer in the content pane
    val frame = new JFrame("Les Miserables")
    frame.setSize(920, 720)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane().add(vis) 
    frame.pack()
    
    // Pops up the JFrame on the screen, and starts the layout process
    frame.setVisible(true)
    vis.start 
      
You may also want to take a look at the [Hello World](https://github.com/rsimon/scala-force-layout/blob/master/src/main/scala/at/ait/dme/forcelayout/examples/HelloWorld.scala)
and [LesMiserablesZoomable](https://github.com/rsimon/scala-force-layout/blob/master/src/main/scala/at/ait/dme/forcelayout/examples/LesMiserablesZoomable.scala)
examples for complete, working code. 

## Current Version

The current version of _Scala Force Layout_ is 0.2.0. Download the jar for Scala 2.10 here: [scala-force-layout_2.10-0.2.0.jar](http://rsimon.github.com/files/scala-force-layout_2.10-0.2.0.jar)

## Building From Source & Running the Examples

_Scala Force Layout_ uses [SBT](http://www.scala-sbt.org/) as a build tool. Please refer to the
[SBT documentation](http://www.scala-sbt.org/release/docs/index.html) for instructions on how to
install SBT on your machine. Once you have installed SBT, you can run the examples by typing ``sbt run``. 
To build a .jar package type ``sbt package``. To generate a project for the 
[Eclipse IDE](http://www.eclipse.org/), type ``sbt eclipse``.

## Future Work

There are many things on the list - feel free to help out if you care to!

* _"The last thing we need is another graph API."_ // TODO use the [Tinkerpop Blueprints](https://github.com/tinkerpop/blueprints/wiki) graph model
* _"Mutable state, everywhere."_ // TODO parts of the code are really ugly and need to be made more functional & Scala-idiomatic
* _"Where can I click?"_ // TODO create a renderer that produces an interactive graph, complete with draggable nodes and such
* _"Yeah, but I want my labels pink!"_ // TODO add a mechanism to control node, edge and label style
* _"Sorry, I don't code."_ // TODO A simple command-line wrapper that opens some [GraphSON](https://github.com/tinkerpop/blueprints/wiki/GraphSON-Reader-and-Writer-Library), 
  with no coding involved, would be nice

## License

_Scala Force Layout_ is released under the [MIT License](http://en.wikipedia.org/wiki/MIT_License).
