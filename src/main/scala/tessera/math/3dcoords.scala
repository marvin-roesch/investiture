package tessera.math

import net.minecraft.util.EnumFacing
import tessera._

/**
 * Coordinates
 *
 * @author PaleoCrafter
 */
abstract class Coordinates3[@specialized S: Numeric, C <: Coordinates3[S, C]]
  extends Coordinates[S, C] {
  this: C =>

  private[this] final val num = DivisibleNumeric(implicitly[Numeric[S]])
  private[this] final val ord = implicitly[Ordering[S]]

  import num._

  def x: S

  def y: S

  def z: S

  def companion: Coordinates3Companion[S, C]

  override def unary_- : C = companion(-x, -y, -z)

  override def +(that: C): C =
    companion(x + that.x, y + that.y, z + that.z)

  override def -(that: C): C =
    companion(x - that.x, y - that.y, z - that.z)

  override def *(scalar: S): C =
    companion(x * scalar, y * scalar, z * scalar)

  override def *(that: C): C =
    companion(x * that.x, y * that.y, z * that.z)

  override def /(scalar: S): C =
    companion(x / scalar, y / scalar, z / scalar)

  override def min(that: C): C = companion(ord.min(x, that.x), ord.min(y, that.y), ord.min(z, that.z))

  override def max(that: C): C = companion(ord.max(x, that.x), ord.max(y, that.y), ord.max(z, that.z))

  override def magnitudeSquared: Double = (x * x + y * y + z * z).toDouble()

  def dot(that: C) = {
    var d: Double = (that.x * x + that.y * y + that.z * z).toDouble()
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }

  def cross(that: C) = {
    val x = this.y * that.z - this.z * that.y
    val y = this.z * that.x - this.x * that.z
    val z = this.x * that.y - this.y * that.x

    companion(x, y, z)
  }
}

trait Coordinates3Companion[@specialized S, C <: Coordinates3[S, C]] {
  def Zero: C

  def One: C

  def apply(x: S, y: S, z: S): C
}

/**
 * Vector3
 *
 * @author PaleoCrafter
 */
case class Vec3d(x: Double, y: Double, z: Double) extends Coordinates3[Double, Vec3d] {
  override def companion = Vec3d
}

object Vec3d extends Coordinates3Companion[Double, Vec3d] {
  final val Zero = Vec3d(0, 0, 0)
  final val One = Vec3d(1, 1, 1)
}

case class Vec3i(x: Int, y: Int, z: Int) extends Coordinates3[Int, Vec3i] {
  def down(n: Int = 1) = this + Vec3i.Down * n

  def up(n: Int = 1) = this + Vec3i.Up * n

  def north(n: Int = 1) = this + Vec3i.North * n

  def south(n: Int = 1) = this + Vec3i.South * n

  def west(n: Int = 1) = this + Vec3i.West * n

  def east(n: Int = 1) = this + Vec3i.East * n

  def offset(direction: EnumFacing, n: Int = 1) = this + direction.getDirectionVec * n

  override def companion = Vec3i
}

object Vec3i extends Coordinates3Companion[Int, Vec3i] {
  final val Zero = Vec3i(0, 0, 0)
  final val One = Vec3i(1, 1, 1)

  final val Down = Vec3i(0, -1, 0)
  final val Up = Vec3i(0, 1, 0)
  final val North = Vec3i(0, 0, -1)
  final val South = Vec3i(0, 0, 1)
  final val West = Vec3i(0, 0, -1)
  final val East = Vec3i(0, 0, 1)
}