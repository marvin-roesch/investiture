package de.mineformers.investiture.allomancy.world;

import com.google.common.collect.ImmutableMap;
import de.mineformers.investiture.allomancy.Allomancy;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generates ores for all allomantic metals.
 */
public class MetalGenerator implements IWorldGenerator
{
    /**
     * Container class for each ore's configuration
     */
    private static class Ore
    {
        final int minY;
        final int maxY;
        final int veins;
        final int veinSize;

        Ore(int minY, int maxY, int veins, int veinSize)
        {
            this.minY = minY;
            this.maxY = maxY;
            this.veins = veins;
            this.veinSize = veinSize;
        }
    }

    // Should be replaced by real config
    private final static Map<String, Ore> ORES = ImmutableMap
        .of("copper", new Ore(0, 64, 20, 9),
            "zinc", new Ore(0, 64, 20, 9),
            "tin", new Ore(0, 64, 20, 9),
            "aluminium", new Ore(0, 64, 20, 9),
            "chromium", new Ore(0, 64, 20, 9));
    private final static Map<String, WorldGenMinable> GENERATORS = ORES.entrySet().stream().collect(
        Collectors.toMap(Map.Entry::getKey, e -> new WorldGenMinable(Allomancy.Blocks.ALLOMANTIC_ORE.fromMetal(e.getKey()), e.getValue().veinSize)));

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if (world.provider.getDimension() == 0)
        {
            int x = chunkX * 16;
            int z = chunkZ * 16;
            for (Map.Entry<String, Ore> entry : ORES.entrySet())
            {
                String metal = entry.getKey();
                Ore ore = entry.getValue();
                for (int i = 0; i < ore.veins; i++)
                {
                    GENERATORS.get(metal).generate(world, random,
                                                   new BlockPos(x + random.nextInt(16),
                                                                random.nextInt(ore.maxY - ore.minY + 1) + ore.minY,
                                                                z + random.nextInt(16)));
                }
            }
        }
    }
}
