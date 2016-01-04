package de.mineformers.allomancy.metal;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;

import static de.mineformers.allomancy.Allomancy.Items.allomantic_ingot;

/**
 * AllomanticMetals
 *
 * @author PaleoCrafter
 */
public final class AllomanticMetals {
    private static final Set<AllomanticMetal> METALS = new HashSet<>();
    public static final AllomanticMetal BRONZE = new SelectiveItemMetal("bronze");
    public static final AllomanticMetal BRASS = new SelectiveItemMetal("brass");
    public static final AllomanticMetal COPPER = new SelectiveItemMetal("copper");
    public static final AllomanticMetal ZINC = new SelectiveItemMetal("zinc");
    public static final AllomanticMetal TIN = new SelectiveItemMetal("tin");
    public static final AllomanticMetal IRON = new VanillaItemMetal("iron", Items.iron_ingot);
    public static final AllomanticMetal PEWTER = new SelectiveItemMetal("pewter");
    public static final AllomanticMetal STEEL = new SelectiveItemMetal("steel");
    public static final AllomanticMetal DURALUMIN = new SelectiveItemMetal("duralumin");
    public static final AllomanticMetal NICROSIL = new SelectiveItemMetal("nicrosil");
    public static final AllomanticMetal ALUMINIUM = new SelectiveItemMetal("aluminium");
    public static final AllomanticMetal CHROMIUM = new SelectiveItemMetal("chromium");
    public static final AllomanticMetal GOLD = new VanillaItemMetal("gold", Items.gold_ingot);
    public static final AllomanticMetal CADMIUM = new SelectiveItemMetal("cadmium");
    public static final AllomanticMetal ELECTRUM = new SelectiveItemMetal("electrum");
    public static final AllomanticMetal BENDALLOY = new SelectiveItemMetal("bendalloy");

    public static void init() {
        METALS.add(BRONZE);
        METALS.add(BRASS);
        METALS.add(COPPER);
        METALS.add(ZINC);
        METALS.add(TIN);
        METALS.add(IRON);
        METALS.add(PEWTER);
        METALS.add(STEEL);
        METALS.add(DURALUMIN);
        METALS.add(NICROSIL);
        METALS.add(ALUMINIUM);
        METALS.add(CHROMIUM);
        METALS.add(GOLD);
        METALS.add(CADMIUM);
        METALS.add(ELECTRUM);
        METALS.add(BENDALLOY);
    }

    public static Optional<AllomanticMetal> get(String id) {
        return FluentIterable.from(METALS).firstMatch(m -> m.id().equals(id));
    }

    public static Set<AllomanticMetal> metals() {
        return Collections.unmodifiableSet(METALS);
    }

    private final static class VanillaItemMetal extends AllomanticMetal.AbstractAllomanticMetal {
        private final Item item;

        VanillaItemMetal(@Nonnull String id, @Nonnull Item item) {
            super(id);
            this.item = item;
        }

        @Override
        public boolean canBurn(@Nonnull ItemStack stack) {
            return stack.getItem() == item;
        }
    }

    private final static class SelectiveItemMetal extends AllomanticMetal.AbstractAllomanticMetal {
        SelectiveItemMetal(@Nonnull String id) {
            super(id);
        }

        @Override
        public boolean canBurn(@Nonnull ItemStack stack) {
            return getValue(stack) > 0 && allomantic_ingot.getPurity(stack) >= 100;
        }

        @Override
        public int getValue(@Nonnull ItemStack stack) {
            if (stack.getItem() == allomantic_ingot && allomantic_ingot.getName(stack).equals(id()))
                return stack.stackSize;
            else
                return 0;
        }
    }
}
