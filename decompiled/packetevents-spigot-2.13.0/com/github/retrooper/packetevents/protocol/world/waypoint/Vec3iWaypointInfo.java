package com.github.retrooper.packetevents.protocol.world.waypoint;

import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Vec3iWaypointInfo implements WaypointInfo {
   private final Vector3i position;

   public Vec3iWaypointInfo(Vector3i position) {
      this.position = position;
   }

   @Internal
   public static Vec3iWaypointInfo read(PacketWrapper<?> wrapper) {
      return new Vec3iWaypointInfo(Vector3i.read(wrapper));
   }

   @Internal
   public static void write(PacketWrapper<?> wrapper, WaypointInfo info) {
      Vector3i.write(wrapper, ((Vec3iWaypointInfo)info).position);
   }

   @Override
   public WaypointInfo.Type getType() {
      return WaypointInfo.Type.VEC3I;
   }

   public Vector3i getPosition() {
      return this.position;
   }
}
