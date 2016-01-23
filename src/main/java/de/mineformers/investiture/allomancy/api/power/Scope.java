package de.mineformers.investiture.allomancy.api.power;

/**
 * The scope of an Allomantic power defines what its effects will be applied to.
 */
public enum Scope
{
    /**
     * Internal powers affect an Allomancer directly.
     * It is noteworthy that this does not mean the power will <strong>only</strong> affect the Allomancer (see
     * {@link de.mineformers.investiture.allomancy.api.misting.Smoker Smoker}).
     */
    INTERNAL,
    /**
     * External powers affect the world surrounding an Allomancer. Targets can be both living and inanimate objects.
     * Note that the effect can also apply to the Allomancer directly (see {@link de.mineformers.investiture.allomancy.api.misting.Lurcher Lurcher}).
     */
    EXTERNAL
}
