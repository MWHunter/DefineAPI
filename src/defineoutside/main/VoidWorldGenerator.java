package defineoutside.main;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {

    @Override
    public ChunkGenerator.ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, ChunkGenerator.BiomeGrid biome) {
        ChunkGenerator.ChunkData chunk = createChunkData(world);

        return chunk;
    }
}
