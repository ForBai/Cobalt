package org.cobalt.internal.ui.screen

import java.awt.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.gui.screens.options.OptionsScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import org.cobalt.api.util.ui.NVGRenderer
import org.cobalt.internal.ui.util.isHoveringOver
import org.cobalt.api.util.render.ShaderProgram
import org.cobalt.api.util.render.FullScreenQuad
import org.lwjgl.opengl.GL11
import com.mojang.blaze3d.opengl.GlDevice
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL30

object UIHomescreen {

    private val mc: Minecraft = Minecraft.getInstance()
    
    private var shaderProgram: ShaderProgram? = null
    private var startTime: Long = 0L
    private var lastFrameTime: Long = 0L
    private var initialized = false
    
    private class HomeButton(
        val text: String, 
        val action: () -> Unit,
        var hoverProgress: Float = 0f
    )

    private val homeButtons = listOf(
        HomeButton("Singleplayer", { mc.setScreen(SelectWorldScreen(mc.screen!!)) }),
        HomeButton("Multiplayer", { mc.setScreen(JoinMultiplayerScreen(mc.screen!!)) }),
        HomeButton("Options", { mc.setScreen(OptionsScreen(mc.screen!!, mc.options)) }),
        HomeButton("Quit", { mc.stop() })
    )
    
    private const val HOVER_SPEED = 6f
    private const val HOVER_SCALE = 1.02f
    private const val GLOW_COLOR = 0x503D5E95

