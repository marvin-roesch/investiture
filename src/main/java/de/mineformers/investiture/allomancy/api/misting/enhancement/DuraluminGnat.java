package de.mineformers.investiture.allomancy.api.misting.enhancement;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.misting.Gnat;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;

import static de.mineformers.investiture.allomancy.api.power.Category.ENHANCEMENT;
import static de.mineformers.investiture.allomancy.api.power.Effect.PUSH;
import static de.mineformers.investiture.allomancy.api.power.Scope.INTERNAL;

/**
 * ${JDOC}
 */
@AllomanticPower(category = ENHANCEMENT, scope = INTERNAL, effect = PUSH)
public interface DuraluminGnat extends Gnat
{
    @Override
    default Metal baseMetal()
    {
        return Metals.DURALUMIN;
    }
}
