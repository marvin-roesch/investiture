package de.mineformers.investiture.allomancy.item;

/**
 * Used as the ingot for all allomantic metals that are not provided by Vanilla Minecaft.
 */
public class MetalChunk extends MetalItem.Abstract
{
    public static final String[] NAMES = {
        "copper", "tin", "zinc", "iron", "lead", "aluminium", "chromium", "gold", "cadmium", "silver",
        "bismuth"
    };

    /**
     * Creates a new instance of the ingot.
     */
    public MetalChunk()
    {
        super("allomantic_metal_chunk", "chunk", NAMES);
    }

    public Type getItemType()
    {
        return Type.CHUNK;
    }

}
