package de.mineformers.investiture.allomancy.client.particle;

import de.mineformers.investiture.client.util.Rendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

/**
 * ${JDOC}
 */
public class FootStep extends EntityFX
{
    private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");

    public FootStep(World world, Vec3d position, float r, float g, float b)
    {
        super(world, position.xCoord, position.yCoord, position.zCoord, 0, 0, 0);
        xSpeed = ySpeed = zSpeed = 0;
        particleRed = r;
        particleGreen = g;
        particleBlue = b;
        particleMaxAge = 200;
    }

    @Override
    public void renderParticle(VertexBuffer renderer, Entity entity, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
    {
        float age = (this.particleAge + partialTicks) / (this.particleMaxAge + 1);
        age = age * age;
        float alpha = 2.0F - age * 2.0F;
        if (alpha > 1.0F)
        {
            alpha = 1.0F;
        }
        alpha = alpha * 0.4F;

        disableLighting();
        Vec3d pos = Rendering.interpolatedPosition(Minecraft.getMinecraft().getRenderViewEntity(), partialTicks);
        double x = this.posX - pos.xCoord;
        double y = this.posY - pos.yCoord;
        double z = this.posZ - pos.zCoord;
        Minecraft.getMinecraft().getTextureManager().bindTexture(FOOTPRINT_TEXTURE);
        disableAlpha();
        enableBlend();
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        renderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        renderer.pos(x - 0.125F, y, z + 0.125F)
                .tex(0, 1)
                .color(particleRed, particleGreen, particleBlue, alpha * particleAlpha)
                .endVertex();
        renderer.pos(x + 0.125F, y, z + 0.125F)
                .tex(1, 1)
                .color(particleRed, particleGreen, particleBlue, alpha * particleAlpha)
                .endVertex();
        renderer.pos(x + 0.125F, y, z - 0.125F)
                .tex(1, 0)
                .color(particleRed, particleGreen, particleBlue, alpha * particleAlpha)
                .endVertex();
        renderer.pos(x - 0.125F, y, z - 0.125F)
                .tex(0, 0)
                .color(particleRed, particleGreen, particleBlue, alpha * particleAlpha)
                .endVertex();
        Tessellator.getInstance().draw();
        disableBlend();
        enableLighting();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (worldObj.isAirBlock(new BlockPos(posX, posY, posZ).down()))
            setExpired();
    }

    @Override
    public int getFXLayer()
    {
        return 3;
    }
}
