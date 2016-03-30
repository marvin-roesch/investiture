package de.mineformers.investiture.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * ${JDOC}
 */
public class PathFinding
{
    public static List<BlockPos> bresenham(Entity entity, BlockPos destination)
    {
        double x = entity.posX + (entity.posX - entity.lastTickPosX) / 2;
        double y = entity.posY + (entity.posY - entity.lastTickPosY) / 2;
        double z = entity.posZ + (entity.posZ - entity.lastTickPosZ) / 2;
        BlockPos pos = new BlockPos(x, y, z);
        return bresenham(entity.worldObj, pos, destination);
    }

    public static List<BlockPos> bresenham(World world, BlockPos start, BlockPos end)
    {
        ImmutableList.Builder<BlockPos> result = ImmutableList.builder();
        int x0 = start.getX();
        int y0 = start.getZ();
        int x1 = end.getX();
        int y1 = end.getZ();
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2; /* error value e_xy */

        while (true)
        {
            result.add(world.getTopSolidOrLiquidBlock(new BlockPos(x0, start.getY(), y0)));
            if (x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if (e2 > dy)
            {
                err += dy;
                x0 += sx;
            } /* e_xy+e_x > 0 */
            if (e2 < dx)
            {
                err += dx;
                y0 += sy;
            } /* e_xy+e_y < 0 */
        }

        return result.build();
    }
}
