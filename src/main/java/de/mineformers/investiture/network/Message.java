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
 * A message is the means by which the network transfers information.
 * The requirements for a working message are that it has a public no-argument construct, only non-final fields and these fields must be
 * translatable by the framework.
 * If there is no translator for a field type, the mod can provide one through {@link #registerTranslator(Class, Translator)}.
 */
public class Message implements IMessage
{
    /**
     * Registers a translator to the serialisation framework.
     *
     * @param type       the highest type the translator supports.
     * @param translator the translator for the given type
     */
    public static <T> void registerTranslator(Class<T> type, Translator<? super T> translator)
    {
        Serialisation.INSTANCE.addTranslator(type, translator);
    }

    /**
     * Deserialises the contents of a byte buffer into this message.
     *
     * @param buf the buffer to read the data from
     */
    @Override
    public final void fromBytes(ByteBuf buf)
    {
        // Use the internal method for efficient deserialisation
        Serialisation.INSTANCE.deserialiseTo(buf, this);
    }

    /**
     * Serialises this message into a byte buffer.
     *
     * @param buf the buffer to write the data to
     */
    @Override
    public final void toBytes(ByteBuf buf)
    {
        // Use the internal method for efficient serialisation
        Serialisation.INSTANCE.serialiseFrom(this, buf);
    }

    /**
     * A translator reads and writes a type to a byte buffer.
     */
    public interface Translator<T>
    {
        /**
         * Serialise a given object and write it to a buffer.
         * The object's type must be supported by the translator.
         *
         * @param value  the object to serialise
         * @param buffer the buffer the data is to be written to
         */
        @SuppressWarnings("unchecked")
        default void serialise(Object value, ByteBuf buffer)
        {
            // Sort of unnecessary for primitives, but unfortunately the only way of doing this without a lot of special casing.
            buffer.writeBoolean(value != null);
            if (value != null)
                serialiseImpl((T) value, buffer);
        }

        /**
         * Deserialises the data from a byte buffer and turn it into an object.
         *
         * @param buffer the buffer to read the data from
         * @return an object with the buffer's data
         */
        default T deserialise(ByteBuf buffer)
        {
            // See the serialise method
            if (!buffer.readBoolean())
                return null;
            else
                return deserialiseImpl(buffer);
        }

        /**
         * Implementation of the serialisation process.
         * WARNING: This should not be called by anyone as it is an internal implementation detail.
         * Calling this method with a null value will most likely result in a crash.
         *
         * @param value  the object to serialise
         * @param buffer the buffer the data is to be written to
         */
        void serialiseImpl(T value, ByteBuf buffer);

        /**
         * Implementation of the deserialisation process.
         * WARNING: This should not be called by anyone as it is an internal implementation detail.
         * Calling this method directly will most likely result in a crash as the 'null' switch is ignored.
         *
         * @param buffer the buffer to read the data from
         * @return an object with the buffer's data
         */
        T deserialiseImpl(ByteBuf buffer);
    }

    /**
     * The context of a message mainly gives information about the side the message is received on.
     * One can deduce the affected player from this information. Additionally, the context provides the means of scheduling a task on the
     * respective side.
     */
    public static class Context
    {
        private final INetHandler netHandler;
        public final Side side;

        /**
         * Create a new context. Package-local because instantiation of this class is an implementation detail.
         *
         * @param netHandler the net handler specific to the message's receiving side
         * @param side       the side the message is received on
         */
        Context(INetHandler netHandler, Side side)
        {
            this.netHandler = netHandler;
            this.side = side;
        }

        /**
         * @return the player affected by this message
         */
        public EntityPlayer player()
        {
            if (!(netHandler instanceof NetHandlerPlayServer))
                return Minecraft.getMinecraft().thePlayer;
            else
                return serverHandler().playerEntity;
        }

        /**
         * Schedules a task for later execution on the specific side's scheduler.
         * This should be required for most messages as their handler will affect the game's world.
         * Due to the fact that messages are handled on a separate thread, though, scheduling the task is required.
         *
         * @param f the task to schedule
         */
        public void schedule(Runnable f)
        {
            if (!(netHandler instanceof NetHandlerPlayServer))
                Minecraft.getMinecraft().addScheduledTask(f);
            else
                ((WorldServer) serverHandler().playerEntity.worldObj).addScheduledTask(f);
        }

        /**
         * WARNING: Do not call this on the server or the game will crash!
         *
         * @return the client-specific network handler
         */
        @SideOnly(Side.CLIENT)
        public NetHandlerPlayClient clientHandler()
        {
            return (NetHandlerPlayClient) netHandler;
        }

        /**
         * WARNING: Do not call this on the client or the game will crash!
         *
         * @return the server-specific network handler
         */
        public NetHandlerPlayServer serverHandler()
        {
            return ((NetHandlerPlayServer) netHandler);
        }
    }

    /**
     * SAM interface to allow message handling with lambdas.
     */
    public interface Handler<IN extends Message, OUT extends Message>
    {
        /**
         * Handle a given message in a given context and optionally send another message as answer.
         *
         * @param message the message to handle
         * @param ctx the context the message was received in
         * @return another message to answer the received one or null if no answer is intended
         */
        OUT handle(IN message, Context ctx);
    }
}
