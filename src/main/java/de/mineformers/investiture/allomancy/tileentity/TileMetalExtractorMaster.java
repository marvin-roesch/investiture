package de.mineformers.investiture.allomancy.tileentity;

import com.google.common.collect.*;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractor.Part;
import de.mineformers.investiture.allomancy.block.MetalExtractorController;
import de.mineformers.investiture.allomancy.extractor.ExtractorOutput;
import de.mineformers.investiture.allomancy.extractor.ExtractorPart;
import de.mineformers.investiture.allomancy.extractor.ExtractorRecipes;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.inventory.SimpleInventory;
import de.mineformers.investiture.multiblock.BlockRecipe;
import de.mineformers.investiture.multiblock.MultiBlock;
import de.mineformers.investiture.multiblock.MultiBlockPart;
import de.mineformers.investiture.util.Fluids;
import de.mineformers.investiture.util.Functional;
import de.mineformers.investiture.util.ItemStacks;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static de.mineformers.investiture.allomancy.block.MetalExtractor.Part.*;

/**
 * Provides the logic of the metal extractor
 */
public class TileMetalExtractorMaster extends TileEntity implements SimpleInventory, ISidedInventory, ITickable
{
    public static class Processor
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
    @Nonnull
    private Optional<Processor> processing = Optional.empty();
    private Multimap<Boolean, BlockPos> verticalFluidPositions = HashMultimap.create();
    private Multimap<Boolean, BlockPos> horizontalFluidPositions = HashMultimap.create();
    private double power = 0;
    public float prevRotation = 0;
    public float rotation = 0;

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
            prevRotation = rotation;
            double oldPower = power;
            power = calculateFlow();
            if (power == 0)
            {
                if (power != oldPower)
                    markDirty();
                return;
            }
            if (primaryOutput() != null && primaryOutput().stackSize == 0)
                inventory[PRIMARY_OUTPUT_SLOT] = null;
            if (secondaryOutput() != null && secondaryOutput().stackSize == 0)
                inventory[SECONDARY_OUTPUT_SLOT] = null;
            if (processing.isPresent())
            {
                double perTick = 360f / 1440 * (1 / 360f) * power;
                rotation += perTick;
                rotation %= 1;
                Processor current = processing.get();
                if (current.timer < 40)
                    current.timer++;
                if (current.timer >= 40)
                {
                    ItemStack primaryOutput = current.output.getPrimaryResult().copy();
                    ItemStack primaryFit = null;
                    if (primaryOutput() == null)
                    {
                        primaryFit = primaryOutput;
                    }
                    else if (primaryOutput().isItemEqual(primaryOutput) &&
                        Objects.equals(primaryOutput().getTagCompound(), primaryOutput.getTagCompound()) &&
                        (primaryOutput().stackSize + primaryOutput.stackSize) < primaryOutput().getMaxStackSize())
                    {
                        primaryFit = primaryOutput().copy();
                        primaryFit.stackSize += primaryOutput.stackSize;
                    }
                    if (primaryFit != null && (Math.random() <= current.output.getSecondaryChance() || current.output.getSecondaryResult() != null))
                    {
                        ItemStack secondaryOutput = current.output.getSecondaryResult() != null ? current.output.getSecondaryResult().copy()
                                                                                                : new ItemStack(Blocks.cobblestone);
                        if (secondaryOutput() == null)
                        {
                            inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                            inventory[SECONDARY_OUTPUT_SLOT] = secondaryOutput;
                            processing = Optional.empty();
                        }
                        else if (secondaryOutput().isItemEqual(secondaryOutput) &&
                            Objects.equals(secondaryOutput().getTagCompound(), secondaryOutput.getTagCompound()) &&
                            (secondaryOutput().stackSize + secondaryOutput.stackSize) < secondaryOutput().getMaxStackSize())
                        {
                            inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                            inventory[SECONDARY_OUTPUT_SLOT].stackSize += secondaryOutput.stackSize;
                            processing = Optional.empty();
                        }
                    }
                    else if (primaryFit != null)
                    {
                        inventory[PRIMARY_OUTPUT_SLOT] = primaryFit;
                        processing = Optional.empty();
                    }
                }
            }
            if (inventory[INPUT_SLOT] != null && !processing.isPresent())
            {
                Optional<ExtractorOutput> output = Functional.flatten(FluentIterable.from(ExtractorRecipes.recipes())
                                                                                    .transform(r -> r.match(inventory[INPUT_SLOT]))
                                                                                    .firstMatch(Optional::isPresent));
                if (output.isPresent())
                    processing = Optional.of(new Processor(decrStackSize(INPUT_SLOT, 1), output.get(), 0));
            }
            BlockPos spawnPos = pos.offset(orientation, 5);
            if (primaryOutput() != null && worldObj.isAirBlock(spawnPos))
            {
                ItemStacks.spawn(worldObj, spawnPos, primaryOutput());
                inventory[PRIMARY_OUTPUT_SLOT] = null;
            }
            if (secondaryOutput() != null && worldObj.isAirBlock(spawnPos))
            {
                ItemStacks.spawn(worldObj, spawnPos, secondaryOutput());
                inventory[SECONDARY_OUTPUT_SLOT] = null;
            }
            markDirty();
        }
    }

    public ItemStack primaryOutput()
    {
        return inventory[PRIMARY_OUTPUT_SLOT];
    }

    public ItemStack secondaryOutput()
    {
        return inventory[SECONDARY_OUTPUT_SLOT];
    }

    @Nonnull
    public Optional<Processor> getProcessing()
    {
        return processing;
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

    private static final MultiBlock multiBlock;

    static
    {
        ImmutableMap.Builder<Character, Predicate<BlockWorldState>> predicates = ImmutableMap.builder();
        predicates.put(' ', s -> s.getBlockState().getBlock() == Blocks.air);
        predicates.put('F', part(FRAME));
        predicates.put('G', part(GLASS));
        predicates.put('C', s -> s.getBlockState().getBlock() == Allomancy.Blocks.metal_extractor_controller);
        predicates.put('g', part(GRINDER));
        predicates.put('W', part(WHEEL));
        predicates.put('A', s -> true);
        multiBlock = BlockRecipe.start()
                                .layer("AAAAAA",
                                       "AAAAAA",
                                       "AAAAAA",
                                       "AAAAAA",
                                       "AAAAAA")
                                .layer("AFFFFF",
                                       "WFFFFF",
                                       "WFFFFF",
                                       "WFFFFF",
                                       "AFFFFF")
                                .layer("WFGCGF",
                                       "WFgggF",
                                       "WFgggF",
                                       "WFgggF",
                                       "WFGCGF")
                                .layer("WFGGGF",
                                       "WFgggF",
                                       "WFgggF",
                                       "WFgggF",
                                       "WFGGGF")
                                .layer("WFFFFF",
                                       "WFFFFF",
                                       "WFFFFF",
                                       "WFFFFF",
                                       "WFFFFF")
                                .layer("AAAAAA",
                                       "WAAAAA",
                                       "WAAAAA",
                                       "WAAAAA",
                                       "AAAAAA")
                                .build(predicates.build());
    }

    public boolean validateMultiBlock()
    {
        if (worldObj.isRemote)
            return false;
        Optional<EnumFacing> orientation = Functional.convert(FluentIterable.from(Arrays.asList(EnumFacing.HORIZONTALS))
                                                                            .firstMatch(f -> {
                                                                                IBlockState state = worldObj.getBlockState(pos.offset(f));
                                                                                return state.getBlock() == Allomancy.Blocks.metal_extractor &&
                                                                                    state.getValue(MetalExtractor.PART) == GRINDER;
                                                                            }));
        validMultiBlock = false;
        verticalFluidPositions.clear();
        horizontalFluidPositions.clear();

        if (orientation.isPresent())
        {
            this.orientation = orientation.get();
            ImmutableList.Builder<BlockPos> childrenBuilder = ImmutableList.builder();
            EnumFacing horizontal = getHorizontal();
            BlockPos corner = pos.offset(horizontal.getOpposite(), 3).down(2);
            if (multiBlock.validate(worldObj, corner, p -> {
                if (!p.getPos().equals(pos) && p.getBlockState().getBlock() instanceof ExtractorPart)
                {
                    childrenBuilder.add(p.getPos().subtract(pos));
                    if (p.getBlockState().getBlock() instanceof MetalExtractor && p.getBlockState().getValue(MetalExtractor.PART) == WHEEL)
                        inspectWheelPart(p);
                }
            }))
            {
                this.children = childrenBuilder.build();
                for (BlockPos child : children)
                {
                    IBlockState state = worldObj.getBlockState(pos.add(child));
                    if (state.getBlock() == Allomancy.Blocks.metal_extractor)
                        state = state.withProperty(MetalExtractor.BUILT, true);
                    else if (state.getBlock() == Allomancy.Blocks.metal_extractor_controller)
                        state = state.withProperty(MetalExtractorController.BUILT, true);
                    worldObj.setBlockState(pos.add(child), state);
                    ((TileMetalExtractorSlave) worldObj.getTileEntity(pos.add(child))).setMasterPosition(pos);
                }
                this.validMultiBlock = true;
                Investiture.net().sendDescription(this);
            }
        }
        return validMultiBlock;
    }

    private void inspectWheelPart(MultiBlockPart part)
    {
        switch (part.index())
        {
            case 36:
            case 42:
            case 48:
                horizontalFluidPositions.put(true, part.getPos().down().subtract(pos));
                break;
            case 60:
                horizontalFluidPositions.put(true, part.getPos().down().subtract(pos));
                verticalFluidPositions.put(true, part.getPos().offset(orientation.getOpposite()).subtract(pos));
                break;
            case 84:
                horizontalFluidPositions.put(true, part.getPos().down().subtract(pos));
                verticalFluidPositions.put(false, part.getPos().offset(orientation).subtract(pos));
                break;
            case 90:
                verticalFluidPositions.put(true, part.getPos().offset(orientation.getOpposite()).subtract(pos));
                break;
            case 114:
                verticalFluidPositions.put(false, part.getPos().offset(orientation).subtract(pos));
                break;
            case 120:
                horizontalFluidPositions.put(false, part.getPos().up().subtract(pos));
                verticalFluidPositions.put(true, part.getPos().offset(orientation.getOpposite()).subtract(pos));
                break;
            case 144:
                horizontalFluidPositions.put(false, part.getPos().up().subtract(pos));
                verticalFluidPositions.put(false, part.getPos().offset(orientation).subtract(pos));
                break;
            case 156:
            case 162:
            case 168:
                horizontalFluidPositions.put(false, part.getPos().up().subtract(pos));
                break;
        }
    }

    private double calculateFlow()
    {
        Vec3 horizontal = new Vec3(0, 0, 0);
        for (Map.Entry<Boolean, BlockPos> entry : horizontalFluidPositions.entries())
        {
            Vec3 flowVector = Fluids.getFlowVector(worldObj, pos.add(entry.getValue()));
            if (entry.getKey())
                horizontal = horizontal.add(flowVector);
            else
                horizontal = horizontal.subtract(flowVector);
        }

        Vec3 vertical = new Vec3(0, 0, 0);
        for (Map.Entry<Boolean, BlockPos> entry : verticalFluidPositions.entries())
        {
            Vec3 flowVector = Fluids.getFlowVector(worldObj, pos.add(entry.getValue()));
            if (entry.getKey())
                vertical = vertical.add(flowVector);
            else
                vertical = vertical.subtract(flowVector);
        }
        return Math.abs(orientation.getFrontOffsetX()) * horizontal.xCoord +
            Math.abs(orientation.getFrontOffsetZ()) * horizontal.zCoord + vertical.yCoord;
    }

    public void invalidateMultiBlock()
    {
        if (!validMultiBlock)
            return;
        for (BlockPos child : children)
        {
            BlockPos childPos = pos.add(child);
            IBlockState state = worldObj.getBlockState(childPos);
            if (state.getBlock() == Allomancy.Blocks.metal_extractor)
                worldObj.setBlockState(pos.add(child), state.withProperty(MetalExtractor.BUILT, false));
            else if (state.getBlock() == Allomancy.Blocks.metal_extractor_controller)
                worldObj.setBlockState(pos.add(child), state.withProperty(MetalExtractorController.BUILT, false)
                                                            .withProperty(MetalExtractorController.MASTER, false));
        }
        if (worldObj.getBlockState(pos).getBlock() == Allomancy.Blocks.metal_extractor_controller)
            worldObj.setBlockState(pos, worldObj.getBlockState(pos)
                                                .withProperty(MetalExtractorController.BUILT, false)
                                                .withProperty(MetalExtractorController.MASTER, false));
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
        if (update.processingInput != null)
        {
            this.processing = Optional.of(new Processor(update.processingInput, null, update.processingTimer));
        }
        else
        {
            this.processing = Optional.empty();
        }
        this.rotation = update.rotation;
        this.prevRotation = update.prevRotation;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState != newSate;
    }

    @Override
    public Packet getDescriptionPacket()
    {
        ItemStack processingStack = processing.isPresent() ? processing.get().input : null;
        int processingTimer = processing.isPresent() ? processing.get().timer : -1;
        return Investiture.net().getPacketFrom(
            new MetalExtractorUpdate(pos, validMultiBlock, orientation, processingStack, processingTimer, rotation, prevRotation));
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
    public void markDirty()
    {
        super.markDirty();
        Investiture.net().sendDescription(this);
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
