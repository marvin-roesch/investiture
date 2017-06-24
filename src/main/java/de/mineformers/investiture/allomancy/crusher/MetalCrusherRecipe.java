package de.mineformers.investiture.allomancy.crusher;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.helper.MetalStacks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

/**
 * ${JDOC}
 */
public class MetalCrusherRecipe implements CrusherRecipe
{
    private static Random RANDOM = new Random();
    private final ItemStack input;
    private final Metal primaryOutput;
    @Nullable
    private final Metal secondaryOutput;
    private final float secondaryChance;

    @Nonnull
    public static CrusherRecipe create(ItemStack input, Metal result)
    {
        return create(input, result, null);
    }

    @Nonnull
    public static CrusherRecipe create(ItemStack input, Metal primaryResult, @Nullable Metal secondaryResult)
    {
        return create(input, primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static CrusherRecipe create(ItemStack input, Metal primaryResult, @Nullable Metal secondaryResult, float secondaryChance)
    {
        return new MetalCrusherRecipe(input, primaryResult, secondaryResult, secondaryChance);
    }

    public MetalCrusherRecipe(ItemStack input, Metal primaryResult, @Nullable Metal secondaryResult, float secondaryChance)
    {
        this.input = input;
        this.primaryOutput = primaryResult;
        this.secondaryOutput = secondaryResult;
        this.secondaryChance = secondaryChance;
    }

    @Nonnull
    @Override
    public Optional<CrusherOutput> match(@Nonnull ItemStack input)
    {
        if (ItemStack.areItemsEqual(input, this.input))
        {
            ItemStack primary = MetalStacks.chunk(primaryOutput, 1, RANDOM.nextInt(10) + 8).get();
            ItemStack secondary = secondaryOutput != null ? MetalStacks.chunk(secondaryOutput, 1, RANDOM.nextInt(10) + 8).get()
                                                          : null;
            return CrusherOutput.optional(primary, secondary, secondaryChance);
        }
        return Optional.empty();
    }
}
