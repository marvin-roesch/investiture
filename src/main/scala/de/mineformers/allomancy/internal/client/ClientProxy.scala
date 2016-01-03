package de.mineformers.allomancy.internal.client

import de.mineformers.allomancy.block.AllomanticMetalOre
import de.mineformers.allomancy.internal.client.gui.MetalHUD
import de.mineformers.allomancy.internal.network.{EntityMetalBurnerUpdate, EntityMetalStorageUpdate}
import de.mineformers.allomancy.item.AllomanticMetalIngot
import de.mineformers.allomancy.metal.{MetalBurner, MetalStorage}
import de.mineformers.allomancy.{Allomancy, Proxy}
import net.minecraft.client.renderer.ItemMeshDefinition
import net.minecraft.client.resources.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemStack
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.relauncher.Side
import org.lwjgl.input.Keyboard
import tessera._

/**
  * ClientProxy
  *
  * @author PaleoCrafter
  */
class ClientProxy extends Proxy {
  override def preInit(event: FMLPreInitializationEvent): Unit = {
    MinecraftForge.EVENT_BUS.register(new MetalHUD)
    Allomancy.Net.addHandler({
      case (EntityMetalStorageUpdate(entityId, storage), ctx) =>
        val entity = ctx.player.worldObj.getEntityByID(entityId)
        MetalStorage(entity).copy(storage)
        null
      case (EntityMetalBurnerUpdate(entityId, storage), ctx) =>
        if (ctx.player != null) {
          val entity = ctx.player.worldObj.getEntityByID(entityId)
          MetalBurner(entity).copy(storage)
        }
        null
    }, Side.CLIENT)

    ModelLoader.setCustomMeshDefinition(AllomanticMetalIngot, new ItemMeshDefinition {
      final val resources = AllomanticMetalIngot.Names.map(n =>
        new ModelResourceLocation(Allomancy.ModId + ":allomantic_metal_ingot", "metal=" + n))

      override def getModelLocation(stack: ItemStack): ModelResourceLocation =
        resources(stack.getItemDamage max 0 min (resources.length - 1))
    })
    ModelBakery.registerItemVariants(AllomanticMetalIngot, AllomanticMetalIngot.Names.map(n =>
      new ModelResourceLocation(Allomancy.ModId + ":allomantic_metal_ingot", "metal=" + n)): _*)

    ModelLoader.setCustomMeshDefinition(AllomanticMetalOre, new ItemMeshDefinition {
      final val resources = AllomanticMetalOre.Names.map(n =>
        new ModelResourceLocation(Allomancy.ModId + ":allomantic_metal_ore", "metal=" + n))

      override def getModelLocation(stack: ItemStack): ModelResourceLocation =
        resources(stack.getItemDamage max 0 min (resources.length - 1))
    })
    ModelBakery.registerItemVariants(AllomanticMetalOre, AllomanticMetalOre.Names.map(n =>
      new ModelResourceLocation(Allomancy.ModId + ":allomantic_metal_ore", "metal=" + n)): _*)
  }

  override def init(event: FMLInitializationEvent): Unit = {
    ClientRegistry.registerKeyBinding(KeyBindings.ShowDial)
  }
}

object KeyBindings {
  var ShowDial = new KeyBinding("key.showDial", Keyboard.KEY_F, "key.categories.allomancy")
}
