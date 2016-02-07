package de.mineformers.investiture.allomancy.api;

import de.mineformers.investiture.allomancy.api.misting.Misting;

/**
 * ${JDOC}
 */
public interface MistingFactory<T extends Misting>
{
    T create();

    default Class<? extends Misting> referenceClass()
    {
        return create().getClass();
    }
}
