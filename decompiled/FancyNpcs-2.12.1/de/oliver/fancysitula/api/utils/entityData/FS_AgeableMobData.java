package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_AgeableMobData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BABY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.AgeableMob", "DATA_BABY_ID"
   );
}
