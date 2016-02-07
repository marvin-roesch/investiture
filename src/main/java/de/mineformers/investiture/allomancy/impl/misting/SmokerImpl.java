package de.mineformers.investiture.allomancy.impl.misting;

import de.mineformers.investiture.allomancy.api.misting.Smoker;
import net.minecraft.util.ITickable;

/**
 * ${JDOC}
 */
public class SmokerImpl extends AbstractMisting implements Smoker
{
    @Override
    public void startBurning()
    {
        System.out.println("TEST");
    }
}
