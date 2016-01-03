package de.mineformers.allomancy.world

import java.util.Random

import de.mineformers.allomancy.block.AllomanticMetalOre
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.feature.WorldGenMinable
import net.minecraftforge.fml.common.IWorldGenerator

/**
  * MetalGenerator
  *
  * @author PaleoCrafter
  */
class MetalGenerator extends IWorldGenerator {

  case class Ore(range: Range, veins: Int, veinSize: Int)

  final val Properties = Map(
    "copper" -> Ore(0 to 64, 20, 9),
    "zinc" -> Ore(0 to 64, 20, 9),
    "tin" -> Ore(0 to 64, 20, 9),
    "aluminium" -> Ore(0 to 64, 20, 9),
    "chromium" -> Ore(0 to 64, 20, 9)
  )
  final val Generators = Properties.map(e =>
    e._1 -> new WorldGenMinable(AllomanticMetalOre.fromMetal(e._1), e._2.veinSize))

  override def generate(random: Random,
                        chunkX: Int,
                        chunkZ: Int,
                        world: World,
                        chunkGenerator:
                        IChunkProvider,
                        chunkProvider: IChunkProvider): Unit =
    if (world.provider.getDimensionId == 0) {
      val x = chunkX * 16
      val z = chunkZ * 16
      val iterator = Properties.iterator
      while (iterator.hasNext) {
        val entry = iterator.next()
        var i = 0
        while (i < entry._2.veins) {
          Generators(entry._1)
            .generate(world, random, new BlockPos(
              x + random.nextInt(16),
              random.nextInt(entry._2.range.max - 1) + entry._2.range.min,
              z + random.nextInt(16)))
          i += 1
        }
      }
    }
}
