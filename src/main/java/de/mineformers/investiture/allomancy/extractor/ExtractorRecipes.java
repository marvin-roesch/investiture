package de.mineformers.investiture.allomancy.extractor;

import net.minecraft.init.Blocks;
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
        register(input, result, new ItemStack(Blocks.cobblestone));
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

    public static void register(ExtractorRecipe recipe)
    {
        recipes.add(recipe);
    }

    public static List<ExtractorRecipe> recipes()
    {
        return Collections.unmodifiableList(recipes);
    }
}
