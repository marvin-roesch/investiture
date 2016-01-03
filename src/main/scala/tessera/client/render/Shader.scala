package tessera.client.render

import org.lwjgl.opengl.{GL11, GL20}
import GL20._
import GL11._
import tessera.math._
import scala.collection.mutable
import scala.io.Source

/**
  * ShaderSystem
  *
  * @author PaleoCrafter
  */
class Shader(vert: String, frag: String) {
  private val varLocations: mutable.HashMap[String, Int] = mutable.HashMap[String, Int]()
  private var lastProgram: Int = 0
  private var initialized: Boolean = false
  private var active: Boolean = false
  private val program: Int = glCreateProgram()

  if(vert != null)
    addShader(vert, GL_VERTEX_SHADER)
  if(frag != null)
    addShader(frag, GL_FRAGMENT_SHADER)

  def init(): Unit = {
    glLinkProgram(program)
    glValidateProgram(program)
    initialized = true
    active = glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE
  }

  def activate(): Unit = {
    if (!initialized) init()
    if (active) {
      lastProgram = glGetInteger(GL_CURRENT_PROGRAM)
      glUseProgram(program)
    }
  }

  def deactivate(): Unit = {
    if (!initialized) init()
    if (active) glUseProgram(lastProgram)
  }

  private def addShader(source: String, `type`: Int) {
    if (!initialized) {
      try {
        glAttachShader(program, createShader(source, `type`))
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }
    }
  }

  private def createShader(source: String, shaderType: Int): Int = {
    var shader: Int = 0
    try {
      shader = glCreateShader(shaderType)
      if (shader == 0) return 0
      glShaderSource(shader, Source.fromInputStream(classOf[Shader].getResourceAsStream(source)).mkString)
      glCompileShader(shader)
      shader
    } catch {
      case exc: Exception =>
        glDeleteShader(shader)
        throw exc
    }
  }

  def setUniformInt(uniform: String, values: Int*): Unit =
    if(active) {
      val location = getUniformLocation(uniform)
      values.toList match {
        case v1 :: Nil => glUniform1i(location, v1)
        case v1 :: v2 :: Nil => glUniform2i(location, v1, v2)
        case v1 :: v2 :: v3 :: Nil => glUniform3i(location, v1, v2, v3)
        case v1 :: v2 :: v3 :: v4 :: Nil => glUniform4i(location, v1, v2, v3, v4)
        case _ =>
      }
    }

  def setUniformFloat(uniform: String, values: Float*): Unit =
    if(active) {
      val location = getUniformLocation(uniform)
      values.toList match {
        case v1 :: Nil => glUniform1f(location, v1)
        case v1 :: v2 :: Nil => glUniform2f(location, v1, v2)
        case v1 :: v2 :: v3 :: Nil => glUniform3f(location, v1, v2, v3)
        case v1 :: v2 :: v3 :: v4 :: Nil => glUniform4f(location, v1, v2, v3, v4)
        case _ =>
      }
    }

  def setUniform(uniform: String, value: Vec2i): Unit =
    setUniformInt(uniform, value.x, value.y)

  def setUniform(uniform: String, value: Vec3i): Unit =
    setUniformInt(uniform, value.x, value.y, value.z)

  def setUniform(uniform: String, value: Vec4i): Unit =
    setUniformInt(uniform, value.x, value.y, value.z, value.w)

  def setUniform(uniform: String, value: Vec2d): Unit =
    setUniformFloat(uniform, value.x.toFloat, value.y.toFloat)

  def setUniform(uniform: String, value: Vec3d): Unit =
    setUniformFloat(uniform, value.x.toFloat, value.y.toFloat, value.z.toFloat)

  def setUniform(uniform: String, value: Vec4d): Unit =
    setUniformFloat(uniform, value.x.toFloat, value.y.toFloat, value.z.toFloat, value.w.toFloat)

  def setUniformBool(uniform: String, value: Boolean): Unit =
    setUniformInt(uniform, if(value) 1 else 0)

  def getUniformLocation(uniform: String): Int = {
    if (!varLocations.contains(uniform)) {
      varLocations.put(uniform, glGetUniformLocation(program, uniform))
    }
    varLocations(uniform)
  }
}