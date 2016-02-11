package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;
import net.minecraft.util.MovingObjectPosition;

/**
 * ${JDOC}
 */
public class TargetEffect extends Message
{
    public int entityId;
    public String type;
    public MovingObjectPosition target;

    public TargetEffect()
    {
    }

    public TargetEffect(int entityId, String type, MovingObjectPosition target)
    {
        this.entityId = entityId;
        this.type = type;
        this.target = target;
    }
}
