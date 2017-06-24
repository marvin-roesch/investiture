package de.mineformers.investiture.allomancy.crusher;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * ${JDOC}
 */
public interface CrusherRecipe
{
    @Nonnull
    Optional<CrusherOutput> match(@Nonnull ItemStack input);
}
