package de.mineformers.investiture.allomancy.crusher;

import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.helper.MetalStacks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ${JDOC}
 */
public class CrusherRecipes
{
    private static final List<CrusherRecipe> recipes = new ArrayList<>();

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
        register(input, new CrusherOutput(primaryResult, secondaryResult, secondaryChance));
    }

    public static void register(ItemStack input, CrusherOutput result)
    {
        register(new SimpleCrusherRecipe(input, result));
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
        register(new MetalCrusherRecipe(input, primaryResult, secondaryResult, secondaryChance));
    }

    public static void register(CrusherRecipe recipe)
    {
        recipes.add(recipe);
    }

    public static List<CrusherRecipe> recipes()
    {
        return Collections.unmodifiableList(recipes);
    }
}
