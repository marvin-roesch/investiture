package de.mineformers.investiture.allomancy.helper;

import com.google.common.base.Optional;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.item.*;
import de.mineformers.investiture.allomancy.metal.AllomanticMetal;
import net.minecraft.item.ItemStack;

public class AllomanticMetalItemHelper
{

    public static Optional<ItemStack> ingot(AllomanticMetal metal)
    {
        return ingot(metal, 1, 100);
    }

    public static Optional<ItemStack> ingot(AllomanticMetal metal, int count, int purity)
    {
        for(int i = 0; i < AllomanticMetalIngot.NAMES.length; i++) {
            if(metal.id().equals(AllomanticMetalIngot.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_ingot, count);
                return Optional.of(Allomancy.Items.allomantic_ingot.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> bead(AllomanticMetal metal)
    {
        return bead(metal, 1, 100);
    }

    public static Optional<ItemStack> bead(AllomanticMetal metal, int count, int purity)
    {
        for(int i = 0; i < AllomanticMetalBead.NAMES.length; i++) {
            if(metal.id().equals(AllomanticMetalBead.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_bead, count);
                return Optional.of(Allomancy.Items.allomantic_bead.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> chunk(AllomanticMetal metal)
    {
        return chunk(metal, 1, 100);
    }

    public static Optional<ItemStack> chunk(AllomanticMetal metal, int count, int purity)
    {
        for(int i = 0; i < AllomanticMetalChunk.NAMES.length; i++) {
            if(metal.id().equals(AllomanticMetalChunk.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_chunk, count);
                return Optional.of(Allomancy.Items.allomantic_chunk.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> dust(AllomanticMetal metal)
    {
        return dust(metal, 1, 100);
    }

    public static Optional<ItemStack> dust(AllomanticMetal metal, int count, int purity)
    {
        for(int i = 0; i < AllomanticMetalDust.NAMES.length; i++) {
            if(metal.id().equals(AllomanticMetalDust.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_dust, count);
                return Optional.of(Allomancy.Items.allomantic_dust.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> nugget(AllomanticMetal metal)
    {
        return nugget(metal, 1, 100);
    }

    public static Optional<ItemStack> nugget(AllomanticMetal metal, int count, int purity)
    {
        for(int i = 0; i < AllomanticMetalNugget.NAMES.length; i++) {
            if(metal.id().equals(AllomanticMetalNugget.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_nugget, count);
                return Optional.of(Allomancy.Items.allomantic_nugget.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

}
