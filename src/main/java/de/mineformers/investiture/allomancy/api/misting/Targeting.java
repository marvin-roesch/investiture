package de.mineformers.investiture.allomancy.api.misting;

import net.minecraft.util.MovingObjectPosition;

/**
 * ${JDOC}
 */
public interface Targeting extends Misting
{
    boolean isValid(MovingObjectPosition target);

    void apply(MovingObjectPosition target);

    boolean repeatEvent();
}
