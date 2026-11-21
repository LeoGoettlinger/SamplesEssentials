package com.viaversion.viabackwards.protocol.v1_19to1_18_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import java.util.UUID;

public final class StoredPainting implements StorableObject {
   private final int entityId;
   private final UUID uuid;
   private final BlockPosition position;
   private final byte direction;
   private int type;

   public StoredPainting(int entityId, UUID uuid, BlockPosition position, byte direction) {
      this.entityId = entityId;
      this.uuid = uuid;
      this.position = position;
      this.direction = direction;
   }

   public StoredPainting(int entityId, UUID uuid, BlockPosition position, int direction) {
      this(entityId, uuid, position, to2dDirection(direction));
   }

   private static byte to2dDirection(int direction) {
      return switch (direction) {
         case 0, 1 -> -1;
         case 2 -> 2;
         case 3 -> 0;
         case 4 -> 1;
         case 5 -> 3;
         default -> throw new IllegalArgumentException("Invalid direction: " + direction);
      };
   }

   public int entityId() {
      return this.entityId;
   }

   public UUID uuid() {
      return this.uuid;
   }

   public BlockPosition position() {
      return this.position;
   }

   public byte direction() {
      return this.direction;
   }

   public int type() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }
}
