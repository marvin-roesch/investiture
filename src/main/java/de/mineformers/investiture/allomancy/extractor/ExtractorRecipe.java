package de.mineformers.investiture.allomancy.extractor;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * ${JDOC}
 */
public interface ExtractorRecipe
{
    @Nonnull
    Optional<ExtractorOutput> match(@Nonnull ItemStack input);
}
