package tessera.math

import net.minecraft.util.{BlockPos, Vec3}

import scala.language.implicitConversions

/**
 * MathConversions
 *
 * @author PaleoCrafter
 */
trait MathConversions {
  type VVec3i = net.minecraft.util.Vec3i

  implicit def vec3ToVec3i(pos: VVec3i): Vec3i = Vec3i(pos.getX, pos.getY, pos.getZ)

  implicit def vec3ToVec3d(vec: Vec3): Vec3d = Vec3d(vec.xCoord, vec.yCoord, vec.zCoord)

  implicit def vec3iToBlockPos(pos: Vec3i): BlockPos = new BlockPos(pos.x, pos.y, pos.z)
}
