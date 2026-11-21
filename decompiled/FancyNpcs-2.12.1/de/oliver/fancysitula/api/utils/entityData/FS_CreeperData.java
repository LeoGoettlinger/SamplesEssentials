package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_CreeperData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SWELL_DIR = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Creeper", "DATA_SWELL_DIR"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_POWERED = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Creeper", "DATA_IS_POWERED"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_IGNITED = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Creeper", "DATA_IS_IGNITED"
   );
}
