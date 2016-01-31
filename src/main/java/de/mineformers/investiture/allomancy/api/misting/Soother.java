package de.mineformers.investiture.allomancy.api.misting;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;

import static de.mineformers.investiture.allomancy.api.power.Category.MENTAL;
import static de.mineformers.investiture.allomancy.api.power.Effect.PULL;
import static de.mineformers.investiture.allomancy.api.power.Scope.EXTERNAL;

/**
 * ${JDOC}
 */
@AllomanticPower(category = MENTAL, scope = EXTERNAL, effect = PULL)
public interface Soother extends Misting
{
    @Override
    default Metal baseMetal()
    {
        return Metals.BRASS;
    }
}
