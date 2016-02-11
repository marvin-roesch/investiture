package de.mineformers.investiture.allomancy.impl.misting.mental;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.api.misting.mental.Rioter;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/**
 * ${JDOC}
 */
public class RioterImpl extends AbstractEmotionManipulator implements Rioter
{
    @Override
    protected AIData gather(EntityCreature entity)
    {
        return AIData.install(entity,
                              ImmutableMap.of(new EntityAIAttackOnCollide(entity, EntityLiving.class, 1.0D, true), 2),
                              ImmutableMap.of(new EntityAINearestAttackableTarget<>(entity, EntityLiving.class, false), 2),
                              t -> t instanceof EntityAIAttackOnCollide,
                              t -> t instanceof EntityAINearestAttackableTarget<?>);
    }

    @Override
    public int villagerReputation()
    {
        return -1;
    }
}
