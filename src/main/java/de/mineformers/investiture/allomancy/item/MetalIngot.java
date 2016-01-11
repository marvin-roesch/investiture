package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class MetalIngot extends MetalItem.Abstract
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "lead", "nickel", "silver", "bismuth",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public MetalIngot()
    {
        super("allomantic_metal_ingot", "ingot", NAMES);
    }

    public Type getItemType()
    {
        return Type.INGOT;
    }

}
