package org.cobalt.api.pathfinder.wrapper

import kotlin.math.sqrt
import net.minecraft.util.Mth

data class PathVector(
  private val x: Double,
  private val y: Double,
  private val z: Double,
) : Cloneable {

  companion object {
    fun computeDistance(A: PathVector, B: PathVector, C: PathVector): Double {
      val d = C.subtract(B).divide(C.distance(B))
      val v = A.subtract(B)
      val t = v.dot(d)
      val P = B.add(d.multiply(t))
      return P.distance(A)
    }
  }

  fun dot(otherVector: PathVector): Double {
    return this.x * otherVector.x + this.y * otherVector.y + this.z * otherVector.z
  }

  fun length(): Double {
    return sqrt(Mth.square(this.x) + Mth.square(this.y) + Mth.square(this.z))
  }

  fun distance(otherVector: PathVector): Double {
    return sqrt(
      Mth.square(this.x - otherVector.x) +
        Mth.square(this.y - otherVector.y) +
        Mth.square(this.z - otherVector.z)
    )
  }

  fun setX(x: Double): PathVector = PathVector(x, this.y, this.z)

  fun setY(y: Double): PathVector = PathVector(this.x, y, this.z)

  fun setZ(z: Double): PathVector = PathVector(this.x, this.y, z)

  fun subtract(otherVector: PathVector): PathVector {
    return PathVector(this.x - otherVector.x, this.y - otherVector.y, this.z - otherVector.z)
  }

  fun multiply(value: Double): PathVector {
    return PathVector(this.x * value, this.y * value, this.z * value)
  }

  fun normalize(): PathVector {
    val magnitude = this.length()
    return PathVector(this.x / magnitude, this.y / magnitude, this.z / magnitude)
  }

  fun divide(value: Double): PathVector {
    return PathVector(this.x / value, this.y / value, this.z / value)
  }

  fun add(otherVector: PathVector): PathVector {
    return PathVector(this.x + otherVector.x, this.y + otherVector.y, this.z + otherVector.z)
  }

  fun getCrossProduct(o: PathVector): PathVector {
    val x = this.y * o.getZ() - o.getY() * this.z
    val y = this.z * o.getX() - o.getZ() * this.x
    val z = this.x * o.getY() - o.getX() * this.y
    return PathVector(x, y, z)
  }

  public override fun clone(): PathVector {
    return PathVector(this.x, this.y, this.z)
  }

  fun getX(): Double = this.x

  fun getY(): Double = this.y

  fun getZ(): Double = this.z
}
