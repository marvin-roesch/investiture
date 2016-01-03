package de.mineformers.allomancy.network;

import com.google.common.collect.FluentIterable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Message
 *
 * @author PaleoCrafter
 */
public class Message implements IMessage {
    public static <T> void registerTranslator(Class<T> type, Translator<? super T> translator) {
        Serialization.INSTANCE.addTranslator(type, translator);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        Serialization.INSTANCE.deserializeTo(buf, this);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        Serialization.INSTANCE.serializeFrom(this, buf);
    }

    public interface Translator<T> {
        @SuppressWarnings("unchecked")
        default void serialize(Object value, ByteBuf buffer) {
            buffer.writeBoolean(value != null);
            if (value != null)
                serializeImpl((T) value, buffer);
        }


        default T deserialize(ByteBuf buffer) {
            if (!buffer.readBoolean())
                return null;
            else
                return deserializeImpl(buffer);
        }

        void serializeImpl(T value, ByteBuf buffer);

        T deserializeImpl(ByteBuf buffer);
    }

    public static class Context {
        private final INetHandler netHandler;
        public final Side side;

        Context(INetHandler netHandler, Side side) {
            this.netHandler = netHandler;
            this.side = side;
        }

        public EntityPlayer player() {
            if (!(netHandler instanceof NetHandlerPlayServer))
                return Minecraft.getMinecraft().thePlayer;
            else
                return serverHandler().playerEntity;
        }

        @SideOnly(Side.CLIENT)
        public NetHandlerPlayClient clientHandler() {
            return (NetHandlerPlayClient) netHandler;
        }

        public NetHandlerPlayServer serverHandler() {
            return ((NetHandlerPlayServer) netHandler);
        }
    }

    public interface Handler<IN extends Message, OUT extends Message> {
        OUT handle(IN message, Context ctx);
    }
}
