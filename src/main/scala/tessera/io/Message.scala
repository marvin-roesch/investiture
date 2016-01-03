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

import java.lang.{Boolean => JBoolean, Byte => JByte, Double => JDouble, Float => JFloat, Long => JLong, Short =>
JShort}

import _root_.io.netty.buffer.ByteBuf
import tessera._
import tessera.math.{Vec3d, Vec3i}
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.{INetHandler, NetHandlerPlayServer}
import net.minecraft.util.EnumFacing
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.relauncher.Side

import scala.collection.immutable.HashMap
import scala.language.existentials

/**
 * Message
 *
 * @author PaleoCrafter
 */
class Message extends IMessage {
  override def fromBytes(buf: ByteBuf): Unit = {
    for (field <- fields) {
      val serializer = serializers.get(field.getName).orNull
      if (serializer != null) {
        field.setAccessible(true)
        field.set(this, serializer.deserialize(buf))
      }
    }
  }

  override def toBytes(buf: ByteBuf): Unit = {
    for (field <- fields) {
      val serializer = serializers.get(field.getName).orNull
      if (serializer != null) {
        field.setAccessible(true)
        serializer.serialize(field.get(this), buf)
      }
    }
  }

  def serializers = {
    if (_serializers == null) {
      val fields = this.getClass.getDeclaredFields.sortBy(_.getName)
      _serializers = HashMap.empty[String, Message.Serializer[Any]]
      for (field <- fields) {
        field.setAccessible(true)
        val serializer = Message.getSerializer(field.getType)
        _serializers += field.getName -> serializer.orNull.asInstanceOf[Message.Serializer[Any]]
      }
    }
    _serializers
  }

  val fields = this.getClass.getDeclaredFields.sortBy(_.getName)
  var _serializers: Map[String, Message.Serializer[Any]] = null
}

object Message {
  type NetReaction = PartialFunction[(Message, Message.Context), Message]
  private var serializers = HashMap.empty[Class[_], Serializer[Any]]
  addSerializer(classOf[String], new Serializer[String] {
    override def serialize0(target: String, buffer: ByteBuf): Unit = ByteBufUtils.writeUTF8String(buffer, target)

    override def deserialize0(buffer: ByteBuf): String = ByteBufUtils.readUTF8String(buffer)
  })

  addSerializer(Integer.TYPE, new Serializer[Integer] {
    override def serialize0(target: Integer, buffer: ByteBuf): Unit = buffer.writeInt(target)

    override def deserialize0(buffer: ByteBuf): Integer = buffer.readInt()
  })

  addSerializer(JByte.TYPE, new Serializer[JByte] {
    override def serialize0(target: JByte, buffer: ByteBuf): Unit = buffer.writeByte(target.byteValue())

    override def deserialize0(buffer: ByteBuf): JByte = buffer.readByte()
  })

  addSerializer(JShort.TYPE, new Serializer[JShort] {
    override def serialize0(target: JShort, buffer: ByteBuf): Unit = buffer.writeShort(target.shortValue())

    override def deserialize0(buffer: ByteBuf): JShort = buffer.readShort()
  })

  addSerializer(JLong.TYPE, new Serializer[JLong] {
    override def serialize0(target: JLong, buffer: ByteBuf): Unit = buffer.writeLong(target)

    override def deserialize0(buffer: ByteBuf): JLong = buffer.readLong()
  })

  addSerializer(Character.TYPE, new Serializer[Character] {
    override def serialize0(target: Character, buffer: ByteBuf): Unit = buffer.writeChar(target.charValue())

    override def deserialize0(buffer: ByteBuf): Character = buffer.readChar()
  })

  addSerializer(JBoolean.TYPE, new Serializer[JBoolean] {
    override def serialize0(target: JBoolean, buffer: ByteBuf): Unit = buffer.writeBoolean(target)

    override def deserialize0(buffer: ByteBuf): JBoolean = buffer.readBoolean()
  })

  addSerializer(JFloat.TYPE, new Serializer[JFloat] {
    override def serialize0(target: JFloat, buffer: ByteBuf): Unit = buffer.writeFloat(target)

    override def deserialize0(buffer: ByteBuf): JFloat = buffer.readFloat()
  })

  addSerializer(JDouble.TYPE, new Serializer[JDouble] {
    override def serialize0(target: JDouble, buffer: ByteBuf): Unit = buffer.writeDouble(target)

    override def deserialize0(buffer: ByteBuf): JDouble = buffer.readDouble()
  })

