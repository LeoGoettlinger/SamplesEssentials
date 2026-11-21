package dev.demora.demoralegendaryweapons.weapons.reinforcedelytra;

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

public class ReinforcedElytraBossBarManager {
   private final DemoraLegendaryWeapons plugin;
   private final ReinforcedElytra reinforcedElytra;
   private final Map<UUID, BossBar> explosionBossBars = new HashMap<>();
   private final ReinforcedElytraTrackingManager trackingManager;

   public ReinforcedElytraBossBarManager(DemoraLegendaryWeapons plugin, ReinforcedElytra reinforcedElytra, ReinforcedElytraTrackingManager trackingManager) {
      this.plugin = plugin;
      this.reinforcedElytra = reinforcedElytra;
      this.trackingManager = trackingManager;
      this.startBossBarUpdateTask();
   }

   private void startBossBarUpdateTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               ItemStack chest = player.getInventory().getChestplate();
               if (!ReinforcedElytraBossBarManager.this.reinforcedElytra.isReinforcedElytra(chest)) {
                  ReinforcedElytraBossBarManager.this.hideBossBar(player);
               } else if (!player.isGliding()) {
                  ReinforcedElytraBossBarManager.this.hideBossBar(player);
               } else {
                  ReinforcedElytraBossBarManager.this.trackingManager.updateMaxGlidingSpeed(player);
                  double maxSpeed = ReinforcedElytraBossBarManager.this.trackingManager.getMaxGlidingSpeed(player.getUniqueId());
                  ReinforcedElytraBossBarManager.this.updateBossBar(player, maxSpeed);
               }
            }
         }
      }).runTaskTimer(this.plugin, 0L, 5L);
   }

   private void updateBossBar(Player player, double maxSpeed) {
      double minSpeed = this.reinforcedElytra.getMinTriggerSpeed();
      double maxSpeedConfig = this.reinforcedElytra.getMaxSpeedForExplosion();
      float minExplosion = this.reinforcedElytra.getMinExplosionPower();
      float maxExplosion = this.reinforcedElytra.getMaxExplosionPower();
      if (maxSpeed < minSpeed) {
         this.hideBossBar(player);
      } else {
         double t = Math.min(1.0, (maxSpeed - minSpeed) / (maxSpeedConfig - minSpeed));
         float explosionPower = (float)(minExplosion + t * (maxExplosion - minExplosion));
         float progress = (float)t;
         String bossBarText = this.plugin
            .getLanguageManager()
            .getMessage("weapon-messages.reinforced-elytra-boss-bar")
            .replace("{power}", String.format("%.1f", explosionPower));
         UUID uid = player.getUniqueId();
         BossBar bossBar = this.explosionBossBars.get(uid);
         if (bossBar == null) {
            Component title = Component.text(bossBarText).color(NamedTextColor.AQUA);
            bossBar = BossBar.bossBar(title, progress, Color.BLUE, Overlay.PROGRESS);
            player.showBossBar(bossBar);
            this.explosionBossBars.put(uid, bossBar);
         } else {
            Component title = Component.text(bossBarText).color(NamedTextColor.AQUA);
            bossBar.name(title);
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
