package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class AllomanticMetalNugget extends AllomanticMetalItem.Abstract
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "cadmium",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public AllomanticMetalNugget()
    {
        super("allomantic_metal_nugget", "nugget", NAMES);
    }

}
