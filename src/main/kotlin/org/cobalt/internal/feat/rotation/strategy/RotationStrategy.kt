package org.cobalt.internal.feat.rotation.strategy

import net.minecraft.client.network.ClientPlayerEntity
import org.cobalt.internal.feat.rotation.DefaultRotationParameters

internal interface RotationStrategy {
  fun perform(
      yaw: Float,
      pitch: Float,
      player: ClientPlayerEntity,
      parameters: DefaultRotationParameters,
  )
}
