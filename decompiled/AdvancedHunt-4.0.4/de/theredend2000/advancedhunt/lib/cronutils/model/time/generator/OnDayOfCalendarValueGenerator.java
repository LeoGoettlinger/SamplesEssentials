package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import java.util.ArrayList;
import java.util.List;

abstract class OnDayOfCalendarValueGenerator extends FieldValueGenerator {
   protected int year;
   protected int month;

   OnDayOfCalendarValueGenerator(CronField cronField, int year, int month) {
      super(cronField);
      this.year = year;
      this.month = month;
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();

      try {
         for (int reference = this.generateNextValue(start); reference < end; reference = this.generateNextValue(reference)) {
            values.add(reference);
         }
      } catch (NoSuchValueException var5) {
      }

      return values;
   }
}
