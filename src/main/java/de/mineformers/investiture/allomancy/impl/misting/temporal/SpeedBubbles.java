package de.mineformers.investiture.allomancy.impl.misting.temporal;

import de.mineformers.investiture.Investiture;
import de.mineformers.investiture.allomancy.network.SpeedBubbleUpdate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * ${JDOC}
 */
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

    private Set<SpeedBubble> bubbles = new HashSet<>();

    public SpeedBubbles(String id)
    {
        super(id);
    }

    public void add(SpeedBubble bubble)
    {
        bubbles.add(bubble);
        if (!world.isRemote)
            Investiture.net().sendToAll(new SpeedBubbleUpdate(SpeedBubbleUpdate.ACTION_REMOVE, bubble));
    }

    public void remove(SpeedBubble bubble)
    {
        bubbles.remove(bubble);
        if (!world.isRemote)
            Investiture.net().sendToAll(new SpeedBubbleUpdate(SpeedBubbleUpdate.ACTION_REMOVE, bubble));
    }

    @Override
    public Iterator<SpeedBubble> iterator()
    {
        return bubbles.iterator();
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
