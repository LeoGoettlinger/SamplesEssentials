package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import java.util.ArrayList;
import java.util.List;

class BetweenFieldValueGenerator extends FieldValueGenerator {
   public BetweenFieldValueGenerator(CronField cronField) {
      super(cronField);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      Between between = (Between)this.cronField.getExpression();
      int candidate = reference;

      while (++candidate < map(between.getFrom())) {
      }

      if (candidate > map(between.getTo())) {
         throw new NoSuchValueException();
      } else {
         return candidate;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      Between between = (Between)this.cronField.getExpression();
      int candidate = reference;

      while (--candidate > map(between.getTo())) {
      }

      if (candidate < map(between.getFrom())) {
         throw new NoSuchValueException();
      } else {
         return candidate;
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();
      Between between = (Between)this.cronField.getExpression();
      int expressionStart = map(between.getFrom());
      int expressionEnd = map(between.getTo());
      int rangestart = start;
      int rangeend = end;
      if (start <= expressionEnd && expressionStart <= end) {
         if (expressionEnd < end) {
            rangeend = expressionEnd;
         }

         if (map(between.getFrom()) > start) {
            rangestart = expressionStart;
         }

         try {
            if (rangestart != start) {
               values.add(rangestart);
            }

            int reference;
            for (reference = this.generateNextValue(rangestart); reference < rangeend; reference = this.generateNextValue(reference)) {
               values.add(reference);
            }

            if (rangeend != end) {
               values.add(reference);
            }
         } catch (NoSuchValueException var10) {
         }
      }

      return values;
   }

   @Override
   public boolean isMatch(int value) {
      Between between = (Between)this.cronField.getExpression();
      return value >= map(between.getFrom()) && value <= map(between.getTo());
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof Between;
   }

   static int map(FieldValue<?> fieldValue) {
      if (fieldValue instanceof IntegerFieldValue) {
         return ((IntegerFieldValue)fieldValue).getValue();
      } else {
         throw new IllegalArgumentException("Non integer values at intervals are not fully supported yet.");
      }
   }
}
