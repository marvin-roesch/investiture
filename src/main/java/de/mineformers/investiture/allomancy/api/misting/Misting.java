package de.mineformers.investiture.allomancy.api.misting;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;
import de.mineformers.investiture.allomancy.api.power.Effect;
import de.mineformers.investiture.allomancy.api.power.PowerDescriptionNotFound;
import de.mineformers.investiture.allomancy.api.power.Scope;

import java.util.Arrays;

/**
 * ${JDOC}
 */
public interface Misting
{
    Metal baseMetal();

    default String category()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = Arrays.stream(clazz.getInterfaces())
                                           .filter(Misting.class::isAssignableFrom)
                                           .findFirst()
                                           .map(c -> c.getAnnotation(AllomanticPower.class))
                                           .orElse(null);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.category();
    }

    default Scope scope()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = Arrays.stream(clazz.getInterfaces())
                                           .filter(Misting.class::isAssignableFrom)
                                           .findFirst()
                                           .map(c -> c.getAnnotation(AllomanticPower.class))
                                           .orElse(null);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.scope();
    }

    default Effect effect()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = Arrays.stream(clazz.getInterfaces())
                                           .filter(Misting.class::isAssignableFrom)
                                           .findFirst()
                                           .map(c -> c.getAnnotation(AllomanticPower.class))
                                           .orElse(null);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.effect();
    }
}
