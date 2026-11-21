package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_HorseData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.equine.Horse", "DATA_ID_TYPE_VARIANT"
   );
   public static final int VARIANT_WHITE = 0;
   public static final int VARIANT_CREAMY = 1;
   public static final int VARIANT_CHESTNUT = 2;
   public static final int VARIANT_BROWN = 3;
   public static final int VARIANT_BLACK = 4;
   public static final int VARIANT_GRAY = 5;
   public static final int VARIANT_DARK_BROWN = 6;
   public static final int MARKING_NONE = 0;
   public static final int MARKING_WHITE = 1;
   public static final int MARKING_WHITE_FIELD = 2;
   public static final int MARKING_WHITE_DOTS = 3;
   public static final int MARKING_BLACK_DOTS = 4;

   public static int createVariantData(int variant, int marking) {
      return variant | marking << 8;
   }
}
