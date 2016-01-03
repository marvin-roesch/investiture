package de.mineformers.allomancy.metal

import de.mineformers.allomancy.item.AllomanticMetalIngot
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}

import scala.collection.mutable

/**
  * AllomanticMetal
  *
  * @author PaleoCrafter
  */
trait AllomanticMetal {
  def id: String

  def canBurn(stack: ItemStack): Boolean

  def value(stack: ItemStack): Int = if (canBurn(stack)) stack.stackSize else 0

  def unlocalizedName = "allomancy.metals." + id + ".name"

  final override def hashCode(): Int = id.hashCode

  final override def equals(obj: Any): Boolean =
    if (obj == null || obj.getClass != this.getClass)
      false
    else obj.asInstanceOf[AllomanticMetal].id == id
}

sealed case class VanillaItemMetal(val id: String, item: Item) extends AllomanticMetal {
  override def canBurn(stack: ItemStack): Boolean = stack.getItem == item
}

sealed case class SelectiveItemMetal(val id: String) extends AllomanticMetal {
  override def canBurn(stack: ItemStack): Boolean =
    value(stack) > 0 && AllomanticMetalIngot.purity(stack) == 100

  override def value(stack: ItemStack): Int =
    if (stack.getItem == AllomanticMetalIngot && AllomanticMetalIngot.name(stack) == id) stack.stackSize
    else 0
}

object AllomanticMetals {
  private final val _metals = mutable.ArrayBuffer.empty[AllomanticMetal]

  final val Bronze = SelectiveItemMetal("bronze")
  final val Brass = SelectiveItemMetal("brass")
  final val Copper = SelectiveItemMetal("copper")
  final val Zinc = SelectiveItemMetal("zinc")
  final val Tin = SelectiveItemMetal("tin")
  final val Iron = VanillaItemMetal("iron", Items.iron_ingot)
  final val Pewter = SelectiveItemMetal("pewter")
  final val Steel = SelectiveItemMetal("steel")
  final val Duralumin = SelectiveItemMetal("duralumin")
  final val Nicrosil = SelectiveItemMetal("nicrosil")
  final val Aluminium = SelectiveItemMetal("aluminium")
  final val Chromium = SelectiveItemMetal("chromium")
  final val Gold = VanillaItemMetal("gold", Items.gold_ingot)
  final val Cadmium = SelectiveItemMetal("cadmium")
  final val Electrum = SelectiveItemMetal("electrum")
  final val Bendalloy = SelectiveItemMetal("bendalloy")

  def apply(id: String) = _metals.find(_.id == id)

  def metals: Seq[AllomanticMetal] = _metals.view

  def init(): Unit = {
    _metals ++= Seq(Bronze, Brass, Copper, Zinc, Tin, Iron, Pewter, Steel,
      Duralumin, Nicrosil, Aluminium, Chromium, Gold, Cadmium, Electrum, Bendalloy)
  }
}
