package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_RaiderData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_CELEBRATING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.raid.Raider", "IS_CELEBRATING"
   );
}
