package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;

/**
 * ${JDOC}
 */
public class MistingUpdate extends Message
{
    public int entityId;
    public String type;
    public byte[] data;

    public MistingUpdate()
    {
    }

    public MistingUpdate(int entityId, String type, byte[] data)
    {
        this.entityId = entityId;
        this.type = type;
        this.data = data;
    }
}
