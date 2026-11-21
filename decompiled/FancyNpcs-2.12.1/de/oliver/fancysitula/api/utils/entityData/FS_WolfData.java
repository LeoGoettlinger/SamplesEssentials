package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_WolfData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor INTERESTED = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.wolf.Wolf", "DATA_INTERESTED_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor COLLAR_COLOR = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.wolf.Wolf", "DATA_COLLAR_COLOR"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor REMAINING_ANGER_TIME = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.wolf.Wolf", "DATA_REMAINING_ANGER_TIME"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.wolf.Wolf", "DATA_VARIANT_ID"
   );
}
