package de.mineformers.allomancy.internal.network

import tessera.io.Message

/**
  * ToggleBurningMetal
  *
  * @author PaleoCrafter
  */
case class ToggleBurningMetal(val metal: String) extends Message {
  def this() = this(null)
}
