package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.BossBarOrderManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

final class BossHealthBarManager {
   private static final int MAX_VISIBLE_BARS_PER_PLAYER = 4;
   private static final Map<UUID, BossHealthBarManager.BossCandidate> candidatesByBoss = new HashMap<>();
   private static final Map<UUID, BossHealthBarManager.PlayerBarPool> poolsByPlayer = new HashMap<>();

   private BossHealthBarManager() {
   }

   static void registerBoss(EliteEntity eliteEntity) {
      if (eliteEntity != null && eliteEntity.isValid()) {
         candidatesByBoss.computeIfAbsent(eliteEntity.getEliteUUID(), ignored -> new BossHealthBarManager.BossCandidate(eliteEntity));
      }
   }

   static void removeBoss(EliteEntity eliteEntity) {
      if (eliteEntity != null) {
         candidatesByBoss.remove(eliteEntity.getEliteUUID());
      }
   }

   static void update() {
      Map<UUID, Player> candidatePlayers = new HashMap<>();
      Map<UUID, List<EliteEntity>> candidatesByPlayer = new HashMap<>();
      Iterator<BossHealthBarManager.BossCandidate> bossIterator = candidatesByBoss.values().iterator();

      while (bossIterator.hasNext()) {
         BossHealthBarManager.BossCandidate candidate = bossIterator.next();
         EliteEntity eliteEntity = candidate.eliteEntity;
         Location bossLocation = eliteEntity == null ? null : eliteEntity.getLocation();
         if (eliteEntity != null && eliteEntity.isValid() && bossLocation != null && bossLocation.getWorld() != null) {
            Player aggroTarget = currentAggroTarget(eliteEntity);
            if (aggroTarget != null) {
               UUID playerUUID = aggroTarget.getUniqueId();
               candidatePlayers.put(playerUUID, aggroTarget);
               candidatesByPlayer.computeIfAbsent(playerUUID, ignored -> new ArrayList<>()).add(eliteEntity);
            }
         } else {
            bossIterator.remove();
         }
      }

      Set<UUID> updatedPlayers = new HashSet<>();

      for (Entry<UUID, List<EliteEntity>> entry : candidatesByPlayer.entrySet()) {
         Player player = candidatePlayers.get(entry.getKey());
         List<EliteEntity> candidates = entry.getValue();
         candidates.sort(Comparator.<EliteEntity>comparingDouble(eliteEntity -> distanceSquared(player, eliteEntity)).thenComparing(EliteEntity::getEliteUUID));
         int visibleCount = Math.min(4, candidates.size());
         poolsByPlayer.computeIfAbsent(entry.getKey(), BossHealthBarManager.PlayerBarPool::new).update(player, candidates.subList(0, visibleCount));
         updatedPlayers.add(entry.getKey());
      }

      Iterator<Entry<UUID, BossHealthBarManager.PlayerBarPool>> poolIterator = poolsByPlayer.entrySet().iterator();

      while (poolIterator.hasNext()) {
         Entry<UUID, BossHealthBarManager.PlayerBarPool> entry = poolIterator.next();
         if (!updatedPlayers.contains(entry.getKey())) {
            entry.getValue().cleanup(Bukkit.getPlayer(entry.getKey()));
            poolIterator.remove();
         }
      }
   }

   static void shutdown() {
      poolsByPlayer.forEach((playerUUID, pool) -> {
         try {
            pool.cleanup(Bukkit.getPlayer(playerUUID));
         } catch (RuntimeException var3) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to clean health boss bars for " + playerUUID, (Throwable)var3);
         }
      });
      poolsByPlayer.clear();
      candidatesByBoss.clear();
   }

   static Player currentAggroTarget(EliteEntity eliteEntity) {
      if (eliteEntity != null && eliteEntity.isValid() && eliteEntity.isInCombat()) {
         if (!(eliteEntity.getLivingEntity() instanceof Mob mob && mob.getTarget() instanceof Player player)) {
            return null;
         } else if (!player.isOnline() || player.isDead() || !player.isValid()) {
            return null;
         } else {
            return !player.getWorld().equals(mob.getWorld()) ? null : player;
         }
      } else {
         return null;
      }
   }

   private static double distanceSquared(Player player, EliteEntity eliteEntity) {
      Location bossLocation = eliteEntity.getLocation();
      return bossLocation != null && bossLocation.getWorld() != null && player.getWorld().equals(bossLocation.getWorld())
         ? player.getLocation().distanceSquared(bossLocation)
         : Double.POSITIVE_INFINITY;
   }

   private static BarColor colorFor(double healthRatio) {
      if (healthRatio > 0.75) {
         return BarColor.GREEN;
      } else {
         return healthRatio > 0.5 ? BarColor.YELLOW : BarColor.RED;
      }
   }

   private static final class BossCandidate {
      private final EliteEntity eliteEntity;

      private BossCandidate(EliteEntity eliteEntity) {
         this.eliteEntity = eliteEntity;
      }
   }

   private static final class PlayerBarPool {
      private final UUID playerUUID;
      private final List<BossBar> slots = new ArrayList<>(4);
      private int visibleSlots;

      private PlayerBarPool(UUID playerUUID) {
         this.playerUUID = playerUUID;
      }

      private void update(Player player, List<EliteEntity> selectedBosses) {
         if (player != null && player.isOnline() && player.isValid()) {
            for (int index = 0; index < selectedBosses.size(); index++) {
               BossBar bossBar = this.getOrCreateSlot(index);
               EliteEntity eliteEntity = selectedBosses.get(index);
               String name = eliteEntity.getName();
               double maxHealth = eliteEntity.getMaxHealth();
               double progress = maxHealth <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, eliteEntity.getHealth() / maxHealth));
               bossBar.setTitle(name == null ? MobCombatSettingsConfig.getFallbackBossBarName() : name);
               bossBar.setProgress(progress);
               bossBar.setColor(BossHealthBarManager.colorFor(progress));
               if (index >= this.visibleSlots) {
                  BossBarOrderManager.show(player, bossBar, BossBarOrderManager.sortKeyFor(this.playerUUID) + index);
               }
            }

            for (int indexx = selectedBosses.size(); indexx < this.visibleSlots; indexx++) {
               BossBarOrderManager.hide(player, this.slots.get(indexx));
            }

            this.visibleSlots = selectedBosses.size();
         }
      }

      private BossBar getOrCreateSlot(int index) {
         if (index < this.slots.size()) {
            return this.slots.get(index);
         } else {
            BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_10, new BarFlag[0]);
            this.slots.add(bossBar);
            return bossBar;
         }
      }

      private void cleanup(Player player) {
         for (BossBar bossBar : this.slots) {
            BossBarOrderManager.hide(player, bossBar);
            bossBar.removeAll();
         }

         this.slots.clear();
         this.visibleSlots = 0;
      }
   }
}
