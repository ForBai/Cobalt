package org.cobalt.api.pathfinder.wrapper

data class Depth private constructor(private var value: Int) {
  companion object {
    fun of(value: Int): Depth = Depth(value)
  }

  fun increment() {
    value++
  }

  fun getValue(): Int = value
}
