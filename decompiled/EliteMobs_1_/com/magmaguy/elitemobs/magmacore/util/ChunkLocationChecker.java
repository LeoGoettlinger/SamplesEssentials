package com.magmaguy.elitemobs.magmacore.util;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkLocationChecker {
   private ChunkLocationChecker() {
   }

   public static boolean locationIsInChunk(Location location, Chunk chunk) {
      if (!chunk.getWorld().equals(location.getWorld())) {
         return false;
      } else {
         double chunkX = chunk.getX() * 16;
         double locationX = location.getX();
         double chunkZ = chunk.getZ() * 16;
         double locationZ = location.getZ();
         return chunkX <= locationX && chunkX + 16.0 >= locationX ? chunkZ <= locationZ && chunkZ + 16.0 >= locationZ : false;
      }
   }

   public static boolean chunkAtLocationIsLoaded(Location location) {
      return location != null && location.getWorld() != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
   }

   public static String locationToChunkString(Location location) {
      if (location != null && location.getWorld() != null) {
         int chunkX = location.getBlockX() >> 4;
         int chunkZ = location.getBlockZ() >> 4;
         return location.getWorld().getName() + ":" + chunkX + ":" + chunkZ;
      } else {
         return null;
      }
   }

   public static String chunkToString(Chunk chunk) {
      return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
   }

   public static String coordinatesToChunkString(String worldName, int chunkX, int chunkZ) {
      return worldName + ":" + chunkX + ":" + chunkZ;
   }

   public static boolean isChunkStringLoaded(String chunkString) {
      if (chunkString == null) {
         return false;
      } else {
         String[] parts = chunkString.split(":");
         if (parts.length != 3) {
            return false;
         } else {
            try {
               String worldName = parts[0];
               int chunkX = Integer.parseInt(parts[1]);
               int chunkZ = Integer.parseInt(parts[2]);
               World world = Bukkit.getWorld(worldName);
               return world != null && world.isChunkLoaded(chunkX, chunkZ);
            } catch (NumberFormatException var6) {
               return false;
            }
         }
      }
   }

   public static String getWorldNameFromChunkString(String chunkString) {
      if (chunkString == null) {
         return null;
      } else {
         String[] parts = chunkString.split(":", 2);
         return parts.length >= 1 ? parts[0] : null;
      }
   }

   public static int getChunkXFromChunkString(String chunkString) {
      if (chunkString == null) {
         return 0;
      } else {
         String[] parts = chunkString.split(":");
         if (parts.length != 3) {
            return 0;
         } else {
            try {
               return Integer.parseInt(parts[1]);
            } catch (NumberFormatException var3) {
               return 0;
            }
         }
      }
   }

   public static int getChunkZFromChunkString(String chunkString) {
      if (chunkString == null) {
         return 0;
      } else {
         String[] parts = chunkString.split(":");
         if (parts.length != 3) {
            return 0;
         } else {
            try {
               return Integer.parseInt(parts[2]);
            } catch (NumberFormatException var3) {
               return 0;
            }
         }
      }
   }
}
