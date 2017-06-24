package de.mineformers.investiture.allomancy.api.metal;

import de.mineformers.investiture.allomancy.api.metal.stack.MetalStack;
import de.mineformers.investiture.allomancy.api.metal.stack.MetalStackProvider;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public interface MetalStorage extends MetalStackProvider
{
    ItemStack consume(@Nonnull ItemStack stack, int amount, boolean simulate);

    float consume(@Nonnull MetalStack stack, float amount, boolean simulate);

    @Nonnull
    Set<MetalStack> burn(@Nonnull Metal metal, float amount, boolean simulate);

    default float getStoredQuantity(Metal metal) {
        return getStored(metal).stream().reduce(0f, (acc, s) -> acc + s.getQuantity(), (a, b) -> a + b);
    }
}
