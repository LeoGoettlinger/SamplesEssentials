package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_AxolotlData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.axolotl.Axolotl", "DATA_VARIANT"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor PLAYING_DEAD = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.axolotl.Axolotl", "DATA_PLAYING_DEAD"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor FROM_BUCKET = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.axolotl.Axolotl", "FROM_BUCKET"
   );
   public static final int VARIANT_LUCY = 0;
   public static final int VARIANT_WILD = 1;
   public static final int VARIANT_GOLD = 2;
   public static final int VARIANT_CYAN = 3;
   public static final int VARIANT_BLUE = 4;
}
