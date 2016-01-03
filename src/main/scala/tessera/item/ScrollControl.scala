package tessera.item

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.input.Keyboard
import tessera.io.{Message, Networking}

/**
  * ScrollControl
  *
  * @author PaleoCrafter
  */
trait ScrollControl {
  this: Item =>
  def onScroll(player: EntityPlayer, stack: ItemStack, amount: Int): Unit

  @SideOnly(Side.CLIENT)
  def activated(player: EntityPlayer, stack: ItemStack): Boolean = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
}

case class ItemScroll(var amount: Int) extends Message {
  def this() = this(0)
}

object ScrollHandler {
  private[this] var network: Networking = _
  private[this] var registered = false

  def register(net: Networking): Unit = {
    if (!registered) {
      MinecraftForge.EVENT_BUS.register(this)
      this.network = net
    }
    registered = true
  }

  @SubscribeEvent
  def onMouseInput(event: MouseEvent): Unit = {
    val player = Minecraft.getMinecraft.thePlayer
    val stack = player.getHeldItem

    if (stack != null && event.dwheel != 0) {
      stack.getItem match {
        case scroll: ScrollControl if scroll.activated(player, stack) =>
          network.sendToServer(ItemScroll(event.dwheel / 120))
          event.setCanceled(true)
        case _ =>
      }
    }
  }
}