package de.mineformers.investiture.allomancy.client.renderer.misting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.renderer.Shader;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import jline.internal.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * ${JDOC}
 */
@SideOnly(Side.CLIENT)
public class SpeedBubbleRenderer implements IResourceManagerReloadListener
{
    private IBakedModel model;
    private Tessellator batchBuffer = new Tessellator(0x200000);
    private final Shader distortionShader = new Shader(null, new ResourceLocation(Allomancy.DOMAIN, "distortion"));

    public SpeedBubbleRenderer()
    {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
        distortionShader.init();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        model = Modeling.loadModel(Allomancy.resource("block/speed_bubble.obj"),
                                   ImmutableMap.of(),
                                   ImmutableList.of("speed_bubble"));
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event)
    {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null)
            return;
        pushMatrix();
        Vec3d pos = Rendering.interpolatedPosition(player, event.getPartialTicks());
        translate(-pos.x, -pos.y, -pos.z);
//        disableCull();
//        disableDepth();
//        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
//        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        color(1f, 1f, 1f, 1f);

        depthMask(false);

        distortionShader.activate();
        distortionShader.setUniformInt("tex", 0);
        distortionShader.setUniformInt("noiseTex", 2);
        distortionShader.setUniformFloat("windowWidth", Minecraft.getMinecraft().displayWidth);
        distortionShader.setUniformFloat("windowHeight", Minecraft.getMinecraft().displayHeight);
        distortionShader.setUniformFloat("ticks", ((player.world.getTotalWorldTime() + event.getPartialTicks()) % 1000) * 0f);
        distortionShader.setUniformFloat("strength", 0.05f);

        Framebuffer frameBuffer = Minecraft.getMinecraft().getFramebuffer();
        setActiveTexture(OpenGlHelper.lightmapTexUnit + 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Allomancy.DOMAIN, "textures/misc/distortion_noise.png"));
        setActiveTexture(OpenGlHelper.defaultTexUnit);
        frameBuffer.bindFramebufferTexture();

        GL11.glFlush();
        Tessellator batchBuffer = Tessellator.getInstance();
        batchBuffer.getBuffer().begin(GL_QUADS, DefaultVertexFormats.ITEM);
        Frustum frustum = new Frustum();
        frustum.setPosition(pos.x, pos.y + player.getEyeHeight(), pos.z);
        AllomancyAPIImpl.INSTANCE.speedBubbles(player.world)
                                 .forEach(bubble ->
                                          {
                                              for (BakedQuad quad : model.getQuads(null, null, 0))
                                              {
                                                  LightUtil.renderQuadColor(batchBuffer.getBuffer(),
                                                                            Modeling.scale(DefaultVertexFormats.ITEM, quad,
                                                                                           new Vec3d(2 * bubble.radius + 0.05,
                                                                                                     2 * bubble.radius + 0.05,
                                                                                                     2 * bubble.radius + 0.05)),
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
        frameBuffer.unbindFramebufferTexture();
        distortionShader.deactivate();

        depthMask(true);
        enableCull();
        enableDepth();
        popMatrix();
    }

    public static void saveGlTexture(String name, int textureId, File outputFolder) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        int size = width * height;

        BufferedImage bufferedimage = new BufferedImage(width, height, 2);
        String fileName = name + ".png";

        File output = new File(outputFolder, fileName);
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        int[] data = new int[size];

        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
//        buffer.get(data);
//        bufferedimage.setRGB(0, 0, width, height, data, 0, width);
//        BufferedImage newImage = new BufferedImage(
//            bufferedimage.getWidth(), bufferedimage.getHeight(),
//            BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = newImage.createGraphics();
//        AffineTransform at = new AffineTransform();
//        at.concatenate(AffineTransform.getScaleInstance(1, -1));
//        at.concatenate(AffineTransform.getTranslateInstance(0, -bufferedimage.getHeight()));
//        g.transform(at);
//        g.drawImage(bufferedimage, 0, 0, null);
//        g.dispose();
//
//        try {
//            ImageIO.write(newImage, "png", output);
//        } catch (IOException ioexception) {
//            Log.info("Unable to write: ", ioexception);
//        }
    }
}
