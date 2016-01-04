package de.mineformers.investiture.allomancy.metal;

/**
 * Provides access to the user of a metal after they start or stop burning it.
 */
public interface MetalEffects extends AllomanticMetal
{
    /**
     * Called whenever a given user starts to burn the metal.
     * Can be used to add effects that are permanent for the duration of the burning.
     *
     * @param burner the user of the metal
     */
    void startBurning(MetalBurner burner);

    /**
     * Called whenever a given user stops burning the metal.
     * Can be used to remove previously added effects.
     *
     * @param burner the user of the metal
     */
    void stopBurning(MetalBurner burner);
}
