package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_WitchData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor USING_ITEM = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Witch", "DATA_USING_ITEM"
   );
}
