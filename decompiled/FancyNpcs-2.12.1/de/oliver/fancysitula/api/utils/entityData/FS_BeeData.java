package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_BeeData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.bee.Bee", "DATA_FLAGS_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor REMAINING_ANGER_TIME = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.bee.Bee", "DATA_REMAINING_ANGER_TIME"
   );
   public static final byte FLAG_ANGRY = 2;
   public static final byte FLAG_HAS_STUNG = 4;
   public static final byte FLAG_HAS_NECTAR = 8;
}
