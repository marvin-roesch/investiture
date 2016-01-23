package de.mineformers.investiture.allomancy.client.renderer.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.tileentity.TileMetalExtractorMaster;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import de.mineformers.investiture.client.util.Textures;
import de.mineformers.investiture.client.util.Textures.TextureType;
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
    private IFlexibleBakedModel modelGrinder;
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
        Textures.stitch(Allomancy.DOMAIN, TextureType.BLOCK, "metal_extractor");
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        modelFrame = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                           ImmutableMap.of("#extractor", Allomancy.resource("blocks/metal_extractor")),
                                           ImmutableList.of("Frame"));
        modelGrinder = Modeling.loadOBJModel(Allomancy.resource("block/metal_extractor.obj"),
                                             ImmutableMap.of("#extractor", Allomancy.resource("blocks/metal_extractor")),
                                             ImmutableList.of("Grinder"));
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
            float angle = 360 * te.rotation - (te.rotation - te.prevRotation < 4 ? 0 : partialTicks);

            GlStateManager.pushMatrix();
            GlStateManager.translate(2.5, 0, -2.5);
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.translate(-2.5, 0, 2.5);
            Rendering.drawModel(modelGrinder);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(2.5, 2.5, -2.5);
            GlStateManager.rotate(angle, 0, 0, 1);
            GlStateManager.translate(-2.5, -2.5, 2.5);
            Rendering.drawModel(modelWaterWheel);
            GlStateManager.popMatrix();

            if (te.getProcessing().isPresent())
            {
                int timer = te.getProcessing().get().timer;
                float progress = (float) (timer / te.getProcessingTime());
                float incomingEnd = (float) (0.375 * te.getProcessingTime());
                float outgoingStart = (float) (0.625 * te.getProcessingTime());
                GlStateManager.translate(1.3 + 3 * progress, 0.5, -2.5);
                if (timer < incomingEnd)
                {
                    GlStateManager.rotate(-20.5f + 20.5f * (timer / incomingEnd), 0, 0, 1);
                }
                else if (timer > outgoingStart)
                {
                    GlStateManager.translate(-0.5, 0, 0);
                    GlStateManager.rotate((float) (20.5f * ((timer - outgoingStart) / (te.getProcessingTime() - outgoingStart))), 0, 0, 1);
                    GlStateManager.translate(0.5, 0, 0);
                }
                GlStateManager.scale(4, 4, 4);
                EntityItem item = new EntityItem(te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(),
                                                 te.getProcessing().get().input);
                item.hoverStart = 0;
                RENDER_ITEM.doRender(item, 0, 0, 0, 0, 0);
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean forceTileEntityRender()
    {
        return true;
    }
}
