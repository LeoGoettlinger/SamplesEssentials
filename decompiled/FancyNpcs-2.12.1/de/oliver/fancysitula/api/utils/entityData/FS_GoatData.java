package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_GoatData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_SCREAMING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.goat.Goat", "DATA_IS_SCREAMING_GOAT"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HAS_LEFT_HORN = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.goat.Goat", "DATA_HAS_LEFT_HORN"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HAS_RIGHT_HORN = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.goat.Goat", "DATA_HAS_RIGHT_HORN"
   );
}
