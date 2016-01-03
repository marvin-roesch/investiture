package tessera.math

/**
  * Coordinates
  *
  * @author PaleoCrafter
  */
abstract class Coordinates2[@specialized S: Numeric, C <: Coordinates2[S, C]]
  extends Coordinates[S, C] {
  this: C =>

  private[this] final val num = DivisibleNumeric(implicitly[Numeric[S]])
  private[this] final val ord = implicitly[Ordering[S]]

  import num._

  def x: S

  def y: S

  def companion: Coordinates2Companion[S, C]

  override def unary_- : C = companion(-x, -y)

  override def +(that: C): C =
    companion(x + that.x, y + that.y)

  override def -(that: C): C =
    companion(x - that.x, y - that.y)

  override def *(scalar: S): C =
    companion(x * scalar, y * scalar)

  override def *(that: C): C =
    companion(x * that.x, y * that.y)

  override def /(scalar: S): C =
    companion(x / scalar, y / scalar)

  override def min(that: C): C = companion(ord.min(x, that.x), ord.min(y, that.y))

  override def max(that: C): C = companion(ord.max(x, that.x), ord.max(y, that.y))

  override def magnitudeSquared: Double = (x * x + y * y).toDouble()

  def dot(that: C) = {
    var d: Double = (that.x * x + that.y * y).toDouble()
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }
}

trait Coordinates2Companion[@specialized S, C <: Coordinates2[S, C]] {
  def Zero: C

  def One: C

  def apply(x: S, y: S): C
}

/**
  * Vector3
  *
  * @author PaleoCrafter
  */
case class Vec2d(x: Double, y: Double) extends Coordinates2[Double, Vec2d] {
  override def companion = Vec2d
}

object Vec2d extends Coordinates2Companion[Double, Vec2d] {
  final val Zero = Vec2d(0, 0)
  final val One = Vec2d(1, 1)
}

case class Vec2i(x: Int, y: Int) extends Coordinates2[Int, Vec2i] {
  def down(n: Int = 1) = this + Vec2i.Down * n

  def up(n: Int = 1) = this + Vec2i.Up * n

  def left(n: Int = 1) = this + Vec2i.Left * n

  def right(n: Int = 1) = this + Vec2i.Right * n

  def toVec2d = Vec2d(x, y)

  override def companion = Vec2i
}

object Vec2i extends Coordinates2Companion[Int, Vec2i] {
  final val Zero = Vec2i(0, 0)
  final val One = Vec2i(1, 1)

  final val Down = Vec2i(0, -1)
  final val Up = Vec2i(0, 1)
  final val Left = Vec2i(-1, 0)
  final val Right = Vec2i(1, 0)
}