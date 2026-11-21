package com.magmaguy.elitemobs.advancedcombat.passives;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.abilities.ClassControlAttribution;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.KnockbackEvents;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

public final class ClassPassiveRuntime implements Listener {
   private static final String MOVEMENT_MODIFIER_KEY = "advanced_class_movement";
   private static final Set<String> CONTROL_EFFECTS = Set.of("slowness", "mining_fatigue", "nausea", "blindness", "weakness", "levitation", "darkness");
   private final Function<UUID, PassiveAggregate> passiveProvider;
   private final Predicate<Player> combatActive;
   private final Map<UUID, Double> appliedMovementAdjustments = new HashMap<>();
   private final Map<UUID, Boolean> observedDrawStates = new HashMap<>();
   private final Set<UUID> mechanicsActivePlayers = new HashSet<>();
   private final Set<UUID> applyingControlResistance = new HashSet<>();
   private final PassiveStateTracker state = new PassiveStateTracker();

   public ClassPassiveRuntime(Function<UUID, PassiveAggregate> passiveProvider, Predicate<Player> combatActive) {
      this.passiveProvider = Objects.requireNonNull(passiveProvider, "passiveProvider");
      this.combatActive = Objects.requireNonNull(combatActive, "combatActive");
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         Player player = event.getPlayer();
         if (this.combatActive.test(player)) {
            PassiveAggregate passive = this.passiveProvider.apply(player.getUniqueId());
            boolean classAbilityDamage = CombatDamageContext.isClassAbilityDamageActive();
            Optional<CombatDamageContext.ClassAbilityDamageDomain> classAbilityDomain = CombatDamageContext.currentClassAbilityDamageDomain();
            SkillType sourceSkill = CombatDamageContext.currentPlayerToEliteSource().map(CombatDamageContext.PlayerDamageSource::progressionSkill).orElse(null);
            PassiveAggregate.Evaluation evaluation = passive.evaluate(
               this.context(
                  player,
                  passive,
                  event.getEliteMobEntity(),
                  event.isCriticalStrike(),
                  event.isRangedAttack(),
                  classAbilityDamage,
                  classAbilityDomain.orElse(null),
                  sourceSkill
               )
            );
            event.setDamage(Math.max(0.0, event.getDamage() * evaluation.outgoingDamageMultiplier()));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
      Player player = event.getPlayer();
      if (this.combatActive.test(player)) {
         PassiveAggregate passive = this.passiveProvider.apply(player.getUniqueId());
         PassiveAggregate.Evaluation evaluation = passive.evaluate(
            this.context(player, passive, event.getEliteMobEntity(), false, event.getProjectile() != null, false, null, null)
         );
         event.setDamage(Math.max(0.0, event.getDamage() * evaluation.incomingDamageMultiplier()));
         this.state.recordHit(player.getUniqueId());
         this.setMovementAdjustment(player, this.evaluatedMovement(player, passive));
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerHeals(EntityRegainHealthEvent event) {
      if (event.getEntity() instanceof Player player && this.combatActive.test(player)) {
         PassiveAggregate passive = this.passiveProvider.apply(player.getUniqueId());
         double multiplier = passive.evaluate(this.playerContext(player)).healingReceivedMultiplier();
         if (!(Math.abs(multiplier - 1.0) < 1.0E-9)) {
            event.setAmount(Math.max(0.0, event.getAmount() * multiplier));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteDeath(EliteMobDeathEvent event) {
      LivingEntity dead = event.getEntityDeathEvent() == null
         ? (event.getEntity() instanceof LivingEntity living ? living : null)
         : event.getEntityDeathEvent().getEntity();
      Player killer = dead == null ? null : dead.getKiller();
      if (killer != null && this.combatActive.test(killer)) {
         this.state.recordEliteKill(killer.getUniqueId());
      }
   }

   public void registerKnockback(Plugin plugin) {
      KnockbackEvents.registerEntityScale(
         plugin,
         this,
         (entity, source) -> entity instanceof Player player && this.combatActive.test(player) && eliteSource(source) != null
            ? PassiveRuntimePolicy.knockbackMultiplier(this.mechanics(player))
            : 1.0
      );
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPotionEffect(EntityPotionEffectEvent event) {
      if (event.getEntity() instanceof Player player && this.combatActive.test(player) && !this.applyingControlResistance.contains(player.getUniqueId())) {
         PotionEffect effect = event.getNewEffect();
         if (effect != null && CONTROL_EFFECTS.contains(effect.getType().getKey().getKey())) {
            int duration = PassiveRuntimePolicy.controlDurationTicks(effect.getDuration(), this.mechanics(player));
            if (duration != effect.getDuration()) {
               event.setCancelled(true);
               PotionEffect resisted = new PotionEffect(
                  effect.getType(), duration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()
               );
               UUID playerId = player.getUniqueId();
               this.applyingControlResistance.add(playerId);

               try {
                  player.addPotionEffect(resisted, true);
               } finally {
                  this.applyingControlResistance.remove(playerId);
               }
            }
         }
      }
   }

   public PassiveMechanics mechanics(Player player) {
      return player != null && this.combatActive.test(player)
         ? this.passiveProvider.apply(player.getUniqueId()).evaluate(this.playerContext(player)).mechanics()
         : PassiveMechanics.NEUTRAL;
   }

   public double healingDoneMultiplier(Player player) {
      return player != null && this.combatActive.test(player)
         ? this.passiveProvider.apply(player.getUniqueId()).evaluate(this.playerContext(player)).healingDoneMultiplier()
         : 1.0;
   }

   public double outgoingClassAbilityDamageMultiplier(Player owner, EliteEntity target, CombatDamageContext.ClassAbilityDamageDomain domain) {
      if (owner != null && target != null && domain != null && this.combatActive.test(owner)) {
         PassiveAggregate passive = this.passiveProvider.apply(owner.getUniqueId());
         return passive.evaluate(
               this.context(owner, passive, target, false, domain.delivery() == CombatDamageContext.ClassAbilityDelivery.PROJECTILE, true, domain, null)
            )
            .outgoingDamageMultiplier();
      } else {
         return 1.0;
      }
   }

   public void signalWardBroken(Player player, int durationTicks) {
      if (player != null && this.combatActive.test(player) && durationTicks > 0) {
         this.state.recordWardBroken(player.getUniqueId(), durationTicks);
      }
   }

   public void reconcile(Player player, boolean mechanicsActive) {
      UUID playerId = player.getUniqueId();
      if (mechanicsActive) {
         this.mechanicsActivePlayers.add(playerId);
      } else {
         this.mechanicsActivePlayers.remove(playerId);
         this.state.clear(playerId);
      }

      PassiveAggregate passive = mechanicsActive ? this.passiveProvider.apply(playerId) : null;
      if (passive != null && passive.requires(PassiveCondition.DRAWING_RANGED_WEAPON)) {
         this.observedDrawStates.put(playerId, drawingRangedWeapon(player));
      } else {
         this.observedDrawStates.remove(playerId);
      }

      double adjustment = mechanicsActive ? this.evaluatedMovement(player, passive) : 0.0;
      this.setMovementAdjustment(player, adjustment);
   }

   private void setMovementAdjustment(Player player, double adjustment) {
      Double previous = this.appliedMovementAdjustments.get(player.getUniqueId());
      if (previous == null || !(Math.abs(previous - adjustment) < 1.0E-9)) {
         if (applyMovementAdjustment(player, adjustment)) {
            this.appliedMovementAdjustments.put(player.getUniqueId(), adjustment);
         }
      }
   }

   public void tickItemUse(Collection<? extends Player> players) {
      for (Player player : players) {
         Boolean previous = this.observedDrawStates.get(player.getUniqueId());
         if (previous != null) {
            boolean active = this.combatActive.test(player) && !player.isDead();
            if (!active || previous != drawingRangedWeapon(player)) {
               this.reconcile(player, active);
            }
         }
      }
   }

   public void discard(Player player) {
      UUID playerId = player.getUniqueId();
      this.appliedMovementAdjustments.remove(playerId);
      this.observedDrawStates.remove(playerId);
      this.mechanicsActivePlayers.remove(playerId);
      this.applyingControlResistance.remove(playerId);
      this.state.clear(playerId);
      applyMovementAdjustment(player, 0.0);
   }

   public void shutdown() {
      for (UUID playerId : this.appliedMovementAdjustments.keySet().toArray(UUID[]::new)) {
         Player player = Bukkit.getPlayer(playerId);
         if (player != null) {
            applyMovementAdjustment(player, 0.0);
         }
      }

      this.appliedMovementAdjustments.clear();
      this.observedDrawStates.clear();
      this.mechanicsActivePlayers.clear();
      this.applyingControlResistance.clear();
      this.state.clearAll();
      HandlerList.unregisterAll(this);
   }

   public static void clearPersistedState(Player player) {
      applyMovementAdjustment(player, 0.0);
   }

   private static boolean applyMovementAdjustment(Player player, double adjustment) {
      AttributeInstance movement = player.getAttribute(Attribute.MOVEMENT_SPEED);
      if (movement == null) {
         return false;
      } else {
         NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, "advanced_class_movement");
         movement.getModifiers().stream().filter(modifier -> modifier.getKey().equals(key)).toList().forEach(movement::removeModifier);
         if (Math.abs(adjustment) < 1.0E-9) {
            return true;
         } else {
            movement.addModifier(new AttributeModifier(key, Math.max(-0.5, Math.min(0.5, adjustment)), Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.ANY));
            return true;
         }
      }
   }

   private double evaluatedMovement(Player player, PassiveAggregate passive) {
      PassiveAggregate.Evaluation evaluation = passive.evaluate(this.playerContext(player));
      return Math.max(-0.5, Math.min(0.5, evaluation.movementSpeedAdjustment() + this.partyMovementAdjustment(player)));
   }

   private PassiveConditionContext playerContext(Player player) {
      UUID playerId = player.getUniqueId();
      return PassiveConditionContext.playerOnly(
         healthFraction(player),
         moving(player),
         this.state.recentlyHit(playerId),
         grouped(player),
         this.state.recentEliteKill(playerId),
         this.state.wardBroken(playerId),
         drawingRangedWeapon(player)
      );
   }

   private PassiveConditionContext context(
      Player player,
      PassiveAggregate passive,
      EliteEntity elite,
      boolean criticalHit,
      boolean rangedAttack,
      boolean classAbilityDamage,
      CombatDamageContext.ClassAbilityDamageDomain classAbilityDomain,
      SkillType sourceSkill
   ) {
      LivingEntity target = elite == null ? null : elite.getLivingEntity();
      boolean present = target != null && target.isValid() && !target.isDead();
      boolean needsDensity = passive.requires(PassiveCondition.TARGET_ISOLATED) || passive.requires(PassiveCondition.TARGET_GROUPED);
      ClassPassiveRuntime.TargetDensity density = present && needsDensity ? targetDensity(target) : ClassPassiveRuntime.TargetDensity.NONE;
      ClassPassiveRuntime.AbilityDamageFacts damageFacts = abilityDamageFacts(classAbilityDamage, classAbilityDomain);
      return new PassiveConditionContext(
         healthFraction(player),
         moving(player),
         this.state.recentlyHit(player.getUniqueId()),
         grouped(player),
         present,
         present ? healthFraction(target) : 1.0,
         present && (!elite.isNaturalEntity() || elite.getHealthMultiplier() > 1.5),
         present && targetControlled(player, target),
         present && density.nearbyElites() == 0,
         present && density.nearbyElites() > 0,
         present ? player.getLocation().distance(target.getLocation()) : Double.POSITIVE_INFINITY,
         criticalHit,
         rangedAttack,
         damageFacts.classAbilityDamage(),
         damageFacts.nonSummonClassAbilityDamage(),
         damageFacts.areaClassAbilityDamage(),
         damageFacts.trapClassAbilityDamage(),
         damageFacts.blastClassAbilityDamage(),
         PassiveRuntimePolicy.isMagicWeaponSkill(sourceSkill),
         this.state.recentEliteKill(player.getUniqueId()),
         this.state.wardBroken(player.getUniqueId()),
         drawingRangedWeapon(player)
      );
   }

   private static boolean drawingRangedWeapon(Player player) {
      if (player.isDead()) {
         return false;
      } else {
         ItemStack item = player.getItemInUse();
         if (item == null) {
            return false;
         } else {
            return switch (item.getType()) {
               case BOW, CROSSBOW -> true;
               default -> false;
            };
         }
      }
   }

   private double partyMovementAdjustment(Player player) {
      List<Player> nearby = PartyManager.getNearbyMembers(player, player.getLocation());
      boolean grouped = nearby.stream().anyMatch(member -> !member.getUniqueId().equals(player.getUniqueId()));
      List<PassiveMechanics> auras = nearby.stream().filter(member -> this.mechanicsActivePlayers.contains(member.getUniqueId())).map(this::mechanics).toList();
      return PassiveRuntimePolicy.partyMovementAdjustment(auras, grouped);
   }

   private static EliteEntity eliteSource(Entity source) {
      EliteEntity direct = source == null ? null : EntityTracker.getEliteMobEntity(source);
      if (direct != null) {
         return direct;
      } else {
         return source instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter ? EntityTracker.getEliteMobEntity(shooter) : null;
      }
   }

   private static boolean grouped(Player player) {
      return PartyManager.getNearbyMembers(player, player.getLocation()).stream().anyMatch(member -> !member.getUniqueId().equals(player.getUniqueId()));
   }

   private static boolean moving(Player player) {
      double horizontalSpeedSquared = player.getVelocity().getX() * player.getVelocity().getX() + player.getVelocity().getZ() * player.getVelocity().getZ();
      return player.isSprinting() || horizontalSpeedSquared > 0.01;
   }

   static boolean targetControlled(Player player, LivingEntity target) {
      return ClassControlAttribution.controlledBy(player, target);
   }

   static ClassPassiveRuntime.AbilityDamageFacts abilityDamageFacts(boolean classAbilityDamage, CombatDamageContext.ClassAbilityDamageDomain domain) {
      if (!classAbilityDamage) {
         return ClassPassiveRuntime.AbilityDamageFacts.NONE;
      } else {
         boolean nonSummon = domain == null || domain.delivery() != CombatDamageContext.ClassAbilityDelivery.SUMMON;
         boolean area = domain != null && domain.targetShape() == CombatDamageContext.ClassAbilityTargetShape.AREA;
         boolean trap = domain != null && domain.archetype() == CombatDamageContext.ClassAbilityArchetype.TRAP;
         boolean blast = domain != null && domain.archetype() == CombatDamageContext.ClassAbilityArchetype.BLAST;
         return new ClassPassiveRuntime.AbilityDamageFacts(true, nonSummon, area, trap, blast);
      }
   }

   private static ClassPassiveRuntime.TargetDensity targetDensity(LivingEntity target) {
      int nearbyElites = 0;

      for (Entity candidate : target.getWorld().getNearbyEntities(target.getLocation(), 7.0, 5.0, 7.0)) {
         if (candidate != target && candidate instanceof LivingEntity living && EntityTracker.getEliteMobEntity(living) != null) {
            nearbyElites++;
            break;
         }
      }

      return new ClassPassiveRuntime.TargetDensity(nearbyElites);
   }

   private static double healthFraction(LivingEntity entity) {
      AttributeInstance maximumHealth = entity.getAttribute(Attribute.MAX_HEALTH);
      double maximum = maximumHealth == null ? entity.getHealth() : maximumHealth.getValue();
      return Double.isFinite(maximum) && !(maximum <= 0.0) ? Math.max(0.0, Math.min(1.0, entity.getHealth() / maximum)) : 1.0;
   }

   record AbilityDamageFacts(
      boolean classAbilityDamage,
      boolean nonSummonClassAbilityDamage,
      boolean areaClassAbilityDamage,
      boolean trapClassAbilityDamage,
      boolean blastClassAbilityDamage
   ) {
      private static final ClassPassiveRuntime.AbilityDamageFacts NONE = new ClassPassiveRuntime.AbilityDamageFacts(false, false, false, false, false);
   }

   private record TargetDensity(int nearbyElites) {
      private static final ClassPassiveRuntime.TargetDensity NONE = new ClassPassiveRuntime.TargetDensity(0);
   }
}
