package de.mineformers.investiture.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.function.Predicate;

/**
 * ${JDOC}
 */
public class RayTracer
{
    public static MovingObjectPosition rayTraceBlocks(Entity entity, double reach,
                                                      Predicate<BlockWorldState> predicate,
                                                      boolean stopOnLiquid,
                                                      boolean ignoreBlockWithoutBoundingBox,
                                                      boolean returnLastUncollidableBlock)
    {
        Vec3 start = entity.getPositionVector().addVector(0, entity.getEyeHeight(), 0);
        Vec3 look = entity.getLook(1);
        Vec3 end = start.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        return rayTraceBlocks(entity.worldObj, start, end, predicate, stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLastUncollidableBlock);
    }

    public static MovingObjectPosition rayTraceBlocks(World world, Vec3 start, Vec3 end,
                                                      Predicate<BlockWorldState> predicate,
                                                      boolean stopOnLiquid,
                                                      boolean ignoreBlockWithoutBoundingBox,
                                                      boolean returnLastUncollidableBlock)
    {
        if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord))
        {
            if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord))
            {
                int startX = MathHelper.floor_double(start.xCoord);
                int startY = MathHelper.floor_double(start.yCoord);
                int startZ = MathHelper.floor_double(start.zCoord);
                int endX = MathHelper.floor_double(end.xCoord);
                int endY = MathHelper.floor_double(end.yCoord);
                int endZ = MathHelper.floor_double(end.zCoord);
                BlockPos pos = new BlockPos(startX, startY, startZ);
                {
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();


                    if (predicate.test(new BlockWorldState(world, pos, true)))
                        if ((!ignoreBlockWithoutBoundingBox || block.getCollisionBoundingBox(world, pos, state) != null) &&
                            block.canCollideCheck(state, stopOnLiquid))
                        {
                            MovingObjectPosition result = block.collisionRayTrace(world, pos, start, end);

                            if (result != null)
                            {
                                return result;
                            }
                        }
                }

                MovingObjectPosition result = null;
                int k1 = 200;

                while (k1-- >= 0)
                {
                    if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord))
                    {
                        return null;
                    }

                    if (startX == endX && startY == endY && startZ == endZ)
                    {
                        return returnLastUncollidableBlock ? result : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double x = 999.0D;
                    double y = 999.0D;
                    double z = 999.0D;

                    if (endX > startX)
                    {
                        x = (double) startX + 1.0D;
                    }
                    else if (endX < startX)
                    {
                        x = (double) startX + 0.0D;
                    }
                    else
                    {
                        flag2 = false;
                    }

                    if (endY > startY)
                    {
                        y = (double) startY + 1.0D;
                    }
                    else if (endY < startY)
                    {
                        y = (double) startY + 0.0D;
                    }
                    else
                    {
                        flag = false;
                    }

                    if (endZ > startZ)
                    {
                        z = (double) startZ + 1.0D;
                    }
                    else if (endZ < startZ)
                    {
                        z = (double) startZ + 0.0D;
                    }
                    else
                    {
                        flag1 = false;
                    }

                    double stepX = 999.0D;
                    double stepY = 999.0D;
                    double stepZ = 999.0D;
                    double dX = end.xCoord - start.xCoord;
                    double dY = end.yCoord - start.yCoord;
                    double dZ = end.zCoord - start.zCoord;

                    if (flag2)
                    {
                        stepX = (x - start.xCoord) / dX;
                    }

                    if (flag)
                    {
                        stepY = (y - start.yCoord) / dY;
                    }

                    if (flag1)
                    {
                        stepZ = (z - start.zCoord) / dZ;
                    }

                    if (stepX == -0.0D)
                    {
                        stepX = -1.0E-4D;
                    }

                    if (stepY == -0.0D)
                    {
                        stepY = -1.0E-4D;
                    }

                    if (stepZ == -0.0D)
                    {
                        stepZ = -1.0E-4D;
                    }

                    EnumFacing direction;

                    if (stepX < stepY && stepX < stepZ)
                    {
                        direction = endX > startX ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3(x, start.yCoord + dY * stepX, start.zCoord + dZ * stepX);
                    }
                    else if (stepY < stepZ)
                    {
                        direction = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3(start.xCoord + dX * stepY, y, start.zCoord + dZ * stepY);
                    }
                    else
                    {
                        direction = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3(start.xCoord + dX * stepZ, start.yCoord + dY * stepZ, z);
                    }

                    startX = MathHelper.floor_double(start.xCoord) - (direction == EnumFacing.EAST ? 1 : 0);
                    startY = MathHelper.floor_double(start.yCoord) - (direction == EnumFacing.UP ? 1 : 0);
                    startZ = MathHelper.floor_double(start.zCoord) - (direction == EnumFacing.SOUTH ? 1 : 0);
                    pos = new BlockPos(startX, startY, startZ);
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (!predicate.test(new BlockWorldState(world, pos, true)))
                    {
                        result = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, start, direction, pos);
                        continue;
                    }

                    if (!ignoreBlockWithoutBoundingBox || block.getCollisionBoundingBox(world, pos, state) != null)
                    {
                        if (block.canCollideCheck(state, stopOnLiquid))
                        {
                            MovingObjectPosition tmp = block.collisionRayTrace(world, pos, start, end);

                            if (tmp != null)
                            {
                                return tmp;
                            }
                        }
                        else
                        {
                            result = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, start, direction, pos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? result : null;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
