package com.viaversion.viabackwards.protocol.v1_21_2to1_21.storage;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import java.util.HashSet;
import java.util.Set;

public final class SignStorage {
   private final Set<BlockPosition> signs = new HashSet<>();

   public void addSign(BlockPosition position) {
      this.signs.add(position);
   }

   public boolean isSign(BlockPosition position) {
      return this.signs.contains(position);
   }

   public void removeSign(BlockPosition position) {
      this.signs.remove(position);
   }

   public void removeSigns(int chunkX, int chunkZ) {
      this.signs.removeIf(pos -> pos.x() >> 4 == chunkX && pos.z() >> 4 == chunkZ);
   }
}
