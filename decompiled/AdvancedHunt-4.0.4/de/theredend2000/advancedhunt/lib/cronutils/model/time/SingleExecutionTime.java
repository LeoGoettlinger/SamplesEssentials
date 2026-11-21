package de.theredend2000.advancedhunt.lib.cronutils.model.time;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraintsBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.generator.FieldValueGenerator;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.generator.FieldValueGeneratorFactory;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.generator.NoSuchValueException;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Predicates;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleExecutionTime implements ExecutionTime {
   private static final int MAX_ITERATIONS = 100000;
   private static final LocalTime MAX_SECONDS = LocalTime.MAX.truncatedTo(ChronoUnit.SECONDS);
   private final CronDefinition cronDefinition;
   private final FieldValueGenerator yearsValueGenerator;
   private final CronField daysOfWeekCronField;
   private final CronField daysOfMonthCronField;
   private final CronField daysOfYearCronField;
   private final TimeNode months;
   private final TimeNode hours;
   private final TimeNode minutes;
   private final TimeNode seconds;

   @VisibleForTesting
   SingleExecutionTime(
      CronDefinition cronDefinition,
      CronField yearsValueCronField,
      CronField daysOfWeekCronField,
      CronField daysOfMonthCronField,
      CronField daysOfYearCronField,
      TimeNode months,
      TimeNode hours,
      TimeNode minutes,
      TimeNode seconds
   ) {
      this.cronDefinition = Preconditions.checkNotNull(cronDefinition);
      if (cronDefinition.containsFieldDefinition(CronFieldName.YEAR)) {
         if (!cronDefinition.getFieldDefinition(CronFieldName.YEAR).isOptional()) {
            Preconditions.checkNotNull(yearsValueCronField);
         }

         this.yearsValueGenerator = yearsValueCronField == null
            ? FieldValueGeneratorFactory.forCronField(
               new CronField(CronFieldName.YEAR, Always.always(), cronDefinition.getFieldDefinition(CronFieldName.YEAR).getConstraints())
            )
            : FieldValueGeneratorFactory.createYearValueGeneratorInstance(yearsValueCronField);
      } else {
         this.yearsValueGenerator = FieldValueGeneratorFactory.forCronField(
            new CronField(CronFieldName.YEAR, Always.always(), FieldConstraintsBuilder.instance().createConstraintsInstance())
         );
      }

      this.daysOfWeekCronField = Preconditions.checkNotNull(daysOfWeekCronField);
      this.daysOfMonthCronField = Preconditions.checkNotNull(daysOfMonthCronField);
      this.daysOfYearCronField = daysOfYearCronField;
      this.months = Preconditions.checkNotNull(months);
      this.hours = Preconditions.checkNotNull(hours);
      this.minutes = Preconditions.checkNotNull(minutes);
      this.seconds = Preconditions.checkNotNull(seconds);
   }

   @Override
   public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
      Preconditions.checkNotNull(date);

      try {
         ZonedDateTime nextMatch = this.nextClosestMatch(date);
         if (nextMatch.equals(date)) {
            nextMatch = this.nextClosestMatch(date.plusSeconds(1L));
            if (nextMatch.getOffset().compareTo(date.getOffset()) > 0) {
               Optional<ZonedDateTime> nextNextExecution = Optional.empty();

               try {
                  nextNextExecution = Optional.of(this.nextClosestMatch(nextMatch.plusSeconds(1L)));
               } catch (NoSuchValueException var5) {
               }

               if (nextNextExecution.isPresent()) {
                  boolean lessFrequentThan1Hour = Duration.between(nextMatch, nextNextExecution.get()).toHours() > 1L;
                  if (lessFrequentThan1Hour) {
                     nextMatch = this.nextClosestMatch(date.plusSeconds(1L).plusHours(1L));
                  }
               }
            }
         }

         return Optional.of(nextMatch);
      } catch (NoSuchValueException var6) {
         return Optional.empty();
      }
   }

   private ZonedDateTime nextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
      SingleExecutionTime.ExecutionTimeResult result = new SingleExecutionTime.ExecutionTimeResult(date, false);

      for (int i = 0; i < 100000; i++) {
         result = this.potentialNextClosestMatch(result.getTime());
         if (result.isMatch()) {
            return result.getTime();
         }

         if (result.getTime().getYear() - date.getYear() > 100) {
            throw new NoSuchValueException();
         }
      }

      throw new NoSuchValueException();
   }

   private SingleExecutionTime.ExecutionTimeResult potentialNextClosestMatch(ZonedDateTime date) throws NoSuchValueException {
      List<Integer> year = this.yearsValueGenerator
         .generateCandidates(date.getYear(), date.getYear())
         .stream()
         .filter(d -> d >= date.getYear())
         .collect(Collectors.toList());
      int lowestMonth = this.months.getValues().get(0);
      int lowestHour = this.hours.getValues().get(0);
      int lowestMinute = this.minutes.getValues().get(0);
      int lowestSecond = this.seconds.getValues().get(0);
      if (year.isEmpty()) {
         return this.getNextPotentialYear(date, lowestMonth, lowestHour, lowestMinute, lowestSecond);
      } else if (!this.months.getValues().contains(date.getMonthValue())) {
         return this.getNextPotentialMonth(date, lowestHour, lowestMinute, lowestSecond);
      } else {
         Optional<TimeNode> optionalDays = this.generateDays(this.cronDefinition, date);
         if (!optionalDays.isPresent()) {
            return new SingleExecutionTime.ExecutionTimeResult(this.toBeginOfNextMonth(date), false);
         } else {
            TimeNode node = optionalDays.get();
            if (!node.getValues().contains(date.getDayOfMonth())) {
               return this.getNextPotentialDayOfMonth(date, lowestHour, lowestMinute, lowestSecond, node);
            } else if (!this.hours.getValues().contains(date.getHour())) {
               return this.getNextPotentialHour(date);
            } else if (!this.minutes.getValues().contains(date.getMinute())) {
               return this.getNextPotentialMinute(date);
            } else {
               return !this.seconds.getValues().contains(date.getSecond())
                  ? this.getNextPotentialSecond(date)
                  : new SingleExecutionTime.ExecutionTimeResult(date, true);
            }
         }
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialYear(ZonedDateTime date, int lowestMonth, int lowestHour, int lowestMinute, int lowestSecond) throws NoSuchValueException {
      int newYear = this.yearsValueGenerator.generateNextValue(date.getYear());
      Optional<TimeNode> optionalDays = this.generateDays(
         this.cronDefinition, ZonedDateTime.of(LocalDate.of(newYear, lowestMonth, 1), LocalTime.MIN, date.getZone())
      );
      if (optionalDays.isPresent()) {
         List<Integer> days = optionalDays.get().getValues();
         return new SingleExecutionTime.ExecutionTimeResult(
            ZonedDateTime.of(LocalDate.of(newYear, lowestMonth, days.get(0)), LocalTime.of(lowestHour, lowestMinute, lowestSecond), date.getZone()), false
         );
      } else {
         return new SingleExecutionTime.ExecutionTimeResult(this.toBeginOfNextMonth(date), false);
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialMonth(ZonedDateTime date, int lowestHour, int lowestMinute, int lowestSecond) {
      NearestValue nearestValue = this.months.getNextValue(date.getMonthValue(), 0);
      int nextMonths = nearestValue.getValue();
      if (nearestValue.getShifts() > 0) {
         return new SingleExecutionTime.ExecutionTimeResult(
            date.truncatedTo(ChronoUnit.DAYS).withMonth(1).withDayOfMonth(1).plusYears(nearestValue.getShifts()), false
         );
      } else {
         Optional<TimeNode> optionalDays = this.generateDays(
            this.cronDefinition, ZonedDateTime.of(LocalDateTime.of(date.getYear(), nextMonths, 1, 0, 0), date.getZone())
         );
         if (optionalDays.isPresent()) {
            List<Integer> days = optionalDays.get().getValues();
            return new SingleExecutionTime.ExecutionTimeResult(
               date.truncatedTo(ChronoUnit.SECONDS)
                  .withMonth(nextMonths)
                  .withDayOfMonth(days.get(0))
                  .with(LocalTime.of(lowestHour, lowestMinute, lowestSecond)),
               false
            );
         } else {
            return new SingleExecutionTime.ExecutionTimeResult(this.toBeginOfNextMonth(date), false);
         }
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialDayOfMonth(
      ZonedDateTime date, int lowestHour, int lowestMinute, int lowestSecond, TimeNode node
   ) {
      NearestValue nearestValue = node.getNextValue(date.getDayOfMonth(), 0);
      return nearestValue.getShifts() > 0
         ? new SingleExecutionTime.ExecutionTimeResult(date.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).plusMonths(nearestValue.getShifts()), false)
         : new SingleExecutionTime.ExecutionTimeResult(
            date.truncatedTo(ChronoUnit.SECONDS).withDayOfMonth(nearestValue.getValue()).with(LocalTime.of(lowestHour, lowestMinute, lowestSecond)), false
         );
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialHour(ZonedDateTime date) throws NoSuchValueException {
      return getNextPotentialValue(date, this.hours, ChronoField.HOUR_OF_DAY);
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialMinute(ZonedDateTime date) throws NoSuchValueException {
      return getNextPotentialValue(date, this.minutes, ChronoField.MINUTE_OF_HOUR);
   }

   private SingleExecutionTime.ExecutionTimeResult getNextPotentialSecond(ZonedDateTime date) throws NoSuchValueException {
      return getNextPotentialValue(date, this.seconds, ChronoField.SECOND_OF_MINUTE);
   }

   private static SingleExecutionTime.ExecutionTimeResult getNextPotentialValue(ZonedDateTime date, TimeNode node, TemporalField field) throws NoSuchValueException {
      Set<Integer> values = new HashSet<>(node.values);
      TemporalUnit unit = field.getBaseUnit();
      long maximum = field.range().getMaximum();
      long minimum = field.range().getMinimum();
      long range = maximum - minimum;
      ZonedDateTime newDate = date;

      for (long i = 0L; i < 2L * range; i++) {
         newDate = newDate.plus(1L, unit);
         if (values.contains(newDate.get(field))) {
            newDate = newDate.truncatedTo(unit);
            return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
         }
      }

      throw new NoSuchValueException();
   }

   private ZonedDateTime toBeginOfNextMonth(ZonedDateTime datetime) {
      return datetime.truncatedTo(ChronoUnit.DAYS).plusMonths(1L).withDayOfMonth(1);
   }

   private ZonedDateTime previousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
      SingleExecutionTime.ExecutionTimeResult result = new SingleExecutionTime.ExecutionTimeResult(date, false);

      for (int i = 0; i < 100000; i++) {
         result = this.potentialPreviousClosestMatch(result.getTime());
         if (result.isMatch()) {
            return result.getTime();
         }
      }

      throw new NoSuchValueException();
   }

   private SingleExecutionTime.ExecutionTimeResult potentialPreviousClosestMatch(ZonedDateTime date) throws NoSuchValueException {
      List<Integer> year = this.yearsValueGenerator.generateCandidates(date.getYear(), date.getYear());
      Optional<TimeNode> optionalDays = this.generateDays(this.cronDefinition, date);
      if (optionalDays.isPresent() && optionalDays.get().getValues().stream().anyMatch(i -> i <= date.getDayOfMonth())) {
         TimeNode days = optionalDays.get();
         int highestMonth = this.months.getValues().get(this.months.getValues().size() - 1);
         int highestDay = days.getValues().get(days.getValues().size() - 1);
         int highestHour = this.hours.getValues().get(this.hours.getValues().size() - 1);
         int highestMinute = this.minutes.getValues().get(this.minutes.getValues().size() - 1);
         int highestSecond = this.seconds.getValues().get(this.seconds.getValues().size() - 1);
         if (year.isEmpty()) {
            return this.getPreviousPotentialYear(date, days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
         } else if (!year.contains(date.getYear())) {
            Optional<Integer> validprevyear = year.stream().filter(y -> y < date.getYear()).max(Integer::compareTo);
            if (validprevyear.isPresent()) {
               int minus = date.getYear() - validprevyear.get();
               return this.getPreviousPotentialYear(date.minusYears(minus), days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
            } else {
               return this.getPreviousPotentialYear(date.minusYears(1L), days, highestMonth, highestDay, highestHour, highestMinute, highestSecond);
            }
         } else if (!this.months.getValues().contains(date.getMonthValue())) {
            return this.getPreviousPotentialMonth(date, highestDay, highestHour, highestMinute, highestSecond);
         } else if (!days.getValues().contains(date.getDayOfMonth())) {
            return this.getPreviousPotentialDayOfMonth(date, days, highestHour, highestMinute, highestSecond);
         } else if (!this.hours.getValues().contains(date.getHour())) {
            return this.getPreviousPotentialHour(date);
         } else if (!this.minutes.getValues().contains(date.getMinute())) {
            return this.getPreviousPotentialMinute(date);
         } else {
            return !this.seconds.getValues().contains(date.getSecond())
               ? this.getPreviousPotentialSecond(date)
               : new SingleExecutionTime.ExecutionTimeResult(date.truncatedTo(ChronoUnit.SECONDS), true);
         }
      } else {
         return new SingleExecutionTime.ExecutionTimeResult(this.toEndOfPreviousMonth(date), false);
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialYear(
      ZonedDateTime date, TimeNode days, int highestMonth, int highestDay, int highestHour, int highestMinute, int highestSecond
   ) throws NoSuchValueException {
      int previousYear = this.yearsValueGenerator.generatePreviousValue(date.getYear());
      if (highestDay > 28) {
         int highestDayOfMonth = LocalDate.of(previousYear, highestMonth, 1).lengthOfMonth();
         if (highestDay > highestDayOfMonth) {
            NearestValue nearestValue = days.getPreviousValue(highestDay, 1);
            if (nearestValue.getShifts() > 0) {
               ZonedDateTime newDate = ZonedDateTime.of(LocalDate.of(previousYear, highestMonth, 1), MAX_SECONDS, date.getZone())
                  .minusMonths(nearestValue.getShifts())
                  .with(TemporalAdjusters.lastDayOfMonth());
               return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
            }

            highestDay = nearestValue.getValue();
         }
      }

      return new SingleExecutionTime.ExecutionTimeResult(
         ZonedDateTime.of(LocalDate.of(previousYear, highestMonth, highestDay), LocalTime.of(highestHour, highestMinute, highestSecond), date.getZone()), false
      );
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialMonth(
      ZonedDateTime date, int highestDay, int highestHour, int highestMinute, int highestSecond
   ) {
      NearestValue nearestValue = this.months.getPreviousValue(date.getMonthValue(), 0);
      int previousMonths = nearestValue.getValue();
      if (nearestValue.getShifts() > 0) {
         ZonedDateTime newDate = ZonedDateTime.of(LocalDate.of(date.getYear(), 12, 31), MAX_SECONDS, date.getZone()).minusYears(nearestValue.getShifts());
         return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
      } else {
         ZonedDateTime newDate = ZonedDateTime.of(date.getYear(), date.getMonthValue(), 1, 0, 0, 0, 0, date.getZone()).minusNanos(1L);
         return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialDayOfMonth(
      ZonedDateTime date, TimeNode days, int highestHour, int highestMinute, int highestSecond
   ) {
      NearestValue nearestValue = days.getPreviousValue(date.getDayOfMonth(), 0);
      if (nearestValue.getShifts() > 0) {
         ZonedDateTime newDate = ZonedDateTime.of(LocalDate.of(date.getYear(), date.getMonthValue(), 1), MAX_SECONDS, date.getZone())
            .minusMonths(nearestValue.getShifts())
            .with(TemporalAdjusters.lastDayOfMonth());
         return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
      } else {
         return new SingleExecutionTime.ExecutionTimeResult(
            date.withDayOfMonth(nearestValue.getValue()).with(LocalTime.of(highestHour, highestMinute, highestSecond)).truncatedTo(ChronoUnit.SECONDS), false
         );
      }
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialHour(ZonedDateTime date) throws NoSuchValueException {
      return getPreviousPotentialValue(date, this.hours, ChronoField.HOUR_OF_DAY);
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialMinute(ZonedDateTime date) throws NoSuchValueException {
      return getPreviousPotentialValue(date, this.minutes, ChronoField.MINUTE_OF_HOUR);
   }

   private SingleExecutionTime.ExecutionTimeResult getPreviousPotentialSecond(ZonedDateTime date) throws NoSuchValueException {
      return getPreviousPotentialValue(date, this.seconds, ChronoField.SECOND_OF_MINUTE);
   }

   private static SingleExecutionTime.ExecutionTimeResult getPreviousPotentialValue(ZonedDateTime date, TimeNode node, TemporalField field) throws NoSuchValueException {
      Set<Integer> values = new HashSet<>(node.values);
      TemporalUnit unit = field.getBaseUnit();
      long maximum = field.range().getMaximum();
      long minimum = field.range().getMinimum();
      long range = maximum - minimum;
      ZonedDateTime newDate = date;

      for (long i = 0L; i < 2L * range; i++) {
         newDate = newDate.minus(1L, unit);
         if (values.contains(newDate.get(field))) {
            newDate = newDate.truncatedTo(unit).plus(1L, unit).minusSeconds(1L);
            return new SingleExecutionTime.ExecutionTimeResult(newDate, false);
         }
      }

      throw new NoSuchValueException();
   }

   private ZonedDateTime toEndOfPreviousMonth(ZonedDateTime datetime) {
      ZonedDateTime previousMonth = datetime.minusMonths(1L).with(TemporalAdjusters.lastDayOfMonth());
      int highestHour = this.hours.getValues().get(this.hours.getValues().size() - 1);
      int highestMinute = this.minutes.getValues().get(this.minutes.getValues().size() - 1);
      int highestSecond = this.seconds.getValues().get(this.seconds.getValues().size() - 1);
      return ZonedDateTime.of(
         previousMonth.getYear(),
         previousMonth.getMonth().getValue(),
         previousMonth.getDayOfMonth(),
         highestHour,
         highestMinute,
         highestSecond,
         0,
         previousMonth.getZone()
      );
   }

   private Optional<TimeNode> generateDays(CronDefinition cronDefinition, ZonedDateTime date) {
      if (this.isGenerateDaysAsDoY(cronDefinition)) {
         return this.generateDayCandidatesUsingDoY(date);
      } else if (cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK) != null && cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_MONTH) != null) {
         return this.generateDaysDoWAndDoMSupported(cronDefinition, date);
      } else {
         return cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK) == null
            ? this.generateDayCandidatesUsingDoM(date)
            : Optional.of(
               this.generateDayCandidatesUsingDoW(
                  date, ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue()
               )
            );
      }
   }

   private boolean isGenerateDaysAsDoY(CronDefinition cronDefinition) {
      if (!cronDefinition.containsFieldDefinition(CronFieldName.DAY_OF_YEAR)) {
         return false;
      } else {
         return !cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_YEAR).getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)
            ? true
            : !(this.daysOfYearCronField.getExpression() instanceof QuestionMark);
      }
   }

   private Optional<TimeNode> generateDayCandidatesUsingDoY(ZonedDateTime reference) {
      int year = reference.getYear();
      int month = reference.getMonthValue();
      LocalDate date = LocalDate.of(year, 1, 1);
      int lengthOfYear = date.lengthOfYear();
      List<Integer> candidates = FieldValueGeneratorFactory.createDayOfYearValueGeneratorInstance(this.daysOfYearCronField, year)
         .generateCandidates(1, lengthOfYear);
      int low = LocalDate.of(year, month, 1).getDayOfYear();
      int high = month == 12 ? LocalDate.of(year, 12, 31).getDayOfYear() + 1 : LocalDate.of(year, month + 1, 1).getDayOfYear();
      List<Integer> collectedCandidates = candidates.stream()
         .filter(dayOfYear -> dayOfYear >= low && dayOfYear < high)
         .map(dayOfYear -> LocalDate.ofYearDay(reference.getYear(), dayOfYear).getDayOfMonth())
         .collect(Collectors.toList());
      return Optional.of(collectedCandidates).filter(Predicates.not(List::isEmpty)).map(TimeNode::new);
   }

   private Optional<TimeNode> generateDaysDoWAndDoMSupported(CronDefinition cronDefinition, ZonedDateTime date) {
      boolean questionMarkSupported = cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)
         .getConstraints()
         .getSpecialChars()
         .contains(SpecialChar.QUESTION_MARK);
      if (questionMarkSupported) {
         List<Integer> candidates = this.generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(
            date.getYear(), date.getMonthValue(), ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue()
         );
         return Optional.of(candidates).filter(Predicates.not(List::isEmpty)).map(TimeNode::new);
      } else {
         List<Integer> candidates = this.generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(
            date.getYear(), date.getMonthValue(), ((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue()
         );
         return Optional.of(candidates).filter(Predicates.not(List::isEmpty)).map(TimeNode::new);
      }
   }

   @Override
   public Optional<Duration> timeToNextExecution(ZonedDateTime date) {
      Optional<ZonedDateTime> next = this.nextExecution(date);
      return next.map(zonedDateTime -> Duration.between(date, zonedDateTime));
   }

   @Override
   public Optional<ZonedDateTime> lastExecution(ZonedDateTime date) {
      Preconditions.checkNotNull(date);

      try {
         ZonedDateTime previousMatch = this.previousClosestMatch(date);
         if (previousMatch.equals(date)) {
            previousMatch = this.previousClosestMatch(date.minusSeconds(1L));
         }

         return Optional.of(previousMatch);
      } catch (NoSuchValueException var3) {
         return Optional.empty();
      }
   }

   @Override
   public Optional<Duration> timeFromLastExecution(ZonedDateTime date) {
      return this.lastExecution(date).map(zonedDateTime -> Duration.between(zonedDateTime, date));
   }

   @Override
   public boolean isMatch(ZonedDateTime date) {
      boolean isSecondGranularity = this.cronDefinition.containsFieldDefinition(CronFieldName.SECOND);
      if (isSecondGranularity) {
         date = date.truncatedTo(ChronoUnit.SECONDS);
      } else {
         date = date.truncatedTo(ChronoUnit.MINUTES);
      }

      Optional<ZonedDateTime> last = this.lastExecution(date);
      if (last.isPresent()) {
         Optional<ZonedDateTime> next = this.nextExecution(last.get());
         if (next.isPresent()) {
            return next.get().equals(date);
         } else {
            boolean everythingInRange = false;

            try {
               everythingInRange = this.dateValuesInExpectedRanges(this.nextClosestMatch(date), date);
            } catch (NoSuchValueException var8) {
            }

            try {
               everythingInRange = this.dateValuesInExpectedRanges(this.previousClosestMatch(date), date);
            } catch (NoSuchValueException var7) {
            }

            return everythingInRange;
         }
      } else {
         try {
            return this.dateValuesInExpectedRanges(this.nextClosestMatch(date.minusSeconds(1L)), date);
         } catch (NoSuchValueException var9) {
            return false;
         }
      }
   }

   private boolean dateValuesInExpectedRanges(ZonedDateTime validCronDate, ZonedDateTime date) {
      boolean everythingInRange = true;
      if (this.cronDefinition.getFieldDefinition(CronFieldName.YEAR) != null) {
         everythingInRange = validCronDate.getYear() == date.getYear();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.MONTH) != null) {
         everythingInRange = everythingInRange && validCronDate.getMonthValue() == date.getMonthValue();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_MONTH) != null) {
         everythingInRange = everythingInRange && validCronDate.getDayOfMonth() == date.getDayOfMonth();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK) != null) {
         everythingInRange = everythingInRange && validCronDate.getDayOfWeek().getValue() == date.getDayOfWeek().getValue();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.HOUR) != null) {
         everythingInRange = everythingInRange && validCronDate.getHour() == date.getHour();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.MINUTE) != null) {
         everythingInRange = everythingInRange && validCronDate.getMinute() == date.getMinute();
      }

      if (this.cronDefinition.getFieldDefinition(CronFieldName.SECOND) != null) {
         everythingInRange = everythingInRange && validCronDate.getSecond() == date.getSecond();
      }

      return everythingInRange;
   }

   private List<Integer> generateDayCandidatesQuestionMarkNotSupportedUsingDoWAndDoM(int year, int month, WeekDay mondayDoWValue) {
      LocalDate date = LocalDate.of(year, month, 1);
      int lengthOfMonth = date.lengthOfMonth();
      if (this.daysOfMonthCronField.getExpression() instanceof Always && this.daysOfWeekCronField.getExpression() instanceof Always) {
         return FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else if (this.daysOfMonthCronField.getExpression() instanceof Always) {
         return FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(this.daysOfWeekCronField, year, month, mondayDoWValue)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else if (this.daysOfWeekCronField.getExpression() instanceof Always) {
         return FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else {
         List<Integer> dayOfWeekCandidates = FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(
               this.daysOfWeekCronField, year, month, mondayDoWValue
            )
            .generateCandidates(1, lengthOfMonth);
         List<Integer> dayOfMonthCandidates = FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month)
            .generateCandidates(1, lengthOfMonth);
         if (this.cronDefinition.isMatchDayOfWeekAndDayOfMonth()) {
            Set<Integer> intersection = new HashSet<>(dayOfWeekCandidates);
            return dayOfMonthCandidates.stream().filter(intersection::contains).distinct().sorted().collect(Collectors.toList());
         } else {
            return Stream.concat(dayOfWeekCandidates.stream(), dayOfMonthCandidates.stream()).distinct().sorted().collect(Collectors.toList());
         }
      }
   }

   private List<Integer> generateDayCandidatesQuestionMarkSupportedUsingDoWAndDoM(int year, int month, WeekDay mondayDoWValue) {
      LocalDate date = LocalDate.of(year, month, 1);
      int lengthOfMonth = date.lengthOfMonth();
      if (this.daysOfMonthCronField.getExpression() instanceof Always && this.daysOfWeekCronField.getExpression() instanceof Always) {
         return FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else if (this.daysOfMonthCronField.getExpression() instanceof QuestionMark) {
         return FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(this.daysOfWeekCronField, year, month, mondayDoWValue)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else if (this.daysOfWeekCronField.getExpression() instanceof QuestionMark) {
         return FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month)
            .generateCandidates(1, lengthOfMonth)
            .stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      } else {
         Set<Integer> candidates = new HashSet<>(
            FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(this.daysOfMonthCronField, year, month).generateCandidates(1, lengthOfMonth)
         );
         Set<Integer> daysOfWeek = new HashSet<>(
            FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(this.daysOfWeekCronField, year, month, mondayDoWValue)
               .generateCandidates(1, lengthOfMonth)
         );
         candidates.retainAll(daysOfWeek);
         return candidates.stream().sorted().collect(Collectors.toList());
      }
   }

   private Optional<TimeNode> generateDayCandidatesUsingDoM(ZonedDateTime reference) {
      LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
      int lengthOfMonth = date.lengthOfMonth();
      List<Integer> candidates = FieldValueGeneratorFactory.createDayOfMonthValueGeneratorInstance(
            this.daysOfMonthCronField, reference.getYear(), reference.getMonthValue()
         )
         .generateCandidates(1, lengthOfMonth)
         .stream()
         .distinct()
         .sorted()
         .collect(Collectors.toList());
      return candidates.isEmpty() ? Optional.empty() : Optional.of(new TimeNode(candidates));
   }

   private TimeNode generateDayCandidatesUsingDoW(ZonedDateTime reference, WeekDay mondayDoWValue) {
      LocalDate date = LocalDate.of(reference.getYear(), reference.getMonthValue(), 1);
      int lengthOfMonth = date.lengthOfMonth();
      List<Integer> candidates = FieldValueGeneratorFactory.createDayOfWeekValueGeneratorInstance(
            this.daysOfWeekCronField, reference.getYear(), reference.getMonthValue(), mondayDoWValue
         )
         .generateCandidates(1, lengthOfMonth)
         .stream()
         .distinct()
         .sorted()
         .collect(Collectors.toList());
      return new TimeNode(candidates);
   }

   private static final class ExecutionTimeResult {
      private final ZonedDateTime time;
      private final boolean isMatch;

      private ExecutionTimeResult(ZonedDateTime time, boolean isMatch) {
         this.time = time;
         this.isMatch = isMatch;
      }

      public ZonedDateTime getTime() {
         return this.time;
      }

      public boolean isMatch() {
         return this.isMatch;
      }

      @Override
      public String toString() {
         return "ExecutionTimeResult{time=" + this.time + ", isMatch=" + this.isMatch + '}';
      }
   }
}
