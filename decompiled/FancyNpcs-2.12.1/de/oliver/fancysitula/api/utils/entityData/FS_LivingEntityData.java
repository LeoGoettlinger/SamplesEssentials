package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_LivingEntityData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LIVING_ENTITY_FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_LIVING_ENTITY_FLAGS"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HEALTH = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_HEALTH_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor EFFECT_COLOR = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_EFFECT_COLOR_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor EFFECT_AMBIENCE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_EFFECT_AMBIENCE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor ARROW_COUNT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_ARROW_COUNT_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor STINGER_COUNT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.LivingEntity", "DATA_STINGER_COUNT_ID"
   );
}
