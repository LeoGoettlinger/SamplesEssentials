package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSAdapter;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

final class EliteCrowdControlRuntime implements Listener, AutoCloseable {
   private static final double FEAR_SPEED_MODIFIER = 1.2;
   private static final String SLOW_POTENCY_KEY = "advanced_control_potency";
   private final FearLeaseRegistry<UUID, Location> fearLeases = new FearLeaseRegistry<>();
   private final SourceSuppressionLeaseRegistry<UUID, UUID> interruptLeases = new SourceSuppressionLeaseRegistry<>();
   private final SourceSuppressionLeaseRegistry<UUID, UUID> rootLeases = new SourceSuppressionLeaseRegistry<>();
   private final Map<UUID, Map<UUID, EliteCrowdControlRuntime.SlowPotencyLease>> slowPotencyLeases = new HashMap<>();
   private final Map<UUID, Double> appliedSlowAdjustments = new HashMap<>();
   private final NamespacedKey slowPotencyKey;
   private final BukkitTask tickTask;
   private long currentTick;
   private boolean closed;

   EliteCrowdControlRuntime(Plugin plugin) {
      Objects.requireNonNull(plugin, "plugin");
      this.slowPotencyKey = new NamespacedKey(plugin, "advanced_control_potency");
      Bukkit.getPluginManager().registerEvents(this, plugin);
      this.tickTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
   }

