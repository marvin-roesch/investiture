package de.mineformers.investiture.tileentity;

import de.mineformers.investiture.block.Conveyor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class ConveyorInterface extends TileEntity implements ITickable
{
    @Override
    public void update()
    {
        if (world.isRemote || world.getTotalWorldTime() % 10 != 0)
            return;
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(Conveyor.FACING);
        Conveyor.InterfaceType type = state.getValue(Conveyor.INTERFACE_TYPE);
        if (type == Conveyor.InterfaceType.INSERTER)
        {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()))
            {
                double minBoxX = facing.getAxis() == EnumFacing.Axis.X ? 0.375 : 0;
                double minBoxZ = facing.getAxis() == EnumFacing.Axis.Z ? 0.375 : 0;
                double maxBoxX = facing.getAxis() == EnumFacing.Axis.X ? 0.625 : 1;
                double maxBoxZ = facing.getAxis() == EnumFacing.Axis.Z ? 0.625 : 1;
                List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(minBoxX, 0.75, minBoxZ,
                                                                                                         maxBoxX, 1.5, maxBoxZ).offset(pos));
                if (items.isEmpty())
                    return;
                IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
                for (EntityItem entity : items)
                {
                    ItemStack stack = entity.getItem();
                    for (int i = 0; i < inventory.getSlots() && !stack.isEmpty(); i++)
                    {
                        stack = inventory.insertItem(i, stack, false);
                    }
                    if (stack.isEmpty())
                    {
                        entity.setDead();
                    }
                    else
                    {
                        entity.setItem(stack);
                    }
                }
            }
        }
        else
        {
            TileEntity te = world.getTileEntity(pos.offset(facing.getOpposite()));
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing))
            {
                IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
                for (int i = 0; i < inventory.getSlots(); i++)
                {
                    ItemStack stack = inventory.extractItem(i, 4, true);
                    if (!stack.isEmpty())
                    {
                        inventory.extractItem(i, stack.getCount(), false);
                        EntityItem entity = new EntityItem(world,
                                                           pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.1,
                                                           pos.getY() + 0.9375,
                                                           pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.1,
                                                           stack);
                        entity.motionX = entity.motionY = entity.motionZ = 0;
                        world.spawnEntity(entity);
                        break;
                    }
                }
            }
        }
    }
}
