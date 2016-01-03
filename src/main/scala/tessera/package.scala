import net.minecraftforge.common.MinecraftForge
import tessera.block.BlockConversions
import tessera.math.MathConversions
import tessera.util.MiscConversions

/**
  * package
  *
  * @author PaleoCrafter
  */
package object tessera
  extends MathConversions
    with BlockConversions
    with MiscConversions
    with Scheduling {
  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(scheduler.events)
  }
}