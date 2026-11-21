package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.ClassAbilityEligibility;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

final class TimedCombatModifiers implements Listener, AutoCloseable {
   private final AbilitySemantics semantics;
   private final Map<UUID, List<TimedCombatModifiers.EnemyMark>> enemyMarks = new HashMap<>();
   private final Map<UUID, List<TimedCombatModifiers.PlayerModifier>> outgoingModifiers = new HashMap<>();
   private final Map<UUID, List<TimedCombatModifiers.PlayerModifier>> incomingModifiers = new HashMap<>();
   private final Map<UUID, List<TimedCombatModifiers.PlayerModifier>> incomingPenalties = new HashMap<>();
   private final Map<PlayerDamagedByEliteMobEvent, List<TimedCombatModifiers.PendingReduction>> pendingReductions = new IdentityHashMap<>();
   private final TimedOutgoingDamageReduction outgoingReductions = new TimedOutgoingDamageReduction(System::nanoTime);
   private boolean closed;

   TimedCombatModifiers(Plugin plugin, AbilitySemantics semantics) {
      this.semantics = Objects.requireNonNull(semantics, "semantics");
      Bukkit.getPluginManager().registerEvents(this, Objects.requireNonNull(plugin, "plugin"));
   }

   void markEnemy(Player source, Entity enemy, Set<UUID> beneficiaries, double multiplier, int durationTicks, String abilityId) {
      this.markEnemy(source, enemy, beneficiaries, multiplier, durationTicks, abilityId, false);
   }

   void markEnemy(Player source, Entity enemy, Set<UUID> beneficiaries, double multiplier, int durationTicks, String abilityId, boolean healthScaled) {
      if (!this.closed && !(multiplier <= 1.0) && durationTicks > 0) {
         List<TimedCombatModifiers.EnemyMark> marks = this.enemyMarks.computeIfAbsent(enemy.getUniqueId(), ignored -> new ArrayList<>());
         TimedCombatModifiers.EnemyMark next = new TimedCombatModifiers.EnemyMark(
            source.getUniqueId(), Set.copyOf(beneficiaries), multiplier, expiresAt(durationTicks), abilityId, healthScaled
         );
         long now = System.nanoTime();
         marks.removeIf(mark -> mark.expiresAtNanos() <= now || mark.equivalent(next) && mark.expiresAtNanos() <= next.expiresAtNanos());
         if (marks.stream().noneMatch(mark -> mark.equivalent(next))) {
            marks.add(next);
         }
      }
   }

   void modifyOutgoing(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
      this.modifyOutgoing(source, target, multiplier, durationTicks, abilityId, TimedCombatModifiers.ModifierDomain.ANY);
   }

   void modifySpellOutgoing(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
      this.modifyOutgoing(source, target, multiplier, durationTicks, abilityId, TimedCombatModifiers.ModifierDomain.SPELL);
   }

   private void modifyOutgoing(Player source, Player target, double multiplier, int durationTicks, String abilityId, TimedCombatModifiers.ModifierDomain domain) {
      if (!this.closed && !(multiplier <= 1.0) && durationTicks > 0) {
         refreshModifier(
            this.outgoingModifiers,
            target.getUniqueId(),
            new TimedCombatModifiers.PlayerModifier(source.getUniqueId(), multiplier, expiresAt(durationTicks), abilityId, domain)
         );
      }
   }

   void modifyIncoming(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
      if (!this.closed && !(multiplier >= 1.0) && !(multiplier < 0.0) && durationTicks > 0) {
         refreshModifier(
            this.incomingModifiers,
            target.getUniqueId(),
            new TimedCombatModifiers.PlayerModifier(
               source.getUniqueId(), multiplier, expiresAt(durationTicks), abilityId, TimedCombatModifiers.ModifierDomain.ANY
            )
         );
      }
   }

   void modifyIncomingPenalty(Player source, Player target, double multiplier, int durationTicks, String abilityId) {
      if (!this.closed && !(multiplier <= 1.0) && durationTicks > 0) {
         refreshModifier(
            this.incomingPenalties,
            target.getUniqueId(),
            new TimedCombatModifiers.PlayerModifier(
               source.getUniqueId(), multiplier, expiresAt(durationTicks), abilityId, TimedCombatModifiers.ModifierDomain.ANY
            )
         );
      }
   }

