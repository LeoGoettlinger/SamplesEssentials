package de.theredend2000.advancedhunt.util;

import de.theredend2000.advancedhunt.lib.cronutils.descriptor.CronDescriptor;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import de.theredend2000.advancedhunt.lib.cronutils.parser.CronParser;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;

public final class CronUtils {
   private static final ThreadLocal<CronParser> cronParser = ThreadLocal.withInitial(
      () -> new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
   );
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

   private CronUtils() {
   }

   public static CronParser getParser() {
      return cronParser.get();
   }

   public static DateTimeFormatter getDateFormat() {
      return DATE_FORMAT;
   }

   public static List<String> getNextExecutions(String cronExpression, int count) {
      if (cronExpression != null && !cronExpression.isEmpty() && count > 0) {
         List<String> executions = new ArrayList<>(count);

         try {
            Cron cron = cronParser.get().parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            ZonedDateTime now = ZonedDateTime.now();
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);

            for (int i = 0; i < count && nextExecution.isPresent(); i++) {
               executions.add(nextExecution.get().format(DATE_FORMAT));
               nextExecution = executionTime.nextExecution(nextExecution.get());
            }
         } catch (Exception var8) {
         }

         return executions;
      } else {
         return Collections.emptyList();
      }
   }

   public static String describeQuartzCron(String cronExpression, Locale locale) {
      if (cronExpression != null && !cronExpression.isEmpty()) {
         try {
            Cron cron = cronParser.get().parse(cronExpression);
            cron.validate();
            Locale effectiveLocale = locale != null ? locale : Locale.ENGLISH;
            CronDescriptor descriptor = CronDescriptor.instance(effectiveLocale);
            String description = descriptor.describe(cron);
            return description != null && !description.trim().isEmpty() ? description : cronExpression;
         } catch (MissingResourceException var7) {
            try {
               Cron cronx = cronParser.get().parse(cronExpression);
               cronx.validate();
               String descriptionx = CronDescriptor.instance(Locale.ENGLISH).describe(cronx);
               return descriptionx != null && !descriptionx.trim().isEmpty() ? descriptionx : cronExpression;
            } catch (Exception var6) {
               return cronExpression;
            }
         } catch (Exception var8) {
            return cronExpression;
         }
      } else {
         return "";
      }
   }

   public static Locale toLocale(String languageTag) {
      if (languageTag != null && !languageTag.trim().isEmpty()) {
         Locale locale = Locale.forLanguageTag(languageTag.trim().replace('_', '-'));
         return locale.getLanguage() != null && !locale.getLanguage().trim().isEmpty() && !"und".equals(locale.getLanguage()) ? locale : Locale.ENGLISH;
      } else {
         return Locale.ENGLISH;
      }
   }

   public static String formatDateTime(ZonedDateTime dateTime) {
      return dateTime.format(DATE_FORMAT);
   }
}
