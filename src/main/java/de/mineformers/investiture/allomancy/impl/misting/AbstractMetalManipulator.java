package de.mineformers.investiture.allomancy.impl.misting;

import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.misting.Coinshot;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.Lurcher;
import de.mineformers.investiture.allomancy.api.misting.MetalManipulator;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.client.util.Rendering;
import de.mineformers.investiture.util.RayTracer;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.*;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * ${JDOC}
 */
public abstract class AbstractMetalManipulator extends AbstractMisting implements MetalManipulator, ITickable
{
    @Inject
    protected Entity entity;
    private Set<BlockPos> affectedBlocks = new HashSet<>();

    @Override
    public Collection<BlockPos> affectedBlocks()
    {
        return Collections.unmodifiableSet(affectedBlocks);
    }

    @Override
    public void update()
    {
        affectedBlocks.clear();
        for (BlockPos pos : BlockPos.getAllInBox(entity.getPosition().subtract(new BlockPos(20, 20, 20)), entity.getPosition().add(20, 20, 20)))
        {
            IBlockState state = entity.worldObj.getBlockState(pos);
            if (state.getBlock() == Blocks.iron_block)
                affectedBlocks.add(pos);
        }
    }

    public static class EventHandler
    {
        private static final ResourceLocation WISP_TEXTURE = Allomancy.resource("textures/misc/wisp.png");
        private List<BlockPos> positions = new ArrayList<>();
        private Set<BlockPos> allPositions = new HashSet<>();
        private TObjectIntMap<BlockPos> fadeInTimer = new TObjectIntHashMap<>();
        private TObjectIntMap<BlockPos> fadeOutTimer = new TObjectIntHashMap<>();
        private boolean active;
        private MovingObjectPosition hit;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event)
        {
            if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().thePlayer == null)
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(Minecraft.getMinecraft().thePlayer).ifPresent(a -> {
                active = a.activePowers().contains(Coinshot.class) || a.activePowers().contains(Lurcher.class);
                if (!active)
                    return;
                allPositions.clear();
                allPositions.addAll(a.as(Coinshot.class).map(MetalManipulator::affectedBlocks).orElse(new HashSet<>()));
                allPositions.addAll(a.as(Lurcher.class).map(MetalManipulator::affectedBlocks).orElse(new HashSet<>()));

                for (TObjectIntIterator<BlockPos> it = fadeOutTimer.iterator(); it.hasNext(); )
                {
                    it.advance();
                    it.setValue(it.value() + 1);
                    if (it.value() > 20)
                        it.remove();
                }
                for (TObjectIntIterator<BlockPos> it = fadeInTimer.iterator(); it.hasNext(); )
                {
                    it.advance();
                    it.setValue(it.value() + 1);
                    if (it.value() > 20)
                    {
                        positions.add(it.key());
                        it.remove();
                    }
                }
                for (Iterator<BlockPos> it = positions.iterator(); it.hasNext(); )
                {
                    BlockPos p = it.next();
                    if (!allPositions.contains(p))
                    {
                        fadeOutTimer.put(p, 0);
                        it.remove();
                    }
                }
                for (BlockPos p : allPositions)
                {
                    if (!positions.contains(p) && !fadeInTimer.containsKey(p))
                    {
                        fadeInTimer.put(p, 0);
                    }
                }
                positions.sort((p1, p2) -> p1.getY() - p2.getY());
            });
            if(active && Minecraft.getMinecraft().thePlayer.getHeldItem() == null && (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)))
            {
                if (hit != null)
                {
                    Vec3 playerPos = Minecraft.getMinecraft().thePlayer.getPositionVector()
                                                                       .addVector(0, Minecraft.getMinecraft().thePlayer.getEyeHeight(), 0);
                    Vec3 hitVec = hit.hitVec;
                    double distance = 1 / playerPos.distanceTo(hitVec) * 0.1;
                    distance *= Mouse.isButtonDown(0) ? 1 : -1;
                    Vec3 direction = playerPos.subtract(hitVec);
                    Minecraft.getMinecraft().thePlayer.addVelocity(direction.xCoord * distance,
                                                                   direction.yCoord * distance,
                                                                   direction.zCoord * distance);
                }
            } else
                hit = null;
        }

        @SubscribeEvent
        public void onRenderLast(RenderWorldLastEvent event)
        {
            if (active)
                renderLines(event.partialTicks);
        }

        @SubscribeEvent
        public void onMouseClick(MouseEvent event)
        {
            if (!active)
                return;
            if(Minecraft.getMinecraft().thePlayer.getHeldItem() == null && event.buttonstate && (event.button == 0 || event.button == 1))
            {
                hit = RayTracer.rayTraceBlocks(Minecraft.getMinecraft().thePlayer, 20,
                                               s -> allPositions.contains(s.getPos()), false, false, false);
                if (hit != null)
                {
                    event.setCanceled(true);
                }
            }
        }

        private void renderLines(float partialTicks)
        {
            pushMatrix();
            pushAttrib();
            Vec3 playerPos = Rendering.interpolatedPosition(Minecraft.getMinecraft().thePlayer, partialTicks);
            translate(-playerPos.xCoord, -playerPos.yCoord, -playerPos.zCoord);
            playerPos = playerPos.addVector(0, -Minecraft.getMinecraft().getRenderViewEntity().getYOffset() +
                Minecraft.getMinecraft().getRenderViewEntity().height / 2.0F, 0);
            Minecraft.getMinecraft().getTextureManager().bindTexture(WISP_TEXTURE);
            disableLighting();
            depthMask(false);
            enableBlend();
            tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            for (BlockPos pos : fadeInTimer.keySet())
            {
                color(1f, 1f, 1f, (fadeInTimer.get(pos) + partialTicks) / 40f);
                drawPos(playerPos, pos);
            }
            for (BlockPos pos : fadeOutTimer.keySet())
            {
                color(1f, 1f, 1f, (20 - fadeOutTimer.get(pos) - partialTicks) / 40f);
                drawPos(playerPos, pos);
            }
            for (BlockPos pos : positions)
            {
                color(1f, 1f, 1f, 0.5f);
                drawPos(playerPos, pos);
            }
            disableBlend();
            depthMask(true);
            enableLighting();
            popAttrib();
            popMatrix();
        }

        private void drawPos(Vec3 playerPos, BlockPos pos)
        {
            List<Vec3> points = calculatePoints(playerPos, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
            for (Vec3 p : points)
            {
                pushMatrix();
                translate(p.xCoord, p.yCoord, p.zCoord);
                Rendering.drawFacingQuad(0.03f);
                popMatrix();
            }
        }

        private List<Vec3> calculatePoints(Vec3 start, Vec3 end)
        {
            ImmutableList.Builder<Vec3> builder = ImmutableList.builder();
            float length = (float) start.distanceTo(end) * 20;
            int steps = (int) length;
            for (int i = steps - 1; i > 0; i--)
            {
                float dist = (float) (i * (length / steps) + Math.toRadians((System.currentTimeMillis() % 72000) * 0.5f));
                double dx = (end.xCoord - start.xCoord) / steps * i + MathHelper.sin(dist / 10.0F) * 0.01f;
                double dy = (end.yCoord - start.yCoord) / steps * i + MathHelper.cos(dist / 8.0F) * 0.01f;
                double dz = (end.zCoord - start.zCoord) / steps * i + MathHelper.sin(dist / 6.0F) * 0.01f;
                Vec3 vp = start.addVector(dx, dy, dz);
                builder.add(vp);
            }
            builder.add(end);
            return builder.build();
        }
    }
}
