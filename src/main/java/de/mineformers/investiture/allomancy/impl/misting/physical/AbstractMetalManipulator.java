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
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
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
            if (AllomancyAPIImpl.INSTANCE.isMetallic(entity.worldObj, pos))
                affectedBlocks.add(pos);
        }
        affectedEntities.clear();
        affectedEntities.addAll(
            entity.worldObj.getEntitiesInAABBexcluding(entity,
                                                       AxisAlignedBB.fromBounds(entity.posX - 20, entity.posY - 20, entity.posZ - 20,
                                                                                entity.posX + 20, entity.posY + 20, entity.posZ + 20),
                                                       AllomancyAPIImpl.INSTANCE::isMetallic));
    }

    @Override
    public boolean isValid(MovingObjectPosition target)
    {
        switch (target.typeOfHit)
        {
            case BLOCK:
                return affectedBlocks().contains(target.getBlockPos());
            case ENTITY:
                return affectedEntities().contains(target.entityHit);
        }
        return false;
    }

    @Override
    public void apply(MovingObjectPosition pos)
    {
        if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            applyBlockEffect(pos);
        else if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY)
            applyEntityEffect(pos);
    }

    private void applyBlockEffect(MovingObjectPosition pos)
    {
        if (!affectedBlocks.contains(pos.getBlockPos()))
            return;
        applyEffect(entity, pos.hitVec, 1);
    }

    private void applyEntityEffect(MovingObjectPosition pos)
    {
        if (!affectedEntities.contains(pos.entityHit) || pos.entityHit == entity)
            return;
        applyEffect(pos.entityHit, pos.hitVec, -1);
    }

    private void applyEffect(Entity target, Vec3 end, double factor)
    {
        Vec3 start = entity.getPositionVector()
                           .addVector(0, entity.height / 2, 0);
        double distance = 1 / start.distanceTo(end) * 0.1;
        Vec3 direction = start.subtract(end);
        Vec3 velocity = new Vec3(direction.xCoord * distance * distanceFactor().xCoord * factor,
                                 direction.yCoord * distance * distanceFactor().yCoord * factor,
                                 direction.zCoord * distance * distanceFactor().zCoord * factor);

        target.addVelocity(velocity.xCoord, velocity.yCoord, velocity.zCoord);
        if (target == entity)
            entity.fallDistance = (float) Math.max(0, entity.fallDistance - velocity.yCoord / factor);
    }

    @Override
    public boolean repeatEvent()
    {
        return true;
    }

    public abstract Vec3 distanceFactor();

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
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
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
                    PositionWrapper wrapper = PositionWrapper.from(player.worldObj, p);
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
                renderLines(event.partialTicks);
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
            glLineWidth(4);

            Vec3 playerPos = Rendering.interpolatedPosition(Minecraft.getMinecraft().thePlayer, partialTicks);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();
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

        private void drawPos(Vec3 start, PositionWrapper pos, float partialTicks, float progress)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();
            Vec3 element = pos.center();
            Vec3 direction = element.subtract(start);
            Vec3 end = new Vec3(direction.xCoord * progress, direction.yCoord * progress, direction.zCoord * progress);

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            Vec3 off = new Vec3(0, -0.09D, 0);
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
                    public Vec3 center()
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
                    public Vec3 center()
                    {
                        Block block = access.getBlockState(pos).getBlock();
                        block.setBlockBoundsBasedOnState(access, pos);
                        Vec3 offset = new Vec3(block.getBlockBoundsMinX() + (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX()) / 2,
                                               block.getBlockBoundsMinY() + (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY()) / 2,
                                               block.getBlockBoundsMinZ() + (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ()) / 2);
                        return new Vec3(pos).add(offset);
                    }
                };
            }

            private PositionWrapper(@Nonnull Object base)
            {
                this.base = base;
            }

            public abstract Vec3 center();

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
