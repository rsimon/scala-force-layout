package at.ait.dme.forcelayout

case class Node(id: String, label: String, var mass: Double = 1.0, group: Int = 0, val state: NodeState = NodeState())

case class NodeState(var pos: Vector2D = Vector2D.random(1.0), var velocity: Vector2D = Vector2D(0, 0), var force: Vector2D = Vector2D(0, 0))  
