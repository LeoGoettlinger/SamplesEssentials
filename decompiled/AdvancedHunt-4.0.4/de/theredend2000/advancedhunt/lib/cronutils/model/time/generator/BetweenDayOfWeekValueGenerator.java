package de.theredend2000.advancedhunt.lib.cronutils.model.time.generator;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraintsBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParserField;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

class BetweenDayOfWeekValueGenerator extends FieldValueGenerator {
   private final int year;
   private final int month;
   private final WeekDay mondayDoWValue;
   private final Set<Integer> dowValidValues;

   public BetweenDayOfWeekValueGenerator(CronField cronField, int year, int month, WeekDay mondayDoWValue) {
      super(cronField);
      Preconditions.checkArgument(CronFieldName.DAY_OF_WEEK.equals(cronField.getField()), "CronField does not belong to day of week");
      this.year = year;
      this.month = month;
      this.mondayDoWValue = mondayDoWValue;
      this.dowValidValues = new HashSet<>();
      Between between = (Between)cronField.getExpression();
      int from = (Integer)between.getFrom().getValue();
      int to = (Integer)between.getTo().getValue();
      if (to >= from) {
         IntStream.rangeClosed(from, to).forEach(this.dowValidValues::add);
      } else {
         IntStream.rangeClosed(from, cronField.getConstraints().getEndRange()).forEach(this.dowValidValues::add);
         IntStream.rangeClosed(cronField.getConstraints().getStartRange(), to).forEach(this.dowValidValues::add);
      }
   }

   @Override
   protected List<Integer> generateCandidatesNotIncludingIntervalExtremes(int start, int end) {
      List<Integer> values = new ArrayList<>();

      for (Integer dayOfWeek : this.dowValidValues) {
         FieldConstraintsBuilder fcb = FieldConstraintsBuilder.instance().forField(CronFieldName.DAY_OF_WEEK);
         CronParserField parser = new CronParserField(CronFieldName.DAY_OF_WEEK, fcb.createConstraintsInstance());
         CronField cronField = parser.parse(dayOfWeek.toString());
         OnDayOfWeekValueGenerator odow = new OnDayOfWeekValueGenerator(cronField, this.year, this.month, this.mondayDoWValue);
         List<Integer> candidatesList = odow.generateCandidates(start, end);
         if (candidatesList != null) {
            values.addAll(candidatesList);
         }
      }

      Collections.sort(values);
      return values;
   }

   @Override
   protected boolean matchesFieldExpressionClass(FieldExpression fieldExpression) {
      return fieldExpression instanceof Between;
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
      int localDateDoW = LocalDate.of(this.year, this.month, value).getDayOfWeek().getValue();
      int cronDoW = localDateDoW % 7 + (this.mondayDoWValue.getMondayDoWValue() - 1);
      return this.dowValidValues.contains(cronDoW);
   }
}
