package de.mineformers.investiture.allomancy.tileentity;

import com.google.common.base.Optional;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractor.Part;
import de.mineformers.investiture.allomancy.extractor.ExtractorOutput;
import de.mineformers.investiture.allomancy.extractor.ExtractorRecipes;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.inventory.SimpleInventory;
import de.mineformers.investiture.multiblock.BlockRecipe;
import de.mineformers.investiture.multiblock.MultiBlock;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Arrays;
import java.util.function.Predicate;

import static de.mineformers.investiture.allomancy.block.MetalExtractor.Part.*;

/**
 * Provides the logic of the metal extractor
 */
public class TileMetalExtractorMaster extends TileEntity implements SimpleInventory, ISidedInventory, ITickable
{
    private static class Processor
    {
        public final ItemStack input;
        public final ExtractorOutput output;
        public int timer;

        public Processor(ItemStack input, ExtractorOutput output, int timer)
        {
            this.input = input;
            this.output = output;
            this.timer = timer;
        }
    }

    public static final int INPUT_SLOT = 0;
    public static final int PRIMARY_OUTPUT_SLOT = 1;
    public static final int SECONDARY_OUTPUT_SLOT = 2;

    private EnumFacing orientation = EnumFacing.NORTH;
    private boolean validMultiBlock = false;
    private boolean checkedValidity = false;
    private ImmutableList<BlockPos> children = ImmutableList.of();
    private ItemStack[] inventory = new ItemStack[3];
    private EvictingQueue<Processor> processing = EvictingQueue.create(3);

