package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.AllomancyConfig;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.physical.Thug;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

import static de.mineformers.investiture.util.Attributes.OP_ADD;

/**
 * ${JDOC}
 */
public class ThugImpl extends AbstractMisting implements Thug
{
    private static final UUID ATTACK_MODIFIER_ID = UUID.fromString("BC768F65-1B49-462F-8286-33EA62E0A909");
    private static final UUID ARMOUR_MODIFIER_ID = UUID.fromString("BC768F65-1B49-462F-8286-33EA62E0A909");
    @Inject
    private Entity entity;

    @Override
    public void startBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            AttributeModifier attackModifier = new AttributeModifier(ATTACK_MODIFIER_ID, "Thug modifier",
                                                                     AllomancyConfig.mistings.thug.attackBoost, OP_ADD);
            AttributeModifier armourModifier = new AttributeModifier(ARMOUR_MODIFIER_ID, "Thug modifier",
                                                                     AllomancyConfig.mistings.thug.damageResistance, OP_ADD);
            IAttributeInstance attackAttribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
            attackAttribute.removeModifier(attackModifier);
            attackAttribute.applyModifier(attackModifier);
            IAttributeInstance armourAttribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ARMOR);
            armourAttribute.removeModifier(attackModifier);
            armourAttribute.applyModifier(armourModifier);
        }
    }

    @Override
    public void stopBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            AttributeModifier attackModifier = new AttributeModifier(ATTACK_MODIFIER_ID, "Thug modifier",
                                                                     AllomancyConfig.mistings.thug.attackBoost, OP_ADD);
            AttributeModifier armourModifier = new AttributeModifier(ARMOUR_MODIFIER_ID, "Thug modifier",
                                                                     AllomancyConfig.mistings.thug.damageResistance, OP_ADD);
            IAttributeInstance attackAttribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
            attackAttribute.removeModifier(attackModifier);

            IAttributeInstance armourAttribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.ARMOR);
            armourAttribute.removeModifier(armourModifier);
        }
    }
}
