package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.mapper.ConstantsMapper;
import de.theredend2000.advancedhunt.lib.cronutils.mapper.WeekDay;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.DayOfWeekFieldDefinition;

public enum Weekdays {
   MONDAY(1),
   TUESDAY(2),
   WEDNESDAY(3),
   THURSDAY(4),
   FRIDAY(5),
   SATURDAY(6),
   SUNDAY(7);

   private int weekday;

   private Weekdays(int weekday) {
      this.weekday = weekday;
   }

   public int getWeekday() {
      return this.getWeekday(ConstantsMapper.CRONTAB_WEEK_DAY);
   }

   public int getWeekday(WeekDay weekDay) {
      return ConstantsMapper.weekDayMapping(ConstantsMapper.CRONTAB_WEEK_DAY, weekDay, this.weekday);
   }

   public int getWeekday(CronDefinition cronDefinition) {
      return this.getWeekday(((DayOfWeekFieldDefinition)cronDefinition.getFieldDefinition(CronFieldName.DAY_OF_WEEK)).getMondayDoWValue());
   }
}
