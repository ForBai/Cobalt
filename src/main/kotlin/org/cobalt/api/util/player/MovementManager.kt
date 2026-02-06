package org.cobalt.api.util.player

object MovementManager {

  @JvmField
  @Volatile
  var isLookLocked = false

  @JvmStatic
  fun setLookLock(state: Boolean = true) {
    isLookLocked = state
  }

}