   private static void refreshModifier(Map<UUID, List<TimedCombatModifiers.PlayerModifier>> byTarget, UUID targetId, TimedCombatModifiers.PlayerModifier next) {
      List<TimedCombatModifiers.PlayerModifier> modifiers = byTarget.computeIfAbsent(targetId, ignored -> new ArrayList<>());
      long now = System.nanoTime();
      modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now || modifier.equivalent(next) && modifier.expiresAtNanos() <= next.expiresAtNanos());
      if (modifiers.stream().noneMatch(modifier -> modifier.equivalent(next))) {
         modifiers.add(next);
      }
   }

   double weakenOutgoing(
      Player source, Entity target, double authoredMultiplier, int effectiveLevel, double potencyMultiplier, int durationTicks, String abilityId
   ) {
      return this.closed
         ? 1.0
         : this.outgoingReductions
            .register(source.getUniqueId(), target.getUniqueId(), abilityId, authoredMultiplier, effectiveLevel, potencyMultiplier, durationTicks);
   }

   void clearSource(UUID sourceId) {
      Objects.requireNonNull(sourceId, "sourceId");
      if (!this.closed) {
         this.enemyMarks.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(mark -> mark.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
         });
         this.outgoingModifiers.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
         });
         this.incomingModifiers.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
         });
         this.incomingPenalties.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(modifier -> modifier.sourceId().equals(sourceId));
            return entry.getValue().isEmpty();
         });
         this.outgoingReductions.clearSource(sourceId);
         this.pendingReductions.values().removeIf(reductions -> {
            reductions.removeIf(reduction -> reduction.attribution().sourceId().equals(sourceId));
            return reductions.isEmpty();
         });
      }
   }

   void clearTarget(UUID targetId) {
      Objects.requireNonNull(targetId, "targetId");
      if (!this.closed) {
         this.outgoingModifiers.remove(targetId);
         this.incomingModifiers.remove(targetId);
         this.incomingPenalties.remove(targetId);
         this.outgoingReductions.clearTarget(targetId);
         this.pendingReductions.values().removeIf(reductions -> {
            reductions.removeIf(reduction -> reduction.attribution().targetId().equals(targetId));
            return reductions.isEmpty();
         });
         this.enemyMarks.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(mark -> mark.beneficiaries().contains(targetId));
            return entry.getValue().isEmpty();
         });
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerDamagesElite(EliteMobDamagedByPlayerEvent event) {
      if (!CombatDamageContext.isDamageTransferActive()) {
         if (!this.closed && !(event.getDamage() <= 0.0)) {
            Player attacker = event.getPlayer();
            if (attacker != null && ClassAbilityEligibility.isEligible(attacker)) {
               long now = System.nanoTime();
               TimedCombatModifiers.PlayerModifier outgoing = this.strongestOutgoing(
                  attacker.getUniqueId(),
                  now,
                  CombatDamageContext.currentPlayerToEliteSource().map(CombatDamageContext.PlayerDamageSource::progressionSkill).orElse(null),
                  CombatDamageContext.currentClassAbilityDamageDomain().orElse(null)
               );
               if (outgoing != null) {
                  event.applyClassAbilityDamageMultiplier(outgoing.abilityId(), outgoing.multiplier());
               }

               TimedCombatModifiers.EnemyMark mark = this.strongestMark(event.getEntity().getUniqueId(), attacker.getUniqueId(), now);
               if (mark != null) {
                  double multiplier = mark.multiplier(healthFraction(event.getEliteMobEntity().getLivingEntity()));
                  event.applyClassAbilityDamageMultiplier(mark.abilityId(), multiplier);
                  this.semantics
                     .observe(
                        AbilityRuntimeObservation.effect(
                           AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED,
                           mark.sourceId(),
                           event.getEntity().getUniqueId(),
                           mark.abilityId(),
                           multiplier,
                           0,
                           AbilityEffect.PARTY_DAMAGE_MARK
                        )
                     );
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onEliteDamagesPlayer(PlayerDamagedByEliteMobEvent event) {
      if (!this.closed && !(event.getDamage() <= 0.0)) {
         Player player = event.getPlayer();
         if (player != null) {
            boolean recipientEligible = ClassAbilityEligibility.isEligible(player);
            if (recipientEligible) {
               double originalDamage = event.getDamage();
               Entity attacker = event.getAttacker();
               TimedOutgoingDamageReduction.Application weakening = attacker == null
                  ? null
                  : this.outgoingReductions
                     .apply(attacker.getUniqueId(), originalDamage, recipientEligible, sourceId -> liveSourceNear(sourceId, attacker))
                     .orElse(null);
               double weakenedDamage = weakening == null ? originalDamage : weakening.modifiedDamage();
               TimedCombatModifiers.PlayerModifier protection = this.strongestProtection(player.getUniqueId(), System.nanoTime());
               TimedCombatModifiers.PlayerModifier penalty = this.strongestPenalty(player.getUniqueId(), System.nanoTime());
               if (weakening != null || protection != null || penalty != null) {
                  double protectedDamage = Math.max(0.0, weakenedDamage * (protection == null ? 1.0 : protection.multiplier()));
                  double modifiedDamage = Math.max(0.0, protectedDamage * (penalty == null ? 1.0 : penalty.multiplier()));
                  event.setDamage(modifiedDamage);
                  if (weakening != null) {
                     double damageWithoutWeakening = Math.max(
                        0.0, originalDamage * (protection == null ? 1.0 : protection.multiplier()) * (penalty == null ? 1.0 : penalty.multiplier())
                     );
                     double prevented = Math.max(0.0, damageWithoutWeakening - modifiedDamage);
                     if (prevented > 0.0) {
                        this.deferReduction(
                           event,
                           new DamageReductionAttribution(weakening.sourceId(), attacker.getUniqueId(), weakening.abilityId(), modifiedDamage, prevented),
                           AbilityEffect.WEAKEN
                        );
                     }
                  }

                  if (protectedDamage < weakenedDamage && protection != null) {
                     double prevented = (weakenedDamage - protectedDamage) * (penalty == null ? 1.0 : penalty.multiplier());
                     this.deferReduction(
                        event,
                        new DamageReductionAttribution(protection.sourceId(), player.getUniqueId(), protection.abilityId(), modifiedDamage, prevented),
                        AbilityEffect.ALLY_PROTECT
                     );
                  }
               }
            }
         }
      }
   }

   private void deferReduction(PlayerDamagedByEliteMobEvent event, DamageReductionAttribution attribution, AbilityEffect effect) {
      this.pendingReductions.computeIfAbsent(event, ignored -> new ArrayList<>(2)).add(new TimedCombatModifiers.PendingReduction(attribution, effect));
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteDamageFinalized(PlayerDamagedByEliteMobEvent event) {
      List<TimedCombatModifiers.PendingReduction> reductions = this.pendingReductions.remove(event);
      if (reductions != null) {
         for (TimedCombatModifiers.PendingReduction reduction : reductions) {
            this.finalizeReduction(event, reduction);
         }
      }
   }

   private void finalizeReduction(PlayerDamagedByEliteMobEvent event, TimedCombatModifiers.PendingReduction reduction) {
      DamageReductionAttribution attribution = reduction.attribution();
      double prevented = attribution.survivingPreventedDamage(event.getDamage(), event.isCancelled());
      if (!(prevented <= 0.0)) {
         Player source = Bukkit.getPlayer(attribution.sourceId());
         if (source != null) {
            this.semantics.recordContribution(source, attribution.abilityId(), new AbilityContribution(0.0, 0.0, prevented, 0.0, 0, 1, 0.0));
         }

         this.semantics
            .observe(
               AbilityRuntimeObservation.effect(
                  AbilityRuntimeObservation.Kind.MODIFIER_TRIGGERED,
                  attribution.sourceId(),
                  attribution.targetId(),
                  attribution.abilityId(),
                  prevented,
                  0,
                  reduction.effect()
               )
            );
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEliteRemoved(EliteMobRemoveEvent event) {
      Entity entity = event.getEntity();
      if (entity != null) {
         this.outgoingReductions.clearTarget(entity.getUniqueId());
      }
   }

   private TimedCombatModifiers.EnemyMark strongestMark(UUID enemyId, UUID attackerId, long now) {
      List<TimedCombatModifiers.EnemyMark> marks = this.enemyMarks.get(enemyId);
      if (marks == null) {
         return null;
      } else {
         marks.removeIf(mark -> mark.expiresAtNanos() <= now || !liveSourceNear(mark.sourceId(), Bukkit.getEntity(enemyId)));
         if (marks.isEmpty()) {
            this.enemyMarks.remove(enemyId);
            return null;
         } else {
            return marks.stream()
               .filter(mark -> mark.beneficiaries().contains(attackerId))
               .max(Comparator.comparingDouble(TimedCombatModifiers.EnemyMark::baseMultiplier))
               .orElse(null);
         }
      }
   }

   private TimedCombatModifiers.PlayerModifier strongestOutgoing(
      UUID playerId, long now, SkillType sourceSkill, CombatDamageContext.ClassAbilityDamageDomain classDomain
   ) {
      List<TimedCombatModifiers.PlayerModifier> modifiers = this.outgoingModifiers.get(playerId);
      if (modifiers == null) {
         return null;
      } else {
         Entity target = Bukkit.getPlayer(playerId);
         modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now || !liveSourceNear(modifier.sourceId(), target));
         if (modifiers.isEmpty()) {
            this.outgoingModifiers.remove(playerId);
            return null;
         } else {
            return modifiers.stream()
               .filter(modifier -> modifier.domain() == TimedCombatModifiers.ModifierDomain.ANY || isSpellDamage(sourceSkill, classDomain))
               .max(Comparator.comparingDouble(TimedCombatModifiers.PlayerModifier::multiplier))
               .orElse(null);
         }
      }
   }

   private TimedCombatModifiers.PlayerModifier strongestProtection(UUID playerId, long now) {
      List<TimedCombatModifiers.PlayerModifier> modifiers = this.incomingModifiers.get(playerId);
      if (modifiers == null) {
         return null;
      } else {
         Entity target = Bukkit.getPlayer(playerId);
         modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now || !liveSourceNear(modifier.sourceId(), target));
         if (modifiers.isEmpty()) {
            this.incomingModifiers.remove(playerId);
            return null;
         } else {
            return modifiers.stream().min(Comparator.comparingDouble(TimedCombatModifiers.PlayerModifier::multiplier)).orElse(null);
         }
      }
   }

   private TimedCombatModifiers.PlayerModifier strongestPenalty(UUID playerId, long now) {
      List<TimedCombatModifiers.PlayerModifier> modifiers = this.incomingPenalties.get(playerId);
      if (modifiers == null) {
         return null;
      } else {
         Entity target = Bukkit.getPlayer(playerId);
         modifiers.removeIf(modifier -> modifier.expiresAtNanos() <= now || !liveSourceNear(modifier.sourceId(), target));
         if (modifiers.isEmpty()) {
            this.incomingPenalties.remove(playerId);
            return null;
         } else {
            return modifiers.stream().max(Comparator.comparingDouble(TimedCombatModifiers.PlayerModifier::multiplier)).orElse(null);
         }
      }
   }

   static boolean isSpellDamage(SkillType sourceSkill, CombatDamageContext.ClassAbilityDamageDomain classDomain) {
      return sourceSkill != SkillType.STAVES && sourceSkill != SkillType.WANDS
         ? classDomain != null && classDomain.delivery() != CombatDamageContext.ClassAbilityDelivery.SUMMON
         : true;
   }

   private static boolean liveSourceNear(UUID sourceId, Entity target) {
      Player source = Bukkit.getPlayer(sourceId);
      return source != null
         && source.isOnline()
         && source.isValid()
         && !source.isDead()
         && ClassAbilityEligibility.isEligible(source)
         && target != null
         && target.isValid()
         && source.getWorld().equals(target.getWorld());
   }

   private static long expiresAt(int ticks) {
      long durationNanos = ticks * 50000000L;
      long now = System.nanoTime();
      return Long.MAX_VALUE - now < durationNanos ? Long.MAX_VALUE : now + durationNanos;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         HandlerList.unregisterAll(this);
         this.enemyMarks.clear();
         this.outgoingModifiers.clear();
         this.incomingModifiers.clear();
         this.incomingPenalties.clear();
         this.pendingReductions.clear();
         this.outgoingReductions.close();
      }
   }

   private static double healthFraction(LivingEntity entity) {
      AttributeInstance maximum = entity.getAttribute(Attribute.MAX_HEALTH);
      double value = maximum == null ? Math.max(1.0, entity.getHealth()) : Math.max(1.0, maximum.getValue());
      return Math.max(0.0, Math.min(1.0, entity.getHealth() / value));
   }

   private record EnemyMark(UUID sourceId, Set<UUID> beneficiaries, double baseMultiplier, long expiresAtNanos, String abilityId, boolean healthScaled) {
      boolean equivalent(TimedCombatModifiers.EnemyMark other) {
         return this.sourceId.equals(other.sourceId)
            && this.beneficiaries.equals(other.beneficiaries)
            && this.baseMultiplier == other.baseMultiplier
            && this.abilityId.equals(other.abilityId)
            && this.healthScaled == other.healthScaled;
      }

      double multiplier(double healthFraction) {
         return this.healthScaled ? AbilityStateMath.healthScaledMark(this.baseMultiplier, healthFraction) : this.baseMultiplier;
      }
   }

   private static enum ModifierDomain {
      ANY,
      SPELL;
   }

   private record PendingReduction(DamageReductionAttribution attribution, AbilityEffect effect) {
   }

   private record PlayerModifier(UUID sourceId, double multiplier, long expiresAtNanos, String abilityId, TimedCombatModifiers.ModifierDomain domain) {
      boolean equivalent(TimedCombatModifiers.PlayerModifier other) {
         return this.sourceId.equals(other.sourceId)
            && this.multiplier == other.multiplier
            && this.abilityId.equals(other.abilityId)
            && this.domain == other.domain;
      }
   }
}
