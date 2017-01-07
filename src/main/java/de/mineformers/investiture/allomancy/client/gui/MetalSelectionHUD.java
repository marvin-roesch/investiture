package de.mineformers.investiture.allomancy.client.gui;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.network.ToggleBurningMetal;
import de.mineformers.investiture.client.KeyBindings;
import de.mineformers.investiture.client.renderer.Shader;
import de.mineformers.investiture.client.util.Colour;
import de.mineformers.investiture.client.util.Guis;
import de.mineformers.investiture.client.util.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.mineformers.investiture.allomancy.api.metal.Metals.*;
import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Handles all required events for displaying the metal selection wheel.
 */
public class MetalSelectionHUD
{
    private static final Metal[] METALS = {
        BRONZE, BRASS, COPPER, ZINC, TIN, IRON, PEWTER, STEEL,
        DURALUMIN, NICROSIL, ALUMINIUM, CHROMIUM, GOLD, CADMIUM, ELECTRUM, BENDALLOY
    };
    private static final Map<Metal, ResourceLocation> METAL_TEXTURES =
        Metals.BASE_METALS.stream()
                          .collect(Collectors.toMap(m -> m, m -> new ResourceLocation(Allomancy.DOMAIN, "textures/metals/" + m.id() + ".png")));
    private Optional<Metal> hoveredMetal = Optional.empty();
    private boolean display;
    private final Shader iconShader = new Shader(null, new ResourceLocation(Allomancy.DOMAIN, "metal_icon"));

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (!display || event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;
        event.setCanceled(true);
        ScaledResolution resolution = event.getResolution();
        int centreX = resolution.getScaledWidth() / 2;
        int centreY = resolution.getScaledHeight() / 2;
        int mouseX = Guis.getMouseX(resolution);
        int mouseY = Guis.getMouseY(resolution);

        hoveredMetal = Optional.empty();
        for (int i = 0; i < METALS.length / 2; i++)
        {
            double angle = PI / 4 * (i + 0.5);
            int startX = centreX + (int) (cos(angle) * 45) - 8;
            int startY = centreY - (int) (sin(angle) * 45) - 8;
            int endX = startX + 16;
            int endY = startY + 16;
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY)
                hoveredMetal = Optional.of(METALS[i * 2]);
            angle = PI / 4 * (i + 0.5);
            startX = centreX + (int) (cos(angle) * 85) - 8;
            startY = centreY - (int) (sin(angle) * 85) - 8;
            endX = startX + 16;
            endY = startY + 16;
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY)
                hoveredMetal = Optional.of(METALS[i * 2 + 1]);
        }

        // We need blending or we won't get translucency
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Disable the alpha test such that everything will get drawn as translucently as desired
        GlStateManager.disableAlpha();
        drawRings(centreX, centreY);
        drawMetalIcons(centreX, centreY);

        // Draw the wheel's frame
