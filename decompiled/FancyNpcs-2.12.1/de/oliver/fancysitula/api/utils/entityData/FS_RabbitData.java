package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_RabbitData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.rabbit.Rabbit", "DATA_TYPE_ID"
   );
   public static final int VARIANT_BROWN = 0;
   public static final int VARIANT_WHITE = 1;
   public static final int VARIANT_BLACK = 2;
   public static final int VARIANT_WHITE_SPLOTCHED = 3;
   public static final int VARIANT_GOLD = 4;
   public static final int VARIANT_SALT = 5;
   public static final int VARIANT_EVIL = 99;
}
