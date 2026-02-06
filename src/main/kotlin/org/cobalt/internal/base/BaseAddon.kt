package org.cobalt.internal.base

import org.cobalt.api.addon.Addon
import org.cobalt.api.addon.AddonMetadata
import org.cobalt.api.module.Module
import org.cobalt.internal.base.modules.ExampleModule

/**
 * Internal base addon bundled with Cobalt.
 *
 * This addon provides built-in modules that ship with Cobalt core.
 * Unlike external addons (loaded from JAR files in config/cobalt/addons/),
 * this addon is compiled directly into Cobalt and loads before all external addons.
 *
 * ## For Addon Developers
 *
 * Use this as a reference for how addons are structured:
 * 1. Extend the [Addon] abstract class
 * 2. Implement [onLoad] for initialization logic
 * 3. Implement [onUnload] for cleanup logic
 * 4. Override [getModules] to return your addon's modules
 *
 * External addons should use a `cobalt.addon.json` file with metadata
 * similar to [metadata] defined here.
 *
 * @see Addon
 * @see ExampleModule
 */
object BaseAddon : Addon() {

  /**
   * Metadata for the internal addon.
   *
   * This mirrors the structure of `cobalt.addon.json` used by external addons.
   * For external addons, this metadata is loaded from the JSON file inside the JAR.
   *
   * Fields:
   * - **id**: Unique identifier (lowercase, no spaces)
   * - **name**: Display name shown in the UI
   * - **version**: Semantic version string
   * - **entrypoints**: List of fully-qualified class names that extend [Addon]
   * - **mixins**: List of mixin configuration files (empty for this internal addon)
   * - **icon**: Optional path to icon image (null for this addon)
   */
  val metadata = AddonMetadata(
    id = "cobalt-base",
    name = "Cobalt Base",
    version = "1.0.0",
    entrypoints = listOf("org.cobalt.internal.base.BaseAddon"),
    mixins = emptyList(),
    icon = null
  )

  /**
   * Called when the addon is loaded during Cobalt initialization.
   *
   * Use this method to:
   * - Initialize resources
   * - Register event listeners (if addon-wide, not module-specific)
   * - Perform one-time setup
   *
   * Note: Module-specific event registration should happen in the module itself.
   */
  override fun onLoad() {
    println("[Cobalt Base] Internal addon loaded")
  }

  /**
   * Called when the addon is unloaded.
   *
   * Use this method to:
   * - Clean up resources
   * - Unregister event listeners
   * - Save any persistent state
   *
   * Note: For the internal addon, this is typically a no-op since
   * the addon lifecycle matches Cobalt's lifecycle.
   */
  override fun onUnload() {
    // No-op for internal addon
  }

  /**
   * Returns the list of modules provided by this addon.
   *
   * Each module should be a Kotlin object (singleton) that extends [Module].
   * Modules are registered with [org.cobalt.api.module.ModuleManager] during
   * Cobalt initialization.
   *
   * @return List of module instances
   */
  override fun getModules(): List<Module> = listOf(
    ExampleModule
  )

}
