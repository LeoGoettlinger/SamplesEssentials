package cm.chunkManager.components;

import cm.chunkManager.utils.ChunkUtils;
import cm.chunkManager.utils.Scheduler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkTicketManager {
   private final JavaPlugin plugin;
   private final Map<String, ChunkTicketManager.TicketData> tickets;
   private volatile boolean enabled;
   private Scheduler.Handle cleanupHandle;
   private static Method addTicketMethod;
   private static Method removeTicketMethod;
   private static boolean ticketApiChecked = false;
   private static boolean ticketApiAvailable = false;

   public ChunkTicketManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.tickets = new ConcurrentHashMap<>();
      this.enabled = true;
      this.checkTicketApi();
      this.startCleanup();
   }

   private void checkTicketApi() {
      if (!ticketApiChecked) {
         ticketApiChecked = true;

         try {
            addTicketMethod = Chunk.class.getMethod("addPluginChunkTicket", Plugin.class);
            removeTicketMethod = Chunk.class.getMethod("removePluginChunkTicket", Plugin.class);
            ticketApiAvailable = true;
         } catch (NoSuchMethodException var2) {
            ticketApiAvailable = false;
         }
      }
   }

   private void startCleanup() {
      this.cleanupHandle = Scheduler.runGlobalTimer(this.plugin, this::cleanup, 200L, 200L);
   }

   public void addTicket(World world, int cx, int cz, String reason, long durationMs) {
      if (this.enabled) {
         String key = this.ticketKey(world.getName(), cx, cz);
         this.tickets.put(key, new ChunkTicketManager.TicketData(world.getName(), cx, cz, reason, durationMs));
         if (ticketApiAvailable) {
            ChunkUtils.getChunkAtAsync(world, cx, cz, this.plugin).thenAccept(chunk -> {
               try {
                  addTicketMethod.invoke(chunk, this.plugin);
               } catch (Exception var3x) {
               }
            });
         } else {
            ChunkUtils.getChunkAtAsync(world, cx, cz, this.plugin);
         }
      }
   }

   public void removeTicket(World world, int cx, int cz) {
      String key = this.ticketKey(world.getName(), cx, cz);
      ChunkTicketManager.TicketData data = this.tickets.remove(key);
      if (data != null) {
         if (ticketApiAvailable) {
            ChunkUtils.getChunkAtAsync(world, cx, cz, this.plugin).thenAccept(chunk -> {
               try {
                  removeTicketMethod.invoke(chunk, this.plugin);
               } catch (Exception var3x) {
               }
            });
         }
      }
   }

   public boolean hasTicket(String worldName, int cx, int cz) {
      String key = this.ticketKey(worldName, cx, cz);
      ChunkTicketManager.TicketData data = this.tickets.get(key);
      return data != null && !data.isExpired();
   }

   public void cleanup() {
      this.tickets.entrySet().removeIf(entry -> {
         ChunkTicketManager.TicketData data = entry.getValue();
         if (!data.isExpired()) {
            return false;
         } else {
            if (ticketApiAvailable) {
               World world = Bukkit.getWorld(data.world);
               if (world != null) {
                  ChunkUtils.getChunkAtAsync(world, data.cx, data.cz, this.plugin).thenAccept(chunk -> {
                     try {
                        removeTicketMethod.invoke(chunk, this.plugin);
                     } catch (Exception var3x) {
                     }
                  });
               }
            }

            return true;
         }
      });
   }

   public int getActiveTicketCount() {
      return (int)this.tickets.values().stream().filter(t -> !t.isExpired()).count();
   }

   public void shutdown() {
      if (this.cleanupHandle != null) {
         this.cleanupHandle.cancel();
      }

      for (Entry<String, ChunkTicketManager.TicketData> entry : this.tickets.entrySet()) {
         ChunkTicketManager.TicketData data = entry.getValue();
         if (ticketApiAvailable) {
            World world = Bukkit.getWorld(data.world);
            if (world != null && world.isChunkLoaded(data.cx, data.cz)) {
               try {
                  Chunk chunk = world.getChunkAt(data.cx, data.cz);
                  removeTicketMethod.invoke(chunk, this.plugin);
               } catch (Exception var6) {
               }
            }
         }
      }

      this.tickets.clear();
      this.enabled = false;
   }

   private String ticketKey(String worldName, int cx, int cz) {
      return worldName + ":" + cx + ":" + cz;
   }

   public static class TicketData {
      public final String world;
      public final int cx;
      public final int cz;
      public final String reason;
      public final long expiry;
      public final long createdAt;

      public TicketData(String world, int cx, int cz, String reason, long durationMs) {
         this.world = world;
         this.cx = cx;
         this.cz = cz;
         this.reason = reason;
         this.createdAt = System.currentTimeMillis();
         this.expiry = this.createdAt + durationMs;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() >= this.expiry;
      }
   }
}
