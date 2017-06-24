package de.mineformers.investiture.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractBlock extends Block
{
    public AbstractBlock(Material material, MapColor mapColor)
    {
        super(material, mapColor);
    }

    public AbstractBlock(Material material)
    {
        super(material);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask,
                                      List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean actualState)
    {
        if (!actualState)
        {
            state = getActualState(state, world, pos);
        }
        if (this instanceof ComplexBounds)
        {
            List<AxisAlignedBB> bounds = ((ComplexBounds) this).getCollisionBoxes(world, pos, state);
            for (AxisAlignedBB box : bounds)
            {
                addCollisionBoxToList(pos, mask, boxes, box);
            }
            return;
        }
        super.addCollisionBoxToList(state, world, pos, mask, boxes, entity, actualState);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end)
    {
        if (this instanceof ComplexBounds)
        {
            ComplexBounds complex = (ComplexBounds) this;
            for (AxisAlignedBB box : complex.getSelectionBoxes(world, pos, state))
            {
                RayTraceResult mop = this.rayTrace(pos, start, end, box);
                if (mop != null)
                    return mop;
            }
            return null;
        }
        return super.collisionRayTrace(state, world, pos, start, end);
    }
}
