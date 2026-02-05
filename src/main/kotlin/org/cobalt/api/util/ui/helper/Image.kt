package org.cobalt.api.util.ui.helper

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import org.cobalt.api.util.setupConnection
import org.lwjgl.system.MemoryUtil

/**
 * Implementation from OdinFabric
 * Original work: https://github.com/odtheking/OdinFabric
 *
 * @author OdinFabric
 */
class Image(
  val identifier: String,
  var isSVG: Boolean = false,
  var stream: InputStream = getStream(identifier),
  private var buffer: ByteBuffer? = null,
) {

  init {
    isSVG = identifier.endsWith(".svg", true)
  }

  /**
   * Gets the image data as a ByteBuffer, loading it if not already cached.
   *
   * The buffer is allocated using off-heap memory and must be manually freed
   * when no longer needed. The data is cached after the first call, so subsequent
   * calls return the same buffer without reloading.
   *
   * @return ByteBuffer containing the raw image data
   * @throws IllegalStateException if the buffer is null after attempting to load
   */
  fun buffer(): ByteBuffer {
    if (buffer == null) {
      val bytes = stream.readBytes()
      buffer = MemoryUtil.memAlloc(bytes.size).put(bytes).flip() as ByteBuffer
      stream.close()
    }
    return buffer ?: throw IllegalStateException("Image has no data")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Image) return false
    return identifier == other.identifier
  }

  override fun hashCode(): Int {
    return identifier.hashCode()
  }

  companion object {

    /**
     * Creates an input stream for the given path.
     *
     * @param path The image path (URL, file path, or resource path)
     * @return InputStream for reading the image data
     * @throws FileNotFoundException if the resource cannot be found
     */
    private fun getStream(path: String): InputStream {
      val trimmedPath = path.trim()
      return if (trimmedPath.startsWith("http")) setupConnection(trimmedPath)
      else {
        val file = File(trimmedPath)
        if (file.exists() && file.isFile) Files.newInputStream(file.toPath())
        else this::class.java.getResourceAsStream(trimmedPath) ?: throw FileNotFoundException(trimmedPath)
      }
    }

  }

}
