package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_PigData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SADDLE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.pig.Pig", "DATA_SADDLE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BOOST_TIME = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.pig.Pig", "DATA_BOOST_TIME"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VARIANT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.animal.pig.Pig", "DATA_VARIANT_ID"
   );
}
