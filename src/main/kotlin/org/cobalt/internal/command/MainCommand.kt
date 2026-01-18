package org.cobalt.internal.command

import org.cobalt.api.command.Command
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand
import org.cobalt.api.notification.NotificationManager
import org.cobalt.api.rotation.EasingType
import org.cobalt.api.rotation.RotationExecutor
import org.cobalt.api.rotation.strategy.TimedEaseStrategy
import org.cobalt.api.util.helper.Rotation
import org.cobalt.internal.ui.screen.UIConfig

internal object MainCommand : Command(
  name = "cobalt",
  aliases = arrayOf("cb")
) {

  @DefaultHandler
  fun main() {
    UIConfig.openUI()
  }

  @SubCommand
  fun rotate(yaw: Double, pitch: Double, duration: Int) {
    RotationExecutor.rotateTo(
      Rotation(yaw.toFloat(), pitch.toFloat()),
      TimedEaseStrategy(
        yawEasing = EasingType.EASE_OUT_EXPO,
        pitchEasing = EasingType.EASE_OUT_EXPO,
        duration = duration.toLong()
      )
    )
  }

  @SubCommand
  fun notification(title: String, description: String) {
    NotificationManager.sendNotification(title, description)
  }

}
