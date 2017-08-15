package de.mineformers.investiture.allomancy.impl.misting.temporal;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.network.SpeedBubbleUpdate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.*;

/**
 * ${JDOC}
 */
@Mod.EventBusSubscriber(modid = Investiture.MOD_ID)
public class SpeedBubbles extends WorldSavedData implements Iterable<SpeedBubble>
{
    public static final String ID = "Allomancy$SpeedBubbles";
    private World world;

    public static SpeedBubbles from(World world)
    {
        WorldSavedData data = world.getPerWorldStorage().getOrLoadData(SpeedBubbles.class, ID);
        if (data == null)
        {
            data = new SpeedBubbles(ID);
            world.getPerWorldStorage().setData(ID, data);
        }
        SpeedBubbles bubbles = (SpeedBubbles) data;
        bubbles.world = world;
        return bubbles;
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
            return;
        World world = event.world;
        SpeedBubbles bubbles = from(event.world);
        Set<UUID> toRemove = new HashSet<>();
        for (SpeedBubble bubble : bubbles)
        {
            Entity owner = world.getPlayerEntityByUUID(bubble.owner);
            Vec3d bubblePos = new Vec3d(bubble.position.getX() + 0.5, bubble.position.getY(), bubble.position.getZ() + 0.5);
            double maxDistance = bubble.radius + 16;
            double maxDistanceSq = maxDistance * maxDistance;
            if (owner == null || owner.dimension != bubble.dimension || owner.getDistanceSq(bubblePos.x, bubblePos.y, bubblePos.z) > maxDistanceSq)
            {
                if (world.isRemote)
                    toRemove.add(bubble.owner);
                continue;
            }
            if (world.isBlockLoaded(bubble.position))
            {
                TileEntity tile = world.getTileEntity(bubble.position);
                if (tile instanceof ITickable)
                {
                    for (int i = 0; i < 16; i++)
                    {
                        ((ITickable) tile).update();
                    }
                }
                if (world.isRemote)
                {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                    Random rand = world.rand;
                    Random random = new Random();

                    for (int i = 0; i < 1000; i++)
                    {
                        int x = bubble.position.getX() + rand.nextInt(16) - rand.nextInt(16);
                        int y = bubble.position.getY() + rand.nextInt(16) - rand.nextInt(16);
                        int z = bubble.position.getZ() + rand.nextInt(16) - rand.nextInt(16);
                        pos.setPos(x, y, z);
                        if (bubblePos.squareDistanceTo(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)) > bubble.radius * bubble.radius)
                            continue;
                        IBlockState state = world.getBlockState(pos);
                        state.getBlock().randomDisplayTick(state, world, pos, random);
                    }
                }
            }
        }
        for (UUID owner : toRemove)
        {
            bubbles.remove(owner);
        }
    }

    private Map<UUID, SpeedBubble> bubbles = new HashMap<>();

    public SpeedBubbles(String id)
    {
        super(id);
    }

    public void add(Entity owner, BlockPos pos, double radius)
    {
        add(owner.getUniqueID(), world.provider.getDimension(), pos, radius);
    }

    public void add(UUID owner, int dimension, BlockPos pos, double radius)
    {
        SpeedBubble existing = bubbles.get(owner);
        if (!world.isRemote && existing != null)
        {
            from(DimensionManager.getWorld(existing.dimension)).remove(owner);
        }
        SpeedBubble bubble = new SpeedBubble(owner, dimension, pos, radius);
        bubbles.put(bubble.owner, bubble);
        if (!world.isRemote)
            Investiture.net().sendToAll(new SpeedBubbleUpdate(SpeedBubbleUpdate.ACTION_ADD, bubble));
    }

    public void remove(UUID owner)
    {
        SpeedBubble bubble = bubbles.remove(owner);
        if (!world.isRemote)
            Investiture.net().sendToAll(new SpeedBubbleUpdate(SpeedBubbleUpdate.ACTION_REMOVE, bubble));
    }

    @Nullable
    public SpeedBubble get(UUID owner)
    {
        return bubbles.get(owner);
    }

    @Override
    public Iterator<SpeedBubble> iterator()
    {
        return bubbles.values().iterator();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return nbt;
    }
}
