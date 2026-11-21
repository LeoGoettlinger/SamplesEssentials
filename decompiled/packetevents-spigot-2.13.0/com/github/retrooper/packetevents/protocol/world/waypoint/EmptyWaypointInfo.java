package com.github.retrooper.packetevents.protocol.world.waypoint;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class EmptyWaypointInfo implements WaypointInfo {
   public static final EmptyWaypointInfo EMPTY = new EmptyWaypointInfo();

   private EmptyWaypointInfo() {
   }

   @Internal
   public static EmptyWaypointInfo read(PacketWrapper<?> wrapper) {
      return EMPTY;
   }

   @Internal
   public static void write(PacketWrapper<?> wrapper, WaypointInfo info) {
   }

   @Override
   public WaypointInfo.Type getType() {
      return WaypointInfo.Type.EMPTY;
   }
}
