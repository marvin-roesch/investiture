package de.mineformers.investiture.api.multiblock;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A block 'recipe' is a builder for multi block structures utilising a syntax similar to MinecraftForge's recipe registry.
 */
public class BlockRecipe
{
    private BlockPos reference;
    private List<String[]> layers = new ArrayList<>();
    private int rowCount = -1;
    private int blockCount = -1;

    private BlockRecipe()
    {
    }

    /**
     * @return a blank block recipe
     */
    public static BlockRecipe start()
    {
        return new BlockRecipe();
    }

    /**
     * Sets the reference point for the structure, relative to the point closest to the world origin (0, 0, 0),
     * when the structure is facing north.
     *
     * @param pos the position used as a reference
     * @return this block recipe with a set reference
     */
    public final BlockRecipe reference(BlockPos pos)
    {
        this.reference = pos;
        return this;
    }

    /**
     * Adds a layer of parts to the underlying structure.
     * Note that the length of the passed array has to be the same for subsequent calls.
     * Additionally, individual rows all have to have the same length.
     * A row might look like <code>"ABC"</code> where <code>'A'</code>, <code>'B'</code> and <code>'C'</code> all refer to different blocks the
     * corresponding parts may consist of.
     *
     * @param rows the rows of blocks the layer to add consists of
     * @return this block recipe with an additional layer
     */
    public final BlockRecipe layer(String... rows)
    {
        if (rowCount != -1 && rows.length != rowCount)
            throw new RuntimeException("MultiBlockStructure layer does not have the exact number of rows!");
        rowCount = rows.length;
        for (String row : rows)
        {
            if (blockCount != -1 && row.length() != blockCount)
                throw new RuntimeException("MultiBlockStructure row does not have the exact number of blocks!");
            blockCount = row.length();
        }
        layers.add(rows);
        return this;
    }

    /**
     * Finalises the building process and 'bakes' the block recipe into a multi block structure.
     *
     * @param predicates a mapping from characters previously used in layer definitions to a
     *                   predicate corresponding to the block the part represents in the world
     * @return a baked multi block structure matching the previously defined structure
     */
    @SuppressWarnings("unchecked")
    public final MultiBlockStructure build(Map<Character, Predicate<BlockWorldState>> predicates)
    {
        Predicate<BlockWorldState>[][][] pattern = new Predicate[rowCount][layers.size()][blockCount];
        for (int l = 0; l < layers.size(); l++)
        {
            String[] rows = layers.get(l);
            for (int r = 0; r < rows.length; r++)
            {
                String row = rows[r];
                for (int block = 0; block < row.length(); block++)
                {
                    char c = row.charAt(block);
                    if (!predicates.containsKey(c))
                        throw new RuntimeException("Could not find predicate for '" + c + "' in block recipe!");
                    pattern[r][l][block] = predicates.get(c);
                }
            }
        }
        return new MultiBlockStructure(reference, pattern);
    }
}
