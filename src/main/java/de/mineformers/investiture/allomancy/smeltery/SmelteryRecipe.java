package de.mineformers.investiture.allomancy.smeltery;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import com.google.common.base.Optional;

public interface SmelteryRecipe
{
    @Nonnull
    Optional<ItemStack> match(@Nonnull ItemStack[] input);
}