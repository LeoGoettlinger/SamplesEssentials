package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

class EveryFieldValueGenerator extends FieldValueGenerator {
   protected final int from;
   protected final int to;

   public EveryFieldValueGenerator(CronField cronField) {
      super(cronField);
      Every every = (Every)cronField.getExpression();
      FieldExpression everyExpression = every.getExpression();
      if (everyExpression instanceof Between) {
         Between between = (Between)everyExpression;
         this.from = Math.max(cronField.getConstraints().getStartRange(), BetweenFieldValueGenerator.map(between.getFrom()));
         this.to = Math.min(cronField.getConstraints().getEndRange(), BetweenFieldValueGenerator.map(between.getTo()));
      } else if (everyExpression instanceof On) {
         On on = (On)everyExpression;
         this.from = on.getTime().getValue();
         this.to = cronField.getConstraints().getEndRange();
      } else {
         this.from = cronField.getConstraints().getStartRange();
         this.to = cronField.getConstraints().getEndRange();
      }
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      if (reference >= this.to) {
         throw new NoSuchValueException();
      } else {
         Every every = (Every)this.cronField.getExpression();
         int next = this.getNext(reference, every);
         if (next < this.from) {
            return this.from;
         } else if (next > this.to) {
            throw new NoSuchValueException();
         } else {
            return next;
         }
      }
   }

   private int getNext(int reference, Every every) {
      int offset = this.offset();
      if (reference >= offset) {
         int referenceWithoutOffset = reference - offset;
         int period = every.getPeriod().getValue();
         int remainder = referenceWithoutOffset % period;
         return reference + (period - remainder);
      } else {
         return offset;
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      Every every = (Every)this.cronField.getExpression();
      if (reference < this.from) {
         throw new NoSuchValueException();
      } else if (reference > this.to) {
         return this.to;
      } else {
         int period = every.getPeriod().getValue();
         int remainder = (reference - this.from) % period;
         return remainder == 0 ? reference - period : reference - remainder;
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();

      try {
         int offset = this.offset();
         if (start < offset && offset < end) {
            values.add(offset);
         }

         for (int reference = this.generateNextValue(start); reference < end; reference = this.generateNextValue(reference)) {
            if (reference != offset) {
               values.add(reference);
            }
         }
      } catch (NoSuchValueException var6) {
      }

      return values;
   }

   @Override
   public boolean isMatch(int value) {
      Every every = (Every)this.cronField.getExpression();
      int start = this.offset();
      return value >= start && (value - start) % every.getPeriod().getValue() == 0 && value >= this.from && value <= this.to;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof Every;
   }

   @VisibleForTesting
   int offset() {
      FieldExpression expression = ((Every)this.cronField.getExpression()).getExpression();
      return expression instanceof On ? ((On)expression).getTime().getValue() : this.from;
   }
}
