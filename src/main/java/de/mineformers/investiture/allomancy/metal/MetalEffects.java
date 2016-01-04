package de.mineformers.investiture.allomancy.metal;

/**
 * MetalEffects
 *
 * @author PaleoCrafter
 */
public interface MetalEffects extends AllomanticMetal
{
    void startBurning(MetalBurner burner);

    void stopBurning(MetalBurner burner);
}
