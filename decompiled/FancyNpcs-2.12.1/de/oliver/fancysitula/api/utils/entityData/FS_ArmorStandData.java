package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_ArmorStandData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor CLIENT_FLAGS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_CLIENT_FLAGS"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HEAD_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_HEAD_POSE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BODY_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_BODY_POSE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LEFT_ARM_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_LEFT_ARM_POSE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor RIGHT_ARM_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_RIGHT_ARM_POSE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LEFT_LEG_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_LEFT_LEG_POSE"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor RIGHT_LEG_POSE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.decoration.ArmorStand", "DATA_RIGHT_LEG_POSE"
   );
   public static final byte FLAG_SMALL = 1;
   public static final byte FLAG_ARMS = 4;
   public static final byte FLAG_NO_BASEPLATE = 8;
   public static final byte FLAG_MARKER = 16;
}
