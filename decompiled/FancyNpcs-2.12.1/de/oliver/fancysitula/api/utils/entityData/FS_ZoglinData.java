package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_ZoglinData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BABY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Zoglin", "DATA_BABY_ID"
   );
}
