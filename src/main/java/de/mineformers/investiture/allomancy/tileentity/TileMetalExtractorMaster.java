package de.mineformers.investiture.allomancy.tileentity;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.Allomancy;
import de.mineformers.investiture.allomancy.block.MetalExtractor;
import de.mineformers.investiture.allomancy.block.MetalExtractor.Part;
import de.mineformers.investiture.allomancy.block.MetalExtractorController;
import de.mineformers.investiture.allomancy.extractor.ExtractorOutput;
import de.mineformers.investiture.allomancy.extractor.ExtractorPart;
import de.mineformers.investiture.allomancy.extractor.ExtractorRecipes;
import de.mineformers.investiture.allomancy.network.MetalExtractorUpdate;
import de.mineformers.investiture.multiblock.BlockRecipe;
import de.mineformers.investiture.multiblock.MultiBlock;
import de.mineformers.investiture.multiblock.MultiBlockPart;
import de.mineformers.investiture.util.Fluids;
import de.mineformers.investiture.util.Fluids.FlowPoint;
import de.mineformers.investiture.util.Functional;
import de.mineformers.investiture.util.ItemStacks;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static de.mineformers.investiture.allomancy.block.MetalExtractor.Part.*;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

/**
 * Provides the logic of the metal extractor
 */
public class TileMetalExtractorMaster extends TileEntity implements ITickable
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
    private ItemStackHandler inventory = new ItemStackHandler(3)
    {
        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (slot > INPUT_SLOT || ExtractorRecipes.recipes().stream().anyMatch(e -> e.match(stack).isPresent()))
            {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (slot == INPUT_SLOT)
            {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    };
    @Nonnull
    private Optional<Processor> processing = Optional.empty();
    private List<FlowPoint> verticalFluidPositions = new ArrayList<>();
    private List<FlowPoint> horizontalFluidPositions = new ArrayList<>();
    private double power = 0;
    public float prevRotation = 0;
    public float rotation = 0;

    @Override
    public void update()
    {
        if (world.isRemote)
            return;
        if (!checkedValidity)
        {
            revalidateMultiBlock();
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
            if (!primaryOutput().isEmpty() && primaryOutput().getCount() == 0)
                inventory.setStackInSlot(PRIMARY_OUTPUT_SLOT, ItemStack.EMPTY);
            if (!secondaryOutput().isEmpty() && secondaryOutput().getCount() == 0)
                inventory.setStackInSlot(SECONDARY_OUTPUT_SLOT, ItemStack.EMPTY);
            if (processing.isPresent())
            {
                double perTick = 360f / 1440 * (1 / 360f) * power;
                rotation += perTick;
                rotation %= 1;
                Processor current = processing.get();
                if (current.timer < getProcessingTime())
                    current.timer++;
                if (current.timer >= getProcessingTime())
                {
                    ItemStack primaryOutput = current.output.getPrimaryResult().copy();
                    ItemStack primaryFit = null;
                    if (primaryOutput().isEmpty())
                    {
                        primaryFit = primaryOutput;
                    }
                    else if (primaryOutput().isItemEqual(primaryOutput) &&
                        Objects.equals(primaryOutput().getTagCompound(), primaryOutput.getTagCompound()) &&
                        (primaryOutput().getCount() + primaryOutput.getCount()) < primaryOutput().getMaxStackSize())
                    {
                        primaryFit = primaryOutput().copy();
                        primaryFit.grow(primaryOutput.getCount());
                    }
                    if (primaryFit != null && (Math.random() <= current.output.getSecondaryChance() || !current.output.getSecondaryResult()
                                                                                                                      .isEmpty()))
                    {
                        ItemStack secondaryOutput = !current.output.getSecondaryResult().isEmpty() ? current.output.getSecondaryResult().copy()
                                                                                                   : new ItemStack(Blocks.COBBLESTONE);
                        if (secondaryOutput().isEmpty())
                        {
                            inventory.setStackInSlot(PRIMARY_OUTPUT_SLOT, primaryFit);
                            inventory.setStackInSlot(SECONDARY_OUTPUT_SLOT, secondaryOutput);
                            processing = Optional.empty();
                        }
                        else if (secondaryOutput().isItemEqual(secondaryOutput) &&
                            Objects.equals(secondaryOutput().getTagCompound(), secondaryOutput.getTagCompound()) &&
                            (secondaryOutput().getCount() + secondaryOutput.getCount()) < secondaryOutput().getMaxStackSize())
                        {
                            inventory.setStackInSlot(PRIMARY_OUTPUT_SLOT, primaryFit);
                            inventory.getStackInSlot(SECONDARY_OUTPUT_SLOT).grow(secondaryOutput.getCount());
                            processing = Optional.empty();
                        }
                    }
                    else if (primaryFit != null)
                    {
                        inventory.setStackInSlot(PRIMARY_OUTPUT_SLOT, primaryFit);
                        processing = Optional.empty();
                    }
                }
            }
            if (!inventory.getStackInSlot(INPUT_SLOT).isEmpty() && !processing.isPresent())
            {
                Optional<ExtractorOutput> output = Functional.flatten(
                    ExtractorRecipes.recipes().stream().map(r -> r.match(inventory.getStackInSlot(INPUT_SLOT))).filter(Optional::isPresent)
                                    .findFirst());
                output.ifPresent(
                    extractorOutput -> processing = Optional.of(new Processor(inventory.extractItem(INPUT_SLOT, 1, false), extractorOutput, 0)));
            }
            BlockPos spawnPos = pos.offset(orientation, 5);
            if (!primaryOutput().isEmpty() && world.isAirBlock(spawnPos))
            {
                ItemStacks.spawn(world, spawnPos, primaryOutput());
                inventory.setStackInSlot(PRIMARY_OUTPUT_SLOT, ItemStack.EMPTY);
            }
            if (!secondaryOutput().isEmpty() && world.isAirBlock(spawnPos))
            {
                ItemStacks.spawn(world, spawnPos, secondaryOutput());
                inventory.setStackInSlot(SECONDARY_OUTPUT_SLOT, ItemStack.EMPTY);
            }
            markDirty();
        }
    }

    public ItemStack primaryOutput()
    {
        return inventory.getStackInSlot(PRIMARY_OUTPUT_SLOT);
    }

    public ItemStack secondaryOutput()
    {
        return inventory.getStackInSlot(SECONDARY_OUTPUT_SLOT);
    }

    @Nonnull
    public Optional<Processor> getProcessing()
    {
        return processing;
    }

    public double getProcessingTime()
    {
        return 100 / Math.abs(power);
    }

    public double getPower()
    {
        return power;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);
        NBTTagList children = new NBTTagList();
        for (BlockPos child : this.children)
            children.appendTag(new NBTTagLong(child.toLong()));
        compound.setTag("Children", children);
        compound.setBoolean("ValidMultiBlock", validMultiBlock);
        compound.setInteger("Orientation", orientation.getHorizontalIndex());
        compound.setFloat("Rotation", rotation);
        compound.setTag("Items", inventory.serializeNBT());
        return compound;
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
        this.rotation = compound.getFloat("Rotation");
        inventory.deserializeNBT(compound.getCompoundTag("Items"));
    }

    private static Predicate<BlockWorldState> part(Part part)
    {
        return s -> s.getBlockState().getBlock() == Allomancy.Blocks.METAL_EXTRACTOR && s.getBlockState().getValue(MetalExtractor.PART) == part;
    }

    private static final MultiBlock multiBlock;

    static
    {
        ImmutableMap.Builder<Character, Predicate<BlockWorldState>> predicates = ImmutableMap.builder();
        predicates.put(' ', s -> s.getBlockState().getBlock() == Blocks.AIR);
        predicates.put('F', part(FRAME));
        predicates.put('G', part(GLASS));
        predicates.put('C', s -> s.getBlockState().getBlock() == Allomancy.Blocks.METAL_EXTRACTOR_CONTROLLER);
        predicates.put('g', part(GRINDER));
        predicates.put('W', part(WHEEL));
        predicates.put('A', s -> true);
        multiBlock = BlockRecipe.start()
                                .layer("FFFFFA",
                                       "FFFFFW",
                                       "FFFFFW",
                                       "FFFFFW",
                                       "FFFFFA")
                                .layer("FFCFFW",
                                       "GgggFW",
                                       "GgggFW",
                                       "GgggFW",
                                       "FFCFFW")
                                .layer("FFFFFW",
                                       "GgggFW",
                                       "GgggFW",
                                       "GgggFW",
                                       "FFFFFW")
                                .layer("FFFFFW",
                                       "FFFFFW",
                                       "FFFFFW",
                                       "FFFFFW",
                                       "FFFFFW")
                                .layer("AAAAAA",
                                       "AAAAAW",
                                       "AAAAAW",
                                       "AAAAAW",
                                       "AAAAAA")
                                .build(predicates.build());
    }

    public boolean validateMultiBlock()
    {
        if (world.isRemote)
            return false;
        Optional<EnumFacing> orientation = Functional.convert(FluentIterable.from(Arrays.asList(EnumFacing.HORIZONTALS))
                                                                            .firstMatch(f ->
                                                                                        {
                                                                                            IBlockState state = world.getBlockState(pos.offset(f));
                                                                                            return state
                                                                                                .getBlock() == Allomancy.Blocks.METAL_EXTRACTOR &&
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
            BlockPos corner = pos.offset(horizontal.getOpposite(), 2).down();
            if (multiBlock.validate(world, corner, p ->
            {
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
                    IBlockState state = world.getBlockState(pos.add(child));
                    if (state.getBlock() == Allomancy.Blocks.METAL_EXTRACTOR)
                        state = state.withProperty(MetalExtractor.BUILT, true);
                    else if (state.getBlock() == Allomancy.Blocks.METAL_EXTRACTOR_CONTROLLER)
                        state = state.withProperty(MetalExtractorController.BUILT, true);
                    world.setBlockState(pos.add(child), state);
                    ((TileMetalExtractorSlave) world.getTileEntity(pos.add(child))).setMasterPosition(pos);
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
                horizontalFluidPositions.add(FlowPoint.withAddition(part.getPos().down().subtract(pos)));
                break;
            case 60:
                horizontalFluidPositions.add(FlowPoint.withAddition(part.getPos().down().subtract(pos)));
                verticalFluidPositions.add(FlowPoint.withAddition(part.getPos().offset(orientation.getOpposite()).subtract(pos)));
                break;
            case 84:
                horizontalFluidPositions.add(FlowPoint.withAddition(part.getPos().down().subtract(pos)));
                verticalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().offset(orientation).subtract(pos)));
                break;
            case 90:
                verticalFluidPositions.add(FlowPoint.withAddition(part.getPos().offset(orientation.getOpposite()).subtract(pos)));
                break;
            case 114:
                verticalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().offset(orientation).subtract(pos)));
                break;
            case 120:
                horizontalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().up().subtract(pos)));
                verticalFluidPositions.add(FlowPoint.withAddition(part.getPos().offset(orientation.getOpposite()).subtract(pos)));
                break;
            case 144:
                horizontalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().up().subtract(pos)));
                verticalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().offset(orientation).subtract(pos)));
                break;
            case 156:
            case 162:
            case 168:
                horizontalFluidPositions.add(FlowPoint.withSubtraction(part.getPos().up().subtract(pos)));
                break;
        }
    }

    private double calculateFlow()
    {
        Vec3d horizontal = Fluids.getFlowVector(world, pos, horizontalFluidPositions);
        Vec3d vertical = Fluids.getFlowVector(world, pos, verticalFluidPositions);
        return Math.abs(orientation.getFrontOffsetX()) * horizontal.xCoord +
            Math.abs(orientation.getFrontOffsetZ()) * horizontal.zCoord + vertical.yCoord;
    }

    public void revalidateMultiBlock()
    {
        if (!validateMultiBlock())
        {
            invalidateMultiBlock(true);
        }
    }

    public void invalidateMultiBlock(boolean force)
    {
        if (!validMultiBlock && !force)
            return;
        for (BlockPos child : children)
        {
            BlockPos childPos = pos.add(child);
            IBlockState state = world.getBlockState(childPos);
            if (state.getBlock() == Allomancy.Blocks.METAL_EXTRACTOR)
                world.setBlockState(pos.add(child), state.withProperty(MetalExtractor.BUILT, false));
            else if (state.getBlock() == Allomancy.Blocks.METAL_EXTRACTOR_CONTROLLER)
                world.setBlockState(pos.add(child), state.withProperty(MetalExtractorController.BUILT, false)
                                                         .withProperty(MetalExtractorController.MASTER, false));
        }
        if (world.getBlockState(pos).getBlock() == Allomancy.Blocks.METAL_EXTRACTOR_CONTROLLER)
            world.setBlockState(pos, world.getBlockState(pos)
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
        switch (orientation)
        {
            case NORTH:
                return EnumFacing.EAST;
            case SOUTH:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.NORTH;
            default:
                return EnumFacing.SOUTH;
        }
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
        this.power = update.power;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState != newSate;
    }

    public Packet getDescriptionPacket()
    {
        ItemStack processingStack = processing.isPresent() ? processing.get().input : null;
        int processingTimer = processing.isPresent() ? processing.get().timer : -1;
        return Investiture.net().getPacketFrom(
            new MetalExtractorUpdate(pos, validMultiBlock, orientation, processingStack, processingTimer, rotation, prevRotation, power));
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        Investiture.net().sendDescription(this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == ITEM_HANDLER_CAPABILITY)
            return facing == null || facing == orientation || facing == orientation.getOpposite();
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ITEM_HANDLER_CAPABILITY)
            if (facing == orientation)
                return ITEM_HANDLER_CAPABILITY.cast(new RangedWrapper(inventory, PRIMARY_OUTPUT_SLOT, SECONDARY_OUTPUT_SLOT + 1));
            else if (facing == orientation.getOpposite())
                return ITEM_HANDLER_CAPABILITY.cast(new RangedWrapper(inventory, INPUT_SLOT, PRIMARY_OUTPUT_SLOT));
            else if (facing == null)
                return ITEM_HANDLER_CAPABILITY.cast(inventory);
        return super.getCapability(capability, facing);
    }
}
