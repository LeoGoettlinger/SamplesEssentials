package me.usainsrht.ujobs.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.morepaperlib.scheduling.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;

public class BossBarManager {
   private final UJobsPlugin plugin;
   private final Map<UUID, Map<String, BossBarManager.BossBarData>> playerBossBars;

   public BossBarManager(UJobsPlugin plugin) {
      this.plugin = plugin;
      this.playerBossBars = new ConcurrentHashMap<>();
   }

   public void showBossBar(Player player, String key, BossBar bossBar, int durationSeconds) {
      UUID playerId = player.getUniqueId();
      Map<String, BossBarManager.BossBarData> bossBars = this.playerBossBars.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
      BossBarManager.BossBarData existing = bossBars.get(key);
      if (existing != null) {
         existing.bossBar.progress(bossBar.progress());
         existing.bossBar.name(bossBar.name());
         bossBar = existing.bossBar;
         if (existing.task != null && !existing.task.isCancelled()) {
            existing.task.cancel();
         }
      } else {
         player.showBossBar(bossBar);
      }

      BossBar finalBossBar = bossBar;
      Runnable cleanup = () -> {
         bossBars.remove(key);
         if (bossBars.isEmpty()) {
            this.playerBossBars.remove(playerId);
         }
      };
      ScheduledTask hideTask = this.plugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).runDelayed(() -> {
         this.hideBossBar(player, finalBossBar);
         cleanup.run();
      }, cleanup, durationSeconds * 20L);
      bossBars.put(key, new BossBarManager.BossBarData(bossBar, hideTask));
   }

   public void hideBossBar(Player player, BossBar bossBar) {
      player.hideBossBar(bossBar);
   }

   public void hideBossBar(Player player, String key) {
      UUID playerId = player.getUniqueId();
      Map<String, BossBarManager.BossBarData> bossBars = this.playerBossBars.get(playerId);
      if (bossBars != null) {
         BossBarManager.BossBarData bossBarData = bossBars.remove(key);
         if (bossBarData != null) {
            this.hideBossBar(player, bossBarData.bossBar);
            if (bossBarData.task != null && !bossBarData.task.isCancelled()) {
               bossBarData.task.cancel();
            }
         }

         if (bossBars.isEmpty()) {
            this.playerBossBars.remove(playerId);
         }
      }
   }

   public void removePlayerBossBars(Player player) {
      UUID playerId = player.getUniqueId();
      Map<String, BossBarManager.BossBarData> bossBars = this.playerBossBars.remove(playerId);
      if (bossBars != null) {
         for (BossBarManager.BossBarData bossBarData : bossBars.values()) {
            this.hideBossBar(player, bossBarData.bossBar);
            if (bossBarData.task != null && !bossBarData.task.isCancelled()) {
               bossBarData.task.cancel();
            }
         }
      }
   }

   public void removeAllBossBars() {
      for (UUID playerId : this.playerBossBars.keySet()) {
         Player player = this.plugin.getServer().getPlayer(playerId);
         if (player != null) {
            this.removePlayerBossBars(player);
         }
      }

      this.playerBossBars.clear();
   }

   public boolean hasBossBar(Player player, String key) {
      UUID playerId = player.getUniqueId();
      Map<String, BossBarManager.BossBarData> bossBars = this.playerBossBars.get(playerId);
      return bossBars != null && bossBars.containsKey(key);
   }

   public void updateBossBar(Player player, String key, BossBar newBossBar) {
      UUID playerId = player.getUniqueId();
      Map<String, BossBarManager.BossBarData> bossBars = this.playerBossBars.get(playerId);
      if (bossBars != null) {
         BossBarManager.BossBarData existing = bossBars.get(key);
         if (existing != null) {
            this.hideBossBar(player, existing.bossBar);
            player.showBossBar(newBossBar);
            bossBars.put(key, new BossBarManager.BossBarData(newBossBar, existing.task));
         }
      }
   }

   private static class BossBarData {
      final BossBar bossBar;
      final ScheduledTask task;

      BossBarData(BossBar bossBar, ScheduledTask task) {
         this.bossBar = bossBar;
         this.task = task;
      }
   }
}
