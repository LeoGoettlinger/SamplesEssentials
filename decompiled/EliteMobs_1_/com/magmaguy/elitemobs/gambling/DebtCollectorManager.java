package com.magmaguy.elitemobs.gambling;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.GamblingConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DebtCollectorManager implements Listener {
   private static final String DEBT_COLLECTOR_FILENAME = "debt_collector.yml";
   private static final Map<UUID, Long> lastSpawnTime = new HashMap<>();
   private static final Map<UUID, CustomBossEntity> activeDebtCollectors = new HashMap<>();
   private static final Map<UUID, UUID> bossToPlayerTarget = new HashMap<>();
   private static BukkitTask checkTask;

   public static void initialize() {
      if (checkTask != null) {
         checkTask.cancel();
         checkTask = null;
      }

      if (GamblingConfig.isGamblingEnabled()) {
         int checkIntervalMinutes = GamblingConfig.getDebtCollectorCheckIntervalMinutes();
         long checkIntervalTicks = 1200L * checkIntervalMinutes;
         checkTask = (new BukkitRunnable() {
            public void run() {
               for (Player player : Bukkit.getOnlinePlayers()) {
                  DebtCollectorManager.checkAndSpawnDebtCollector(player);
               }
            }
         }).runTaskTimer(MetadataHandler.PLUGIN, checkIntervalTicks, checkIntervalTicks);
      }
   }

   private static void checkAndSpawnDebtCollector(Player player) {
      if (GamblingConfig.isGamblingEnabled() && player != null && player.isOnline()) {
         UUID uuid = player.getUniqueId();
         if (PlayerData.hasGamblingDebt(uuid)) {
            if (activeDebtCollectors.containsKey(uuid)) {
               CustomBossEntity existingBoss = activeDebtCollectors.get(uuid);
               if (existingBoss != null && existingBoss.exists()) {
                  return;
               }

               activeDebtCollectors.remove(uuid);
            }

            long now = System.currentTimeMillis();
            Long lastSpawn = lastSpawnTime.get(uuid);
            if (lastSpawn != null) {
               long cooldownMs = GamblingConfig.getDebtCollectorCheckIntervalMinutes() * 60L * 1000L;
               if (now - lastSpawn < cooldownMs) {
                  return;
               }
            }

            if (!(ThreadLocalRandom.current().nextDouble() > GamblingConfig.getDebtCollectorSpawnChance())) {
               spawnDebtCollector(player);
               lastSpawnTime.put(uuid, now);
            }
         }
      }
   }

   public static void spawnDebtCollector(final Player player) {
      if (GamblingConfig.isGamblingEnabled() && player != null && player.isOnline()) {
         Location spawnLoc = findSpawnLocation(player);
         if (spawnLoc == null) {
            spawnLoc = player.getLocation().add(ThreadLocalRandom.current().nextDouble(-5.0, 5.0), 0.0, ThreadLocalRandom.current().nextDouble(-5.0, 5.0));
         }

         final CustomBossEntity boss = CustomBossEntity.createCustomBossEntity("debt_collector.yml");
         if (boss != null) {
            boss.spawn(spawnLoc, false);
            if (boss.exists()) {
               final UUID bossUUID = boss.getEliteUUID();
               activeDebtCollectors.put(player.getUniqueId(), boss);
               bossToPlayerTarget.put(bossUUID, player.getUniqueId());
               if (boss.getLivingEntity() != null && boss.getLivingEntity() instanceof Mob mob) {
                  mob.setTarget(player);
               }

               double debt = PlayerData.getGamblingDebt(player.getUniqueId());
               player.sendMessage(
                  ChatColorConverter.convert(
                     GamblingConfig.getDebtCollectorSpawnMessage().replace("$player", player.getName()).replace("$amount", String.format("%.0f", debt))
                  )
               );
               int timeoutSeconds = GamblingConfig.getDebtCollectorTimeoutSeconds();
               (new BukkitRunnable() {
                  public void run() {
                     if (boss.exists()) {
                        player.sendMessage(ChatColorConverter.convert(GamblingConfig.getDebtCollectorTimeoutMessage().replace("$player", player.getName())));
                        boss.remove(RemovalReason.BOSS_TIMEOUT);
                        DebtCollectorManager.activeDebtCollectors.remove(player.getUniqueId());
                        DebtCollectorManager.bossToPlayerTarget.remove(bossUUID);
                     }
                  }
               }).runTaskLater(MetadataHandler.PLUGIN, timeoutSeconds * 20L);
            }
         }
      }
   }

   private static Location findSpawnLocation(Player player) {
      Location playerLoc = player.getLocation();

      for (int attempt = 0; attempt < 10; attempt++) {
         double angle = ThreadLocalRandom.current().nextDouble() * 2.0 * Math.PI;
         double distance = ThreadLocalRandom.current().nextDouble(5.0, 10.0);
         double x = playerLoc.getX() + Math.cos(angle) * distance;
         double z = playerLoc.getZ() + Math.sin(angle) * distance;
         Location testLoc = new Location(playerLoc.getWorld(), x, playerLoc.getY(), z);
         testLoc = testLoc.getWorld().getHighestBlockAt(testLoc).getLocation().add(0.0, 1.0, 0.0);
         if (testLoc.getBlock().isPassable() && testLoc.clone().add(0.0, 1.0, 0.0).getBlock().isPassable()) {
            return testLoc;
         }
      }

      return null;
   }

   public static CustomBossEntity getDebtCollectorForPlayer(UUID playerUUID) {
      return activeDebtCollectors.get(playerUUID);
   }

   public static boolean hasActiveDebtCollector(UUID playerUUID) {
      CustomBossEntity boss = activeDebtCollectors.get(playerUUID);
      return boss != null && boss.exists();
   }

   public static void shutdown() {
      if (checkTask != null) {
         checkTask.cancel();
         checkTask = null;
      }

      for (CustomBossEntity boss : activeDebtCollectors.values()) {
         if (boss != null && boss.exists()) {
            boss.remove(RemovalReason.SHUTDOWN);
         }
      }

      activeDebtCollectors.clear();
      bossToPlayerTarget.clear();
      lastSpawnTime.clear();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onDebtCollectorDeath(EliteMobDeathEvent event) {
      if (event.getEliteEntity() instanceof CustomBossEntity boss) {
         if (boss.getCustomBossesConfigFields() != null) {
            if ("debt_collector.yml".equals(boss.getCustomBossesConfigFields().getFilename())) {
               UUID bossUUID = boss.getEliteUUID();
               UUID targetPlayerUUID = bossToPlayerTarget.get(bossUUID);
               if (targetPlayerUUID != null) {
                  Player targetPlayer = Bukkit.getPlayer(targetPlayerUUID);
                  if (targetPlayer != null && targetPlayer.isOnline()) {
                     targetPlayer.sendMessage(
                        ChatColorConverter.convert(GamblingConfig.getDebtCollectorDeathMessage().replace("$player", targetPlayer.getName()))
                     );
                  }
               }

               activeDebtCollectors.values().removeIf(b -> b.equals(boss));
               bossToPlayerTarget.remove(bossUUID);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      EntityDamageByEntityEvent lastDamage = player.getLastDamageCause() instanceof EntityDamageByEntityEvent
         ? (EntityDamageByEntityEvent)player.getLastDamageCause()
         : null;
      if (lastDamage != null) {
         if (EntityTracker.getEliteMobEntity(lastDamage.getDamager()) instanceof CustomBossEntity killerBoss) {
            if (killerBoss.getCustomBossesConfigFields() != null) {
               if ("debt_collector.yml".equals(killerBoss.getCustomBossesConfigFields().getFilename())) {
                  UUID uuid = player.getUniqueId();
                  double debtReduction = GamblingConfig.getDebtReductionOnPlayerDeath();
                  PlayerData.reduceGamblingDebt(uuid, debtReduction);
                  double remainingDebt = PlayerData.getGamblingDebt(uuid);
                  player.sendMessage(ChatColorConverter.convert(GamblingConfig.getDebtCollectorKillMessage().replace("$player", player.getName())));
                  if (remainingDebt > 0.0) {
                     player.sendMessage(
                        ChatColorConverter.convert(
                           GamblingConfig.getDebtPaidMessage()
                              .replace("$amount", String.format("%.0f", debtReduction))
                              .replace("$remaining", String.format("%.0f", remainingDebt))
                        )
                     );
                  } else {
                     player.sendMessage(ChatColorConverter.convert(GamblingConfig.getDebtClearedMessage()));
                  }

                  UUID bossUUID = killerBoss.getEliteUUID();
                  activeDebtCollectors.remove(uuid);
                  bossToPlayerTarget.remove(bossUUID);
                  (new BukkitRunnable() {
                     public void run() {
                        if (killerBoss.exists()) {
                           killerBoss.remove(RemovalReason.KILL_COMMAND);
                        }
                     }
                  }).runTaskLater(MetadataHandler.PLUGIN, 100L);
               }
            }
         }
      }
   }
}
