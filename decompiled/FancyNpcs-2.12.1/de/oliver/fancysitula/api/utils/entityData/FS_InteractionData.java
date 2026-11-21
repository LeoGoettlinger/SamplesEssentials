package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_InteractionData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor WIDTH = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Interaction", "DATA_WIDTH_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HEIGHT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Interaction", "DATA_HEIGHT_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor RESPONSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Interaction", "DATA_RESPONSE_ID"
   );
}
