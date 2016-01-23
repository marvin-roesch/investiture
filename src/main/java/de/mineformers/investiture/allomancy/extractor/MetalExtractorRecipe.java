package de.mineformers.investiture.allomancy.extractor;

import de.mineformers.investiture.allomancy.helper.AllomanticMetalItemHelper;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Random;

/**
 * ${JDOC}
 */
public class MetalExtractorRecipe implements ExtractorRecipe
{
    private static Random RANDOM = new Random();
    private final ItemStack input;
    private final Metal primaryOutput;
    private final Metal secondaryOutput;
    private final float secondaryChance;

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, Metal result)
    {
        return create(input, result, null);
    }

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, Metal primaryResult, Metal secondaryResult)
    {
        return create(input, primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static ExtractorRecipe create(ItemStack input, Metal primaryResult, Metal secondaryResult, float secondaryChance)
    {
        return new MetalExtractorRecipe(input, primaryResult, secondaryResult, secondaryChance);
    }

    public MetalExtractorRecipe(ItemStack input, Metal primaryResult, Metal secondaryResult, float secondaryChance)
    {
        this.input = input;
        this.primaryOutput = primaryResult;
        this.secondaryOutput = secondaryResult;
        this.secondaryChance = secondaryChance;
    }

    @Nonnull
    @Override
    public Optional<ExtractorOutput> match(@Nonnull ItemStack input)
    {
        if (ItemStack.areItemsEqual(input, this.input))
        {
            ItemStack primary = AllomanticMetalItemHelper.chunk(primaryOutput, 1, RANDOM.nextInt(6) + 95).get();
            ItemStack secondary = secondaryOutput != null ? AllomanticMetalItemHelper.chunk(secondaryOutput, 1, RANDOM.nextInt(6) + 95).get()
                                                          : null;
            return ExtractorOutput.optional(primary, secondary, secondaryChance);
        }
        return Optional.empty();
    }
}
