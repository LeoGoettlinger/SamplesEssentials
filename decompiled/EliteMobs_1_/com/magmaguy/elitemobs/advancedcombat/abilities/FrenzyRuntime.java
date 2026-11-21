package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

final class FrenzyRuntime implements Listener, AutoCloseable {
   private static final String MOVEMENT_KEY = "advanced_frenzy_speed";
   private final NamespacedKey movementKey;
   private final Map<UUID, FrenzyRuntime.ActiveFrenzy> activeByPlayer = new HashMap<>();
   private final BukkitTask refreshTask;
   private boolean closed;

   FrenzyRuntime(Plugin plugin) {
      Objects.requireNonNull(plugin, "plugin");
      this.movementKey = new NamespacedKey(plugin, "advanced_frenzy_speed");
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshMovement, 1L, 1L);
   }

   void activate(Player player, double maximumDamageBonus, double maximumSpeedAdjustment, int durationTicks, String abilityId) {
      Objects.requireNonNull(player, "player");
      Objects.requireNonNull(abilityId, "abilityId");
      if (!this.closed && durationTicks > 0) {
         FrenzyScalingPolicy.scale(player.getHealth(), maximumHealth(player), maximumDamageBonus, maximumSpeedAdjustment);
         this.activeByPlayer
            .put(player.getUniqueId(), new FrenzyRuntime.ActiveFrenzy(abilityId, maximumDamageBonus, maximumSpeedAdjustment, expiresAt(durationTicks)));
         this.refreshMovement(player, this.activeByPlayer.get(player.getUniqueId()));
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         if (!this.closed && !(event.getDamage() <= 0.0)) {
            Player player = event.getPlayer();
            FrenzyRuntime.ActiveFrenzy frenzy = this.live(player);
            if (frenzy != null) {
               FrenzyScalingPolicy.Scaling scaling = scaling(player, frenzy);
               if (scaling.damageMultiplier() > 1.0) {
                  event.applyClassAbilityDamageMultiplier(frenzy.abilityId, scaling.damageMultiplier());
               }
            }
         }
      }
   }

   void clear(Player player) {
      Objects.requireNonNull(player, "player");
      this.activeByPlayer.remove(player.getUniqueId());
      this.applyMovement(player, 0.0);
   }

   private void refreshMovement() {
      for (UUID playerId : this.activeByPlayer.keySet().toArray(UUID[]::new)) {
         Player player = Bukkit.getPlayer(playerId);
         FrenzyRuntime.ActiveFrenzy frenzy = player == null ? null : this.live(player);
         if (frenzy == null) {
            this.activeByPlayer.remove(playerId);
            if (player != null) {
               this.applyMovement(player, 0.0);
            }
         } else {
            this.refreshMovement(player, frenzy);
         }
      }
   }

   private void refreshMovement(Player player, FrenzyRuntime.ActiveFrenzy frenzy) {
      double adjustment = scaling(player, frenzy).movementSpeedAdjustment();
      if (!Double.isFinite(frenzy.appliedSpeed) || !(Math.abs(frenzy.appliedSpeed - adjustment) < 1.0E-4)) {
         this.applyMovement(player, adjustment);
         frenzy.appliedSpeed = adjustment;
      }
   }

   private FrenzyRuntime.ActiveFrenzy live(Player player) {
      if (player == null) {
         return null;
      } else {
         FrenzyRuntime.ActiveFrenzy frenzy = this.activeByPlayer.get(player.getUniqueId());
         if (frenzy == null) {
            return null;
         } else if (frenzy.expiresAtNanos > System.nanoTime()
            && player.isOnline()
            && player.isValid()
            && !player.isDead()
            && ClassAbilityEligibility.isEligible(player)) {
            return frenzy;
         } else {
            this.activeByPlayer.remove(player.getUniqueId());
            this.applyMovement(player, 0.0);
            return null;
         }
      }
   }

   private static FrenzyScalingPolicy.Scaling scaling(Player player, FrenzyRuntime.ActiveFrenzy frenzy) {
      return FrenzyScalingPolicy.scale(player.getHealth(), maximumHealth(player), frenzy.maximumDamageBonus, frenzy.maximumSpeedAdjustment);
   }

   private void applyMovement(Player player, double adjustment) {
      AttributeInstance movement = player.getAttribute(Attribute.MOVEMENT_SPEED);
      if (movement != null) {
         movement.getModifiers().stream().filter(modifier -> modifier.getKey().equals(this.movementKey)).toList().forEach(movement::removeModifier);
         if (!(adjustment <= 0.0)) {
            movement.addModifier(new AttributeModifier(this.movementKey, Math.min(0.5, adjustment), Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));
         }
      }
   }

   private static double maximumHealth(Player player) {
      AttributeInstance maximum = player.getAttribute(Attribute.MAX_HEALTH);
      return maximum == null ? Math.max(1.0, player.getHealth()) : Math.max(1.0, maximum.getValue());
   }

   private static long expiresAt(int durationTicks) {
      long now = System.nanoTime();
      long duration = Math.max(1, durationTicks) * 50000000L;
      return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.refreshTask.cancel();
         HandlerList.unregisterAll(this);

         for (UUID playerId : this.activeByPlayer.keySet().toArray(UUID[]::new)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
               this.applyMovement(player, 0.0);
            }
         }

         this.activeByPlayer.clear();
      }
   }

   private static final class ActiveFrenzy {
      private final String abilityId;
      private final double maximumDamageBonus;
      private final double maximumSpeedAdjustment;
      private final long expiresAtNanos;
      private double appliedSpeed = Double.NaN;

      private ActiveFrenzy(String abilityId, double maximumDamageBonus, double maximumSpeedAdjustment, long expiresAtNanos) {
         this.abilityId = abilityId;
         this.maximumDamageBonus = maximumDamageBonus;
         this.maximumSpeedAdjustment = maximumSpeedAdjustment;
         this.expiresAtNanos = expiresAtNanos;
      }
   }
}