    @Override
    public void update()
    {
        if (worldObj.isRemote)
            return;
        if (!checkedValidity)
        {
            validMultiBlock = validateMultiBlock();
            checkedValidity = true;
        }
        if (isValidMultiBlock())
        {
            if (inventory[PRIMARY_OUTPUT_SLOT] != null && inventory[PRIMARY_OUTPUT_SLOT].stackSize == 0)
                inventory[PRIMARY_OUTPUT_SLOT] = null;
            if (inventory[SECONDARY_OUTPUT_SLOT] != null && inventory[SECONDARY_OUTPUT_SLOT].stackSize == 0)
                inventory[SECONDARY_OUTPUT_SLOT] = null;
            for (Processor process : processing)
                process.timer++;
            if (!processing.isEmpty())
            {
                Processor current = processing.peek();
                if (current.timer == 40)
                {
                    ItemStack primaryOutput = current.output.getPrimaryResult().copy();
                    ItemStack secondaryOutput = current.output.getSecondaryResult().copy();
                    ItemStack primaryFit = null;
                    if (inventory[PRIMARY_OUTPUT_SLOT] == null)
                    {
                        primaryFit = primaryOutput;
                    }
                    else if (inventory[PRIMARY_OUTPUT_SLOT].isItemEqual(secondaryOutput) &&
                        inventory[PRIMARY_OUTPUT_SLOT].stackSize < inventory[PRIMARY_OUTPUT_SLOT].getMaxStackSize())
                    {
                        primaryFit = inventory[PRIMARY_OUTPUT_SLOT].copy();
                        primaryFit.stackSize++;
                    }
                    if (primaryFit != null && Math.random() <= current.output.getSecondaryChance())
                    {
                        if (inventory[SECONDARY_OUTPUT_SLOT] == null)
                        {
                            inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                            inventory[SECONDARY_OUTPUT_SLOT] = secondaryOutput;
                            processing.poll();
                        }
                        else if (inventory[SECONDARY_OUTPUT_SLOT].isItemEqual(secondaryOutput) &&
                            inventory[SECONDARY_OUTPUT_SLOT].stackSize < inventory[SECONDARY_OUTPUT_SLOT].getMaxStackSize())
                        {
                            inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                            inventory[SECONDARY_OUTPUT_SLOT].stackSize++;
                            processing.poll();
                        }
                    }
                    else if (primaryFit != null)
                    {
                        inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                        processing.poll();
                    }
                }
            }
            if (inventory[INPUT_SLOT] != null && processing.remainingCapacity() > 0)
            {
                Optional<Optional<ExtractorOutput>> output = FluentIterable.from(ExtractorRecipes.recipes())
                                                                           .transform(r -> r.match(inventory[INPUT_SLOT]))
                                                                           .firstMatch(Optional::isPresent);
                if (output.isPresent() && output.get().isPresent())
                    processing.offer(new Processor(decrStackSize(INPUT_SLOT, 1), output.get().get(), 0));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList children = new NBTTagList();
        for (BlockPos child : this.children)
            children.appendTag(new NBTTagLong(child.toLong()));
        compound.setTag("Children", children);
        compound.setBoolean("ValidMultiBlock", validMultiBlock);
        compound.setInteger("Orientation", orientation.getHorizontalIndex());
        writeInventoryToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        ImmutableList.Builder<BlockPos> childrenBuilder = ImmutableList.builder();
        NBTTagList children = compound.getTagList("Children", Constants.NBT.TAG_LONG);
        for (int i = 0; i < children.tagCount(); i++)
            childrenBuilder.add(BlockPos.fromLong(((NBTTagLong) children.get(i)).getLong()));
        this.children = childrenBuilder.build();
        this.validMultiBlock = compound.getBoolean("ValidMultiBlock");
        this.orientation = EnumFacing.getHorizontal(compound.getInteger("Orientation"));
        readInventoryFromNBT(compound);
    }

    private static Predicate<BlockWorldState> part(Part part)
    {
        return s -> s.getBlockState().getBlock() == Allomancy.Blocks.metal_extractor && s.getBlockState().getValue(MetalExtractor.PART) == part;
    }

    private static final MultiBlock multiBlock = BlockRecipe.start()
                                                            .layer("FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF")
                                                            .layer("FGCGF",
                                                                   "FgggF",
                                                                   "FgggF",
                                                                   "FgggF",
                                                                   "FGCGF")
                                                            .layer("FGGGF",
                                                                   "FgggF",
                                                                   "FgggF",
                                                                   "FgggF",
                                                                   "FGGGF")
                                                            .layer("FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF",
                                                                   "FFFFF")
                                                            .build(ImmutableMap.of('F', part(FRAME),
                                                                                   'G', part(GLASS),
                                                                                   'C', part(CONTROLLER),
                                                                                   'g', part(GRINDER)));

    public boolean validateMultiBlock()
    {
        if (worldObj.isRemote)
            return false;
        Optional<EnumFacing> orientation = FluentIterable.from(Arrays.asList(EnumFacing.HORIZONTALS))
                                                         .firstMatch(f -> {
                                                             IBlockState state = worldObj.getBlockState(pos.offset(f));
                                                             return state.getBlock() == this.getBlockType() &&
                                                                 state.getValue(MetalExtractor.PART) == GRINDER;
                                                         });
        validMultiBlock = false;

        if (orientation.isPresent())
        {
            this.orientation = orientation.get();
            ImmutableList.Builder<BlockPos> childrenBuilder = ImmutableList.builder();
            EnumFacing horizontal = getHorizontal();
            BlockPos corner = pos.offset(horizontal.getOpposite(), 2).down();
            if (multiBlock.validate(worldObj, corner, p -> {
                if (!p.equals(pos)) childrenBuilder.add(p.subtract(pos));
            }))
            {
                this.children = childrenBuilder.build();
                for (BlockPos child : children)
                {
                    worldObj.setBlockState(pos.add(child), worldObj.getBlockState(pos.add(child)).withProperty(MetalExtractor.BUILT, true));
                    ((TileMetalExtractorDummy) worldObj.getTileEntity(pos.add(child))).setMasterPosition(pos);
                }
                this.validMultiBlock = true;
                Investiture.net().sendToAllAround(new MetalExtractorUpdate(pos, true, orientation.get()),
                                                  new NetworkRegistry.TargetPoint(worldObj.provider.getDimensionId(),
                                                                                  pos.getX(), pos.getY(), pos.getZ(), 32));
            }
        }
        return validMultiBlock;
    }

    public void invalidateMultiBlock()
    {
        if (!validMultiBlock)
            return;
        for (BlockPos child : children)
            if (worldObj.getBlockState(pos.add(child)).getBlock() == Allomancy.Blocks.metal_extractor)
                worldObj.setBlockState(pos.add(child), worldObj.getBlockState(pos.add(child))
                                                               .withProperty(MetalExtractor.BUILT, false)
                                                               .withProperty(MetalExtractor.MASTER, false));
        if (worldObj.getBlockState(pos).getBlock() == Allomancy.Blocks.metal_extractor)
            worldObj.setBlockState(pos, worldObj.getBlockState(pos)
                                                .withProperty(MetalExtractor.BUILT, false)
                                                .withProperty(MetalExtractor.MASTER, false));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(
            pos.getX() - 6, pos.getY() - 2, pos.getZ() - 6,
            pos.getX() + 6, pos.getY() + 5, pos.getZ() + 6
        );
    }

    public boolean isValidMultiBlock()
    {
        return validMultiBlock;
    }

    public EnumFacing getOrientation()
    {
        return orientation;
    }

    public EnumFacing getHorizontal()
    {
        return orientation.getAxis() == EnumFacing.Axis.X ? EnumFacing.SOUTH : EnumFacing.EAST;
    }

    public void processUpdate(MetalExtractorUpdate update)
    {
        this.validMultiBlock = update.validMultiBlock;
        this.orientation = update.orientation;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState != newSate;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return Investiture.net().getPacketFrom(new MetalExtractorUpdate(pos, validMultiBlock, orientation));
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (side == orientation)
            return new int[]{PRIMARY_OUTPUT_SLOT, SECONDARY_OUTPUT_SLOT};
        else if (side == orientation.getOpposite())
            return new int[]{INPUT_SLOT};
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction)
    {
        return direction == orientation.getOpposite() && index == INPUT_SLOT && isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return direction == orientation && (index == PRIMARY_OUTPUT_SLOT || index == SECONDARY_OUTPUT_SLOT);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return index != INPUT_SLOT || ExtractorRecipes.recipes().stream().anyMatch(e -> e.match(stack).isPresent());
    }

    @Override
    public int getSizeInventory()
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory[index];
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory[index] = stack;
        markDirty();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }
}
