package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;
import net.minecraft.util.math.RayTraceResult;

/**
 * ${JDOC}
 */
public class TargetEffect extends Message
{
    public int entityId;
    public String type;
    public RayTraceResult target;

    public TargetEffect()
    {
    }

    public TargetEffect(int entityId, String type, RayTraceResult target)
    {
        this.entityId = entityId;
        this.type = type;
        this.target = target;
    }
}
