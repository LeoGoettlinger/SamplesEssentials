package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_AbstractHorseData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.equine.AbstractHorse", "DATA_ID_FLAGS"
   );
   public static final byte FLAG_TAMED = 2;
   public static final byte FLAG_BRED = 8;
   public static final byte FLAG_EATING = 16;
   public static final byte FLAG_STANDING = 32;
   public static final byte FLAG_MOUTH_OPEN = 64;
}
