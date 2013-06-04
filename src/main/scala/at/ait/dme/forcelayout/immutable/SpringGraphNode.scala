package at.ait.dme.forcelayout.immutable

import at.ait.dme.forcelayout.Vector

case class SpringGraphNode(id: String, label: String, mass: Double = 1.0, group: Int = 0, val state: SpringGraphNodeState = SpringGraphNodeState())

case class SpringGraphNodeState(pos: Vector = Vector.random(0.1), velocity: Vector = Vector(0, 0))  
