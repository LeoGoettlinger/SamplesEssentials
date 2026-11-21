package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_MobData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor MOB_FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Mob", "DATA_MOB_FLAGS_ID"
   );
}
