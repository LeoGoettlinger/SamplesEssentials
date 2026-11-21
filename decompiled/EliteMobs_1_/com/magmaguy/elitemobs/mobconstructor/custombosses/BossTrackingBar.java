package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import com.magmaguy.elitemobs.wormhole.WormholeNavigation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class BossTrackingBar implements BossTrackingLifecycle.Handle {
   private static final String DEFAULT_LOCATION_MESSAGE = "$name: $distance blocks away!";
   private final CustomBossEntity customBossEntity;
   private final Map<Player, BossBar> bossBars = new HashMap<>();
   private final HashSet<Player> trackingPlayers = new HashSet<>();
   private BukkitTask bossBarUpdater;

   public BossTrackingBar(CustomBossEntity customBossEntity) {
      this.customBossEntity = customBossEntity;
      this.start();
   }

   @Override
   public void notifyPlayers() {
      Location bossLoc = this.customBossEntity.getLocation();
      if (bossLoc != null && bossLoc.getWorld() != null) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(bossLoc.getWorld())) {
               TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.getBossLocationMessage());
               interactiveMessage.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/elitemobs track boss " + this.customBossEntity.getEliteUUID()));
               interactiveMessage.setHoverEvent(
                  new HoverEvent(
                     net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                     new ComponentBuilder(DefaultConfig.getTrackMessage().replace("$name", this.customBossEntity.getName())).create()
                  )
               );
               player.spigot().sendMessage(interactiveMessage);
            }
         }
      }
   }

   public void addTrackingPlayer(Player player) {
      if (!this.customBossEntity.exists()) {
         this.remove();
      } else if (this.trackingPlayers.contains(player)) {
         this.removeTrackingPlayer(player);
      } else {
         this.trackingPlayers.add(player);
      }
   }

   public void removeTrackingPlayer(Player player) {
      this.trackingPlayers.remove(player);
      BossBar bossBar = this.bossBars.remove(player);
      if (bossBar != null) {
         BossBarOrderManager.hide(player, bossBar);
      }
   }

   public void remove() {
      this.customBossEntity.removeTracking(this);
   }

   @Override
   public void dispose() {
      this.bossBars.forEach((player, bossBar) -> BossBarOrderManager.hide(player, bossBar));
      this.bossBars.clear();
      this.trackingPlayers.clear();
      if (this.bossBarUpdater != null) {
         this.bossBarUpdater.cancel();
      }
   }

   public String bossBarMessage(Player player, String locationString) {
      String locationMessage = this.getLocationMessageTemplate();
      Location trackedLocation = this.getTrackedLocation(player);
      if (trackedLocation == null) {
         return MobCombatSettingsConfig.getDefaultOtherWorldBossLocationMessage().replace("$name", this.customBossEntity.getName());
      } else if (!locationMessage.contains("$distance") && !locationMessage.contains("$location") && !locationMessage.contains("$name")) {
         return locationMessage;
      } else {
         String trackedLocationString = trackedLocation.getBlockX() + ", " + trackedLocation.getBlockY() + ", " + trackedLocation.getBlockZ();
         return locationMessage.replace("$name", this.customBossEntity.getName())
            .replace("$distance", (int)trackedLocation.distance(player.getLocation()) + "")
            .replace("$location", trackedLocationString);
      }
   }

   private void updateBossBar(Player player, BossBar bossBar) {
      if (player != null) {
         if (bossBar != null) {
            Location loc = this.customBossEntity.getLocation();
            if (loc != null && loc.getWorld() != null) {
               String locationString = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
               bossBar.setTitle(this.bossBarMessage(player, locationString));
               double progress = Math.min(1.0, Math.max(0.0, this.customBossEntity.getHealth() / this.customBossEntity.getMaxHealth()));
               bossBar.setProgress(progress);
            }
         }
      }
   }

   public void start() {
      this.bossBarUpdater = (new BukkitRunnable() {
         public void run() {
            if (!BossTrackingBar.this.customBossEntity.exists()) {
               this.cancel();
               BossTrackingBar.this.remove();
            } else {
               Location bossLocation = BossTrackingBar.this.customBossEntity.getLocation();
               if (bossLocation != null && bossLocation.getWorld() != null && Bukkit.getWorld(bossLocation.getWorld().getUID()) != null) {
                  BossTrackingBar.this.trackingPlayers.removeIf(playerx -> !playerx.isOnline());

                  for (Player player : new HashSet<>(BossTrackingBar.this.trackingPlayers)) {
                     if (!BossTrackingBar.this.bossBars.containsKey(player)) {
                        BossTrackingBar.this.createBossBar(player);
                     } else {
                        BossTrackingBar.this.updateBossBar(player, BossTrackingBar.this.bossBars.get(player));
                     }
                  }

                  BossTrackingBar.this.bossBars.entrySet().removeIf(entry -> {
                     Player playerx = entry.getKey();
                     boolean shouldRemove = !playerx.isOnline() || !BossTrackingBar.this.trackingPlayers.contains(playerx);
                     if (shouldRemove) {
                        BossBarOrderManager.hide(playerx, entry.getValue());
                     }

                     return shouldRemove;
                  });
               } else {
                  this.cancel();
                  BossTrackingBar.this.remove();
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 5L);
   }

   private void createBossBar(Player player) {
      Location loc = this.customBossEntity.getLocation();
      if (loc != null && loc.getWorld() != null) {
         String locationString = (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ();
         BossBar bossBar = Bukkit.createBossBar(
            this.bossBarMessage(player, locationString), BarColor.GREEN, BarStyle.SOLID, new BarFlag[]{BarFlag.PLAY_BOSS_MUSIC}
         );
         if (this.customBossEntity.exists()) {
            this.bossBars.put(player, bossBar);
            this.updateBossBar(player, bossBar);
            BossBarOrderManager.show(player, bossBar, BossBarOrderManager.sortKeyFor(this.customBossEntity.getEliteUUID()));
         }
      }
   }

   private String getLocationMessageTemplate() {
      String configuredLocationMessage = this.customBossEntity.getCustomBossesConfigFields().getLocationMessage();
      return configuredLocationMessage != null && !configuredLocationMessage.isBlank() ? configuredLocationMessage : "$name: $distance blocks away!";
   }

   private Location getTrackedLocation(Player player) {
      if (player == null) {
         return null;
      } else {
         Location playerLocation = player.getLocation();
         Location bossLocation = this.customBossEntity.getLocation();
         return WormholeNavigation.findNavigationTarget(playerLocation, bossLocation);
      }
   }
}
