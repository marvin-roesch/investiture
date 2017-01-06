package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.physical.Coinshot;
import de.mineformers.investiture.allomancy.api.misting.physical.Lurcher;
import de.mineformers.investiture.allomancy.api.misting.physical.MetalManipulator;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import de.mineformers.investiture.client.util.Rendering;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
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
    private Set<Entity> affectedEntities = new HashSet<>();
    private int updateCounter = 20;

    @Override
    public Collection<BlockPos> affectedBlocks()
    {
        return Collections.unmodifiableSet(affectedBlocks);
    }

    @Override
    public Collection<Entity> affectedEntities()
    {
        return Collections.unmodifiableSet(affectedEntities);
    }

    @Override
    public void update()
    {
        updateCounter++;
        if (updateCounter > 20)
            updateCounter = 0;
        else
            return;
        affectedBlocks.clear();
        for (BlockPos pos : BlockPos.getAllInBox(entity.getPosition().subtract(new BlockPos(20, 20, 20)), entity.getPosition().add(20, 20, 20)))
        {
            if (AllomancyAPIImpl.INSTANCE.isMetallic(entity.world, pos))
                affectedBlocks.add(pos);
        }
        affectedEntities.clear();
        affectedEntities.addAll(
            entity.world.getEntitiesInAABBexcluding(entity,
                                                       new AxisAlignedBB(entity.posX - 20, entity.posY - 20, entity.posZ - 20,
                                                                         entity.posX + 20, entity.posY + 20, entity.posZ + 20),
                                                       AllomancyAPIImpl.INSTANCE::isMetallic));
    }

    @Override
    public boolean isValid(Entity entity)
    {
        return affectedEntities().contains(entity);
    }

    @Override
    public boolean isValid(BlockPos pos)
    {
        return affectedBlocks().contains(pos);
    }

    @Override
    public void apply(RayTraceResult pos)
    {
        if (pos.typeOfHit == RayTraceResult.Type.BLOCK)
            applyBlockEffect(pos);
        else if (pos.typeOfHit == RayTraceResult.Type.ENTITY)
            applyEntityEffect(pos);
    }

    private void applyBlockEffect(RayTraceResult pos)
    {
        if (!affectedBlocks.contains(pos.getBlockPos()))
            return;
        applyEffect(entity, pos.hitVec, 1);
    }

    private void applyEntityEffect(RayTraceResult pos)
    {
        if (!affectedEntities.contains(pos.entityHit) || pos.entityHit == entity)
            return;
        applyEffect(pos.entityHit, pos.hitVec, -1);
    }

    private void applyEffect(Entity target, Vec3d end, double factor)
    {
        Vec3d start = entity.getPositionVector()
                            .addVector(0, entity.height / 2, 0);
        double distance = 1 / (start.distanceTo(end) + 0.1) * 0.1;
        Vec3d direction = start.subtract(end);
        Vec3d velocity = new Vec3d(direction.xCoord * distance * distanceFactor().xCoord * factor,
                                   direction.yCoord * distance * distanceFactor().yCoord * factor,
                                   direction.zCoord * distance * distanceFactor().zCoord * factor);

        target.addVelocity(velocity.xCoord, velocity.yCoord, velocity.zCoord);
        if (target == entity)
            entity.fallDistance = (float) Math.max(0, entity.fallDistance - velocity.yCoord);
    }

    @Override
    public boolean repeatEvent()
    {
        return true;
    }

    public abstract Vec3d distanceFactor();

    public static class EventHandler
    {
        private Set<BlockPos> allPositions = new HashSet<>();
        private Set<Entity> allEntities = new HashSet<>();
        private List<PositionWrapper> positions = new ArrayList<>();
        private TObjectIntMap<PositionWrapper> fadeInTimer = new TObjectIntHashMap<>();
        private TObjectIntMap<PositionWrapper> fadeOutTimer = new TObjectIntHashMap<>();
        private boolean active;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event)
        {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if (event.phase != TickEvent.Phase.END || player == null)
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(player).ifPresent(a -> {
                active = a.activePowers().contains(Coinshot.class) || a.activePowers().contains(Lurcher.class);
                allEntities.clear();
                allPositions.clear();
                if (active)
                {
                    allEntities.addAll(a.as(Coinshot.class).map(MetalManipulator::affectedEntities).orElse(new HashSet<>()));
                    allEntities.addAll(a.as(Lurcher.class).map(MetalManipulator::affectedEntities).orElse(new HashSet<>()));
                    allPositions.addAll(a.as(Coinshot.class).map(MetalManipulator::affectedBlocks).orElse(new HashSet<>()));
                    allPositions.addAll(a.as(Lurcher.class).map(MetalManipulator::affectedBlocks).orElse(new HashSet<>()));
                }

                Set<PositionWrapper> toRemove = new HashSet<>();
                for (PositionWrapper p : fadeOutTimer.keySet())
                {
                    fadeOutTimer.increment(p);
                    if (fadeOutTimer.get(p) > 20)
                        toRemove.add(p);
                }
                for (PositionWrapper p : toRemove)
                    fadeOutTimer.remove(p);

                toRemove.clear();
                for (PositionWrapper p : fadeInTimer.keySet())
                {
                    fadeInTimer.increment(p);
                    if (fadeInTimer.get(p) > 20)
                    {
                        positions.add(p);
                        toRemove.add(p);
                    }
                    else if ((p.base instanceof BlockPos && !allPositions.contains(p.base)) ||
                        (p.base instanceof Entity && !allEntities.contains(p.base)))
                    {
                        fadeOutTimer.put(p, 21 - fadeInTimer.get(p));
                        toRemove.add(p);
                    }
                }
                for (PositionWrapper p : toRemove)
                    fadeInTimer.remove(p);

                for (Iterator<PositionWrapper> it = positions.iterator(); it.hasNext(); )
                {
                    PositionWrapper p = it.next();
                    if ((p.base instanceof BlockPos && !allPositions.contains(p.base)) ||
                        (p.base instanceof Entity && !allEntities.contains(p.base)))
                    {
                        fadeOutTimer.put(p, 0);
                        it.remove();
                    }
                }

                for (BlockPos p : allPositions)
                {
                    PositionWrapper wrapper = PositionWrapper.from(player.world, p);
                    if (!positions.contains(wrapper) && !fadeInTimer.containsKey(wrapper))
                    {
                        fadeInTimer.put(wrapper, 0);
                    }
                }
                for (Entity e : allEntities)
                {
                    PositionWrapper wrapper = PositionWrapper.from(e);
                    if (!positions.contains(wrapper) && !fadeInTimer.containsKey(wrapper))
                    {
                        fadeInTimer.put(wrapper, 0);
                    }
                }
            });
        }

        @SubscribeEvent
        public void onRenderLast(RenderWorldLastEvent event)
        {
            if (active || !fadeInTimer.isEmpty() || !fadeOutTimer.isEmpty())
                renderLines(event.getPartialTicks());
        }

        private void renderLines(float partialTicks)
        {
            pushMatrix();
            pushAttrib();
            disableLighting();
            disableDepth();
            depthMask(false);
            disableTexture2D();
            enableBlend();
            tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
            blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            GlStateManager.glLineWidth(4);

            Vec3d playerPos = Rendering.interpolatedPosition(Minecraft.getMinecraft().player, partialTicks);
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer renderer = tessellator.getBuffer();
            renderer.begin(GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            for (PositionWrapper pos : fadeInTimer.keySet())
            {
                drawPos(playerPos, pos, partialTicks, (fadeInTimer.get(pos) + partialTicks) / 21f);
            }
            for (PositionWrapper pos : fadeOutTimer.keySet())
            {
                drawPos(playerPos, pos, partialTicks, (21 - fadeOutTimer.get(pos) - partialTicks) / 21f);
            }
            for (PositionWrapper pos : positions)
            {
                drawPos(playerPos, pos, partialTicks, 1);
            }
            tessellator.draw();

            disableBlend();
            depthMask(true);
            enableLighting();
            enableTexture2D();
            enableDepth();
            popAttrib();
            popMatrix();
        }

        private void drawPos(Vec3d start, PositionWrapper pos, float partialTicks, float progress)
        {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer renderer = tessellator.getBuffer();
            Vec3d element = pos.center();
            Vec3d direction = element.subtract(start);
            Vec3d end = new Vec3d(direction.xCoord * progress, direction.yCoord * progress, direction.zCoord * progress);

            EntityPlayer player = Minecraft.getMinecraft().player;
            Vec3d off = new Vec3d(0, -0.06, 0.09D);
            off = off.rotatePitch(-(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks) *
                                      (float) Math.PI / 180.0F);
            off = off.rotateYaw(-(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks) *
                                    (float) Math.PI / 180.0F);

            double pX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks + off.xCoord;
            double pY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + off.yCoord;
            double pZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks + off.zCoord;
            double pEye = player.getEyeHeight();

            if (Minecraft.getMinecraft().gameSettings.thirdPersonView > 0)
            {
                pX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
                pY = player.prevPosY + pEye + (player.posY - player.prevPosY) * partialTicks;
                pZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
                pEye = player.isSneaking() ? -0.1875 : 0;
                pEye -= 0.5;
            }

            double x = pX - element.xCoord;
            double y = pY - element.yCoord + pEye;
            double z = pZ - element.zCoord;

            renderer.pos(element.xCoord - start.xCoord + x, element.yCoord - start.yCoord + y, element.zCoord - start.zCoord + z)
                    .color(0.28627452f, 0.7254902f, 0.87058824f, progress * 0.5f)
                    .endVertex();
            renderer.pos(end.xCoord, end.yCoord, end.zCoord)
                    .color(0.28627452f, 0.7254902f, 0.87058824f, progress * 0.5f)
                    .endVertex();
        }

        private abstract static class PositionWrapper
        {
            @Nonnull
            final Object base;

            static PositionWrapper from(Entity entity)
            {
                return new PositionWrapper(entity)
                {
                    @Override
                    public Vec3d center()
                    {
                        return entity.getPositionVector().addVector(0, -entity.getYOffset() + entity.height / 2, 0);
                    }
                };
            }

            static PositionWrapper from(IBlockAccess access, BlockPos pos)
            {
                return new PositionWrapper(pos)
                {
                    @Override
                    public Vec3d center()
                    {
                        IBlockState block = access.getBlockState(pos);
                        AxisAlignedBB bounds = block.getBoundingBox(access, pos);
                        Vec3d offset = new Vec3d(bounds.minX + (bounds.maxX - bounds.minX) / 2,
                                                 bounds.minY + (bounds.maxY - bounds.minY) / 2,
                                                 bounds.minZ + (bounds.maxZ - bounds.minZ) / 2);
                        return new Vec3d(pos).add(offset);
                    }
                };
            }

            private PositionWrapper(@Nonnull Object base)
            {
                this.base = base;
            }

            public abstract Vec3d center();

            @Override
            public int hashCode()
            {
                return base.hashCode();
            }

            @Override
            public boolean equals(Object obj)
            {
                return obj instanceof PositionWrapper && base.equals(((PositionWrapper) obj).base);
            }
        }
    }
}
