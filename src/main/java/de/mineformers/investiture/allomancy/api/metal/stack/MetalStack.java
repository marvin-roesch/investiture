package de.mineformers.investiture.allomancy.api.metal.stack;

import de.mineformers.investiture.allomancy.api.metal.Metal;

/**
 * Capability to retrieve metal data.
 */
public interface MetalStack
{
    Metal getMetal();

    float getQuantity();

    float getPurity();
}
