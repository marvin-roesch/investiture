package de.mineformers.investiture.allomancy.client.renderer.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IFlexibleBakedModel;

/**
 * Renders the metal extractor OBJ model.
 */
public class MetalExtractorRenderer extends TileEntitySpecialRenderer<TileMetalExtractorMaster> implements IResourceManagerReloadListener
{
    private IFlexibleBakedModel modelFrame;
    private IFlexibleBakedModel modelGrinderTop;
    private IFlexibleBakedModel modelGrinderBottom;
    private IFlexibleBakedModel modelWaterWheel;
    private static final RenderEntityItem RENDER_ITEM = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(),
                                                                             Minecraft.getMinecraft().getRenderItem())
    {
        @Override
        public boolean shouldBob()
        {
            return false;
        }

        @Override
        public boolean shouldSpreadItems()
        {
            return false;
        }
    };

    public MetalExtractorRenderer()
    {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        modelFrame = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                           ImmutableMap.of("#frame", Allomancy.resource("blocks/metal_extractor_frame"),
                                                           "#glass", Allomancy.resource("blocks/metal_extractor_glass")),
                                           ImmutableList.of("Frame", "Input", "Output"));
        modelGrinderTop = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                                ImmutableMap.of("#grinder", Allomancy.resource("blocks/metal_extractor_grinder")),
                                                ImmutableList.of("GrinderTop"));
        modelGrinderBottom = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                                   ImmutableMap.of("#grinder", Allomancy.resource("blocks/metal_extractor_grinder")),
                                                   ImmutableList.of("GrinderBottom"));
        modelWaterWheel = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                                ImmutableMap.of("#wood", new ResourceLocation("blocks/planks_oak")),
                                                ImmutableList.of("WaterWheel", "WaterWheelConnection"));
    }

    @Override
    public void renderTileEntityAt(TileMetalExtractorMaster te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        if (te.isValidMultiBlock())
        {
            EnumFacing orientation = te.getOrientation();
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1, 1, 1, 1f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            GlStateManager.translate(x, y - 1, z);
            switch (orientation)
            {
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
            Rendering.drawModel(modelFrame);
            float angle = te.getWorld().getTotalWorldTime();
            angle = lerp(angle, angle + 1, partialTicks);

            GlStateManager.pushMatrix();
            GlStateManager.translate(2.5, 0, -2.5);
            if (te.getProcessing().isPresent())
                GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.translate(-2.5, 0, 2.5);
            Rendering.drawModel(modelGrinderTop);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(2.5, 0, -2.5);
            if (te.getProcessing().isPresent())
                GlStateManager.rotate(-angle, 0, 1, 0);
            GlStateManager.translate(-2.5, 0, 2.5);
            Rendering.drawModel(modelGrinderBottom);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(2.5, 2, -2.5);
            GlStateManager.rotate(angle, 0, 0, 1);
            GlStateManager.translate(-2.5, -2, 2.5);
            Rendering.drawModel(modelWaterWheel);
            GlStateManager.popMatrix();

            if (te.getProcessing().isPresent())
            {
                int timer = te.getProcessing().get().timer;
                GlStateManager.translate(1.5 + 2 * (timer / 40d), 1.25, -2.5);
                GlStateManager.scale(2.8, 2.8, 2.8);
                EntityItem item = new EntityItem(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(),
                                                 te.getProcessing().get().input);
                item.hoverStart = 0;
                RENDER_ITEM.doRender(item, 0, 0, 0, 0, 0);
            }
            GlStateManager.popMatrix();
        }
    }

    float lerp(float v0, float v1, float t)
    {
        return (1 - t) * v0 + t * v1;
    }

    @Override
    public boolean forceTileEntityRender()
    {
        return true;
    }
}
