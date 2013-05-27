# Scala Force Layout

_Scala Force Layout_ is a force-directed graph layout implementation in Scala, based on a spring 
physics model. To some extent, this code is a port of the [Springy](http://getspringy.com/) JavaScript 
graph layout code by Dennis Hotson. But it additionally uses the [Barnes-Hut](http://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation)
algorithm to improve performance on bigger graphs.

![Scala Force Layout Example](http://github.com/rsimon/scala-force-layout/raw/master/scala-force-layout.png)

## Current Version

The current version of _Scala Force Layout_ is 0.0.1. Download the jar here: [scala-force-layout_2.10-0.1.0.jar](http://rsimon.github.com/files/scala-force-layout_2.10-0.1.0.jar)

## Building From Source & Running the Examples

_Scala Force Layout_ uses [SBT](http://www.scala-sbt.org/) as a build tool. Please refer to the
[SBT documentation](http://www.scala-sbt.org/release/docs/index.html) for instructions on how to
install SBT on your machine. Once you have installed SBT, you can run the examples by typing ``sbt run``. 
To build a .jar package type ``sbt package``. To generate a project for the 
[Eclipse IDE](http://www.eclipse.org/), type ``sbt eclipse``.

## API

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
    
Run the layout algorithm using the ``graph.doLayout()`` method. You can attach ``onIteration`` and
``onComplete`` handlers to capture intermediate and final results of the layout process.

    graph
      .onIteration(it => { ... do something on every layout iteration ... })
      .onComplete(it => { println("completed in " + it + " iterations") })
      .doLayout()
      
The ``GraphRenderer`` is a simple utility for rendering an image of your graph. If all you
want is to store an image of the final layout, this is what you're looking for:

    graph
      .onComplete(it => {
        // Renders a 500x500 pixel image of the final graph layout  
        val image = GraphRenderer.drawGraph(graph, 500, 500)
        
        // Writes the image to a PNG file
        ImageIO.write(image, "png", new File("my-graph.png"))
      })
      .doLayout()
      
You may also want to take a look at the [Hello World 
example](https://github.com/rsimon/scala-force-layout/blob/master/src/main/scala/at/ait/dme/forcelayout/examples/HelloWorld.scala)
for complete, working code. 

## Future Work

There are many things on the list - feel free to help out if you care to!

* _"The last thing we need is another graph API."_ // TODO use the [Tinkerpop Blueprints](https://github.com/tinkerpop/blueprints/wiki) graph model
* _"Mutable state, everywhere."_ // TODO parts of the code are really ugly and need to be made more functional & Scala-idiomatic
* _"Where can I click?"_ // TODO create a renderer that produces an interactive graph
* _"Yeah, but I want my labels pink!"_ // TODO add a mechanism to control fill, line and font style

## License

_Scala Force Layout_ is released under the [MIT License](http://en.wikipedia.org/wiki/MIT_License).
