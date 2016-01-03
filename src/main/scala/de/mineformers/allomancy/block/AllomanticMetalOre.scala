package de.mineformers.allomancy.block

import java.util

import de.mineformers.allomancy.block.properties.PropertyString
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.util.EnumWorldBlockLayer
import net.minecraftforge.fml.relauncher.{SideOnly, Side}
import tessera._

/**
  * AllomanticMetalOre
  *
  * @author PaleoCrafter
  */
object AllomanticMetalOre extends Block(Material.rock) {
  lazy final val Names = Seq("copper", "zinc", "tin", "aluminium", "chromium")
  lazy final val Metal = new PropertyString("metal", Names: _*)

  setDefaultState(blockState.getBaseState.withProperty(Metal, Names.head))
  setUnlocalizedName("allomantic_metal_ore")
  setCreativeTab(CreativeTabs.tabBlock)

  def fromMetal(metal: String) = getDefaultState.withProperty(Metal, metal)

  @SideOnly(Side.CLIENT)
  override def getSubBlocks(itemIn: Item, tab: CreativeTabs, list: util.List[ItemStack]): Unit = {
    for (dmg <- Names.indices)
      list.add(new ItemStack(itemIn, 1, dmg))
  }

  @SideOnly(Side.CLIENT)
  override def getBlockLayer: EnumWorldBlockLayer = EnumWorldBlockLayer.CUTOUT

  override def damageDropped(state: IBlockState): Int = getMetaFromState(state)

  override def getMetaFromState(state: IBlockState): Int = Names.indexOf(state.getValue(Metal))

  override def getStateFromMeta(meta: Int): IBlockState =
    getDefaultState
      .withProperty(Metal, Names(meta max 0 min (Names.length - 1)))

  override def getHarvestTool(state: IBlockState): String = null

  override def createBlockState(): BlockState = createState(this, Metal)

  class OreItemBlock(block: Block) extends ItemBlock(block) {
    setHasSubtypes(true)

    override def getMetadata(damage: Int): Int = damage

    override def getUnlocalizedName(stack: ItemStack): String =
      s"tile.${Names(stack.getItemDamage max 0 min (Names.length - 1))}_ore"
  }

}
