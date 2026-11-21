package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParser;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ValidationUtil {
   private static final ThreadLocal<CronParser> cronParser = ThreadLocal.withInitial(
      () -> new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
   );

   public static boolean validateCron(String cronExpression) {
      try {
         cronParser.get().parse(cronExpression);
         return true;
      } catch (IllegalArgumentException var2) {
         return false;
      }
   }

   public static boolean validateTime(String time) {
      try {
         LocalTime.parse(time);
         return true;
      } catch (DateTimeParseException var2) {
         return false;
      }
   }

   public static boolean validateDateRange(String dateRange) {
      return ActFormatParser.isValidDateRange(dateRange);
   }

   public static boolean validateDuration(String duration) {
      return ActFormatParser.isValidDuration(duration);
   }

   public static boolean validateActFormat(String actFormat) {
      return ActFormatParser.parse(actFormat).isPresent();
   }
}
