package de.mineformers.investiture.network;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import de.mineformers.investiture.serialisation.Serialisation;
import io.netty.channel.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
 * Provides packet handling with a functional API.
 * Messages and handlers get registered separately and a discriminator does not have to be provided.
 * <p>
 * Acquire an instance through {@link FunctionalNetwork#create(String)}.
 * <p>
 * Most of this is a straight copy from Forge's {@link net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper simpleimpl}, so
 * credits go to cpw.
 */
public class FunctionalNetwork
{
    /**
     * Creates a network with a specific channel name.
     *
     * @param channelName the name of the channel to use
     * @return a network ready for registering messages etc.
     */
    public static FunctionalNetwork create(String channelName)
    {
        return new FunctionalNetwork(channelName);
    }

    // Copied from Forge, new in 1.8
    // Seems to have to do with generating names for the packet handlers
    private static Class<?> defaultChannelPipeline;
    private static Method generateName;

    static
    {
        try
        {
            defaultChannelPipeline = Class.forName("io.netty.channel.DefaultChannelPipeline");
            generateName = defaultChannelPipeline.getDeclaredMethod("generateName", ChannelHandler.class);
            generateName.setAccessible(true);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.FATAL, e, "What? Netty isn't installed, what magic is this?");
            throw Throwables.propagate(e);
        }
    }

    private EnumMap<Side, FMLEmbeddedChannel> channels;
    private SimpleIndexedCodec packetCodec;
    private int lastDiscriminator = 0;

    /**
     * Creates an instance of the class. Private because {@link FunctionalNetwork#create(String)} is the way to acquire an instance.
     *
     * @param channelName the name of the channel
     */
    private FunctionalNetwork(String channelName)
    {
        packetCodec = new SimpleIndexedCodec();
        channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec);
    }

    /**
     * Generates a name for a given handler.
     *
     * @param pipeline the pipeline used for generating the name
     * @param handler  the handler to generate a name for
     * @return a name for the handler
     */
    private String generateName(ChannelPipeline pipeline, ChannelHandler handler)
    {
        try
        {
            return (String) generateName.invoke(defaultChannelPipeline.cast(pipeline), handler);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.FATAL, e, "It appears we somehow have a not-standard pipeline. Huh");
            throw Throwables.propagate(e);
        }
    }

    /**
     * Register a message which will take the next discriminator byte available.
     *
     * @param type the message type
     */
    public <IN extends Message> void registerMessage(Class<IN> type)
    {
        registerMessage(type, lastDiscriminator++);
    }

    /**
     * Register a message which will have the supplied discriminator byte.
     *
     * @param type          the message type
     * @param discriminator a discriminator byte
     */
    public <IN extends Message> void registerMessage(Class<IN> type, int discriminator)
    {
        packetCodec.addDiscriminator(discriminator, type);
        Serialisation.INSTANCE.registerClass(type, false);
        if (lastDiscriminator < discriminator) lastDiscriminator = discriminator;
    }

    /**
     * Adds a handler for a message.
     *
     * @param type    the class of the message
     * @param side    the side the handler is supposed to run on
     * @param handler the handler
     */
    public <IN extends Message, OUT extends Message> void addHandler(Class<IN> type, Side side, Message.Handler<? super IN, ? extends OUT> handler)
    {
        Wrapper<IN, OUT> wrapped = new Wrapper<>(handler, side, type);
        FMLEmbeddedChannel channel = channels.get(side);
        String tp = channel.findChannelHandlerNameForType(SimpleIndexedCodec.class);
        channel.pipeline().addAfter(tp, generateName(channel.pipeline(), wrapped), wrapped);
    }

    /**
     * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required, such as
     * {@link TileEntity#getUpdatePacket()}.
     *
     * @param message The message to translate into packet form
     * @return A minecraft {@link Packet} suitable for use in minecraft APIs
     */
    public Packet<?> getPacketFrom(Message message)
    {
        return channels.get(Side.SERVER).generatePacketFrom(message);
    }

    /**
     * Send this message to everyone.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    public void sendToAll(IMessage message)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to the specified player.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param player  The player to send it to
     * @param message The message to send
     */
    public void sendTo(EntityPlayerMP player, Message message)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param point   The {@link NetworkRegistry.TargetPoint} around which to send
     * @param message The message to send
     */
    public void sendToAllAround(NetworkRegistry.TargetPoint point, Message message)
    {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param dimensionId The dimension id to target
     * @param message     The message to send
     */
    public void sendToDimension(int dimensionId, Message message)
    {
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
    public void sendToServer(Message message)
    {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    /**
     * Send the description message for a particular packet to all players watching its chunk.
     * Note that this will only work on the server.
     *
     * @param tileEntity the tile entity whose description packet to send
     */
    public void sendDescription(TileEntity tileEntity)
    {
        SPacketUpdateTileEntity packet = tileEntity.getUpdatePacket();
        if (packet == null)
            return;
        if (tileEntity.getWorld() instanceof WorldServer)
        {
            PlayerChunkMap manager = ((WorldServer) tileEntity.getWorld()).getPlayerChunkMap();
            for (EntityPlayer player : tileEntity.getWorld().playerEntities)
                if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, tileEntity.getPos().getX() >> 4, tileEntity.getPos().getZ() >> 4))
                    ((EntityPlayerMP) player).connection.sendPacket(packet);
        }
    }

    /**
     * Send a message to all players watching the chunk at a given position.
     *
     * @param world   the world the chunk is in
     * @param pos     a position belonging to the chunk
     * @param message the message to send
     */
    public void sendToWatching(World world, BlockPos pos, Message message)
    {
        if (world instanceof WorldServer)
        {
            PlayerChunkMap manager = ((WorldServer) world).getPlayerChunkMap();
            for (EntityPlayer player : world.playerEntities)
                if (manager.isPlayerWatchingChunk((EntityPlayerMP) player, pos.getX() >> 4, pos.getZ() >> 4))
                    sendTo((EntityPlayerMP) player, message);
        }
    }

    /**
     * Send a message to all players tracking a given entity.
     *
     * @param entity  the entity to check for tracking
     * @param message the message to send
     */
    public void sendToTracking(Entity entity, Message message)
    {
        if (entity.world instanceof WorldServer)
        {
            EntityTracker tracker = ((WorldServer) entity.world).getEntityTracker();
            for (EntityPlayer p : tracker.getTrackingPlayers(entity))
                sendTo((EntityPlayerMP) p, message);
            if (entity instanceof EntityPlayerMP)
                sendTo((EntityPlayerMP) entity, message);
        }
    }

    /**
     * Internal class acting as interface between {@link de.mineformers.investiture.network.Message.Handler Handlers} and Netty.
     */
    private static class Wrapper<IN extends Message, OUT extends Message> extends SimpleChannelInboundHandler<IN>
    {
        private final Message.Handler<? super IN, ? extends OUT> messageHandler;
        private final Side side;

        public Wrapper(Message.Handler<? super IN, ? extends OUT> handler, Side side, Class<IN> requestType)
        {
            super(requestType);
            messageHandler = Preconditions.checkNotNull(handler, "IMessageHandler must not be null");
            this.side = side;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, IN msg) throws Exception
        {
            INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
            OUT result = messageHandler.handle(msg, new Message.Context(netHandler, side));
            if (result != null)
            {
                ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY);
                ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
        {
            FMLLog.log(Level.ERROR, cause, "SimpleChannelHandlerWrapper exception");
            super.exceptionCaught(ctx, cause);
        }
    }
}
