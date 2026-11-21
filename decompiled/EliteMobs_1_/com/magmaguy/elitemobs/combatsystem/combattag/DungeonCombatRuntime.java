package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.AdvancedCombatSystemConfig;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.scheduler.BukkitTask;

public final class DungeonCombatRuntime implements org.bukkit.event.Listener, PlayerCombatState {
   private static DungeonCombatRuntime instance;
   private final CombatSessionTracker combatSessions;
   private final List<PlayerCombatState.Listener> listeners = new CopyOnWriteArrayList<>();
   private final long foodRegenerationIntervalTicks;
   private long currentTick;
   private long nextFoodRegenerationTick;
   private BukkitTask updateTask;

   public DungeonCombatRuntime() {
      this(
         AdvancedCombatSystemConfig.isEnabled() ? 400L : DungeonsConfig.getDungeonFoodRegenerationCombatTimeoutSeconds() * 20L,
         DungeonsConfig.getDungeonFoodRegenerationIntervalSeconds() * 20L
      );
   }

   public DungeonCombatRuntime(long combatTimeoutTicks, long foodRegenerationIntervalTicks) {
      if (instance != null) {
         throw new IllegalStateException("Dungeon combat runtime is already initialized.");
      } else {
         this.combatSessions = new CombatSessionTracker(combatTimeoutTicks);
         this.foodRegenerationIntervalTicks = foodRegenerationIntervalTicks;
         this.nextFoodRegenerationTick = foodRegenerationIntervalTicks;
         instance = this;
      }
   }

   public static DungeonCombatRuntime getInstance() {
      if (instance == null) {
         throw new IllegalStateException("Dungeon combat runtime is not initialized.");
      } else {
         return instance;
      }
   }

   public static void shutdownIfInitialized() {
      if (instance != null) {
         instance.shutdown();
      }
   }

   public static boolean isEligiblePlayer(Player player) {
      return DungeonFoodRegeneration.isEligibleDungeonPlayer(player);
   }

   public static boolean isInManagedCombatWorld(Player player) {
      return DungeonFoodRegeneration.isInEligibleCombatContent(player);
   }

   public void start() {
      if (this.updateTask != null) {
         throw new IllegalStateException("Dungeon combat runtime is already running.");
      } else {
         this.updateTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::tick, 1L, 1L);
      }
   }

   public void shutdown() {
      if (this.updateTask != null) {
         this.updateTask.cancel();
         this.updateTask = null;
      }

      HandlerList.unregisterAll(this);
      this.listeners.clear();
      if (instance == this) {
         instance = null;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onEnemyCombatDamage(EntityDamageByEntityEvent event) {
      double absorptionModifier = getAbsorptionDamageModifier(event);
      if (DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(event.getFinalDamage(), absorptionModifier)) {
         Player player = CombatParticipantResolver.resolveEnemyCombatPlayer(event);
         if (player != null && DungeonFoodRegeneration.isEligibleDungeonPlayer(player)) {
            if (this.combatSessions.recordDamage(player.getUniqueId(), this.currentTick) == CombatSessionTracker.DamageResult.STARTED_COMBAT) {
               for (PlayerCombatState.Listener listener : this.listeners) {
                  listener.onCombatStarted(player.getUniqueId());
               }
            }
         }
      }
   }

   private void tick() {
      this.currentTick++;

      for (UUID playerId : this.combatSessions.expire(this.currentTick)) {
         for (PlayerCombatState.Listener listener : this.listeners) {
            listener.onCombatEnded(playerId);
         }
      }

      if (DungeonsConfig.isEnableDungeonFoodRegeneration()) {
         if (!AdvancedCombatSystemConfig.isEnabled()) {
            if (this.currentTick >= this.nextFoodRegenerationTick) {
               this.nextFoodRegenerationTick = this.currentTick + this.foodRegenerationIntervalTicks;

               for (Player player : Bukkit.getOnlinePlayers()) {
                  if (DungeonFoodRegeneration.shouldRegenerateFood(player, this.combatSessions.isInCombat(player.getUniqueId()))) {
                     player.addPotionEffect(DungeonFoodRegeneration.createFoodRegenerationEffect());
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean isInCombat(UUID playerId) {
      return this.combatSessions.isInCombat(playerId);
   }

   @Override
   public void addListener(PlayerCombatState.Listener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("Combat listener cannot be null.");
      } else {
         this.listeners.add(listener);
      }
   }

   @Override
   public void removeListener(PlayerCombatState.Listener listener) {
      this.listeners.remove(listener);
   }

   private static double getAbsorptionDamageModifier(EntityDamageByEntityEvent event) {
      return event.isApplicable(DamageModifier.ABSORPTION) ? event.getDamage(DamageModifier.ABSORPTION) : 0.0;
   }
}
