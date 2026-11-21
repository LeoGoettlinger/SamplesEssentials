package com.magmaguy.elitemobs.magmacore.initialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginInitializationProgressBar {
   private static final Map<String, PluginInitializationProgressBar.ProgressBarData> progressBars = new ConcurrentHashMap<>();

   private PluginInitializationProgressBar() {
   }

   public static void start(JavaPlugin plugin, String displayName, String permission, int totalSteps) {
      install(new PluginInitializationProgressBar.ProgressBarData(plugin, null, 0L, displayName, permission, totalSteps), true);
   }

   static void start(JavaPlugin plugin, String ownerToken, long generation, String displayName, String permission, int totalSteps) {
      install(new PluginInitializationProgressBar.ProgressBarData(plugin, ownerToken, generation, displayName, permission, totalSteps), false);
   }

   private static void install(PluginInitializationProgressBar.ProgressBarData replacement, boolean force) {
      String pluginName = replacement.plugin.getName();

      PluginInitializationProgressBar.ProgressBarData previous;
      boolean installed;
      do {
         previous = progressBars.get(pluginName);
         if (!force && previous != null && previous.generation > replacement.generation) {
            return;
         }

         installed = previous == null ? progressBars.putIfAbsent(pluginName, replacement) == null : progressBars.replace(pluginName, previous, replacement);
      } while (!installed);

      if (previous != null) {
         previous.complete();
      }

      replacement.start();
   }

   public static void step(JavaPlugin plugin, String description) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.get(plugin.getName());
      if (progressBarData != null) {
         progressBarData.step(description);
      }
   }

   static void step(JavaPlugin plugin, String ownerToken, String description) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.get(plugin.getName());
      if (progressBarData != null && progressBarData.isOwnedBy(ownerToken)) {
         progressBarData.step(description);
      }
   }

   public static void status(JavaPlugin plugin, String description) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.get(plugin.getName());
      if (progressBarData != null) {
         progressBarData.status(description);
      }
   }

   static void status(JavaPlugin plugin, String ownerToken, String description) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.get(plugin.getName());
      if (progressBarData != null && progressBarData.isOwnedBy(ownerToken)) {
         progressBarData.status(description);
      }
   }

   public static void complete(JavaPlugin plugin) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.remove(plugin.getName());
      if (progressBarData != null) {
         progressBarData.complete();
      }
   }

   static void complete(JavaPlugin plugin, String ownerToken) {
      PluginInitializationProgressBar.ProgressBarData progressBarData = progressBars.get(plugin.getName());
      if (progressBarData != null && progressBarData.isOwnedBy(ownerToken)) {
         if (progressBars.remove(plugin.getName(), progressBarData)) {
            progressBarData.complete();
         }
      }
   }

   private static class ProgressBarData {
      private final JavaPlugin plugin;
      private final String ownerToken;
      private final long generation;
      private final String displayName;
      private final String permission;
      private final int totalSteps;
      private int currentStep;
      private BossBar bossBar;
      private Listener listener;
      private boolean completed;

      private ProgressBarData(JavaPlugin plugin, String ownerToken, long generation, String displayName, String permission, int totalSteps) {
         this.plugin = plugin;
         this.ownerToken = ownerToken;
         this.generation = generation;
         this.displayName = displayName;
         this.permission = permission;
         this.totalSteps = totalSteps;
      }

      private synchronized void start() {
         if (!this.completed) {
            this.currentStep = 0;
            this.bossBar = Bukkit.createBossBar(
               "" + ChatColor.GREEN + ChatColor.BOLD + this.displayName + ChatColor.WHITE + " ▸ Initializing...",
               BarColor.GREEN,
               BarStyle.SEGMENTED_10,
               new BarFlag[0]
            );
            this.bossBar.setProgress(0.0);

            for (Player player : Bukkit.getOnlinePlayers()) {
               if (this.canView(player)) {
                  this.bossBar.addPlayer(player);
               }
            }

            this.listener = new Listener() {
               @EventHandler
               public void onPlayerJoin(PlayerJoinEvent event) {
                  if (ProgressBarData.this.bossBar != null && ProgressBarData.this.canView(event.getPlayer())) {
                     ProgressBarData.this.bossBar.addPlayer(event.getPlayer());
                  }
               }
            };
            Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
         }
      }

      private synchronized void step(String description) {
         if (!this.completed) {
            this.currentStep++;
            double progress = this.totalSteps <= 0 ? 1.0 : Math.min((double)this.currentStep / this.totalSteps, 1.0);
            this.update(description, progress);
         }
      }

      private synchronized void status(String description) {
         if (!this.completed) {
            double progress = this.totalSteps <= 0 ? 0.0 : Math.min((double)this.currentStep / this.totalSteps, 1.0);
            this.update(description, progress);
         }
      }

      private void update(String description, double progress) {
         Runnable task = () -> {
            synchronized (this) {
               if (!this.completed && this.bossBar != null) {
                  this.bossBar.setTitle("" + ChatColor.GREEN + ChatColor.BOLD + this.displayName + ChatColor.WHITE + " ▸ " + ChatColor.YELLOW + description);
                  this.bossBar.setProgress(progress);
               }
            }
         };
         if (Bukkit.isPrimaryThread()) {
            task.run();
         } else {
            Bukkit.getScheduler().runTask(this.plugin, task);
         }
      }

      private synchronized void complete() {
         if (!this.completed) {
            this.completed = true;
            if (this.bossBar != null) {
               this.bossBar.removeAll();
               this.bossBar = null;
            }

            if (this.listener != null) {
               HandlerList.unregisterAll(this.listener);
               this.listener = null;
            }
         }
      }

      private boolean canView(Player player) {
         return this.permission != null && !this.permission.isBlank() ? player.hasPermission(this.permission) : player.isOp();
      }

      private boolean isOwnedBy(String candidateOwnerToken) {
         return this.ownerToken != null && this.ownerToken.equals(candidateOwnerToken);
      }
   }
}
