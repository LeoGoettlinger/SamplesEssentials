package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownBossBarSettings;
import com.skyblockexp.ezrtp.config.effects.CountdownParticleSettings;
import com.skyblockexp.ezrtp.integration.EzCountdownDisplayBridge;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.BossBarCompat;
import com.skyblockexp.ezrtp.util.compat.ParticleCompat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CountdownManager {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final MessageProvider messageProvider;
   private final PvpTagService pvpTagService;
   private final Map<UUID, BossBarCompat.Wrapper> countdownBossBars = new HashMap<>();
   private EzCountdownDisplayBridge ezCountdownBridge;

   public CountdownManager(JavaPlugin plugin, PlatformScheduler scheduler, MessageProvider messageProvider) {
      this(plugin, scheduler, messageProvider, null);
   }

   public CountdownManager(JavaPlugin plugin, PlatformScheduler scheduler, MessageProvider messageProvider, PvpTagService pvpTagService) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.messageProvider = messageProvider;
      this.pvpTagService = pvpTagService;
   }

   public void setEzCountdownBridge(EzCountdownDisplayBridge bridge) {
      this.ezCountdownBridge = bridge;
   }

   public void startCountdown(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason, Consumer<Boolean> callback, Runnable onComplete) {
      int countdown = teleportSettings.getCountdownSeconds();
      if (countdown <= 0) {
         onComplete.run();
      } else {
         if (teleportSettings.isCountdownChatMessagesEnabled() && !teleportSettings.isSuppressPlayerMessages()) {
            MessageUtil.send(player, this.messageProvider.format(MessageKey.COUNTDOWN_START, Map.of("seconds", String.valueOf(countdown)), player));
         }

         if (this.ezCountdownBridge != null && teleportSettings.getEzCountdownIntegrationSettings().isEnabled()) {
            boolean started = this.ezCountdownBridge
               .startCountdown(
                  player,
                  countdown,
                  teleportSettings.getEzCountdownIntegrationSettings(),
                  teleportSettings,
                  this.pvpTagService,
                  this.messageProvider,
                  callback,
                  onComplete
               );
            if (started) {
               return;
            }
         }

         Location startLocation = player.getLocation().clone();
         boolean[] warningSent = new boolean[]{false};
         this.runCountdown(player, teleportSettings, callback, countdown, countdown, onComplete, startLocation, warningSent);
      }
   }

   private void runCountdown(
      Player player,
      RandomTeleportSettings teleportSettings,
      Consumer<Boolean> callback,
      int seconds,
      int totalSeconds,
      Runnable onComplete,
      Location startLocation,
      boolean[] warningSent
   ) {
      if (seconds <= 0) {
         this.clearCountdownBossBar(player.getUniqueId());
         onComplete.run();
      } else if (!player.isOnline()) {
         this.clearCountdownBossBar(player.getUniqueId());
         if (callback != null) {
            callback.accept(false);
         }
      } else {
         if (teleportSettings.isCancelOnMove() && teleportSettings.getCountdownSeconds() > 0) {
            double movedDistanceSq = player.getLocation().distanceSquared(startLocation);
            double cancelDistanceSq = teleportSettings.getCancelDistance() * teleportSettings.getCancelDistance();
            double warnDistanceSq = teleportSettings.getWarnDistance() * teleportSettings.getWarnDistance();
            if (movedDistanceSq >= cancelDistanceSq) {
               this.clearCountdownBossBar(player.getUniqueId());
               if (!teleportSettings.isSuppressPlayerMessages()) {
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.COUNTDOWN_MOVE_CANCEL, player));
               }

               if (callback != null) {
                  callback.accept(false);
               }

               return;
            }

            if (!warningSent[0] && warnDistanceSq > 0.0 && movedDistanceSq >= warnDistanceSq) {
               warningSent[0] = true;
               if (!teleportSettings.isSuppressPlayerMessages()) {
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.COUNTDOWN_MOVE_WARN, player));
               }
            }
         }

         if (this.pvpTagService != null && teleportSettings.getPvpTagIntegrationSettings().isCancelCountdownOnPvpTag() && this.pvpTagService.isInCombat(player)
            )
          {
            this.clearCountdownBossBar(player.getUniqueId());
            if (!teleportSettings.isSuppressPlayerMessages()) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.COUNTDOWN_PVP_CANCEL, player));
            }

            if (callback != null) {
               callback.accept(false);
            }
         } else {
            if (teleportSettings.isCountdownChatMessagesEnabled() && !teleportSettings.isSuppressPlayerMessages()) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.COUNTDOWN_TICK, Map.of("seconds", String.valueOf(seconds)), player));
            }

            this.updateCountdownBossBar(player, teleportSettings, seconds, totalSeconds);
            this.playCountdownParticles(player, teleportSettings.getCountdownParticleSettings());
            this.scheduler
               .executeGlobalDelayed(
                  () -> this.runCountdown(player, teleportSettings, callback, seconds - 1, totalSeconds, onComplete, startLocation, warningSent), 20L
               );
         }
      }
   }

   private void updateCountdownBossBar(Player player, RandomTeleportSettings teleportSettings, int seconds, int totalSeconds) {
      CountdownBossBarSettings bossBarSettings = teleportSettings.getCountdownBossBarSettings();
      if (bossBarSettings != null && bossBarSettings.isEnabled()) {
         BossBarCompat.Wrapper bossBar = this.countdownBossBars.get(player.getUniqueId());
         if (bossBar != null && bossBar.isSupported()) {
            bossBar.setColor(bossBarSettings.getColor());
            bossBar.setStyle(bossBarSettings.getStyle());
         } else {
            bossBar = BossBarCompat.create("Teleporting...", bossBarSettings.getColor(), bossBarSettings.getStyle());
            this.countdownBossBars.put(player.getUniqueId(), bossBar);
         }

         bossBar.setTitle(bossBarSettings.titleComponent(seconds));
         double progress = totalSeconds > 0 ? (double)seconds / totalSeconds : 1.0;
         bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
         if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
         }

         bossBar.setVisible(true);
      } else {
         this.clearCountdownBossBar(player.getUniqueId());
      }
   }

   private void clearCountdownBossBar(UUID playerId) {
      BossBarCompat.Wrapper bossBar = this.countdownBossBars.remove(playerId);
      if (bossBar != null) {
         bossBar.removeAll();
      }
   }

   private void playCountdownParticles(Player player, CountdownParticleSettings settings) {
      if (settings != null && settings.isEnabled()) {
         Location baseLocation = player.getLocation().clone().add(0.0, settings.getHeightOffset(), 0.0);
         World world = baseLocation.getWorld();
         if (world != null) {
            int points = Math.max(1, settings.getPoints());
            double radius = settings.getRadius();

            for (int i = 0; i < points; i++) {
               double angle = (Math.PI * 2) * i / points;
               double x = Math.cos(angle) * radius;
               double z = Math.sin(angle) * radius;
               Location particleLocation = baseLocation.clone().add(x, 0.0, z);
               ParticleCompat.spawnParticle(world, settings.getParticle(), particleLocation, 1, 0.0, 0.0, 0.0, settings.getExtra(), null, settings.isForce());
            }

            Particle secondary = settings.getSecondaryParticle();
            if (secondary != null && settings.getSecondaryCount() > 0) {
               ParticleCompat.spawnParticle(
                  world,
                  secondary,
                  baseLocation,
                  settings.getSecondaryCount(),
                  settings.getSecondaryOffset(),
                  settings.getSecondaryOffset(),
                  settings.getSecondaryOffset(),
                  settings.getExtra(),
                  null,
                  settings.isForce()
               );
            }
         }
      }
   }

   public void shutdown() {
      this.countdownBossBars.values().forEach(wrapper -> wrapper.removeAll());
      this.countdownBossBars.clear();
      if (this.ezCountdownBridge != null) {
         this.ezCountdownBridge.shutdown();
      }
   }

   private static String legacy(Component component) {
      return MessageUtil.serializeComponent(component);
   }
}
