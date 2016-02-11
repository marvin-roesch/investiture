package de.mineformers.investiture.allomancy.api.misting.temporal;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.allomancy.api.power.AllomanticPower;
import net.minecraft.util.BlockPos;

import static de.mineformers.investiture.allomancy.api.power.Category.TEMPORAL;
import static de.mineformers.investiture.allomancy.api.power.Effect.PUSH;
import static de.mineformers.investiture.allomancy.api.power.Scope.INTERNAL;

/**
 * ${JDOC}
 */
@AllomanticPower(category = TEMPORAL, scope = INTERNAL, effect = PUSH)
public interface Oracle extends Misting
{
    @Override
    default Metal baseMetal()
    {
        return Metals.ELECTRUM;
    }

    BlockPos spawnPoint();
}
