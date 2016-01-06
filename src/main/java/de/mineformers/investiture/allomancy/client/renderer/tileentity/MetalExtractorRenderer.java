package de.mineformers.investiture.allomancy.client.renderer.tileentity;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Renders the metal extractor OBJ model.
 */
public class MetalExtractorRenderer extends TileEntitySpecialRenderer<TileMetalExtractorMaster> implements IResourceManagerReloadListener
{
    private IFlexibleBakedModel model;

    public MetalExtractorRenderer()
    {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        try
        {
            OBJModel obj = (OBJModel) ModelLoaderRegistry.getModel(new ResourceLocation(Allomancy.DOMAIN, "block/metal_extractor.obj"));
            IModel model = ((OBJModel) obj.retexture(
                ImmutableMap.of("#frame", "allomancy:blocks/metal_extractor_frame",
                                "#glass", "allomancy:blocks/metal_extractor_glass",
                                "#grinder", "allomancy:blocks/metal_extractor_grinder")))
                .process(ImmutableMap.of("flip-v", "true"));
            this.model = model.bake(new OBJModel.OBJState(ImmutableList.of(OBJModel.Group.ALL), true), Attributes.DEFAULT_BAKED_FORMAT,
                                    res -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString()));
        }
        catch (IOException e)
        {
            Throwables.propagate(e);
        }
    }

    @Override
    public void renderTileEntityAt(TileMetalExtractorMaster te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (te.isValidMultiBlock())
        {
            RenderHelper.disableStandardItemLighting();
            EnumFacing orientation = te.getOrientation();
            GlStateManager.pushMatrix();
            GlStateManager.color(1f, 1f, 1f, 1f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            GlStateManager.translate(x, y - 1, z);
            if (Minecraft.isAmbientOcclusionEnabled())
            {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            }
            else
            {
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
            switch (orientation)
            {
                case SOUTH:
                    GlStateManager.translate(2.5 - 2, 0, 2.5 + 5);
                    GlStateManager.rotate(90, 0, 1, 0);
                    GlStateManager.translate(2.5, 0, 2.5);
                    break;
                case NORTH:
                    GlStateManager.translate(2.5 - 2, 0, 2.5 + 1);
                    GlStateManager.rotate(90, 0, 1, 0);
                    GlStateManager.translate(2.5, 0, 2.5);
                    break;
                case EAST:
                    GlStateManager.translate(0, 0, 3);
                    break;
                case WEST:
                    GlStateManager.translate(-4, 0, 3);
                    break;
            }
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(GL11.GL_QUADS, model.getFormat());
            for (BakedQuad bakedquad : model.getGeneralQuads())
            {
                LightUtil.renderQuadColor(worldrenderer, bakedquad, 0xffffffff);
            }
            tessellator.draw();
            GlStateManager.popMatrix();
            RenderHelper.enableStandardItemLighting();
        }
    }

    @Override
    public boolean forceTileEntityRender()
    {
        return true;
    }
}
