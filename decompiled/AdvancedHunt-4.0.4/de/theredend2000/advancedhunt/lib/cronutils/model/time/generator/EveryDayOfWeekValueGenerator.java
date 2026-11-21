package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.ConstantsMapper;
import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class EveryDayOfWeekValueGenerator extends EveryFieldValueGenerator {
   private final int lastDayOfMonth;
   private int year;
   private int month;
   private final Set<DayOfWeek> dowValidValues;

   EveryDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
      super(cronField);
      Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
      this.year = year;
      this.month = month;
      LocalDate date = LocalDate.of(year, month, 1);
      this.lastDayOfMonth = date.lengthOfMonth();
      Every every = (Every)cronField.getExpression();
      int period = every.getPeriod().getValue();
      Preconditions.checkArgument(period > 0 && period < 8, "Cron Expression for day of week has an invalid period.");
      this.dowValidValues = getValidDays(mondayDoWValue, period, this.from, this.to);
   }

   private static Set<DayOfWeek> getValidDays(WeekDay mondayDoWValue, int period, int from, int to) {
      List<DayOfWeek> validDays = new ArrayList<>(7);
      int day = from;

      while (day <= to) {
         int javaDay = ConstantsMapper.weekDayMapping(mondayDoWValue, ConstantsMapper.JAVA8, day);
         validDays.add(DayOfWeek.of(javaDay));
         day += period;
      }

      return EnumSet.copyOf(validDays);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      int day = reference;

      do {
         day++;
      } while (!this.isMatch(day) && day <= this.lastDayOfMonth);

      if (day > this.lastDayOfMonth) {
         throw new NoSuchValueException();
      } else {
         return day;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      int day = reference;

      do {
         day--;
      } while (!this.isMatch(day) && day > 0);

      if (day <= 0) {
         throw new NoSuchValueException();
      } else {
         return day;
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> candidates = new ArrayList<>();
      int reference = start;

      try {
         while (reference < end) {
            reference = this.generateNextValue(reference);
            if (reference < end) {
               candidates.add(reference);
            }
         }
      } catch (NoSuchValueException var6) {
      }

      return candidates;
   }

   @Override
   public boolean isMatch(int value) {
      if (value <= this.lastDayOfMonth && value >= 1) {
         DayOfWeek dayOfWeek = LocalDate.of(this.year, this.month, value).getDayOfWeek();
         return this.dowValidValues.contains(dayOfWeek);
      } else {
         return false;
      }
   }
}
