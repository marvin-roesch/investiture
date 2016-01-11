package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class MetalNugget extends MetalItem.Abstract
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public MetalNugget()
    {
        super("allomantic_metal_nugget", "nugget", NAMES);
    }

    public Type getItemType()
    {
        return Type.NUGGET;
    }

}
