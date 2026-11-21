package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParser;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActFormatParser {
   private static final Pattern ACT_PATTERN = Pattern.compile("\\[([^]]+)]\\s+\\[([^]]+)]\\s+\\[([^]]+)]");
   private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhd])");
   private static final Pattern DATE_RANGE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}):(\\d{4}-\\d{2}-\\d{2})");
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

   public static Optional<ActFormatParser.ActSchedule> parse(String actFormat) {
      ActFormatParser.ActComponents components = parseToComponents(actFormat);
      if (components == null) {
         return Optional.empty();
      } else {
         try {
            return Optional.of(new ActFormatParser.ActSchedule(components.getDateRange(), components.getDuration(), components.getCron()));
         } catch (IllegalArgumentException var3) {
            return Optional.empty();
         }
      }
   }

   public static boolean isValidDateRange(String dateRange) {
      if (dateRange == null) {
         return false;
      } else if (!dateRange.equals("*") && !dateRange.equalsIgnoreCase("ALWAYS")) {
         Matcher matcher = DATE_RANGE_PATTERN.matcher(dateRange);
         if (!matcher.matches()) {
            return false;
         } else {
            try {
               String startStr = matcher.group(1);
               String endStr = matcher.group(2);
               LocalDate.parse(startStr, DATE_FORMATTER);
               LocalDate.parse(endStr, DATE_FORMATTER);
               return true;
            } catch (DateTimeParseException var4) {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   public static boolean isValidDuration(String duration) {
      if (duration == null) {
         return false;
      } else {
         return !duration.equals("*") && !duration.equalsIgnoreCase("PERMANENT") ? DURATION_PATTERN.matcher(duration).matches() : true;
      }
   }

   public static boolean isValidCron(String cron) {
      if (cron == null) {
         return false;
      } else {
         return cron.equalsIgnoreCase("NONE") ? true : ValidationUtil.validateCron(cron);
      }
   }

   public static String getHumanReadableDuration(String duration) {
      if (duration == null) {
         return "Invalid";
      } else if (!duration.equals("*") && !duration.equalsIgnoreCase("PERMANENT")) {
         Matcher matcher = DURATION_PATTERN.matcher(duration);
         if (!matcher.matches()) {
            return duration;
         } else {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
               case "s":
                  return value == 1L ? "1 Second" : value + " Seconds";
               case "m":
                  return value == 1L ? "1 Minute" : value + " Minutes";
               case "h":
                  return value == 1L ? "1 Hour" : value + " Hours";
               case "d":
                  return value == 1L ? "1 Day" : value + " Days";
               default:
                  return duration;
            }
         }
      } else {
         return "Permanent";
      }
   }

   public static String getHumanReadableDateRange(String dateRange) {
      if (dateRange == null) {
         return "Invalid";
      } else if (!dateRange.equals("*") && !dateRange.equalsIgnoreCase("ALWAYS")) {
         Matcher matcher = DATE_RANGE_PATTERN.matcher(dateRange);
         return !matcher.matches() ? dateRange : matcher.group(1) + " to " + matcher.group(2);
      } else {
         return "Always Active";
      }
   }

   public static String getHumanReadableCron(String cron, Locale locale, String noneText, String invalidText) {
      if (cron == null || cron.trim().isEmpty()) {
         return invalidText;
      } else if (cron.equalsIgnoreCase("NONE")) {
         return noneText;
      } else {
         return !isValidCron(cron) ? invalidText : CronUtils.describeQuartzCron(cron, locale);
      }
   }

   public static ActFormatParser.ActComponents parseToComponents(String actFormat) {
      String normalizedInput = normalizeActFormatInput(actFormat);
      if (normalizedInput != null && !normalizedInput.isEmpty()) {
         Matcher matcher = ACT_PATTERN.matcher(normalizedInput);
         if (matcher.matches()) {
            String dateRange = matcher.group(1).trim();
            String duration = matcher.group(2).trim();
            String cron = matcher.group(3).trim();
            return new ActFormatParser.ActComponents(dateRange, duration, cron);
         } else {
            String[] parts = normalizedInput.split("\\s+");
            if (parts.length < 3) {
               return null;
            } else {
               String dateRange = parts[0];
               String duration = parts[1];
               StringJoiner cronJoiner = new StringJoiner(" ");

               for (int i = 2; i < parts.length; i++) {
                  cronJoiner.add(parts[i]);
               }

               return new ActFormatParser.ActComponents(dateRange, duration, cronJoiner.toString());
            }
         }
      } else {
         return null;
      }
   }

   private static String normalizeActFormatInput(String actFormat) {
      return actFormat == null ? null : actFormat.trim();
   }

   public static String buildActFormat(String dateRange, String duration, String cron) {
      return "[" + dateRange + "] [" + duration + "] [" + cron + "]";
   }

   static Duration parseDuration(String durationStr) {
      if (!durationStr.equals("*") && !durationStr.equalsIgnoreCase("PERMANENT")) {
         Matcher matcher = DURATION_PATTERN.matcher(durationStr);
         if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration format: " + durationStr);
         } else {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
               case "s":
                  return Duration.ofSeconds(value);
               case "m":
                  return Duration.ofMinutes(value);
               case "h":
                  return Duration.ofHours(value);
               case "d":
                  return Duration.ofDays(value);
               default:
                  throw new IllegalArgumentException("Unknown duration unit: " + unit);
            }
         }
      } else {
         return null;
      }
   }

   public static class ActComponents {
      private final String dateRange;
      private final String duration;
      private final String cron;

      public ActComponents(String dateRange, String duration, String cron) {
         this.dateRange = dateRange;
         this.duration = duration;
         this.cron = cron;
      }

      public String getDateRange() {
         return this.dateRange;
      }

      public String getDuration() {
         return this.duration;
      }

      public String getCron() {
         return this.cron;
      }

      public boolean isValid() {
         return ActFormatParser.isValidDateRange(this.dateRange) && ActFormatParser.isValidDuration(this.duration) && ActFormatParser.isValidCron(this.cron);
      }
   }

   public static class ActSchedule {
      private final String dateRangeStr;
      private final String durationStr;
      private final String cronStr;
      private final ZonedDateTime ruleStart;
      private final ZonedDateTime ruleEnd;
      private final Duration activeDuration;
      private final ExecutionTime executionTime;
      private final boolean isNone;

      ActSchedule(String dateRange, String duration, String cron) {
         this.dateRangeStr = dateRange;
         this.durationStr = duration;
         this.cronStr = cron;
         ZoneId serverZone = ZoneId.systemDefault();
         if (!dateRange.equals("*") && !dateRange.equalsIgnoreCase("ALWAYS")) {
            Matcher matcher = ActFormatParser.DATE_RANGE_PATTERN.matcher(dateRange);
            if (!matcher.matches()) {
               throw new IllegalArgumentException("Invalid date range: " + dateRange);
            }

            LocalDate startDate = LocalDate.parse(matcher.group(1), ActFormatParser.DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(matcher.group(2), ActFormatParser.DATE_FORMATTER);
            this.ruleStart = startDate.atStartOfDay(serverZone);
            this.ruleEnd = endDate.atTime(LocalTime.MAX).atZone(serverZone);
         } else {
            this.ruleStart = null;
            this.ruleEnd = null;
         }

         this.activeDuration = ActFormatParser.parseDuration(duration);
         this.isNone = cron.equalsIgnoreCase("NONE");
         if (this.isNone) {
            this.executionTime = null;
         } else {
            try {
               CronParser parser = CronUtils.getParser();
               Cron parsedCron = parser.parse(cron);
               this.executionTime = ExecutionTime.forCron(parsedCron);
            } catch (Exception var8) {
               throw new IllegalArgumentException("Invalid cron expression: " + cron, var8);
            }
         }
      }

      public boolean isRuleActive(ZonedDateTime now) {
         return this.ruleStart == null && this.ruleEnd == null ? true : !now.isBefore(this.ruleStart) && !now.isAfter(this.ruleEnd);
      }

      public boolean isAvailable(ZonedDateTime now, ZonedDateTime lastActivation) {
         if (!this.isRuleActive(now)) {
            return false;
         } else if (this.isNone) {
            return true;
         } else {
            Optional<ZonedDateTime> lastExecution = this.executionTime.lastExecution(now);
            if (!lastExecution.isPresent()) {
               return false;
            } else {
               ZonedDateTime lastTrigger = lastExecution.get();
               if (this.ruleStart != null && lastTrigger.isBefore(this.ruleStart)) {
                  return false;
               } else if (this.ruleEnd != null && lastTrigger.isAfter(this.ruleEnd)) {
                  return false;
               } else if (this.activeDuration == null) {
                  return true;
               } else {
                  ZonedDateTime expirationTime = lastTrigger.plus(this.activeDuration);
                  return now.isBefore(expirationTime);
               }
            }
         }
      }

      public Optional<ZonedDateTime> getNextTrigger(ZonedDateTime now) {
         if (!this.isNone && this.executionTime != null) {
            Optional<ZonedDateTime> next = this.executionTime.nextExecution(now);
            if (!next.isPresent()) {
               return Optional.empty();
            } else {
               ZonedDateTime nextTime = next.get();
               if (this.ruleEnd != null && nextTime.isAfter(this.ruleEnd)) {
                  return Optional.empty();
               } else {
                  return this.ruleStart != null && nextTime.isBefore(this.ruleStart)
                     ? this.executionTime.nextExecution(this.ruleStart.minusSeconds(1L))
                     : Optional.of(nextTime);
               }
            }
         } else {
            return Optional.empty();
         }
      }

      public String getDateRange() {
         return this.dateRangeStr;
      }

      public String getDuration() {
         return this.durationStr;
      }

      public String getCron() {
         return this.cronStr;
      }

      public boolean isNone() {
         return this.isNone;
      }

      public Duration getActiveDuration() {
         return this.activeDuration;
      }
   }
}
