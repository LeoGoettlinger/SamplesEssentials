package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.model.PlayerData;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerManager implements Listener {
   private static final long DEFAULT_TIMEOUT_MS = 5000L;
   private final DataRepository repository;
   private final Cache<UUID, PlayerData> playerDataCache;
   private final Logger logger;

   public PlayerManager(JavaPlugin plugin, DataRepository repository) {
      this.repository = repository;
      this.logger = plugin.getLogger();
      this.playerDataCache = Caffeine.newBuilder().expireAfterAccess(10L, TimeUnit.MINUTES).maximumSize(1000L).build();
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public boolean isPlayerDataLoaded(UUID uuid) {
      return this.playerDataCache.getIfPresent(uuid) != null;
   }

   public PlayerData getPlayerData(UUID uuid) {
      PlayerData cached = this.playerDataCache.getIfPresent(uuid);
      if (cached != null) {
         return cached;
      } else {
         PlayerData placeholder = new PlayerData(uuid);
         this.playerDataCache.put(uuid, placeholder);
         this.repository.loadPlayerData(uuid).thenAccept(loaded -> {
            if (loaded != null) {
               this.playerDataCache.put(uuid, loaded);
            }
         }).exceptionally(ex -> {
            this.logger.log(Level.WARNING, "Failed to load player data for " + uuid, ex);
            return null;
         });
         return placeholder;
      }
   }

   public PlayerData getPlayerDataWithTimeout(UUID uuid, long timeoutMs) {
      PlayerData cached = this.playerDataCache.getIfPresent(uuid);
      if (cached != null) {
         return cached;
      } else {
         try {
            PlayerData loaded = this.repository.loadPlayerData(uuid).get(timeoutMs, TimeUnit.MILLISECONDS);
            if (loaded == null) {
               loaded = new PlayerData(uuid);
            }

            this.playerDataCache.put(uuid, loaded);
            return loaded;
         } catch (TimeoutException var7) {
            this.logger.log(Level.WARNING, "Timeout loading player data for " + uuid);
            PlayerData placeholder = new PlayerData(uuid);
            this.playerDataCache.put(uuid, placeholder);
            return placeholder;
         } catch (Exception var8) {
            this.logger.log(Level.WARNING, "Exception loading player data for " + uuid, (Throwable)var8);
            PlayerData placeholderx = new PlayerData(uuid);
            this.playerDataCache.put(uuid, placeholderx);
            return placeholderx;
         }
      }
   }

   public PlayerData getPlayerDataWithTimeout(UUID uuid) {
      return this.getPlayerDataWithTimeout(uuid, 5000L);
   }

   public void savePlayerData(UUID uuid) {
      PlayerData data = this.playerDataCache.getIfPresent(uuid);
      if (data != null) {
         this.repository.savePlayerData(data);
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.repository.loadPlayerData(event.getPlayer().getUniqueId()).thenAccept(data -> this.playerDataCache.put(event.getPlayer().getUniqueId(), data));
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      UUID uuid = event.getPlayer().getUniqueId();
      PlayerData data = this.playerDataCache.getIfPresent(uuid);
      if (data != null) {
         this.repository.savePlayerData(data);
      }
   }

   public void invalidate(UUID uuid) {
      this.playerDataCache.invalidate(uuid);
   }

   public void stop() {
      HandlerList.unregisterAll(this);
      this.playerDataCache.invalidateAll();
   }
}
