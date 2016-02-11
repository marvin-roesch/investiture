package de.mineformers.investiture.allomancy.api.power;

import de.mineformers.investiture.allomancy.api.misting.physical.Coinshot;
import de.mineformers.investiture.allomancy.api.misting.physical.Lurcher;

/**
 * Allomantic powers can be classified by the effect they have on a target.
 * The essential difference is between pushing and pulling powers, which can have immediate physical implications (see
 * {@link Coinshot Coinshot} vs {@link Lurcher Lurcher})
 * or describe an antagonism on the meta level.
 */
public enum Effect
{
    /**
     * Pushing powers are always granted by an alloy of the corresponding pulling metal.
     * They accordingly act as complementary power to the pulling power.
     */
    PUSH,
    /**
     * Pulling powers can only be gained through 'pure' metals (i.e. not through alloys).
     */
    PULL
}
