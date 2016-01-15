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

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

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
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + height, 0)
          .tex(uMin, vMax)
          .endVertex();
        wr.pos(x + width, y + height, 0)
          .tex(uMax, vMax)
          .endVertex();
        wr.pos(x + width, y, 0)
          .tex(uMax, vMin)
          .endVertex();
        wr.pos(x, y, 0)
          .tex(uMin, vMin)
          .endVertex();
        tessellator.draw();
    }

    public static void drawRing(int centreX, int centreY, int innerRadius, int width, Colour colour)
    {
        drawRing(centreX, centreY, innerRadius, width, colour, colour);
    }

    public static void drawRing(int centreX, int centreY, int innerRadius, int width, Colour innerColour, Colour outerColour)
    {
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer wr = tess.getWorldRenderer();

        wr.begin(GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(centreX + cos(0) * innerRadius, centreY + sin(0) * innerRadius, 0)
          .color(innerColour.r(), innerColour.g(), innerColour.b(), innerColour.a())
          .endVertex();
        for (int i = -1; i <= 99; i++)
        {
            double angle0 = PI * 2 * i / 100;
            double angle1 = PI * 2 * (i + 1) / 100;
            wr.pos(centreX + cos(angle1) * innerRadius, centreY + sin(angle1) * innerRadius, 0)
              .color(innerColour.r(), innerColour.g(), innerColour.b(), innerColour.a())
              .endVertex();
            wr.pos(centreX + cos(angle0) * (innerRadius + width), centreY + sin(angle0) * (innerRadius + width), 0)
              .color(outerColour.r(), outerColour.g(), outerColour.b(), outerColour.a())
              .endVertex();
        }
        tess.draw();
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