  addSerializer(classOf[ItemStack], new Serializer[ItemStack] {
    override def serialize0(target: ItemStack, buffer: ByteBuf): Unit = ByteBufUtils.writeItemStack(buffer, target)

    override def deserialize0(buffer: ByteBuf): ItemStack = ByteBufUtils.readItemStack(buffer)
  })

  addSerializer(classOf[NBTTagCompound], new Serializer[NBTTagCompound] {
    override def serialize0(target: NBTTagCompound, buffer: ByteBuf): Unit = ByteBufUtils.writeTag(buffer, target)

    override def deserialize0(buffer: ByteBuf): NBTTagCompound = ByteBufUtils.readTag(buffer)
  })

  addSerializer(classOf[EnumFacing], new Serializer[EnumFacing] {
    override def serialize0(target: EnumFacing, buffer: ByteBuf): Unit = buffer.writeInt(target.ordinal())

    override def deserialize0(buffer: ByteBuf): EnumFacing = EnumFacing.values()(buffer.readInt())
  })

  addSerializer(classOf[Vec3i], new Serializer[Vec3i] {
    override def serialize0(target: Vec3i, buffer: ByteBuf): Unit = {
      buffer.writeInt(target.x)
      buffer.writeInt(target.y)
      buffer.writeInt(target.z)
    }

    override def deserialize0(buffer: ByteBuf): Vec3i = Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt())
  })

  addSerializer(classOf[Vec3d], new Serializer[Vec3d] {
    override def serialize0(target: Vec3d, buffer: ByteBuf): Unit = {
      buffer.writeDouble(target.x)
      buffer.writeDouble(target.y)
      buffer.writeDouble(target.z)
    }

    override def deserialize0(buffer: ByteBuf): Vec3d =
      Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
  })

  addSerializer(classOf[VVec3i], new Serializer[VVec3i] {
    override def serialize0(target: VVec3i, buffer: ByteBuf): Unit = {
      buffer.writeInt(target.getX)
      buffer.writeInt(target.getY)
      buffer.writeInt(target.getZ)
    }

    override def deserialize0(buffer: ByteBuf): VVec3i =
      new VVec3i(buffer.readInt(), buffer.readInt(), buffer.readInt())
  })

  def serialize[A](target: A, buffer: ByteBuf): Boolean = {
    getSerializer[A](target.getClass.asInstanceOf[Class[A]]) match {
      case Some(s) =>
        s.serialize(target, buffer)
        true
      case _ =>
        false
    }
  }

  def addSerializer[A](clazz: Class[A], serializer: Serializer[A]): Unit = {
    serializers += clazz -> serializer.asInstanceOf[Serializer[Any]]
  }

  def getSerializer[A](clazz: Class[A]): Option[Serializer[A]] = serializers.get(clazz)
    .asInstanceOf[Option[Serializer[A]]] match {
    case s: Some[Serializer[A]] => s
    case None => serializers find {
      e =>
        clazz.isAssignableFrom(e._1)
    } map {
      _._2.asInstanceOf[Serializer[A]]
    }
  }

  class Context(netHandler: INetHandler, val side: Side) {
    def player: EntityPlayer = {
      if (!netHandler.isInstanceOf[NetHandlerPlayServer]) Minecraft.getMinecraft.thePlayer
      else serverHandler.playerEntity
    }

    def schedule(f: => Unit): Unit = {
      val task = new Runnable {
        override def run(): Unit = f
      }
      if (!netHandler.isInstanceOf[NetHandlerPlayServer]) Minecraft.getMinecraft.addScheduledTask(task)
      else serverHandler.playerEntity.worldObj.asInstanceOf[WorldServer].addScheduledTask(task)
    }

    def serverHandler: NetHandlerPlayServer = netHandler.asInstanceOf[NetHandlerPlayServer]

    def clientHandler: NetHandlerPlayClient = netHandler.asInstanceOf[NetHandlerPlayClient]
  }

  trait Serializer[T] {
    def serialize(target: T, buffer: ByteBuf): Unit = {
      buffer.writeByte(if (target != null) 1 else 0)
      if (target != null)
        serialize0(target, buffer)
    }

    def deserialize(buffer: ByteBuf): T = {
      if (buffer.readByte() == 0)
        null.asInstanceOf[T]
      else deserialize0(buffer)
    }

    protected def serialize0(target: T, buffer: ByteBuf)

    protected def deserialize0(buffer: ByteBuf): T
  }

}

