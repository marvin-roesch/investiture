package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class AllomanticMetalChunk extends AllomanticMetalItem.Abstract
{
    public static final String[] NAMES = {
        "copper", "tin", "zinc", "iron", "lead", "aluminium", "chromium", "gold", "cadmium", "silver",
        "bismuth", "lead"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public AllomanticMetalChunk()
    {
        super("allomantic_metal_chunk", "chunk", NAMES);
    }

}
