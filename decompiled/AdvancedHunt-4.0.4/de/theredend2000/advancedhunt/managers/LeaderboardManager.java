package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Cache;
import de.theredend2000.advancedhunt.lib.caffeine.cache.Caffeine;
import de.theredend2000.advancedhunt.model.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class LeaderboardManager {
   private final JavaPlugin plugin;
   private final DataRepository repository;
   private final Map<String, List<LeaderboardManager.LeaderboardEntry>> leaderboardCache = new ConcurrentHashMap<>();
   private final Cache<UUID, String> nameCache;
   private final int displayLimit;
   private BukkitTask updateTask;

   public LeaderboardManager(JavaPlugin plugin, DataRepository repository) {
      this.plugin = plugin;
      this.repository = repository;
      this.displayLimit = plugin.getConfig().getInt("leaderboard.display-limit", 100);
      int refreshInterval = plugin.getConfig().getInt("leaderboard.cache-refresh-interval", 300);
      this.nameCache = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).maximumSize(5000L).build();
      long intervalTicks = 20L * refreshInterval;
      this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateLeaderboards, 0L, intervalTicks);
   }

   public void stop() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }
   }

   private void updateLeaderboards() {
      this.repository.rebuildLeaderboardCache();
      this.repository.loadCollections().thenAccept(collections -> {
         for (Collection c : collections) {
            this.repository.getLeaderboard(c.getId(), this.displayLimit).thenAccept(map -> {
               List<LeaderboardManager.LeaderboardEntry> entries = new ArrayList<>();
               int rank = 1;

               for (Entry<UUID, Integer> entry : map.entrySet()) {
                  UUID playerId = entry.getKey();
                  String playerName = this.nameCache.get(playerId, uuid -> {
                     String name = Bukkit.getOfflinePlayer(uuid).getName();
                     return name != null ? name : "Unknown";
                  });
                  entries.add(new LeaderboardManager.LeaderboardEntry(rank++, playerId, playerName, entry.getValue()));
               }

               this.leaderboardCache.put(c.getName(), entries);
            });
         }
      });
      this.repository.flushIndexes();
   }

   public LeaderboardManager.LeaderboardEntry getEntry(String collectionName, int rank) {
      List<LeaderboardManager.LeaderboardEntry> entries = this.leaderboardCache.get(collectionName);
      return entries != null && rank >= 1 && rank <= entries.size() ? entries.get(rank - 1) : null;
   }

   public List<LeaderboardManager.LeaderboardEntry> getEntries(String collectionName) {
      return this.leaderboardCache.get(collectionName);
   }

   public void forceUpdate() {
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, this::updateLeaderboards);
   }

   public static class LeaderboardEntry {
      private final int rank;
      private final UUID playerId;
      private final String playerName;
      private final int score;

      public LeaderboardEntry(int rank, UUID playerId, String playerName, int score) {
         this.rank = rank;
         this.playerId = playerId;
         this.playerName = playerName;
         this.score = score;
      }

      public int getRank() {
         return this.rank;
      }

      public UUID getPlayerId() {
         return this.playerId;
      }

      public String getPlayerName() {
         return this.playerName;
      }

      public int getScore() {
         return this.score;
      }
   }
}
