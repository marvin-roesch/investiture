package tessera.client.ui

import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.collection.mutable.ListBuffer

/**
 * Overlay
 *
 * @author PaleoCrafter
 */
trait Overlay extends DelayedInit {
  private val initCode = new ListBuffer[() => Unit]
  private var _event: RenderGameOverlayEvent = _

  def event = _event

  def register(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  override def delayedInit(body: => Unit) {
    initCode += (() => body)
  }

  @SubscribeEvent
  def onRender(event: RenderGameOverlayEvent.Pre): Unit = {
    _event = event
    for (proc <- initCode) proc()
  }
}
