package de.mineformers.allomancy.internal.client.gui

import de.mineformers.allomancy.Allomancy
import de.mineformers.allomancy.internal.client.KeyBindings
import de.mineformers.allomancy.internal.network.ToggleBurningMetal
import de.mineformers.allomancy.metal.{AllomanticMetal, AllomanticMetals, MetalBurner}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, Tessellator}
import net.minecraft.client.resources.I18n
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.client.event.{MouseEvent, RenderGameOverlayEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11._
import tessera.client.render.Shader
import tessera.math.{Vec2i, Vec2d, Vec3d}

/**
  * MetalHUD
  *
  * @author PaleoCrafter
  */
class MetalHUD {
  val metals = Seq("bronze", "brass", "copper", "zinc", "tin", "iron", "pewter", "steel",
    "duralumin", "nicrosil", "aluminium", "chromium", "gold", "cadmium", "electrum", "bendalloy")
  val metalTextures = metals.map(m => new ResourceLocation(Allomancy.ModId, s"textures/metals/$m.png"))
  val wheelTexture = new ResourceLocation(Allomancy.ModId, "textures/gui/wheel.png")
  val wheelInnerTexture = new ResourceLocation(Allomancy.ModId, "textures/gui/wheel_inner.png")
  var mousePos = Vec2d(0, 0)
  var hoveredMetal: Option[AllomanticMetal] = None
  var display = false
  var prevAlignment = Vec3d(0, 0, 0)
  lazy val shaderWheel = new Shader("/assets/allomancy/shaders/metal_wheel.vert", "/assets/allomancy/shaders/metal_wheel.frag")
  lazy val shaderIcons = new Shader(null, "/assets/allomancy/shaders/metal_icon.frag")

  @SubscribeEvent
  def onRenderOverlay(event: RenderGameOverlayEvent.Post): Unit = {
    if (event.`type` != ElementType.ALL || !display)
      return

    val sr = new ScaledResolution(Minecraft.getMinecraft)

    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
    GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    val tess = Tessellator.getInstance()
    val wr = tess.getWorldRenderer
    val center = Vec2i(sr.getScaledWidth / 2, sr.getScaledHeight / 2)

    Minecraft.getMinecraft.getTextureManager.bindTexture(wheelInnerTexture)
    rect(center.x - 100, center.y - 100, 0, 0, 1, 1, 200, 200)

    GlStateManager.disableTexture2D()
    GlStateManager.disableAlpha()

    shaderWheel.activate()
    shaderWheel.setUniform("corner", (center - Vec2i(90, 90)).toVec2d)
    shaderWheel.setUniformFloat("scale", sr.getScaleFactor)
    shaderWheel.setUniformFloat("time", (System.currentTimeMillis() % 72000) / 7200f)
    shaderWheel.setUniformInt("octavesMin", 0)
    shaderWheel.setUniformInt("octavesMax", 5)
    shaderWheel.setUniformFloat("persistence", 1f)
    wr.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR)
    wr.pos(center.x, center.y, 0).color(1, 1, 1, 0.3f).endVertex()
    for (i <- 100 to 0 by -1) {
      val angle = math.Pi * 2 * i / 100
      wr.pos(center.x + math.cos(angle) * 60, center.y + math.sin(angle) * 60, 0).color(1, 1, 1, 0.3f).endVertex()
    }
    tess.draw()

    GlStateManager.shadeModel(GL_SMOOTH)
    wr.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR)
    wr.pos(center.x + math.cos(0) * 60, center.y + math.sin(0) * 60, 0).color(1, 1, 1, 0.3f).endVertex()
    for (i <- -1 to 99) {
      val angle0 = math.Pi * 2 * i / 100
      val angle1 = math.Pi * 2 * (i + 1) / 100
      wr.pos(center.x + math.cos(angle1) * 60, center.y + math.sin(angle1) * 60, 0).color(1, 1, 1, 0.3f).endVertex()
      wr.pos(center.x + math.cos(angle0) * 90, center.y + math.sin(angle0) * 90, 0).color(1, 1, 1, 0.3f).endVertex()
    }
    tess.draw()
    GlStateManager.shadeModel(GL_FLAT)
    shaderWheel.deactivate()

    GlStateManager.enableTexture2D()

    GlStateManager.color(1, 1, 1, 1)

    val burner = MetalBurner(Minecraft.getMinecraft.thePlayer)
    shaderIcons.activate()
    shaderIcons.setUniformInt("tex", 0)
    shaderIcons.setUniformFloat("deltaBrightness", 0.1f)
    shaderIcons.setUniformFloat("level", 0.5f)
    shaderIcons.setUniform("hoveredColor", Vec3d(171 / 255f, 137 / 255f, 19 / 255f))
    for (i <- 0 until 8) {
      val angle = math.Pi / 4 * (i + 0.5)
      val innerMetal = AllomanticMetals(metals(i * 2)).get
      shaderIcons.setUniformBool("hovered", hoveredMetal.orNull == innerMetal)
      shaderIcons.setUniform("normalColor",
        if (burner.burning(innerMetal)) Vec3d(205 / 255f, 43 / 255f, 0)
        else Vec3d(0.1f, 0.1f, 0.1f))
      Minecraft.getMinecraft.getTextureManager.bindTexture(metalTextures(i * 2))
      rect(
        center.x + (math.cos(angle) * 40).toInt - 8,
        center.y - (math.sin(angle) * 40).toInt - 8, 0, 0, 1, 1, 16, 16)

      val outerMetal = AllomanticMetals(metals(i * 2 + 1)).get
      shaderIcons.setUniformBool("hovered", hoveredMetal.orNull == outerMetal)
      shaderIcons.setUniform("normalColor",
        if (burner.burning(outerMetal)) Vec3d(205 / 255f, 43 / 255f, 0)
        else Vec3d(0.1f, 0.1f, 0.1f))
      Minecraft.getMinecraft.getTextureManager.bindTexture(metalTextures(i * 2 + 1))
      rect(
        center.x + (math.cos(angle) * 75).toInt - 8,
        center.y - (math.sin(angle) * 75).toInt - 8, 0, 0, 1, 1, 16, 16)
    }
    shaderIcons.deactivate()

    Minecraft.getMinecraft.getTextureManager.bindTexture(wheelTexture)
    rect(center.x - 100, center.y - 100, 0, 0, 1, 1, 200, 200)

    val font = Minecraft.getMinecraft.fontRendererObj
    hoveredMetal match {
      case Some(metal) =>
        val text = I18n.format(metal.unlocalizedName)
        val width = font.getStringWidth(text)
        font.drawString(text, center.x - width / 2, center.y - font.FONT_HEIGHT / 2 + 2, 0xFFFAFAFA)
      case _ =>
    }
    GlStateManager.enableAlpha()
  }

  @SubscribeEvent
  def onClientTick(event: RenderTickEvent): Unit = {
    val entity = Minecraft.getMinecraft.getRenderViewEntity
    if (event.phase == TickEvent.Phase.START) {
      if (KeyBindings.ShowDial.isKeyDown && !display) {
        prevAlignment = Vec3d(entity.rotationYaw, entity.getRotationYawHead, entity.rotationPitch)
        display = true
      } else if (!KeyBindings.ShowDial.isKeyDown && display) {
        display = false
      }
      if (KeyBindings.ShowDial.isKeyDown) {
        Mouse.getDX()
        Mouse.getDY()
        Minecraft.getMinecraft.mouseHelper.deltaX = 0
        Minecraft.getMinecraft.mouseHelper.deltaY = 0
        entity.rotationYaw = prevAlignment.x.toFloat
        entity.prevRotationYaw = prevAlignment.x.toFloat
        entity.setRotationYawHead(prevAlignment.x.toFloat)
        entity match {
          case base: EntityLivingBase => base.prevRotationYawHead = prevAlignment.x.toFloat
          case _ =>
        }
        entity.rotationPitch = prevAlignment.z.toFloat
        entity.prevRotationPitch = prevAlignment.z.toFloat
      }
    }
  }

  @SubscribeEvent
  def onMouse(event: MouseEvent): Unit = {
    mousePos += Vec2d(-event.dx, event.dy) / 400
    val mag = mousePos.magnitude
    if (mag > 1)
      mousePos = mousePos / mag

    val mag2 = mousePos.magnitude
    val angle = (450 + math.toDegrees(math.atan2(mousePos.x, mousePos.y))) % 360
    hoveredMetal = None
    for (i <- 0 until 8) {
      if (angle > i * 45 && angle <= (i + 1) * 45 && mag2 > 0.4 && mag2 <= 0.77)
        hoveredMetal = AllomanticMetals(metals(i * 2))
      if (angle > i * 45 && angle <= (i + 1) * 45 && mag2 > 0.77 && mag2 <= 1)
        hoveredMetal = AllomanticMetals(metals(i * 2 + 1))
    }

    if (event.button == 0 && event.buttonstate)
      hoveredMetal match {
        case Some(m) => Allomancy.Net.sendToServer(ToggleBurningMetal(m.id))
        case _ =>
      }

    if (display)
      event.setCanceled(true)
  }

  def rect(x: Int, y: Int, uMin: Float, vMin: Float, uMax: Float, vMax: Float, width: Int, height: Int): Unit = {
    val tessellator = Tessellator.getInstance
    val worldrenderer = tessellator.getWorldRenderer
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
    worldrenderer.pos(x, y + height, 0).tex(uMin, vMax).endVertex()
    worldrenderer.pos(x + width, y + height, 0).tex(uMax, vMax).endVertex()
    worldrenderer.pos(x + width, y, 0).tex(uMax, vMin).endVertex()
    worldrenderer.pos(x, y, 0).tex(uMin, vMin).endVertex()
    tessellator.draw()
  }
}
