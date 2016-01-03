package tessera.block

import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.BlockState
import net.minecraft.item.Item

import scala.language.{existentials, implicitConversions}

/**
 * BlockConversions
 *
 * @author PaleoCrafter
 */
trait BlockConversions {
  def createState(block: Block, properties: IProperty[_]*) = {
    new BlockState(block, properties.asInstanceOf[Seq[IProperty[T] forSome {type T <: Comparable[T]}]]: _*)
  }

  implicit def blockToItem(block: Block): Item = Item.getItemFromBlock(block)
}
