package cm.chunkManager.hooks;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class WgBridge {
   private static Boolean available;

   public static boolean isAvailable() {
      if (available == null) {
         available = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
      }

      return available;
   }

   public static boolean isChunkProtected(Chunk chunk, List<String> whitelist) {
      if (!isAvailable()) {
         return false;
      } else {
         try {
            return WgHook.isChunkInRegion(chunk, whitelist);
         } catch (Exception | NoClassDefFoundError var3) {
            return false;
         }
      }
   }

   public static Set<Long> getRegionChunks(World world, List<String> whitelist) {
      if (!isAvailable()) {
         return Collections.emptySet();
      } else {
         try {
            return WgHook.getRegionChunks(world, whitelist);
         } catch (Exception | NoClassDefFoundError var3) {
            return Collections.emptySet();
         }
      }
   }
}
