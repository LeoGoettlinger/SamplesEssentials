package de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FieldConstraintsBuilder {
   private Map<String, Integer> stringMapping = new HashMap<>();
   private final Map<Integer, Integer> intMapping = new HashMap<>();
   private int startRange = 0;
   private int endRange = Integer.MAX_VALUE;
   private boolean strictRange = false;
   private final Set<SpecialChar> specialChars = new HashSet<>();

   private FieldConstraintsBuilder() {
      this.specialChars.add(SpecialChar.NONE);
   }

   public FieldConstraintsBuilder forField(CronFieldName field) {
      switch (field) {
         case SECOND:
         case MINUTE:
            this.endRange = 59;
            return this;
         case HOUR:
            this.endRange = 23;
            return this;
         case DAY_OF_WEEK:
            this.stringMapping = daysOfWeekMapping();
            this.endRange = 6;
            return this;
         case DAY_OF_MONTH:
            this.startRange = 1;
            this.endRange = 31;
            return this;
         case MONTH:
            this.stringMapping = monthsMapping();
            this.startRange = 1;
            this.endRange = 12;
            return this;
         case DAY_OF_YEAR:
            this.startRange = 1;
            this.endRange = 366;
            return this;
         default:
            return this;
      }
   }

   public FieldConstraintsBuilder addHashSupport() {
      this.specialChars.add(SpecialChar.HASH);
      return this;
   }

   public FieldConstraintsBuilder addLSupport() {
      this.specialChars.add(SpecialChar.L);
      return this;
   }

   public FieldConstraintsBuilder addWSupport() {
      this.specialChars.add(SpecialChar.W);
      return this;
   }

   public FieldConstraintsBuilder addLWSupport() {
      this.specialChars.add(SpecialChar.LW);
      return this;
   }

   public FieldConstraintsBuilder addQuestionMarkSupport() {
      this.specialChars.add(SpecialChar.QUESTION_MARK);
      return this;
   }

   public FieldConstraintsBuilder withIntValueMapping(int source, int dest) {
      this.intMapping.put(source, dest);
      return this;
   }

   public FieldConstraintsBuilder withValidRange(int startRange, int endRange) {
      this.startRange = startRange;
      this.endRange = endRange;
      return this;
   }

   public FieldConstraintsBuilder withStrictRange() {
      this.strictRange = true;
      return this;
   }

   public FieldConstraintsBuilder withShiftedStringMapping(int shiftSize) {
      if (shiftSize > 0 || this.endRange < this.stringMapping.size()) {
         for (Entry<String, Integer> entry : this.stringMapping.entrySet()) {
            int value = entry.getValue();
            value += shiftSize;
            if (value > this.endRange) {
               value -= this.stringMapping.size();
            } else if (value < this.startRange) {
               value += this.startRange - this.endRange;
            }

            this.stringMapping.put(entry.getKey(), value);
         }
      }

      return this;
   }

   public FieldConstraints createConstraintsInstance() {
      return new FieldConstraints(this.stringMapping, this.intMapping, this.specialChars, this.startRange, this.endRange, this.strictRange);
   }

   private static Map<String, Integer> daysOfWeekMapping() {
      Map<String, Integer> stringMapping = new HashMap<>();
      stringMapping.put("MON", 1);
      stringMapping.put("TUE", 2);
      stringMapping.put("WED", 3);
      stringMapping.put("THU", 4);
      stringMapping.put("FRI", 5);
      stringMapping.put("SAT", 6);
      stringMapping.put("SUN", 7);
      return stringMapping;
   }

   private static Map<String, Integer> monthsMapping() {
      Map<String, Integer> stringMapping = new HashMap<>();
      stringMapping.put("JAN", 1);
      stringMapping.put("FEB", 2);
      stringMapping.put("MAR", 3);
      stringMapping.put("APR", 4);
      stringMapping.put("MAY", 5);
      stringMapping.put("JUN", 6);
      stringMapping.put("JUL", 7);
      stringMapping.put("AUG", 8);
      stringMapping.put("SEP", 9);
      stringMapping.put("OCT", 10);
      stringMapping.put("NOV", 11);
      stringMapping.put("DEC", 12);
      return stringMapping;
   }

   public static FieldConstraintsBuilder instance() {
      return new FieldConstraintsBuilder();
   }
}
