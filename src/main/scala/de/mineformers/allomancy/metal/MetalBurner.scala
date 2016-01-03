package de.mineformers.allomancy.metal

import de.mineformers.allomancy.Allomancy
import de.mineformers.allomancy.internal.network.EntityMetalBurnerUpdate
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraftforge.common.IExtendedEntityProperties

import scala.collection.mutable

/**
  * MetalBurner
  *
  * @author PaleoCrafter
  */
class MetalBurner extends MetalStorage {
  private final val _burningMetals = mutable.HashMap.empty[AllomanticMetal, Int]
  var burnTime = 20

  def burning(metal: AllomanticMetal) = _burningMetals.exists(e => e._1 == metal && e._2 >= 0)

  def startBurning(metal: AllomanticMetal): Boolean = {
    val storage = this (metal)
    if (storage > 0) {
      _burningMetals(metal) = 0
      onUpdate()
      true
    } else false
  }

  def updateBurnTimer(metal: AllomanticMetal): Boolean = {
    if (!burning(metal))
      false
    else {
      _burningMetals(metal) += 1
      if (_burningMetals(metal) == 20) {
        _burningMetals(metal) = 0
        if (!remove(metal, 1))
          stopBurning(metal)
        else if(this(metal) == 0)
          stopBurning(metal)
        true
      } else false
    }
  }

  def stopBurning(metal: AllomanticMetal) = {
    if (burning(metal)) {
      _burningMetals -= metal
      onUpdate()
    }
  }

  def burningMetals: Seq[AllomanticMetal] = _burningMetals.view.filter(_._2 >= 0).map(_._1).toSeq

  def burnTimers = _burningMetals.toMap

  def setBurnTimer(metal: AllomanticMetal, time: Int) = _burningMetals(metal) = time

  override def copy(from: MetalStorage): Unit = {
    super.copy(from)
    from match {
      case burner: MetalBurner =>
        _burningMetals.clear()
        _burningMetals ++= burner._burningMetals
    }
  }
}

object MetalBurner {
  def apply(entity: Entity) = entity.getExtendedProperties(Allomancy.BurnerId).asInstanceOf[MetalBurner]
}


class EntityMetalBurner extends MetalBurner with IExtendedEntityProperties {
  private var entity: Entity = null

  override def init(entity: Entity, world: World): Unit = this.entity = entity

  override def saveNBTData(compound: NBTTagCompound): Unit = {
    val root = new NBTTagCompound
    val storage = new NBTTagCompound
    for ((metal, amount) <- consumedMetals)
      storage.setInteger(metal.id, amount)
    val burning = new NBTTagCompound
    for ((metal, time) <- burnTimers)
      burning.setInteger(metal.id, time)

    root.setTag("metals", storage)
    root.setTag("burning", burning)
    root.setInteger("impurities", impurities)
    compound.setTag(Allomancy.BurnerId, root)
  }

  override def loadNBTData(compound: NBTTagCompound): Unit = {
    val root = compound.getCompoundTag(Allomancy.BurnerId)
    val storage = root.getCompoundTag("metals")
    import scala.collection.JavaConverters._
    for {
      id <- storage.getKeySet.asScala
      metal = AllomanticMetals(id)
      if metal.isDefined
    } store(metal.get, storage.getInteger(id))

    val burning = root.getCompoundTag("burning")
    for {
      id <- burning.getKeySet.asScala
      metal = AllomanticMetals(id)
      if metal.isDefined
    } setBurnTimer(metal.get, burning.getInteger(id))

    impurities = root.getInteger("impurities")
  }

  override protected def onUpdate(): Unit = sync()

  def sync(): Unit = {
    if (entity != null && !entity.worldObj.isRemote)
      Allomancy.Net.sendToAll(EntityMetalBurnerUpdate(entity.getEntityId, this))
  }
}
