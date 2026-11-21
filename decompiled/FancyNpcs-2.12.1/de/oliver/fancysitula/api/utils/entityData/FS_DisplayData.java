package de.oliver.fancysitula.api.utils.entityData;

import de.oliver.fancysitula.api.packets.FS_ClientboundSetEntityDataPacket;

public class FS_DisplayData {
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor TRANSFORMATION_INTERPOLATION_DURATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor POS_ROT_INTERPOLATION_DURATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_POS_ROT_INTERPOLATION_DURATION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor TRANSLATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_TRANSLATION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SCALE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_SCALE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor LEFT_ROTATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_LEFT_ROTATION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor RIGHT_ROTATION = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_RIGHT_ROTATION_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BILLBOARD_RENDER_CONSTRAINTS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_BILLBOARD_RENDER_CONSTRAINTS_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor BRIGHTNESS_OVERRIDE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_BRIGHTNESS_OVERRIDE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor VIEW_RANGE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_VIEW_RANGE_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SHADOW_RADIUS = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_SHADOW_RADIUS_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor SHADOW_STRENGTH = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_SHADOW_STRENGTH_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor WIDTH = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_WIDTH_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor HEIGHT = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_HEIGHT_ID"
   );
   public static final FS_ClientboundSetEntityDataPacket.EntityDataAccessor GLOW_COLOR_OVERRIDE = new FS_ClientboundSetEntityDataPacket.EntityDataAccessor(
      "net.minecraft.world.entity.Display", "DATA_GLOW_COLOR_OVERRIDE_ID"
   );
}
