package de.mineformers.investiture.allomancy.impl.misting.mental;

import de.mineformers.investiture.allomancy.api.misting.mental.Smoker;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;

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
