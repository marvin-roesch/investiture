package de.mineformers.allomancy.network.messages;

import de.mineformers.allomancy.metal.MetalStorage;
import de.mineformers.allomancy.network.Message;

/**
 * EntityMetalStorageUpdate
 *
 * @author PaleoCrafter
 */
public class EntityMetalStorageUpdate extends Message {
    public int entity;
    public MetalStorage storage;

    public EntityMetalStorageUpdate() {
    }

    public EntityMetalStorageUpdate(int entity, MetalStorage storage) {
        this.entity = entity;
        this.storage = storage;
    }
}
