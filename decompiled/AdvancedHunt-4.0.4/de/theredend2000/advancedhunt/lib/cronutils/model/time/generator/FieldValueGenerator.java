package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class FieldValueGenerator {
   protected static final int NO_VALUE = Integer.MIN_VALUE;
   protected CronField cronField;

   public FieldValueGenerator(CronField cronField) {
      this.cronField = Preconditions.checkNotNull(cronField, "CronField must not be null");
      Preconditions.checkArgument(this.matchesFieldExpressionClass(cronField.getExpression()), "FieldExpression does not match required class");
   }

   public abstract int generateNextValue(int var1) throws NoSuchValueException;

   public abstract int generatePreviousValue(int var1) throws NoSuchValueException;

   protected abstract List<Integer> generateCandidatesNotIncludingIntervalExtremes(int var1, int var2);

   public abstract boolean isMatch(int var1);

   public final List<Integer> generateCandidates(int start, int end) {
      List<Integer> candidates = this.generateCandidatesNotIncludingIntervalExtremes(start, end);
      if (this.isMatch(start)) {
         candidates.add(start);
      }

      if (this.isMatch(end)) {
         candidates.add(end);
      }

      List<Integer> var4 = new LinkedList<>(new HashSet<>(candidates));
      Collections.sort(var4);
      return var4;
   }

   protected abstract boolean matchesFieldExpressionClass(FieldExpression var1);
}
