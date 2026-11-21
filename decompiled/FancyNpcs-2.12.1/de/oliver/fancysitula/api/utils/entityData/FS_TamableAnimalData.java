package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_TamableAnimalData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.TamableAnimal", "DATA_FLAGS_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor OWNER_UUID = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.TamableAnimal", "DATA_OWNERUUID_ID"
   );
   public static final byte FLAG_SITTING = 1;
   public static final byte FLAG_TAMED = 4;
}
