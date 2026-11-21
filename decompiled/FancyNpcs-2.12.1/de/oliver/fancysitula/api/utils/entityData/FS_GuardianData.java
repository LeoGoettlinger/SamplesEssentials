package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_GuardianData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor MOVING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Guardian", "DATA_ID_MOVING"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor ATTACK_TARGET = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Guardian", "DATA_ID_ATTACK_TARGET"
   );
}
