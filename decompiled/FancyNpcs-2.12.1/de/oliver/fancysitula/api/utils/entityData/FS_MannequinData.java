package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_MannequinData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DATA_PROFILE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.Mannequin", "DATA_PROFILE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DATA_IMMOVABLE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.Mannequin", "DATA_IMMOVABLE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DATA_DESCRIPTION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.Mannequin", "DATA_DESCRIPTION"
   );
}
