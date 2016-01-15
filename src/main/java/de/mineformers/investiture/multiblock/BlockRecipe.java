package de.mineformers.investiture.multiblock;

import net.minecraft.block.state.BlockWorldState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * ${JDOC}
 */
public class BlockRecipe
{
    private List<String[]> layers = new ArrayList<>();
    private int rowCount = -1;
    private int blockCount = -1;

    private BlockRecipe()
    {
    }

    public static BlockRecipe start()
    {
        return new BlockRecipe();
    }

    public final BlockRecipe layer(String... rows)
    {
        if (rowCount != -1 && rows.length != rowCount)
            throw new RuntimeException("MultiBlock layer does not have the exact number of rows!");
        rowCount = rows.length;
        for (String row : rows)
        {
            if (blockCount != -1 && row.length() != blockCount)
                throw new RuntimeException("MultiBlock row does not have the exact number of blocks!");
            blockCount = row.length();
        }
        layers.add(rows);
        return this;
    }

    @SuppressWarnings("unchecked")
    public final MultiBlock build(Map<Character, Predicate<BlockWorldState>> predicates)
    {
        Predicate<BlockWorldState>[][][] pattern = new Predicate[layers.size()][rowCount][blockCount];
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
                    pattern[l][r][block] = predicates.get(c);
                }
            }
        }
        return new MultiBlock(pattern);
    }
}
