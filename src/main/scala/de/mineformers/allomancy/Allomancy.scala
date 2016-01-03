package de.mineformers.allomancy

import de.mineformers.allomancy.block.AllomanticMetalOre
import de.mineformers.allomancy.internal.EntityHandler
import de.mineformers.allomancy.internal.network.{EntityMetalBurnerUpdate, EntityMetalStorageUpdate, ToggleBurningMetal}
import de.mineformers.allomancy.item.AllomanticMetalIngot
import de.mineformers.allomancy.metal.{AllomanticMetals, MetalBurner, MetalStorage}
import de.mineformers.allomancy.world.MetalGenerator
import io.netty.buffer.ByteBuf
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import tessera.io.Message.Serializer
import tessera.io.{Message, Networking}

@Mod(modid = Allomancy.ModId, name = "Allomancy", version = Allomancy.ModVersion, modLanguage = "scala")
object Allomancy {
  final val ModId = "allomancy"
  final val ModVersion = "1.0.0"
  final val StorageId = "allomancy_metal_storage"
  final val BurnerId = "allomancy_metal_burner"
  final lazy val Net = Networking(ModId)
  final val CreativeTab = new CreativeTabs(CreativeTabs.getNextID, ModId) {
    @SideOnly(Side.CLIENT)
    override def getTabIconItem: Item = AllomanticMetalIngot

    @SideOnly(Side.CLIENT)
    override def getIconItemDamage: Int = 8
  }

  @SidedProxy(clientSide = "de.mineformers.allomancy.internal.client.ClientProxy",
    serverSide = "de.mineformers.allomancy.internal.client.ServerProxy")
  var proxy: Proxy = null

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    GameRegistry.registerItem(AllomanticMetalIngot, "allomantic_metal_ingot")
    GameRegistry.registerBlock(AllomanticMetalOre, classOf[AllomanticMetalOre.OreItemBlock], "allomantic_metal_ore")
//    GameRegistry.registerWorldGenerator(new MetalGenerator, 0)
    AllomanticMetals.init()
    proxy.preInit(event)
    tessera.init()
    MinecraftForge.EVENT_BUS.register(new EntityHandler)
    Message.addSerializer(classOf[MetalStorage], new Serializer[MetalStorage] {
      override protected def serialize0(target: MetalStorage, buffer: ByteBuf): Unit = {
        buffer.writeInt(target.consumedMetals.size)
        for ((metal, amount) <- target.consumedMetals) {
          ByteBufUtils.writeUTF8String(buffer, metal.id)
          buffer.writeInt(amount)
        }

        buffer.writeInt(target.impurities)
      }

      override protected def deserialize0(buffer: ByteBuf): MetalStorage = {
        val storage = new MetalStorage
        for (i <- 0 until buffer.readInt())
          storage.store(AllomanticMetals(ByteBufUtils.readUTF8String(buffer)).get, buffer.readInt())

        storage.addImpurity(buffer.readInt())
        storage
      }
    })

    Message.addSerializer(classOf[MetalBurner], new Serializer[MetalBurner] {
      override protected def serialize0(target: MetalBurner, buffer: ByteBuf): Unit = {
        buffer.writeInt(target.consumedMetals.size)
        for ((metal, amount) <- target.consumedMetals) {
          ByteBufUtils.writeUTF8String(buffer, metal.id)
          buffer.writeInt(amount)
        }

        buffer.writeInt(target.burnTimers.size)
        for ((metal, time) <- target.burnTimers) {
          ByteBufUtils.writeUTF8String(buffer, metal.id)
          buffer.writeInt(time)
        }

        buffer.writeInt(target.impurities)
      }

      override protected def deserialize0(buffer: ByteBuf): MetalBurner = {
        val burner = new MetalBurner
        for (i <- 0 until buffer.readInt())
          burner.store(AllomanticMetals(ByteBufUtils.readUTF8String(buffer)).get, buffer.readInt())

        for (i <- 0 until buffer.readInt())
          burner.setBurnTimer(AllomanticMetals(ByteBufUtils.readUTF8String(buffer)).get, buffer.readInt())

        burner.addImpurity(buffer.readInt())
        burner
      }
    })
    Net.register[EntityMetalStorageUpdate]
    Net.register[EntityMetalBurnerUpdate]
    Net.register[ToggleBurningMetal]

    Net.addHandler({
      case (ToggleBurningMetal(metal: String), ctx) =>
        val burner = MetalBurner(ctx.player)
        val m = AllomanticMetals(metal)
        if (m.isDefined) {
          if (burner.burning(m.get))
            burner.stopBurning(m.get)
          else
            burner.startBurning(m.get)
        }
        null
    }, Side.SERVER)
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    proxy.init(event)
  }
}

trait Proxy {
  def preInit(event: FMLPreInitializationEvent) = ()

  def init(event: FMLInitializationEvent) = ()
}