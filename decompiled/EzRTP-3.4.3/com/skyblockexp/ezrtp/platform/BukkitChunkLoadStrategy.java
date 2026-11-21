package com.skyblockexp.ezrtp.platform;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;

public final class BukkitChunkLoadStrategy implements ChunkLoadStrategy {
   @Override
   public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
      world.loadChunk(chunkX, chunkZ);
      return CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
   }
}
