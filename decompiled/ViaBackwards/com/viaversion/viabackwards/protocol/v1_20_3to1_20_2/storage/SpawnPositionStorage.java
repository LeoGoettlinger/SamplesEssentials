package com.viaversion.viabackwards.protocol.v1_20_3to1_20_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.libs.fastutil.Pair;
import java.util.Objects;

public class SpawnPositionStorage implements StorableObject {
   public static final Pair<BlockPosition, Float> DEFAULT_SPAWN_POSITION = Pair.of(new BlockPosition(8, 64, 8), 0.0F);
   private Pair<BlockPosition, Float> spawnPosition;
   private String dimension;

   public Pair<BlockPosition, Float> getSpawnPosition() {
      return this.spawnPosition;
   }

   public void setSpawnPosition(Pair<BlockPosition, Float> spawnPosition) {
      this.spawnPosition = spawnPosition;
   }

   public void setDimension(String dimension) {
      boolean changed = !Objects.equals(this.dimension, dimension);
      this.dimension = dimension;
      if (changed) {
         this.spawnPosition = DEFAULT_SPAWN_POSITION;
      }
   }
}
