package com.github.retrooper.packetevents.protocol.entity.villager.level;

import org.jetbrains.annotations.Nullable;

public enum VillagerLevel {
   NOVICE,
   APPRENTICE,
   JOURNEYMAN,
   EXPERT,
   MASTER;

   private static final VillagerLevel[] VALUES = values();

   @Nullable
   public static VillagerLevel getById(int id) {
      return id >= 1 && id <= VALUES.length ? VALUES[id - 1] : null;
   }

   public int getId() {
      return this.ordinal() + 1;
   }
}
