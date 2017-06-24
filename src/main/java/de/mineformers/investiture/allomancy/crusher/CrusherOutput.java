package de.mineformers.investiture.allomancy.crusher;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * ${JDOC}
 */
public final class CrusherOutput
{
    private final ItemStack primary;
    @Nullable
    private final ItemStack secondary;
    private final float secondaryChance;

    @Nonnull
    public static Optional<CrusherOutput> optional(ItemStack result)
    {
        return optional(result, new ItemStack(Blocks.COBBLESTONE));
    }

    @Nonnull
    public static Optional<CrusherOutput> optional(ItemStack primaryResult, @Nullable ItemStack secondaryResult)
    {
        return optional(primaryResult, secondaryResult, 1);
    }

    @Nonnull
    public static Optional<CrusherOutput> optional(ItemStack primaryResult, @Nullable ItemStack secondaryResult, float secondaryChance)
    {
        return Optional.of(new CrusherOutput(primaryResult, secondaryResult, secondaryChance));
    }

    public CrusherOutput(ItemStack primaryResult, @Nullable ItemStack secondaryResult, float secondaryChance)
    {
        this.primary = primaryResult;
        this.secondary = secondaryResult;
        this.secondaryChance = secondaryChance;
    }

    public ItemStack getPrimaryResult()
    {
        return primary;
    }

    @Nullable
    public ItemStack getSecondaryResult()
    {
        return secondary;
    }

    public float getSecondaryChance()
    {
        return secondaryChance;
    }
}
