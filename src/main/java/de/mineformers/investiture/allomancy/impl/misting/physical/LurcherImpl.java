package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.physical.Lurcher;
import net.minecraft.util.Vec3;

/**
 * ${JDOC}
 */
public class LurcherImpl extends AbstractMetalManipulator implements Lurcher
{
    @Override
    public Vec3 distanceFactor()
    {
        return new Vec3(-1, -2, -1);
    }
}
