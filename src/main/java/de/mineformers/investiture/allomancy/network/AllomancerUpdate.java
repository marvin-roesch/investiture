package de.mineformers.investiture.allomancy.network;

import com.google.common.base.Throwables;
import de.mineformers.investiture.allomancy.api.misting.Misting;
import de.mineformers.investiture.network.ManualTranslation;
import de.mineformers.investiture.network.Message;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * ${JDOC}
 */
public class AllomancerUpdate extends Message
{
    public int entityId;
    @ManualTranslation
    public Set<Class<? extends Misting>> activePowers;

    public AllomancerUpdate()
    {
    }

    public AllomancerUpdate(int entityId, Set<Class<? extends Misting>> activePowers)
    {
        this.entityId = entityId;
        this.activePowers = activePowers;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        activePowers = new HashSet<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++)
        {
            try
            {
                activePowers.add((Class<? extends Misting>) Class.forName(ByteBufUtils.readUTF8String(buf)));
            }
            catch (ClassNotFoundException e)
            {
                Throwables.propagate(e);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);
        buf.writeInt(activePowers.size());
        for (Class<? extends Misting> m : activePowers)
            ByteBufUtils.writeUTF8String(buf, m.getName());
    }
}
