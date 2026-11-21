package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_FrogData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.frog.Frog", "DATA_VARIANT_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor TONGUE_TARGET = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.frog.Frog", "DATA_TONGUE_TARGET_ID"
   );
}
