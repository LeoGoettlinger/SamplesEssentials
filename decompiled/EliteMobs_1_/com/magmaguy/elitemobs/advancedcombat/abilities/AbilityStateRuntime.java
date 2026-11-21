package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

final class AbilityStateRuntime implements Listener, AutoCloseable {
   private static final double REDIRECT_FRACTION = 0.35;
   private static final double REDIRECT_EFFICIENCY = 0.7;
   private static final double SHARE_FRACTION = 0.3;
   private static final Set<PotionEffectType> CONTROL_EFFECTS = Set.of(
      PotionEffectType.SLOWNESS, PotionEffectType.LEVITATION, PotionEffectType.MINING_FATIGUE, PotionEffectType.BLINDNESS, PotionEffectType.DARKNESS
   );
   private static final Set<PotionEffectType> DEBUFF_EFFECTS = Set.of(
      PotionEffectType.SLOWNESS,
      PotionEffectType.WEAKNESS,
      PotionEffectType.POISON,
      PotionEffectType.WITHER,
      PotionEffectType.BLINDNESS,
      PotionEffectType.NAUSEA,
      PotionEffectType.MINING_FATIGUE,
      PotionEffectType.LEVITATION,
      PotionEffectType.DARKNESS,
      PotionEffectType.HUNGER,
      PotionEffectType.UNLUCK
   );
   private static final long RECENT_ATTACKER_TICKS = 160L;
   private final ClassAbilityDamage damage;
   private final Plugin plugin;
   private final AbilitySemantics semantics;
   private final FixedAbilityRegistry registry;
   private final Map<UUID, List<AbilityStateRuntime.RedirectLink>> redirectsByTarget = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.DeathGuard> deathGuards = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TriggeredBarrier> triggeredBarriers = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TriggeredProtection> triggeredProtections = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.ShareLink> sharesByMember = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TimedSource> lifestealWindows = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TimedSource> controlImmunity = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TimedSource> debuffImmunity = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.PlantedGuard> plantedGuards = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.DetonatingMark> detonatingMarks = new HashMap<>();
   private final Map<UUID, RetaliationBudget> storedRetaliation = new HashMap<>();
   private final Map<UUID, Map<UUID, Long>> recentAttackers = new HashMap<>();
   private final Map<UUID, Map<UUID, Long>> tauntsByCaster = new HashMap<>();
   private final Map<UUID, Map<UUID, Long>> defenseBreaksByCaster = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TimedSource> activeFields = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.TimedSource> activeWindUps = new HashMap<>();
   private final Map<UUID, AbilityStateRuntime.PendingHealEcho> pendingHealEchoes = new HashMap<>();
   private final Map<UUID, Long> lifecycleGenerations = new HashMap<>();
   private final Set<UUID> redistributing = new HashSet<>();
   private final Set<UUID> ownedVelocities = new HashSet<>();
   private final AbilityStateOwnership stateOwnership;
   private boolean closed;

