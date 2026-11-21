package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class BossHealthDisplay implements Listener {
   private static final Map<UUID, EliteOverheadHealthDisplay> activeDisplays = new ConcurrentHashMap<>();
   private static BukkitTask masterUpdateTask;

   public static void startMasterUpdateTask() {
      if (masterUpdateTask == null) {
         masterUpdateTask = (new BukkitRunnable() {
               public void run() {
                  Iterator<EliteOverheadHealthDisplay> iterator = BossHealthDisplay.activeDisplays.values().iterator();

                  while (iterator.hasNext()) {
                     EliteOverheadHealthDisplay display = iterator.next();

                     try {
                        if (!display.isValid()) {
                           BossHealthDisplay.removeInvalidDisplay(iterator, display);
                        } else if (display.hasTimedOut()) {
                           BossHealthDisplay.removeOverheadDisplay(iterator, display);
                        } else {
                           display.refreshIfDirty();
                           display.updatePositions();
                        }
                     } catch (RuntimeException var4) {
                        MetadataHandler.PLUGIN
                           .getLogger()
                           .log(Level.WARNING, "Discarding a failed overhead health display for " + display.eliteEntity().getEliteUUID(), (Throwable)var4);
                        BossHealthDisplay.removeOverheadDisplay(iterator, display);
                     }
                  }

                  BossHealthDisplay.updateSafely("health boss bars", BossHealthBarManager::update);
                  BossHealthDisplay.updateSafely("combat popups", CombatPopupManager::update);
                  BossHealthDisplay.updateSafely("hit effects", HitEffectManager::update);
                  BossHealthDisplay.updateSafely("XP popups", XpPopupManager::update);
                  LeashReturnDamageIndicator.expire();
               }
            })
            .runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
      }
   }

   public static void shutdown() {
      if (masterUpdateTask != null) {
         masterUpdateTask.cancel();
         masterUpdateTask = null;
      }

      activeDisplays.values().forEach(BossHealthDisplay::cleanupDisplay);
      activeDisplays.clear();
      updateSafely("health boss-bar shutdown", BossHealthBarManager::shutdown);
      updateSafely("combat-popup shutdown", CombatPopupManager::shutdown);
      updateSafely("hit-effect shutdown", HitEffectManager::shutdown);
      updateSafely("XP-popup shutdown", XpPopupManager::shutdown);
   }

   public static void removeDisplay(EliteEntity eliteEntity) {
      if (eliteEntity != null) {
         EliteOverheadHealthDisplay display = activeDisplays.remove(eliteEntity.getEliteUUID());
         if (display != null) {
            cleanupDisplay(display);
         }

         BossHealthBarManager.removeBoss(eliteEntity);
      }
   }

   public static void createXPPopup(Location location, Player player, long xpAmount) {
      XpPopupManager.create(location, player, xpAmount);
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onDamage(EliteMobDamagedByPlayerEvent event) {
      EliteEntity eliteEntity = event.getEliteMobEntity();
      if (eliteEntity.isValid()) {
         Vector offset = new Vector(ThreadLocalRandom.current().nextDouble(-1.5, 1.5), 0.0, ThreadLocalRandom.current().nextDouble(-1.5, 1.5));
         CombatPopupManager.createDamagePopup(
            eliteEntity, event.getDamage(), event.isCriticalStrike(), event.getDamageModifier(), event.getClassAbilityBonusDamage(), offset, event.getPlayer()
         );
         if (anyHealthDisplayEnabled()) {
            EliteOverheadHealthDisplay display = getOrCreateDisplay(eliteEntity);
            display.resetCombatTimer();
            display.markDirty();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onHeal(EliteMobHealEvent event) {
      EliteEntity eliteEntity = event.getEliteEntity();
      if (eliteEntity.isValid()) {
         CombatPopupManager.createHealPopup(eliteEntity, event.getHealAmount(), event.isFullHeal());
         if (anyHealthDisplayEnabled()) {
            EliteOverheadHealthDisplay display = activeDisplays.get(eliteEntity.getEliteUUID());
            if (display != null) {
               display.markDirty();
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEnterCombat(EliteMobEnterCombatEvent event) {
      EliteEntity eliteEntity = event.getEliteMobEntity();
      if (eliteEntity.isValid() && anyHealthDisplayEnabled()) {
         getOrCreateDisplay(eliteEntity).resetCombatTimer();
         if (MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier()
            && eliteEntity instanceof CustomBossEntity customBoss
            && !(customBoss.getHealthMultiplier() <= MobCombatSettingsConfig.getBossBarHealthMultiplierThreshold())) {
            BossHealthBarManager.registerBoss(eliteEntity);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onExitCombat(EliteMobExitCombatEvent event) {
      removeDisplay(event.getEliteMobEntity());
   }

   private static EliteOverheadHealthDisplay getOrCreateDisplay(EliteEntity eliteEntity) {
      return activeDisplays.computeIfAbsent(eliteEntity.getEliteUUID(), ignored -> new EliteOverheadHealthDisplay(eliteEntity));
   }

   private static void removeInvalidDisplay(Iterator<EliteOverheadHealthDisplay> iterator, EliteOverheadHealthDisplay display) {
      cleanupDisplay(display);
      BossHealthBarManager.removeBoss(display.eliteEntity());
      iterator.remove();
   }

   private static void removeOverheadDisplay(Iterator<EliteOverheadHealthDisplay> iterator, EliteOverheadHealthDisplay display) {
      cleanupDisplay(display);
      iterator.remove();
   }

   private static void cleanupDisplay(EliteOverheadHealthDisplay display) {
      try {
         display.cleanup();
      } catch (RuntimeException var2) {
         MetadataHandler.PLUGIN
            .getLogger()
            .log(Level.WARNING, "Failed to clean an overhead health display for " + display.eliteEntity().getEliteUUID(), (Throwable)var2);
      }
   }

   private static void updateSafely(String subsystem, Runnable update) {
      try {
         update.run();
      } catch (RuntimeException var3) {
         MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to update " + subsystem, (Throwable)var3);
      }
   }

   private static boolean anyHealthDisplayEnabled() {
      return MobCombatSettingsConfig.isDisplayVisualHealthBars()
         || MobCombatSettingsConfig.isDisplayNumericHealth()
         || MobCombatSettingsConfig.isDisplayBossBarForHighMultiplier();
   }
}
