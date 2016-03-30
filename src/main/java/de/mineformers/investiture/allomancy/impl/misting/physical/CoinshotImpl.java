package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.physical.Coinshot;
import net.minecraft.util.math.Vec3d;

/**
 * ${JDOC}
 */
public class CoinshotImpl extends AbstractMetalManipulator implements Coinshot
{
    @Override
    public Vec3d distanceFactor()
    {
        return new Vec3d(1, 1, 1);
    }
}
