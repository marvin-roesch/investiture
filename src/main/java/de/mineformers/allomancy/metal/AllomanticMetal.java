package de.mineformers.allomancy.metal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * AllomanticMetal
 *
 * @author PaleoCrafter
 */
public interface AllomanticMetal {
    String id();

    boolean canBurn(@Nonnull ItemStack stack);

    default int getValue(@Nonnull ItemStack stack) {
        if(canBurn(stack))
            return stack.stackSize;
        else
            return 0;
    }

    default String unlocalizedName() {
        return "allomancy.metal." + id() + ".name";
    }

    abstract class AbstractAllomanticMetal implements AllomanticMetal {
        private final String _id;

        AbstractAllomanticMetal(@Nonnull String id) {
            this._id = id;
        }

        @Override
        public String id() {
            return _id;
        }

        @Override
        public int hashCode() {
            return _id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if(obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            return Objects.equals(id(), ((AllomanticMetal) obj).id());
        }
    }
}
