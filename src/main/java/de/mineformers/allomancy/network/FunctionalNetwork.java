package de.mineformers.allomancy.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import io.netty.channel.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Method;
import java.util.EnumMap;

/**
 * FunctionalNetwork
 *
 * @author PaleoCrafter
 */
public class FunctionalNetwork {
    private static Class<?> defaultChannelPipeline;
    private static Method generateName;

    public static FunctionalNetwork create(String channelName) {
        return new FunctionalNetwork(channelName);
    }

    static {
        try {
            defaultChannelPipeline = Class.forName("io.netty.channel.DefaultChannelPipeline");
            generateName = defaultChannelPipeline.getDeclaredMethod("generateName", ChannelHandler.class);
            generateName.setAccessible(true);
        } catch (Exception e) {
            FMLLog.log(Level.FATAL, e, "What? Netty isn't installed, what magic is this?");
            throw Throwables.propagate(e);
        }
    }

    private EnumMap<Side, FMLEmbeddedChannel> channels;
    private SimpleIndexedCodec packetCodec;
    private int lastDiscriminator = 0;

    private FunctionalNetwork(String channelName) {
        packetCodec = new SimpleIndexedCodec();
        channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec);
    }

    private String generateName(ChannelPipeline pipeline, ChannelHandler handler) {
        try {
            return (String) generateName.invoke(defaultChannelPipeline.cast(pipeline), handler);
        } catch (Exception e) {
            FMLLog.log(Level.FATAL, e, "It appears we somehow have a not-standard pipeline. Huh");
            throw Throwables.propagate(e);
        }
    }

    /**
     * Register a message which will have the supplied discriminator byte.
     *
     * @param requestMessageType the message type
     */
    public <IN extends Message> void registerMessage(Class<IN> requestMessageType) {
        registerMessage(requestMessageType, lastDiscriminator++);
    }

    /**
     * Register a message which will have the supplied discriminator byte.
     *
     * @param requestMessageType the message type
     * @param discriminator      a discriminator byte
     */
    public <IN extends Message> void registerMessage(Class<IN> requestMessageType, int discriminator) {
        packetCodec.addDiscriminator(discriminator, requestMessageType);
        Serialization.INSTANCE.registerMessage(requestMessageType);
        if (lastDiscriminator < discriminator)
            lastDiscriminator = discriminator;
    }

    public <IN extends Message, OUT extends Message> void addHandler(Class<IN> type, Side side, Message.Handler<? super IN, ? extends OUT> handler) {
        Wrapper<IN, OUT> wrapped = getHandlerWrapper(handler, side, type);
        FMLEmbeddedChannel channel = channels.get(side);
        String tp = channel.findChannelHandlerNameForType(SimpleIndexedCodec.class);
        channel.pipeline().addAfter(tp, generateName(channel.pipeline(), wrapped), wrapped);
    }

    private <IN extends Message, OUT extends Message> Wrapper<IN, OUT> getHandlerWrapper(Message.Handler<? super IN, ? extends OUT> messageHandler, Side side, Class<IN> requestType) {
        return new Wrapper<>(messageHandler, side, requestType);
    }

    /**
     * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required, such as
     * {@link TileEntity#getDescriptionPacket}.
     *
     * @param message The message to translate into packet form
     * @return A minecraft {@link Packet} suitable for use in minecraft APIs
     */
    public Packet<?> getPacketFrom(IMessage message) {
        return channels.get(Side.SERVER).generatePacketFrom(message);
    }

    /**
     * Send this message to everyone.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    public void sendToAll(IMessage message) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the specified player.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    public void sendTo(IMessage message, EntityPlayerMP player) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param point   The {@link NetworkRegistry.TargetPoint} around which to send
     */
    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    public void sendToDimension(IMessage message, int dimensionId) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the server.
     * The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send
     */
    public void sendToServer(IMessage message) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    private static class Wrapper<IN extends Message, OUT extends Message> extends SimpleChannelInboundHandler<IN> {
        private final Message.Handler<? super IN, ? extends OUT> messageHandler;
        private final Side side;

        public Wrapper(Message.Handler<? super IN, ? extends OUT> handler, Side side, Class<IN> requestType) {
            super(requestType);
            messageHandler = Preconditions.checkNotNull(handler, "IMessageHandler must not be null");
            this.side = side;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, IN msg) throws Exception {
            INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            OUT result = messageHandler.handle(msg, new Message.Context(netHandler, side));
            if (result != null) {
                ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY);
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            FMLLog.log(Level.ERROR, cause, "SimpleChannelHandlerWrapper exception");
            super.exceptionCaught(ctx, cause);
        }
    }
}
