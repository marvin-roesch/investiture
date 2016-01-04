package de.mineformers.allomancy.network.messages;

import de.mineformers.allomancy.metal.MetalBurner;
import de.mineformers.allomancy.network.Message;

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
