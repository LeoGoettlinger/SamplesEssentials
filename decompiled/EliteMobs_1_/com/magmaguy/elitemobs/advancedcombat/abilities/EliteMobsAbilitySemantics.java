package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class EliteMobsAbilitySemantics implements AbilitySemantics {
   private final EliteMobsAbilitySemantics.ThreatSink threatSink;
   private final EliteMobsAbilitySemantics.ContributionSink contributionSink;
   private final EliteMobsAbilitySemantics.HealingMultiplier healingMultiplier;
   private final EliteMobsAbilitySemantics.ResourceSink resourceSink;
   private final EliteMobsAbilitySemantics.ActiveLineageProvider activeLineageProvider;
   private final EliteMobsAbilitySemantics.SourceCleanup sourceCleanup;
   private final EliteMobsAbilitySemantics.MechanicModifierProvider mechanicModifierProvider;
   private final EliteMobsAbilitySemantics.RuntimeSignalSink runtimeSignalSink;
   private final EliteMobsAbilitySemantics.ObservationSink observationSink;

   public EliteMobsAbilitySemantics(
      EliteMobsAbilitySemantics.ThreatSink threatSink,
      EliteMobsAbilitySemantics.ContributionSink contributionSink,
      EliteMobsAbilitySemantics.HealingMultiplier healingMultiplier,
      EliteMobsAbilitySemantics.ResourceSink resourceSink,
      EliteMobsAbilitySemantics.ActiveLineageProvider activeLineageProvider,
      EliteMobsAbilitySemantics.SourceCleanup sourceCleanup,
      EliteMobsAbilitySemantics.MechanicModifierProvider mechanicModifierProvider,
      EliteMobsAbilitySemantics.RuntimeSignalSink runtimeSignalSink,
      EliteMobsAbilitySemantics.ObservationSink observationSink
   ) {
      this.threatSink = Objects.requireNonNull(threatSink, "threatSink");
      this.contributionSink = Objects.requireNonNull(contributionSink, "contributionSink");
      this.healingMultiplier = Objects.requireNonNull(healingMultiplier, "healingMultiplier");
      this.resourceSink = Objects.requireNonNull(resourceSink, "resourceSink");
      this.activeLineageProvider = Objects.requireNonNull(activeLineageProvider, "activeLineageProvider");
      this.sourceCleanup = Objects.requireNonNull(sourceCleanup, "sourceCleanup");
      this.mechanicModifierProvider = Objects.requireNonNull(mechanicModifierProvider, "mechanicModifierProvider");
      this.runtimeSignalSink = Objects.requireNonNull(runtimeSignalSink, "runtimeSignalSink");
      this.observationSink = Objects.requireNonNull(observationSink, "observationSink");
   }

   @Override
   public boolean isEnemy(Player caster, LivingEntity candidate) {
      return candidate != null && EntityTracker.getEliteMobEntity(candidate) != null;
   }

   @Override
   public boolean canTargetEnemy(Player caster, LivingEntity candidate, FixedAbilitySpec spec) {
      return EliteAbilityTargetAuthorization.canTarget(caster, candidate, spec);
   }

   @Override
   public boolean canApplyEnemyEffect(Player caster, LivingEntity candidate, FixedAbilitySpec spec, AbilityEffect effect) {
      return EliteAbilityTargetAuthorization.canApply(caster, candidate, spec, effect);
   }

   @Override
   public Collection<Player> alliesOf(Player caster) {
      MatchInstance casterMatch = PlayerData.getMatchInstance(caster);
      List<Player> nearbyParty = PartyManager.getNearbyMembers(caster, caster.getLocation())
         .stream()
         .filter(ally -> PlayerData.getMatchInstance(ally) == casterMatch)
         .toList();
      if (nearbyParty.stream().anyMatch(player -> player.getUniqueId().equals(caster.getUniqueId()))) {
         return nearbyParty;
      } else {
         ArrayList<Player> allies = new ArrayList<>(nearbyParty.size() + 1);
         allies.add(caster);
         allies.addAll(nearbyParty);
         return List.copyOf(allies);
      }
   }

   @Override
   public void requestThreat(AbilitySemantics.ThreatRequest request) {
      this.threatSink.apply(request);
   }

   @Override
   public void recordContribution(Player caster, String abilityId, AbilityContribution contribution) {
      this.contributionSink.record(caster, abilityId, contribution);
   }

   @Override
   public void observe(AbilityRuntimeObservation observation) {
      this.observationSink.record(observation);
   }

   @Override
   public double healingMultiplier(Player caster) {
      double multiplier = this.healingMultiplier.multiplier(caster);
      return Double.isFinite(multiplier) ? Math.max(0.0, multiplier) : 1.0;
   }

   @Override
   public AbilityMechanicModifiers mechanicModifiers(Player caster) {
      AbilityMechanicModifiers modifiers = this.mechanicModifierProvider.modifiers(caster);
      return modifiers == null ? AbilityMechanicModifiers.NEUTRAL : modifiers;
   }

   @Override
   public void signal(Player player, AbilityRuntimeSignal signal, int durationTicks) {
      this.runtimeSignalSink.signal(player, signal, Math.max(1, durationTicks));
   }

   @Override
   public void grantResource(Player caster, double amount) {
      this.resourceSink.grant(caster, amount);
   }

   @Override
   public List<String> activeFormIds(Player player) {
      return List.copyOf(this.activeLineageProvider.formIds(player));
   }

   @Override
   public void clearSource(Player caster) {
      this.sourceCleanup.clear(caster);
   }

   @FunctionalInterface
   public interface ActiveLineageProvider {
      List<String> formIds(Player var1);
   }

   @FunctionalInterface
   public interface ContributionSink {
      void record(Player var1, String var2, AbilityContribution var3);
   }

   @FunctionalInterface
   public interface HealingMultiplier {
      double multiplier(Player var1);
   }

   @FunctionalInterface
   public interface MechanicModifierProvider {
      AbilityMechanicModifiers modifiers(Player var1);
   }

   @FunctionalInterface
   public interface ObservationSink {
      void record(AbilityRuntimeObservation var1);
   }

   @FunctionalInterface
   public interface ResourceSink {
      void grant(Player var1, double var2);
   }

   @FunctionalInterface
   public interface RuntimeSignalSink {
      void signal(Player var1, AbilityRuntimeSignal var2, int var3);
   }

   @FunctionalInterface
   public interface SourceCleanup {
      void clear(Player var1);
   }

   @FunctionalInterface
   public interface ThreatSink {
      void apply(AbilitySemantics.ThreatRequest var1);
   }
}
