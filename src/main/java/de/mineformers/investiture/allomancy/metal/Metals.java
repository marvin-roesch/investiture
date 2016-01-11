package de.mineformers.investiture.allomancy.metal;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import de.mineformers.investiture.allomancy.item.MetalItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides access to all allomantic metals, especially the basic 16 ones provided by the Allomancy module itself.
 */
public final class Metals
{
    private static final Set<Metal> METALS = new HashSet<>();
    private static final Set<MetalAlloy> ALLOYS = new HashSet<>();
    // Base metals
    public static final Metal COPPER = new ItemMetalBurnable("copper");
    public static final Metal ZINC = new ItemMetalNonBurnable("zinc");
    public static final Metal TIN = new ItemMetalBurnable("tin");
    public static final Metal IRON = new ItemMetalBurnable("iron");
    public static final Metal ALUMINIUM = new ItemMetalBurnable("aluminium");
    public static final Metal CHROMIUM = new ItemMetalNonBurnable("chromium");
    public static final Metal GOLD = new ItemMetalBurnable("gold");
    public static final Metal CADMIUM = new ItemMetalBurnable("cadmium");
    public static final Metal LEAD = new ItemMetalNonBurnable("lead");
    public static final Metal BISMUTH = new ItemMetalNonBurnable("bismuth");
    public static final Metal SILVER = new ItemMetalNonBurnable("silver");
    public static final Metal NICKEL = new ItemMetalNonBurnable("nickel");
    public static final Metal CARBON = new ItemMetalNonBurnable("carbon");
    // MetalAlloy metals
    public static final MetalAlloy BRONZE = new ItemAlloyBurnable("bronze", COPPER, 0.75F, TIN, 0.25F);
    public static final MetalAlloy BRASS = new ItemAlloyBurnable("brass", COPPER, 0.65F, ZINC, 0.35F);
    public static final MetalAlloy PEWTER = new ItemAlloyBurnable("pewter", TIN, 0.91F, LEAD, 0.09F);
    public static final MetalAlloy STEEL = new ItemAlloyBurnable("steel", IRON, 0.98F, CARBON, 0.02F);
    public static final MetalAlloy DURALUMIN = new ItemAlloyBurnable("duralumin", ALUMINIUM, 0.96F, COPPER, 0.04F);
    public static final MetalAlloy NICROSIL = new ItemAlloyBurnable("nicrosil", NICKEL, 0.86F, CHROMIUM, 0.14F);
    public static final MetalAlloy ELECTRUM = new ItemAlloyBurnable("electrum", GOLD, 0.45F, SILVER, 0.55F);
    public static final MetalAlloy BENDALLOY = new ItemAlloyBurnable("bendalloy", BISMUTH, 0.5F, LEAD, 0.27F, TIN, 0.13F, CADMIUM, 0.1F);
    // Mappings for non mod metals
    private static final Set<MetalMapping> MAPPINGS = new HashSet<>();

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

        ALLOYS.add(BRONZE);
        ALLOYS.add(BRASS);
        ALLOYS.add(PEWTER);
        ALLOYS.add(STEEL);
        ALLOYS.add(DURALUMIN);
        ALLOYS.add(NICROSIL);
        ALLOYS.add(ELECTRUM);
        ALLOYS.add(BENDALLOY);

        MAPPINGS.add(new MetalMapping.MetalMappingItem(
                IRON, new ItemStack(Items.iron_ingot), MetalItem.Abstract.Type.INGOT.conversion));
        MAPPINGS.add(new MetalMapping.MetalMappingItem(
                GOLD, new ItemStack(Items.gold_ingot), MetalItem.Abstract.Type.INGOT.conversion));
        MAPPINGS.add(new MetalMapping.MetalMappingItem(
                GOLD, new ItemStack(Items.gold_nugget), MetalItem.Abstract.Type.NUGGET.conversion));
        MAPPINGS.add(new MetalMapping.MetalMappingOreDict(
                IRON, "ingotIron", MetalItem.Abstract.Type.INGOT.conversion, true));
        MAPPINGS.add(new MetalMapping.MetalMappingOreDict(
                GOLD, "ingotGold", MetalItem.Abstract.Type.INGOT.conversion, true));
    }

    /**
     * @param id the ID of the searched metal
     * @return a present {@link Optional} if the metal exists, {@link Optional#absent()} otherwise
     */
    public static Optional<Metal> get(String id)
    {
        return FluentIterable.from(METALS).firstMatch(m -> m.id().equals(id));
    }

    /**
     * @return an unmodifiable view of all allomantic metals
     */
    public static Set<Metal> metals()
    {
        return Collections.unmodifiableSet(METALS);
    }

    public static Set<MetalAlloy> alloys()
    {
        return Collections.unmodifiableSet(ALLOYS);
    }

    public static Set<MetalMapping> mappings()
    {
        return Collections.unmodifiableSet(MAPPINGS);
    }

    public static Optional<Metal> getMetal(@Nonnull ItemStack stack)
    {
        if(stack.getItem() instanceof MetalItem) {
            return Optional.of(((MetalItem) stack.getItem()).getMetal(stack));
        } else {
            for(MetalMapping mapping : MAPPINGS) {
                if(mapping.matches(stack)) {
                    return Optional.of(mapping.getMetal(stack));
                }
            }
        }

        return Optional.absent();
    }

    /**
     * Representation of burnable metal
     */
    private final static class ItemMetalBurnable extends Metal.AbstractMetal
    {
        ItemMetalBurnable(@Nonnull String id)
        {
            super(id);
        }
    }

    /**
     * Representation of non burnable metal
     */
    private final static class ItemMetalNonBurnable extends Metal.AbstractMetal
    {
        ItemMetalNonBurnable(@Nonnull String id)
        {
            super(id);
        }

        public boolean canBurn()
        {
            return false;
        }
    }

    /**
     * Representation of burnable alloy
     */
    private final static class ItemAlloyBurnable extends MetalAlloy.AbstractAlloy
    {
        ItemAlloyBurnable(@Nonnull String id, Metal alloy, Object... components)
        {
            super(id, alloy, components);
        }
    }

}
