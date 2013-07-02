package at.ait.dme.forcelayout

/**
 * An edge in the force layout simulation.
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
case class Edge(from: Node, to: Node, weight: Double = 1.0)