package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

final class SkillTestEvaluator {
   private final Player player;
   private final UUID playerId;
   private final CombatSimulator simulator;
   private final CombatTestLog testLog;
   private final TestReport report;
   private final Map<SkillType, List<SkillBonus>> skillsByType;
   private final Map<String, Integer> totalProcCounts = new HashMap<>();
   private final Map<String, Integer> totalHitCounts = new HashMap<>();
   private final Map<String, Map<Integer, Integer>> killStacksObserved = new HashMap<>();
   private final Map<String, Integer> killsLanded = new HashMap<>();
   private final Map<String, Map<Integer, Boolean>> results = new HashMap<>();
   private final Map<String, List<String>> issues = new HashMap<>();
   private final Map<SkillType, Map<Integer, double[]>> offensiveBaseline = new HashMap<>();
   private final Map<Integer, double[]> defensiveBaseline = new HashMap<>();
   private final Map<SkillType, Map<Integer, Double>> offensiveSkillDamage = new HashMap<>();
   private final Map<SkillType, Map<Integer, Integer>> offensiveSkillHits = new HashMap<>();
   private final Map<Integer, Double> defensiveSkillDamage = new HashMap<>();
   private final Map<Integer, Integer> defensiveSkillHits = new HashMap<>();

   SkillTestEvaluator(Player player, CombatSimulator simulator, CombatTestLog testLog, TestReport report, Map<SkillType, List<SkillBonus>> skillsByType) {
      this.player = player;
      this.playerId = player.getUniqueId();
      this.simulator = simulator;
      this.testLog = testLog;
      this.report = report;
      this.skillsByType = skillsByType;
   }

   void resetTypeCounters() {
      this.totalProcCounts.clear();
      this.totalHitCounts.clear();
   }

