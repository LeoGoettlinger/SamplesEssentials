package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_ZombieData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BABY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.zombie.Zombie", "DATA_BABY_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DROWNED_CONVERSION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.zombie.Zombie", "DATA_DROWNED_CONVERSION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BABY_LEGACY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Zombie", "DATA_BABY_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DROWNED_CONVERSION_LEGACY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Zombie", "DATA_DROWNED_CONVERSION_ID"
   );
}
