package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.allomancy.metal.MetalBurner;
import de.mineformers.investiture.network.Message;

/**
 * EntityMetalBurnerUpdate
 *
 * @author PaleoCrafter
 */
public class EntityMetalBurnerUpdate extends Message
{
    public int entity;
    public MetalBurner burner;

    public EntityMetalBurnerUpdate()
    {
    }

    public EntityMetalBurnerUpdate(int entity, MetalBurner burner)
    {
        this.entity = entity;
        this.burner = burner;
    }
}
