package de.mineformers.investiture.allomancy.api.metal;

import de.mineformers.investiture.allomancy.api.misting.Misting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Interface representing the properties of any allomantic metal.
 * This is to be used like Vanilla's Blocks and Items, not storing any data itself.
 */
public interface Metal
{
    /**
     * @return the metal's internal ID
     */
    String id();

    default float burnRate() {
        return 0f;
    }

    /**
     * Determines whether the stack can be burned or is impure.
     *
     * @return true if the stack is burnable, false if it is not pure enough
     */
    default boolean canBurn()
    {
        return true;
    }

    default boolean matches(@Nonnull ItemStack stack)
    {
        return Metals.getMetalStacks(stack).stream().anyMatch(s -> this.equals(s.getMetal()));
    }

    /**
     * Apply any effects caused by the consumption of impure metals to an entity.
     *
     * @param entity the affected entity
     */
    default void applyImpurityEffects(Entity entity, float impurities)
    {
        if (entity instanceof EntityLivingBase)
        {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30, 3));
        }
    }

    /**
     * @return the metal's unlocalised name
     */
    default String unlocalisedName()
    {
        return "allomancy.metals." + id() + ".name";
    }

    Class<? extends Misting> mistingType();

    /**
     * Basic abstract implementation of metals with an ID which also functions as equality measure.
     */
    abstract class AbstractMetal implements Metal
    {
        private final String _id;
        private final Class<? extends Misting> mistingType;

        AbstractMetal(@Nonnull String id, Class<? extends Misting> mistingType)
        {
            this._id = id;
            this.mistingType = mistingType;
        }

        @Override
        public String id()
        {
            return _id;
        }

        @Override
        public Class<? extends Misting> mistingType()
        {
            return mistingType;
        }

        @Override
        public int hashCode()
        {
            return _id.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            return Objects.equals(id(), ((Metal) obj).id());
        }

        @Override
        public String toString()
        {
            return id();
        }
    }
}
