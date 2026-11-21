package com.github.retrooper.packetevents.protocol.world.waypoint;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WaypointInfo {
   WaypointInfo.Type getType();

   public static enum Type {
      EMPTY(EmptyWaypointInfo::read, EmptyWaypointInfo::write),
      VEC3I(Vec3iWaypointInfo::read, Vec3iWaypointInfo::write),
      CHUNK(ChunkWaypointInfo::read, ChunkWaypointInfo::write),
      AZIMUTH(AzimuthWaypointInfo::read, AzimuthWaypointInfo::write);

      private final PacketWrapper.Reader<WaypointInfo> reader;
      private final PacketWrapper.Writer<WaypointInfo> writer;

      private Type(PacketWrapper.Reader<WaypointInfo> reader, PacketWrapper.Writer<WaypointInfo> writer) {
         this.reader = reader;
         this.writer = writer;
      }

      @Internal
      public WaypointInfo read(PacketWrapper<?> wrapper) {
         return this.reader.apply(wrapper);
      }

      @Internal
      public void write(PacketWrapper<?> wrapper, WaypointInfo info) {
         this.writer.accept(wrapper, info);
      }
   }
}
