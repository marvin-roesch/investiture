package de.mineformers.allomancy.metal

import de.mineformers.allomancy.Allomancy
import de.mineformers.allomancy.internal.network.EntityMetalStorageUpdate
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.IExtendedEntityProperties

import scala.collection.mutable

/**
  * MetalStorage
  *
  * @author PaleoCrafter
  */
class MetalStorage {
  private final val _consumedMetals = mutable.HashMap.empty[AllomanticMetal, Int]
  private var _impurities = 0

  def apply(metal: AllomanticMetal) = _consumedMetals.getOrElse(metal, 0)

  def store(metal: AllomanticMetal, amount: Int) = {
    _consumedMetals(metal) = this (metal) + amount
    onUpdate()
  }

  def remove(metal: AllomanticMetal, amount: Int): Boolean = {
    val storage = this (metal)
    if (storage < amount) {
      false
    } else {
      _consumedMetals(metal) = storage - amount
      onUpdate()
      true
    }
  }

  def consumedMetals = _consumedMetals.toMap

  def impurities = _impurities

  def addImpurity(amount: Int = 1) = {
    _impurities += amount
    onUpdate()
  }

  def removeImpurity(amount: Int = 1) = {
    val oldImpurities = impurities
    _impurities = (impurities - amount) max 0
    if (impurities != oldImpurities)
      onUpdate()
  }

  def consume(stack: ItemStack): Boolean = {
    for {
      m <- AllomanticMetals.metals
      value = m.value(stack)
      if value > 0
    } {
      if (m.canBurn(stack))
        store(m, value)
      else
        addImpurity()
      return true
    }

    false
  }

  protected def onUpdate(): Unit = ()

  protected def impurities_=(value: Int) = _impurities = value

  def copy(from: MetalStorage): Unit = {
    _consumedMetals.clear()
    _consumedMetals ++= from.consumedMetals
    _impurities = from.impurities
  }
}

object MetalStorage {
  def apply(entity: Entity) = entity.getExtendedProperties(Allomancy.StorageId).asInstanceOf[MetalStorage]
}

class EntityMetalStorage extends MetalStorage with IExtendedEntityProperties {
  private var entity: Entity = null

  override def init(entity: Entity, world: World): Unit = this.entity = entity

  override def saveNBTData(compound: NBTTagCompound): Unit = {
    val root = new NBTTagCompound
    val storage = new NBTTagCompound
    for ((metal, amount) <- consumedMetals)
      storage.setInteger(metal.id, amount)

    root.setTag("metals", storage)
    root.setInteger("impurities", impurities)
    compound.setTag(Allomancy.StorageId, root)
  }

  override def loadNBTData(compound: NBTTagCompound): Unit = {
    val root = compound.getCompoundTag(Allomancy.StorageId)
    val storage = root.getCompoundTag("metals")
    import scala.collection.JavaConverters._
    for {
      id <- storage.getKeySet.asScala
      metal = AllomanticMetals(id)
      if metal.isDefined
    } store(metal.get, storage.getInteger(id))

    impurities = root.getInteger("impurities")
  }

  override protected def onUpdate(): Unit = sync()

  def sync(): Unit = {
    if (entity != null && !entity.worldObj.isRemote)
      Allomancy.Net.sendToAll(EntityMetalStorageUpdate(entity.getEntityId, this))
  }
}
