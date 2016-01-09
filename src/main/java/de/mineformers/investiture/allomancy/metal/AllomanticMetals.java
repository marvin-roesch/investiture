package de.mineformers.investiture.allomancy.metal;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static de.mineformers.investiture.allomancy.Allomancy.Items.allomantic_ingot;

/**
 * Provides access to all allomantic metals, especially the basic 16 ones provided by the Allomancy module itself.
 */
public final class AllomanticMetals
{
    private static final Set<AllomanticMetal> METALS = new HashSet<>();
    // Base metals
    public static final AllomanticMetal COPPER = new SelectiveItemMetal("copper");
    public static final AllomanticMetal ZINC = new SelectiveItemMetal("zinc");
    public static final AllomanticMetal TIN = new SelectiveItemMetal("tin");
    public static final AllomanticMetal IRON = new VanillaItemMetal("iron", Items.iron_ingot);
    public static final AllomanticMetal ALUMINIUM = new SelectiveItemMetal("aluminium");
    public static final AllomanticMetal CHROMIUM = new SelectiveItemMetal("chromium");
    public static final AllomanticMetal GOLD = new VanillaItemMetal("gold", Items.gold_ingot);
    public static final AllomanticMetal CADMIUM = new SelectiveItemMetal("cadmium");
    public static final AllomanticMetal LEAD = new SelectiveItemMetal("lead");
    public static final AllomanticMetal BISMUTH = new SelectiveItemMetal("bismuth");
    public static final AllomanticMetal SILVER = new SelectiveItemMetal("silver");
    public static final AllomanticMetal NICKEL = new SelectiveItemMetal("nickel");
    public static final AllomanticMetal CARBON = new VanillaItemMetal("CARBON", Items.coal); // not a metal
    // Alloy metals
    public static final AllomanticAlloy BRONZE = new AlloyItemMetal("bronze", COPPER, 0.75F, TIN, 0.25F);
    public static final AllomanticAlloy BRASS = new AlloyItemMetal("brass", COPPER, 0.65F, ZINC, 0.35F);
    public static final AllomanticAlloy PEWTER = new AlloyItemMetal("pewter", TIN, 0.91F, LEAD, 0.09F);
    public static final AllomanticAlloy STEEL = new AlloyItemMetal("steel", IRON, 0.98F, CARBON, 0.02F);
    public static final AllomanticAlloy DURALUMIN = new AlloyItemMetal("duralumin", ALUMINIUM, 0.96F, COPPER, 0.04F);
    public static final AllomanticAlloy NICROSIL = new AlloyItemMetal("nicrosil", NICKEL, 0.86F, CHROMIUM, 0.14F);
    public static final AllomanticAlloy ELECTRUM = new AlloyItemMetal("electrum", GOLD, 0.45F, SILVER, 0.55F);
    public static final AllomanticAlloy BENDALLOY = new AlloyItemMetal("bendalloy", BISMUTH, 0.5F, LEAD, 0.27F, TIN, 0.13F, CADMIUM, 0.1F);

    /**
     * Register all 16 basic metals
     */
    public static void init()
    {
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
        METALS.add(LEAD);
        METALS.add(BISMUTH);
        METALS.add(SILVER);
        METALS.add(NICKEL);
    }

    /**
     * @param id the ID of the searched metal
     * @return a present {@link Optional} if the metal exists, {@link Optional#absent()} otherwise
     */
    public static Optional<AllomanticMetal> get(String id)
    {
        return FluentIterable.from(METALS).firstMatch(m -> m.id().equals(id));
    }

    /**
     * @return an unmodifiable view of all allomantic metals
     */
    public static Set<AllomanticMetal> metals()
    {
        return Collections.unmodifiableSet(METALS);
    }

    /**
     * Simple representation of vanilla metals
     */
    private final static class VanillaItemMetal extends AllomanticMetal.Abstract
    {
        private final Item item;

        VanillaItemMetal(@Nonnull String id, @Nonnull Item item)
        {
            super(id);
            this.item = item;
        }

        @Override
        public boolean canBurn(@Nonnull ItemStack stack)
        {
            return stack.getItem() == item;
        }
    }

    /**
     * Simple representation of basic allomantic metals
     */
    private final static class SelectiveItemMetal extends AllomanticMetal.Abstract implements MetalEffects
    {
        SelectiveItemMetal(@Nonnull String id)
        {
            super(id);
        }

        @Override
        public void startBurning(MetalBurner burner)
        {
        }

        @Override
        public void stopBurning(MetalBurner burner)
        {
        }

        @Override
        public boolean canBurn(@Nonnull ItemStack stack)
        {
            // Only pure metals are burnable
            return getValue(stack) > 0 && allomantic_ingot.getPurity(stack) >= 100;
        }

        @Override
        public int getValue(@Nonnull ItemStack stack)
        {
            if (stack.getItem() == allomantic_ingot && allomantic_ingot.getName(stack).equals(id()))
                return stack.stackSize;
            else
                return 0;
        }
    }

    /**
     * Simple representation of alloy allomantic metals
     */
    private final static class AlloyItemMetal extends AllomanticAlloy.AbstractAlloy implements MetalEffects
    {

        AlloyItemMetal(@Nonnull String id, AllomanticMetal alloy, Object... components)
        {
            super(id, alloy, components);
        }

        @Override
        public void startBurning(MetalBurner burner)
        {
        }

        @Override
        public void stopBurning(MetalBurner burner)
        {
        }

        @Override
        public boolean canBurn(@Nonnull ItemStack stack)
        {
            // Only pure metals are burnable
            return getValue(stack) > 0 && allomantic_ingot.getPurity(stack) >= 100;
        }

        @Override
        public int getValue(@Nonnull ItemStack stack)
        {
            if (stack.getItem() == allomantic_ingot && allomantic_ingot.getName(stack).equals(id()))
                return stack.stackSize;
            else
                return 0;
        }

    }
}
