package de.mineformers.investiture.allomancy.client.renderer.misting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.renderer.Shader;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import static java.lang.Math.sin;
import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * ${JDOC}
 */
@SideOnly(Side.CLIENT)
public class SpeedBubbleRenderer implements IResourceManagerReloadListener
{
    private IBakedModel model;
    private Framebuffer buffer;
    private final Shader distortionShader = new Shader(null, new ResourceLocation(Allomancy.DOMAIN, "distortion"));
    private final Minecraft mc;
    private int displayWidth, displayHeight;

    public SpeedBubbleRenderer()
    {
        mc = Minecraft.getMinecraft();
        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(this);
        distortionShader.init();
        displayWidth = mc.displayWidth;
        displayHeight = mc.displayHeight;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        model = Modeling.loadModel(Allomancy.resource("block/speed_bubble.obj"),
                                   ImmutableMap.of(),
                                   ImmutableList.of("speed_bubble"));
    }

    private void onResize(int newWidth, int newHeight)
    {
        if (buffer == null)
        {
            buffer = new Framebuffer(newWidth, newHeight, true);
        }
        buffer.createBindFramebuffer(newWidth, newHeight);
        this.displayWidth = newWidth;
        this.displayHeight = newHeight;
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event)
    {
        EntityPlayer player = mc.player;
        if (player == null)
            return;
        if (displayWidth != mc.displayWidth || displayHeight != mc.displayHeight || buffer == null)
        {
            onResize(mc.displayWidth, mc.displayHeight);
        }

        Framebuffer vanillaBuffer = Minecraft.getMinecraft().getFramebuffer();
        buffer.framebufferClear();
        buffer.bindFramebuffer(false);
        pushMatrix();
        mc.entityRenderer.setupOverlayRendering();
        vanillaBuffer.framebufferRender(displayWidth, displayHeight);
        popMatrix();
        enableDepth();
        enableAlpha();
        Rendering.setupCameraTransforms(event.getPartialTicks(), 2);

        pushMatrix();
        Vec3d pos = Rendering.interpolatedPosition(player, event.getPartialTicks());
        translate(-pos.x, -pos.y, -pos.z);
        disableCull();
        color(1f, 1f, 1f, 1f);
        vanillaBuffer.bindFramebuffer(false);
        distortionShader.activate();
        distortionShader.setUniformInt("tex", 0);
        distortionShader.setUniformInt("noiseTex", 2);
        distortionShader.setUniformFloat("windowWidth", mc.displayWidth);
        distortionShader.setUniformFloat("windowHeight", mc.displayHeight);
        float t = (player.world.getTotalWorldTime() + event.getPartialTicks());
        double pi2 = 2 * Math.PI;
        double basePeriod = pi2 / 20f;
        float anim = (float) ((0.5f * sin(basePeriod * t)) + 1.5f);
        distortionShader.setUniformFloat("speed", 0);
        distortionShader.setUniformFloat("strength",  0.02f);

        setActiveTexture(OpenGlHelper.lightmapTexUnit + 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Allomancy.DOMAIN, "textures/misc/distortion_noise.png"));
        setActiveTexture(OpenGlHelper.defaultTexUnit);
        buffer.bindFramebufferTexture();

        GL11.glFlush();
        Tessellator batchBuffer = Tessellator.getInstance();
        batchBuffer.getBuffer().begin(GL_QUADS, DefaultVertexFormats.ITEM);
        Frustum frustum = new Frustum();
        frustum.setPosition(pos.x, pos.y + player.getEyeHeight(), pos.z);
        AllomancyAPIImpl.INSTANCE.speedBubbles(player.world)
                                 .forEach(bubble ->
                                          {
                                              int seed = 0;//new Random(bubble.position.toLong()).nextInt(256);
                                              for (BakedQuad quad : model.getQuads(null, null, 0))
                                              {
                                                  LightUtil.renderQuadColor(batchBuffer.getBuffer(),
                                                                            Modeling.transform(DefaultVertexFormats.ITEM,
                                                                                               Modeling.scale(DefaultVertexFormats.ITEM, quad,
                                                                                                              new Vec3d(2 * bubble.radius + 0.05,
                                                                                                                        2 * bubble.radius + 0.05,
                                                                                                                        2 * bubble.radius + 0.05)),
                                                                                               (usage, data) -> {
                                                                                                   if (usage == VertexFormatElement.EnumUsage.COLOR)
                                                                                                   {
                                                                                                       data[0] = seed;
                                                                                                   }
                                                                                                   return data;
                                                                                               }),
                                                                            0xFFFFFFFF);
                                                  batchBuffer.getBuffer()
                                                             .putPosition(bubble.position.getX() + 0.5,
                                                                          bubble.position.getY() + 0.5,
                                                                          bubble.position.getZ() + 0.5);
                                              }
                                          });
        batchBuffer.getBuffer().sortVertexData((float) pos.x, (float) pos.y + player.getEyeHeight(), (float) pos.z);
        batchBuffer.draw();
        batchBuffer.getBuffer().getVertexState();
        distortionShader.deactivate();

        enableCull();
        depthMask(true);
        popMatrix();
    }
}
