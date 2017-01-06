package de.mineformers.investiture.allomancy.api.misting;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * ${JDOC}
 */
public interface Targeting extends Misting
{
    default boolean isValid(RayTraceResult target)
    {
        switch (target.typeOfHit)
        {
            case BLOCK:
                return isValid(target.getBlockPos());
            case ENTITY:
                return isValid(target.entityHit);
        }
        return false;
    }

    boolean isValid(BlockPos pos);

    boolean isValid(Entity entity);

    void apply(RayTraceResult target);

    boolean repeatEvent();
}
