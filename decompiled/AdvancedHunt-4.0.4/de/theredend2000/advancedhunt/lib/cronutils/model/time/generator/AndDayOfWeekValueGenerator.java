package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.ArrayList;
import java.util.List;

class AndDayOfWeekValueGenerator extends FieldValueGenerator {
   private final int year;
   private final int month;
   private final WeekDay mondayDoWValue;

   public AndDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
      super(cronField);
      Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
      this.year = year;
      this.month = month;
      this.mondayDoWValue = mondayDoWValue;
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();
      And and = (And)this.cronField.getExpression();

      for (FieldExpression expression : and.getExpressions()) {
         CronField cronField = new CronField(CronFieldName.DAY_OF_WEEK, expression, this.cronField.getConstraints());
         List<Integer> candidatesList = FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(cronField, this.year, this.month, this.mondayDoWValue)
            .generateCandidates(start, end);
         if (candidatesList != null) {
            values.addAll(candidatesList);
         }
      }

      return values;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof And;
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      return 0;
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      return 0;
   }

   @Override
   public boolean isMatch(int value) {
      return false;
   }
}
