package de.mineformers.investiture.allomancy.metal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * AllomanticMetal
 *
 * @author PaleoCrafter
 */
public interface AllomanticMetal
{
    String id();

    boolean canBurn(@Nonnull ItemStack stack);

    default int getValue(@Nonnull ItemStack stack)
    {
        if (canBurn(stack))
            return stack.stackSize;
        else
            return 0;
    }

    default void applyImpurityEffects(Entity entity)
    {
        if (entity instanceof EntityLivingBase)
        {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 30, 3));
        }
    }

    default String unlocalizedName()
    {
        return "allomancy.metals." + id() + ".name";
    }

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
