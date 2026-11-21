package com.lishid.openinv.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.errorprone.annotations.Keep;
import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.profile.OfflinePlayerProfileStore;
import com.lishid.openinv.util.profile.Profile;
import com.lishid.openinv.util.profile.ProfileStore;
import com.lishid.openinv.util.profile.sqlite.SqliteProfileStore;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerLoader implements Listener {
   @NotNull
   private final OpenInv plugin;
   @NotNull
   private final Config config;
   @NotNull
   private final InventoryManager inventoryManager;
   @NotNull
   private final InternalAccessor internalAccessor;
   @NotNull
   private final Logger logger;
   @NotNull
   private final Cache<String, Profile> lookupCache;
   @NotNull
   private ProfileStore profileStore;

   public PlayerLoader(
      @NotNull OpenInv plugin,
      @NotNull Config config,
      @NotNull InventoryManager inventoryManager,
      @NotNull InternalAccessor internalAccessor,
      @NotNull Logger logger
   ) {
      this.plugin = plugin;
      this.config = config;
      this.inventoryManager = inventoryManager;
      this.internalAccessor = internalAccessor;

      try {
         SqliteProfileStore sqliteStore = new SqliteProfileStore(plugin);
         sqliteStore.setup();
         sqliteStore.tryImport();
         this.profileStore = sqliteStore;
      } catch (Exception var7) {
         this.profileStore = new OfflinePlayerProfileStore(logger);
      }

      this.logger = logger;
      this.lookupCache = CacheBuilder.newBuilder().maximumSize(20L).build();
   }

   @NotNull
   public ProfileStore getProfileStore() {
      return this.profileStore;
   }

   public void setProfileStore(@NotNull ProfileStore profileStore) {
      this.plugin.getLogger().log(Level.INFO, () -> "Setting profile store implementation to " + profileStore.getClass().getName());
      this.profileStore = profileStore;
   }

   @Nullable
   public Player load(@NotNull OfflinePlayer offline) {
      Player player = offline.getPlayer();
      if (player != null) {
         return player;
      } else if (!this.config.isOfflineDisabled() && this.internalAccessor.isSupported()) {
         player = this.inventoryManager.getLoadedPlayer(offline.getUniqueId());
         if (player != null) {
            return player;
         } else if (Bukkit.isPrimaryThread()) {
            return this.internalAccessor.getPlayerDataManager().loadPlayer(offline);
         } else {
            CompletableFuture<Player> future = new CompletableFuture<>();
            this.plugin.getScheduler().runTask(() -> future.complete(this.internalAccessor.getPlayerDataManager().loadPlayer(offline)));

            try {
               return future.get();
            } catch (ExecutionException | InterruptedException var5) {
               this.logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
               return null;
            }
         }
      } else {
         return null;
      }
   }

   @Nullable
   public OfflinePlayer matchExact(@NotNull String name) {
      try {
         UUID uuid = UUID.fromString(name);
         OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
         return !player.hasPlayedBefore() && !player.isOnline() ? null : player;
      } catch (IllegalArgumentException var5) {
         OfflinePlayer playerx = Bukkit.getServer().getPlayerExact(name);
         if (playerx != null) {
            return playerx;
         } else {
            Profile cachedResult = (Profile)this.lookupCache.getIfPresent(name);
            if (cachedResult != null) {
               playerx = Bukkit.getOfflinePlayer(cachedResult.id());
               return !playerx.hasPlayedBefore() && !playerx.isOnline() ? null : playerx;
            } else {
               Profile profile = this.profileStore.getProfileExact(name);
               if (profile == null) {
                  return null;
               } else {
                  playerx = Bukkit.getOfflinePlayer(profile.id());
                  if (playerx.hasPlayedBefore()) {
                     this.lookupCache.put(name, profile);
                     return playerx;
                  } else {
                     return null;
                  }
               }
            }
         }
      }
   }

   @Nullable
   public OfflinePlayer match(@NotNull String name) {
      OfflinePlayer player = this.matchExact(name);
      if (player != null) {
         return player;
      } else {
         OfflinePlayer var4 = Bukkit.getServer().getPlayer(name);
         if (var4 != null) {
            return var4;
         } else {
            Profile profile = this.getProfileStore().getProfileInexact(name);
            if (profile == null) {
               return null;
            } else {
               player = Bukkit.getOfflinePlayer(profile.id());
               if (player.hasPlayedBefore()) {
                  this.lookupCache.put(name, profile);
                  return player;
               } else {
                  return null;
               }
            }
         }
      }
   }

   @Keep
   @EventHandler
   private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
      this.plugin.getScheduler().runTaskLaterAsynchronously(() -> this.updateMatches(event), 7L);
   }

   private void updateMatches(@NotNull PlayerJoinEvent event) {
      this.profileStore.addProfile(new Profile(event.getPlayer()));
      if (!event.getPlayer().hasPlayedBefore() && this.lookupCache.size() != 0L) {
         String name = event.getPlayer().getName();
         this.lookupCache.invalidate(name);
         Iterator<Entry<String, Profile>> iterator = this.lookupCache.asMap().entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<String, Profile> entry = iterator.next();
            String oldMatch = entry.getValue().name();
            String lookup = entry.getKey();
            float oldMatchScore = StringMetric.compareJaroWinkler(lookup, oldMatch);
            float newMatchScore = StringMetric.compareJaroWinkler(lookup, name);
            if (newMatchScore > oldMatchScore) {
               iterator.remove();
            }
         }
      }
   }
}
