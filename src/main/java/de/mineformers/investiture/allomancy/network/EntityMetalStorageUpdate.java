package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.allomancy.api.metal.MetalStorage;
import de.mineformers.investiture.network.Message;

/**
 * Updates a metal storage.
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
