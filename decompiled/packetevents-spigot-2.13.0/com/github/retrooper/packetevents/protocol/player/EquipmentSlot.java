package com.github.retrooper.packetevents.protocol.player;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.jetbrains.annotations.Nullable;

public enum EquipmentSlot {
   MAIN_HAND(0),
   OFF_HAND(0),
   BOOTS(1),
   LEGGINGS(2),
   CHEST_PLATE(3),
   HELMET(4),
   BODY(0),
   SADDLE(0);

   private static final EquipmentSlot[] VALUES = values();
   private final byte legacyId;

   private EquipmentSlot(int legacyId) {
      this.legacyId = (byte)legacyId;
   }

   public int getId(ServerVersion version) {
      return this.getId(version.toClientVersion());
   }

   public int getId(ClientVersion version) {
      return version.isOlderThan(ClientVersion.V_1_9) ? this.legacyId : this.ordinal();
   }

   @Nullable
   public static EquipmentSlot getById(ClientVersion version, int id) {
      if (version.isOlderThan(ClientVersion.V_1_9)) {
         for (EquipmentSlot slot : VALUES) {
            if (slot.getId(version) == id) {
               return slot;
            }
         }

         return null;
      } else {
         return VALUES[id];
      }
   }

   @Nullable
   public static EquipmentSlot getById(ServerVersion version, int id) {
      return getById(version.toClientVersion(), id);
   }
}
