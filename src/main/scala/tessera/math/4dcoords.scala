package tessera.math

/**
  * Coordinates
  *
  * @author PaleoCrafter
  */
abstract class Coordinates4[@specialized S: Numeric, C <: Coordinates4[S, C]]
  extends Coordinates[S, C] {
  this: C =>

  private[this] final val num = DivisibleNumeric(implicitly[Numeric[S]])
  private[this] final val ord = implicitly[Ordering[S]]

  import num._

  def x: S

  def y: S

  def z: S

  def w: S

  def companion: Coordinates4Companion[S, C]

  override def unary_- : C = companion(-x, -y, -z, -w)

  override def +(that: C): C =
    companion(x + that.x, y + that.y, z + that.z, w + that.w)

  override def -(that: C): C =
    companion(x - that.x, y - that.y, z - that.z, w - that.w)

  override def *(scalar: S): C =
    companion(x * scalar, y * scalar, z * scalar, w * scalar)

  override def *(that: C): C =
    companion(x * that.x, y * that.y, z * that.z, w * that.w)

  override def /(scalar: S): C =
    companion(x / scalar, y / scalar, z / scalar, w / scalar)

  override def min(that: C): C = companion(ord.min(x, that.x), ord.min(y, that.y), ord.min(z, that.z), ord.min(w, that.w))

  override def max(that: C): C = companion(ord.max(x, that.x), ord.max(y, that.y), ord.max(z, that.z), ord.max(w, that.w))

  override def magnitudeSquared: Double = (x * x + y * y + z * z + w * w).toDouble()

  def dot(that: C) = {
    var d: Double = (that.x * x + that.y * y + that.z * z + that.w * that.w).toDouble()
    if (d > 1 && d < 1.00001) d = 1
    else if (d < -1 && d > -1.00001) d = -1
    d
  }
}

trait Coordinates4Companion[@specialized S, C <: Coordinates4[S, C]] {
  def Zero: C

  def One: C

  def apply(x: S, y: S, z: S, w: S): C
}

/**
  * Vector3
  *
  * @author PaleoCrafter
  */
case class Vec4d(x: Double, y: Double, z: Double, w: Double) extends Coordinates4[Double, Vec4d] {
  override def companion = Vec4d
}

object Vec4d extends Coordinates4Companion[Double, Vec4d] {
  final val Zero = Vec4d(0, 0, 0, 0)
  final val One = Vec4d(1, 1, 1, 0)
}

case class Vec4i(x: Int, y: Int, z: Int, w: Int) extends Coordinates4[Int, Vec4i] {
  override def companion = Vec4i
}

object Vec4i extends Coordinates4Companion[Int, Vec4i] {
  final val Zero = Vec4i(0, 0, 0, 0)
  final val One = Vec4i(1, 1, 1, 1)
}