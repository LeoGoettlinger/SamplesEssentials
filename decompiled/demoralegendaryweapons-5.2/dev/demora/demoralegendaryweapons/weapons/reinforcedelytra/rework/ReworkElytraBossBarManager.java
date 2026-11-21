package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra.rework;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ReworkElytraBossBarManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReworkElytra reworkElytra;
   private final Map<UUID, BossBar> explosionBossBars = new HashMap<>();
   private final ReworkElytraTrackingManager trackingManager;

   public ReworkElytraBossBarManager(DemoraLegendaryWeapons plugin, ReworkElytra reworkElytra, ReworkElytraTrackingManager trackingManager) {
      this.plugin = plugin;
      this.reworkElytra = reworkElytra;
      this.trackingManager = trackingManager;
      this.startBossBarUpdateTask();
   }

   private void startBossBarUpdateTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (!ReworkElytraBossBarManager.this.reworkElytra.isReworkElytra(chest)) {
                  ReworkElytraBossBarManager.this.hideBossBar(player);
               } else if (!player.isGliding()) {
                  ReworkElytraBossBarManager.this.hideBossBar(player);
               } else {
                  ReworkElytraBossBarManager.this.trackingManager.updateMaxGlidingSpeed(player);
                  double maxSpeed = ReworkElytraBossBarManager.this.trackingManager.getMaxGlidingSpeed(player.getUniqueId());
                  ReworkElytraBossBarManager.this.updateBossBar(player, maxSpeed);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 5L);
   }

   private void updateBossBar(Player player, double maxSpeed) {
      double minSpeed = this.reworkElytra.getMinTriggerSpeed();
      double maxSpeedConfig = this.reworkElytra.getMaxSpeedForExplosion();
      float minExplosion = this.reworkElytra.getMinExplosionPower();
      float maxExplosion = this.reworkElytra.getMaxExplosionPower();
      if (maxSpeed < minSpeed) {
         this.hideBossBar(player);
      } else {
         double t = Math.min(1.0, (maxSpeed - minSpeed) / (maxSpeedConfig - minSpeed));
         float explosionPower = (float)(minExplosion + t * (maxExplosion - minExplosion));
         float progress = (float)t;
         String bossBarText = this.plugin
            .getLanguageManager()
            .getMessage("weapon-messages.rework-elytra-boss-bar")
            .replace("{power}", String.format("%.1f", explosionPower));
         UUID uid = player.getUniqueId();
         BossBar bossBar = this.explosionBossBars.get(uid);
         if (bossBar == null) {
            Component title = Component.text(bossBarText).color(NamedTextColor.DARK_RED);
            bossBar = BossBar.bossBar(title, progress, Color.RED, Overlay.PROGRESS);
            player.showBossBar(bossBar);
            this.explosionBossBars.put(uid, bossBar);
         } else {
            bossBar.name(Component.text(bossBarText).color(NamedTextColor.DARK_RED));
            bossBar.progress(progress);
         }
      }
   }

   public void hideBossBar(Player player) {
      UUID uid = player.getUniqueId();
      BossBar bossBar = this.explosionBossBars.remove(uid);
      if (bossBar != null) {
         player.hideBossBar(bossBar);
      }
   }

   public void cleanupPlayer(UUID playerId) {
      BossBar bossBar = this.explosionBossBars.remove(playerId);
      if (bossBar != null) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null && player.isOnline()) {
            player.hideBossBar(bossBar);
         }
      }
   }
}
