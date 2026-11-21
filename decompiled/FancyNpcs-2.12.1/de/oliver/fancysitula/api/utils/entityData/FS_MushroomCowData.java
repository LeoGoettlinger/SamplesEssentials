package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_MushroomCowData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor TYPE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.cow.MushroomCow", "DATA_TYPE"
   );
   public static final String TYPE_RED = "RED";
   public static final String TYPE_BROWN = "BROWN";
}
