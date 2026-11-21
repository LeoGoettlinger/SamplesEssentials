package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_SlimeData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SIZE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.Slime", "ID_SIZE"
   );
}
