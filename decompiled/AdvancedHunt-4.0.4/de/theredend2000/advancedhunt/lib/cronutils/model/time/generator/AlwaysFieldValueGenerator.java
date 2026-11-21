package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import java.util.ArrayList;
import java.util.List;

class AlwaysFieldValueGenerator extends FieldValueGenerator {
   public AlwaysFieldValueGenerator(CronField cronField) {
      super(cronField);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      int newvalue = reference + 1;
      if (newvalue <= this.cronField.getConstraints().getEndRange()) {
         return newvalue;
      } else {
         throw new NoSuchValueException();
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      int newvalue = reference - 1;
      if (newvalue >= this.cronField.getConstraints().getStartRange()) {
         return newvalue;
      } else {
         throw new NoSuchValueException();
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();

      for (int j = start + 1; j < end; j++) {
         values.add(j);
      }

      return values;
   }

   @Override
   public boolean isMatch(int value) {
      return this.cronField.getField() == CronFieldName.DAY_OF_WEEK || this.cronField.getConstraints().isInRange(value);
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof Always;
   }
}
