package org.cobalt.api.module.setting.impl

/**
 * Singleton provider for globally synced rainbow phase computation.
 *
 * All ColorSettings using SyncedRainbow mode share the same phase from this provider.
 * Phase is computed based on elapsed time since the provider was initialized.
 */
object RainbowPhaseProvider {

  private val startTime = System.currentTimeMillis()

  /**
   * Get the current hue value (0..1) for globally synced rainbow.
   *
   * @param speed Speed multiplier (default 1.0, higher = faster rotation)
   * @return Hue value in range 0..1 (wraps at 1.0)
   */
  fun getHue(speed: Float = 1f): Float {
    val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
    return ((elapsed * speed) % 1.0 + 1.0).toFloat() % 1f
  }

}
