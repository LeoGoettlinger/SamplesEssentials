package com.github.retrooper.packetevents.protocol.world.waypoint;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ChunkWaypointInfo implements WaypointInfo {
   private final int chunkX;
   private final int chunkZ;

   public ChunkWaypointInfo(int chunkX, int chunkZ) {
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   @Internal
   public static ChunkWaypointInfo read(PacketWrapper<?> wrapper) {
      return new ChunkWaypointInfo(wrapper.readVarInt(), wrapper.readVarInt());
   }

   @Internal
   public static void write(PacketWrapper<?> wrapper, WaypointInfo info) {
      ChunkWaypointInfo chunkInfo = (ChunkWaypointInfo)info;
      wrapper.writeVarInt(chunkInfo.chunkX);
      wrapper.writeVarInt(chunkInfo.chunkZ);
   }

   @Override
   public WaypointInfo.Type getType() {
      return WaypointInfo.Type.CHUNK;
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }
}
