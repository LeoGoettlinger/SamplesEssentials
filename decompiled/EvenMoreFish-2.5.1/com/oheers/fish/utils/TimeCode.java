package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;

public record TimeCode(@NonNull String code) {
   public static TimeCode now() {
      return fromDateTime(LocalDateTime.now());
   }

   public static TimeCode fromDateTime(LocalDateTime time) {
      return exact(time.getDayOfWeek(), time.getHour(), time.getMinute());
   }

   public static TimeCode exact(@NonNull DayOfWeek day, @Range(from = 0L,to = 23L) int hour, @Range(from = 0L,to = 59L) int minute) {
      return new TimeCode(day + "-" + hour + "-" + minute);
   }

   public long toMillis() {
      String[] split = this.code.split("-");
      if (split.length != 3) {
         return -1L;
      } else {
         DayOfWeek day = FishUtils.getDay(split[0]);
         Integer hour = FishUtils.getInteger(split[1]);
         Integer minute = FishUtils.getInteger(split[2]);
         if (day != null && hour != null && minute != null) {
            LocalDate targetDay = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
            LocalDateTime exactTime = targetDay.atTime(hour, minute);
            ZonedDateTime zoned = exactTime.atZone(ZoneId.systemDefault());
            return zoned.toInstant().toEpochMilli();
         } else {
            return -1L;
         }
      }
   }

   public Long toMillisNullable() {
      long millis = this.toMillis();
      return millis == -1L ? null : millis;
   }

   public static Comparator<TimeCode> getComparator() {
      return Comparator.comparingLong(TimeCode::toMillis);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TimeCode(String var4) ? var4.equals(this.code) : false;
      }
   }

   @NonNull
   @Override
   public String toString() {
      return this.code;
   }
}
