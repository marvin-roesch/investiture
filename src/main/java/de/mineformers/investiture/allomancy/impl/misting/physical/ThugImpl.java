package de.mineformers.investiture.allomancy.impl.misting.physical;

import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.physical.Thug;
import de.mineformers.investiture.allomancy.impl.misting.AbstractMisting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

import static de.mineformers.investiture.util.Attributes.*;

/**
 * ${JDOC}
 */
public class ThugImpl extends AbstractMisting implements Thug
{
    private static final UUID ATTACK_MODIFIER_ID = UUID.fromString("BC768F65-1B49-462F-8286-33EA62E0A909");
    private static final UUID RESISTANCE_MODIFIER_ID = UUID.fromString("BC768F65-1B49-462F-8286-33EA62E0A909");
    @Inject
    private Entity entity;

    @Override
    public void startBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            AttributeModifier attackModifier = new AttributeModifier(ATTACK_MODIFIER_ID, "Thug modifier", 6, OP_ADD);
            AttributeModifier damageModifier = new AttributeModifier(RESISTANCE_MODIFIER_ID, "Thug modifier", 2.5, OP_ADD);
            IAttributeInstance attribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage);
            attribute.removeModifier(attackModifier);
            attribute.applyModifier(attackModifier);
            applyDamageResistanceModifier(living, damageModifier);
        }
    }

    @Override
    public void stopBurning()
    {
        if (entity instanceof EntityLivingBase)
        {
            EntityLivingBase living = (EntityLivingBase) entity;
            AttributeModifier attackModifier = new AttributeModifier(ATTACK_MODIFIER_ID, "Thug modifier", 6, OP_ADD);
            AttributeModifier damageModifier = new AttributeModifier(RESISTANCE_MODIFIER_ID, "Thug modifier", 2.5, OP_ADD);
            IAttributeInstance attribute = living.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage);
            attribute.removeModifier(attackModifier);
            removeDamageResistanceModifier(living, damageModifier);
        }
    }
}
