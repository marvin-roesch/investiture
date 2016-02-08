package de.mineformers.investiture.allomancy.impl.misting;

import de.mineformers.investiture.allomancy.api.misting.Augur;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.client.particle.FootStep;
import de.mineformers.investiture.allomancy.impl.AllomancyAPIImpl;
import de.mineformers.investiture.serialisation.Serialise;
import de.mineformers.investiture.util.PathFinding;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

/**
 * ${JDOC}
 */
public class AugurImpl extends AbstractMisting implements Augur, ITickable
{
    @Inject
    private Entity entity;
    @Serialise
    private int deathDimension;
    @Serialise
    private Vec3 position;
    private boolean validPath = false;
    private int timer;
    private Queue<BlockPos> path = new ArrayDeque<>();

    @Override
    public void startBurning()
    {
        validPath = false;
        timer = 0;
        path.clear();
        if(position == null)
            return;
        if (entity.dimension == deathDimension)
        {
            path.addAll(PathFinding.bresenham(entity, new BlockPos(position)));
            validPath = true;
        }
    }

    @Override
    public void update()
    {
        if(!entity.worldObj.isRemote || position == null)
            return;
        if(!validPath && entity.dimension == deathDimension)
        {
            path.clear();
            path.addAll(PathFinding.bresenham(entity, new BlockPos(position)));
            timer = 0;
            validPath = true;
        }
        if(!validPath || path.isEmpty())
            return;
        timer++;
        if (timer > 5)
            timer = 0;
        else
            return;
        BlockPos step = path.poll();
        FootStep particle1 = new FootStep(entity.worldObj, new Vec3(step).addVector(0.3, 0.0001, 0.3), 0.28627452f, 0.7254902f, 0.87058824f);
        FootStep particle2 = new FootStep(entity.worldObj, new Vec3(step).addVector(0.6, 0.0001, 0.6), 0.28627452f, 0.7254902f, 0.87058824f);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle1);
        Minecraft.getMinecraft().effectRenderer.addEffect(particle2);
    }

    @Override
    public Optional<Vec3> lastDeathPosition()
    {
        return Optional.ofNullable(position);
    }

    public static class EventHandler
    {
        @SubscribeEvent
        public void onDeath(LivingDeathEvent event)
        {
            if (event.entity.worldObj.isRemote)
                return;
            AllomancyAPIImpl.INSTANCE.toAllomancer(event.entity).flatMap(a -> a.as(Augur.class)).ifPresent(a -> {
                if (a instanceof AugurImpl)
                {
                    ((AugurImpl) a).deathDimension = event.entity.dimension;
                    ((AugurImpl) a).position = event.entity.getPositionVector();
                }
            });
        }
    }
}
