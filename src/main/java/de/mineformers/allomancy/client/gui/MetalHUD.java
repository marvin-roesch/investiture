package de.mineformers.allomancy.client.gui;

import com.google.common.base.Optional;
import de.mineformers.allomancy.Allomancy;
import de.mineformers.allomancy.client.KeyBindings;
import de.mineformers.allomancy.client.render.Shader;
import de.mineformers.allomancy.metal.AllomanticMetal;
import de.mineformers.allomancy.metal.AllomanticMetals;
import de.mineformers.allomancy.metal.MetalBurner;
import de.mineformers.allomancy.metal.MetalStorage;
import de.mineformers.allomancy.network.messages.ToggleBurningMetal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.Arrays;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * MetalHUD
 *
 * @author PaleoCrafter
 */
public class MetalHUD {
    public static final String[] METALS = {
            "bronze", "brass", "copper", "zinc", "tin", "iron", "pewter", "steel",
            "duralumin", "nicrosil", "aluminium", "chromium", "gold", "cadmium", "electrum", "bendalloy"
    };
    public static final ResourceLocation[] METAL_TEXTURES = (ResourceLocation[])
            Arrays.stream(METALS)
                    .map(m -> new ResourceLocation(Allomancy.MOD_ID, "textures/metals/" + m + ".png"))
                    .toArray(ResourceLocation[]::new);
    public static final ResourceLocation WHEEL_BG_TEXTURE = new ResourceLocation(Allomancy.MOD_ID, "textures/gui/wheel_background.png");
    public static final ResourceLocation WHEEL_TEXTURE = new ResourceLocation(Allomancy.MOD_ID, "textures/gui/wheel.png");
    private double mouseX, mouseY;
    private Optional<AllomanticMetal> hoveredMetal = Optional.absent();
    private boolean display;
    private Vec3 previousAlignment = new Vec3(0, 0, 0);
    private final Shader wheelShader = new Shader("/assets/allomancy/shaders/metal_wheel.vert", "/assets/allomancy/shaders/metal_wheel.frag");
    private final Shader iconShader = new Shader(null, "/assets/allomancy/shaders/metal_icon.frag");

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !display)
            return;

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        Minecraft.getMinecraft().getTextureManager().bindTexture(WHEEL_BG_TEXTURE);
        rect(centerX - 100, centerY - 100, 0, 0, 1, 1, 200, 200);

        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();

        wheelShader.activate();
        wheelShader.setUniformFloat("corner", centerX - 90, centerY - 90);
        wheelShader.setUniformFloat("scale", sr.getScaleFactor());
        wheelShader.setUniformFloat("time", (System.currentTimeMillis() % 72000) / 7200f);
        wheelShader.setUniformInt("octavesMin", 0);
        wheelShader.setUniformInt("octavesMax", 5);
        wheelShader.setUniformFloat("persistence", 1f);
        wr.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(centerX, centerY, 0).color(1, 1, 1, 0.3f).endVertex();
        for (int i = 100; i >= 0; i--) {
            double angle = PI * 2 * i / 100;
            wr.pos(centerX + cos(angle) * 60, centerY + sin(angle) * 60, 0).color(1, 1, 1, 0.3f).endVertex();
        }
        tess.draw();

        GlStateManager.shadeModel(GL_SMOOTH);
        wr.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(centerX + Math.cos(0) * 60, centerY + Math.sin(0) * 60, 0).color(1, 1, 1, 0.3f).endVertex();
        for (int i = -1; i <= 99; i++) {
            double angle0 = Math.PI * 2 * i / 100;
            double angle1 = Math.PI * 2 * (i + 1) / 100;
            wr.pos(centerX + Math.cos(angle1) * 60, centerY + Math.sin(angle1) * 60, 0).color(1, 1, 1, 0.3f).endVertex();
            wr.pos(centerX + Math.cos(angle0) * 90, centerY + Math.sin(angle0) * 90, 0).color(1, 1, 1, 0f).endVertex();
        }
        tess.draw();
        GlStateManager.shadeModel(GL_FLAT);
        wheelShader.deactivate();

        GlStateManager.enableTexture2D();

        GlStateManager.color(1, 1, 1, 1);

        MetalBurner burner = MetalBurner.from(Minecraft.getMinecraft().thePlayer);
        iconShader.activate();
        iconShader.setUniformInt("tex", 0);
        iconShader.setUniformFloat("deltaBrightness", 0.1f);
        iconShader.setUniform("hoveredColor", new Vec3(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("metalColor", new Vec3(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("impurityColor", new Vec3(141 / 255f, 19 / 255f, 171 / 255f));
        for (int i = 0; i < METALS.length / 2; i++) {
            double angle = Math.PI / 4 * (i + 0.5);
            AllomanticMetal innerMetal = AllomanticMetals.get(METALS[i * 2]).get();
            iconShader.setUniformBool("hovered", hoveredMetal.orNull() == innerMetal);
            iconShader.setUniform("backColor",
                    burner.isBurning(innerMetal) ?
                            new Vec3(205 / 255f, 43 / 255f, 0) :
                            new Vec3(0.1f, 0.1f, 0.1f));
            iconShader.setUniformFloat("metalLevel", (float) burner.get(innerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(innerMetal) / MetalStorage.MAX_STORAGE);
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES[i * 2]);
            rect(
                    centerX + (int) (Math.cos(angle) * 40) - 8,
                    centerY - (int) (Math.sin(angle) * 40) - 8, 0, 0, 1, 1, 16, 16);

            AllomanticMetal outerMetal = AllomanticMetals.get(METALS[i * 2 + 1]).get();
            iconShader.setUniformBool("hovered", hoveredMetal.orNull() == outerMetal);
            iconShader.setUniform("backColor",
                    burner.isBurning(outerMetal) ?
                            new Vec3(205 / 255f, 43 / 255f, 0) :
                            new Vec3(0.1f, 0.1f, 0.1f));
            iconShader.setUniformFloat("metalLevel", (float) burner.get(outerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(outerMetal) / MetalStorage.MAX_STORAGE);
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES[i * 2 + 1]);
            rect(
                    centerX + (int) (Math.cos(angle) * 75) - 8,
                    centerY - (int) (Math.sin(angle) * 75) - 8, 0, 0, 1, 1, 16, 16);
        }
        iconShader.deactivate();

        Minecraft.getMinecraft().getTextureManager().bindTexture(WHEEL_TEXTURE);
        rect(centerX - 100, centerY - 100, 0, 0, 1, 1, 200, 200);

        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        if (hoveredMetal.isPresent()) {
            AllomanticMetal metal = hoveredMetal.get();
            String text = I18n.format(metal.unlocalizedName());
            int width = font.getStringWidth(text);
            font.drawString(text, centerX - width / 2, centerY - font.FONT_HEIGHT / 2 + 2, 0xFF_FA_FA_FA);
        }
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event) {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        if (event.phase == TickEvent.Phase.START) {
            if (KeyBindings.SHOW_DIAL.isKeyDown() && !display) {
                previousAlignment = new Vec3(entity.rotationYaw, entity.getRotationYawHead(), entity.rotationPitch);
                display = true;
            } else if (!KeyBindings.SHOW_DIAL.isKeyDown() && display) {
                display = false;
            }
            if (KeyBindings.SHOW_DIAL.isKeyDown()) {
                Mouse.getDX();
                Mouse.getDY();
                Minecraft.getMinecraft().mouseHelper.deltaX = Minecraft.getMinecraft().mouseHelper.deltaY = 0;
                entity.rotationYaw = entity.prevRotationYaw = (float) previousAlignment.xCoord;
                entity.setRotationYawHead((float) previousAlignment.xCoord);
                if (entity instanceof EntityLivingBase)
                    ((EntityLivingBase) entity).prevRotationYawHead = (float) previousAlignment.xCoord;
                entity.rotationPitch = entity.prevRotationPitch = (float) previousAlignment.zCoord;
            }
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        mouseX -= event.dx / 400d;
        mouseY += event.dy / 400d;
        double mag = Math.sqrt(mouseX * mouseX + mouseY * mouseY);
        if (mag > 1) {
            mouseX /= mag;
            mouseY /= mag;
        }

        mag = Math.sqrt(mouseX * mouseX + mouseY * mouseY);
        double angle = (450 + Math.toDegrees(Math.atan2(mouseX, mouseY))) % 360;
        hoveredMetal = Optional.absent();
        for (int i = 0; i < METALS.length / 2; i++) {
            if (angle > i * 45 && angle <= (i + 1) * 45 && mag > 0.4 && mag <= 0.77)
                hoveredMetal = AllomanticMetals.get(METALS[i * 2]);
            if (angle > i * 45 && angle <= (i + 1) * 45 && mag > 0.77 && mag <= 1)
                hoveredMetal = AllomanticMetals.get(METALS[i * 2 + 1]);
        }

        if (event.button == 0 && event.buttonstate && hoveredMetal.isPresent())
            Allomancy.net().sendToServer(new ToggleBurningMetal(hoveredMetal.get().id()));

        if (display)
            event.setCanceled(true);
    }

    public void rect(int x, int y, float uMin, float vMin, float uMax, float vMax, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0).tex(uMin, vMax).endVertex();
        worldrenderer.pos(x + width, y + height, 0).tex(uMax, vMax).endVertex();
        worldrenderer.pos(x + width, y, 0).tex(uMax, vMin).endVertex();
        worldrenderer.pos(x, y, 0).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }
}
