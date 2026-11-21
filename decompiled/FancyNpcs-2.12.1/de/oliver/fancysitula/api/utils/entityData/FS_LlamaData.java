package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_LlamaData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.equine.Llama", "DATA_VARIANT_ID"
   );
   public static final int VARIANT_CREAMY = 0;
   public static final int VARIANT_WHITE = 1;
   public static final int VARIANT_BROWN = 2;
   public static final int VARIANT_GRAY = 3;
}
