package at.ait.dme.forcelayout

case class Edge(from: Node, to: Node, weight: Double = 1.0)

class ImmutableEdge(val from: ImmutableNode, val to: ImmutableNode, val weight: Double = 1.0)