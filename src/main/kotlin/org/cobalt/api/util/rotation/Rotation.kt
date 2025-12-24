package org.cobalt.api.util.rotation

import net.minecraft.client.network.ClientPlayerEntity

interface Rotation {

  fun rotateTo(
    yaw: Float,
    pitch: Float,
    player: ClientPlayerEntity,
    parameters: RotationParameters,
  )

}
