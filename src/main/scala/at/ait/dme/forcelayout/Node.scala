package at.ait.dme.forcelayout

case class Node(
  id: String,
  label: String,
  var mass: Double = 1.0,
  group: Int = 0,
  val state: NodeState = NodeState()) {
  
  def immutable = new ImmutableNode(id, label, mass, group, new ImmutableNodeState(state.pos, state.velocity, state.force))
  
}

class ImmutableNode(
  val id: String,
  val label: String,
  val mass: Double = 1.0,
  val group: Int = 0,
  val state: ImmutableNodeState = new ImmutableNodeState()) 
   
case class NodeState(
  var pos: Vector2D = Vector2D.random(1.0), 
  var velocity: Vector2D = Vector2D(0, 0), 
  var force: Vector2D = Vector2D(0, 0))
    
class ImmutableNodeState(
  val pos: Vector2D = Vector2D.random(1.0), 
  val velocity: Vector2D = Vector2D(0, 0), 
  val force: Vector2D = Vector2D(0, 0))

