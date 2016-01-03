package de.mineformers.allomancy.item

import java.util

import de.mineformers.allomancy.Allomancy
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector
import net.minecraftforge.fml.relauncher.{SideOnly, Side}

/**
  * ItemAllomanticMetal
  *
  * @author PaleoCrafter
  */
object AllomanticMetalIngot extends Item {
  final val Names = Seq("bronze", "brass", "copper", "zinc", "tin", "pewter", "steel",
    "duralumin", "nicrosil", "aluminium", "chromium", "cadmium", "electrum", "bendalloy")

  setMaxDamage(0)
  setHasSubtypes(true)
  setCreativeTab(Allomancy.CreativeTab)

  private def compound(stack: ItemStack) = {
    if(stack.getTagCompound == null)
      stack.setTagCompound(new NBTTagCompound)
    stack.getTagCompound
  }

  def name(stack: ItemStack) = Names(stack.getItemDamage max 0 min (Names.length - 1))

  def purity(stack: ItemStack) = compound(stack).getInteger("purity")

  @SideOnly(Side.CLIENT)
  override def addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: util.List[String], advanced: Boolean): Unit = {
    val purity = this.purity(stack)
    tooltip.add(StatCollector.translateToLocalFormatted("allomancy.message.purity", purity: Integer))
  }

  @SideOnly(Side.CLIENT)
  override def getSubItems(itemIn: Item, tab: CreativeTabs, subItems: util.List[ItemStack]): Unit = {
    for (dmg <- 0 to 13) {
      val stack = new ItemStack(itemIn, 1, dmg)
      compound(stack).setInteger("purity", 100)
      subItems.add(stack)
    }
  }

  override def getUnlocalizedName(stack: ItemStack): String =
    s"item.${name(stack)}_ingot"
}