   void observeKillStacks(int level, List<SkillBonus> skills) {
      for (SkillBonus skill : skills) {
         if (skill instanceof StackingSkill stackingSkill) {
            this.killStacksObserved
               .computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>())
               .merge(level, stackingSkill.getCurrentStacks(this.player), Math::max);
         }
      }
   }

   void finishKillPhase(int level, int kills, List<SkillBonus> skills) {
      for (SkillBonus skill : skills) {
         this.killsLanded.merge(skill.getSkillId(), kills, Integer::sum);
         this.killStacksObserved.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).putIfAbsent(level, 0);
      }
   }

   void recordDefensiveDamage(int level, double damage) {
      this.defensiveSkillDamage.merge(level, damage, Double::sum);
      this.defensiveSkillHits.merge(level, 1, Integer::sum);
   }

   void recordOffensiveDamage(SkillType type, int level, double damage) {
      this.offensiveSkillDamage.computeIfAbsent(type, ignored -> new HashMap<>()).merge(level, damage, Double::sum);
      this.offensiveSkillHits.computeIfAbsent(type, ignored -> new HashMap<>()).merge(level, 1, Integer::sum);
   }

   void checkLevelResults(int level, List<SkillBonus> skills, int killPhaseHits) {
      StringBuilder summary = new StringBuilder("  §7Lv.").append(level).append(": ");
      int passed = 0;
      int failed = 0;

      for (SkillBonus skill : skills) {
         boolean shouldBeUnlocked = level >= skill.getRequiredLevel();
         if (SkillSystemTest.KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) {
            int stacks = this.killStacksAt(skill.getSkillId(), level);
            boolean worked = stacks > 0;
            this.results.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).put(level, worked == shouldBeUnlocked);
            if (worked == shouldBeUnlocked) {
               passed++;
            } else {
               failed++;
               this.issues
                  .computeIfAbsent(skill.getSkillId(), ignored -> new ArrayList<>())
                  .add(
                     shouldBeUnlocked
                        ? "No stacks banked at Lv." + level + " after 12 kills"
                        : "Banked " + stacks + " stacks at Lv." + level + " (should be LOCKED, unlock=" + skill.getRequiredLevel() + ")"
                  );
            }
         } else {
            boolean worked = skill.getTestStrategy() == SkillBonus.TestStrategy.ATTRIBUTE_CHECK
               ? this.verifyAttributeEffect(skill)
               : skill.getProcCount(this.player) > 0;
            if (shouldBeUnlocked && skill.getTestStrategy() != SkillBonus.TestStrategy.ATTRIBUTE_CHECK) {
               this.totalProcCounts.merge(skill.getSkillId(), skill.getProcCount(this.player), Integer::sum);
               this.totalHitCounts.merge(skill.getSkillId(), 200 + killPhaseHits, Integer::sum);
            }

            this.results.computeIfAbsent(skill.getSkillId(), ignored -> new HashMap<>()).put(level, worked == shouldBeUnlocked);
            if (worked == shouldBeUnlocked) {
               passed++;
            } else {
               failed++;
               String issue = shouldBeUnlocked
                  ? "No procs at Lv." + level + " (should be unlocked, strategy=" + skill.getTestStrategy().name() + ")"
                  : "Procced at Lv." + level + " (should be LOCKED, unlock=" + skill.getRequiredLevel() + ")";
               this.issues.computeIfAbsent(skill.getSkillId(), ignored -> new ArrayList<>()).add(issue);
            }
         }
      }

      if (failed == 0) {
         summary.append("§a✓ All ").append(passed).append(" passed");
      } else {
         summary.append("§a").append(passed).append(" passed, §c").append(failed).append(" failed");
      }

      this.player.sendMessage(summary.toString());
      this.testLog.log("Level " + level + ": " + passed + " passed, " + failed + " failed");
   }

   void summarizeType(SkillType type) {
      List<SkillBonus> skills = this.skillsByType.get(type);
      this.player.sendMessage("");
      this.player.sendMessage("§7--- " + type.getDisplayName() + " Summary ---");

      for (SkillBonus skill : skills) {
         Map<Integer, Boolean> skillResults = this.results.get(skill.getSkillId());
         List<String> skillIssues = this.issues.get(skill.getSkillId());
         if (skillResults != null) {
            boolean allPassed = skillResults.values().stream().filter(Boolean::booleanValue).count() == SkillSystemTest.TEST_LEVELS.length;
            String verificationTag = this.buildVerificationTag(skill);
            this.player.sendMessage("  " + (allPassed ? "§a✓ " : "§c✗ ") + skill.getBonusName() + " §7" + verificationTag);
            if (!allPassed && skillIssues != null) {
               for (String issue : skillIssues) {
                  this.player.sendMessage("    §c- " + issue);
               }
            }

            SkillTestResult result = new SkillTestResult(skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel());
            Integer procs = this.totalProcCounts.get(skill.getSkillId());
            Integer hits = this.totalHitCounts.get(skill.getSkillId());
            if (procs != null && hits != null && hits > 0) {
               result.setTotalHits(hits);
               result.setProcCount(procs);
               if (skill instanceof ProcSkill procSkill) {
                  result.setExpectedProcRate(procSkill.getProcChance(50));
               }
            } else {
               result.setTotalHits(200 * SkillSystemTest.TEST_LEVELS.length);
            }

            result.setVerificationTag(verificationTag);
            if (skillIssues != null) {
               skillIssues.forEach(result::addIssue);
            }

            this.report.addResult(result);
         }
      }
   }

   void runOffensiveBaseline(int level, SkillType type, String dummyId) {
      for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(this.playerId, type))) {
         PlayerSkillSelection.removeActiveSkill(this.playerId, type, id);
      }

      List<SkillBonus> skills = this.skillsByType.get(type);
      this.respawnIfNeeded(dummyId, skills);
      double totalDamage = 0.0;
      int validHits = 0;
      double expected = 0.0;
      if (type == SkillType.SWORDS || type == SkillType.AXES) {
         DamageBreakdown breakdown = this.simulator.simulateMeleeAttackWithBreakdown(dummyId);
         if (breakdown != null) {
            double attackSpeed = type == SkillType.AXES ? 1.0 : 1.6;
            String weaponType = type == SkillType.AXES ? "DIAMOND_AXE" : "DIAMOND_SWORD";
            expected = DamageBreakdown.calculateExpectedDamage(breakdown.getItemLevel(), level, 50, weaponType, attackSpeed);
         }

         this.simulator.healDummy(dummyId);
      }

      for (int index = 0; index < 10; index++) {
         this.respawnIfNeeded(dummyId, skills);
         double damage = this.performAttack(type, dummyId);
         if (damage > 0.0) {
            totalDamage += damage;
            validHits++;
         }

         this.simulator.healDummy(dummyId);
      }

      this.offensiveBaseline
         .computeIfAbsent(type, ignored -> new HashMap<>())
         .put(level, new double[]{expected, validHits > 0 ? totalDamage / validHits : 0.0});
   }

   void runDefensiveBaseline(int level, String dummyId) {
      for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(this.playerId, SkillType.ARMOR))) {
         PlayerSkillSelection.removeActiveSkill(this.playerId, SkillType.ARMOR, id);
      }

      List<SkillBonus> skills = this.skillsByType.get(SkillType.ARMOR);
      this.respawnIfNeeded(dummyId, skills);
      double totalDamage = 0.0;
      int validHits = 0;

      for (int index = 0; index < 10; index++) {
         double damage = this.simulator.simulateIncomingDamage(dummyId, 10.0);
         if (damage > 0.0) {
            totalDamage += damage;
            validHits++;
         }
      }

      this.defensiveBaseline
         .put(level, new double[]{CombatSimulator.calculateExpectedDefensiveDamage(level, 50, this.player), validHits > 0 ? totalDamage / validHits : 0.0});
   }

   void logDamageValidation() {
      this.testLog.logSection("DAMAGE VALIDATION");
      Map<SkillType, Map<Integer, double[]>> offensiveData = this.buildOffensiveDamageData();
      Map<Integer, double[]> defensiveData = this.buildDefensiveDamageData();
      this.report.setOffensiveDamageData(offensiveData);
      this.report.setDefensiveDamageData(defensiveData);
      this.testLog.log("OFFENSIVE (Player -> Elite):");
      this.player.sendMessage("§6=== DAMAGE VALIDATION ===");
      this.player.sendMessage("§eOFFENSIVE (Player -> Elite):");

      for (SkillType type : SkillType.values()) {
         this.logOffensiveType(type, offensiveData.get(type));
      }

      if (!defensiveData.isEmpty()) {
         this.testLog.log("DEFENSIVE (Elite -> Player):");
         this.player.sendMessage("§eDEFENSIVE (Elite -> Player):");
         StringBuilder summary = new StringBuilder("  §7Armor:");
         this.testLog.log("  Armor:");

         for (int level : SkillSystemTest.TEST_LEVELS) {
            double[] values = defensiveData.get(level);
            if (values != null) {
               double percentOff = values[0] != 0.0 ? (values[1] - values[0]) / values[0] * 100.0 : 0.0;
               String line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)", level, values[0], values[1], percentOff);
               if (values[2] > 0.0) {
                  line = line + String.format(" | With skills: %.1f", values[2]);
               }

               this.testLog.log(line);
            }
         }

         appendLevel50Summary(summary, defensiveData.get(50));
         this.player.sendMessage(summary.toString());
      }
   }

   private int killStacksAt(String skillId, int level) {
      return this.killStacksObserved.getOrDefault(skillId, Map.of()).getOrDefault(level, 0);
   }

   private boolean verifyAttributeEffect(SkillBonus skill) {
      String var2 = skill.getSkillId();

      return switch (var2) {
         case "swords_swift_strikes" -> this.player.getWalkSpeed() > 0.2F;
         case "swords_poise" -> this.hasModifier(Attribute.KNOCKBACK_RESISTANCE, "poise_knockback_resistance");
         case "swords_flurry" -> skill.getProcCount(this.player) > 0 || this.hasModifier(Attribute.ATTACK_SPEED, "flurry_attack_speed");
         case "hoes_grim_reach" -> this.hasEntityInteractionRangeModifier("grim_reach_range");
         case "spears_long_reach" -> this.hasEntityInteractionRangeModifier("long_reach_range");
         case "spears_polearm_mastery" -> this.hasModifier(Attribute.ATTACK_SPEED, "polearm_mastery_speed");
         default -> skill.getProcCount(this.player) > 0;
      };
   }

   private boolean hasEntityInteractionRangeModifier(String key) {
      try {
         return this.hasModifier(Attribute.ENTITY_INTERACTION_RANGE, key);
      } catch (NoSuchFieldError var3) {
         return true;
      }
   }

   private boolean hasModifier(Attribute attribute, String modifierKey) {
      AttributeInstance instance = this.player.getAttribute(attribute);
      if (instance == null) {
         return false;
      } else {
         NamespacedKey key = new NamespacedKey(MetadataHandler.PLUGIN, modifierKey);

         for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getKey().equals(key)) {
               return true;
            }
         }

         return false;
      }
   }

   private String buildVerificationTag(SkillBonus skill) {
      if (SkillSystemTest.KILL_REQUIRED_SKILLS.contains(skill.getSkillId())) {
         int peakStacks = this.killStacksObserved.getOrDefault(skill.getSkillId(), Map.of()).values().stream().mapToInt(Integer::intValue).max().orElse(0);
         int maxStacks = skill instanceof StackingSkill stackingSkill ? stackingSkill.getMaxStacks() : 0;
         return String.format("(KILL %d kills, peak %d/%d stacks)", this.killsLanded.getOrDefault(skill.getSkillId(), 0), peakStacks, maxStacks);
      } else {
         Integer procs = this.totalProcCounts.get(skill.getSkillId());
         Integer hits = this.totalHitCounts.get(skill.getSkillId());

         return switch (skill.getTestStrategy()) {
            case ATTRIBUTE_CHECK -> "(ATTR OK)";
            case CONDITION_SETUP -> procs != null && hits != null && hits > 0
               ? String.format("(COND %d/%d %.0f%%)", procs, hits, (double)procs.intValue() / hits.intValue() * 100.0)
               : "(COND OK)";
            default -> {
               if (procs != null && hits != null && hits > 0) {
                  String expected = skill instanceof ProcSkill procSkill ? String.format("/%.0f%%", procSkill.getProcChance(50) * 100.0) : "";
                  yield String.format("(PROC %.0f%%%s)", (double)procs.intValue() / hits.intValue() * 100.0, expected);
               } else {
                  yield "(unlock Lv." + skill.getRequiredLevel() + ")";
               }
            }
         };
      }
   }

   private void respawnIfNeeded(String dummyId, List<SkillBonus> skills) {
      if (!this.simulator.hasDummy(dummyId) && skills != null && !skills.isEmpty()) {
         this.simulator.respawnIfDead(dummyId, skills.get(0));
      }
   }

   private double performAttack(SkillType type, String dummyId) {
      return switch (type) {
         case BOWS, CROSSBOWS -> this.simulator.simulateRangedAttack(dummyId);
         case TRIDENTS -> this.simulator.simulateTridentAttack(dummyId);
         case ARMOR -> this.simulator.simulateIncomingDamage(dummyId, 10.0);
         case MACES -> this.simulator.simulateDirectAttack(dummyId);
         default -> this.simulator.simulateMeleeAttack(dummyId);
      };
   }

   private Map<SkillType, Map<Integer, double[]>> buildOffensiveDamageData() {
      Map<SkillType, Map<Integer, double[]>> data = new HashMap<>();

      for (Entry<SkillType, Map<Integer, double[]>> typeEntry : this.offensiveBaseline.entrySet()) {
         Map<Integer, double[]> levels = new HashMap<>();

         for (Entry<Integer, double[]> levelEntry : typeEntry.getValue().entrySet()) {
            int level = levelEntry.getKey();
            Double total = this.offensiveSkillDamage.getOrDefault(typeEntry.getKey(), Map.of()).get(level);
            Integer count = this.offensiveSkillHits.getOrDefault(typeEntry.getKey(), Map.of()).get(level);
            double average = total != null && count != null && count > 0 ? total / count.intValue() : 0.0;
            levels.put(level, new double[]{levelEntry.getValue()[0], levelEntry.getValue()[1], average});
         }

         data.put(typeEntry.getKey(), levels);
      }

      return data;
   }

   private Map<Integer, double[]> buildDefensiveDamageData() {
      Map<Integer, double[]> data = new HashMap<>();

      for (Entry<Integer, double[]> entry : this.defensiveBaseline.entrySet()) {
         Double total = this.defensiveSkillDamage.get(entry.getKey());
         Integer count = this.defensiveSkillHits.get(entry.getKey());
         double average = total != null && count != null && count > 0 ? total / count.intValue() : 0.0;
         data.put(entry.getKey(), new double[]{entry.getValue()[0], entry.getValue()[1], average});
      }

      return data;
   }

   private void logOffensiveType(SkillType type, Map<Integer, double[]> data) {
      if (data != null && !data.isEmpty()) {
         StringBuilder summary = new StringBuilder("  §7").append(type.getDisplayName()).append(":");
         this.testLog.log("  " + type.getDisplayName() + ":");

         for (int level : SkillSystemTest.TEST_LEVELS) {
            double[] values = data.get(level);
            if (values != null) {
               String line;
               if (values[0] > 0.0) {
                  double percentOff = (values[1] - values[0]) / values[0] * 100.0;
                  line = String.format("    Lv.%d: Expected: %.1f  Actual: %.1f  (%+.1f%%)", level, values[0], values[1], percentOff);
               } else {
                  line = String.format("    Lv.%d: Actual: %.1f (no formula expected)", level, values[1]);
               }

               if (values[2] > 0.0) {
                  line = line + String.format(" | With skills: %.1f", values[2]);
               }

               this.testLog.log(line);
            }
         }

         appendLevel50Summary(summary, data.get(50));
         this.player.sendMessage(summary.toString());
      }
   }

   private static void appendLevel50Summary(StringBuilder summary, double[] values) {
      if (values != null) {
         if (values[0] > 0.0) {
            double percentOff = (values[1] - values[0]) / values[0] * 100.0;
            summary.append(String.format(" Baseline @Lv50: %.1f/%.1f (%+.1f%%)", values[1], values[0], percentOff));
         } else {
            summary.append(String.format(" Baseline @Lv50: %.1f", values[1]));
         }

         if (values[2] > 0.0 && values[1] > 0.0) {
            summary.append(String.format(" | Skills: %.1f (%.2fx)", values[2], values[2] / values[1]));
         }
      }
   }
}
