package de.theredend2000.advancedhunt.lib.cronutils.descriptor;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.RebootCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class CronDescriptor {
   public static final Locale DEFAULT_LOCALE = Locale.UK;
   private static final String BUNDLE = "de.theredend2000.advancedhunt.lib.cronutils.CronUtilsI18N";
   private final ResourceBundle resourceBundle;

   public CronDescriptor(ResourceBundle resourceBundle) {
      this.resourceBundle = resourceBundle;
   }

   private CronDescriptor(Locale locale) {
      this.resourceBundle = ResourceBundle.getBundle("de.theredend2000.advancedhunt.lib.cronutils.CronUtilsI18N", locale);
   }

   private CronDescriptor() {
      this.resourceBundle = ResourceBundle.getBundle("de.theredend2000.advancedhunt.lib.cronutils.CronUtilsI18N", DEFAULT_LOCALE);
   }

   public String describe(Cron cron) {
      Preconditions.checkNotNull(cron, "Cron must not be null");
      if (cron instanceof RebootCron) {
         return this.resourceBundle.getString("on_reboot");
      } else {
         Map<CronFieldName, CronField> expressions = cron.retrieveFieldsAsMap();
         Map<CronFieldName, FieldDefinition> fieldDefinitions = cron.getCronDefinition().retrieveFieldDefinitionsAsMap();
         return (this.describeHHmmss(expressions)
               + " "
               + this.describeDayOfMonth(expressions)
               + " "
               + this.describeMonth(expressions)
               + " "
               + this.describeDayOfWeek(expressions, fieldDefinitions)
               + " "
               + this.describeYear(expressions))
            .replaceAll("\\s+", " ")
            .trim();
      }
   }

   public String describeHHmmss(Map<CronFieldName, CronField> fields) {
      return DescriptionStrategyFactory.hhMMssInstance(
            this.resourceBundle,
            fields.containsKey(CronFieldName.HOUR) ? fields.get(CronFieldName.HOUR).getExpression() : null,
            fields.containsKey(CronFieldName.MINUTE) ? fields.get(CronFieldName.MINUTE).getExpression() : null,
            fields.containsKey(CronFieldName.SECOND) ? fields.get(CronFieldName.SECOND).getExpression() : null
         )
         .describe();
   }

   public String describeDayOfMonth(Map<CronFieldName, CronField> fields) {
      String description = DescriptionStrategyFactory.daysOfMonthInstance(
            this.resourceBundle, fields.containsKey(CronFieldName.DAY_OF_MONTH) ? fields.get(CronFieldName.DAY_OF_MONTH).getExpression() : null
         )
         .describe();
      return this.addTimeExpressions(description, this.resourceBundle.getString("day"), this.resourceBundle.getString("days"));
   }

   public String describeMonth(Map<CronFieldName, CronField> fields) {
      String description = DescriptionStrategyFactory.monthsInstance(
            this.resourceBundle, fields.containsKey(CronFieldName.MONTH) ? fields.get(CronFieldName.MONTH).getExpression() : null
         )
         .describe();
      return this.addTimeExpressions(description, this.resourceBundle.getString("month"), this.resourceBundle.getString("months"));
   }

   private String addTimeExpressions(String description, String singular, String plural) {
      return description.replaceAll("%s", singular).replaceAll("replace_plural", plural);
   }

   public String describeDayOfWeek(Map<CronFieldName, CronField> fields, Map<CronFieldName, FieldDefinition> definitions) {
      String description = DescriptionStrategyFactory.daysOfWeekInstance(
            this.resourceBundle,
            fields.containsKey(CronFieldName.DAY_OF_WEEK) ? fields.get(CronFieldName.DAY_OF_WEEK).getExpression() : null,
            definitions.containsKey(CronFieldName.DAY_OF_WEEK) ? definitions.get(CronFieldName.DAY_OF_WEEK) : null
         )
         .describe();
      return this.addExpressions(description, this.resourceBundle.getString("day"), this.resourceBundle.getString("days"));
   }

   private String addExpressions(String description, String singular, String plural) {
      return description.replaceAll("%s", singular).replaceAll("replace_plural", plural);
   }

   public String describeYear(Map<CronFieldName, CronField> fields) {
      String description = DescriptionStrategyFactory.plainInstance(
            this.resourceBundle, fields.containsKey(CronFieldName.YEAR) ? fields.get(CronFieldName.YEAR).getExpression() : null
         )
         .describe();
      return this.addExpressions(description, this.resourceBundle.getString("year"), this.resourceBundle.getString("years"));
   }

   public static CronDescriptor instance() {
      return new CronDescriptor();
   }

   public static CronDescriptor instance(Locale locale) {
      return new CronDescriptor(locale);
   }

   public ResourceBundle getResourceBundle() {
      return this.resourceBundle;
   }
}
