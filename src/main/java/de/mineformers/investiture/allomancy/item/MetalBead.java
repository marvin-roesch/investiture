package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class MetalBead extends MetalItem.Abstract
{
    public static final String[] NAMES = {
        "bronze", "brass", "copper", "zinc", "tin", "pewter", "steel", "iron", "lead", "nickel", "silver", "bismuth", "gold",
        "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public MetalBead()
    {
        super("allomantic_metal_bead", "bead", NAMES);
    }

    public Type getItemType()
    {
        return Type.BEAD;
    }

}
