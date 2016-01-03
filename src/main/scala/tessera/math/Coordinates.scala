package tessera.math

trait Coordinates[@specialized S, C <: Coordinates[S, C]] {
  this: C =>
  def unary_- : C

  def +(that: C): C

  def -(that: C): C

  def *(that: C): C

  def *(scalar: S): C

  def /(scalar: S): C

  def min(that: C): C

  def max(that: C): C

  def magnitude: Double = math.sqrt(magnitudeSquared)

  def magnitudeSquared: Double

  def distance(that: C): Double = math.sqrt(distanceSquared(that))

  def distanceSquared(that: C): Double = (this - that).magnitudeSquared
}