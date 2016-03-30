package de.mineformers.investiture.allomancy.api.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.misting.Targeting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

/**
 * ${JDOC}
 */
public interface MetalManipulator extends Misting, Targeting
{
    Collection<BlockPos> affectedBlocks();

    Collection<Entity> affectedEntities();
}
