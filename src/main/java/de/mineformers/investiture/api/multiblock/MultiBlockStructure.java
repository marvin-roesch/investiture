package de.mineformers.investiture.api.multiblock;

import com.google.common.collect.ImmutableList;
import de.mineformers.investiture.util.Vectors;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
public class MultiBlockStructure
{
    /**
     * @return an empty builder for a multi block structure
     */
    public static Builder builder()
    {
        return new Builder();
    }

    private BlockPos reference;
    private Predicate<BlockWorldState>[][][] pattern;

    MultiBlockStructure(BlockPos reference, Predicate<BlockWorldState>[][][] pattern)
    {
        this.reference = reference;
        this.pattern = pattern;
    }

    /**
     * Tries to match the pattern of the multi block structure against the actual world.
     *
     * @param world     the world the structure is in
     * @param reference the in-world position of the reference block
     * @param operation an operation to apply to all parts of the structure that definitely match the pattern
     * @return true if the whole pattern was matched successfully, false otherwise
     */
    public boolean validate(World world, BlockPos reference, EnumFacing direction, Consumer<MultiBlockPart> operation)
    {
        Rotation rotation = Vectors.getRotation(direction);
        EnumFacing horizontal = rotation.rotate(EnumFacing.EAST);
        Vec3d centre = getBounds(Rotation.NONE).getCenter();
        Vec3d rotatedCentre = getBounds(rotation).getCenter();
        Vec3d vec = new Vec3d(this.reference).subtract(centre);
        BlockPos rotated = new BlockPos(Vectors.rotateY(vec, rotation).add(rotatedCentre));
        return validateImpl(world, reference.subtract(rotated), direction, horizontal, EnumFacing.UP, operation);
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

    public List<BlockPos> getPositions(BlockPos origin, EnumFacing orientation)
    {
        ImmutableList.Builder<BlockPos> positions = new ImmutableList.Builder<>();
        Rotation rotation = Vectors.getRotation(orientation);
        EnumFacing horizontal = rotation.rotate(EnumFacing.EAST);
        for (int depth = 0; depth < pattern.length; depth++)
        {
            Predicate<BlockWorldState>[][] layer = pattern[depth];
            for (int height = 0; height < layer.length; height++)
            {
                Predicate<BlockWorldState>[] row = layer[height];
                for (int width = 0; width < row.length; width++)
                {
                    positions.add(origin.offset(horizontal, width).offset(EnumFacing.UP, height).offset(orientation, depth));
                }
            }
        }
        return positions.build();
    }

    public BlockPos getLocalPosition(int part, EnumFacing orientation)
    {
        Rotation rotation = Vectors.getRotation(orientation);
        EnumFacing horizontal = rotation.rotate(EnumFacing.EAST);
        int id = 0;
        for (int depth = 0; depth < pattern.length; depth++)
        {
            Predicate<BlockWorldState>[][] layer = pattern[depth];
            for (int height = 0; height < layer.length; height++)
            {
                Predicate<BlockWorldState>[] row = layer[height];
                for (int width = 0; width < row.length; width++)
                {
                    if (id == part)
                    {
                        return BlockPos.ORIGIN.offset(horizontal, width).offset(EnumFacing.UP, height).offset(orientation, depth);
                    }
                    id++;
                }
            }
        }
        return BlockPos.ORIGIN;
    }

    public AxisAlignedBB getBounds(Rotation rotation)
    {
        AxisAlignedBB base = new AxisAlignedBB(0, 0, 0, pattern[0][0].length, pattern[0].length, pattern.length);
        Vec3d baseCentre = base.getCenter();
        Vec3d rotatedCentre = Vectors.rotateY(baseCentre, rotation);
        return Vectors.rotateY(base.offset(baseCentre.x, baseCentre.y, baseCentre.z), rotation)
                      .offset(rotatedCentre.x, rotatedCentre.y, rotatedCentre.z);
    }

    /**
     * Provides a builder for multi block structures.
     */
    public static final class Builder
    {
        private BlockPos reference = BlockPos.ORIGIN;
        private List<Predicate<BlockWorldState>[][]> layers = new ArrayList<>();
        private int rowCount = -1;
        private int blockCount = -1;

        private Builder()
        {
        }

        /**
         * Sets the reference point for the structure, relative to the point closest to the world origin (0, 0, 0),
         * when the structure is facing north.
         *
         * @param pos the position used as a reference
         * @return this builder with a set reference
         */
        public final Builder reference(BlockPos pos)
        {
            this.reference = pos;
            return this;
        }

        /**
         * Adds a layer of parts to the underlying structure.
         * Note that the length of the passed array has to be the same for subsequent calls.
         * Additionally, individual rows all have to have the same length.
         *
         * @param rows the rows of blocks the layer to add consists of
         * @return this builder with an additional layer
         */
        @SafeVarargs
        public final Builder layer(Predicate<BlockWorldState>[]... rows)
        {
            if (rowCount != -1 && rows.length != rowCount)
                throw new RuntimeException("MultiBlockStructure layer does not have the exact number of rows!");
            rowCount = rows.length;
            for (Predicate<BlockWorldState>[] row : rows)
            {
                if (blockCount != -1 && row.length != blockCount)
                    throw new RuntimeException("MultiBlockStructure row does not have the exact number of blocks!");
                blockCount = row.length;
            }
            layers.add(rows);
            return this;
        }

        /**
         * Finalises the building process.
         *
         * @return a multi block structure matching the previously defined structure
         */
        @SuppressWarnings("unchecked")
        public final MultiBlockStructure build()
        {
            return new MultiBlockStructure(reference, layers.toArray(new Predicate[layers.size()][rowCount][blockCount]));
        }
    }
}
