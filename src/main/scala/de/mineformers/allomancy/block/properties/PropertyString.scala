package de.mineformers.allomancy.block.properties

import net.minecraft.block.properties.PropertyHelper

/**
  * PropertyString
  *
  * @author PaleoCrafter
  */
class PropertyString(name: String, allowedValues: String*) extends PropertyHelper[String](name, classOf[String]) {
  import scala.collection.JavaConverters._

  private val jValues = allowedValues.asJavaCollection

  override def getName(value: String): String = value

  override def getAllowedValues = jValues
}
