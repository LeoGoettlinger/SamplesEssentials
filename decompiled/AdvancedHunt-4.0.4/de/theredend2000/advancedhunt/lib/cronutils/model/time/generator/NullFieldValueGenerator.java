package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import java.util.ArrayList;
import java.util.List;

class NullFieldValueGenerator extends FieldValueGenerator {
   public NullFieldValueGenerator(CronField cronField) {
      super(cronField);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      throw new NoSuchValueException();
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      throw new NoSuchValueException();
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      return new ArrayList<>();
   }

   @Override
   public boolean isMatch(int value) {
      return false;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return true;
   }
}
