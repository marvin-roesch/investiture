package de.mineformers.investiture.allomancy.helper;

import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.api.metal.Metal;
import de.mineformers.investiture.allomancy.api.metal.Metals;
import de.mineformers.investiture.allomancy.block.MetalOre;
import de.mineformers.investiture.allomancy.item.MetalItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class AllomanticMetalItemHelper
{
    public static Optional<ItemStack> ore(Metal metal)
    {
        return ore(metal, 1);
    }

    public static Optional<ItemStack> ore(Metal metal, int count)
    {
        if (metal == Metals.IRON)
            return Optional.of(new ItemStack(Blocks.IRON_ORE, count));
        if (metal == Metals.GOLD)
            return Optional.of(new ItemStack(Blocks.GOLD_ORE, count));
        int dmg = Arrays.asList(MetalOre.NAMES).indexOf(metal.id());
        if (dmg != -1)
            return Optional.of(new ItemStack(Allomancy.Blocks.ORE, count, dmg));
        return Optional.empty();
    }

    public static Optional<ItemStack> ingot(Metal metal)
    {
        return ingot(metal, 1, 100);
    }

    public static Optional<ItemStack> ingot(Metal metal, int count, int purity)
    {
        return stack(MetalItem.Type.INGOT, metal, count, purity);
    }

    public static Optional<ItemStack> bead(Metal metal)
    {
        return bead(metal, 1, 100);
    }

    public static Optional<ItemStack> bead(Metal metal, int count, int purity)
    {
        return stack(MetalItem.Type.BEAD, metal, count, purity);
    }

    public static Optional<ItemStack> chunk(Metal metal)
    {
        return chunk(metal, 1, 100);
    }

    public static Optional<ItemStack> chunk(Metal metal, int count, int purity)
    {
        return stack(MetalItem.Type.CHUNK, metal, count, purity);
    }

    public static Optional<ItemStack> dust(Metal metal)
    {
        return dust(metal, 1, 100);
    }

    public static Optional<ItemStack> dust(Metal metal, int count, int purity)
    {
        return stack(MetalItem.Type.DUST, metal, count, purity);
    }

    public static Optional<ItemStack> nugget(Metal metal)
    {
        return nugget(metal, 1, 100);
    }

    public static Optional<ItemStack> nugget(Metal metal, int count, int purity)
    {
        return stack(MetalItem.Type.NUGGET, metal, count, purity);
    }

    public static Optional<ItemStack> stack(MetalItem.Type type, Metal metal, int count, int purity)
    {
        try
        {
            MetalItem item = type.getter.call();
            for (int i = 0; i < item.getNames().length; i++)
            {
                if (metal.id().equals(item.getNames()[i]))
                {
                    ItemStack stack = new ItemStack(item, count, i);
                    return Optional.of(item.setPurity(stack, purity));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}