package de.mineformers.investiture.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * ${JDOC}
 */
public class Attributes
{
    public static final int OP_ADD = 0;
    public static final int OP_ADD_PERCENTAGE = 1;
    public static final int OP_PERCENTAGE = 2;

    public static final IAttribute DAMAGE_RESISTANCE = new RangedAttribute(null, "investiture.damageResistance", 0, 0, 2048);

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static double getDamageResistance(EntityLivingBase entity)
    {
        IAttributeInstance instance = entity.getAttributeMap().getAttributeInstance(DAMAGE_RESISTANCE);
        if (instance != null)
        {
            return instance.getAttributeValue();
        }
        else
        {
            return 0;
        }
    }

    public static void applyDamageResistanceModifier(EntityLivingBase entity, AttributeModifier modifier)
    {
        IAttributeInstance instance = entity.getAttributeMap().getAttributeInstance(DAMAGE_RESISTANCE);
        if (instance == null)
            instance = entity.getAttributeMap().registerAttribute(DAMAGE_RESISTANCE);
        instance.removeModifier(modifier);
        instance.applyModifier(modifier);
    }

    public static void removeDamageResistanceModifier(EntityLivingBase entity, AttributeModifier modifier)
    {
        IAttributeInstance instance = entity.getAttributeMap().getAttributeInstance(DAMAGE_RESISTANCE);
        if (instance == null)
            return;
        instance.removeModifier(modifier);
    }

    private static class EventHandler
    {
        @SubscribeEvent
        public void onDamage(LivingHurtEvent event)
        {
            event.ammount = (float) Math.max(event.ammount - getDamageResistance(event.entityLiving), 0);
        }
    }
}
