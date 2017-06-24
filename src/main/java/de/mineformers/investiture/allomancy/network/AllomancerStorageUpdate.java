package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.network.Message;

import javax.annotation.Nullable;

/**
 * ${JDOC}
 */
public class AllomancerStorageUpdate extends Message
{
    public static final int ACTION_APPEND = 0;
    public static final int ACTION_UPDATE_LAST = 1;
    public static final int ACTION_REMOVE_LAST = 2;

    public int entityId;
    public int action;
    public MetalStack stack;

    public AllomancerStorageUpdate()
    {
    }

    public AllomancerStorageUpdate(int entityId, int action, MetalStack stack)
    {
        this.entityId = entityId;
        this.action = action;
        this.stack = stack;
    }
}
