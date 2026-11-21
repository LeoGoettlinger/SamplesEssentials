package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CombatTestLog {
   private final List<String> logEntries = new ArrayList<>();
   private final String sessionId;
   private final long startTime = System.currentTimeMillis();

   public CombatTestLog() {
      this.sessionId = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
   }

   public void log(String message) {
      long elapsed = System.currentTimeMillis() - this.startTime;
      String timestamp = String.format("[%d.%03ds]", elapsed / 1000L, elapsed % 1000L);
      this.logEntries.add(timestamp + " " + message);
   }

   public void logSection(String title) {
      this.log("");
      this.log("=".repeat(60));
      this.log(title);
      this.log("=".repeat(60));
   }

   public void logSubsection(String title) {
      this.log("");
      this.log("--- " + title + " ---");
   }

   public void logDamageEvent(String skillName, int level, double baseDamage, double finalDamage, boolean procced, boolean isCrit, String notes) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("  [HIT] Base: %.2f -> Final: %.2f", baseDamage, finalDamage));
      if (procced) {
         sb.append(" [PROC]");
      }

      if (isCrit) {
         sb.append(" [CRIT]");
      }

      if (notes != null && !notes.isEmpty()) {
         sb.append(" | ").append(notes);
      }

      this.log(sb.toString());
   }

   public void logProcTest(
      String skillId,
      String skillName,
      int level,
      double expectedProcRate,
      int totalHits,
      int procCount,
      double avgBaseDamage,
      double avgProcDamage,
      double damageIncrease
   ) {
      this.log(String.format("  Skill: %s (Lv.%d)", skillName, level));
      this.log(String.format("  Expected Proc Rate: %.1f%%", expectedProcRate * 100.0));
      this.log(String.format("  Actual Proc Rate: %.1f%% (%d/%d hits)", totalHits > 0 ? (double)procCount / totalHits * 100.0 : 0.0, procCount, totalHits));
      this.log(String.format("  Base Damage (avg): %.2f", avgBaseDamage));
      this.log(String.format("  Proc Damage (avg): %.2f", avgProcDamage));
      this.log(String.format("  Damage Increase: +%.1f%%", damageIncrease * 100.0));
   }

   public void logBaseline(int level, double avgDamage, int hitsToKill, double dps) {
      this.log(String.format("  Level %d: Avg Damage = %.2f, Hits to Kill = %d, DPS = %.1f", level, avgDamage, hitsToKill, dps));
   }

   public void logError(String error) {
      this.log("[ERROR] " + error);
   }

   public void logWarning(String warning) {
      this.log("[WARN] " + warning);
   }

   public void logResult(String skillName, boolean passed, String details) {
      String status = passed ? "[PASS]" : "[FAIL]";
      this.log(String.format("  %s %s - %s", status, skillName, details));
   }

   public File saveToFile() {
      File logsDir = new File(MetadataHandler.PLUGIN.getDataFolder(), "logs/combat_tests");
      if (!logsDir.exists()) {
         logsDir.mkdirs();
      }

      File logFile = new File(logsDir, "combat_test_" + this.sessionId + ".log");

      try {
         File var9;
         try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
            writer.println("ELITEMOBS COMBAT SYSTEM TEST LOG");
            writer.println("Session: " + this.sessionId);
            writer.println("Generated: " + new Date());
            writer.println();

            for (String entry : this.logEntries) {
               writer.println(entry);
            }

            writer.println();
            writer.println("=== END OF LOG ===");
            Logger.info("Combat test log saved to: " + logFile.getAbsolutePath());
            var9 = logFile;
         }

         return var9;
      } catch (IOException var8) {
         Logger.warn("Failed to save combat test log: " + var8.getMessage());
         return null;
      }
   }

   public List<String> getEntries() {
      return new ArrayList<>(this.logEntries);
   }
}
