package de.mineformers.allomancy.metal;

/**
 * MetalEffects
 *
 * @author PaleoCrafter
 */
public interface MetalEffects extends AllomanticMetal {
    void startBurning(MetalBurner burner);

    void stopBurning(MetalBurner burner);
}
