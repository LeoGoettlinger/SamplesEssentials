package de.theredend2000.advancedhunt.lib.cronutils.utils;

import java.time.ZonedDateTime;

public enum WeekendPolicy {
   THURSDAY_FRIDAY,
   FRIDAY_SATURDAY,
   SATURDAY_SUNDAY;

   public static int daysToWeekend(WeekendPolicy policy, ZonedDateTime date) {
      int dow = date.getDayOfWeek().getValue();
      switch (policy) {
         case THURSDAY_FRIDAY:
            if (dow < 4) {
               return 4 - dow;
            }

            if (dow > 5) {
               return 7 - dow + 4;
            }
            break;
         case FRIDAY_SATURDAY:
            if (dow < 5) {
               return 6 - dow;
            }

            if (dow > 6) {
               return 7 - dow + 5;
            }
            break;
         case SATURDAY_SUNDAY:
            if (dow < 6) {
               return Math.max(0, 6 - dow);
            }
      }

      return 0;
   }

   public static int daysFromWeekend(WeekendPolicy policy, ZonedDateTime date) {
      int dow = date.getDayOfWeek().getValue();
      switch (policy) {
         case THURSDAY_FRIDAY:
            if (dow < 4) {
               return dow;
            }

            if (dow > 5) {
               return dow - 5;
            }
            break;
         case FRIDAY_SATURDAY:
            if (dow < 5) {
               return dow;
            }

            if (dow > 6) {
               return dow - 6;
            }
            break;
         case SATURDAY_SUNDAY:
            if (dow < 6) {
               return dow;
            }
      }

      return 0;
   }
}
