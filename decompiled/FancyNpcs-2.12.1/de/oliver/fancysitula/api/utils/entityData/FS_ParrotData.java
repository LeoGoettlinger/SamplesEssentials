package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_ParrotData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.parrot.Parrot", "DATA_VARIANT_ID"
   );
   public static final int VARIANT_RED_BLUE = 0;
   public static final int VARIANT_BLUE = 1;
   public static final int VARIANT_GREEN = 2;
   public static final int VARIANT_YELLOW_BLUE = 3;
   public static final int VARIANT_GRAY = 4;
}
