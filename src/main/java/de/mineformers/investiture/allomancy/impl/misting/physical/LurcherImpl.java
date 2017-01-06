package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.physical.Lurcher;
import net.minecraft.util.math.Vec3d;

/**
 * ${JDOC}
 */
public class LurcherImpl extends AbstractMetalManipulator implements Lurcher
{
    @Override
    public Vec3d distanceFactor()
    {
        return new Vec3d(-1, -1.5, -1);
    }
}
