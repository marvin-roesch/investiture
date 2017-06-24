package de.mineformers.investiture.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides utilities related to ray tracing, i.e. 'searching things under the cursor'.
 */
public class RayTracing
{
    /**
     * Casts a ray searching entities.
     *
     * @param camera    the camera to cast the ray from
     * @param reach     the maximum distance the ray may travel
     * @param predicate a predicate filtering the set of plausible targets
     * @return a result indicating the success of the ray trace
     */
    @Nullable
    public static RayTraceResult rayTraceEntities(Entity camera, double reach, Predicate<Entity> predicate)
    {
        // Effectively a copy of EntityRenderer.getMouseOver, see that for details
        Vec3d start = camera.getPositionEyes(1);
        Vec3d direction = camera.getLook(1);
        Vec3d end = start.addVector(direction.x * reach, direction.y * reach, direction.z * reach);
        Entity result = null;
        Vec3d hitVec = null;
        List<Entity> list = camera.world.getEntitiesInAABBexcluding(camera, camera.getEntityBoundingBox()
                                                                                  .expand(direction.x * reach,
                                                                                            direction.y * reach,
                                                                                            direction.z * reach)
                                                                                  .grow(1, 1, 1), EntitySelectors.NOT_SPECTATING);
        double minDistance = reach;

        for (Entity checkedEntity : list)
        {
            float borderSize = checkedEntity.getCollisionBorderSize();
            AxisAlignedBB bounds = checkedEntity.getEntityBoundingBox().grow(borderSize, borderSize, borderSize);
            RayTraceResult mop = bounds.calculateIntercept(start, end);

            if (bounds.contains(start))
            {
                if (predicate.test(checkedEntity) && minDistance >= 0.0D)
                {
                    result = checkedEntity;
                    hitVec = mop == null ? start : mop.hitVec;
                    minDistance = 0.0D;
                }
            }
            else if (mop != null)
            {
                double distance = start.distanceTo(mop.hitVec);

                if (distance < minDistance || minDistance == 0.0D)
                {
                    if (checkedEntity == camera.getRidingEntity() && !camera.canRiderInteract())
                    {
                        if (predicate.test(checkedEntity) && minDistance == 0.0D)
                        {
                            result = checkedEntity;
                            hitVec = mop.hitVec;
                        }
                    }
                    else
                    {
                        if (predicate.test(checkedEntity))
                        {
                            result = checkedEntity;
                            hitVec = mop.hitVec;
                            minDistance = distance;
                        }
                    }
                }
            }
        }
        if (result != null)
            return new RayTraceResult(result, hitVec);
        else
            return null;
    }

    /**
     * Casts a ray searching blocks.
     *
     * @param camera                       the camera to cast the ray from
     * @param reach                        the maximum distance the ray may travel
     * @param predicate                    a predicate filtering the set of plausible targets
     * @param stopOnLiquid                 determines if the ray should go through liquids
     * @param collideWithBoundingBoxesOnly determines if blocks without bounding box should be ignored
     * @param returnLastUncollidableBlock  determines if the last uncollided block should be returned if the ray hits the last block without a result
     * @return a result indicating the success of the ray trace
     */
    @Nullable
    public static RayTraceResult rayTraceBlocks(Entity camera, double reach,
                                                Predicate<BlockWorldState> predicate,
                                                boolean stopOnLiquid,
                                                boolean collideWithBoundingBoxesOnly,
                                                boolean returnLastUncollidableBlock)
    {
        Vec3d start = camera.getPositionVector().addVector(0, camera.getEyeHeight(), 0);
        Vec3d look = camera.getLook(1);
        Vec3d end = start.addVector(look.x * reach, look.y * reach, look.z * reach);
        return rayTraceBlocks(camera.world, start, end, predicate, stopOnLiquid, collideWithBoundingBoxesOnly, returnLastUncollidableBlock);
    }

    /**
     * Casts a ray searching blocks.
     *
     * @param world                        the world the ray should be cast in
     * @param start                        the start of the ray
     * @param end                          the end of the ray
     * @param predicate                    a predicate filtering the set of plausible targets
     * @param stopOnLiquid                 determines if the ray should go through liquids
     * @param collideWithBoundingBoxesOnly determines if blocks without bounding box should be ignored
     * @param returnLastUncollidableBlock  determines if the last uncollided block should be returned if the ray hits the last block without a result
     * @return a result indicating the success of the ray trace
     */
    @Nullable
    public static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end,
                                                Predicate<BlockWorldState> predicate,
                                                boolean stopOnLiquid,
                                                boolean collideWithBoundingBoxesOnly,
                                                boolean returnLastUncollidableBlock)
    {
        // See World.rayTraceBlocks, this just adds the predicate
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z))
        {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z))
            {
                int startX = MathHelper.floor(start.x);
                int startY = MathHelper.floor(start.y);
                int startZ = MathHelper.floor(start.z);
                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);
                BlockPos pos = new BlockPos(startX, startY, startZ);
                {
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();


                    if (predicate.test(new BlockWorldState(world, pos, true)))
                        if ((!collideWithBoundingBoxesOnly || block.getCollisionBoundingBox(state, world, pos) != null) &&
                            block.canCollideCheck(state, stopOnLiquid))
                        {
                            RayTraceResult result = block.collisionRayTrace(state, world, pos, start, end);

                            if (result != null)
                            {
                                return result;
                            }
                        }
                }

                RayTraceResult result = null;
                int k1 = 200;

                while (k1-- >= 0)
                {
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
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
                    double dX = end.x - start.x;
                    double dY = end.y - start.y;
                    double dZ = end.z - start.z;

                    if (flag2)
                    {
                        stepX = (x - start.x) / dX;
                    }

                    if (flag)
                    {
                        stepY = (y - start.y) / dY;
                    }

                    if (flag1)
                    {
                        stepZ = (z - start.z) / dZ;
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
                        start = new Vec3d(x, start.y + dY * stepX, start.z + dZ * stepX);
                    }
                    else if (stepY < stepZ)
                    {
                        direction = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.x + dX * stepY, y, start.z + dZ * stepY);
                    }
                    else
                    {
                        direction = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.x + dX * stepZ, start.y + dY * stepZ, z);
                    }

                    startX = MathHelper.floor(start.x) - (direction == EnumFacing.EAST ? 1 : 0);
                    startY = MathHelper.floor(start.y) - (direction == EnumFacing.UP ? 1 : 0);
                    startZ = MathHelper.floor(start.z) - (direction == EnumFacing.SOUTH ? 1 : 0);
                    pos = new BlockPos(startX, startY, startZ);
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (!predicate.test(new BlockWorldState(world, pos, true)))
                    {
                        result = new RayTraceResult(RayTraceResult.Type.MISS, start, direction, pos);
                        continue;
                    }

                    if (!collideWithBoundingBoxesOnly || block.getCollisionBoundingBox(state, world, pos) != null)
                    {
                        if (block.canCollideCheck(state, stopOnLiquid))
                        {
                            RayTraceResult tmp = block.collisionRayTrace(state, world, pos, start, end);

                            if (tmp != null)
                            {
                                return tmp;
                            }
                        }
                        else
                        {
                            result = new RayTraceResult(RayTraceResult.Type.MISS, start, direction, pos);
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
