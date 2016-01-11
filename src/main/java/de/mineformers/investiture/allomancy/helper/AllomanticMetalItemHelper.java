package de.mineformers.investiture.allomancy.helper;

import com.google.common.base.Optional;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.item.*;
import de.mineformers.investiture.allomancy.metal.Metal;
import net.minecraft.item.ItemStack;

public class AllomanticMetalItemHelper
{

    public static Optional<ItemStack> ingot(Metal metal)
    {
        return ingot(metal, 1, 100);
    }

    public static Optional<ItemStack> ingot(Metal metal, int count, int purity)
    {
        for(int i = 0; i < MetalIngot.NAMES.length; i++) {
            if(metal.id().equals(MetalIngot.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_ingot, count);
                return Optional.of(Allomancy.Items.allomantic_ingot.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> bead(Metal metal)
    {
        return bead(metal, 1, 100);
    }

    public static Optional<ItemStack> bead(Metal metal, int count, int purity)
    {
        for(int i = 0; i < MetalBead.NAMES.length; i++) {
            if(metal.id().equals(MetalBead.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_bead, count);
                return Optional.of(Allomancy.Items.allomantic_bead.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> chunk(Metal metal)
    {
        return chunk(metal, 1, 100);
    }

    public static Optional<ItemStack> chunk(Metal metal, int count, int purity)
    {
        for(int i = 0; i < MetalChunk.NAMES.length; i++) {
            if(metal.id().equals(MetalChunk.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_chunk, count);
                return Optional.of(Allomancy.Items.allomantic_chunk.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> dust(Metal metal)
    {
        return dust(metal, 1, 100);
    }

    public static Optional<ItemStack> dust(Metal metal, int count, int purity)
    {
        for(int i = 0; i < MetalDust.NAMES.length; i++) {
            if(metal.id().equals(MetalDust.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_dust, count);
                return Optional.of(Allomancy.Items.allomantic_dust.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

    public static Optional<ItemStack> nugget(Metal metal)
    {
        return nugget(metal, 1, 100);
    }

    public static Optional<ItemStack> nugget(Metal metal, int count, int purity)
    {
        for(int i = 0; i < MetalNugget.NAMES.length; i++) {
            if(metal.id().equals(MetalNugget.NAMES[i])) {
                ItemStack stack = new ItemStack(Allomancy.Items.allomantic_nugget, count);
                return Optional.of(Allomancy.Items.allomantic_nugget.setPurity(stack, purity));
            }
        }

        return Optional.absent();
    }

}
