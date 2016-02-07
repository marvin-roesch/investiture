package de.mineformers.investiture.allomancy.api.power;

import java.lang.annotation.*;

/**
 * Marker annotation for all Allomantic powers (i.e. sub classes of {@link de.mineformers.investiture.allomancy.api.misting.Misting Misting}).
 * Provides easy access to the relevant data inside the interface without need for implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AllomanticPower
{
    /**
     * @return the category of this power, e.g. 'physical', 'mental', 'enhancement' or 'temporal'
     */
    String category();

    /**
     * @return the scope of this power
     */
    Scope scope();

    /**
     * @return the type of effect this power will have on targets
     */
    Effect effect();
}
