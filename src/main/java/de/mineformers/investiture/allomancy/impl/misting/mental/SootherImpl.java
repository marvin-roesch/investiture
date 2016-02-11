package de.mineformers.investiture.allomancy.impl.misting.mental;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.api.misting.mental.Soother;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/**
 * ${JDOC}
 */
public class SootherImpl extends AbstractEmotionManipulator implements Soother
{
    @Override
    protected AIData gather(EntityCreature entity)
    {
        return AIData.install(entity,
                              ImmutableMap.of(),
                              ImmutableMap.of(),
                              t -> t instanceof EntityAIAttackOnCollide,
                              t -> t instanceof EntityAINearestAttackableTarget<?>);
    }

    @Override
    public int villagerReputation()
    {
        return 1;
    }
}
