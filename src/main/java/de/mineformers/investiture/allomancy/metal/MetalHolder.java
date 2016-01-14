package de.mineformers.investiture.allomancy.metal;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public interface MetalHolder<T>
{

    Metal getMetal(T stack);

    float getMetalQuantity(T stack);

}
