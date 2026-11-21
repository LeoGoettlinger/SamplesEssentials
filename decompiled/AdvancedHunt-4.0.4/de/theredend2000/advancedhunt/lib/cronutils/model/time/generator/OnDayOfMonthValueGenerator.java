package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.time.DayOfWeek;
import java.time.LocalDate;

class OnDayOfMonthValueGenerator extends OnDayOfCalendarValueGenerator {
   public OnDayOfMonthValueGenerator(CronField cronField, int year, int month) {
      super(cronField, year, month);
      Preconditions.checkArgument(CronFieldName.DAY_OF_MONTH.equals(cronField.getField()), "CronField does not belong to day of month");
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      On on = (On)this.cronField.getExpression();
      int value = this.generateValue(on, this.year, this.month);
      if (value <= reference) {
         throw new NoSuchValueException();
      } else {
         return value;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      On on = (On)this.cronField.getExpression();
      int value = this.generateValue(on, this.year, this.month);
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
         return value == this.generateValue(on, this.year, this.month);
      } catch (NoSuchValueException var4) {
         return false;
      }
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof On;
   }

   private int generateValue(On on, int year, int month) throws NoSuchValueException {
      int dayOfMonth = on.getTime().getValue();
      switch (on.getSpecialChar().getValue()) {
         case L:
            int daysBefore = on.getNth().getValue();
            return LocalDate.of(year, month, 1).lengthOfMonth() - (daysBefore > 0 ? daysBefore : 0);
         case W:
            LocalDate doM = LocalDate.of(year, month, dayOfMonth);
            if (doM.getDayOfWeek() == DayOfWeek.SATURDAY) {
               if (dayOfMonth == 1) {
                  return 3;
               }

               return dayOfMonth - 1;
            } else {
               if (doM.getDayOfWeek() == DayOfWeek.SUNDAY && dayOfMonth + 1 <= doM.lengthOfMonth()) {
                  return dayOfMonth + 1;
               }

               return dayOfMonth;
            }
         case LW:
            LocalDate lastDayOfMonth = LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth());
            int dow = lastDayOfMonth.getDayOfWeek().getValue();
            int diff = dow - 5;
            if (diff > 0) {
               return lastDayOfMonth.minusDays(diff).getDayOfMonth();
            }

            return lastDayOfMonth.getDayOfMonth();
         default:
            throw new NoSuchValueException();
      }
   }
}
