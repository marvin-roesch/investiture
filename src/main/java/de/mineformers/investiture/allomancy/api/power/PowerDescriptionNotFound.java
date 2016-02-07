package de.mineformers.investiture.allomancy.api.power;

/**
 * Thrown when an {@link AllomanticPower} annotation can't be found on a class
 * implementing {@link de.mineformers.investiture.allomancy.api.misting.Misting Misting}.
 * Note that it will definitely not be thrown if the class overrides the right methods.
 */
public class PowerDescriptionNotFound extends RuntimeException
{
    public PowerDescriptionNotFound(Class<?> target)
    {
        super(String.format("Allomantic power description (de.mineformers.investiture.allomancy.api.power.AllomanticPower" +
                                "annotation) not found for class '%s'!", target.getName()));
    }
}
