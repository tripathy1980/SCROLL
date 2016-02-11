package scroll.internal.graph

import scroll.internal.support.DispatchQuery
import scala.reflect.runtime.universe._
import org.kiama.util.Memoiser

object CachedScalaRoleGraph extends Memoiser {

  sealed trait KeyOption

  object Contains extends KeyOption

  object Predecessors extends KeyOption

  object Roles extends KeyOption

  case class Key(obj: Any, opt: KeyOption)

  class Cache extends IdMemoised[Key, Set[Any]]

}

class CachedScalaRoleGraph extends ScalaRoleGraph {

  import CachedScalaRoleGraph._

  private lazy val cache = new Cache()

  override def addBinding[P <: AnyRef : WeakTypeTag, R <: AnyRef : WeakTypeTag](player: P, role: R) {
    super.addBinding(player, role)
    cache.resetAt(Key(player, Contains))
    cache.resetAt(Key(player, Predecessors))
    cache.resetAt(Key(player, Roles))
    cache.resetAt(Key(role, Contains))
    cache.resetAt(Key(role, Predecessors))
    cache.resetAt(Key(role, Roles))
  }

  override def containsPlayer(player: Any): Boolean = {
    val key = Key(player, Contains)
    cache.get(key) match {
      case Some(v) => v.nonEmpty
      case None =>
        super.containsPlayer(player) match {
          case true =>
            cache.put(key, Set(player))
            true
          case false =>
            cache.put(key, Set.empty)
            false
        }
    }
  }

  override def detach(other: RoleGraph) {
    assert(other.isInstanceOf[CachedScalaRoleGraph], "You can only detach RoleGraphs of the same type!")
    super.detach(other)
    cache.reset()
  }

  override def getPredecessors(player: Any)(implicit dispatchQuery: DispatchQuery): List[Any] = {
    val key = Key(player, Predecessors)
    cache.get(key) match {
      case Some(v) => v.toList
      case None =>
        val ps = super.getPredecessors(player)
        cache.put(key, ps.toSet)
        ps
    }
  }

  override def getRoles(player: Any)(implicit dispatchQuery: DispatchQuery): Set[Any] = {
    val key = Key(player, Roles)
    cache.get(key) match {
      case Some(v) => v
      case None =>
        val rs = super.getRoles(player)
        cache.put(key, rs)
        rs
    }
  }

  override def merge(other: RoleGraph) {
    assert(other.isInstanceOf[CachedScalaRoleGraph], "You can only merge RoleGraphs of the same type!")
    super.merge(other)
    cache.reset()
  }

  override def removeBinding[P <: AnyRef : WeakTypeTag, R <: AnyRef : WeakTypeTag](player: P, role: R) {
    super.removeBinding(player, role)
    cache.resetAt(Key(player, Contains))
    cache.resetAt(Key(player, Predecessors))
    cache.resetAt(Key(player, Roles))
    cache.resetAt(Key(role, Contains))
    cache.resetAt(Key(role, Predecessors))
    cache.resetAt(Key(role, Roles))
  }

  override def removePlayer[P <: AnyRef : WeakTypeTag](player: P) {
    super.removePlayer(player)
    cache.resetAt(Key(player, Contains))
    cache.resetAt(Key(player, Predecessors))
    cache.resetAt(Key(player, Roles))
  }
}
