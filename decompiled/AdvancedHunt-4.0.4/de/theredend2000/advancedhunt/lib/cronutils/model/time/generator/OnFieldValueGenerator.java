package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import java.util.ArrayList;
import java.util.List;

class OnFieldValueGenerator extends FieldValueGenerator {
   public OnFieldValueGenerator(CronField cronField) {
      super(cronField);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      int time = ((On)this.cronField.getExpression()).getTime().getValue();
      if (time <= reference) {
         throw new NoSuchValueException();
      } else {
         return time;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      int time = ((On)this.cronField.getExpression()).getTime().getValue();
      if (time >= reference) {
         throw new NoSuchValueException();
      } else {
         return time;
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();
      int time = ((On)this.cronField.getExpression()).getTime().getValue();
      if (time > start && time < end) {
         values.add(time);
      }

      return values;
   }

   @Override
   public boolean isMatch(int value) {
      return ((On)this.cronField.getExpression()).getTime().getValue() == value;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof On;
   }
}
