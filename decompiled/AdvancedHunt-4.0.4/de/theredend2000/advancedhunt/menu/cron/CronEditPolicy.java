package de.theredend2000.advancedhunt.menu.cron;

import java.util.Objects;

public final class CronEditPolicy {
   private final String cronTypeMessageKey;
   private final boolean allowSpecialValues;
   private final boolean allowNone;
   private final String defaultBuilderExpression;
   public static final String SPECIAL_NONE = "NONE";

   public CronEditPolicy(String cronTypeMessageKey, boolean allowSpecialValues, boolean allowNone, String defaultBuilderExpression) {
      this.cronTypeMessageKey = cronTypeMessageKey;
      this.allowSpecialValues = allowSpecialValues;
      this.allowNone = allowNone;
      this.defaultBuilderExpression = defaultBuilderExpression;
   }

   public String cronTypeMessageKey() {
      return this.cronTypeMessageKey;
   }

   public boolean allowSpecialValues() {
      return this.allowSpecialValues;
   }

   public boolean allowNone() {
      return this.allowNone;
   }

   public String defaultBuilderExpression() {
      return this.defaultBuilderExpression;
   }

   public static CronEditPolicy progressReset() {
      return new CronEditPolicy("gui.cron.type.reset", false, false, "0 0 0 * * ? *");
   }

   public static CronEditPolicy actSchedule() {
      return new CronEditPolicy("gui.cron.type.act", true, true, "0 0 0 * * ? *");
   }

   public boolean isNone(String value) {
      return value != null && value.equalsIgnoreCase("NONE");
   }

   public boolean isSpecial(String value) {
      return this.allowSpecialValues && value != null ? this.isNone(value) : false;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof CronEditPolicy)) {
         return false;
      } else {
         CronEditPolicy that = (CronEditPolicy)o;
         return this.allowSpecialValues == that.allowSpecialValues
            && this.allowNone == that.allowNone
            && Objects.equals(this.cronTypeMessageKey, that.cronTypeMessageKey)
            && Objects.equals(this.defaultBuilderExpression, that.defaultBuilderExpression);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.cronTypeMessageKey, this.allowSpecialValues, this.allowNone, this.defaultBuilderExpression);
   }

   @Override
   public String toString() {
      return "CronEditPolicy{cronTypeMessageKey='"
         + this.cronTypeMessageKey
         + '\''
         + ", allowSpecialValues="
         + this.allowSpecialValues
         + ", allowNone="
         + this.allowNone
         + ", defaultBuilderExpression='"
         + this.defaultBuilderExpression
         + '\''
         + '}';
   }
}
