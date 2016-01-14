package de.mineformers.investiture.allomancy.client.renderer.tileentity;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

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
        model = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"), ImmutableMap.of("#frame", Allomancy.resource("blocks/metal_extractor_frame"), "#glass", Allomancy.resource("blocks/metal_extractor_glass"), "#grinder", Allomancy.resource("blocks/metal_extractor_grinder")));
    }

    @Override
    public void renderTileEntityAt(TileMetalExtractorMaster te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (te.isValidMultiBlock()) {
            EnumFacing orientation = te.getOrientation();
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1, 1, 1, 1f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            GlStateManager.translate(x, y - 1, z);
            switch (orientation) {
                case SOUTH:
                    GlStateManager.translate(-2, 0, 5);
                    GlStateManager.translate(2.5, 0, -2.5);
                    GlStateManager.rotate(-90, 0, 1, 0);
                    GlStateManager.translate(-2.5, 0, 2.5);
                    break;
                case NORTH:
                    GlStateManager.translate(-2, 0, 1);
                    GlStateManager.translate(2.5, 0, -2.5);
                    GlStateManager.rotate(90, 0, 1, 0);
                    GlStateManager.translate(-2.5, 0, 2.5);
                    break;
                case EAST:
                    GlStateManager.translate(0, 0, 3);
                    break;
                case WEST:
                    GlStateManager.translate(-4, 0, 3);
                    GlStateManager.translate(2.5, 0, -2.5);
                    GlStateManager.rotate(180, 0, 1, 0);
                    GlStateManager.translate(-2.5, 0, 2.5);
                    break;
            }
            Rendering.drawModel(model);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean forceTileEntityRender()
    {
        return true;
    }
}
