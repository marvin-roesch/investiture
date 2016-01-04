package de.mineformers.investiture.allomancy.metal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Interface representing the properties of any allomantic metal.
 * This is to be used like Vanilla's Blocks and Items, not storing any data itself.
 */
public interface AllomanticMetal
{
    /**
     * @return the metal's internal ID
     */
    String id();

    /**
     * Determines whether the stack can be burned or is impure.
     *
     * @param stack the stack to check
     * @return true if the stack is burnable, false if it is not pure enough
     */
    boolean canBurn(@Nonnull ItemStack stack);

    /**
     * Determines the energy value stored in some form of metal.
     *
     * @param stack the stack to check
     * @return the energy value of the stack
     */
    default int getValue(@Nonnull ItemStack stack)
    {
        if (canBurn(stack))
            return stack.stackSize;
        else
            return 0;
    }

    /**
     * Apply any effects caused by the consumption of impure metals to an entity.
     *
     * @param entity the affected entity
     */
    default void applyImpurityEffects(Entity entity)
    {
        if (entity instanceof EntityLivingBase)
        {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 30, 3));
        }
    }

    /**
     * @return the metal's unlocalised name
     */
    default String unlocalisedName()
    {
        return "allomancy.metals." + id() + ".name";
    }

    /**
     * Basic abstract implementation of metals with an ID which also functions as equality measure.
     */
    abstract class Abstract implements AllomanticMetal
    {
        private final String _id;

        Abstract(@Nonnull String id)
        {
            this._id = id;
        }

        @Override
        public String id()
        {
            return _id;
        }

        @Override
        public int hashCode()
        {
            return _id.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return Objects.equals(id(), ((AllomanticMetal) obj).id());
        }
    }
}
