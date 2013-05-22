# Scala Force Layout

_Scala Force Layout_ is a force-directed graph layout implementation for Scala, based on a basic spring 
physics model. To a wide extent, this code is a port of the [Springy](http://getspringy.com/) JavaScript 
graph layout code by Dennis Hotson. But I'm working on including ideas from other libraries as well.  

![Scala Force Layout Example](http://github.com/rsimon/scala-force-layout/raw/master/scala-force-layout.png)

## Building From Source & Running the Examples

_Scala Force Layout_ uses [SBT](http://www.scala-sbt.org/) as a build tool. Please refer to the
[SBT documentation](http://www.scala-sbt.org/release/docs/index.html) for instructions on how to
install SBT on your machine. Once you have installed SBT, you can run the examples by typing ``sbt run``. 
To build a .jar package type ``sbt package``. To generate a project for the 
[Eclipse IDE](http://www.eclipse.org/), type ``sbt eclipse``.

## API

Create a graph as a collection of __nodes__ and __edges__.

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
    
Run the layout algorithm using the ``graph.doLayout()`` method. You can attach ``onIteration`` and
``onComplete`` handlers to capture intermediate and final results of the layout process.

    graph
      .onIteration(it => { ... do something on every layout iteration ... })
      .onComplete(it => { println("completed in " + it + " iterations") })
      .doLayout()
      
The ``GraphRenderer`` is a simply utility for rendering an image of your graph. If all you
want is to store an image of the final layout, this is what you're looking for:

    graph
      .onComplete(it => {
        val image = GraphRenderer.drawGraph(graph, 500, 500)
        ImageIO.write(image, "png", new File("my-graph.png"))
      })
      .doLayout()
      
You may also want to take a look at the [Hello World](https://github.com/rsimon/scala-force-layout/blob/master/src/main/scala/at/ait/dme/forcelayout/examples/HelloWorld.scala)
for a full code example. 

## To Do

There are many things that could be done - feel free to help out if you care ;-)

* Create a renderer that produces an interactive graph.
* Implement [Barnes-Hut](http://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation) to speed up computation.
* Align API with the [Tinkerpop Blueprints](https://github.com/tinkerpop/blueprints/wiki) graph model
* More & better styling options

## License

_Scala Force Layout_ is released under the [MIT License](http://en.wikipedia.org/wiki/MIT_License).
