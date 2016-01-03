package de.mineformers.allomancy.internal

import de.mineformers.allomancy.Allomancy
import de.mineformers.allomancy.metal._
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import net.minecraftforge.event.entity.player.{PlayerEvent, PlayerInteractEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent

/**
  * EntityHandler
  *
  * @author PaleoCrafter
  */
class EntityHandler {
  @SubscribeEvent
  def onConstructing(event: EntityConstructing): Unit = {
    event.entity match {
      case p: EntityPlayer =>
        if (MetalBurner(p) == null)
          p.registerExtendedProperties(Allomancy.BurnerId, new EntityMetalBurner)
      case _ =>
    }
  }

  @SubscribeEvent
  def onClone(event: PlayerEvent.Clone): Unit = {
    val oldStorage = MetalBurner(event.original)
    val newStorage = MetalBurner(event.entity)

    newStorage.copy(oldStorage)
  }

  @SubscribeEvent
  def onLogin(event: PlayerLoggedInEvent): Unit = {
    event.player.getExtendedProperties(Allomancy.BurnerId).asInstanceOf[EntityMetalBurner].sync()
  }

  @SubscribeEvent
  def onInteract(event: PlayerInteractEvent): Unit = {
    if (!event.entity.worldObj.isRemote && event.entityPlayer.getHeldItem != null) {
      if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
        val metals = MetalBurner(event.entity)
        if (metals.consume(event.entityPlayer.getHeldItem))
          event.entityPlayer.destroyCurrentEquippedItem()
      }
    }
  }

  @SubscribeEvent
  def onPlayerTick(event: PlayerTickEvent): Unit = {
    if (event.player.worldObj.isRemote || event.phase == TickEvent.Phase.END)
      return

    val metals = MetalBurner(event.player)
    for (metal <- metals.burningMetals) {
      metals.updateBurnTimer(metal)
    }
  }
}
