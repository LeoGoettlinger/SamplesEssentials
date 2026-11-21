package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_PolarBearData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor STANDING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.polarbear.PolarBear", "DATA_STANDING_ID"
   );
}
