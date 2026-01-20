package org.cobalt.api.pathfinder.util

fun interface ParameterizedSupplier<T> {
  fun accept(value: T): T
}
