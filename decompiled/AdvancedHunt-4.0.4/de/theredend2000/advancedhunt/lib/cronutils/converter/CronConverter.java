package de.theredend2000.advancedhunt.lib.cronutils.converter;

import de.theredend2000.advancedhunt.lib.cronutils.utils.StringUtils;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CronConverter {
   private static final Logger LOGGER = LoggerFactory.getLogger(CronConverter.class);
   private static final String CRON_FIELDS_SEPARATOR = " ";
   private String[] cronParts;
   private Calendar fromCalendar;
   private String sourceCron;
   private ZoneId sourceZoneId;
   private ZoneId targetZoneId;
   private CronToCalendarTransformer toCalendarConverter;
   private CalendarToCronTransformer toCronConverter;

   public CronConverter(CronToCalendarTransformer toCalendarConverter, CalendarToCronTransformer toCronConverter) {
      this.toCalendarConverter = toCalendarConverter;
      this.toCronConverter = toCronConverter;
   }

   public CronConverter using(String cronExpression) {
      this.sourceCron = cronExpression;
      this.cronParts = cronExpression.split(" ");
      LOGGER.debug("Cron '{}' split into {}", cronExpression, this.cronParts);
      return this;
   }

   public CronConverter from(ZoneId zoneId) {
      this.sourceZoneId = zoneId;
      this.fromCalendar = this.getCalendar(zoneId);
      this.toCalendarConverter.apply(this.cronParts, this.fromCalendar);
      LOGGER.debug("Calendar object built using cron :{} and zoneID {} => {}", this.cronParts, zoneId, this.fromCalendar);
      return this;
   }

   public CronConverter to(ZoneId zoneId) {
      this.targetZoneId = zoneId;
      Calendar toCalendar = this.getCalendar(zoneId);
      toCalendar.setTimeInMillis(this.fromCalendar.getTimeInMillis());
      LOGGER.debug("Calendar object built from calendar {} and zoneID {} => {}", this.fromCalendar, zoneId, toCalendar);
      this.toCronConverter.apply(this.cronParts, toCalendar);
      LOGGER.debug("cron after applying calendar {} => {}", toCalendar, this.cronParts);
      return this;
   }

   public String convert() {
      String targetCron = StringUtils.join(this.cronParts, " ");
      LOGGER.info("Converted CRON -- {} :[{}] => {} :[{}]", this.sourceZoneId, this.sourceCron, this.targetZoneId, targetCron);
      return targetCron;
   }

   private Calendar getCalendar(ZoneId id) {
      return Calendar.getInstance(TimeZone.getTimeZone(id));
   }
}
