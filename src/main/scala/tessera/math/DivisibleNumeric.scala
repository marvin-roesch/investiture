package tessera.math

import scala.language.implicitConversions

/**
 * DivisibleNumeric
 *
 * @author PaleoCrafter
 */
case class DivisibleNumeric[T](num: Numeric[T]) extends Numeric[T] {
  private val isFractional = num.isInstanceOf[Fractional[T]]
  private val isIntegral = num.isInstanceOf[Integral[T]]

  private def fractional = num.asInstanceOf[Fractional[T]]

  private def integral = num.asInstanceOf[Integral[T]]

  override def negate(x: T): T = num.negate(x)

  override def plus(x: T, y: T): T = num.plus(x, y)

  override def minus(x: T, y: T): T = num.minus(x, y)

  override def times(x: T, y: T): T = num.times(x, y)

  def div(x: T, y: T): T =
    if (isFractional)
      fractional.div(x, y)
    else if (isIntegral)
      fractional.div(x, y)
    else
      throw new RuntimeException("Could not prove that " + num + " is divisible!")

  override def fromInt(x: Int): T = num.fromInt(x)

  override def toInt(x: T): Int = num.toInt(x)

  override def toLong(x: T): Long = num.toLong(x)

  override def toFloat(x: T): Float = num.toFloat(x)

  override def toDouble(x: T): Double = num.toDouble(x)

  override def compare(x: T, y: T): Int = num.compare(x, y)

  class DivisibleOps(lhs: T) extends Ops(lhs) {
    override def +(rhs: T) = num.plus(lhs, rhs)

    override def -(rhs: T) = num.minus(lhs, rhs)

    override def *(rhs: T) = num.times(lhs, rhs)

    def /(rhs: T) = div(lhs, rhs)

    override def unary_-() = num.negate(lhs)

    override def abs(): T = num.abs(lhs)

    override def signum(): Int = num.signum(lhs)

    override  def toInt(): Int = num.toInt(lhs)

    override def toLong(): Long = num.toLong(lhs)

    override def toFloat(): Float = num.toFloat(lhs)

    override def toDouble(): Double = num.toDouble(lhs)
  }

  override implicit def mkNumericOps(lhs: T): DivisibleOps = new DivisibleOps(lhs)
}
