package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;

public final class UsageResetScheduler {
   private final EzRtpPlugin plugin;
   private final PlatformScheduler scheduler;
   private final RtpUsageStorage usageStorage;

   public UsageResetScheduler(EzRtpPlugin plugin, PlatformScheduler scheduler, RtpUsageStorage usageStorage) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.usageStorage = usageStorage;
   }

   public void schedule() {
      long ticksPerDay = 1728000L;
      long ticksUntilMidnight = this.getTicksUntilNext(0, 0);
      this.scheduler.scheduleRepeating(this::resetDailyUsage, ticksUntilMidnight, ticksPerDay);
      long ticksPerWeek = 12096000L;
      long ticksUntilSunday = this.getTicksUntilNextSundayMidnight();
      this.scheduler.scheduleRepeating(this::resetWeeklyUsage, ticksUntilSunday, ticksPerWeek);
   }

   private void resetDailyUsage() {
      try {
         this.usageStorage.resetUsage(null, null, "daily");
         this.usageStorage.save();
         this.plugin.getLogger().info("Daily RTP usage counts reset.");
      } catch (Exception var2) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to reset daily RTP usage counts", (Throwable)var2);
      }
   }

   private void resetWeeklyUsage() {
      try {
         this.usageStorage.resetUsage(null, null, "weekly");
         this.usageStorage.save();
         this.plugin.getLogger().info("Weekly RTP usage counts reset.");
      } catch (Exception var2) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to reset weekly RTP usage counts", (Throwable)var2);
      }
   }

   private long getTicksUntilNext(int hour, int minute) {
      ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
      ZonedDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
      if (!next.isAfter(now)) {
         next = next.plusDays(1L);
      }

      long seconds = Duration.between(now, next).getSeconds();
      return seconds * 20L;
   }

   private long getTicksUntilNextSundayMidnight() {
      ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
      ZonedDateTime next = now.with(DayOfWeek.SUNDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
      if (!next.isAfter(now)) {
         next = next.plusWeeks(1L);
      }

      long seconds = Duration.between(now, next).getSeconds();
      return seconds * 20L;
   }
}
