package de.theredend2000.advancedhunt.lib.cronutils.model.time;

import de.theredend2000.advancedhunt.lib.cronutils.model.CompositeCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.SingleCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ExecutionTime {
   static ExecutionTime forCron(Cron cron) {
      if (cron instanceof SingleCron) {
         Map<CronFieldName, CronField> fields = cron.retrieveFieldsAsMap();
         ExecutionTimeBuilder executionTimeBuilder = new ExecutionTimeBuilder(cron);

         for (CronFieldName name : CronFieldName.values()) {
            if (fields.get(name) != null) {
               switch (name) {
                  case SECOND:
                     executionTimeBuilder.forSecondsMatching(fields.get(name));
                     break;
                  case MINUTE:
                     executionTimeBuilder.forMinutesMatching(fields.get(name));
                     break;
                  case HOUR:
                     executionTimeBuilder.forHoursMatching(fields.get(name));
                     break;
                  case DAY_OF_WEEK:
                     executionTimeBuilder.forDaysOfWeekMatching(fields.get(name));
                     break;
                  case DAY_OF_MONTH:
                     executionTimeBuilder.forDaysOfMonthMatching(fields.get(name));
                     break;
                  case MONTH:
                     executionTimeBuilder.forMonthsMatching(fields.get(name));
                     break;
                  case YEAR:
                     executionTimeBuilder.forYearsMatching(fields.get(name));
                     break;
                  case DAY_OF_YEAR:
                     executionTimeBuilder.forDaysOfYearMatching(fields.get(name));
               }
            }
         }

         return executionTimeBuilder.build();
      } else {
         return (ExecutionTime)(cron instanceof CompositeCron
            ? new CompositeExecutionTime(((CompositeCron)cron).getCrons().parallelStream().map(ExecutionTime::forCron).collect(Collectors.toList()))
            : new ExecutionTime() {
               @Override
               public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
                  return Optional.empty();
               }

               @Override
               public Optional<Duration> timeToNextExecution(ZonedDateTime date) {
                  return Optional.empty();
               }

               @Override
               public Optional<ZonedDateTime> lastExecution(ZonedDateTime date) {
                  return Optional.empty();
               }

               @Override
               public Optional<Duration> timeFromLastExecution(ZonedDateTime date) {
                  return Optional.empty();
               }

               @Override
               public boolean isMatch(ZonedDateTime date) {
                  return false;
               }
            });
      }
   }

   Optional<ZonedDateTime> nextExecution(ZonedDateTime var1);

   Optional<Duration> timeToNextExecution(ZonedDateTime var1);

   Optional<ZonedDateTime> lastExecution(ZonedDateTime var1);

   Optional<Duration> timeFromLastExecution(ZonedDateTime var1);

   boolean isMatch(ZonedDateTime var1);

   default int countExecutions(ZonedDateTime startDate, ZonedDateTime endDate) {
      return this.getExecutionDates(startDate, endDate).size();
   }

   default List<ZonedDateTime> getExecutionDates(ZonedDateTime startDate, ZonedDateTime endDate) {
      if (!endDate.equals(startDate) && !endDate.isBefore(startDate)) {
         List<ZonedDateTime> executions = new ArrayList<>();
         ZonedDateTime nextExecutionDate = this.nextExecution(startDate).orElse(null);
         if (nextExecutionDate == null) {
            return Collections.emptyList();
         } else {
            while (nextExecutionDate != null && (nextExecutionDate.isBefore(endDate) || nextExecutionDate.equals(endDate))) {
               executions.add(nextExecutionDate);
               nextExecutionDate = this.nextExecution(nextExecutionDate).orElse(null);
            }

            return executions;
         }
      } else {
         throw new IllegalArgumentException("endDate should take place later in time than startDate");
      }
   }
}
