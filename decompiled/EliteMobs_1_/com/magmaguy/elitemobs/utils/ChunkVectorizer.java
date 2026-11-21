package com.magmaguy.elitemobs.utils;

import java.util.UUID;
import org.bukkit.Chunk;

public final class ChunkVectorizer {
   private ChunkVectorizer() {
   }

   public static ChunkVectorizer.Key key(Chunk chunk) {
      return key(chunk.getX(), chunk.getZ(), chunk.getWorld().getUID());
   }

   public static ChunkVectorizer.Key key(int x, int z, UUID worldId) {
      return new ChunkVectorizer.Key(worldId, x, z);
   }

   public record Key(UUID worldId, int x, int z) {
   }
}
