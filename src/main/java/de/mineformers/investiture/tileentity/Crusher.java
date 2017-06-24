package de.mineformers.investiture.tileentity;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.crusher.CrusherOutput;
import de.mineformers.investiture.allomancy.crusher.CrusherRecipes;
import de.mineformers.investiture.util.Functional;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static de.mineformers.investiture.Investiture.MOD_ID;

public class Crusher extends TileEntity implements ITickable
{
    @Nullable
    private final IAnimationStateMachine asm;
    private final TimeValues.VariableValue animationTrigger = new TimeValues.VariableValue(Float.NEGATIVE_INFINITY);
    private int counter;

    public Crusher()
    {
        asm = Investiture.proxy.loadASM(new ResourceLocation(MOD_ID, "asms/block/crusher.json"), ImmutableMap.of(
            "animation_trigger", animationTrigger
        ));
    }

    public void triggerAnimation()
    {
        if (asm.currentState().equals("up"))
        {
            animationTrigger.setValue(Animation.getWorldTime(world));
            asm.transition("pushing");
        }
    }

    @Override
    public void update()
    {
        if (world.isRemote)
            return;
        if (counter == 0)
        {
            world.addBlockEvent(pos, getBlockType(), 0, 0);
        }
        if (counter == 9)
        {
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(0, -1, 0, 1, 0, 1).offset(pos));
            for (EntityItem item : items)
            {
                ItemStack stack = item.getItem();
                Optional<CrusherOutput> output = Functional.flatten(CrusherRecipes.recipes().stream()
                                                                                  .map(r -> r.match(stack)).filter(Optional::isPresent).findFirst());
                output.ifPresent(o ->
                                 {
                                     EntityItem entity = new EntityItem(world,
                                                                        pos.getX() + 0.5,
                                                                        pos.getY() - 1 + 0.125,
                                                                        pos.getZ() + 0.5,
                                                                        o.getPrimaryResult());
                                     entity.motionX = entity.motionY = entity.motionZ = 0;
                                     world.spawnEntity(entity);
                                     item.setDead();
                                 });
            }
        }
        if (counter >= 40)
        {
            counter = 0;
        }
        else
        {
            counter++;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.counter = compound.getInteger("Counter");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound tag = super.writeToNBT(compound);
        tag.setInteger("Counter", counter);
        return tag;
    }

    @Override
    public boolean hasFastRenderer()
    {
        return true;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side)
    {
        if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, side);
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side)
    {
        if (capability == CapabilityAnimation.ANIMATION_CAPABILITY)
        {
            return CapabilityAnimation.ANIMATION_CAPABILITY.cast(asm);
        }
        return super.getCapability(capability, side);
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(0, -1, 0, 1, 2, 1).offset(pos);
    }
}
