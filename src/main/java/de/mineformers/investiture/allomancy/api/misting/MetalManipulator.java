package de.mineformers.investiture.allomancy.api.misting;

import net.minecraft.util.BlockPos;

import java.util.Collection;

/**
 * ${JDOC}
 */
public interface MetalManipulator extends Misting
{
    Collection<BlockPos> affectedBlocks();
}
