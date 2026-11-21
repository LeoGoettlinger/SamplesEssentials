package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AndFieldValueGenerator extends FieldValueGenerator {
   private static final Logger log = LoggerFactory.getLogger(AndFieldValueGenerator.class);

   public AndFieldValueGenerator(CronField cronField) {
      super(cronField);
   }

   @Override
   public int generateNextValue(int reference) throws NoSuchValueException {
      List<Integer> candidates = this.computeCandidates(fieldValueGenerator -> {
         try {
            return fieldValueGenerator.generateNextValue(reference);
         } catch (NoSuchValueException var3) {
            return Integer.MIN_VALUE;
         }
      });
      if (candidates.isEmpty()) {
         throw new NoSuchValueException();
      } else {
         return candidates.get(0);
      }
   }

   @Override
   public int generatePreviousValue(int reference) throws NoSuchValueException {
      List<Integer> candidates = this.computeCandidates(candidateGenerator -> {
         try {
            return candidateGenerator.generatePreviousValue(reference);
         } catch (NoSuchValueException var3) {
            return Integer.MIN_VALUE;
         }
      });
      if (candidates.isEmpty()) {
         throw new NoSuchValueException();
      } else {
         return candidates.get(candidates.size() - 1);
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();

      try {
         for (int reference = this.generateNextValue(start); reference < end; reference = this.generateNextValue(reference)) {
            values.add(reference);
         }
      } catch (NoSuchValueException var5) {
         log.debug("Catched expected exception while generating candidates", (Throwable)var5);
      }

      return values;
   }

   @Override
   public boolean isMatch(int value) {
      And and = (And)this.cronField.getExpression();
      boolean match = false;

      for (FieldExpression expression : and.getExpressions()) {
         match = match
            || this.createCandidateGeneratorInstance(new CronField(this.cronField.getField(), expression, this.cronField.getConstraints())).isMatch(value);
      }

      return match;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof And;
   }

   private List<Integer> computeCandidates(Function<FieldValueGenerator, Integer> function) {
      And and = (And)this.cronField.getExpression();
      List<Integer> candidates = new ArrayList<>();

      for (FieldExpression expression : and.getExpressions()) {
         candidates.add(
            function.apply(this.createCandidateGeneratorInstance(new CronField(this.cronField.getField(), expression, this.cronField.getConstraints())))
         );
      }

      List<Integer> filteredCandidates = new ArrayList<>();

      for (Integer candidate : candidates) {
         if (candidate >= 0) {
            filteredCandidates.add(candidate);
         }
      }

      Collections.sort(filteredCandidates);
      return filteredCandidates;
   }

   private FieldValueGenerator createCandidateGeneratorInstance(CronField cronField) {
      FieldExpression expression = cronField.getExpression();
      if (expression instanceof Always) {
         return new AlwaysFieldValueGenerator(cronField);
      } else if (expression instanceof Between) {
         return new BetweenFieldValueGenerator(cronField);
      } else if (expression instanceof Every) {
         return new EveryFieldValueGenerator(cronField);
      } else if (expression instanceof On) {
         return new OnFieldValueGenerator(cronField);
      } else {
         throw new IllegalArgumentException(String.format("FieldExpression %s not supported!", expression.getClass()));
      }
   }
}
