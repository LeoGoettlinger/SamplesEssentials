package cm.chunkManager.components;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PortalPreloader implements Listener {
   private static final long COOLDOWN_MS = 3000L;
   private final JavaPlugin plugin;
   private final ChunkPrefetcher prefetcher;
   private boolean enabled;
   private int radius;
   private final Map<UUID, Long> cooldowns;

   public PortalPreloader(JavaPlugin plugin, ChunkPrefetcher prefetcher, int radius) {
      this.plugin = plugin;
      this.prefetcher = prefetcher;
      this.enabled = true;
      this.radius = radius;
      this.cooldowns = new ConcurrentHashMap<>();
   }

   @EventHandler
   public void onPortal(PlayerPortalEvent event) {
      if (this.enabled) {
         UUID uuid = event.getPlayer().getUniqueId();
         long now = System.currentTimeMillis();
         Long lastUse = this.cooldowns.get(uuid);
         if (lastUse == null || now - lastUse >= 3000L) {
            this.cooldowns.put(uuid, now);
            Location from = event.getFrom();
            Location destination = null;
            switch (event.getCause()) {
               case NETHER_PORTAL:
                  destination = this.computeNetherDestination(from);
                  break;
               case END_PORTAL:
                  World endWorld = this.plugin.getServer().getWorld("world_the_end");
                  if (endWorld != null) {
                     destination = endWorld.getSpawnLocation();
                  }
            }

            if (destination != null && destination.getWorld() != null) {
               this.prefetcher.prefetchArea(destination.getWorld(), destination, this.radius);
            }
         }
      }
   }

   public Location computeNetherDestination(Location from) {
      if (from != null && from.getWorld() != null) {
         Environment env = from.getWorld().getEnvironment();
         if (env == Environment.NORMAL) {
            World nether = this.plugin.getServer().getWorld("world_nether");
            return nether == null ? null : new Location(nether, from.getX() / 8.0, from.getY(), from.getZ() / 8.0);
         } else if (env == Environment.NETHER) {
            World overworld = this.plugin.getServer().getWorld("world");
            return overworld == null ? null : new Location(overworld, from.getX() * 8.0, from.getY(), from.getZ() * 8.0);
         } else {
            return null;
         }
      } else {
         return null;
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
}
