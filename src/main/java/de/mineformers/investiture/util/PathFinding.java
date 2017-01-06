package de.mineformers.investiture.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Provides utilities dealing with path finding problems in a voxel world.
 */
public class PathFinding
{
    /**
     * Generates a line from an entity's position according to the Bresenham rasterisation algorithm.
     *
     * @param entity      the entity
     * @param destination the destination of the entity
     * @return a list of positions forming the line pointing from the entity to the destination
     */
    public static List<BlockPos> bresenham(Entity entity, BlockPos destination)
    {
        double x = entity.posX + (entity.posX - entity.lastTickPosX) / 2;
        double y = entity.posY + (entity.posY - entity.lastTickPosY) / 2;
        double z = entity.posZ + (entity.posZ - entity.lastTickPosZ) / 2;
        BlockPos pos = new BlockPos(x, y, z);
        return bresenham(entity.world, pos, destination);
    }

    /**
     * Generates a line from one point to another according to the Bresenham rasterisation algorithm.
     * The path is effectively 2-dimensional, but due to the 3D properties of a Minecraft world, all points of the path will choose the highest
     * available block.
     *
     * @param world the world
     * @param start the line's starting position
     * @param end   the line's ending position
     * @return a list of positions forming the line pointing from the start to the end
     */
    public static List<BlockPos> bresenham(World world, BlockPos start, BlockPos end)
    {
        // See https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
        ImmutableList.Builder<BlockPos> result = ImmutableList.builder();
        int x0 = start.getX();
        int y0 = start.getZ();
        int x1 = end.getX();
        int y1 = end.getZ();
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2;

        while (true)
        {
            result.add(world.getTopSolidOrLiquidBlock(new BlockPos(x0, start.getY(), y0)));
            if (x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if (e2 > dy)
            {
                err += dy;
                x0 += sx;
            }
            if (e2 < dx)
            {
                err += dx;
                y0 += sy;
            }
        }

        return result.build();
    }
}
