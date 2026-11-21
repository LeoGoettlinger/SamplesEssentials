package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_CamelData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DASH = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.camel.Camel", "DASH"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LAST_POSE_CHANGE_TICK = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.camel.Camel", "LAST_POSE_CHANGE_TICK"
   );
}
