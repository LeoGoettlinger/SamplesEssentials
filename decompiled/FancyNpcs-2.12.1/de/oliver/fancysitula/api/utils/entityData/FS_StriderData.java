package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_StriderData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SADDLE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Strider", "DATA_SADDLE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SUFFOCATING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Strider", "DATA_SUFFOCATING"
   );
}
