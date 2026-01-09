package org.cobalt.internal.ui.notification

import java.awt.Color
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.UIComponent
import org.cobalt.internal.ui.animation.EaseOutAnimation

internal class UINotification(
  private val title: String,
  private val description: String,
  private val duration: Long = 5000L,
) : UIComponent(
  x = 0F,
  y = 0F,
  width = 350F,
  height = 100F
) {

  private var createdAt = System.currentTimeMillis()
  private val slideAnim = EaseOutAnimation(150L)
  private var isClosing = false
  private val descriptionMaxWidth = width - 30F

  private val wrappedDescription: List<String> by lazy {
    wrapText(description, descriptionMaxWidth, 12F)
  }

  fun getNotificationHeight(): Float =
    wrappedDescription.size * 16F + 52F

  init {
    slideAnim.start()
  }

  @Suppress("SameParameterValue")
  private fun wrapText(text: String, maxWidth: Float, fontSize: Float): List<String> {
    val lines = mutableListOf<String>()
    val words = text.split(" ")
    var currentLine = ""

    for (word in words) {
      val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
      val estimatedWidth = NVGRenderer.textWidth(testLine, fontSize)

      if (estimatedWidth <= maxWidth) {
        currentLine = testLine
      } else {
        if (currentLine.isNotEmpty()) {
          lines.add(currentLine)
        }
        currentLine = word
      }
    }

    if (currentLine.isNotEmpty()) {
      lines.add(currentLine)
    }

    return lines
  }

  fun getOffsetX(): Float {
    return if (isClosing) {
      slideAnim.get(0F, width)
    } else {
      width - slideAnim.get(0F, width)
    }
  }

  fun shouldRemove(): Boolean {
    val elapsed = System.currentTimeMillis() - createdAt
    return elapsed > duration + 150L && isClosing && !slideAnim.isAnimating()
  }

  fun startClosing() {
    if (!isClosing) {
      isClosing = true
      slideAnim.start()
    }
  }

  override fun render() {
    val offsetX = getOffsetX()
    val finalX = x + offsetX
    val finalHeight = getNotificationHeight()

    NVGRenderer.rect(
      finalX,
      y,
      width,
      finalHeight,
      Color(25, 25, 25).rgb,
      8F
    )

    NVGRenderer.hollowRect(
      finalX,
      y,
      width,
      finalHeight,
      1.5F,
      Color(61, 94, 149).rgb,
      8F
    )

    NVGRenderer.text(
      title,
      finalX + 15F,
      y + 23F,
      14F,
      Color(230, 230, 230).rgb
    )

    var yOffset = 37F

    for (line in wrappedDescription) {
      NVGRenderer.text(
        line,
        finalX + 15F,
        y + yOffset,
        12F,
        Color(179, 179, 179).rgb
      )

      yOffset += 16F
    }
  }

  fun getCreatedAt(): Long = createdAt
  fun getDuration(): Long = duration

}
