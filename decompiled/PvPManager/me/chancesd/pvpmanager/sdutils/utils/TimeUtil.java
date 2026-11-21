package me.chancesd.pvpmanager.sdutils.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class TimeUtil {
   private static final ChronoUnit[] types = new ChronoUnit[]{ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS};

   private TimeUtil() {
   }

   public static String getDiffUntil(TimeUtil.TimeLangProvider lang, long date) {
      return getDiffMsg(lang, Instant.now(), Instant.ofEpochMilli(date));
   }

   public static String getDiffUntil(Class<? extends Enum<? extends TimeUtil.TimeLangProvider>> langClass, long date) {
      TimeUtil.TimeLangProvider provider = getProviderFromClass(langClass);
      return getDiffUntil(provider, date);
   }

   public static String getDiffDuration(TimeUtil.TimeLangProvider lang, long date) {
      return getDiffMsg(lang, Instant.now(), Instant.now().plusMillis(date));
   }

   public static String getDiffDuration(Class<? extends Enum<? extends TimeUtil.TimeLangProvider>> langClass, long durationMs) {
      TimeUtil.TimeLangProvider provider = getProviderFromClass(langClass);
      return getDiffDuration(provider, durationMs);
   }

   public static String getDiffMsg(TimeUtil.TimeLangProvider lang, Instant from, Instant to) {
      boolean future = false;
      if (to.equals(from)) {
         return lang.getTime(ChronoUnit.FOREVER);
      } else {
         if (to.isAfter(from)) {
            future = true;
         }

         NCDuration duration = NCDuration.between(from, to).plusMillis(future ? 50L : -50L);
         StringBuilder sb = new StringBuilder();
         int accuracy = 0;

         for (int i = 0; i < types.length && accuracy < 2; i++) {
            int value = duration.get(types[i]);
            if (value > 0) {
               accuracy++;
               sb.append(" ").append(value).append(lang.getTime(types[i]));
            }
         }

         return sb.isEmpty() ? lang.getTime(ChronoUnit.FOREVER) : sb.toString().trim();
      }
   }

   private static TimeUtil.TimeLangProvider getProviderFromClass(Class<? extends Enum<? extends TimeUtil.TimeLangProvider>> langClass) {
      try {
         Enum<? extends TimeUtil.TimeLangProvider>[] constants = (Enum<? extends TimeUtil.TimeLangProvider>[])langClass.getEnumConstants();
         if (constants != null && constants.length > 0) {
            return (TimeUtil.TimeLangProvider)constants[0];
         }
      } catch (Exception var2) {
      }

      return null;
   }

   public interface TimeLangProvider {
      String getTime(ChronoUnit var1);
   }
}
