package de.mineformers.investiture.allomancy.client.gui;

import com.google.common.base.Optional;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.metal.Metal;
import de.mineformers.investiture.allomancy.metal.Metals;
import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.allomancy.network.ToggleBurningMetal;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.client.renderer.Shader;
import de.mineformers.investiture.client.util.Rendering;
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
 * Handles all required events for displaying the metal selection wheel.
 */
public class MetalSelectionHUD
{
    public static final String[] METALS = {
        "bronze", "brass", "copper", "zinc", "tin", "iron", "pewter", "steel",
        "duralumin", "nicrosil", "aluminium", "chromium", "gold", "cadmium", "electrum", "bendalloy"
    };
    public static final ResourceLocation[] METAL_TEXTURES = (ResourceLocation[])
        Arrays.stream(METALS)
              .map(m -> new ResourceLocation(Allomancy.DOMAIN, "textures/metals/" + m + ".png"))
              .toArray(ResourceLocation[]::new);
    public static final ResourceLocation WHEEL_BG_TEXTURE = new ResourceLocation(Allomancy.DOMAIN, "textures/gui/wheel_background.png");
    public static final ResourceLocation WHEEL_TEXTURE = new ResourceLocation(Allomancy.DOMAIN, "textures/gui/wheel.png");
    private double mouseX, mouseY;
    private Optional<Metal> hoveredMetal = Optional.absent();
    private boolean display;
    private Vec3 previousRotation = new Vec3(0, 0, 0);
    private final Shader wheelShader = new Shader(new ResourceLocation(Allomancy.DOMAIN, "metal_wheel"),
                                                  new ResourceLocation(Allomancy.DOMAIN, "metal_wheel"));
    private final Shader iconShader = new Shader(null, new ResourceLocation(Allomancy.DOMAIN, "metal_icon"));