   AbilityStateRuntime(Plugin plugin, AbilitySemantics semantics, FixedAbilityRegistry registry, ClassAbilityDamage damage) {
      this.damage = damage;
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.semantics = Objects.requireNonNull(semantics, "semantics");
      this.registry = Objects.requireNonNull(registry, "registry");
      this.stateOwnership = this.buildStateOwnership();
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   boolean canActivate(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      AbilityTargeting.TargetSelection qualified = this.qualifyTargets(caster, spec, selection);
      double nearestEnemyDistance = qualified.enemies()
         .stream()
         .mapToDouble(enemy -> enemy.getLocation().distance(caster.getLocation()))
         .min()
         .orElse(Double.POSITIVE_INFINITY);
      double lowestAllyHealth = qualified.allies().stream().mapToDouble(AbilityStateRuntime::healthFraction).min().orElse(Double.POSITIVE_INFINITY);
      double minimumRange = Math.max(8.0, spec.tuning().range() * 0.25);
      boolean moving = caster.isSprinting()
         || caster.getVelocity().getX() * caster.getVelocity().getX() + caster.getVelocity().getZ() * caster.getVelocity().getZ() > 0.01;
      if (!AbilityActivationPolicy.permits(mechanics, moving, nearestEnemyDistance, minimumRange, lowestAllyHealth)) {
         return false;
      } else if ((mechanics.contains(AbilityMechanic.DAMAGE_REDIRECT) || mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT))
         && qualified.allies().stream().noneMatch(ally -> !ally.getUniqueId().equals(caster.getUniqueId()))) {
         return false;
      } else if (mechanics.contains(AbilityMechanic.RECENT_ATTACKERS) && qualified.enemies().isEmpty()) {
         return false;
      } else if (mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY) && qualified.enemies().isEmpty()) {
         return false;
      } else if (mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK) && qualified.enemies().isEmpty()) {
         return false;
      } else {
         long now = System.nanoTime();
         return mechanics.contains(AbilityMechanic.REQUIRES_ACTIVE_FIELD) && live(this.activeFields, caster.getUniqueId(), now) == null
            ? false
            : !mechanics.contains(AbilityMechanic.REQUIRES_WIND_UP) || live(this.activeWindUps, caster.getUniqueId(), now) != null;
      }
   }

   AbilityTargeting.TargetSelection qualifyTargets(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      List<LivingEntity> enemies = new ArrayList<>(selection.enemies());
      List<Player> allies = new ArrayList<>(selection.allies());
      long now = System.nanoTime();
      if (mechanics.contains(AbilityMechanic.RECENT_ATTACKERS)) {
         Player defended = allies.stream().filter(ally -> !ally.getUniqueId().equals(caster.getUniqueId())).findFirst().orElse(caster);
         enemies = new ArrayList<>(
            this.recentAttackers(defended, now)
               .stream()
               .filter(enemy -> enemy.getWorld().equals(caster.getWorld()))
               .filter(enemy -> this.semantics.canTargetEnemy(caster, enemy, spec))
               .filter(enemy -> withinAbilityReach(enemy, selection.origin(), spec))
               .toList()
         );
      }

      if (mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY)) {
         enemies.removeIf(enemy -> !hasLiveStatus(this.tauntsByCaster, caster.getUniqueId(), enemy.getUniqueId(), now));
      }

      if (mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK)) {
         enemies.removeIf(enemy -> !hasLiveStatus(this.defenseBreaksByCaster, caster.getUniqueId(), enemy.getUniqueId(), now));
      }

      if (mechanics.contains(AbilityMechanic.LOW_HEALTH_ALLY_ONLY)) {
         allies.removeIf(ally -> healthFraction(ally) > 0.35);
      }

      if (mechanics.contains(AbilityMechanic.CASTER_ONLY_FIELD)) {
         allies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));
      }

      if (mechanics.contains(AbilityMechanic.CASTER_ONLY_SUPPORT)) {
         allies.removeIf(ally -> !ally.getUniqueId().equals(caster.getUniqueId()));
      }

      return new AbilityTargeting.TargetSelection(enemies, allies, selection.origin());
   }

   void activate(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel) {
      if (!this.closed) {
         Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
         int durationTicks = Math.max(20, ActiveAbilityLevelScaling.durationTicks(spec.tuning().durationTicks(), effectiveLevel));
         long expires = expiresAt(durationTicks);
         double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
         AbilityMechanicModifiers mechanicModifiers = this.semantics.mechanicModifiers(caster);
         double burstHealingMultiplier = this.semantics.healingMultiplier(caster)
            * AbilityHealingPolicy.multiplier(mechanicModifiers, AbilityHealingType.BURST, selection.enemies().size());
         if (mechanics.contains(AbilityMechanic.FOLLOW_CASTER_FIELD)) {
            this.activeFields
               .compute(
                  caster.getUniqueId(),
                  (ignored, current) -> (AbilityStateRuntime.TimedSource)(current != null && current.expiresAtNanos() > System.nanoTime()
                     ? current
                     : new AbilityStateRuntime.TimedSource(caster.getUniqueId(), expires, spec.id()))
               );
            this.observeStateArmed(caster, caster, spec, AbilityMechanic.FOLLOW_CASTER_FIELD, spec.tuning().radius(), durationTicks);
         }

         if (mechanics.contains(AbilityMechanic.DAMAGE_REDIRECT) || mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT)) {
            int maximum = mechanics.contains(AbilityMechanic.MULTI_ALLY_REDIRECT) ? 2 : 1;
            AbilityMechanic redirectMechanic = maximum > 1 ? AbilityMechanic.MULTI_ALLY_REDIRECT : AbilityMechanic.DAMAGE_REDIRECT;

            for (Player ally : selection.allies()
               .stream()
               .filter(allyx -> !allyx.getUniqueId().equals(caster.getUniqueId()))
               .sorted(Comparator.comparingDouble(AbilityStateRuntime::healthFraction))
               .limit(maximum)
               .toList()) {
               List<AbilityStateRuntime.RedirectLink> links = this.redirectsByTarget.computeIfAbsent(ally.getUniqueId(), ignored -> new ArrayList<>());
               AbilityStateRuntime.RedirectLink next = new AbilityStateRuntime.RedirectLink(
                  caster.getUniqueId(), 0.35, 0.7, mechanicModifiers.redirectedDamageMultiplier(), expires, spec.id(), redirectMechanic
               );
               long now = System.nanoTime();
               links.removeIf(linkx -> linkx.expiresAtNanos() <= now || linkx.equivalent(next) && linkx.expiresAtNanos() <= next.expiresAtNanos());
               if (links.stream().noneMatch(linkx -> linkx.equivalent(next))) {
                  links.add(next);
               }

               this.observeStateArmed(caster, ally, spec, redirectMechanic, 0.35, durationTicks);
            }
         }

         if (mechanics.contains(AbilityMechanic.DEATH_GUARD)) {
            for (Player target : selection.allies().isEmpty() ? List.of(caster) : selection.allies()) {
               AbilityStateRuntime.DeathGuard guard = new AbilityStateRuntime.DeathGuard(
                  caster.getUniqueId(),
                  spec.tuning().healingFraction() * levelScale * burstHealingMultiplier,
                  spec.tuning().shieldFraction() * levelScale * mechanicModifiers.shieldStrengthMultiplier(),
                  mechanics.contains(AbilityMechanic.WARD_BREAK_SIGNAL),
                  Math.max(20, Math.min(200, durationTicks)),
                  expires,
                  spec.id()
               );
               this.deathGuards.put(target.getUniqueId(), guard);
               double projectedRecovery = maximumHealth(target) * (guard.healingFraction() + guard.shieldFraction());
               this.observeStateArmed(caster, target, spec, AbilityMechanic.DEATH_GUARD, projectedRecovery, durationTicks);
            }
         }

         if (mechanics.contains(AbilityMechanic.BARRIER_BREAK_HEAL)) {
            for (Player target : selection.allies()) {
               AbilityStateRuntime.TriggeredBarrier barrier = new AbilityStateRuntime.TriggeredBarrier(
                  caster.getUniqueId(),
                  spec.tuning().healingFraction() * levelScale * burstHealingMultiplier,
                  target.getAbsorptionAmount(),
                  maximumHealth(target) * spec.tuning().shieldFraction() * levelScale * mechanicModifiers.shieldStrengthMultiplier(),
                  expires,
                  spec.id()
               );
               this.triggeredBarriers.put(target.getUniqueId(), barrier);
               this.observeStateArmed(
                  caster, target, spec, AbilityMechanic.BARRIER_BREAK_HEAL, maximumHealth(target) * barrier.healingFraction(), durationTicks
               );
            }
         }

         if (mechanics.contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)) {
            for (Player target : selection.allies()) {
               AbilityStateRuntime.TriggeredProtection protection = new AbilityStateRuntime.TriggeredProtection(
                  caster.getUniqueId(),
                  maximumHealth(target) * spec.tuning().shieldFraction() * levelScale * mechanicModifiers.shieldStrengthMultiplier(),
                  expires,
                  spec.id()
               );
               this.triggeredProtections.put(target.getUniqueId(), protection);
               this.observeStateArmed(caster, target, spec, AbilityMechanic.THREAT_TRIGGERED_PROTECTION, protection.protectionAmount(), durationTicks);
            }
         }

         if (mechanics.contains(AbilityMechanic.DAMAGE_SHARE)) {
            List<UUID> members = selection.allies().stream().filter(OfflinePlayer::isOnline).<UUID>map(OfflinePlayer::getUniqueId).distinct().toList();
            if (members.size() > 1) {
               AbilityStateRuntime.ShareLink link = new AbilityStateRuntime.ShareLink(caster.getUniqueId(), members, 0.3, expires, spec.id());

               for (UUID member : members) {
                  this.sharesByMember.put(member, link);
                  this.observeStateArmed(caster.getUniqueId(), member, spec, AbilityMechanic.DAMAGE_SHARE, 0.3, durationTicks);
               }
            }
         }

         if (mechanics.contains(AbilityMechanic.LIFESTEAL_WINDOW)) {
            this.lifestealWindows.put(caster.getUniqueId(), new AbilityStateRuntime.TimedSource(caster.getUniqueId(), expires, spec.id()));
            this.observeStateArmed(caster, caster, spec, AbilityMechanic.LIFESTEAL_WINDOW, 0.16, durationTicks);
         }

         if (mechanics.contains(AbilityMechanic.CONTROL_IMMUNITY)) {
            for (Player target : selection.allies().isEmpty() ? List.of(caster) : selection.allies()) {
               this.controlImmunity.put(target.getUniqueId(), new AbilityStateRuntime.TimedSource(caster.getUniqueId(), expires, spec.id()));
               CONTROL_EFFECTS.forEach(target::removePotionEffect);
               this.observeStateArmed(caster, target, spec, AbilityMechanic.CONTROL_IMMUNITY, 1.0, durationTicks);
            }
         }

         if (mechanics.contains(AbilityMechanic.DEBUFF_IMMUNITY)) {
            for (Player target : selection.allies().isEmpty() ? List.of(caster) : selection.allies()) {
               this.debuffImmunity.put(target.getUniqueId(), new AbilityStateRuntime.TimedSource(caster.getUniqueId(), expires, spec.id()));
               DEBUFF_EFFECTS.forEach(target::removePotionEffect);
               this.observeStateArmed(caster, target, spec, AbilityMechanic.DEBUFF_IMMUNITY, 1.0, durationTicks);
            }
         }

         if (mechanics.contains(AbilityMechanic.PLANTED_GUARD)) {
            this.plantedGuards.put(caster.getUniqueId(), new AbilityStateRuntime.PlantedGuard(caster.getUniqueId(), caster.getLocation(), expires, spec.id()));
            this.observeStateArmed(caster, caster, spec, AbilityMechanic.PLANTED_GUARD, 0.35, durationTicks);
         }

         if (mechanics.contains(AbilityMechanic.SUSTAINED_TETHER)) {
            this.scheduleTether(caster, spec, selection, effectiveLevel);
            selection.allies()
               .stream()
               .findFirst()
               .ifPresent(
                  target -> this.observeStateArmed(caster, target, spec, AbilityMechanic.SUSTAINED_TETHER, spec.tuning().healingFraction(), durationTicks)
               );
         }

         if (mechanics.contains(AbilityMechanic.HEAL_ECHO)) {
            double echoFraction = ActiveAbilityLevelScaling.amount(0.55, effectiveLevel);
            this.pendingHealEchoes.put(caster.getUniqueId(), new AbilityStateRuntime.PendingHealEcho(expires, spec.id(), echoFraction));
            this.observeStateArmed(caster, caster, spec, AbilityMechanic.HEAL_ECHO, echoFraction, durationTicks);
         }
      }
   }

   void armDetonation(Player caster, FixedAbilitySpec spec, LivingEntity enemy, int effectiveLevel) {
      if (!this.closed && spec.executionTraits().mechanics().contains(AbilityMechanic.DETONATING_MARK)) {
         int durationTicks = Math.max(20, ActiveAbilityLevelScaling.durationTicks(spec.tuning().durationTicks(), effectiveLevel));
         this.detonatingMarks.put(enemy.getUniqueId(), new AbilityStateRuntime.DetonatingMark(caster.getUniqueId(), expiresAt(durationTicks), spec.id()));
         this.semantics
            .observe(
               AbilityRuntimeObservation.state(
                  AbilityRuntimeObservation.Kind.STATE_ARMED,
                  caster.getUniqueId(),
                  enemy.getUniqueId(),
                  spec.id(),
                  0.55,
                  durationTicks,
                  AbilityMechanic.DETONATING_MARK
               )
            );
      }
   }

   void beginWindUp(Player caster, FixedAbilitySpec spec, int durationTicks) {
      if (!this.closed && spec.executionTraits().mechanics().contains(AbilityMechanic.WIND_UP)) {
         this.activeWindUps.put(caster.getUniqueId(), new AbilityStateRuntime.TimedSource(caster.getUniqueId(), expiresAt(durationTicks), spec.id()));
         this.observeStateArmed(caster, caster, spec, AbilityMechanic.WIND_UP, 1.0, Math.max(1, durationTicks));
      }
   }

   Set<UUID> registerTaunts(Player caster, Collection<? extends LivingEntity> enemies, int durationTicks, boolean extendExisting) {
      return registerStatuses(this.tauntsByCaster, caster, enemies, durationTicks, extendExisting);
   }

   boolean hasOwnedTaunt(Player caster, LivingEntity target) {
      return !this.closed && caster != null && target != null
         ? hasLiveStatus(this.tauntsByCaster, caster.getUniqueId(), target.getUniqueId(), System.nanoTime())
         : false;
   }

   void registerDefenseBreaks(Player caster, Collection<? extends LivingEntity> enemies, int durationTicks) {
      registerStatuses(this.defenseBreaksByCaster, caster, enemies, durationTicks, false);
   }

   int linkedDurationTicks(Player caster, FixedAbilitySpec spec, int authoredTicks) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      AbilityStateRuntime.TimedSource linked = null;
      long now = System.nanoTime();
      if (mechanics.contains(AbilityMechanic.REQUIRES_ACTIVE_FIELD)) {
         linked = live(this.activeFields, caster.getUniqueId(), now);
      }

      if (mechanics.contains(AbilityMechanic.REQUIRES_WIND_UP)) {
         linked = live(this.activeWindUps, caster.getUniqueId(), now);
      }

      if (linked == null) {
         return authoredTicks;
      } else {
         long remaining = Math.max(0L, linked.expiresAtNanos() - now);
         int remainingTicks = (int)Math.min(2147483647L, Math.max(1L, remaining / 50000000L));
         return Math.min(Math.max(1, authoredTicks), remainingTicks);
      }
   }

   boolean suppressesImmediate(FixedAbilitySpec spec, AbilityEffect effect) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      if (mechanics.contains(AbilityMechanic.HEAL_ECHO) && effect == AbilityEffect.HEAL) {
         return true;
      } else if (mechanics.contains(AbilityMechanic.BARRIER_BREAK_HEAL) && effect == AbilityEffect.HEAL) {
         return true;
      } else {
         return !mechanics.contains(AbilityMechanic.THREAT_TRIGGERED_PROTECTION)
            ? false
            : effect == AbilityEffect.SHIELD || effect == AbilityEffect.ALLY_PROTECT;
      }
   }

   double damageMultiplier(FixedAbilitySpec spec, LivingEntity target) {
      double multiplier = 1.0;
      if (spec.executionTraits().mechanics().contains(AbilityMechanic.EXECUTE_DAMAGE)) {
         AttributeInstance maximum = target.getAttribute(Attribute.MAX_HEALTH);
         double max = maximum == null ? Math.max(1.0, target.getHealth()) : Math.max(1.0, maximum.getValue());
         double missing = 1.0 - Math.max(0.0, Math.min(1.0, target.getHealth() / max));
         multiplier *= 1.0 + missing * 1.5;
      }

      return multiplier;
   }

   double retaliationAvailable(Player caster, FixedAbilitySpec spec) {
      RetaliationBudget stored = this.storedRetaliation.get(caster.getUniqueId());
      return stored != null && stored.abilityId().equals(spec.id()) ? stored.amount() : 0.0;
   }

   double consumeRetaliation(Player caster, FixedAbilitySpec spec, LivingEntity target) {
      RetaliationBudget stored = this.storedRetaliation.get(caster.getUniqueId());
      if (stored != null && stored.abilityId().equals(spec.id()) && this.storedRetaliation.remove(caster.getUniqueId(), stored)) {
         this.semantics
            .observe(
               AbilityRuntimeObservation.state(
                  AbilityRuntimeObservation.Kind.RETALIATION_RELEASED,
                  caster.getUniqueId(),
                  target.getUniqueId(),
                  spec.id(),
                  stored.amount(),
                  0,
                  AbilityMechanic.RETALIATION_RELEASE
               )
            );
         return stored.amount();
      } else {
         return 0.0;
      }
   }

   void scheduleHealEcho(Player caster, Player target, double effectiveHealing, FixedAbilitySpec spec) {
      if (!(effectiveHealing <= 0.0) && !spec.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO)) {
         AbilityStateRuntime.PendingHealEcho pending = this.pendingHealEchoes.get(caster.getUniqueId());
         if (pending != null) {
            if (pending.expiresAtNanos() <= System.nanoTime()) {
               this.pendingHealEchoes.remove(caster.getUniqueId(), pending);
            } else if (this.pendingHealEchoes.remove(caster.getUniqueId(), pending)) {
               this.observeStateConsumed(caster.getUniqueId(), target.getUniqueId(), pending.abilityId(), effectiveHealing, AbilityMechanic.HEAL_ECHO);
               UUID sourceId = caster.getUniqueId();
               UUID targetId = target.getUniqueId();
               long sourceGeneration = this.lifecycleGeneration(sourceId);
               long targetGeneration = this.lifecycleGeneration(targetId);
               Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                  if (this.delayedAllyStillValid(sourceId, sourceGeneration, target, targetId, targetGeneration, 24.0)) {
                     double healed = heal(target, effectiveHealing * pending.echoFraction());
                     if (healed > 0.0) {
                        this.semantics.recordContribution(caster, pending.abilityId(), new AbilityContribution(0.0, healed, 0.0, 0.0, 0, 1, 0.0));
                        this.observeEffect(AbilityRuntimeObservation.Kind.HEAL, sourceId, targetId, pending.abilityId(), healed, 0, AbilityEffect.HEAL);
                     }
                  }
               }, 30L);
            }
         }
      }
   }

   private FixedAbilitySpec retaliationAbility(Player player) {
      FixedAbilitySpec active = null;

      for (String formId : this.semantics.activeFormIds(player)) {
         FixedAbilitySpec candidate;
         try {
            candidate = this.registry.require(formId + ".signature");
         } catch (IllegalArgumentException var7) {
            continue;
         }

         if (candidate.executionTraits().mechanics().contains(AbilityMechanic.RETALIATION_RELEASE)) {
            active = candidate;
         }
      }

      return active;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerDamaged(PlayerDamagedByEliteMobEvent event) {
      if (!this.closed && !(event.getDamage() <= 0.0)) {
         Player target = event.getPlayer();
         if (ClassAbilityEligibility.isEligible(target)) {
            long now = System.nanoTime();
            double damage = event.getDamage();
            LivingEntity attacker = event.getEliteMobEntity().getLivingEntity();
            if (valid(attacker)) {
               this.recentAttackers.computeIfAbsent(target.getUniqueId(), ignored -> new HashMap<>()).put(attacker.getUniqueId(), expiresAt(160));
            }

            AbilityStateRuntime.PlantedGuard planted = this.livePlanted(target, now);
            if (planted != null) {
               damage *= 0.35;
            }

            FixedAbilitySpec retaliationAbility = this.retaliationAbility(target);
            if (retaliationAbility != null && valid(attacker) && hasLiveStatus(this.tauntsByCaster, target.getUniqueId(), attacker.getUniqueId(), now)) {
               double cap = maximumHealth(target) * 2.0;
               RetaliationBudget budget = RetaliationBudget.accumulate(
                  this.storedRetaliation.get(target.getUniqueId()), damage * 0.35, cap, retaliationAbility.id(), attacker.getUniqueId()
               );
               this.storedRetaliation.put(target.getUniqueId(), budget);
               this.semantics
                  .observe(
                     AbilityRuntimeObservation.state(
                        AbilityRuntimeObservation.Kind.RETALIATION_STORED,
                        target.getUniqueId(),
                        attacker.getUniqueId(),
                        retaliationAbility.id(),
                        budget.amount(),
                        0,
                        AbilityMechanic.RETALIATION_RELEASE
                     )
                  );
            }

            boolean redistributedDamage = this.redistributing.contains(target.getUniqueId());
            AbilityStateRuntime.RedirectLink redirect = redistributedDamage ? null : this.strongestRedirect(target.getUniqueId(), now);
            if (redirect != null) {
               Player protector = Bukkit.getPlayer(redirect.sourceId());
               if (validLinkedPlayers(protector, target, 24.0)) {
                  double redirected = damage * redirect.fraction();
                  damage -= redirected;
                  double received = redirected * redirect.efficiency() * redirect.redirectedDamageMultiplier();
                  this.dealSharedDamage(protector, event.getEliteMobEntity().getLivingEntity(), received);
                  this.semantics
                     .recordContribution(
                        protector, redirect.abilityId(), new AbilityContribution(0.0, 0.0, Math.max(0.0, redirected - received), 0.0, 0, 1, 0.0)
                     );
                  this.semantics
                     .observe(
                        new AbilityRuntimeObservation(
                           AbilityRuntimeObservation.Kind.DAMAGE_REDIRECTED,
                           redirect.sourceId(),
                           target.getUniqueId(),
                           redirect.abilityId(),
                           redirected,
                           0,
                           1,
                           Set.of(AbilityEffect.ALLY_PROTECT),
                           Set.of(redirect.mechanic())
                        )
                     );
               }
            }

            AbilityStateRuntime.ShareLink share = this.liveShare(target.getUniqueId(), now);
            if (share != null && !redistributedDamage) {
               List<Player> recipients = share.members()
                  .stream()
                  .filter(id -> !id.equals(target.getUniqueId()))
                  .filter(id -> this.sharesByMember.get(id) == share)
                  .<Player>map(Bukkit::getPlayer)
                  .filter(Objects::nonNull)
                  .filter(OfflinePlayer::isOnline)
                  .filter(player -> !player.isDead())
                  .filter(player -> player.getWorld().equals(target.getWorld()))
                  .filter(player -> player.getLocation().distanceSquared(target.getLocation()) <= 400.0)
                  .toList();
               if (!recipients.isEmpty()) {
                  double shared = damage * share.fraction();
                  damage -= shared;
                  double each = shared / recipients.size();

                  for (Player recipient : recipients) {
                     this.dealSharedDamage(recipient, event.getEliteMobEntity().getLivingEntity(), each);
                  }

                  this.semantics
                     .observe(
                        new AbilityRuntimeObservation(
                           AbilityRuntimeObservation.Kind.DAMAGE_SHARED,
                           share.sourceId(),
                           target.getUniqueId(),
                           share.abilityId(),
                           shared,
                           0,
                           recipients.size(),
                           Set.of(),
                           Set.of(AbilityMechanic.DAMAGE_SHARE)
                        )
                     );
               }
            }

            AbilityStateRuntime.TriggeredBarrier barrier = this.liveBarrier(target.getUniqueId(), now);
            if (barrier != null) {
               double ownedRemaining = Math.min(barrier.ownedAbsorption(), Math.max(0.0, target.getAbsorptionAmount() - barrier.baselineAbsorption()));
               if (AbilityStateMath.breaksOwnedBarrier(damage, ownedRemaining)) {
                  this.triggeredBarriers.remove(target.getUniqueId());
                  this.observeStateConsumed(barrier.sourceId(), target.getUniqueId(), barrier.abilityId(), ownedRemaining, AbilityMechanic.BARRIER_BREAK_HEAL);
                  UUID targetId = target.getUniqueId();
                  long sourceGeneration = this.lifecycleGeneration(barrier.sourceId());
                  long targetGeneration = this.lifecycleGeneration(targetId);
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           if (this.delayedAllyStillValid(barrier.sourceId(), sourceGeneration, target, targetId, targetGeneration, 24.0)) {
                              double healed = heal(target, maximumHealth(target) * barrier.healingFraction());
                              Player sourcex = Bukkit.getPlayer(barrier.sourceId());
                              if (sourcex != null && healed > 0.0) {
                                 this.semantics.recordContribution(sourcex, barrier.abilityId(), new AbilityContribution(0.0, healed, 0.0, 0.0, 0, 1, 0.0));
                                 this.observeEffect(
                                    AbilityRuntimeObservation.Kind.HEAL, barrier.sourceId(), targetId, barrier.abilityId(), healed, 0, AbilityEffect.HEAL
                                 );
                              }
                           }
                        }
                     );
               }
            }

            AbilityStateRuntime.TriggeredProtection protection = this.liveProtection(target.getUniqueId(), now);
            if (protection != null && AbilityStateMath.isMajorThreat(damage, target.getHealth(), maximumHealth(target))) {
               this.triggeredProtections.remove(target.getUniqueId());
               this.observeStateConsumed(
                  protection.sourceId(),
                  target.getUniqueId(),
                  protection.abilityId(),
                  protection.protectionAmount(),
                  AbilityMechanic.THREAT_TRIGGERED_PROTECTION
               );
               double prevented = Math.min(damage, protection.protectionAmount());
               damage -= prevented;
               Player source = Bukkit.getPlayer(protection.sourceId());
               if (source != null && prevented > 0.0) {
                  this.semantics.recordContribution(source, protection.abilityId(), new AbilityContribution(0.0, 0.0, prevented, 0.0, 0, 1, 0.0));
               }
            }

            AbilityStateRuntime.DeathGuard guard = this.liveGuard(target.getUniqueId(), now);
            if (guard != null && damage >= target.getHealth() + target.getAbsorptionAmount() - 0.01) {
               this.deathGuards.remove(target.getUniqueId());
               this.observeStateConsumed(guard.sourceId(), target.getUniqueId(), guard.abilityId(), damage, AbilityMechanic.DEATH_GUARD);
               if (guard.emitWardBroken()) {
                  this.semantics.signal(target, AbilityRuntimeSignal.WARD_BROKEN, guard.wardBrokenTicks());
               }

               double survivableDamage = AbilityStateMath.deathGuardDamageCeiling(target.getHealth(), target.getAbsorptionAmount());
               double prevented = Math.max(0.0, damage - survivableDamage);
               damage = Math.min(damage, survivableDamage);
               UUID targetId = target.getUniqueId();
               long sourceGeneration = this.lifecycleGeneration(guard.sourceId());
               long targetGeneration = this.lifecycleGeneration(targetId);
               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        if (this.delayedAllyStillValid(guard.sourceId(), sourceGeneration, target, targetId, targetGeneration, 24.0)) {
                           double healed = heal(target, maximumHealth(target) * guard.healingFraction());
                           double shield = maximumHealth(target) * guard.shieldFraction();
                           applyAbsorption(target, shield, 80);
                           Player sourcex = Bukkit.getPlayer(guard.sourceId());
                           if (sourcex != null) {
                              this.semantics.recordContribution(sourcex, guard.abilityId(), new AbilityContribution(0.0, healed, prevented, 0.0, 0, 1, 0.0));
                              if (healed > 0.0) {
                                 this.observeEffect(
                                    AbilityRuntimeObservation.Kind.HEAL, guard.sourceId(), targetId, guard.abilityId(), healed, 0, AbilityEffect.HEAL
                                 );
                              }

                              if (shield > 0.0) {
                                 this.observeEffect(
                                    AbilityRuntimeObservation.Kind.SHIELD, guard.sourceId(), targetId, guard.abilityId(), shield, 80, AbilityEffect.SHIELD
                                 );
                              }
                           }
                        }
                     }
                  );
            }

            event.setDamage(Math.max(0.0, damage));
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         if (!this.closed && !(event.getDamage() <= 0.0)) {
            Player player = event.getPlayer();
            long now = System.nanoTime();
            AbilityStateRuntime.TimedSource window = live(this.lifestealWindows, player.getUniqueId(), now);
            if (window != null) {
               double multiplier = this.semantics.healingMultiplier(player)
                  * AbilityHealingPolicy.multiplier(this.semantics.mechanicModifiers(player), AbilityHealingType.BURST, 1);
               double healed = heal(player, event.getDamage() * 0.16 * multiplier);
               if (healed > 0.0) {
                  this.semantics.recordContribution(player, window.abilityId(), new AbilityContribution(0.0, healed, 0.0, 0.0, 0, 1, 0.0));
                  this.observeEffect(
                     AbilityRuntimeObservation.Kind.HEAL, player.getUniqueId(), player.getUniqueId(), window.abilityId(), healed, 0, AbilityEffect.HEAL
                  );
               }
            }

            AbilityStateRuntime.DetonatingMark mark = this.detonatingMarks.get(event.getEntity().getUniqueId());
            if (mark != null && mark.expiresAtNanos() > now && mark.sourceId().equals(player.getUniqueId())) {
               this.detonatingMarks.remove(event.getEntity().getUniqueId());
               this.observeStateConsumed(mark.sourceId(), event.getEntity().getUniqueId(), mark.abilityId(), event.getDamage(), AbilityMechanic.DETONATING_MARK);
               Location impact = event.getEntity().getLocation().clone();
               double explosionDamage = Math.max(1.0, event.getDamage() * 0.55);
               FixedAbilitySpec detonationSpec = this.registry.require(mark.abilityId());
               UUID sourceId = player.getUniqueId();
               long generation = this.lifecycleGeneration(sourceId);
               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        if (!this.closed && player.isOnline() && impact.getWorld() != null && this.isCurrentLifecycleGeneration(sourceId, generation)) {
                           impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 3, 1.0, 0.35, 1.0, 0.03);
                           impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 0.8F, 1.35F);

                           for (Entity nearby : impact.getWorld().getNearbyEntities(impact, 4.0, 4.0, 4.0)) {
                              if (nearby instanceof LivingEntity living
                                 && this.semantics.canApplyEnemyEffect(player, living, detonationSpec, AbilityEffect.DAMAGE)) {
                                 double dealt = this.damage
                                    .apply(player, null, living, explosionDamage, CombatDamageContext.ClassAbilityDamageDomain.AREA_BLAST);
                                 if (dealt > 0.0) {
                                    this.observeEffect(
                                       AbilityRuntimeObservation.Kind.DAMAGE, sourceId, living.getUniqueId(), mark.abilityId(), dealt, 0, AbilityEffect.DAMAGE
                                    );
                                 }
                              }
                           }
                        }
                     }
                  );
            } else {
               if (mark != null && mark.expiresAtNanos() <= now) {
                  this.detonatingMarks.remove(event.getEntity().getUniqueId());
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPotionEffect(EntityPotionEffectEvent event) {
      if (!this.closed && event.getEntity() instanceof Player player && event.getNewEffect() != null) {
         PotionEffectType type = event.getNewEffect().getType();
         long now = System.nanoTime();
         AbilityStateRuntime.TimedSource immunity = null;
         AbilityMechanic mechanic = null;
         if (CONTROL_EFFECTS.contains(type)) {
            immunity = live(this.controlImmunity, player.getUniqueId(), now);
            mechanic = AbilityMechanic.CONTROL_IMMUNITY;
         }

         if (immunity == null && DEBUFF_EFFECTS.contains(type)) {
            immunity = live(this.debuffImmunity, player.getUniqueId(), now);
            mechanic = AbilityMechanic.DEBUFF_IMMUNITY;
         }

         if (immunity != null) {
            event.setCancelled(true);
            this.semantics
               .observe(
                  new AbilityRuntimeObservation(
                     AbilityRuntimeObservation.Kind.STATUS_BLOCKED,
                     immunity.sourceId(),
                     player.getUniqueId(),
                     immunity.abilityId(),
                     1.0,
                     0,
                     1,
                     Set.of(),
                     Set.of(mechanic)
                  )
               );
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onVelocity(PlayerVelocityEvent event) {
      if (!this.closed) {
         if (!this.ownedVelocities.contains(event.getPlayer().getUniqueId())) {
            AbilityStateRuntime.TimedSource immunity = live(this.controlImmunity, event.getPlayer().getUniqueId(), System.nanoTime());
            if (immunity != null) {
               event.setCancelled(true);
               this.semantics
                  .observe(
                     new AbilityRuntimeObservation(
                        AbilityRuntimeObservation.Kind.STATUS_BLOCKED,
                        immunity.sourceId(),
                        event.getPlayer().getUniqueId(),
                        immunity.abilityId(),
                        1.0,
                        0,
                        1,
                        Set.of(),
                        Set.of(AbilityMechanic.CONTROL_IMMUNITY)
                     )
                  );
            }
         }
      }
   }

   void applyOwnedVelocity(Player player, Vector velocity) {
      UUID playerId = player.getUniqueId();
      this.ownedVelocities.add(playerId);

      try {
         player.setVelocity(velocity);
      } finally {
         this.ownedVelocities.remove(playerId);
      }
   }

   private AbilityStateOwnership buildStateOwnership() {
      return AbilityStateOwnership.builder()
         .bind(AbilityStateOwnership.Bucket.REDIRECTS, sourceId -> this.redirectsByTarget.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(link -> link.sourceId().equals(sourceId));
            return entry.getKey().equals(sourceId) || entry.getValue().isEmpty();
         }), this.redirectsByTarget::clear)
         .bind(
            AbilityStateOwnership.Bucket.DEATH_GUARDS,
            sourceId -> this.deathGuards.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.deathGuards::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.TRIGGERED_BARRIERS,
            sourceId -> this.triggeredBarriers.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.triggeredBarriers::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.TRIGGERED_PROTECTIONS,
            sourceId -> this.triggeredProtections.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.triggeredProtections::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.DAMAGE_SHARES,
            sourceId -> this.sharesByMember
               .entrySet()
               .removeIf(entry -> entry.getValue().members().contains(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.sharesByMember::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.LIFESTEAL_WINDOWS,
            sourceId -> this.lifestealWindows.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.lifestealWindows::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.CONTROL_IMMUNITY,
            sourceId -> this.controlImmunity.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.controlImmunity::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.DEBUFF_IMMUNITY,
            sourceId -> this.debuffImmunity.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.debuffImmunity::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.PLANTED_GUARDS,
            sourceId -> this.plantedGuards.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.plantedGuards::clear
         )
         .bind(
            AbilityStateOwnership.Bucket.DETONATING_MARKS,
            sourceId -> this.detonatingMarks.entrySet().removeIf(entry -> entry.getKey().equals(sourceId) || entry.getValue().sourceId().equals(sourceId)),
            this.detonatingMarks::clear
         )
         .bind(AbilityStateOwnership.Bucket.RETALIATION, this.storedRetaliation::remove, this.storedRetaliation::clear)
         .bind(AbilityStateOwnership.Bucket.RECENT_ATTACKERS, sourceId -> clearStatusMap(this.recentAttackers, sourceId), this.recentAttackers::clear)
         .bind(AbilityStateOwnership.Bucket.TAUNTS, sourceId -> clearStatusMap(this.tauntsByCaster, sourceId), this.tauntsByCaster::clear)
         .bind(AbilityStateOwnership.Bucket.DEFENSE_BREAKS, sourceId -> clearStatusMap(this.defenseBreaksByCaster, sourceId), this.defenseBreaksByCaster::clear)
         .bind(AbilityStateOwnership.Bucket.ACTIVE_FIELDS, this.activeFields::remove, this.activeFields::clear)
         .bind(AbilityStateOwnership.Bucket.ACTIVE_WIND_UPS, this.activeWindUps::remove, this.activeWindUps::clear)
         .bind(AbilityStateOwnership.Bucket.PENDING_HEAL_ECHOES, this.pendingHealEchoes::remove, this.pendingHealEchoes::clear)
         .bind(
            AbilityStateOwnership.Bucket.LIFECYCLE_GENERATIONS,
            sourceId -> this.lifecycleGenerations.compute(sourceId, (ignored, current) -> current != null && current != Long.MAX_VALUE ? current + 1L : 1L),
            this.lifecycleGenerations::clear
         )
         .bind(AbilityStateOwnership.Bucket.REDISTRIBUTION_GUARDS, this.redistributing::remove, this.redistributing::clear)
         .bind(AbilityStateOwnership.Bucket.OWNED_VELOCITIES, this.ownedVelocities::remove, this.ownedVelocities::clear)
         .build();
   }

   private static void clearStatusMap(Map<UUID, Map<UUID, Long>> statuses, UUID sourceId) {
      statuses.remove(sourceId);
      statuses.values().forEach(targets -> targets.remove(sourceId));
      statuses.entrySet().removeIf(entry -> entry.getValue().isEmpty());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onTeleport(PlayerTeleportEvent event) {
      if (!this.closed && event.getTo() != null) {
         this.plantedGuards.remove(event.getPlayer().getUniqueId());
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onMove(PlayerMoveEvent event) {
      if (!this.closed && event.getTo() != null) {
         Player player = event.getPlayer();
         AbilityStateRuntime.PlantedGuard guard = this.livePlanted(player, System.nanoTime());
         if (guard != null) {
            if (event.getFrom().getX() != event.getTo().getX()
               || event.getFrom().getY() != event.getTo().getY()
               || event.getFrom().getZ() != event.getTo().getZ()) {
               Location rooted = event.getFrom().clone();
               rooted.setYaw(event.getTo().getYaw());
               rooted.setPitch(event.getTo().getPitch());
               event.setTo(rooted);
            }
         }
      }
   }

   void clearSource(UUID sourceId) {
      this.semantics
         .observe(new AbilityRuntimeObservation(AbilityRuntimeObservation.Kind.LIFECYCLE_CLEARED, sourceId, null, "lifecycle", 0.0, 0, 1, Set.of(), Set.of()));
      this.stateOwnership.clearSource(sourceId);
   }

   private void observeStateArmed(Player source, Player target, FixedAbilitySpec spec, AbilityMechanic mechanic, double amount, int durationTicks) {
      this.observeStateArmed(source.getUniqueId(), target.getUniqueId(), spec, mechanic, amount, durationTicks);
   }

   private void observeStateArmed(UUID sourceId, UUID targetId, FixedAbilitySpec spec, AbilityMechanic mechanic, double amount, int durationTicks) {
      this.semantics
         .observe(
            AbilityRuntimeObservation.state(
               AbilityRuntimeObservation.Kind.STATE_ARMED, sourceId, targetId, spec.id(), Math.max(0.0, amount), Math.max(1, durationTicks), mechanic
            )
         );
   }

   private void observeStateConsumed(UUID sourceId, UUID targetId, String abilityId, double amount, AbilityMechanic mechanic) {
      this.semantics
         .observe(
            AbilityRuntimeObservation.state(AbilityRuntimeObservation.Kind.STATE_CONSUMED, sourceId, targetId, abilityId, Math.max(0.0, amount), 0, mechanic)
         );
   }

   private void observeEffect(
      AbilityRuntimeObservation.Kind kind, UUID sourceId, UUID targetId, String abilityId, double amount, int durationTicks, AbilityEffect effect
   ) {
      this.semantics.observe(AbilityRuntimeObservation.effect(kind, sourceId, targetId, abilityId, Math.max(0.0, amount), Math.max(0, durationTicks), effect));
   }

   private void scheduleTether(Player caster, FixedAbilitySpec spec, AbilityTargeting.TargetSelection selection, int effectiveLevel) {
      if (!selection.allies().isEmpty()) {
         Player target = selection.allies().get(0);
         AbilityMechanicModifiers modifiers = this.semantics.mechanicModifiers(caster);
         int durationTicks = ActiveAbilityLevelScaling.durationTicks(
            AbilityHealingPolicy.durationTicks(spec.tuning().durationTicks(), AbilityHealingType.PERIODIC, modifiers), effectiveLevel
         );
         int pulses = Math.max(3, Math.min(8, (int)Math.ceil(durationTicks / 20.0)));
         double levelScale = AbilityLevelScaling.multiplier(effectiveLevel);
         double healingMultiplier = this.semantics.healingMultiplier(caster)
            * AbilityHealingPolicy.multiplier(modifiers, AbilityHealingType.PERIODIC, selection.enemies().size());
         UUID sourceId = caster.getUniqueId();
         UUID targetId = target.getUniqueId();
         long sourceGeneration = this.lifecycleGeneration(sourceId);
         long targetGeneration = this.lifecycleGeneration(targetId);

         for (int pulse = 1; pulse <= pulses; pulse++) {
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (this.delayedAllyStillValid(sourceId, sourceGeneration, target, targetId, targetGeneration, 24.0)) {
                  double healed = heal(target, maximumHealth(target) * spec.tuning().healingFraction() * 0.28 * levelScale * healingMultiplier);
                  if (healed > 0.0) {
                     this.semantics.recordContribution(caster, spec.id(), new AbilityContribution(0.0, healed, 0.0, 0.0, 0, 1, 0.0));
                     this.observeEffect(AbilityRuntimeObservation.Kind.HEAL, sourceId, targetId, spec.id(), healed, 0, AbilityEffect.HEAL);
                     this.scheduleHealEcho(caster, target, healed, spec);
                  }
               }
            }, pulse * 20L);
         }
      }
   }

   private AbilityStateRuntime.RedirectLink strongestRedirect(UUID targetId, long now) {
      List<AbilityStateRuntime.RedirectLink> links = this.redirectsByTarget.get(targetId);
      if (links == null) {
         return null;
      } else {
         links.removeIf(link -> link.expiresAtNanos() <= now);
         if (links.isEmpty()) {
            this.redirectsByTarget.remove(targetId);
            return null;
         } else {
            return links.stream().max(Comparator.comparingDouble(AbilityStateRuntime.RedirectLink::fraction)).orElse(null);
         }
      }
   }

   private AbilityStateRuntime.DeathGuard liveGuard(UUID targetId, long now) {
      AbilityStateRuntime.DeathGuard guard = this.deathGuards.get(targetId);
      if (guard != null && guard.expiresAtNanos() <= now) {
         this.deathGuards.remove(targetId);
         return null;
      } else {
         return guard;
      }
   }

   private AbilityStateRuntime.TriggeredBarrier liveBarrier(UUID targetId, long now) {
      AbilityStateRuntime.TriggeredBarrier barrier = this.triggeredBarriers.get(targetId);
      if (barrier != null && barrier.expiresAtNanos() <= now) {
         this.triggeredBarriers.remove(targetId);
         return null;
      } else {
         return barrier;
      }
   }

   private AbilityStateRuntime.TriggeredProtection liveProtection(UUID targetId, long now) {
      AbilityStateRuntime.TriggeredProtection protection = this.triggeredProtections.get(targetId);
      if (protection != null && protection.expiresAtNanos() <= now) {
         this.triggeredProtections.remove(targetId);
         return null;
      } else {
         return protection;
      }
   }

   private AbilityStateRuntime.ShareLink liveShare(UUID memberId, long now) {
      AbilityStateRuntime.ShareLink link = this.sharesByMember.get(memberId);
      if (link == null) {
         return null;
      } else if (link.expiresAtNanos() <= now) {
         link.members().forEach(id -> this.sharesByMember.remove(id, link));
         return null;
      } else {
         return link;
      }
   }

   private AbilityStateRuntime.PlantedGuard livePlanted(Player player, long now) {
      AbilityStateRuntime.PlantedGuard guard = this.plantedGuards.get(player.getUniqueId());
      if (guard == null) {
         return null;
      } else if (guard.expiresAtNanos() > now
         && guard.origin().getWorld() != null
         && guard.origin().getWorld().equals(player.getWorld())
         && !(guard.origin().distanceSquared(player.getLocation()) > 2.25)) {
         return guard;
      } else {
         this.plantedGuards.remove(player.getUniqueId());
         return null;
      }
   }

   private List<LivingEntity> recentAttackers(Player target, long now) {
      Map<UUID, Long> attackers = this.recentAttackers.get(target.getUniqueId());
      if (attackers == null) {
         return List.of();
      } else {
         attackers.entrySet().removeIf(entry -> entry.getValue() <= now);
         if (attackers.isEmpty()) {
            this.recentAttackers.remove(target.getUniqueId());
            return List.of();
         } else {
            return attackers.keySet()
               .stream()
               .<Entity>map(Bukkit::getEntity)
               .filter(LivingEntity.class::isInstance)
               .map(LivingEntity.class::cast)
               .filter(AbilityStateRuntime::valid)
               .toList();
         }
      }
   }

   private static boolean withinAbilityReach(LivingEntity enemy, Location origin, FixedAbilitySpec spec) {
      if (origin.getWorld() != null && origin.getWorld().equals(enemy.getWorld())) {
         double reach = Math.max(1.0, Math.max(spec.tuning().range(), spec.tuning().radius()));
         return enemy.getLocation().distanceSquared(origin) <= reach * reach;
      } else {
         return false;
      }
   }

   private static boolean hasLiveStatus(Map<UUID, Map<UUID, Long>> statuses, UUID sourceId, UUID targetId, long now) {
      Map<UUID, Long> targets = statuses.get(sourceId);
      if (targets == null) {
         return false;
      } else {
         Long expiresAt = targets.get(targetId);
         if (expiresAt == null) {
            return false;
         } else if (expiresAt > now) {
            return true;
         } else {
            targets.remove(targetId);
            if (targets.isEmpty()) {
               statuses.remove(sourceId);
            }

            return false;
         }
      }
   }

   private static Set<UUID> registerStatuses(
      Map<UUID, Map<UUID, Long>> statuses, Player caster, Collection<? extends LivingEntity> targets, int durationTicks, boolean extendExisting
   ) {
      long now = System.nanoTime();
      long duration = Math.max(1, durationTicks) * 50000000L;
      Map<UUID, Long> byTarget = statuses.computeIfAbsent(caster.getUniqueId(), ignored -> new HashMap<>());
      return TimedStatusLeasePolicy.apply(byTarget, targets.stream().map(Entity::getUniqueId).toList(), now, duration, extendExisting);
   }

   private static AbilityStateRuntime.TimedSource live(Map<UUID, AbilityStateRuntime.TimedSource> states, UUID playerId, long now) {
      AbilityStateRuntime.TimedSource state = states.get(playerId);
      if (state != null && state.expiresAtNanos() <= now) {
         states.remove(playerId);
         return null;
      } else {
         return state;
      }
   }

   private void dealSharedDamage(Player target, LivingEntity attacker, double amount) {
      if (target != null && target.isOnline() && !target.isDead() && !(amount <= 0.0)) {
         UUID targetId = target.getUniqueId();
         if (this.redistributing.add(targetId)) {
            try {
               CombatDamageContext.runEliteToPlayerBypass(() -> target.damage(amount, attacker));
            } finally {
               this.redistributing.remove(targetId);
            }
         }
      }
   }

   private long lifecycleGeneration(UUID playerId) {
      return this.lifecycleGenerations.getOrDefault(playerId, 0L);
   }

   private boolean isCurrentLifecycleGeneration(UUID playerId, long generation) {
      return this.lifecycleGeneration(playerId) == generation;
   }

   long lifecycleToken(Player player) {
      return this.lifecycleGeneration(player.getUniqueId());
   }

   boolean isCurrentLifecycle(Player player, long token) {
      return this.isCurrentLifecycleGeneration(player.getUniqueId(), token);
   }

   private boolean delayedAllyStillValid(UUID sourceId, long sourceGeneration, Player target, UUID targetId, long targetGeneration, double maximumDistance) {
      if (!this.closed && this.isCurrentLifecycleGeneration(sourceId, sourceGeneration) && this.isCurrentLifecycleGeneration(targetId, targetGeneration)) {
         Player source = Bukkit.getPlayer(sourceId);
         return source != null
            && source.isOnline()
            && source.isValid()
            && !source.isDead()
            && ClassAbilityEligibility.isEligible(source)
            && target.isOnline()
            && target.isValid()
            && !target.isDead()
            && ClassAbilityEligibility.isEligible(target)
            && source.getWorld().equals(target.getWorld())
            && source.getLocation().distanceSquared(target.getLocation()) <= maximumDistance * maximumDistance;
      } else {
         return false;
      }
   }

   private static boolean validLinkedPlayers(Player source, Player target, double maximumDistance) {
      return source != null
         && source.isOnline()
         && !source.isDead()
         && source.getWorld().equals(target.getWorld())
         && source.getLocation().distanceSquared(target.getLocation()) <= maximumDistance * maximumDistance;
   }

   private static double healthFraction(Player player) {
      return player.getHealth() / maximumHealth(player);
   }

   private static boolean valid(LivingEntity entity) {
      return entity != null && entity.isValid() && !entity.isDead() && entity.getHealth() > 0.0;
   }

   private static double heal(Player target, double amount) {
      if (target.isOnline() && !target.isDead() && Double.isFinite(amount) && !(amount <= 0.0)) {
         double before = target.getHealth();
         double bounded = Math.min(Math.max(0.0, maximumHealth(target) - before), amount);
         if (bounded <= 0.0) {
            return 0.0;
         } else {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, bounded, RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && Double.isFinite(event.getAmount()) && !(event.getAmount() <= 0.0)) {
               target.setHealth(Math.min(maximumHealth(target), before + event.getAmount()));
               return Math.max(0.0, target.getHealth() - before);
            } else {
               return 0.0;
            }
         }
      } else {
         return 0.0;
      }
   }

   private static void applyAbsorption(Player target, double amount, int durationTicks) {
      if (Double.isFinite(amount) && !(amount <= 0.0)) {
         int amplifier = Math.max(0, Math.min(255, (int)Math.ceil(amount / 4.0) - 1));
         target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Math.max(20, durationTicks), amplifier, false, true, true), true);
      }
   }

   private static double maximumHealth(LivingEntity entity) {
      AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
      return attribute == null ? Math.max(1.0, entity.getHealth()) : Math.max(1.0, attribute.getValue());
   }

   private static long expiresAt(int ticks) {
      long now = System.nanoTime();
      long duration = Math.max(1, ticks) * 50000000L;
      return Long.MAX_VALUE - now < duration ? Long.MAX_VALUE : now + duration;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         HandlerList.unregisterAll(this);
         this.stateOwnership.clearAll();
      }
   }

   private record DeathGuard(
      UUID sourceId, double healingFraction, double shieldFraction, boolean emitWardBroken, int wardBrokenTicks, long expiresAtNanos, String abilityId
   ) {
   }

   private record DetonatingMark(UUID sourceId, long expiresAtNanos, String abilityId) {
   }

   private record PendingHealEcho(long expiresAtNanos, String abilityId, double echoFraction) {
      private PendingHealEcho(long expiresAtNanos, String abilityId, double echoFraction) {
         if (Double.isFinite(echoFraction) && !(echoFraction <= 0.0)) {
            this.expiresAtNanos = expiresAtNanos;
            this.abilityId = abilityId;
            this.echoFraction = echoFraction;
         } else {
            throw new IllegalArgumentException("echoFraction must be positive");
         }
      }
   }

   private record PlantedGuard(UUID sourceId, Location origin, long expiresAtNanos, String abilityId) {
      private PlantedGuard(UUID sourceId, Location origin, long expiresAtNanos, String abilityId) {
         origin = origin.clone();
         this.sourceId = sourceId;
         this.origin = origin;
         this.expiresAtNanos = expiresAtNanos;
         this.abilityId = abilityId;
      }
   }

   private record RedirectLink(
      UUID sourceId, double fraction, double efficiency, double redirectedDamageMultiplier, long expiresAtNanos, String abilityId, AbilityMechanic mechanic
   ) {
      boolean equivalent(AbilityStateRuntime.RedirectLink other) {
         return this.sourceId.equals(other.sourceId)
            && this.fraction == other.fraction
            && this.efficiency == other.efficiency
            && this.redirectedDamageMultiplier == other.redirectedDamageMultiplier
            && this.abilityId.equals(other.abilityId)
            && this.mechanic == other.mechanic;
      }
   }

   private record ShareLink(UUID sourceId, List<UUID> members, double fraction, long expiresAtNanos, String abilityId) {
      private ShareLink(UUID sourceId, List<UUID> members, double fraction, long expiresAtNanos, String abilityId) {
         members = List.copyOf(members);
         this.sourceId = sourceId;
         this.members = members;
         this.fraction = fraction;
         this.expiresAtNanos = expiresAtNanos;
         this.abilityId = abilityId;
      }
   }

   private record TimedSource(UUID sourceId, long expiresAtNanos, String abilityId) {
   }

   private record TriggeredBarrier(
      UUID sourceId, double healingFraction, double baselineAbsorption, double ownedAbsorption, long expiresAtNanos, String abilityId
   ) {
   }

   private record TriggeredProtection(UUID sourceId, double protectionAmount, long expiresAtNanos, String abilityId) {
   }
}
