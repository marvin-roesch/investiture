package de.mineformers.investiture.allomancy.crusher;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * ${JDOC}
 */
public class SimpleCrusherRecipe implements CrusherRecipe
{
    private final ItemStack input;
    private final CrusherOutput output;

    @Nonnull
    public static CrusherRecipe create(ItemStack input, ItemStack result)
    {
        return create(input, result, null);
    }

    @Nonnull
    public static CrusherRecipe create(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult)
    {
        return create(input, primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static CrusherRecipe create(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult, float secondaryChance)
    {
        return create(input, new CrusherOutput(primaryResult, secondaryResult, secondaryChance));
    }

    @Nonnull
    public static CrusherRecipe create(ItemStack input, CrusherOutput result)
    {
        return new SimpleCrusherRecipe(input, result);
    }

    public SimpleCrusherRecipe(ItemStack input, CrusherOutput output)
    {
        this.input = input;
        this.output = output;
    }

    @Nonnull
    @Override
    public Optional<CrusherOutput> match(@Nonnull ItemStack input)
    {
        if (ItemStack.areItemsEqual(input, this.input))
            return Optional.of(output);
        return Optional.empty();
    }
}
