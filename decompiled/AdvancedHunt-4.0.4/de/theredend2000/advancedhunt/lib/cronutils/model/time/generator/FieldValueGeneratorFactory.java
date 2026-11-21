package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;

public class FieldValueGeneratorFactory {
   private FieldValueGeneratorFactory() {
   }

   public static FieldValueGenerator forCronField(CronField cronField) {
      FieldExpression fieldExpression = cronField.getExpression();
      if (fieldExpression instanceof Always) {
         return new AlwaysFieldValueGenerator(cronField);
      } else if (fieldExpression instanceof And) {
         return new AndFieldValueGenerator(cronField);
      } else if (fieldExpression instanceof Between) {
         return new BetweenFieldValueGenerator(cronField);
      } else if (fieldExpression instanceof Every) {
         return new EveryFieldValueGenerator(cronField);
      } else if (fieldExpression instanceof On) {
         On on = (On)fieldExpression;
         if (!SpecialChar.NONE.equals(on.getSpecialChar().getValue())) {
            throw new IllegalArgumentException(String.format("Cannot create instance for On instance with %s value", on.getSpecialChar()));
         } else {
            return new OnFieldValueGenerator(cronField);
         }
      } else {
         return new NullFieldValueGenerator(cronField);
      }
   }

   public static FieldValueGenerator createDayOfYearValueGeneratorInstance(CronField cronField, int year) {
      return forCronField(cronField);
   }

   public static FieldValueGenerator createDayOfMonthValueGeneratorInstance(CronField cronField, int year, int month) {
      FieldExpression fieldExpression = cronField.getExpression();
      if (fieldExpression instanceof On) {
         On on = (On)fieldExpression;
         if (!SpecialChar.NONE.equals(on.getSpecialChar().getValue())) {
            return new OnDayOfMonthValueGenerator(cronField, year, month);
         }
      }

      return forCronField(cronField);
   }

   public static FieldValueGenerator createDayOfWeekValueGeneratorInstance(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
      FieldExpression fieldExpression = cronField.getExpression();
      if (fieldExpression instanceof On) {
         return new OnDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
      } else if (fieldExpression instanceof Between) {
         return new BetweenDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
      } else if (fieldExpression instanceof And) {
         return new AndDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue);
      } else {
         return (FieldValueGenerator)(fieldExpression instanceof Every
            ? new EveryDayOfWeekValueGenerator(cronField, year, month, mondayDoWValue)
            : forCronField(cronField));
      }
   }

   public static FieldValueGenerator createYearValueGeneratorInstance(CronField cronField) {
      return forCronField(cronField);
   }
}