    fun init() {
        if (!initialized) {
            try {
                shaderProgram = ShaderProgram(
                    "/assets/cobalt/shaders/homescreen.vert",
                    "/assets/cobalt/shaders/homescreen.frag"
                )
                FullScreenQuad.init()
                startTime = System.currentTimeMillis()
                lastFrameTime = startTime
                initialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun render(unusedWidth: Float, unusedHeight: Float, mouseX: Double, mouseY: Double) {
        val window = mc.window
        val width = window.screenWidth.toFloat()
        val height = window.screenHeight.toFloat()
        
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastFrameTime) / 1000f
        lastFrameTime = currentTime
        
        renderShaderBackground(width, height, mouseX, mouseY)

        NVGRenderer.beginFrame(width, height)
        
        val title = "Cobalt"
        val titleWidth = NVGRenderer.textWidth(title, 48f, NVGRenderer.interFont)
        NVGRenderer.text(title, (width - titleWidth) / 2f, height / 4f, 48f, Color(230, 230, 230).rgb)

        val buttonWidth = 240f
        val buttonHeight = 40f
        val startY = height / 2f
        val spacing = 12f

        homeButtons.forEachIndexed { index, button ->
            val baseX = (width - buttonWidth) / 2f
            val baseY = startY + index * (buttonHeight + spacing)
            
            val isHovering = isHoveringOver(baseX, baseY, buttonWidth, buttonHeight)
            val targetProgress = if (isHovering) 1f else 0f
            button.hoverProgress = lerp(button.hoverProgress, targetProgress, HOVER_SPEED * deltaTime)
            
            if (button.hoverProgress < 0.01f) button.hoverProgress = 0f
            if (button.hoverProgress > 0.99f) button.hoverProgress = 1f
            
            val scale = 1f + (HOVER_SCALE - 1f) * button.hoverProgress
            val glowIntensity = button.hoverProgress
            
            val scaledWidth = buttonWidth * scale
            val scaledHeight = buttonHeight * scale
            val x = baseX - (scaledWidth - buttonWidth) / 2f
            val y = baseY - (scaledHeight - buttonHeight) / 2f
            
            if (button.hoverProgress > 0f) {
                NVGRenderer.glowRect(
                    x, y, scaledWidth, scaledHeight,
                    10f,
                    glowIntensity,
                    GLOW_COLOR
                )
            }
            
            val baseColor = Color(30, 30, 30)
            val hoverColor = Color(61, 94, 149)
            val buttonColor = lerpColor(baseColor, hoverColor, button.hoverProgress)
            
            NVGRenderer.rect(x, y, scaledWidth, scaledHeight, buttonColor.rgb, 10f)
            
            val textSize = 16f
            val textWidth = NVGRenderer.textWidth(button.text, textSize, NVGRenderer.interFont)
            val textX = x + (scaledWidth - textWidth) / 2f
            val textY = y + (scaledHeight - textSize) / 2f + 5f
            NVGRenderer.text(button.text, textX, textY, textSize, Color(230, 230, 230).rgb)
        }


        val toggleButtonWidth = 80f
        val toggleButtonHeight = 25f
        val toggleButtonX = width - toggleButtonWidth - 10f
        val toggleButtonY = 10f
        
        val isHoveringToggle = isHoveringOver(toggleButtonX, toggleButtonY, toggleButtonWidth, toggleButtonHeight)
        val toggleColor = if (isHoveringToggle) Color(61, 94, 149) else Color(30, 30, 30)
        
        NVGRenderer.rect(toggleButtonX, toggleButtonY, toggleButtonWidth, toggleButtonHeight, toggleColor.rgb, 6f)
        
        val toggleText = "Vanilla"
        val toggleTextSize = 14f
        val toggleTextWidth = NVGRenderer.textWidth(toggleText, toggleTextSize, NVGRenderer.interFont)
        NVGRenderer.text(
            toggleText,
            toggleButtonX + (toggleButtonWidth - toggleTextWidth) / 2f,
            toggleButtonY + (toggleButtonHeight - toggleTextSize) / 2f + 3f,
            toggleTextSize,
            Color(230, 230, 230).rgb
        )

        NVGRenderer.endFrame()
    }
    
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t.coerceIn(0f, 1f)
    }
    
    private fun lerpColor(start: Color, end: Color, t: Float): Color {
        val clampedT = t.coerceIn(0f, 1f)
        return Color(
            (start.red + (end.red - start.red) * clampedT).toInt(),
            (start.green + (end.green - start.green) * clampedT).toInt(),
            (start.blue + (end.blue - start.blue) * clampedT).toInt(),
            (start.alpha + (end.alpha - start.alpha) * clampedT).toInt()
        )
    }
    
    private fun renderShaderBackground(width: Float, height: Float, mouseX: Double, mouseY: Double) {
        val shader = shaderProgram ?: run {
            NVGRenderer.beginFrame(width, height)
            NVGRenderer.rect(0f, 0f, width, height, Color(18, 18, 18).rgb)
            NVGRenderer.endFrame()
            return
        }
        
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f
        
        val framebuffer = mc.mainRenderTarget
        val glFramebuffer = (framebuffer.colorTexture as GlTexture).getFbo(
            (RenderSystem.getDevice() as GlDevice).directStateAccess(),
            null
        )
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, glFramebuffer)
        GlStateManager._viewport(0, 0, framebuffer.width, framebuffer.height)
        
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
        
        shader.use()
        shader.setUniform("u_Time", elapsedTime)
        shader.setUniform("u_Resolution", width, height)
        shader.setUniform("u_Mouse", mouseX.toFloat(), mouseY.toFloat())
        
        FullScreenQuad.render()
        
        shader.unbind()
        
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    fun mouseClicked(event: MouseButtonEvent): Boolean {
        if (event.button() != 0) return false
        
        val window = mc.window
        val width = window.screenWidth.toFloat()
        val height = window.screenHeight.toFloat()
        val buttonWidth = 240f
        val buttonHeight = 40f
        val startY = height / 2f
        val spacing = 12f

        val toggleButtonWidth = 80f
        val toggleButtonHeight = 25f
        val toggleButtonX = width - toggleButtonWidth - 10f
        val toggleButtonY = 10f
        
        if (isHoveringOver(toggleButtonX, toggleButtonY, toggleButtonWidth, toggleButtonHeight)) {
            UIPreferences.setCustomHomescreenEnabled(false)
            mc.setScreen(mc.screen)
            return true
        }

        homeButtons.forEachIndexed { index, btn ->
            val x = (width - buttonWidth) / 2f
            val y = startY + index * (buttonHeight + spacing)
            if (isHoveringOver(x, y, buttonWidth, buttonHeight)) {
                btn.action()
                return true
            }
        }
        return false
    }
}
