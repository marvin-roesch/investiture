package de.mineformers.allomancy.internal.network

import de.mineformers.allomancy.metal.MetalStorage
import tessera.io.Message

/**
  * EntityMetalStorageUpdate
  *
  * @author PaleoCrafter
  */
case class EntityMetalStorageUpdate(var entity: Int, var storage: MetalStorage) extends Message {
  def this() = this(-1, null)
}
