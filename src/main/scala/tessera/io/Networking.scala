/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MineFormers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tessera.io

import com.google.common.base.Throwables
import com.google.common.collect.Maps
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.Packet
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec
import net.minecraftforge.fml.common.network.{FMLOutboundHandler, NetworkRegistry}
import net.minecraftforge.fml.relauncher.Side
import tessera.io.Message.NetReaction
import tessera.item.{ItemScroll, ScrollControl}

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scala.util.control.Breaks

/**
  * MFCodec
  *
  * @author PaleoCrafter
  */
object Networking {
  def apply(channelName: String): Networking = {
    val net = new Networking(channelName)
    net.register[ItemScroll]()
    net.addHandler({
      case (ItemScroll(amount), ctx) =>
        ctx.schedule {
          val stack = ctx.player.getHeldItem
          if (stack == null) {}
          else {
            stack.getItem match {
              case scroll: ScrollControl => scroll.onScroll(ctx.player, stack, amount)
              case _ =>
            }
          }
        }
        null
    }, Side.SERVER)
    net
  }
}

class Networking private(channelName: String) {
  private val packetCodec = new SimpleIndexedCodec
  private val channels = NetworkRegistry.INSTANCE.newChannel(channelName, packetCodec)
  private val handlers = {
    val result = Maps.newEnumMap[Side, PartialFunctionChannelHandler](classOf[Side])
    for (side <- Side.values) {
      val handler = new PartialFunctionChannelHandler(side)
      result.put(side, handler)
      val channel = channels.get(side)
      channel.pipeline
        .addAfter(channel.findChannelHandlerNameForType(classOf[SimpleIndexedCodec]), handler.toString, handler)
    }
    result
  }
  private var lastDiscriminator: Byte = 0

  def register[M <: Message]()(implicit ev: ClassTag[M]): Unit = {
    packetCodec.addDiscriminator(lastDiscriminator, ev.runtimeClass.asInstanceOf[Class[M]])
    lastDiscriminator = (lastDiscriminator + 1).toByte
  }

  def register[M <: Message](discriminator: Byte)(implicit ev: ClassTag[M]): Unit = {
    packetCodec.addDiscriminator(discriminator, ev.runtimeClass.asInstanceOf[Class[M]])
    if (lastDiscriminator < discriminator)
      lastDiscriminator = discriminator
  }

  def addHandler(reaction: NetReaction, side: Side): Unit = {
    handlers.get(side).addReaction(reaction)
  }

  /**
    * Construct a minecraft packet from the supplied message. Can be used where minecraft packets are required, such as
    * [[net.minecraft.tileentity.TileEntity.]].
    *
    * @param message The message to translate into packet form
    * @return A minecraft { @link Packet} suitable for use in minecraft APIs
    */
  def getPacketFrom(message: Message): Packet[_] = channels.get(Side.SERVER).generatePacketFrom(message)

  /**
    * Send this message to everyone.
    * The reaction for this message type should be on the CLIENT side.
    *
    * @param message The message to send
    */
  def sendToAll(message: Message) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
    * Send this message to the specified player.
    * The reaction for this message type should be on the CLIENT side.
    *
    * @param message The message to send
    * @param player  The player to send it to
    */
  def sendTo(message: Message, player: EntityPlayerMP) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
    * Send this message to everyone within a certain range of a point.
    * The reaction for this message type should be on the CLIENT side.
    *
    * @param message The message to send
    * @param point   The { @link TargetPoint} around which to send
    */
  def sendToAllAround(message: Message, point: NetworkRegistry.TargetPoint) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
      .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point)
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
    * Send this message to everyone within the supplied dimension.
    * The reaction for this message type should be on the CLIENT side.
    *
    * @param message     The message to send
    * @param dimensionId The dimension id to target
    */
  def sendToDimension(message: Message, dimensionId: Int) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
      .set(FMLOutboundHandler.OutboundTarget.DIMENSION)
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(Integer.valueOf(dimensionId))
    channels.get(Side.SERVER).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  /**
    * Send this message to the server.
    * The reaction for this message type should be on the SERVER side.
    *
    * @param message The message to send
    */
  def sendToServer(message: Message) {
    channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER)
    channels.get(Side.CLIENT).writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

  class PartialFunctionChannelHandler(side: Side) extends SimpleChannelInboundHandler[Message](classOf[Message]) {
    private val handlers = ListBuffer.empty[NetReaction]

    def addReaction(reaction: NetReaction): Unit = {
      handlers += reaction
    }

    override def channelRead0(ctx: ChannelHandlerContext, msg: Message): Unit = {
      val iNetHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get
      val context = new Message.Context(iNetHandler, side)
      val param = (msg, context)
      var result: Message = null
      Breaks.breakable {
        for (handler <- handlers) {
          if (handler.isDefinedAt(param)) {
            result = handler(param)
          }
        }
      }
      if (result != null) {
        ctx.writeAndFlush(result).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      Throwables.propagate(cause)
      super.exceptionCaught(ctx, cause)
    }
  }

}
