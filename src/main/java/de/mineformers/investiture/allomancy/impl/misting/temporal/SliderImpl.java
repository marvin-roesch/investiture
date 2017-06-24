package de.mineformers.investiture.allomancy.impl.misting.temporal;

import de.mineformers.investiture.allomancy.api.Allomancer;
import de.mineformers.investiture.allomancy.api.misting.Inject;
import de.mineformers.investiture.allomancy.api.misting.temporal.Slider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
        SpeedBubbles.from(entity.world).add(bubble);
    }

    @Override
    public void update()
    {
        if (bubble != null)
        {
            Vec3d bubblePos = new Vec3d(bubble.position.getX() + 0.5, bubble.position.getY(), bubble.position.getZ() + 0.5);
            if (entity.dimension != bubble.dimension || entity.getDistanceSq(bubblePos.x, bubblePos.y, bubblePos.z) > 25)
            {
//                if (!entity.world.isRemote)
//                    allomancer.deactivate(Slider.class);
                return;
            }
            for (BlockPos pos : BlockPos.getAllInBox(bubble.position.add(-bubble.radius, -bubble.radius, -bubble.radius),
                                                     bubble.position.add(bubble.radius, bubble.radius, bubble.radius)))
            {
                if (bubblePos.squareDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > bubble.radius * bubble.radius)
                    continue;
                TileEntity tile = entity.world.getTileEntity(pos);
                if (tile instanceof ITickable)
                    ((ITickable) tile).update();
            }
            if (entity.world.isRemote)
            {
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                Random rand = entity.world.rand;
                Random random = new Random();

                for (int i = 0; i < 1000; i++)
                {
                    int x = bubble.position.getX() + rand.nextInt(16) - rand.nextInt(16);
                    int y = bubble.position.getY() + rand.nextInt(16) - rand.nextInt(16);
                    int z = bubble.position.getZ() + rand.nextInt(16) - rand.nextInt(16);
                    pos.setPos(x, y, z);
                    if (bubblePos.squareDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > bubble.radius * bubble.radius)
                        continue;
                    IBlockState state = entity.world.getBlockState(pos);
                    state.getBlock().randomDisplayTick(state, entity.world, pos, random);
                }
            }
        }
    }

    @Override
    public void stopBurning()
    {
        SpeedBubbles.from(entity.world).remove(bubble);
        bubble = null;
    }
}
