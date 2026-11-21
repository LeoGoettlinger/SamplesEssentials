package de.theredend2000.advancedhunt.lib.cronutils.utils;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import java.time.ZonedDateTime;
import java.util.Comparator;

public class CronFrequencyComparator implements Comparator<Cron> {
   private final ZonedDateTime startDate;
   private final ZonedDateTime endDate;

   public CronFrequencyComparator(ZonedDateTime startDate, ZonedDateTime endDate) {
      this.startDate = startDate;
      this.endDate = endDate;
   }

   public int compare(Cron o1, Cron o2) {
      int executions1 = ExecutionTime.forCron(o1).countExecutions(this.startDate, this.endDate);
      int executions2 = ExecutionTime.forCron(o2).countExecutions(this.startDate, this.endDate);
      return executions1 - executions2;
   }
}
