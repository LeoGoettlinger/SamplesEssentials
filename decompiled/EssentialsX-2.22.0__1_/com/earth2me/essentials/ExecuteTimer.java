package com.earth2me.essentials;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExecuteTimer {
   private final transient List<ExecuteTimer.ExecuteRecord> times;
   private final transient DecimalFormat decimalFormat = new DecimalFormat("#0.000", DecimalFormatSymbols.getInstance(Locale.US));

   public ExecuteTimer() {
      this.times = new ArrayList<>();
   }

   public void start() {
      this.times.clear();
      this.mark("start");
   }

   public void mark(final String label) {
      if (!this.times.isEmpty() || "start".equals(label)) {
         this.times.add(new ExecuteTimer.ExecuteRecord(label, System.nanoTime()));
      }
   }

   public String end() {
      StringBuilder output = new StringBuilder();
      output.append("execution time: ");
      long time0 = 0L;
      long time1 = 0L;
      long time2 = 0L;

      for (ExecuteTimer.ExecuteRecord pair : this.times) {
         String mark = pair.getMark();
         time2 = pair.getTime();
         if (time1 > 0L) {
            double duration = (time2 - time1) / 1000000.0;
            output.append(mark).append(": ").append(this.decimalFormat.format(duration)).append("ms - ");
         } else {
            time0 = time2;
         }

         time1 = time2;
      }

      double duration = (time1 - time0) / 1000000.0;
      output.append("Total: ").append(this.decimalFormat.format(duration)).append("ms");
      this.times.clear();
      return output.toString();
   }

   private static class ExecuteRecord {
      private final String mark;
      private final long time;

      ExecuteRecord(final String mark, final long time) {
         this.mark = mark;
         this.time = time;
      }

      public String getMark() {
         return this.mark;
      }

      public long getTime() {
         return this.time;
      }
   }
}
