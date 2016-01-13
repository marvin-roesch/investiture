package de.mineformers.investiture.allomancy.smeltery;

import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface SmelteryRecipe
{
    @Nonnull
    Optional<ItemStack> match(@Nonnull ItemStack[] input);
}