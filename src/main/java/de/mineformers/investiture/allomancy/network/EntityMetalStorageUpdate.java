package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.allomancy.metal.MetalStorage;
import de.mineformers.investiture.network.Message;

/**
 * EntityMetalStorageUpdate
 *
 * @author PaleoCrafter
 */
public class EntityMetalStorageUpdate extends Message
{
    public int entity;
    public MetalStorage storage;

    public EntityMetalStorageUpdate()
    {
    }

    public EntityMetalStorageUpdate(int entity, MetalStorage storage)
    {
        this.entity = entity;
        this.storage = storage;
    }
}
