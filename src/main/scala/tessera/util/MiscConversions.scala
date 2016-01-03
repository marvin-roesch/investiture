package tessera.util

import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, ChatComponentTranslation}
import net.minecraft.world.World

/**
 * WorldConversions
 *
 * @author PaleoCrafter
 */
trait MiscConversions {

  implicit class RichWorld(world: World) {
    def tile[T <: TileEntity](pos: BlockPos) = world.getTileEntity(pos).asInstanceOf[T]
  }

  implicit class Translations(s: String) {
    def chat(args: Any*) = new ChatComponentTranslation(s, args.map(_.asInstanceOf[AnyRef]): _*)
  }

}