    /**
     * Renders the actual HUD
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event)
    {
        // We only need to draw if everything else on the screen was and only if the key binding is active
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !display)
            return;

        // We need the ScaledResolution for properly positioning objects disregarding the user's settings
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int centreX = sr.getScaledWidth() / 2;
        int centreY = sr.getScaledHeight() / 2;

        // We need blending or we won't get translucency
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Draws the wheel's background texture (the red metal part)
        Minecraft.getMinecraft().getTextureManager().bindTexture(WHEEL_BG_TEXTURE);
        Rendering.drawRectangle(centreX - 100, centreY - 100, 0, 0, 1, 1, 200, 200);

        // Disable the alpha test such that everything will get drawn as translucently as desired
        GlStateManager.disableAlpha();

        drawMist(centreX, centreY);

        drawMetalIcons(centreX, centreY);

        // Draw the wheel's frame
        Minecraft.getMinecraft().getTextureManager().bindTexture(WHEEL_TEXTURE);
        Rendering.drawRectangle(centreX - 100, centreY - 100, 0, 0, 1, 1, 200, 200);

        // Draw the hovered metal's name in the circle's centre
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        if (hoveredMetal.isPresent())
        {
            Metal metal = hoveredMetal.get();
            String text = I18n.format(metal.unlocalisedName());
            int width = font.getStringWidth(text);
            font.drawString(text, centreX - width / 2, centreY - font.FONT_HEIGHT / 2 + 2, 0xFF_FA_FA_FA);
        }
        GlStateManager.enableAlpha();
    }

    /**
     * Draws the mist effect using shaders.
     *
     * @param centreX the x coordinate the mist circle should be centred on
     * @param centreY the y coordinate the mist circle should be centred on
     */
    private void drawMist(int centreX, int centreY)
    {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        // We don't need textures for drawing the mist
        GlStateManager.disableTexture2D();

        // The wheel shader uses Perlin noise to generate cloud-like effects
        wheelShader.activate();
        wheelShader.setUniformFloat("corner", centreX - 90, centreY - 90);
        wheelShader.setUniformFloat("scale", sr.getScaleFactor());
        wheelShader.setUniformFloat("time", (System.currentTimeMillis() % 72000) / 7200f);
        wheelShader.setUniformInt("octavesMin", 0);
        wheelShader.setUniformInt("octavesMax", 5);
        wheelShader.setUniformFloat("persistence", 1f);
        // Draw a simple circle with a radius of 60
        // If shaders are not supported, players will just see a translucent white circle
        wr.begin(GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(centreX, centreY, 0)
          .color(1, 1, 1, 0.3f)
          .endVertex();
        for (int i = 100; i >= 0; i--)
        {
            double angle = PI * 2 * i / 100;
            wr.pos(centreX + cos(angle) * 60, centreY + sin(angle) * 60, 0)
              .color(1, 1, 1, 0.3f)
              .endVertex();
        }
        tess.draw();

        // Draw the outer part of the circle as triangle strip, resulting in a ring
        // Use the smooth shading model to create a gradient from translucent to transparent
        GlStateManager.shadeModel(GL_SMOOTH);
        wr.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(centreX + cos(0) * 60, centreY + sin(0) * 60, 0)
          .color(1, 1, 1, 0.3f)
          .endVertex();
        for (int i = -1; i <= 99; i++)
        {
            double angle0 = PI * 2 * i / 100;
            double angle1 = PI * 2 * (i + 1) / 100;
            wr.pos(centreX + cos(angle1) * 60, centreY + sin(angle1) * 60, 0)
              .color(1, 1, 1, 0.3f)
              .endVertex();
            wr.pos(centreX + cos(angle0) * 90, centreY + sin(angle0) * 90, 0)
              .color(1, 1, 1, 0f)
              .endVertex();
        }
        tess.draw();
        GlStateManager.shadeModel(GL_FLAT);
        wheelShader.deactivate();

        // We're about to draw textures again
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws the metal icons using shaders.
     *
     * @param centreX the x coordinate the mist circle should be centred on
     * @param centreY the y coordinate the mist circle should be centred on
     */
    private void drawMetalIcons(int centreX, int centreY)
    {
        GlStateManager.color(1, 1, 1, 1);
        MetalBurner burner = MetalBurner.from(Minecraft.getMinecraft().thePlayer);
        // The icon shader will replace the colours of the metal icons entirely, depending on the amount stored of the respective metal
        iconShader.activate();
        iconShader.setUniformInt("tex", 0);
        iconShader.setUniformFloat("deltaBrightness", 0.1f);
        iconShader.setUniform("hoveredColour", new Vec3(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("metalColour", new Vec3(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("impurityColour", new Vec3(141 / 255f, 19 / 255f, 171 / 255f));
        for (int i = 0; i < METALS.length / 2; i++)
        {
            double angle = PI / 4 * (i + 0.5);
            Metal innerMetal = Metals.get(METALS[i * 2]).get();
            iconShader.setUniformBool("hovered", hoveredMetal.orNull() == innerMetal);
            // Change the main colour of the icon if the metal is burning
            iconShader.setUniform("backColour", burner.isBurning(innerMetal) ? new Vec3(205 / 255f, 43 / 255f, 0)
                                                                            : new Vec3(0.1f, 0.1f, 0.1f));
            iconShader.setUniformFloat("metalLevel", (float) burner.get(innerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(innerMetal) / MetalStorage.MAX_STORAGE);

            // The textures in the array are aligned in pairs of two
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES[i * 2]);
            // Draw the inner icon 40 units away from the circle's centre
            Rendering.drawRectangle(centreX + (int) (cos(angle) * 40) - 8,
                 centreY - (int) (sin(angle) * 40) - 8, 0, 0, 1, 1, 16, 16);

            Metal outerMetal = Metals.get(METALS[i * 2 + 1]).get();
            iconShader.setUniformBool("hovered", hoveredMetal.orNull() == outerMetal);
            // Change the main colour of the icon if the metal is burning
            iconShader.setUniform("backColour", burner.isBurning(outerMetal) ? new Vec3(205 / 255f, 43 / 255f, 0)
                                                                            : new Vec3(0.1f, 0.1f, 0.1f));
            iconShader.setUniformFloat("metalLevel", (float) burner.get(outerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(outerMetal) / MetalStorage.MAX_STORAGE);

            // The textures in the array are aligned in pairs of two
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES[i * 2 + 1]);
            // Draw the outer icon 75 units away from the circle's centre
            Rendering.drawRectangle(centreX + (int) (cos(angle) * 75) - 8,
                                    centreY - (int) (sin(angle) * 75) - 8, 0, 0, 1, 1, 16, 16);
        }
        iconShader.deactivate();
    }

    /**
     * Checks the key binding and resets the player's view if the HUD is displayed.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
        // Only operate at the beginning of the tick, otherwise the player's head might move
        if (event.phase == TickEvent.Phase.START)
        {
            if (KeyBindings.SHOW_DIAL.isKeyDown() && !display)
            {
                // Store the player's rotation to keep them looking forward
                previousRotation = new Vec3(entity.rotationYaw, entity.getRotationYawHead(), entity.rotationPitch);
                display = true;
            }
            else if (!KeyBindings.SHOW_DIAL.isKeyDown() && display)
            {
                display = false;
            }

            // Reset the player's rotation
            if (KeyBindings.SHOW_DIAL.isKeyDown())
            {
                // Prevent Vanilla from reading meaningful values
                Mouse.getDX();
                Mouse.getDY();
                Minecraft.getMinecraft().mouseHelper.deltaX = Minecraft.getMinecraft().mouseHelper.deltaY = 0;

                // Reset all rotational values to their initial state
                entity.rotationYaw = entity.prevRotationYaw = (float) previousRotation.xCoord;
                entity.setRotationYawHead((float) previousRotation.xCoord);
                if (entity instanceof EntityLivingBase)
                    ((EntityLivingBase) entity).prevRotationYawHead = (float) previousRotation.xCoord;
                entity.rotationPitch = entity.prevRotationPitch = (float) previousRotation.zCoord;
            }
        }
    }

    /**
     * Determines the currently hovered metal.
     *
     * @param event the event triggering this method
     */
    @SubscribeEvent
    public void onMouse(MouseEvent event)
    {
        if (!display)
            return;

        // Artificially slow down the mouse
        mouseX -= event.dx / 400d;
        mouseY += event.dy / 400d;

        // Basic vector maths to determine distance from centre
        double mag = sqrt(mouseX * mouseX + mouseY * mouseY);
        if (mag > 1)
        {
            mouseX /= mag;
            mouseY /= mag;
        }

        mag = sqrt(mouseX * mouseX + mouseY * mouseY);
        double angle = (450 + toDegrees(atan2(mouseX, mouseY))) % 360;

        // Rest the hovered metal and check each metal individually
        hoveredMetal = Optional.absent();
        for (int i = 0; i < METALS.length / 2; i++)
        {
            if (angle > i * 45 && angle <= (i + 1) * 45 && mag > 0.4 && mag <= 0.77)
                hoveredMetal = Metals.get(METALS[i * 2]);
            if (angle > i * 45 && angle <= (i + 1) * 45 && mag > 0.77 && mag <= 1)
                hoveredMetal = Metals.get(METALS[i * 2 + 1]);
        }

        // The hovered metal was clicked,
        if (event.button == 0 && event.buttonstate && hoveredMetal.isPresent())
            Investiture.net().sendToServer(new ToggleBurningMetal(hoveredMetal.get().id()));

        // We don't want anybody else interfering with our handling.
        event.setCanceled(true);
    }
}
