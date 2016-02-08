package de.mineformers.investiture.allomancy.impl.misting;

import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.Oracle;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.util.Rendering;
import de.mineformers.investiture.serialisation.Serialise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.client.renderer.GlStateManager.*;

/**
 * ${JDOC}
 */
public class OracleImpl extends AbstractMisting implements Oracle
{
    @Inject
    private Entity entity;
    @Serialise
    private int spawnDimension;
    @Serialise
    private BlockPos spawnPoint;

    @Override
    public void startBurning()
    {
        if (entity instanceof EntityPlayer)
            spawnPoint = ((EntityPlayer) entity).getBedLocation(entity.dimension);
        else
            spawnPoint = entity.worldObj.getSpawnPoint();
        spawnDimension = entity.dimension;
    }

    @Override
    public BlockPos spawnPoint()
    {
        return spawnPoint;
    }

    public static class EventHandler
    {
        @SubscribeEvent
        public void onRenderLast(RenderWorldLastEvent event)
        {
            AllomancyAPIImpl.INSTANCE.toAllomancer(Minecraft.getMinecraft().thePlayer)
                                     .filter(a -> a.activePowers().contains(Oracle.class))
                                     .flatMap(a -> a.as(Oracle.class))
                                     .ifPresent(o -> {
                                         if (o instanceof OracleImpl)
                                             render((OracleImpl) o, event.partialTicks);
                                     });
        }

        private void render(OracleImpl oracle, float partialTicks)
        {
            if (oracle.entity.dimension != oracle.spawnDimension || oracle.spawnPoint == null)
                return;
            pushMatrix();
            pushAttrib();
            Vec3 playerPos = Rendering.interpolatedPosition(Minecraft.getMinecraft().thePlayer, partialTicks);
            translate(-playerPos.xCoord, -playerPos.yCoord, -playerPos.zCoord);
            translate(oracle.spawnPoint.getX() + 0.5, oracle.spawnPoint.getY() + 0.5, oracle.spawnPoint.getZ() + 0.5);
            disableDepth();
            depthMask(false);

            double scale = 0.00390625D * (playerPos.distanceTo(new Vec3(oracle.spawnPoint).addVector(0.5, 0.5, 0.5)) + 4.0D) / 3.0D * 4;
            rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            scale(-scale, -scale, -scale);
            FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
            int width = font.getStringWidth("Spawn");
            font.drawString("Spawn", -width / 2, -font.FONT_HEIGHT / 2, 0xFFFFFFFF);

            depthMask(true);
            enableDepth();
            popAttrib();
            popMatrix();
        }
    }
}
