package de.theredend2000.advancedhunt.lib.cronutils.model.time;

import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CompositeExecutionTime implements ExecutionTime {
   private List<ExecutionTime> executionTimes;

   public CompositeExecutionTime(List<ExecutionTime> executionTimes) {
      Preconditions.checkNotNullNorEmpty(executionTimes, "ExecutionTime list cannot be null or empty");
      this.executionTimes = Collections.unmodifiableList(executionTimes);
   }

   @Override
   public Optional<ZonedDateTime> nextExecution(ZonedDateTime date) {
      Optional<Optional<ZonedDateTime>> next = this.executionTimes
         .parallelStream()
         .map(e -> e.nextExecution(date))
         .filter(Optional::isPresent)
         .sorted((o1, o2) -> {
            if (o1.isPresent() && o2.isPresent()) {
               ZonedDateTime first = o1.get();
               ZonedDateTime second = o2.get();
               return first.compareTo(second);
            } else {
               return 0;
            }
         })
         .findFirst();
      return next.orElseGet(Optional::empty);
   }

   @Override
   public Optional<Duration> timeToNextExecution(ZonedDateTime date) {
      Optional<ZonedDateTime> next = this.nextExecution(date);
      return next.map(zonedDateTime -> Duration.between(date, zonedDateTime));
   }

   @Override
   public Optional<ZonedDateTime> lastExecution(ZonedDateTime date) {
      Optional<Optional<ZonedDateTime>> next = this.executionTimes
         .parallelStream()
         .map(e -> e.lastExecution(date))
         .filter(Optional::isPresent)
         .sorted((o1, o2) -> {
            if (o1.isPresent() && o2.isPresent()) {
               ZonedDateTime first = o1.get();
               ZonedDateTime second = o2.get();
               return second.compareTo(first);
            } else {
               return 0;
            }
         })
         .findFirst();
      return next.orElseGet(Optional::empty);
   }

   @Override
   public Optional<Duration> timeFromLastExecution(ZonedDateTime date) {
      return this.lastExecution(date).map(zonedDateTime -> Duration.between(zonedDateTime, date));
   }

   @Override
   public boolean isMatch(ZonedDateTime date) {
      return this.executionTimes.parallelStream().map(e -> e.isMatch(date)).filter(v -> v).count() > 0L;
   }
}
