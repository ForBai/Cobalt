package org.cobalt.internal.ui.screen

import net.minecraft.client.gui.Click
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.cobalt.Cobalt.mc
import org.cobalt.api.event.EventBus
import org.cobalt.api.event.annotation.SubscribeEvent
import org.cobalt.api.event.impl.render.NvgEvent
import org.cobalt.api.module.Category
import org.cobalt.api.module.ModuleManager
import org.cobalt.api.util.TickScheduler
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.helper.Config
import org.cobalt.internal.ui.animation.EaseInOutAnimation
import org.cobalt.internal.ui.component.impl.CategoryComponent
import org.cobalt.internal.ui.component.impl.ModuleComponent
import org.cobalt.internal.ui.util.Constants
import org.cobalt.internal.ui.util.ScrollHandler
import org.cobalt.internal.ui.util.isHoveringOver

internal object ConfigScreen : Screen(Text.empty()) {

  /** Needed for opening animation */
  private val openAnim = EaseInOutAnimation(400)
  private var wasClosed = true

  private val categories = mutableListOf<CategoryComponent>()
  private val modules = mutableListOf<ModuleComponent>()

  private var selectedCategory: Category = ModuleManager.getCategories().first()

  private val categoryScroll = ScrollHandler()
  private val moduleScroll = ScrollHandler()

  fun setSelectedCategory(category: Category) {
    if (category != selectedCategory) {
      selectedCategory = category
      updateRenderedModules()
      moduleScroll.reset()
    }
  }

  fun updateRenderedModules() {
    modules.clear()
    modules.addAll(
      ModuleManager.getModules()
        .filter { it.category == selectedCategory }
        .map { ModuleComponent(it) }
    )
  }

  init {
    EventBus.register(this)

    categories.addAll(ModuleManager.getCategories().map {
      CategoryComponent(it)
    })

    updateRenderedModules()
  }

  @Suppress("unused")
  @SubscribeEvent
  fun onRender(event: NvgEvent) {
    if (mc.currentScreen != this)
      return

    val width = mc.window.width.toFloat()
    val height = mc.window.height.toFloat()

    val startX = (width / 2) - (Constants.BASE_WIDTH / 2)
    val startY = (height / 2) - (Constants.BASE_HEIGHT / 2)

    val categoryContentHeight = categories.size * 40f
    val moduleRows = (modules.size + 2) / 3
    val moduleContentHeight = moduleRows * (Constants.MODULE_HEIGHT + 10f)

    categoryScroll.setMaxScroll(categoryContentHeight, Constants.SIDEBAR_HEIGHT - 80f)
    moduleScroll.setMaxScroll(moduleContentHeight, Constants.BODY_HEIGHT)

    NVGRenderer.beginFrame(width, height)

    if (openAnim.isAnimating()) {
      val scale = openAnim.get(0f, 1f)
      NVGRenderer.translate(width / 2, height / 2)
      NVGRenderer.scale(scale, scale)
      NVGRenderer.translate(-width / 2, -height / 2)
    }

    drawSidebar(startX, startY)
    drawTopbar(startX, startY)
    drawBody(startX, startY)

    NVGRenderer.endFrame()
  }

  private fun drawSidebar(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX, startY,
      Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 6F
    )

    NVGRenderer.textWidth("cb", 20F).let {
      NVGRenderer.text(
        "cb",
        startX + (Constants.SIDEBAR_WIDTH / 2) - (it / 2),
        startY + 20F,
        20F, Constants.COLOR_WHITE.rgb
      )

      NVGRenderer.line(
        startX + (Constants.SIDEBAR_WIDTH / 2) - (it / 2),
        startY + 60F,
        startX + (Constants.SIDEBAR_WIDTH / 2) + (it / 2),
        startY + 60F,
        1F, Constants.COLOR_BORDER.rgb
      )
    }

    NVGRenderer.pushScissor(
      startX, startY + 80f,
      Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT - 80f
    )

    for ((index, category) in categories.withIndex()) {
      category.draw(
        startX + (Constants.SIDEBAR_WIDTH / 2) - 10f,
        startY + 80f + index * 40f - categoryScroll.getOffset(),
        selectedCategory
      )
    }

    NVGRenderer.popScissor()
  }

  private fun drawTopbar(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX + Constants.SIDEBAR_WIDTH + 3F,
      startY,
      Constants.TOPBAR_WIDTH, Constants.TOPBAR_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 6F
    )

    NVGRenderer.text(
      selectedCategory.name,
      startX + Constants.SIDEBAR_WIDTH + 23F,
      startY + (Constants.TOPBAR_HEIGHT / 2) - 9F,
      18F, Constants.COLOR_WHITE.rgb
    )
  }

  private fun drawBody(startX: Float, startY: Float) {
    NVGRenderer.rect(
      startX + Constants.SIDEBAR_WIDTH + 3F,
      startY + Constants.TOPBAR_HEIGHT + 3F,
      Constants.BODY_WIDTH, Constants.BODY_HEIGHT,
      Constants.COLOR_BACKGROUND.rgb, 6F
    )

    NVGRenderer.pushScissor(
      startX + Constants.SIDEBAR_WIDTH + 3f,
      startY + Constants.TOPBAR_HEIGHT + 3f,
      Constants.BODY_WIDTH, Constants.BODY_HEIGHT
    )

    for ((index, module) in modules.withIndex()) {
      val col = index % 3
      val row = index / 3
      module.draw(
        startX + Constants.SIDEBAR_WIDTH + 3f + 5.5f + col * (Constants.MODULE_WIDTH + 5.5f),
        startY + Constants.TOPBAR_HEIGHT + 10f + row * (Constants.MODULE_HEIGHT + 10f) - moduleScroll.getOffset()
      )
    }

    NVGRenderer.popScissor()
  }

  override fun mouseScrolled(
    amount: Double,
    mouseY: Double,
    horizontalAmount: Double,
    verticalAmount: Double
  ): Boolean {
    val width = mc.window.width.toFloat()
    val height = mc.window.height.toFloat()
    val startX = (width / 2) - (Constants.BASE_WIDTH / 2)
    val startY = (height / 2) - (Constants.BASE_HEIGHT / 2)

    if (
      isHoveringOver(
        startX, startY + 80f,
        Constants.SIDEBAR_WIDTH, Constants.SIDEBAR_HEIGHT - 80f
      )
    ) {
      categoryScroll.handleScroll(verticalAmount)
      return true
    }

    if (
      isHoveringOver(
        startX + Constants.SIDEBAR_WIDTH + 3f,
        startY + Constants.TOPBAR_HEIGHT + 3f,
        Constants.BODY_WIDTH, Constants.BODY_HEIGHT
      )
    ) {
      moduleScroll.handleScroll(verticalAmount)
      return true
    }

    return super.mouseScrolled(amount, mouseY, horizontalAmount, verticalAmount)
  }

  override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
    for (module in modules) {
      if (module.onClick(click.button())) {
        return true
      }
    }

    for (category in categories) {
      if (category.onClick(click.button())) {
        return true
      }
    }

    return super.mouseClicked(click, doubled)
  }

  override fun shouldPause(): Boolean {
    return false
  }

  override fun init() {
    if (wasClosed) {
      openAnim.start()
      wasClosed = false
    }

    super.init()
  }

  override fun close() {
    Config.saveModulesConfig()
    wasClosed = true
    super.close()
  }

  fun openUI() {
    TickScheduler.schedule(1) {
      mc.setScreen(this)
    }
  }

}
