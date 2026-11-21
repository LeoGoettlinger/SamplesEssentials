package com.ranull.graves.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class HologramData extends EntityData {
   private static final long serialVersionUID = 1L;
   private final int line;
   private final int chunkX;
   private final int chunkZ;
   private HologramData.Backend backend;

   public HologramData(Location location, UUID uuidEntity, UUID uuidGrave, int line) {
      this(location, uuidEntity, uuidGrave, line, HologramData.Backend.ARMOR_STAND);
   }

   public HologramData(Location location, UUID uuidEntity, UUID uuidGrave, int line, HologramData.Backend backend) {
      super(location, uuidEntity, uuidGrave, EntityData.Type.HOLOGRAM);
      this.line = line;
      this.backend = backend != null ? backend : HologramData.Backend.ARMOR_STAND;
      if (location != null) {
         this.chunkX = location.getBlockX() >> 4;
         this.chunkZ = location.getBlockZ() >> 4;
      } else {
         this.chunkX = 0;
         this.chunkZ = 0;
      }
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      if (this.backend == null) {
         this.backend = HologramData.Backend.ARMOR_STAND;
      }
   }

   public int getLine() {
      return this.line;
   }

   public HologramData.Backend getBackend() {
      return this.backend;
   }

   public int getChunkX() {
      return this.chunkX;
   }

   public int getChunkZ() {
      return this.chunkZ;
   }

   public boolean isChunkLoaded() {
      Location loc = this.getLocation();
      World world = loc != null ? loc.getWorld() : null;
      return world == null ? false : world.isChunkLoaded(this.getChunkX(), this.getChunkZ());
   }

   public Chunk getLoadedChunkOrNull() {
      Location loc = this.getLocation();
      World world = loc != null ? loc.getWorld() : null;
      return world != null && world.isChunkLoaded(this.getChunkX(), this.getChunkZ()) ? world.getChunkAt(this.getChunkX(), this.getChunkZ()) : null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else {
         return !(o instanceof HologramData other)
            ? false
            : this.getUUIDGrave() != null && this.getUUIDGrave().equals(other.getUUIDGrave()) && this.line == other.line && this.backend == other.backend;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.getUUIDGrave(), this.line, this.backend);
   }

   public static enum Backend {
      ARMOR_STAND,
      TEXT_DISPLAY;
   }
}
