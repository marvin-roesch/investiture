package de.mineformers.investiture.allomancy.api.power;

import de.mineformers.investiture.allomancy.api.misting.mental.Seeker;

/**
 * Holds constants for easy access to the various power categories.
 * The category is the most important piece of information for determining the effect of a metal.
 */
public class Category
{
    /**
     * Physical Allomancy has to do with the Allomancer's body or the physical world surrounding them.
     * Examples are pewter enhancing strength or steel and iron controlling metallic objects.
     */
    public static final String PHYSICAL = "physical";
    /**
     * Mental Allomancy affects people's minds.
     * Examples are copper clouds hiding you from {@link Seeker Seekers} or brass soothing the
     * emotions of a target.
     */
    public static final String MENTAL = "mental";
    /**
     * Enhancing Allomancy manipulates both Allomantic strength and reserves.
     * Examples are duralumin giving a surge of Allomantic power while burning out all other metals or chromium wiping metal reserves of a target.
     */
    public static final String ENHANCEMENT = "enhancement";
    /**
     * Temporal Allomancy grants an Allomancer access to the flow of time.
     * Examples are cadmium and bendalloy which will accelerate or slow down time for the user respectively.
     */
    public static final String TEMPORAL = "temporal";
}
