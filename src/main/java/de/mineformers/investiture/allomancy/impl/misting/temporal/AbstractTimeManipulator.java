package de.mineformers.investiture.allomancy.impl.misting.temporal;

import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.temporal.TimeManipulator;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import de.mineformers.investiture.client.KeyBindings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * ${JDOC}
 */
public abstract class AbstractTimeManipulator extends AbstractMisting implements TimeManipulator
{
    @Inject
    protected Allomancer allomancer;
    @Inject
    protected Entity entity;

    @Override
    public boolean isValid(BlockPos pos)
    {
        if (entity.world.isRemote)
        {
            return KeyBindings.SET_TEMPORAL.isKeyDown();
        }
        return true;
    }

    @Override
    public boolean isValid(Entity entity)
    {
        return false;
    }

    @Override
    public void apply(RayTraceResult target)
    {
        if (entity.world.isRemote)
            return;
        SpeedBubbles bubbles = SpeedBubbles.from(entity.world);
        SpeedBubble existing = bubbles.get(entity.getUniqueID());
        if (existing != null && existing.dimension == entity.world.provider.getDimension() && existing.position.equals(target.getBlockPos()))
            bubbles.remove(entity.getUniqueID());
        else
            bubbles.add(entity, target.getBlockPos(), 0.5);
    }

    @Override
    public boolean repeatEvent()
    {
        return false;
    }

    @Override
    public void stopBurning()
    {
        SpeedBubbles.from(entity.world).remove(entity.getUniqueID());
    }
}
