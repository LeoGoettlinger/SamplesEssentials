package cm.chunkManager.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.World;

public class WgHook {
   public static boolean isChunkInRegion(Chunk chunk, List<String> whitelist) {
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager rm = container.get(BukkitAdapter.adapt(chunk.getWorld()));
      if (rm == null) {
         return false;
      } else {
         int minX = chunk.getX() << 4;
         int minZ = chunk.getZ() << 4;
         int maxX = minX + 15;
         int maxZ = minZ + 15;

         for (ProtectedRegion region : rm.getRegions().values()) {
            if (whitelist.isEmpty() || whitelist.contains(region.getId())) {
               BlockVector3 rMin = region.getMinimumPoint();
               BlockVector3 rMax = region.getMaximumPoint();
               if (rMin.getX() <= maxX && rMax.getX() >= minX && rMin.getZ() <= maxZ && rMax.getZ() >= minZ) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public static Set<Long> getRegionChunks(World world, List<String> whitelist) {
      Set<Long> result = new HashSet<>();
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager rm = container.get(BukkitAdapter.adapt(world));
      if (rm == null) {
         return result;
      } else {
         for (ProtectedRegion region : rm.getRegions().values()) {
            if (whitelist.isEmpty() || whitelist.contains(region.getId())) {
               BlockVector3 rMin = region.getMinimumPoint();
               BlockVector3 rMax = region.getMaximumPoint();
               int minCX = rMin.getX() >> 4;
               int maxCX = rMax.getX() >> 4;
               int minCZ = rMin.getZ() >> 4;
               int maxCZ = rMax.getZ() >> 4;

               for (int cx = minCX; cx <= maxCX; cx++) {
                  for (int cz = minCZ; cz <= maxCZ; cz++) {
                     result.add((long)cx << 32 | cz & 4294967295L);
                  }
               }
            }
         }

         return result;
      }
   }
}
