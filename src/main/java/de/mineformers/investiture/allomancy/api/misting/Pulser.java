package de.mineformers.investiture.allomancy.api.misting;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;

import static de.mineformers.investiture.allomancy.api.power.Category.PHYSICAL;
import static de.mineformers.investiture.allomancy.api.power.Category.TEMPORAL;
import static de.mineformers.investiture.allomancy.api.power.Effect.PULL;
import static de.mineformers.investiture.allomancy.api.power.Effect.PUSH;
import static de.mineformers.investiture.allomancy.api.power.Scope.EXTERNAL;

/**
 * ${JDOC}
 */
@AllomanticPower(category = TEMPORAL, scope = EXTERNAL, effect = PULL)
public interface Pulser extends Misting
{
    @Override
    default Metal baseMetal()
    {
        return Metals.CADMIUM;
    }
}
