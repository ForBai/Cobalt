package org.cobalt.api.pathfinder.pathing

import org.cobalt.api.pathfinder.wrapper.PathVector

object NeighborStrategies {
  val VERTICAL_AND_HORIZONTAL = INeighborStrategy {
    listOf(
      PathVector(1.0, 0.0, 0.0),
      PathVector(-1.0, 0.0, 0.0),
      PathVector(0.0, 0.0, 1.0),
      PathVector(0.0, 0.0, -1.0),
      PathVector(0.0, 1.0, 0.0),
      PathVector(0.0, -1.0, 0.0)
    )
  }

  val DIAGONAL_3D = INeighborStrategy {
    buildList {
      for (x in -1..1) {
        for (y in -1..1) {
          for (z in -1..1) {
            if (x == 0 && y == 0 && z == 0) continue
            add(PathVector(x.toDouble(), y.toDouble(), z.toDouble()))
          }
        }
      }
    }
  }

  val HORIZONTAL_DIAGONAL_AND_VERTICAL = INeighborStrategy {
    listOf(
      PathVector(1.0, 0.0, 0.0),
      PathVector(-1.0, 0.0, 0.0),
      PathVector(0.0, 0.0, 1.0),
      PathVector(0.0, 0.0, -1.0),
      PathVector(0.0, 1.0, 0.0),
      PathVector(0.0, -1.0, 0.0),
      PathVector(1.0, 0.0, 1.0),
      PathVector(1.0, 0.0, -1.0),
      PathVector(-1.0, 0.0, 1.0),
      PathVector(-1.0, 0.0, -1.0)
    )
  }
}
