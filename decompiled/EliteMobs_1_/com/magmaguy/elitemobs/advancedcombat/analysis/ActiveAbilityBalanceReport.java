package com.magmaguy.elitemobs.advancedcombat.analysis;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassBand;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassResourceType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public record ActiveAbilityBalanceReport(List<ActiveAbilityBalanceReport.Row> rows) {
   public ActiveAbilityBalanceReport(List<ActiveAbilityBalanceReport.Row> rows) {
      rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
      this.rows = rows;
   }

   public List<ActiveAbilityBalanceReport.Row> outliers() {
      return this.rows.stream().filter(row -> row.outlier() != ActiveAbilityBalanceReport.Outlier.NONE).toList();
   }

   public List<String> graphLines() {
      List<String> lines = new ArrayList<>();
      lines.add("ACTIVE ABILITY BALANCE GRAPH");
      lines.add("Score is a normalized matched-level effect budget. D/G/P means 1, 3 and 8 targets.");
      lines.add("Cadence uses in-combat passive recovery; excludes combat gains, nearby/damage-free bonuses and passives.");

      for (AbilitySlot slot : AbilitySlot.values()) {
         List<ActiveAbilityBalanceReport.Row> slotRows = this.rows
            .stream()
            .filter(rowx -> rowx.slot() == slot)
            .sorted(
               Comparator.<ActiveAbilityBalanceReport.Row, Integer>comparing(rowx -> rowx.band().depth()).thenComparing(ActiveAbilityBalanceReport.Row::formId)
            )
            .toList();
         if (!slotRows.isEmpty()) {
            lines.add("");
            lines.add(slot.name());
            double maximumEfficiency = slotRows.stream().mapToDouble(ActiveAbilityBalanceReport.Row::resourceEfficiency).max().orElse(1.0);

            for (ActiveAbilityBalanceReport.Row row : slotRows) {
               int barLength = maximumEfficiency <= 0.0 ? 0 : (int)Math.round(row.resourceEfficiency() / maximumEfficiency * 24.0);
               String bar = "#".repeat(Math.max(0, barLength));
               ActiveAbilityBalanceReport.TargetScenario duel = row.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL);
               ActiveAbilityBalanceReport.TargetScenario group = row.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP);
               ActiveAbilityBalanceReport.TargetScenario pack = row.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK);
               lines.add(
                  String.format(
                     Locale.ROOT,
                     "  %-27s Lv%3d-%3d %3.0f %s %-24s %5.2f->%5.2f effect | %4.2f casts/min | D/G/P %5.1f/%5.1f/%5.1f per min [%s/%s/%s] | %s",
                     row.formId() + "." + row.slot().name().toLowerCase(Locale.ROOT),
                     row.startLevel(),
                     row.evaluationLevel(),
                     row.resourceCost(),
                     marker(row.outlier()),
                     bar,
                     row.startEffect(),
                     row.normalizedEffect(),
                     row.cadence().sustainableCastsPerMinute(),
                     duel.sustainableEffectPerMinute(),
                     group.sustainableEffectPerMinute(),
                     pack.sustainableEffectPerMinute(),
                     marker(duel.outlier()),
                     marker(group.outlier()),
                     marker(pack.outlier()),
                     row.outlier()
                  )
               );
               row.summon()
                  .ifPresent(
                     summon -> lines.add(
                        String.format(
                           Locale.ROOT,
                           "    summon %dx, cap %d, %.1fs uptime, %.1fs attacks, %.1f HP, %.2fx hit",
                           summon.summonCount(),
                           summon.ownerCap(),
                           summon.uptimeTicks() / 20.0,
                           summon.attackPeriodTicks() / 20.0,
                           summon.maxHealth(),
                           summon.scaledDamagePerHit()
                        )
                     )
                  );
            }
         }
      }

      return List.copyOf(lines);
   }

   public String csv() {
      StringBuilder output = new StringBuilder(
         "root_class,form_id,ability_id,slot,band,start_level,end_level,resource,cost,base_effect,start_effect,normalized_effect,efficiency,outlier,resource_refill_seconds,casts_per_minute,summon_uptime_ticks,summon_count,summon_cap,summon_health,summon_attack_period_ticks,duel_effect,group_effect,pack_effect,duel_per_minute,group_per_minute,pack_per_minute,duel_outlier,group_outlier,pack_outlier\n"
      );

      for (ActiveAbilityBalanceReport.Row row : this.rows) {
         ActiveAbilityBalanceReport.SummonMetrics summon = row.summon().orElse(null);
         output.append(row.rootClassId())
            .append(',')
            .append(row.formId())
            .append(',')
            .append(row.abilityId())
            .append(',')
            .append(row.slot())
            .append(',')
            .append(row.band())
            .append(',')
            .append(row.startLevel())
            .append(',')
            .append(row.evaluationLevel())
            .append(',')
            .append(row.resourceType())
            .append(',')
            .append(format(row.resourceCost()))
            .append(',')
            .append(format(row.baseEffect()))
            .append(',')
            .append(format(row.startEffect()))
            .append(',')
            .append(format(row.normalizedEffect()))
            .append(',')
            .append(format(row.resourceEfficiency()))
            .append(',')
            .append(row.outlier())
            .append(',')
            .append(format(row.cadence().resourceRefillSeconds()))
            .append(',')
            .append(format(row.cadence().sustainableCastsPerMinute()))
            .append(',')
            .append(summon == null ? "" : summon.uptimeTicks())
            .append(',')
            .append(summon == null ? "" : summon.summonCount())
            .append(',')
            .append(summon == null ? "" : summon.ownerCap())
            .append(',')
            .append(summon == null ? "" : format(summon.maxHealth()))
            .append(',')
            .append(summon == null ? "" : summon.attackPeriodTicks())
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL).castEffect()))
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP).castEffect()))
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK).castEffect()))
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL).sustainableEffectPerMinute()))
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP).sustainableEffectPerMinute()))
            .append(',')
            .append(format(row.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK).sustainableEffectPerMinute()))
            .append(',')
            .append(row.scenario(ActiveAbilityBalanceReport.TargetDensity.DUEL).outlier())
            .append(',')
            .append(row.scenario(ActiveAbilityBalanceReport.TargetDensity.GROUP).outlier())
            .append(',')
            .append(row.scenario(ActiveAbilityBalanceReport.TargetDensity.PACK).outlier())
            .append('\n');
      }

      return output.toString();
   }

   private static String marker(ActiveAbilityBalanceReport.Outlier outlier) {
      return switch (outlier) {
         case LOW -> "-";
         case NONE -> "=";
         case HIGH -> "+";
         case MIXED -> "±";
      };
   }

   private static String format(double value) {
      return String.format(Locale.ROOT, "%.4f", value);
   }

   public record Cadence(double resourceRefillSeconds, double sustainableCastsPerMinute) {
      public Cadence(double resourceRefillSeconds, double sustainableCastsPerMinute) {
         if (Double.isFinite(resourceRefillSeconds)
            && !(resourceRefillSeconds < 0.0)
            && Double.isFinite(sustainableCastsPerMinute)
            && !(sustainableCastsPerMinute <= 0.0)) {
            this.resourceRefillSeconds = resourceRefillSeconds;
            this.sustainableCastsPerMinute = sustainableCastsPerMinute;
         } else {
            throw new IllegalArgumentException("Ability cadence must be finite and positive");
         }
      }
   }

   public static enum Outlier {
      LOW,
      NONE,
      HIGH,
      MIXED;
   }

   public record Row(
      String rootClassId,
      String formId,
      String abilityId,
      AbilitySlot slot,
      ClassBand band,
      int startLevel,
      int evaluationLevel,
      ClassResourceType resourceType,
      double resourceCost,
      double baseEffect,
      double startEffect,
      double normalizedEffect,
      double resourceEfficiency,
      ActiveAbilityBalanceReport.Outlier outlier,
      ActiveAbilityBalanceReport.Cadence cadence,
      Optional<ActiveAbilityBalanceReport.SummonMetrics> summon,
      List<ActiveAbilityBalanceReport.TargetScenario> scenarios
   ) {
      public Row(
         String rootClassId,
         String formId,
         String abilityId,
         AbilitySlot slot,
         ClassBand band,
         int startLevel,
         int evaluationLevel,
         ClassResourceType resourceType,
         double resourceCost,
         double baseEffect,
         double startEffect,
         double normalizedEffect,
         double resourceEfficiency,
         ActiveAbilityBalanceReport.Outlier outlier,
         ActiveAbilityBalanceReport.Cadence cadence,
         Optional<ActiveAbilityBalanceReport.SummonMetrics> summon,
         List<ActiveAbilityBalanceReport.TargetScenario> scenarios
      ) {
         Objects.requireNonNull(rootClassId, "rootClassId");
         Objects.requireNonNull(formId, "formId");
         Objects.requireNonNull(abilityId, "abilityId");
         Objects.requireNonNull(slot, "slot");
         Objects.requireNonNull(band, "band");
         Objects.requireNonNull(resourceType, "resourceType");
         Objects.requireNonNull(outlier, "outlier");
         Objects.requireNonNull(cadence, "cadence");
         summon = Objects.requireNonNull(summon, "summon");
         scenarios = List.copyOf(Objects.requireNonNull(scenarios, "scenarios"));
         if (startLevel >= 1 && evaluationLevel >= startLevel) {
            if (scenarios.size() != ActiveAbilityBalanceReport.TargetDensity.values().length) {
               throw new IllegalArgumentException("Every row must model every target density");
            } else {
               this.rootClassId = rootClassId;
               this.formId = formId;
               this.abilityId = abilityId;
               this.slot = slot;
               this.band = band;
               this.startLevel = startLevel;
               this.evaluationLevel = evaluationLevel;
               this.resourceType = resourceType;
               this.resourceCost = resourceCost;
               this.baseEffect = baseEffect;
               this.startEffect = startEffect;
               this.normalizedEffect = normalizedEffect;
               this.resourceEfficiency = resourceEfficiency;
               this.outlier = outlier;
               this.cadence = cadence;
               this.summon = summon;
               this.scenarios = scenarios;
            }
         } else {
            throw new IllegalArgumentException("Ability level band is invalid");
         }
      }

      public ActiveAbilityBalanceReport.TargetScenario scenario(ActiveAbilityBalanceReport.TargetDensity density) {
         Objects.requireNonNull(density, "density");
         return this.scenarios
            .stream()
            .filter(scenario -> scenario.density() == density)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Missing target density " + density));
      }
   }

   public record SummonMetrics(
      int summonCount,
      int ownerCap,
      int uptimeTicks,
      int attacksPerMinion,
      int attackPeriodTicks,
      double maxHealth,
      double scaledDamagePerHit,
      boolean supportRole
   ) {
      public SummonMetrics(
         int summonCount,
         int ownerCap,
         int uptimeTicks,
         int attacksPerMinion,
         int attackPeriodTicks,
         double maxHealth,
         double scaledDamagePerHit,
         boolean supportRole
      ) {
         if (summonCount >= 1
            && ownerCap >= summonCount
            && uptimeTicks >= 1
            && attacksPerMinion >= 1
            && attackPeriodTicks >= 1
            && Double.isFinite(maxHealth)
            && !(maxHealth <= 0.0)
            && Double.isFinite(scaledDamagePerHit)
            && !(scaledDamagePerHit < 0.0)) {
            this.summonCount = summonCount;
            this.ownerCap = ownerCap;
            this.uptimeTicks = uptimeTicks;
            this.attacksPerMinion = attacksPerMinion;
            this.attackPeriodTicks = attackPeriodTicks;
            this.maxHealth = maxHealth;
            this.scaledDamagePerHit = scaledDamagePerHit;
            this.supportRole = supportRole;
         } else {
            throw new IllegalArgumentException("Summon diagnostics are invalid");
         }
      }
   }

   public static enum TargetDensity {
      DUEL(1),
      GROUP(3),
      PACK(8);

      private final int targetCount;

      private TargetDensity(int targetCount) {
         this.targetCount = targetCount;
      }

      public int targetCount() {
         return this.targetCount;
      }
   }

   public record TargetScenario(
      ActiveAbilityBalanceReport.TargetDensity density,
      int targetCount,
      int affectedTargets,
      double castEffect,
      double resourceEfficiency,
      double sustainableEffectPerMinute,
      ActiveAbilityBalanceReport.Outlier outlier
   ) {
      public TargetScenario(
         ActiveAbilityBalanceReport.TargetDensity density,
         int targetCount,
         int affectedTargets,
         double castEffect,
         double resourceEfficiency,
         double sustainableEffectPerMinute,
         ActiveAbilityBalanceReport.Outlier outlier
      ) {
         Objects.requireNonNull(density, "density");
         Objects.requireNonNull(outlier, "outlier");
         if (targetCount < 1 || affectedTargets < 1 || affectedTargets > targetCount) {
            throw new IllegalArgumentException("Target scenario counts are invalid");
         } else if (Double.isFinite(castEffect)
            && !(castEffect < 0.0)
            && Double.isFinite(resourceEfficiency)
            && !(resourceEfficiency < 0.0)
            && Double.isFinite(sustainableEffectPerMinute)
            && !(sustainableEffectPerMinute < 0.0)) {
            this.density = density;
            this.targetCount = targetCount;
            this.affectedTargets = affectedTargets;
            this.castEffect = castEffect;
            this.resourceEfficiency = resourceEfficiency;
            this.sustainableEffectPerMinute = sustainableEffectPerMinute;
            this.outlier = outlier;
         } else {
            throw new IllegalArgumentException("Target scenario scores must be finite and non-negative");
         }
      }
   }
}
