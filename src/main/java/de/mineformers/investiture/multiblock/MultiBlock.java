package de.mineformers.investiture.multiblock;

import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides detection and handling for multi block structures.
 */
@ParametersAreNonnullByDefault
public class MultiBlock
{
    public static Builder builder()
    {
        return new Builder();
    }

    private Predicate<BlockWorldState>[][][] pattern;

    MultiBlock(Predicate<BlockWorldState>[][][] pattern)
    {
        this.pattern = pattern;
    }

    public boolean validate(World world, BlockPos corner, Consumer<MultiBlockPart> operation)
    {
        return validateImpl(world, corner, EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.EAST, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.WEST, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH, operation) ||
            validateImpl(world, corner, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.WEST, operation);
    }

    private boolean validateImpl(World world, BlockPos corner, EnumFacing direction, EnumFacing horizontal, EnumFacing vertical,
                                 Consumer<MultiBlockPart> operation)
    {
        int id = 0;
        for (int depth = 0; depth < pattern.length; depth++)
        {
            Predicate<BlockWorldState>[][] layer = pattern[depth];
            for (int height = 0; height < layer.length; height++)
            {
                Predicate<BlockWorldState>[] row = layer[height];
                for (int width = 0; width < row.length; width++)
                {
                    BlockPos childPos = corner.offset(horizontal, width).offset(vertical, height).offset(direction, depth);
                    if (!row[width].test(new BlockWorldState(world, childPos, true)))
                        return false;

                    operation.accept(new MultiBlockPart(world, childPos, true, id));
                    id++;
                }
            }
        }
        return true;
    }

    public static final class Builder
    {
        private List<Predicate<BlockWorldState>[][]> layers = new ArrayList<>();
        private int rowCount = -1;
        private int blockCount = -1;

        private Builder()
        {
        }

        @SafeVarargs
        public final Builder layer(Predicate<BlockWorldState>[]... rows)
        {
            if (rowCount != -1 && rows.length != rowCount)
                throw new RuntimeException("MultiBlock layer does not have the exact number of rows!");
            rowCount = rows.length;
            for (Predicate<BlockWorldState>[] row : rows)
            {
                if (blockCount != -1 && row.length != blockCount)
                    throw new RuntimeException("MultiBlock row does not have the exact number of blocks!");
                blockCount = row.length;
            }
            layers.add(rows);
            return this;
        }

        @SuppressWarnings("unchecked")
        public final MultiBlock build()
        {
            return new MultiBlock(layers.toArray(new Predicate[layers.size()][rowCount][blockCount]));
        }
    }
}
