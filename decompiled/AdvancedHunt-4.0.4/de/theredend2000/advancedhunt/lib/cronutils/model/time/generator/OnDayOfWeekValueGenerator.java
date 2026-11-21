package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.ConstantsMapper;
import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.time.DayOfWeek;
import java.time.LocalDate;

class OnDayOfWeekValueGenerator extends OnDayOfCalendarValueGenerator {
   private static final On ON_SATURDAY = new On(new IntegerFieldValue(7));
   private final WeekDay mondayDoWValue;

   public OnDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
      super(cronField, year, month);
      Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
      this.mondayDoWValue = mondayDoWValue;
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      On on = (On)this.cronField.getExpression();
      int value = this.generateValue(on, this.year, this.month, reference);
      if (value <= reference) {
         throw new NoSuchValueException();
      } else {
         return value;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      On on = (On)this.cronField.getExpression();
      int value = this.generateValue(on, this.year, this.month, reference);
      if (value >= reference) {
         throw new NoSuchValueException();
      } else {
         return value;
      }
   }

   @Override
   public boolean isMatch(int value) {
      On on = (On)this.cronField.getExpression();

      try {
         return value == this.generateValue(on, this.year, this.month, value - 1);
      } catch (NoSuchValueException var4) {
         return false;
      }
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof On;
   }

   private int generateValue(On on, int year, int month, int reference) throws NoSuchValueException {
      switch (on.getSpecialChar().getValue()) {
         case HASH:
            return this.generateHashValues(on, year, month);
         case L:
            return on.getTime().getValue() == -1 ? this.generateNoneValues(ON_SATURDAY, year, month, reference) : this.generateLValues(on, year, month);
         case NONE:
            return this.generateNoneValues(on, year, month, reference);
         default:
            throw new NoSuchValueException();
      }
   }

   private int generateHashValues(On on, int year, int month) {
      DayOfWeek dowForFirstDoM = LocalDate.of(year, month, 1).getDayOfWeek();
      int requiredDoW = ConstantsMapper.weekDayMapping(this.mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());
      int requiredNth = on.getNth().getValue();
      int baseDay = 1;
      int diff = dowForFirstDoM.getValue() - requiredDoW;
      if (diff < 0) {
         baseDay += Math.abs(diff);
      }

      if (diff > 0) {
         baseDay = baseDay + 7 - diff;
      }

      return (requiredNth - 1) * 7 + baseDay;
   }

   private int generateLValues(On on, int year, int month) throws NoSuchValueException {
      int lastDoM = LocalDate.of(year, month, 1).lengthOfMonth();
      LocalDate lastDoMDateTime = LocalDate.of(year, month, lastDoM);
      int dowForLastDoM = lastDoMDateTime.getDayOfWeek().getValue();
      int requiredDoW = ConstantsMapper.weekDayMapping(this.mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());
      int dowDiff = dowForLastDoM - requiredDoW;
      if (dowDiff == 0) {
         return lastDoMDateTime.getDayOfMonth();
      } else if (dowDiff < 0) {
         return lastDoMDateTime.minusDays((long)dowForLastDoM + (7 - requiredDoW)).getDayOfMonth();
      } else if (dowDiff > 0) {
         return lastDoMDateTime.minusDays(dowDiff).getDayOfMonth();
      } else {
         throw new NoSuchValueException();
      }
   }

   private int generateNoneValues(On on, int year, int month, int reference) {
      int dowForFirstDoM = LocalDate.of(year, month, 1).getDayOfWeek().getValue();
      int requiredDoW = ConstantsMapper.weekDayMapping(this.mondayDoWValue, ConstantsMapper.JAVA8, on.getTime().getValue());
      int baseDay = 1;
      int diff = dowForFirstDoM - requiredDoW;
      if (diff < 0) {
         baseDay += Math.abs(diff);
      }

      if (diff > 0) {
         baseDay = baseDay + 7 - diff;
      }

      if (reference < 1) {
         return baseDay;
      } else {
         while (baseDay <= reference) {
            baseDay += 7;
         }

         return baseDay;
      }
   }
}
