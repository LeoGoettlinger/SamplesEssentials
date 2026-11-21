package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_PiglinData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IMMUNE_TO_ZOMBIFICATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.piglin.AbstractPiglin", "DATA_IMMUNE_TO_ZOMBIFICATION"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_BABY = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.piglin.Piglin", "DATA_BABY_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_CHARGING_CROSSBOW = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.piglin.Piglin", "DATA_IS_CHARGING_CROSSBOW"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor IS_DANCING = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.monster.piglin.Piglin", "DATA_IS_DANCING"
   );
}
