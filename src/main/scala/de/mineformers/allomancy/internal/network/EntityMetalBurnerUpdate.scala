package de.mineformers.allomancy.internal.network

import de.mineformers.allomancy.metal.{MetalBurner, MetalStorage}
import tessera.io.Message

/**
  * EntityMetalStorageUpdate
  *
  * @author PaleoCrafter
  */
case class EntityMetalBurnerUpdate(var entity: Int, var storage: MetalBurner) extends Message {
  def this() = this(-1, null)
}
