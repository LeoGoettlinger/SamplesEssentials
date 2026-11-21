package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_VillagerData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VILLAGER_DATA = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.npc.Villager", "DATA_VILLAGER_DATA"
   );
}
