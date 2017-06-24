package de.mineformers.investiture.allomancy.client.renderer.misting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.util.Modeling;
import de.mineformers.investiture.client.util.Rendering;
import de.mineformers.investiture.client.util.Textures;
import jline.internal.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.ProgressManager;
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
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * ${JDOC}
 */
@SideOnly(Side.CLIENT)
public class SpeedBubbleRenderer implements IResourceManagerReloadListener
{
    private Framebuffer entityOutlineFramebuffer;
    private ShaderGroup entityOutlineShader;
    private IBakedModel model;
    private Tessellator batchBuffer = new Tessellator(0x200000);

    public SpeedBubbleRenderer()
    {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
        Textures.stitch(Allomancy.DOMAIN, Textures.TextureType.MISC, "speed_bubble");
    }

    public void postInit() {
        Minecraft mc = Minecraft.getMinecraft();
        try
        {
            this.entityOutlineShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(),  new ResourceLocation("investiture","shaders/post/entity_outline.json"));
            this.entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null)
            return;
        if (entityOutlineShader == null)
            postInit();
        List<Entity> entities = player.world.getEntities(EntityChicken.class, e -> e.getDistanceSqToEntity(player) <= 400);
        if (entities.isEmpty())
            return;
        this.entityOutlineFramebuffer.framebufferClear();
        GlStateManager.depthFunc(519);
        GlStateManager.disableFog();
        this.entityOutlineFramebuffer.bindFramebuffer(false);
        RenderHelper.disableStandardItemLighting();
        mc.getRenderManager().setRenderOutlines(true);


        for (Entity entity : entities)
        {
            mc.getRenderManager().renderEntityStatic(entity, event.getPartialTicks(), false);
        }

        mc.getRenderManager().setRenderOutlines(false);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.depthMask(false);
        this.entityOutlineShader.render(event.getPartialTicks());
        saveGlTexture("test", entityOutlineFramebuffer.framebufferTexture, new File("./"));
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
//        GlStateManager.enableFog();
        GlStateManager.enableBlend();
        GlStateManager.enableColorMaterial();
        GlStateManager.depthFunc(515);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        mc.getFramebuffer().bindFramebuffer(false);
        pushMatrix();
        pushAttrib();
        Vec3d pos = Rendering.interpolatedPosition(player, event.getPartialTicks());
        translate(-pos.x, -pos.y, -pos.z);
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

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
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
                                                                                           new Vec3d(bubble.radius,
                                                                                                     bubble.radius,
                                                                                                     bubble.radius)),
                                                                            0xFFFFFFFF);
                                                  batchBuffer.getBuffer()
                                                             .putPosition(bubble.position.getX() + 0.5,
                                                                          bubble.position.getY(),
                                                                          bubble.position.getZ() + 0.5);
                                              }
                                          });
        batchBuffer.getBuffer().sortVertexData((float) pos.x, (float) pos.y, (float) pos.z);
        batchBuffer.draw();
        batchBuffer.getBuffer().getVertexState();

        enableCull();
        disableBlend();
        enableLighting();
        popAttrib();
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
            buffer.get(data);
            bufferedimage.setRGB(0, 0, width, height, data, 0, width);
            BufferedImage newImage = new BufferedImage(
                bufferedimage.getWidth(), bufferedimage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = newImage.createGraphics();
            AffineTransform at = new AffineTransform();
            at.concatenate(AffineTransform.getScaleInstance(1, -1));
            at.concatenate(AffineTransform.getTranslateInstance(0, -bufferedimage.getHeight()));
            g.transform(at);
            g.drawImage(bufferedimage, 0, 0, null);
            g.dispose();

            try {
                ImageIO.write(newImage, "png", output);
            } catch (IOException ioexception) {
                Log.info("Unable to write: ", ioexception);
            }
    }
}
