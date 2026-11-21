package cm.chunkManager.utils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChunkUtils {
   private static Boolean paperAvailable = null;
   private static Method getChunkAtAsyncMethod = null;

   private ChunkUtils() {
   }

   public static boolean isPaperServer() {
      if (paperAvailable == null) {
         try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paperAvailable = true;
         } catch (ClassNotFoundException var3) {
            try {
               Class.forName("io.papermc.paper.configuration.Configuration");
               paperAvailable = true;
            } catch (ClassNotFoundException var2) {
               paperAvailable = false;
            }
         }
      }

      return paperAvailable;
   }

   public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z, JavaPlugin plugin) {
      if (!isPaperServer() && !Scheduler.isFolia()) {
         return getChunkSync(world, x, z, plugin);
      } else {
         try {
            if (getChunkAtAsyncMethod == null) {
               getChunkAtAsyncMethod = World.class.getMethod("getChunkAtAsync", int.class, int.class);
            }

            return (CompletableFuture<Chunk>)getChunkAtAsyncMethod.invoke(world, x, z);
         } catch (Exception var5) {
            return getChunkSync(world, x, z, plugin);
         }
      }
   }

   private static CompletableFuture<Chunk> getChunkSync(World world, int x, int z, JavaPlugin plugin) {
      CompletableFuture<Chunk> future = new CompletableFuture<>();
      Scheduler.runGlobal(plugin, () -> future.complete(world.getChunkAt(x, z)));
      return future;
   }

   public static void loadChunkAsync(World world, int x, int z, JavaPlugin plugin, Runnable callback) {
      getChunkAtAsync(world, x, z, plugin).thenRun(() -> {
         if (callback != null) {
            Scheduler.runGlobal(plugin, callback);
         }
      });
   }
}
