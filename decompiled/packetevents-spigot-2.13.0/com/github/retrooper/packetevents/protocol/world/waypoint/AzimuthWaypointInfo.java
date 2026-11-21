package com.github.retrooper.packetevents.protocol.world.waypoint;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AzimuthWaypointInfo implements WaypointInfo {
   private final float angle;

   public AzimuthWaypointInfo(float angle) {
      this.angle = angle;
   }

   @Internal
   public static AzimuthWaypointInfo read(PacketWrapper<?> wrapper) {
      return new AzimuthWaypointInfo(wrapper.readFloat());
   }

   @Internal
   public static void write(PacketWrapper<?> wrapper, WaypointInfo info) {
      wrapper.writeFloat(((AzimuthWaypointInfo)info).angle);
   }

   @Override
   public WaypointInfo.Type getType() {
      return WaypointInfo.Type.AZIMUTH;
   }

   public float getAngle() {
      return this.angle;
   }
}
