package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class AllomanticMetalIngot extends AllomanticMetalItem
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "lead", "nickel", "silver", "bismuth",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public AllomanticMetalIngot()
    {
        super("allomantic_metal_ingot", "ingot", NAMES);
    }

}
