package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_EndermanData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor CREEPY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.EnderMan", "DATA_CREEPY"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor STARED_AT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.EnderMan", "DATA_STARED_AT"
   );
}
