package de.mineformers.investiture.allomancy.network;

import de.mineformers.investiture.network.Message;
import net.minecraft.util.Vec3;

/**
 * ${JDOC}
 */
public class MetalManipulatorEffect extends Message
{
    public int affectedEntity;
    public Vec3 velocity;

    public MetalManipulatorEffect()
    {
    }

    public MetalManipulatorEffect(int affectedEntity, Vec3 velocity)
    {
        this.affectedEntity = affectedEntity;
        this.velocity = velocity;
    }
}
