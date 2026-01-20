package org.cobalt.api.pathfinder.wrapper

import kotlin.math.sqrt
import net.minecraft.util.Mth

data class PathPosition(
  private val x: Double,
  private val y: Double,
  private val z: Double,
) : Cloneable {

  fun distanceSquared(otherPosition: PathPosition): Double {
    return Mth.square(this.x - otherPosition.x) +
      Mth.square(this.y - otherPosition.y) +
      Mth.square(this.z - otherPosition.z)
  }

  fun distance(otherPosition: PathPosition): Double {
    return sqrt(this.distanceSquared(otherPosition))
  }

  fun setX(x: Double): PathPosition = PathPosition(x, this.y, this.z)

  fun setY(y: Double): PathPosition = PathPosition(this.x, y, this.z)

  fun setZ(z: Double): PathPosition = PathPosition(this.x, this.y, z)

  fun getCenteredX(): Double = getFlooredX() + 0.5

  fun getCenteredY(): Double = getFlooredY() + 0.5

  fun getCenteredZ(): Double = getFlooredZ() + 0.5

  fun getFlooredX(): Int = Mth.floor(this.x)

  fun getFlooredY(): Int = Mth.floor(this.y)

  fun getFlooredZ(): Int = Mth.floor(this.z)

  fun add(x: Double, y: Double, z: Double): PathPosition {
    return PathPosition(this.x + x, this.y + y, this.z + z)
  }

  fun add(vector: PathVector): PathPosition {
    return add(vector.getX(), vector.getY(), vector.getZ())
  }

  fun subtract(x: Double, y: Double, z: Double): PathPosition {
    return PathPosition(this.x - x, this.y - y, this.z - z)
  }

  fun subtract(vector: PathVector): PathPosition {
    return subtract(vector.getX(), vector.getY(), vector.getZ())
  }

  fun toVector(): PathVector = PathVector(this.x, this.y, this.z)

  fun floor(): PathPosition = PathPosition(getFlooredX().toDouble(), getFlooredY().toDouble(), getFlooredZ().toDouble())

  fun mid(): PathPosition = PathPosition(getFlooredX() + 0.5, getFlooredY() + 0.5, getFlooredZ() + 0.5)

  fun midPoint(end: PathPosition): PathPosition {
    return PathPosition((this.x + end.x) / 2, (this.y + end.y) / 2, (this.z + end.z) / 2)
  }

  public override fun clone(): PathPosition {
    return PathPosition(this.x, this.y, this.z)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as PathPosition
    return getFlooredX() == other.getFlooredX() &&
      getFlooredY() == other.getFlooredY() &&
      getFlooredZ() == other.getFlooredZ()
  }

  override fun hashCode(): Int {
    val x = getFlooredX()
    val y = getFlooredY()
    val z = getFlooredZ()
    var result = x
    result = 31 * result + y
    result = 31 * result + z
    return result
  }

  fun getX(): Double = this.x

  fun getY(): Double = this.y

  fun getZ(): Double = this.z

  override fun toString(): String {
    return "PathPosition(x=${getX()}, y=${getY()}, z=${getZ()})"
  }
}
