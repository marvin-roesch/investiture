package de.mineformers.investiture.allomancy.extractor;

import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * ${JDOC}
 */
public interface ExtractorRecipe
{
    @Nonnull
    Optional<ExtractorOutput> match(@Nonnull ItemStack input);
}