   boolean fear(Player caster, EliteEntity elite, int durationTicks) {
      if (!this.closed && caster != null && elite != null && durationTicks > 0) {
         LivingEntity target = elite.getLivingEntity();
         if (validPlayer(caster) && valid(target) && caster.getWorld() == target.getWorld()) {
            NMSAdapter adapter = NMSManager.getAdapter();
            if (adapter == null) {
               return false;
            } else {
               Location threat = caster.getLocation().clone();
               long expiresAt = saturatedAdd(this.currentTick, durationTicks);
               return this.fearLeases.apply(target.getUniqueId(), caster.getUniqueId(), threat, expiresAt, () -> open(adapter, target, threat));
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   boolean interrupt(Player caster, EliteEntity elite, int durationTicks) {
      if (!this.closed && caster != null && elite != null && durationTicks > 0) {
         LivingEntity target = elite.getLivingEntity();
         if (validPlayer(caster) && valid(target)) {
            long expiresAt = saturatedAdd(this.currentTick, durationTicks);
            return this.interruptLeases
               .apply(target.getUniqueId(), caster.getUniqueId(), expiresAt, () -> elite.getPowerSuppression().acquire(ElitePowerPauseReason.INTERRUPT));
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   boolean root(Player caster, EliteEntity elite, int durationTicks) {
      if (!this.closed && caster != null && elite != null && durationTicks > 0) {
         LivingEntity target = elite.getLivingEntity();
         if (validPlayer(caster) && valid(target)) {
            long expiresAt = saturatedAdd(this.currentTick, durationTicks);
            if (!this.rootLeases
               .apply(target.getUniqueId(), caster.getUniqueId(), expiresAt, () -> elite.getPowerSuppression().acquire(ElitePowerPauseReason.ROOT))) {
               return false;
            } else {
               immobilize(target);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   boolean extendRoot(Player caster, EliteEntity elite, int additionalTicks) {
      if (!this.closed && caster != null && elite != null && additionalTicks > 0) {
         LivingEntity target = elite.getLivingEntity();
         return validPlayer(caster) && valid(target) && this.rootLeases.extend(target.getUniqueId(), caster.getUniqueId(), additionalTicks, this.currentTick);
      } else {
         return false;
      }
   }

   int remainingTicks(Player caster, LivingEntity target, AbilityEffect effect) {
      return switch (effect) {
         case ROOT -> this.rootLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), this.currentTick);
         case INTERRUPT -> this.interruptLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), this.currentTick);
         case FEAR -> this.fearLeases.remainingTicks(target.getUniqueId(), caster.getUniqueId(), this.currentTick);
         default -> throw new IllegalArgumentException("Not a leased control: " + effect);
      };
   }

   boolean controlledBy(Player caster, LivingEntity target) {
      if (!this.closed && caster != null && target != null) {
         UUID targetId = target.getUniqueId();
         UUID casterId = caster.getUniqueId();
         return this.rootLeases.ownedBy(targetId, casterId) || this.fearLeases.ownedBy(targetId, casterId);
      } else {
         return false;
      }
   }

   void applySlowPotency(Player caster, EliteEntity elite, EliteControlEffectPlan plan) {
      if (!this.closed && caster != null && elite != null && plan != null && plan.durationTicks() > 0) {
         LivingEntity target = elite.getLivingEntity();
         if (validPlayer(caster) && valid(target)) {
            this.slowPotencyLeases
               .computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>())
               .put(
                  caster.getUniqueId(),
                  new EliteCrowdControlRuntime.SlowPotencyLease(
                     saturatedAdd(this.currentTick, plan.durationTicks()), plan.potionAmplifier(), plan.additionalMovementSpeedAdjustment()
                  )
               );
            this.refreshSlowPotency(target);
         }
      }
   }

   int ownedSlowRemainingTicks(Player caster, LivingEntity target, int amplifier) {
      Map<UUID, EliteCrowdControlRuntime.SlowPotencyLease> leases = this.slowPotencyLeases.get(target.getUniqueId());
      if (!this.closed && leases != null) {
         EliteCrowdControlRuntime.SlowPotencyLease lease = leases.get(caster.getUniqueId());
         PotionEffect actual = target.getPotionEffect(PotionEffectType.SLOWNESS);
         return lease != null && actual != null && lease.potionAmplifier() == amplifier && actual.getAmplifier() == amplifier
            ? (int)Math.max(0L, Math.min((long)actual.getDuration(), lease.expiresAtTick() - this.currentTick))
            : 0;
      } else {
         return 0;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onSlowChanged(EntityPotionEffectEvent event) {
      if (event.getModifiedType() == PotionEffectType.SLOWNESS && this.slowPotencyLeases.containsKey(event.getEntity().getUniqueId())) {
         if (event.getNewEffect() == null || event.getOldEffect() == null || event.isOverride()) {
            this.clearSlowPotency(event.getEntity().getUniqueId(), event.getEntity() instanceof LivingEntity living ? living : null);
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onEliteRemoved(EliteMobRemoveEvent event) {
      Entity entity = event.getEntity();
      if (entity != null) {
         this.fearLeases.remove(entity.getUniqueId());
         this.interruptLeases.remove(entity.getUniqueId());
         this.rootLeases.remove(entity.getUniqueId());
         this.clearSlowPotency(entity.getUniqueId(), entity instanceof LivingEntity living ? living : null);
      }
   }

   void clearSource(UUID sourceId) {
      this.fearLeases.clearSource(sourceId);
      this.rootLeases.clearSource(sourceId);
      this.interruptLeases.clearSource(sourceId);

      for (UUID targetId : this.slowPotencyLeases.keySet().toArray(UUID[]::new)) {
         if (this.slowPotencyLeases.get(targetId).remove(sourceId) != null) {
            Entity entity = Bukkit.getEntity(targetId);
            if (!this.slowPotencyLeases.get(targetId).isEmpty() && entity instanceof LivingEntity) {
               this.refreshSlowPotency((LivingEntity)entity);
            } else {
               this.clearSlowPotency(targetId, entity instanceof LivingEntity living ? living : null);
            }
         }
      }
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.tickTask.cancel();
         HandlerList.unregisterAll(this);
         this.fearLeases.close();
         this.interruptLeases.close();
         this.rootLeases.close();

         for (UUID targetId : this.slowPotencyLeases.keySet().toArray(UUID[]::new)) {
            this.clearSlowPotency(targetId, Bukkit.getEntity(targetId) instanceof LivingEntity living ? living : null);
         }

         this.slowPotencyLeases.clear();
         this.appliedSlowAdjustments.clear();
      }
   }

   private void tick() {
      if (!this.closed) {
         this.currentTick = saturatedAdd(this.currentTick, 1L);
         this.fearLeases.maintain(this.currentTick, this::validElite, this::currentThreat);
         this.interruptLeases.maintain(this.currentTick, this::validElite);
         this.rootLeases.maintain(this.currentTick, this::validElite);
         this.maintainSlowPotency();

         for (UUID entityId : this.rootLeases.activeTargets()) {
            if (Bukkit.getEntity(entityId) instanceof LivingEntity living) {
               NMSAdapter adapter = NMSManager.getAdapter();
               if (adapter != null) {
                  adapter.doNotMove(living);
               }

               immobilize(living);
            }
         }
      }
   }

   private Optional<Location> currentThreat(UUID casterId) {
      Player player = Bukkit.getPlayer(casterId);
      return validPlayer(player) ? Optional.of(player.getLocation().clone()) : Optional.empty();
   }

   private boolean validElite(UUID entityId) {
      return Bukkit.getEntity(entityId) instanceof LivingEntity livingEntity && valid(livingEntity) && EntityTracker.getEliteMobEntity(livingEntity) != null;
   }

   private void maintainSlowPotency() {
      for (UUID targetId : this.slowPotencyLeases.keySet().toArray(UUID[]::new)) {
         Entity entity = Bukkit.getEntity(targetId);
         if (entity instanceof LivingEntity target && valid(target) && EntityTracker.getEliteMobEntity(target) != null) {
            Map<UUID, EliteCrowdControlRuntime.SlowPotencyLease> leases = this.slowPotencyLeases.get(targetId);
            leases.entrySet().removeIf(entry -> entry.getValue().expiresAtTick() <= this.currentTick || !validSource(entry.getKey()));
            if (leases.isEmpty()) {
               this.clearSlowPotency(targetId, target);
            } else {
               this.refreshSlowPotency(target);
            }
         } else {
            this.clearSlowPotency(targetId, entity instanceof LivingEntity living ? living : null);
         }
      }
   }

   private void refreshSlowPotency(LivingEntity target) {
      Map<UUID, EliteCrowdControlRuntime.SlowPotencyLease> leases = this.slowPotencyLeases.get(target.getUniqueId());
      if (leases != null && !leases.isEmpty()) {
         PotionEffect currentSlow = target.getPotionEffect(PotionEffectType.SLOWNESS);
         if (currentSlow == null) {
            this.applySlowAdjustment(target, 0.0);
         } else {
            double adjustment = leases.values()
               .stream()
               .filter(lease -> lease.potionAmplifier() >= currentSlow.getAmplifier())
               .mapToDouble(EliteCrowdControlRuntime.SlowPotencyLease::movementSpeedAdjustment)
               .min()
               .orElse(0.0);
            this.applySlowAdjustment(target, adjustment);
         }
      } else {
         this.applySlowAdjustment(target, 0.0);
      }
   }

   private void applySlowAdjustment(LivingEntity target, double adjustment) {
      UUID targetId = target.getUniqueId();
      double previous = this.appliedSlowAdjustments.getOrDefault(targetId, 0.0);
      if (!(Math.abs(previous - adjustment) < 1.0E-9)) {
         AttributeInstance movement = target.getAttribute(Attribute.MOVEMENT_SPEED);
         if (movement != null) {
            movement.getModifiers().stream().filter(modifier -> modifier.getKey().equals(this.slowPotencyKey)).toList().forEach(movement::removeModifier);
            if (adjustment < 0.0) {
               movement.addModifier(
                  new AttributeModifier(this.slowPotencyKey, Math.max(-0.95, adjustment), Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY)
               );
               this.appliedSlowAdjustments.put(targetId, adjustment);
            } else {
               this.appliedSlowAdjustments.remove(targetId);
            }
         }
      }
   }

   private void clearSlowPotency(UUID targetId, LivingEntity target) {
      this.slowPotencyLeases.remove(targetId);
      if (target != null) {
         this.applySlowAdjustment(target, 0.0);
      }

      this.appliedSlowAdjustments.remove(targetId);
   }

   private static boolean validSource(UUID sourceId) {
      Player source = Bukkit.getPlayer(sourceId);
      return validPlayer(source) && ClassAbilityEligibility.isEligible(source);
   }

   private static Optional<FearLeaseRegistry.Redirectable<Location>> open(NMSAdapter adapter, LivingEntity target, Location threat) {
      try {
         return adapter.beginFlee(target, threat, 1.2).map(EliteCrowdControlRuntime.MovementRedirect::new);
      } catch (RuntimeException var4) {
         return Optional.empty();
      }
   }

   private static boolean valid(LivingEntity entity) {
      return entity != null && entity.isValid() && !entity.isDead() && entity.getHealth() > 0.0;
   }

   private static boolean validPlayer(Player player) {
      return player != null && player.isOnline() && valid(player);
   }

   private static void immobilize(LivingEntity target) {
      target.setVelocity(new Vector());
      target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3, 255, false, false, false), true);
   }

   private static long saturatedAdd(long value, long increment) {
      return value > Long.MAX_VALUE - increment ? Long.MAX_VALUE : value + increment;
   }

   private record MovementRedirect(TransientMovementOverride delegate) implements FearLeaseRegistry.Redirectable<Location> {
      private MovementRedirect(TransientMovementOverride delegate) {
         Objects.requireNonNull(delegate, "delegate");
         this.delegate = delegate;
      }

      @Override
      public boolean isActive() {
         return this.delegate.isActive();
      }

      public boolean retarget(Location sourceState) {
         return this.delegate.retarget(sourceState);
      }

      @Override
      public void close() {
         this.delegate.close();
      }
   }

   private record SlowPotencyLease(long expiresAtTick, int potionAmplifier, double movementSpeedAdjustment) {
   }
}
