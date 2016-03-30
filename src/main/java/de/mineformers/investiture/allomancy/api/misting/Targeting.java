package de.mineformers.investiture.allomancy.api.misting;

import net.minecraft.util.math.RayTraceResult;

/**
 * ${JDOC}
 */
public interface Targeting extends Misting
{
    boolean isValid(RayTraceResult target);

    void apply(RayTraceResult target);

    boolean repeatEvent();
}
