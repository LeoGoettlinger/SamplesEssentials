package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_AllayData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DANCING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.allay.Allay", "DATA_DANCING"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor CAN_DUPLICATE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.allay.Allay", "DATA_CAN_DUPLICATE"
   );
}
