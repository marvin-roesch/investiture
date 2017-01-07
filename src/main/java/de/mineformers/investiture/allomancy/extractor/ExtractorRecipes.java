package de.mineformers.investiture.allomancy.extractor;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.helper.MetalStacks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ${JDOC}
 */
public class ExtractorRecipes
{
    private static final List<ExtractorRecipe> recipes = new ArrayList<>();

    public static void register(ItemStack input, ItemStack result)
    {
        register(input, result, null);
    }

    public static void register(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult)
    {
        register(input, primaryResult, secondaryResult, 1);
    }

    public static void register(ItemStack input, ItemStack primaryResult, ItemStack secondaryResult, float secondaryChance)
    {
        register(input, new ExtractorOutput(primaryResult, secondaryResult, secondaryChance));
    }

    public static void register(ItemStack input, ExtractorOutput result)
    {
        register(new SimpleExtractorRecipe(input, result));
    }

    public static void register(Metal input)
    {
        register(input, input);
    }

    public static void register(Metal input, Metal result)
    {
        register(MetalStacks.ore(input).get(), result);
    }

    public static void register(Metal input, Metal primaryResult, Metal secondaryResult)
    {
        register(MetalStacks.ore(input).get(), primaryResult, secondaryResult);
    }

    public static void register(Metal input, Metal primaryResult, Metal secondaryResult, float secondaryChance)
    {
        register(MetalStacks.ore(input).get(), primaryResult, secondaryResult, secondaryChance);
    }

    public static void register(ItemStack input, Metal result)
    {
        register(input, result, null);
    }

    public static void register(ItemStack input, Metal primaryResult, Metal secondaryResult)
    {
        register(input, primaryResult, secondaryResult, 1);
    }

    public static void register(ItemStack input, Metal primaryResult, Metal secondaryResult, float secondaryChance)
    {
        register(new MetalExtractorRecipe(input, primaryResult, secondaryResult, secondaryChance));
    }

    public static void register(ExtractorRecipe recipe)
    {
        recipes.add(recipe);
    }

    public static List<ExtractorRecipe> recipes()
    {
        return Collections.unmodifiableList(recipes);
    }
}
