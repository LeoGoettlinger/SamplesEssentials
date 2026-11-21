package cm.chunkManager.components;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeleportPreloader {
   private static final long COOLDOWN_MS = 3000L;
   private final JavaPlugin plugin;
   private final ChunkPrefetcher prefetcher;
   private boolean enabled;
   private int radius;
   private final Map<UUID, Long> cooldowns;

   public TeleportPreloader(JavaPlugin plugin, ChunkPrefetcher prefetcher, int radius) {
      this.plugin = plugin;
      this.prefetcher = prefetcher;
      this.enabled = true;
      this.radius = radius;
      this.cooldowns = new ConcurrentHashMap<>();
   }

   public void onTeleport(Player player, Location destination) {
      if (this.enabled && destination != null && destination.getWorld() != null) {
         UUID uuid = player.getUniqueId();
         long now = System.currentTimeMillis();
         Long lastUse = this.cooldowns.get(uuid);
         if (lastUse == null || now - lastUse >= 3000L) {
            this.cooldowns.put(uuid, now);
            this.prefetcher.prefetchArea(destination.getWorld(), destination, this.radius);
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setRadius(int radius) {
      this.radius = radius;
   }

   public int getRadius() {
      return this.radius;
   }

   public void onPlayerQuit(UUID uuid) {
      this.cooldowns.remove(uuid);
   }
}
