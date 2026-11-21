package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_CatData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.feline.Cat", "DATA_VARIANT_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_LYING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.feline.Cat", "IS_LYING"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor RELAX_STATE_ONE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.feline.Cat", "RELAX_STATE_ONE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor COLLAR_COLOR = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.feline.Cat", "DATA_COLLAR_COLOR"
   );
}
