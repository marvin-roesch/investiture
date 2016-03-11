package de.mineformers.investiture.allomancy.impl.misting.temporal;

import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.temporal.Slider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.Vec3;

import java.util.Random;

/**
 * ${JDOC}
 */
public class SliderImpl extends AbstractTimeManipulator implements Slider, ITickable
{
    @Inject
    private Allomancer allomancer;
    @Inject
    private Entity entity;
    private SpeedBubble bubble;

    @Override
    public void startBurning()
    {
        bubble = new SpeedBubble(entity.dimension, entity.getPosition(), 5);
        SpeedBubbles.from(entity.worldObj).add(bubble);
    }

    @Override
    public void update()
    {
        if (bubble != null)
        {
            Vec3 bubblePos = new Vec3(bubble.position.getX() + 0.5, bubble.position.getY(), bubble.position.getZ() + 0.5);
            if (entity.dimension != bubble.dimension || entity.getDistanceSq(bubblePos.xCoord, bubblePos.yCoord, bubblePos.zCoord) > 25)
            {
//                if (!entity.worldObj.isRemote)
//                    allomancer.deactivate(Slider.class);
                return;
            }
            for (BlockPos pos : BlockPos.getAllInBox(bubble.position.add(-bubble.radius, -bubble.radius, -bubble.radius),
                                                     bubble.position.add(bubble.radius, bubble.radius, bubble.radius)))
            {
                if (bubblePos.squareDistanceTo(new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > bubble.radius * bubble.radius)
                    continue;
                TileEntity tile = entity.worldObj.getTileEntity(pos);
                if (tile instanceof ITickable)
                    ((ITickable) tile).update();
            }
            if (entity.worldObj.isRemote)
            {
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                Random rand = entity.worldObj.rand;
                Random random = new Random();

                for (int i = 0; i < 1000; i++)
                {
                    int x = bubble.position.getX() + rand.nextInt(16) - rand.nextInt(16);
                    int y = bubble.position.getY() + rand.nextInt(16) - rand.nextInt(16);
                    int z = bubble.position.getZ() + rand.nextInt(16) - rand.nextInt(16);
                    pos.set(x, y, z);
                    if (bubblePos.squareDistanceTo(new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > bubble.radius * bubble.radius)
                        continue;
                    IBlockState state = entity.worldObj.getBlockState(pos);
                    state.getBlock().randomDisplayTick(entity.worldObj, pos, state, random);
                }
            }
        }
    }

    @Override
    public void stopBurning()
    {
        SpeedBubbles.from(entity.worldObj).remove(bubble);
        bubble = null;
    }
}
