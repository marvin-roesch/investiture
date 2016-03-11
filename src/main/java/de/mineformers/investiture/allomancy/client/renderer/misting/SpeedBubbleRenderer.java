package de.mineformers.investiture.allomancy.client.renderer.misting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import de.mineformers.investiture.client.util.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * ${JDOC}
 */
public class SpeedBubbleRenderer implements IResourceManagerReloadListener
{
    private IFlexibleBakedModel model;
    private Tessellator batchBuffer = new Tessellator(0x200000);

    public SpeedBubbleRenderer()
    {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
        Textures.stitch(Allomancy.DOMAIN, Textures.TextureType.MISC, "speed_bubble");
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        model = Modeling.loadModel(Allomancy.resource("block/speed_bubble.obj"),
                                   ImmutableMap.of("#sphere", Allomancy.resource("misc/speed_bubble")),
                                   ImmutableList.of("Sphere"));
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null)
            return;
        pushMatrix();
        pushAttrib();
        Vec3 pos = Rendering.interpolatedPosition(player, event.partialTicks);
        translate(-pos.xCoord, -pos.yCoord, -pos.zCoord);
        disableLighting();
        enableBlend();
        disableCull();
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        color(1f, 0, 0, 1f);
        if (net.minecraft.client.Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(GL_SMOOTH);
        else
            GlStateManager.shadeModel(GL_FLAT);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        batchBuffer.getWorldRenderer().begin(GL_QUADS, model.getFormat());
        Frustum frustum = new Frustum();
        frustum.setPosition(pos.xCoord, pos.yCoord + player.getEyeHeight(), pos.zCoord);
        AllomancyAPIImpl.INSTANCE.speedBubbles(player.worldObj).forEach(bubble -> {
            if(!frustum.isBoundingBoxInFrustum(bubble.bounds))
                return;
            for (BakedQuad quad : model.getGeneralQuads())
            {
                LightUtil.renderQuadColor(batchBuffer.getWorldRenderer(),
                                          Modeling.scale(model.getFormat(), quad, new Vec3(bubble.radius, bubble.radius, bubble.radius)),
                                          0xFFFFFFFF);
                batchBuffer.getWorldRenderer()
                           .putPosition(bubble.position.getX() + 0.5, bubble.position.getY(), bubble.position.getZ() + 0.5);
            }
        });
        batchBuffer.getWorldRenderer().sortVertexData((float) pos.xCoord, (float) pos.yCoord, (float) pos.zCoord);
        batchBuffer.draw();
        batchBuffer.getWorldRenderer().getVertexState();

        enableCull();
        disableBlend();
        enableLighting();
        popAttrib();
        popMatrix();
    }
}
