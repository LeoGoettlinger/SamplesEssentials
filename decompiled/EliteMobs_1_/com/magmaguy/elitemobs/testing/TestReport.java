package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.advancedcombat.analysis.ActiveAbilityBalanceAnalyzer;
import com.magmaguy.elitemobs.advancedcombat.analysis.ActiveAbilityBalanceReport;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class TestReport {
   private static final Map<UUID, TestReport> lastReports = new HashMap<>();
   private final UUID playerUUID;
   private final long startTime;
   private long endTime;
   private final List<SkillTestResult> results = new ArrayList<>();
   private final Map<SkillType, List<SkillTestResult>> resultsByType = new HashMap<>();
   private final List<SkillTestResult> skippedResults = new ArrayList<>();
   private final ActiveAbilityBalanceReport activeAbilityBalance = ActiveAbilityBalanceAnalyzer.analyzeBuiltIns();
   private final Map<String, SkillTestResult> unobserved = new LinkedHashMap<>();
   private int totalTests = 0;
   private int passedTests = 0;
   private int failedTests = 0;
   private int skippedTests = 0;
   private Map<SkillType, Map<Integer, double[]>> offensiveDamageData;
   private Map<Integer, double[]> defensiveDamageData;

   void expect(SkillBonus skill) {
      this.unobserved
         .putIfAbsent(skill.getSkillId(), new SkillTestResult(skill.getSkillId(), skill.getBonusName(), skill.getSkillType(), skill.getRequiredLevel()));
   }

   void finishCoverage(String reason) {
      for (SkillTestResult result : List.copyOf(this.unobserved.values())) {
         result.addIssue("Not fully observed across requested levels: " + reason);
         this.addResult(result);
      }
   }

   private boolean allPassed() {
      return this.unobserved.isEmpty() && this.passedTests > 0 && this.failedTests == 0;
   }

   public TestReport(UUID playerUUID) {
      this.playerUUID = playerUUID;
      this.startTime = System.currentTimeMillis();
   }

   public static TestReport getLastReport(UUID playerUUID) {
      return lastReports.get(playerUUID);
   }

   public void store() {
      lastReports.put(this.playerUUID, this);
   }

   public void addResult(SkillTestResult result) {
      this.unobserved.remove(result.getSkillId());
      this.results.add(result);
      this.resultsByType.computeIfAbsent(result.getSkillType(), k -> new ArrayList<>()).add(result);
      this.totalTests++;
      if (result.isSkipped()) {
         this.skippedTests++;
      } else if (result.isPassed()) {
         this.passedTests++;
      } else {
         this.failedTests++;
      }
   }

   public void addSkippedResult(SkillTestResult result) {
      this.unobserved.remove(result.getSkillId());
      this.skippedResults.add(result);
      this.totalTests++;
      this.skippedTests++;
   }

   public List<String> generateSummary() {
      this.endTime = System.currentTimeMillis();
      double durationSec = (this.endTime - this.startTime) / 1000.0;
      List<String> lines = new ArrayList<>();
      int testedCount = this.passedTests + this.failedTests;
      lines.add("");
      lines.add(
         String.format(
            "&6&l=== SKILL TEST COMPLETE (%.1fs) | %d skills | %d passed | %d failed | %d skipped ===",
            durationSec,
            this.totalTests,
            this.passedTests,
            this.failedTests,
            this.skippedTests
         )
      );
      lines.add("");

      for (SkillType type : SkillType.values()) {
         List<SkillTestResult> typeResults = this.resultsByType.get(type);
         if (typeResults != null && !typeResults.isEmpty()) {
            long passed = typeResults.stream().filter(r -> r.isPassed() && !r.isSkipped()).count();
            long skipped = typeResults.stream().filter(SkillTestResult::isSkipped).count();
            long total = typeResults.size() - skipped;
            String statusColor = passed == total ? "&a" : "&c";
            lines.add(String.format("&e%s (%s%d/%d&e)", type.getDisplayName(), statusColor, passed, total));
            StringBuilder skillLine = new StringBuilder("  ");

            for (SkillTestResult result : typeResults) {
               if (!result.isSkipped()) {
                  if (result.isPassed()) {
                     skillLine.append("&a").append(result.getSkillName());
                     if (result.getVerificationTag() != null) {
                        skillLine.append("&7[").append(result.getVerificationTag()).append("]");
                     }
                  } else {
                     skillLine.append("&c").append(result.getSkillName()).append("&7[FAILED]");
                  }

                  skillLine.append(" ");
               }
            }

            lines.add(skillLine.toString().trim());

            for (SkillTestResult resultx : typeResults) {
               if (!resultx.isPassed() && !resultx.isSkipped()) {
                  for (String issue : resultx.getIssues()) {
                     lines.add("    &c- " + resultx.getSkillName() + ": " + issue);
                  }
               }
            }
         }
      }

      if (!this.skippedResults.isEmpty()) {
         StringBuilder skippedLine = new StringBuilder("&7SKIPPED (").append(this.skippedResults.size()).append("): ");

         for (SkillTestResult resultxx : this.skippedResults) {
            skippedLine.append(resultxx.getSkillName());
            if (!resultxx.getIssues().isEmpty()) {
               skippedLine.append("(").append(resultxx.getIssues().get(0)).append(")");
            }

            skippedLine.append(", ");
         }

         String skippedStr = skippedLine.toString();
         if (skippedStr.endsWith(", ")) {
            skippedStr = skippedStr.substring(0, skippedStr.length() - 2);
         }

         lines.add("");
         lines.add(skippedStr);
      }

      if (this.offensiveDamageData != null || this.defensiveDamageData != null) {
         lines.add("&6=== DAMAGE VALIDATION ===");
         if (this.offensiveDamageData != null) {
            lines.add("&eOFFENSIVE (Player -> Elite)");

            for (SkillType typex : SkillType.values()) {
               Map<Integer, double[]> data = this.offensiveDamageData.get(typex);
               if (data != null && !data.isEmpty()) {
                  double[] lv50 = data.get(50);
                  if (lv50 != null) {
                     StringBuilder sb = new StringBuilder();
                     sb.append("  &7").append(typex.getDisplayName()).append(": ");
                     if (lv50[0] > 0.0) {
                        double pctOff = (lv50[1] - lv50[0]) / lv50[0] * 100.0;
                        sb.append(String.format("Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
                     } else {
                        sb.append(String.format("Baseline @Lv50: %.1f", lv50[1]));
                     }

                     if (lv50[2] > 0.0 && lv50[1] > 0.0) {
                        sb.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
                     }

                     lines.add(sb.toString());
                  }
               }
            }
         }

         if (this.defensiveDamageData != null && !this.defensiveDamageData.isEmpty()) {
            lines.add("&eDEFENSIVE (Elite -> Player)");
            double[] lv50 = this.defensiveDamageData.get(50);
            if (lv50 != null) {
               StringBuilder sbx = new StringBuilder("  &7Armor: ");
               double pctOff = lv50[0] != 0.0 ? (lv50[1] - lv50[0]) / lv50[0] * 100.0 : 0.0;
               sbx.append(String.format("Baseline @Lv50: %.1f/%.1f (%+.1f%%)", lv50[1], lv50[0], pctOff));
               if (lv50[2] > 0.0 && lv50[1] > 0.0) {
                  sbx.append(String.format(" | Skills: %.1f (%.2fx)", lv50[2], lv50[2] / lv50[1]));
               }

               lines.add(sbx.toString());
            }
         }
      }

      List<ActiveAbilityBalanceReport.Row> activeOutliers = this.activeAbilityBalance.outliers();
      lines.add("&6=== ACTIVE ABILITY BALANCE ===");
      lines.add(
         String.format("&7%d abilities modeled across level bands; &e%d efficiency outliers.", this.activeAbilityBalance.rows().size(), activeOutliers.size())
      );
      activeOutliers.stream().limit(6L).forEach(row -> {
         lines.add(String.format("  %s &f%s &7%.2f/100 resource", switch (row.outlier()) {
            case HIGH -> "&cHIGH";
            case LOW -> "&9LOW";
            case MIXED -> "&eMIXED";
            case NONE -> "&7NONE";
         }, row.abilityId(), row.resourceEfficiency()));
      });
      lines.add("");
      if (this.allPassed()) {
         lines.add("&a&lALL TESTS PASSED!" + (this.skippedTests > 0 ? " &7(" + this.skippedTests + " skipped)" : ""));
      } else if (this.failedTests == 0) {
         lines.add("&c&lINCOMPLETE: no complete passing observations");
      } else {
         lines.add(String.format("&c&l%d TEST(S) FAILED &7- Use /em debug combat results for details", this.failedTests));
      }

      lines.add("&6&l================================================");
      return lines;
   }

   public List<String> generateDetailedReport() {
      List<String> lines = new ArrayList<>();
      lines.add("&6&l========== DETAILED TEST REPORT ==========");
      lines.add("");

      for (SkillType type : SkillType.values()) {
         List<SkillTestResult> typeResults = this.resultsByType.get(type);
         if (typeResults != null && !typeResults.isEmpty()) {
            lines.add("&e&l" + type.getDisplayName());
            lines.add("&7" + "-".repeat(40));

            for (SkillTestResult result : typeResults) {
               lines.add(result.getSummary());

               for (String log : result.getLogs()) {
                  lines.add("  &7" + log);
               }
            }

            lines.add("");
         }
      }

      lines.add("&6&lActive Ability Efficiency Outliers");
      if (this.activeAbilityBalance.outliers().isEmpty()) {
         lines.add("&aNone detected.");
      } else {
         for (ActiveAbilityBalanceReport.Row row : this.activeAbilityBalance.outliers()) {
            lines.add(
               String.format(
                  "  &7[%s] &f%s &7Lv.%d cost %.0f, efficiency %.2f",
                  row.outlier(),
                  row.abilityId(),
                  row.evaluationLevel(),
                  row.resourceCost(),
                  row.resourceEfficiency()
               )
            );
         }
      }

      return lines;
   }

   public String exportToText() {
      StringBuilder sb = new StringBuilder();
      sb.append("ELITEMOBS SKILL SYSTEM TEST REPORT\n");
      sb.append("==================================\n\n");
      sb.append("Generated: ").append(new Date()).append("\n");
      sb.append("Duration: ").append((this.endTime - this.startTime) / 1000L).append(" seconds\n\n");
      sb.append("SUMMARY\n");
      sb.append("-------\n");
      sb.append("Total: ").append(this.totalTests).append("\n");
      sb.append("Passed: ").append(this.passedTests).append("\n");
      sb.append("Failed: ").append(this.failedTests).append("\n");
      sb.append("Skipped: ").append(this.skippedTests).append("\n\n");
      sb.append("DETAILED RESULTS\n");
      sb.append("----------------\n\n");

      for (SkillType type : SkillType.values()) {
         List<SkillTestResult> typeResults = this.resultsByType.get(type);
         if (typeResults != null && !typeResults.isEmpty()) {
            sb.append(type.getDisplayName().toUpperCase()).append("\n");
            sb.append("-".repeat(type.getDisplayName().length())).append("\n");

            for (SkillTestResult result : typeResults) {
               sb.append(String.format("  [%s] %s (Lv.%d)\n", result.isPassed() ? "PASS" : "FAIL", result.getSkillName(), result.getTestLevel()));
               if (result.getExpectedProcRate() > 0.0) {
                  sb.append(
                     String.format("    Proc Rate: %.1f%% (expected %.1f%%)\n", result.getActualProcRate() * 100.0, result.getExpectedProcRate() * 100.0)
                  );
               }

               if (result.getExpectedDamageMultiplier() != 1.0) {
                  sb.append(
                     String.format("    Damage Mult: %.2fx (expected %.2fx)\n", result.getActualDamageMultiplier(), result.getExpectedDamageMultiplier())
                  );
               }

               sb.append(
                  String.format(
                     "    Hits: %d | Total Damage: %.1f | Skill Bonus: %.1f\n",
                     result.getTotalHits(),
                     result.getTotalDamageDealt(),
                     result.getSkillBonusDamage()
                  )
               );
               if (!result.isPassed()) {
                  for (String issue : result.getIssues()) {
                     sb.append("    ! ").append(issue).append("\n");
                  }
               }

               sb.append("\n");
            }
         }
      }

      if (this.offensiveDamageData != null || this.defensiveDamageData != null) {
         sb.append("\nDAMAGE VALIDATION\n");
         sb.append("=================\n\n");
         if (this.offensiveDamageData != null) {
            sb.append("OFFENSIVE (Player -> Elite)\n");
            sb.append("--------------------------\n");

            for (SkillType typex : SkillType.values()) {
               Map<Integer, double[]> data = this.offensiveDamageData.get(typex);
               if (data != null && !data.isEmpty()) {
                  sb.append(typex.getDisplayName()).append(":\n");
                  int[] levels = data.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();

                  for (int level : levels) {
                     double[] vals = data.get(level);
                     if (vals[0] > 0.0) {
                        double pctOff = (vals[1] - vals[0]) / vals[0] * 100.0;
                        sb.append(String.format("  Lv.%d: Expected: %.1f   Actual: %.1f   (%+.1f%%)\n", level, vals[0], vals[1], pctOff));
                     } else {
                        sb.append(String.format("  Lv.%d: Actual: %.1f (no formula expected)\n", level, vals[1]));
                     }
                  }

                  double[] lv50 = data.get(50);
                  if (lv50 != null && lv50[2] > 0.0 && lv50[1] > 0.0) {
                     sb.append(String.format("  Avg with skills @Lv50: %.1f/hit (%.2fx baseline)\n", lv50[2], lv50[2] / lv50[1]));
                  }

                  sb.append("\n");
               }
            }
         }

         if (this.defensiveDamageData != null && !this.defensiveDamageData.isEmpty()) {
            sb.append("DEFENSIVE (Elite -> Player)\n");
            sb.append("---------------------------\n");
            sb.append("Armor:\n");
            int[] levels = this.defensiveDamageData.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();

            for (int levelx : levels) {
               double[] vals = this.defensiveDamageData.get(levelx);
               double pctOff = vals[0] != 0.0 ? (vals[1] - vals[0]) / vals[0] * 100.0 : 0.0;
               sb.append(String.format("  Lv.%d: Expected: %.1f   Actual: %.1f   (%+.1f%%)\n", levelx, vals[0], vals[1], pctOff));
            }

            double[] lv50 = this.defensiveDamageData.get(50);
            if (lv50 != null && lv50[2] > 0.0 && lv50[1] > 0.0) {
               sb.append(String.format("  Avg with skills @Lv50: %.1f/hit (%.2fx baseline)\n", lv50[2], lv50[2] / lv50[1]));
            }

            sb.append("\n");
         }
      }

      sb.append("\nACTIVE ABILITY BALANCE\n");
      sb.append("======================\n\n");

      for (String line : this.activeAbilityBalance.graphLines()) {
         sb.append(line).append('\n');
      }

      sb.append("\nACTIVE ABILITY DATA (CSV)\n");
      sb.append("=========================\n");
      sb.append(this.activeAbilityBalance.csv());
      return sb.toString();
   }

   public ItemStack generateBook() {
      if (this.endTime == 0L) {
         this.endTime = System.currentTimeMillis();
      }

      ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
      BookMeta bookMeta = (BookMeta)book.getItemMeta();
      if (bookMeta == null) {
         return book;
      } else {
         bookMeta.setAuthor("EliteMobs");
         bookMeta.setTitle("Skill Test Report");
         int PAGE_LIMIT = 256;
         double durationSec = (this.endTime - this.startTime) / 1000.0;
         StringBuilder cover = new StringBuilder();
         cover.append("§6§lSkill Test Report\n\n");
         cover.append("§0Duration: §e").append(String.format("%.1f", durationSec)).append("s\n\n");
         cover.append("§0Skills tested: §e").append(this.totalTests).append("\n");
         cover.append("§aPassed: ").append(this.passedTests).append("\n");
         cover.append("§cFailed: ").append(this.failedTests).append("\n");
         cover.append("§7Skipped: ").append(this.skippedTests).append("\n\n");
         if (this.allPassed()) {
            cover.append("§a§lALL PASSED");
         } else {
            cover.append("§c§l").append(this.failedTests > 0 ? this.failedTests + " FAILED" : "INCOMPLETE");
         }

         bookMeta.addPage(new String[]{cover.toString()});

         for (SkillType type : SkillType.values()) {
            List<SkillTestResult> typeResults = this.resultsByType.get(type);
            if (typeResults != null && !typeResults.isEmpty()) {
               List<SkillTestResult> skippedForType = new ArrayList<>();

               for (SkillTestResult sr : this.skippedResults) {
                  if (sr.getSkillType() == type) {
                     skippedForType.add(sr);
                  }
               }

               StringBuilder page = new StringBuilder();
               page.append("§6§l").append(type.getDisplayName()).append("\n\n");
               List<SkillTestResult> allForType = new ArrayList<>(typeResults);

               for (SkillTestResult srx : skippedForType) {
                  if (!allForType.contains(srx)) {
                     allForType.add(srx);
                  }
               }

               for (SkillTestResult result : allForType) {
                  StringBuilder entry = new StringBuilder();
                  if (result.isSkipped()) {
                     entry.append("§7[SKIP] ").append(result.getSkillName());
                  } else if (result.isPassed()) {
                     entry.append("§a✓ ").append(result.getSkillName());
                     if (result.getVerificationTag() != null) {
                        entry.append(" §7").append(result.getVerificationTag());
                     }
                  } else {
                     entry.append("§c✗ ").append(result.getSkillName());
                     if (result.getVerificationTag() != null) {
                        entry.append(" §7").append(result.getVerificationTag());
                     }
                  }

                  entry.append("\n");
                  if (page.length() + entry.length() > PAGE_LIMIT) {
                     bookMeta.addPage(new String[]{page.toString()});
                     page = new StringBuilder();
                     page.append("§6§l").append(type.getDisplayName()).append(" (cont.)\n\n");
                  }

                  page.append((CharSequence)entry);
               }

               if (page.length() > 0) {
                  bookMeta.addPage(new String[]{page.toString()});
               }
            }
         }

         if (this.failedTests > 0) {
            StringBuilder issuePage = new StringBuilder();
            issuePage.append("§c§lFailed Skills\n\n");

            for (SkillType typex : SkillType.values()) {
               List<SkillTestResult> typeResults = this.resultsByType.get(typex);
               if (typeResults != null) {
                  for (SkillTestResult result : typeResults) {
                     if (!result.isPassed() && !result.isSkipped()) {
                        StringBuilder entryx = new StringBuilder();
                        entryx.append("§c").append(result.getSkillName());
                        if (!result.getIssues().isEmpty()) {
                           entryx.append("\n§7").append(result.getIssues().get(0));
                        }

                        entryx.append("\n");
                        if (issuePage.length() + entryx.length() > PAGE_LIMIT) {
                           bookMeta.addPage(new String[]{issuePage.toString()});
                           issuePage = new StringBuilder();
                           issuePage.append("§c§lFailed Skills (cont.)\n\n");
                        }

                        issuePage.append((CharSequence)entryx);
                     }
                  }
               }
            }

            if (issuePage.length() > 0) {
               bookMeta.addPage(new String[]{issuePage.toString()});
            }
         }

         book.setItemMeta(bookMeta);
         return book;
      }
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   @Generated
   public long getStartTime() {
      return this.startTime;
   }

   @Generated
   public long getEndTime() {
      return this.endTime;
   }

   @Generated
   public List<SkillTestResult> getResults() {
      return this.results;
   }

   @Generated
   public Map<SkillType, List<SkillTestResult>> getResultsByType() {
      return this.resultsByType;
   }

   @Generated
   public List<SkillTestResult> getSkippedResults() {
      return this.skippedResults;
   }

   @Generated
   public ActiveAbilityBalanceReport getActiveAbilityBalance() {
      return this.activeAbilityBalance;
   }

   @Generated
   public Map<String, SkillTestResult> getUnobserved() {
      return this.unobserved;
   }

   @Generated
   public int getTotalTests() {
      return this.totalTests;
   }

   @Generated
   public int getPassedTests() {
      return this.passedTests;
   }

   @Generated
   public int getFailedTests() {
      return this.failedTests;
   }

   @Generated
   public int getSkippedTests() {
      return this.skippedTests;
   }

   @Generated
   public Map<SkillType, Map<Integer, double[]>> getOffensiveDamageData() {
      return this.offensiveDamageData;
   }

   @Generated
   public Map<Integer, double[]> getDefensiveDamageData() {
      return this.defensiveDamageData;
   }

   @Generated
   public void setOffensiveDamageData(Map<SkillType, Map<Integer, double[]>> offensiveDamageData) {
      this.offensiveDamageData = offensiveDamageData;
   }

   @Generated
   public void setDefensiveDamageData(Map<Integer, double[]> defensiveDamageData) {
      this.defensiveDamageData = defensiveDamageData;
   }
}
