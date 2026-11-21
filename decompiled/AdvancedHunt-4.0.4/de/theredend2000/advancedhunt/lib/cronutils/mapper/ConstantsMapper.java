package de.theredend2000.advancedhunt.lib.cronutils.mapper;

public class ConstantsMapper {
   public static final WeekDay QUARTZ_WEEK_DAY = new WeekDay(2, false);
   public static final WeekDay JAVA8 = new WeekDay(1, false);
   public static final WeekDay CRONTAB_WEEK_DAY = new WeekDay(1, true);

   private ConstantsMapper() {
   }

   public static int weekDayMapping(WeekDay source, WeekDay target, int weekday) {
      return source.mapTo(weekday, target);
   }
}
