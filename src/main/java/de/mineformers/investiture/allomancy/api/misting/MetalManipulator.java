package de.mineformers.investiture.allomancy.api.misting;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.Collection;

/**
 * ${JDOC}
 */
public interface MetalManipulator extends Misting
{
    Collection<BlockPos> affectedBlocks();

    Collection<Entity> affectedEntities();

    void applyEffect(MovingObjectPosition pos);
}
