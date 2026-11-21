package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

public class SkillTestResult {
   private final String skillId;
   private final String skillName;
   private final SkillType skillType;
   private final int testLevel;
   private int totalHits = 0;
   private int procCount = 0;
   private double expectedProcRate = 0.0;
   private double totalDamageDealt = 0.0;
   private double baseDamageDealt = 0.0;
   private double skillBonusDamage = 0.0;
   private int hitsToKill = 0;
   private double expectedDamageMultiplier = 1.0;
   private double actualDamageMultiplier = 1.0;
   private boolean passed = true;
   private boolean skipped = false;
   private String verificationTag = null;
   private final List<String> issues = new ArrayList<>();
   private final List<String> logs = new ArrayList<>();

   public SkillTestResult(String skillId, String skillName, SkillType skillType, int testLevel) {
      this.skillId = skillId;
      this.skillName = skillName;
      this.skillType = skillType;
      this.testLevel = testLevel;
   }

   public void setExpectedProcRate(double rate) {
      this.expectedProcRate = rate;
   }

   public void setTotalHits(int hits) {
      this.totalHits = hits;
   }

   public void setProcCount(int count) {
      this.procCount = count;
   }

   public void setExpectedDamageMultiplier(double multiplier) {
      this.expectedDamageMultiplier = multiplier;
   }

   public void setHitsToKill(int hits) {
      this.hitsToKill = hits;
   }

   public double getActualProcRate() {
      return this.totalHits > 0 ? (double)this.procCount / this.totalHits : 0.0;
   }

   public void calculateActualDamageMultiplier() {
      if (this.baseDamageDealt > 0.0) {
         this.actualDamageMultiplier = this.totalDamageDealt / this.baseDamageDealt;
      }
   }

   public void addLog(String message) {
      this.logs.add(message);
   }

   public void addIssue(String issue) {
      this.issues.add(issue);
      this.passed = false;
   }

   public void markSkipped(String reason) {
      this.skipped = true;
      this.passed = true;
      this.issues.add(reason);
   }

   public void setVerificationTag(String tag) {
      this.verificationTag = tag;
   }

   public void validate() {
      if (this.expectedProcRate > 0.0 && this.totalHits >= 100) {
         double actualRate = this.getActualProcRate();
         double tolerance = this.expectedProcRate * 0.15;
         if (Math.abs(actualRate - this.expectedProcRate) > tolerance) {
            this.addIssue(
               String.format(
                  "Proc rate mismatch: expected %.1f%%, got %.1f%% (tolerance: %.1f%%)", this.expectedProcRate * 100.0, actualRate * 100.0, tolerance * 100.0
               )
            );
         }
      }

      if (this.expectedDamageMultiplier != 1.0 && this.totalHits > 0) {
         this.calculateActualDamageMultiplier();
         double tolerance = this.expectedDamageMultiplier * 0.05;
         if (Math.abs(this.actualDamageMultiplier - this.expectedDamageMultiplier) > tolerance) {
            this.addIssue(String.format("Damage multiplier mismatch: expected %.2fx, got %.2fx", this.expectedDamageMultiplier, this.actualDamageMultiplier));
         }
      }
   }

   public String getSummary() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.passed ? "&a✓ " : "&c✗ ");
      sb.append("&f").append(this.skillName).append(" &7(Lv.").append(this.testLevel).append(")");
      if (this.expectedProcRate > 0.0) {
         sb.append(String.format(" &7| Proc: &e%.1f%%&7/&e%.1f%%", this.getActualProcRate() * 100.0, this.expectedProcRate * 100.0));
      }

      if (this.expectedDamageMultiplier != 1.0) {
         sb.append(String.format(" &7| Dmg: &e%.2fx&7/&e%.2fx", this.actualDamageMultiplier, this.expectedDamageMultiplier));
      }

      if (!this.passed) {
         for (String issue : this.issues) {
            sb.append("\n  &c→ ").append(issue);
         }
      }

      return sb.toString();
   }

   @Generated
   public String getSkillId() {
      return this.skillId;
   }

   @Generated
   public String getSkillName() {
      return this.skillName;
   }

   @Generated
   public SkillType getSkillType() {
      return this.skillType;
   }

   @Generated
   public int getTestLevel() {
      return this.testLevel;
   }

   @Generated
   public int getTotalHits() {
      return this.totalHits;
   }

   @Generated
   public int getProcCount() {
      return this.procCount;
   }

   @Generated
   public double getExpectedProcRate() {
      return this.expectedProcRate;
   }

   @Generated
   public double getTotalDamageDealt() {
      return this.totalDamageDealt;
   }

   @Generated
   public double getBaseDamageDealt() {
      return this.baseDamageDealt;
   }

   @Generated
   public double getSkillBonusDamage() {
      return this.skillBonusDamage;
   }

   @Generated
   public int getHitsToKill() {
      return this.hitsToKill;
   }

   @Generated
   public double getExpectedDamageMultiplier() {
      return this.expectedDamageMultiplier;
   }

   @Generated
   public double getActualDamageMultiplier() {
      return this.actualDamageMultiplier;
   }

   @Generated
   public boolean isPassed() {
      return this.passed;
   }

   @Generated
   public boolean isSkipped() {
      return this.skipped;
   }

   @Generated
   public String getVerificationTag() {
      return this.verificationTag;
   }

   @Generated
   public List<String> getIssues() {
      return this.issues;
   }

   @Generated
   public List<String> getLogs() {
      return this.logs;
   }
}
