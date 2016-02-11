package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.physical.Coinshot;
import net.minecraft.util.Vec3;

/**
 * ${JDOC}
 */
public class CoinshotImpl extends AbstractMetalManipulator implements Coinshot
{
    @Override
    public Vec3 distanceFactor()
    {
        return new Vec3(1, 1, 1);
    }
}
