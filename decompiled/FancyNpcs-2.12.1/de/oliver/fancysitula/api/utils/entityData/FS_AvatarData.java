package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_AvatarData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DATA_PLAYER_MODE_CUSTOMISATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Avatar", "DATA_PLAYER_MODE_CUSTOMISATION"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor DATA_PLAYER_MAIN_HAND = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Avatar", "DATA_PLAYER_MAIN_HAND"
   );
   public static final byte SKIN_CAPE = 1;
   public static final byte SKIN_JACKET = 2;
   public static final byte SKIN_LEFT_SLEEVE = 4;
   public static final byte SKIN_RIGHT_SLEEVE = 8;
   public static final byte SKIN_LEFT_PANTS = 16;
   public static final byte SKIN_RIGHT_PANTS = 32;
   public static final byte SKIN_HAT = 64;
   public static final byte SKIN_ALL = 127;
}
