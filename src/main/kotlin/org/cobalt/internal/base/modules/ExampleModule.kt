package org.cobalt.internal.base.modules

import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.client.TickEvent
import org.cobalt.api.module.Module
import org.cobalt.api.module.setting.impl.CheckboxSetting
import org.cobalt.api.module.setting.impl.SliderSetting
import org.cobalt.api.module.setting.impl.TextSetting
import org.cobalt.api.util.ChatUtils

/**
 * Example module demonstrating Cobalt module development best practices.
 *
 * This module serves as a comprehensive reference for addon developers,
 * showcasing how to:
 * - Create a module by extending [Module]
 * - Define settings using the property delegate pattern
 * - Subscribe to and handle events
 * - Interact with the Minecraft client safely
 *
 * ## Module Basics
 *
 * Modules are the primary building blocks of Cobalt addons. Each module:
 * 1. Extends the [Module] class with a display name
 * 2. Defines settings that users can configure
 * 3. Contains logic that responds to game events
 *
 * ## Creating a Module
 *
 * ```kotlin
 * object MyModule : Module("My Module") {
 *     // Settings and logic here
 * }
 * ```
 *
 * Use Kotlin `object` for modules to ensure singleton behavior.
 * The string passed to [Module] constructor is the display name shown in the UI.
 *
 * ## Settings
 *
 * Cobalt provides several setting types in `org.cobalt.api.module.setting.impl`:
 *
 * | Type | Description | Example |
 * |------|-------------|---------|
 * | [CheckboxSetting] | Boolean toggle | Enable/disable features |
 * | [SliderSetting] | Numeric value with min/max | Speed, range, etc. |
 * | [TextSetting] | String input | Custom messages, names |
 * | `ModeSetting` | Selection from predefined options | Algorithm choice |
 * | `ColorSetting` | Color picker | UI colors |
 * | `KeyBindSetting` | Key binding | Hotkeys |
 * | `RangeSetting` | Min/max numeric range | Random delays |
 * | `InfoSetting` | Read-only information display | Status text |
 *
 * Settings use Kotlin property delegates for clean syntax:
 * ```kotlin
 * private var myEnabled by CheckboxSetting(
 *     name = "Enabled",
 *     description = "Toggle this feature",
 *     defaultValue = false
 * )
 * ```
 *
 * ## Event Handling
 *
 * To respond to game events:
 * 1. Register with [EventBus] in an `init` block
 * 2. Create methods annotated with [@SubscribeEvent][SubscribeEvent]
 * 3. The method receives the event as its single parameter
 *
 * ```kotlin
 * init {
 *     EventBus.register(this)
 * }
 *
 * @SubscribeEvent
 * fun onTick(event: TickEvent.Start) {
 *     // Handle tick event
 * }
 * ```
 *
 * Available events include:
 * - [TickEvent.Start] / [TickEvent.End] - Game tick (20 times per second)
 * - `ChatEvent.Send` / `ChatEvent.Receive` - Chat messages
 * - `PacketEvent.Send` / `PacketEvent.Receive` - Network packets
 * - `WorldRenderEvent` - World rendering
 * - `NvgEvent` - NanoVG UI rendering
 *
 * ## Best Practices
 *
 * 1. **Always check for null**: `mc.player` and `mc.level` may be null
 * 2. **Use ChatUtils for feedback**: Provides consistent styling
 * 3. **Keep tick handlers lightweight**: They run 20 times per second
 * 4. **Document your settings**: Use clear names and descriptions
 * 5. **Unregister when appropriate**: Call `EventBus.unregister(this)` if needed
 *
 * @see Module
 * @see EventBus
 * @see CheckboxSetting
 * @see SliderSetting
 * @see TextSetting
 */
object ExampleModule : Module("Example") {

  /**
   * Master toggle for this module's functionality.
   *
   * [CheckboxSetting] is used for boolean (on/off) options.
   * When false, the [onTick] handler will skip processing.
   *
   * Parameters:
   * - **name**: Display name in the UI
   * - **description**: Tooltip text explaining the setting
   * - **defaultValue**: Initial value when first loaded
   */
  private var enabled by CheckboxSetting(
    name = "Enabled",
    description = "Enable or disable the example module functionality",
    defaultValue = false
  )

  /**
   * Controls how often the module sends a message (in ticks).
   *
   * [SliderSetting] is used for numeric values within a range.
   * The value is clamped between [min] and [max] automatically.
   *
   * Note: 20 ticks = 1 second in Minecraft.
   *
   * Parameters:
   * - **name**: Display name in the UI
   * - **description**: Tooltip text explaining the setting
   * - **defaultValue**: Initial value when first loaded
   * - **min**: Minimum allowed value
   * - **max**: Maximum allowed value
   */
  private var intervalTicks by SliderSetting(
    name = "Interval (ticks)",
    description = "How often to display the example message (20 ticks = 1 second)",
    defaultValue = 100.0,
    min = 20.0,
    max = 200.0
  )

  /**
   * Custom message displayed when the interval is reached.
   *
   * [TextSetting] is used for string input fields.
   * Users can enter any text value.
   *
   * Parameters:
   * - **name**: Display name in the UI
   * - **description**: Tooltip text explaining the setting
   * - **defaultValue**: Initial value when first loaded
   */
  private var customMessage by TextSetting(
    name = "Message",
    description = "Custom message to display in chat",
    defaultValue = "Hello from ExampleModule!"
  )

  /**
   * Tick counter for interval-based actions.
   *
   * This tracks how many ticks have passed since the last action.
   * When it reaches [intervalTicks], we perform the action and reset.
   */
  private var tickCounter = 0

  /**
   * Module initialization block.
   *
   * The `init` block runs when the module object is first accessed.
   * Use it to register with [EventBus] so event methods are called.
   *
   * Important: Always register with EventBus if your module uses
   * [@SubscribeEvent][SubscribeEvent] annotated methods.
   */
  init {
    EventBus.register(this)
  }

  /**
   * Called at the start of every game tick (20 times per second).
   *
   * This method demonstrates:
   * 1. Using a setting to control whether logic runs
   * 2. Implementing interval-based actions with a tick counter
   * 3. Safely interacting with game state
   *
   * The [@SubscribeEvent][SubscribeEvent] annotation marks this method
   * as an event listener. The event type is determined by the parameter type.
   *
   * Available priority levels (higher = called first):
   * - Priority 3: High priority
   * - Priority 2: Normal priority (default)
   * - Priority 1: Low priority
   *
   * @param event The tick event (unused but required for event dispatch)
   */
  @SubscribeEvent
  fun onTick(event: TickEvent.Start) {
    if (!enabled) {
      tickCounter = 0
      return
    }

    tickCounter++

    if (tickCounter >= intervalTicks.toInt()) {
      tickCounter = 0
      ChatUtils.sendMessage(customMessage)
    }
  }

}
