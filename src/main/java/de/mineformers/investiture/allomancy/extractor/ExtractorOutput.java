package de.mineformers.investiture.allomancy.extractor;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * ${JDOC}
 */
public final class ExtractorOutput
{
    private final ItemStack primary, secondary;
    private final float secondaryChance;

    @Nonnull
    public static Optional<ExtractorOutput> optional(ItemStack result)
    {
        return optional(result, new ItemStack(Blocks.cobblestone));
    }

    @Nonnull
    public static Optional<ExtractorOutput> optional(ItemStack primaryResult, ItemStack secondaryResult)
    {
        return optional(primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static Optional<ExtractorOutput> optional(ItemStack primaryResult, ItemStack secondaryResult, float secondaryChance)
    {
        return Optional.of(new ExtractorOutput(primaryResult, secondaryResult, secondaryChance));
    }

    public ExtractorOutput(ItemStack primaryResult, ItemStack secondaryResult, float secondaryChance)
    {
        this.primary = primaryResult;
        this.secondary = secondaryResult;
        this.secondaryChance = secondaryChance;
    }

    public ItemStack getPrimaryResult()
    {
        return primary;
    }

    public ItemStack getSecondaryResult()
    {
        return secondary;
    }

    public float getSecondaryChance()
    {
        return secondaryChance;
    }
}
