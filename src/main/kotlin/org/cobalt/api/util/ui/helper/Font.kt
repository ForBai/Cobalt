package org.cobalt.api.util.ui.helper

import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Implementation from OdinFabric
 * Original work: https://github.com/odtheking/OdinFabric
 *
 * @author OdinFabric
 */
class Font(val name: String, private val resourcePath: String) {

  private val cachedBytes: ByteArray? = null

  /**
   * Creates a new ByteBuffer containing the font data.
   *
   * This method loads the font file from resources (if not already cached) and wraps
   * it in a direct ByteBuffer with native byte ordering. Note that this creates a new
   * buffer each time it's called, so you should cache the result if you need to use
   * it multiple times.
   *
   * The ByteBuffer is allocated as a direct buffer, which means it uses off-heap memory
   * for better performance when passing to native libraries like NanoVG.
   *
   * @return A new direct ByteBuffer containing the font data in native byte order
   * @throws FileNotFoundException if the font resource cannot be found at the specified path
   */
  fun buffer(): ByteBuffer {
    val bytes = cachedBytes ?: run {
      val stream = this::class.java.getResourceAsStream(resourcePath) ?: throw FileNotFoundException(resourcePath)
      stream.use { it.readBytes() }
    }
    return ByteBuffer.allocateDirect(bytes.size)
      .order(ByteOrder.nativeOrder())
      .put(bytes)
      .flip() as ByteBuffer
  }

  override fun hashCode(): Int {
    return name.hashCode()
  }

  override fun equals(other: Any?): Boolean {
    return other is Font && name == other.name
  }

}
