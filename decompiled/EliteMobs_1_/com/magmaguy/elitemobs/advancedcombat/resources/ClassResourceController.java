package com.magmaguy.elitemobs.advancedcombat.resources;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class ClassResourceController {
   private final Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions;
   private final Map<UUID, ClassResourceController.ResourceState> states = new HashMap<>();
   private long currentTick;

   public ClassResourceController(Map<ClassResourceType, ClassResourceDefinition> resourceDefinitions) {
      Objects.requireNonNull(resourceDefinitions, "resourceDefinitions");
      if (!resourceDefinitions.keySet().equals(EnumSet.allOf(ClassResourceType.class))) {
         throw new IllegalArgumentException("Every class resource type requires one resource definition");
      } else {
         for (Entry<ClassResourceType, ClassResourceDefinition> entry : resourceDefinitions.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "resourceDefinitions contains a null type");
            ClassResourceDefinition definition = Objects.requireNonNull(entry.getValue(), "resourceDefinitions contains a null definition");
            if (definition.type() != entry.getKey()) {
               throw new IllegalArgumentException("Resource key does not match definition " + definition.type());
            }
         }

         this.resourceDefinitions = Map.copyOf(resourceDefinitions);
      }
   }

   public void open(Player player, ClassResourceType type) {
      this.open(player, type, null);
   }

   public void open(Player player, ClassResourceType type, UUID runToken) {
      ClassResourceController.ResourceState existing = this.states.get(player.getUniqueId());
      if (existing != null && existing.type == type && Objects.equals(existing.runToken, runToken)) {
         existing.suspended = false;
      } else {
         this.close(player);
         double initial = this.rules(type).initialAmount();
         ClassResourceController.ResourceState state = new ClassResourceController.ResourceState(type, runToken, initial, 0L);
         state.lastDamageTick = this.currentTick;
         this.states.put(player.getUniqueId(), state);
      }
   }

   public void close(Player player) {
      this.states.remove(player.getUniqueId());
   }

   public void discard(Player player) {
      this.states.remove(player.getUniqueId());
   }

   public void suspend(Player player) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null) {
         state.suspended = true;
      }
   }

   public void tick(Iterable<? extends Player> players, Predicate<UUID> inCombat) {
      this.currentTick += 20L;

      for (Player player : players) {
         ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
         if (state != null && !state.suspended) {
            boolean playerInCombat = inCombat.test(player.getUniqueId());
            ClassResourceDefinition rules = this.rules(state.type);
            double delta = playerInCombat ? rules.inCombatTickDelta() : rules.outOfCombatTickDelta();
            if (this.currentTick < state.recoveryBlockedUntil && delta > 0.0) {
               delta = 0.0;
            }

            if (delta > 0.0) {
               delta *= rules.damageFreeRecoveryBonus().multiplierAfter(this.currentTick - state.lastDamageTick);
            }

            if (delta > 0.0 && state.amount < rules.maximum()) {
               delta *= nearbyRecoveryMultiplier(player, rules.nearbyRecoveryBonus());
            }

            this.set(state, state.amount + delta);
         }
      }
   }

   private static double nearbyRecoveryMultiplier(Player player, ClassResourceDefinition.NearbyRecoveryBonus recovery) {
      if (recovery.maximumEntities() == 0) {
         return 1.0;
      } else {
         Location center = player.getLocation();
         double radius = recovery.radius();
         double radiusSquared = radius * radius;
         int count = 0;

         for (Entity candidate : player.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (candidate != player
               && candidate instanceof LivingEntity living
               && !living.isDead()
               && living.isValid()
               && !(living.getLocation().distanceSquared(center) > radiusSquared)) {
               boolean var10000 = switch (recovery.target()) {
                  case ELITES -> EntityTracker.getEliteMobEntity(living) != null;
                  case OTHER_PLAYERS -> {
                     if (living instanceof Player nearbyPlayer
                        && nearbyPlayer.isOnline()
                        && nearbyPlayer.getGameMode() != GameMode.SPECTATOR
                        && !nearbyPlayer.hasMetadata("NPC")) {
                        yield true;
                     }

                     yield false;
                  }
               };

               boolean matches = var10000;
               if (matches && ++count >= recovery.maximumEntities()) {
                  break;
               }
            }
         }

         return recovery.multiplier(count);
      }
   }

   public boolean trySpend(Player player, double amount) {
      requireSpendAmount(amount);
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state == null) {
         return false;
      } else if (state.amount + 1.0E-9 < amount) {
         return false;
      } else {
         this.set(state, state.amount - amount);
         return true;
      }
   }

   public boolean canAfford(Player player, double amount) {
      requireSpendAmount(amount);
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      return state != null && state.amount + 1.0E-9 >= amount;
   }

   public void refund(Player player, double amount) {
      requireSpendAmount(amount);
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null) {
         this.set(state, state.amount + amount);
      }
   }

   public void setPracticeMode(Player player, boolean enabled) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null) {
         state.practiceMode = enabled;
         if (enabled) {
            this.set(state, this.rules(state.type).maximum());
         }
      }
   }

   public void grant(Player player, double amount) {
      if (Double.isFinite(amount) && !(amount <= 0.0)) {
         ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
         if (state != null) {
            this.set(state, state.amount + amount);
         }
      }
   }

   public void onDamageDealt(Player player, double actualDamage) {
      if (Double.isFinite(actualDamage) && !(actualDamage <= 0.0)) {
         ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
         if (state != null && !state.suspended) {
            double gain = this.rules(state.type).damageDealtFlatGain();
            if (gain > 0.0) {
               this.set(state, state.amount + gain);
            }
         }
      }
   }

   public void onDamageReceived(Player player, double actualDamage) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null) {
         ClassResourceDefinition rules = this.rules(state.type);
         if (rules.damageReceivedHealthEquivalentGain() > 0.0) {
            this.gainScaledToHealth(player, state, actualDamage, rules.damageReceivedHealthEquivalentGain());
         }

         if (rules.damageReceivedFlatChange() != 0.0) {
            this.set(state, state.amount + rules.damageReceivedFlatChange());
         }

         if (rules.recoveryDelayAfterDamageTicks() > 0L) {
            state.recoveryBlockedUntil = this.currentTick + rules.recoveryDelayAfterDamageTicks();
         }
      }
   }

   public void observeDamage(Player player) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null && !state.suspended) {
         state.lastDamageTick = this.currentTick;
      }
   }

   public void onEffectiveHealing(Player player, double healthRestored) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null && !(healthRestored <= 0.0)) {
         double gain = this.rules(state.type).healingHealthEquivalentGain();
         if (gain > 0.0) {
            this.gainScaledToHealth(player, state, healthRestored, gain);
         }
      }
   }

   public void onDamagePrevented(Player player, double damagePrevented) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null && !(damagePrevented <= 0.0)) {
         double gain = this.rules(state.type).preventedDamageHealthEquivalentGain();
         if (gain > 0.0) {
            this.gainScaledToHealth(player, state, damagePrevented, gain);
         }
      }
   }

   public void onTaunt(Player player, int affectedEnemies) {
      ClassResourceController.ResourceState state = this.states.get(player.getUniqueId());
      if (state != null && affectedEnemies > 0) {
         ClassResourceDefinition rules = this.rules(state.type);
         if (!(rules.tauntGainPerEnemy() <= 0.0)) {
            this.set(state, state.amount + Math.min(rules.tauntGainCap(), affectedEnemies * rules.tauntGainPerEnemy()));
         }
      }
   }

   public Optional<ClassResourceController.Snapshot> snapshot(UUID playerId) {
      ClassResourceController.ResourceState state = this.states.get(playerId);
      return state == null ? Optional.empty() : Optional.of(new ClassResourceController.Snapshot(state.type, state.amount, this.rules(state.type).maximum()));
   }

   public boolean isOpen(UUID playerId, ClassResourceType type) {
      ClassResourceController.ResourceState state = this.states.get(playerId);
      return state != null && state.type == type && !state.suspended;
   }

   public boolean isOpen(UUID playerId, ClassResourceType type, UUID runToken) {
      ClassResourceController.ResourceState state = this.states.get(playerId);
      return state != null && state.type == type && Objects.equals(state.runToken, runToken) && !state.suspended;
   }

   public void shutdown() {
      this.states.clear();
   }

   private void gainScaledToHealth(Player player, ClassResourceController.ResourceState state, double healthAmount, double fullHealthEquivalentGain) {
      AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
      double maximumHealth = Math.max(1.0, maxHealth == null ? player.getHealth() : maxHealth.getValue());
      this.set(state, state.amount + healthAmount / maximumHealth * fullHealthEquivalentGain);
   }

   private void set(ClassResourceController.ResourceState state, double amount) {
      double maximum = this.rules(state.type).maximum();
      double bounded = state.practiceMode ? maximum : Math.max(0.0, Math.min(maximum, amount));
      if (!(Math.abs(bounded - state.amount) < 1.0E-9)) {
         state.amount = bounded;
      }
   }

   private ClassResourceDefinition rules(ClassResourceType type) {
      ClassResourceDefinition rules = this.resourceDefinitions.get(type);
      if (rules == null) {
         throw new IllegalArgumentException("No resource rules for " + type);
      } else {
         return rules;
      }
   }

   private static void requireSpendAmount(double amount) {
      if (!Double.isFinite(amount) || amount < 0.0) {
         throw new IllegalArgumentException("Resource spend amount must be finite and non-negative");
      }
   }

   private static final class ResourceState {
      private final ClassResourceType type;
      private final UUID runToken;
      private boolean suspended;
      private boolean practiceMode;
      private double amount;
      private long recoveryBlockedUntil;
      private long lastDamageTick;

      private ResourceState(ClassResourceType type, UUID runToken, double amount, long recoveryBlockedUntil) {
         this.type = type;
         this.runToken = runToken;
         this.amount = amount;
         this.recoveryBlockedUntil = recoveryBlockedUntil;
      }
   }

   public record Snapshot(ClassResourceType type, double amount, double maximum) {
      public double fraction() {
         return this.maximum <= 0.0 ? 0.0 : this.amount / this.maximum;
      }
   }
}