//        Minecraft.getMinecraft().getTextureManager().bindTexture(WHEEL_TEXTURE);
//        Rendering.drawRectangle(centreX - 100, centreY - 100, 0, 0, 1, 1, 200, 200);

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
     * Draws the selection rings.
     *
     * @param centreX the x coordinate the rings should be centred on
     * @param centreY the y coordinate the rings should be centred on
     */
    private void drawRings(int centreX, int centreY)
    {
        GlStateManager.disableTexture2D();
        Rendering.drawRing(centreX, centreY, 30, 30, 8, 0, new Colour(0, 0, 0, 0.75f));
        Rendering.drawRing(centreX, centreY, 70, 30, 8, 0, new Colour(0, 0, 0, 0.75f));
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
        Allomancer allomancer = AllomancyAPIImpl.INSTANCE.toAllomancer(Minecraft.getMinecraft().player).get();
        // The icon shader will replace the colours of the metal icons entirely, depending on the amount stored of the respective metal
        iconShader.activate();
        iconShader.setUniformInt("tex", 0);
        iconShader.setUniformFloat("deltaBrightness", 0.1f);
        iconShader.setUniform("hoveredColour", new Vec3d(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("metalColour", new Vec3d(171 / 255f, 137 / 255f, 19 / 255f));
        iconShader.setUniform("impurityColour", new Vec3d(141 / 255f, 19 / 255f, 171 / 255f));
        for (int i = 0; i < METALS.length / 2; i++)
        {
            double angle = PI / 4 * (i + 0.5);
            Metal innerMetal = METALS[i * 2];
            iconShader.setUniformBool("hovered", hoveredMetal.orElse(null) == innerMetal);
            // Change the main colour of the icon if the metal is burning
            iconShader.setUniform("backColour", allomancer.activePowers().contains(innerMetal.mistingType()) ? new Vec3d(205 / 255f, 43 / 255f, 0)
                                                                                                             : new Vec3d(0.9f, 0.9f, 0.9f));
//            iconShader.setUniformFloat("metalLevel", (float) burner.get(innerMetal) / MetalStorage.MAX_STORAGE);
//            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(innerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("metalLevel", 0);
            iconShader.setUniformFloat("impurityLevel", 0);

            // The textures in the array are aligned in pairs of two
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES.get(innerMetal));
            // Draw the inner icon 45 units away from the circle's centre
            Rendering.drawRectangle(centreX + (int) (cos(angle) * 45) - 8,
                                    centreY - (int) (sin(angle) * 45) - 8, 0, 0, 1, 1, 16, 16);

            Metal outerMetal = METALS[i * 2 + 1];
            iconShader.setUniformBool("hovered", hoveredMetal.orElse(null) == outerMetal);
            // Change the main colour of the icon if the metal is burning
            iconShader.setUniform("backColour", allomancer.activePowers().contains(outerMetal.mistingType()) ? new Vec3d(205 / 255f, 43 / 255f, 0)
                                                                                                             : new Vec3d(0.9f, 0.9f, 0.9f));
//            iconShader.setUniformFloat("metalLevel", (float) burner.get(outerMetal) / MetalStorage.MAX_STORAGE);
//            iconShader.setUniformFloat("impurityLevel", (float) burner.getImpurity(outerMetal) / MetalStorage.MAX_STORAGE);
            iconShader.setUniformFloat("metalLevel", 0);
            iconShader.setUniformFloat("impurityLevel", 0);

            // The textures in the array are aligned in pairs of two
            Minecraft.getMinecraft().getTextureManager().bindTexture(METAL_TEXTURES.get(outerMetal));
            // Draw the outer icon 75 units away from the circle's centre
            Rendering.drawRectangle(centreX + (int) (cos(angle) * 85) - 8,
                                    centreY - (int) (sin(angle) * 85) - 8, 0, 0, 1, 1, 16, 16);
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
        // Only operate at the beginning of the tick, otherwise the player's head might move
        if (event.phase == TickEvent.Phase.START)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (KeyBindings.SHOW_DIAL.isKeyDown() && !display && mc.inGameHasFocus && mc.currentScreen == null)
            {
                display = true;
                mc.inGameHasFocus = false;
                mc.mouseHelper.ungrabMouseCursor();
            }
            else if (!KeyBindings.SHOW_DIAL.isKeyDown() && display)
            {
                display = false;
                if (mc.currentScreen == null)
                {
                    mc.inGameHasFocus = true;
                    mc.mouseHelper.grabMouseCursor();
                }
            }

            if (display && mc.currentScreen != null)
            {
                KeyBinding.setKeyBindState(KeyBindings.SHOW_DIAL.getKeyCode(), false);
                display = false;
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

        // The hovered metal was clicked,
        if (event.getButton() == 0 && event.isButtonstate() && hoveredMetal.isPresent())
            Investiture.net().sendToServer(new ToggleBurningMetal(hoveredMetal.get().id()));

        // We don't want anybody else interfering with our handling.
        event.setCanceled(true);
    }
}
