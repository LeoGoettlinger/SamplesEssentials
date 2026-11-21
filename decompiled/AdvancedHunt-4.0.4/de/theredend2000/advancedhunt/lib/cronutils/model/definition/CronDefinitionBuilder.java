package de.theredend2000.advancedhunt.lib.cronutils.model.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDayOfWeekDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldQuestionMarkDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldSpecialCharsDefinitionBuilder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CronDefinitionBuilder {
   private final Map<CronFieldName, FieldDefinition> fields = new EnumMap<>(CronFieldName.class);
   private final Set<CronConstraint> cronConstraints = new HashSet<>();
   private final Set<CronNicknames> cronNicknames = new HashSet<>();
   private boolean matchDayOfWeekAndDayOfMonth;

   private CronDefinitionBuilder() {
   }

   public static CronDefinitionBuilder defineCron() {
      return new CronDefinitionBuilder();
   }

   public FieldDefinitionBuilder withSeconds() {
      return new FieldDefinitionBuilder(this, CronFieldName.SECOND);
   }

   public FieldDefinitionBuilder withMinutes() {
      return new FieldDefinitionBuilder(this, CronFieldName.MINUTE);
   }

   public FieldDefinitionBuilder withHours() {
      return new FieldDefinitionBuilder(this, CronFieldName.HOUR);
   }

   public FieldSpecialCharsDefinitionBuilder withDayOfMonth() {
      return new FieldSpecialCharsDefinitionBuilder(this, CronFieldName.DAY_OF_MONTH);
   }

   public FieldDefinitionBuilder withMonth() {
      return new FieldDefinitionBuilder(this, CronFieldName.MONTH);
   }

   public FieldDayOfWeekDefinitionBuilder withDayOfWeek() {
      return new FieldDayOfWeekDefinitionBuilder(this, CronFieldName.DAY_OF_WEEK);
   }

   public FieldDefinitionBuilder withYear() {
      return new FieldDefinitionBuilder(this, CronFieldName.YEAR);
   }

   public FieldQuestionMarkDefinitionBuilder withDayOfYear() {
      return new FieldQuestionMarkDefinitionBuilder(this, CronFieldName.DAY_OF_YEAR);
   }

   public CronDefinitionBuilder matchDayOfWeekAndDayOfMonth() {
      this.matchDayOfWeekAndDayOfMonth = true;
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameYearly() {
      this.cronNicknames.add(CronNicknames.YEARLY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameAnnually() {
      this.cronNicknames.add(CronNicknames.ANNUALLY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameMonthly() {
      this.cronNicknames.add(CronNicknames.MONTHLY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameWeekly() {
      this.cronNicknames.add(CronNicknames.WEEKLY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameDaily() {
      this.cronNicknames.add(CronNicknames.DAILY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameMidnight() {
      this.cronNicknames.add(CronNicknames.MIDNIGHT);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameHourly() {
      this.cronNicknames.add(CronNicknames.HOURLY);
      return this;
   }

   public CronDefinitionBuilder withSupportedNicknameReboot() {
      this.cronNicknames.add(CronNicknames.REBOOT);
      return this;
   }

   public CronDefinitionBuilder withCronValidation(CronConstraint validation) {
      this.cronConstraints.add(validation);
      return this;
   }

   public void register(FieldDefinition definition) {
      boolean hasOptionalField = false;

      for (FieldDefinition fieldDefinition : this.fields.values()) {
         if (fieldDefinition.isOptional()) {
            hasOptionalField = true;
            break;
         }
      }

      if (!definition.isOptional() && hasOptionalField) {
         throw new IllegalArgumentException("Can't register mandatory definition after a optional definition.");
      } else {
         this.fields.put(definition.getFieldName(), definition);
      }
   }

   public CronDefinition instance() {
      Set<CronConstraint> validations = new HashSet<>();
      validations.addAll(this.cronConstraints);
      List<FieldDefinition> values = new ArrayList<>(this.fields.values());
      values.sort(FieldDefinition.createFieldDefinitionComparator());
      return new CronDefinition(values, validations, this.cronNicknames, this.matchDayOfWeekAndDayOfMonth);
   }

   private static CronDefinition cron4j() {
      return defineCron()
         .withMinutes()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withHours()
         .withValidRange(0, 23)
         .withStrictRange()
         .and()
         .withDayOfMonth()
         .withValidRange(0, 31)
         .supportsL()
         .withStrictRange()
         .and()
         .withMonth()
         .withValidRange(1, 12)
         .withStrictRange()
         .and()
         .withDayOfWeek()
         .withValidRange(0, 6)
         .withMondayDoWValue(1)
         .withStrictRange()
         .and()
         .matchDayOfWeekAndDayOfMonth()
         .instance();
   }

   private static CronDefinition quartz() {
      return defineCron()
         .withSeconds()
         .withValidRange(0, 59)
         .and()
         .withMinutes()
         .withValidRange(0, 59)
         .and()
         .withHours()
         .withValidRange(0, 23)
         .and()
         .withDayOfMonth()
         .withValidRange(1, 31)
         .supportsL()
         .supportsW()
         .supportsLW()
         .supportsQuestionMark()
         .and()
         .withMonth()
         .withValidRange(1, 12)
         .and()
         .withDayOfWeek()
         .withValidRange(1, 7)
         .withMondayDoWValue(2)
         .supportsHash()
         .supportsL()
         .supportsQuestionMark()
         .and()
         .withYear()
         .withValidRange(1970, 2099)
         .withStrictRange()
         .optional()
         .and()
         .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
         .instance();
   }

   private static CronDefinition spring() {
      return defineCron()
         .withSeconds()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withMinutes()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withHours()
         .withValidRange(0, 23)
         .withStrictRange()
         .and()
         .withDayOfMonth()
         .withValidRange(1, 31)
         .supportsQuestionMark()
         .and()
         .withMonth()
         .withValidRange(1, 12)
         .and()
         .withDayOfWeek()
         .withValidRange(0, 7)
         .withMondayDoWValue(1)
         .withIntMapping(7, 0)
         .supportsHash()
         .supportsQuestionMark()
         .and()
         .instance();
   }

   private static CronDefinition spring53() {
      return defineCron()
         .withSeconds()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withMinutes()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withHours()
         .withValidRange(0, 23)
         .withStrictRange()
         .and()
         .withDayOfMonth()
         .withValidRange(1, 31)
         .supportsL()
         .supportsW()
         .supportsLW()
         .supportsQuestionMark()
         .and()
         .withMonth()
         .withValidRange(1, 12)
         .and()
         .withDayOfWeek()
         .withValidRange(0, 7)
         .withMondayDoWValue(1)
         .withIntMapping(7, 0)
         .supportsHash()
         .supportsL()
         .supportsQuestionMark()
         .and()
         .withSupportedNicknameYearly()
         .withSupportedNicknameAnnually()
         .withSupportedNicknameMonthly()
         .withSupportedNicknameWeekly()
         .withSupportedNicknameDaily()
         .withSupportedNicknameMidnight()
         .withSupportedNicknameHourly()
         .instance();
   }

   private static CronDefinition unixCrontab() {
      return defineCron()
         .withMinutes()
         .withValidRange(0, 59)
         .withStrictRange()
         .and()
         .withHours()
         .withValidRange(0, 23)
         .withStrictRange()
         .and()
         .withDayOfMonth()
         .withValidRange(1, 31)
         .withStrictRange()
         .and()
         .withMonth()
         .withValidRange(1, 12)
         .withStrictRange()
         .and()
         .withDayOfWeek()
         .withValidRange(0, 7)
         .withMondayDoWValue(1)
         .withIntMapping(7, 0)
         .withStrictRange()
         .and()
         .instance();
   }

   public static CronDefinition instanceDefinitionFor(CronType cronType) {
      switch (cronType) {
         case CRON4J:
            return cron4j();
         case QUARTZ:
            return quartz();
         case UNIX:
            return unixCrontab();
         case SPRING:
            return spring();
         case SPRING53:
            return spring53();
         default:
            throw new IllegalArgumentException(String.format("No cron definition found for %s", cronType));
      }
   }
}
