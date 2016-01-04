package de.mineformers.investiture.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Message
 *
 * @author PaleoCrafter
 */
public class Message implements IMessage
{
    public static <T> void registerTranslator(Class<T> type, Translator<? super T> translator)
    {
        Serialisation.INSTANCE.addTranslator(type, translator);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        Serialisation.INSTANCE.deserializeTo(buf, this);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        Serialisation.INSTANCE.serializeFrom(this, buf);
    }

    public interface Translator<T>
    {
        @SuppressWarnings("unchecked")
        default void serialise(Object value, ByteBuf buffer)
        {
            buffer.writeBoolean(value != null);
            if (value != null)
                serialiseImpl((T) value, buffer);
        }

        default T deserialise(ByteBuf buffer)
        {
            if (!buffer.readBoolean())
                return null;
            else
                return deserialiseImpl(buffer);
        }

        void serialiseImpl(T value, ByteBuf buffer);

        T deserialiseImpl(ByteBuf buffer);
    }

    public static class Context
    {
        private final INetHandler netHandler;
        public final Side side;

        Context(INetHandler netHandler, Side side)
        {
            this.netHandler = netHandler;
            this.side = side;
        }

        public EntityPlayer player()
        {
            if (!(netHandler instanceof NetHandlerPlayServer))
                return Minecraft.getMinecraft().thePlayer;
            else
                return serverHandler().playerEntity;
        }

        public void schedule(Runnable f)
        {
            if (!(netHandler instanceof NetHandlerPlayServer))
                Minecraft.getMinecraft().addScheduledTask(f);
            else
                ((WorldServer) serverHandler().playerEntity.worldObj).addScheduledTask(f);
        }

        @SideOnly(Side.CLIENT)
        public NetHandlerPlayClient clientHandler()
        {
            return (NetHandlerPlayClient) netHandler;
        }

        public NetHandlerPlayServer serverHandler()
        {
            return ((NetHandlerPlayServer) netHandler);
        }
    }

    public interface Handler<IN extends Message, OUT extends Message>
    {
        OUT handle(IN message, Context ctx);
    }
}
