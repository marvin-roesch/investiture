package de.mineformers.investiture.allomancy.extractor;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * ${JDOC}
 */
public class SimpleExtractorRecipe implements ExtractorRecipe
{
    private final ItemStack input;
    private final ExtractorOutput output;

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, ItemStack result)
    {
        return create(input, result, null);
    }

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult)
    {
        return create(input, primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult, float secondaryChance)
    {
        return create(input, new ExtractorOutput(primaryResult, secondaryResult, secondaryChance));
    }

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, ExtractorOutput result)
    {
        return new SimpleExtractorRecipe(input, result);
    }

    public SimpleExtractorRecipe(ItemStack input, ExtractorOutput output)
    {
        this.input = input;
        this.output = output;
    }

    @Nonnull
    @Override
    public Optional<ExtractorOutput> match(@Nonnull ItemStack input)
    {
        if (ItemStack.areItemsEqual(input, this.input))
            return Optional.of(output);
        return Optional.empty();
    }
}
