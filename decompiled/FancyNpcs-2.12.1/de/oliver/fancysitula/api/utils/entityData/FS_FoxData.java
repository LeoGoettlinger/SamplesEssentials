package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_FoxData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.fox.Fox", "DATA_TYPE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.fox.Fox", "DATA_FLAGS_ID"
   );
   public static final int VARIANT_RED = 0;
   public static final int VARIANT_SNOW = 1;
   public static final byte FLAG_SITTING = 1;
   public static final byte FLAG_CROUCHING = 4;
   public static final byte FLAG_SLEEPING = 32;
}
