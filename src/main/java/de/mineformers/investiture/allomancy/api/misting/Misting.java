package de.mineformers.investiture.allomancy.api.misting;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;
import de.mineformers.investiture.allomancy.api.power.Effect;
import de.mineformers.investiture.allomancy.api.power.PowerDescriptionNotFound;
import de.mineformers.investiture.allomancy.api.power.Scope;

/**
 * ${JDOC}
 */
public interface Misting
{
    Metal baseMetal();

    default String category()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = clazz.getAnnotation(AllomanticPower.class);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.category();
    }

    default Scope scope()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = clazz.getAnnotation(AllomanticPower.class);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.scope();
    }

    default Effect effect()
    {
        Class<?> clazz = this.getClass();
        AllomanticPower descriptor = clazz.getAnnotation(AllomanticPower.class);
        if (descriptor == null)
            throw new PowerDescriptionNotFound(clazz);
        return descriptor.effect();
    }
}
