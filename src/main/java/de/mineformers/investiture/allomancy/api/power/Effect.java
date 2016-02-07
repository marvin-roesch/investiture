package de.mineformers.investiture.allomancy.api.power;

/**
 * Allomantic powers can be classified by the effect they have on a target.
 * The essential difference is between pushing and pulling powers, which can have immediate physical implications (see
 * {@link de.mineformers.investiture.allomancy.api.misting.Coinshot Coinshot} vs {@link de.mineformers.investiture.allomancy.api.misting.Lurcher Lurcher})
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
