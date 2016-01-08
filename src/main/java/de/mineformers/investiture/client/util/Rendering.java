package de.mineformers.investiture.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

/**
 * Provides utility methods for rendering.
 */
public class Rendering
{
    /**
     * Draws a rectangle to the screen.
     *
     * @param x      the rectangle's upper left-hand corner's x coordinate
     * @param y      the rectangle's upper left-hand corner's y coordinate
     * @param uMin   the u (horizontal) coordinate of the rectangle's upper left-hand corner in the texture
     * @param vMin   the v (vertical) coordinate of the rectangle's lower right-hand corner in the texture
     * @param uMax   the u (horizontal) coordinate of the rectangle's upper left-hand corner in the texture
     * @param vMax   the v (vertical) coordinate of the rectangle's lower right-hand corner in the texture
     * @param width  the rectangle's width on screen
     * @param height the rectangle's height on screen
     */
    public static void drawRectangle(int x, int y, float uMin, float vMin, float uMax, float vMax, int width, int height)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0)
                     .tex(uMin, vMax)
                     .endVertex();
        worldrenderer.pos(x + width, y + height, 0)
                     .tex(uMax, vMax)
                     .endVertex();
        worldrenderer.pos(x + width, y, 0)
                     .tex(uMax, vMin)
                     .endVertex();
        worldrenderer.pos(x, y, 0)
                     .tex(uMin, vMin)
                     .endVertex();
        tessellator.draw();
    }

    /**
     * Renders a baked model into the world. Transformations should be applied before calling this method.
     *
     * @param model the model to draw
     */
    public static void drawModel(IFlexibleBakedModel model)
    {
        drawModel(model, 0xFFFFFFFF);
    }

    /**
     * Renders a tinted baked model into the world. Transformations should be applied before calling this method.
     *
     * @param model  the model to draw
     * @param colour the tint that should be applied to the model, format is 0xAABBGGRR
     */
    public static void drawModel(IFlexibleBakedModel model, int colour)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushAttrib();
        RenderHelper.disableStandardItemLighting();
        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        }
        else
        {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
        for (BakedQuad bakedquad : model.getGeneralQuads())
        {
            LightUtil.renderQuadColor(worldrenderer, bakedquad, colour);
        }
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
    }
}
