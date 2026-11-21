package de.theredend2000.advancedhunt.lib.cronutils.model.time;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraintsBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.generator.FieldValueGenerator;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.generator.FieldValueGeneratorFactory;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

class ExecutionTimeBuilder {
   private final Cron cron;
   private FieldValueGenerator yearsValueGenerator;
   private CronField daysOfWeekCronField;
   private CronField daysOfMonthCronField;
   private CronField daysOfYearCronField;
   private TimeNode months;
   private TimeNode hours;
   private TimeNode minutes;
   private TimeNode seconds;

   protected ExecutionTimeBuilder(Cron cron) {
      this.cron = cron;
   }

   protected ExecutionTimeBuilder forSecondsMatching(CronField cronField) {
      this.validate(CronFieldName.SECOND, cronField);
      this.seconds = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 59));
      return this;
   }

   protected ExecutionTimeBuilder forMinutesMatching(CronField cronField) {
      this.validate(CronFieldName.MINUTE, cronField);
      this.minutes = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 59));
      return this;
   }

   protected ExecutionTimeBuilder forHoursMatching(CronField cronField) {
      this.validate(CronFieldName.HOUR, cronField);
      this.hours = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(0, 23));
      return this;
   }

   protected ExecutionTimeBuilder forMonthsMatching(CronField cronField) {
      this.validate(CronFieldName.MONTH, cronField);
      this.months = new TimeNode(FieldValueGeneratorFactory.forCronField(cronField).generateCandidates(1, 12));
      return this;
   }

   protected ExecutionTimeBuilder forYearsMatching(CronField cronField) {
      this.validate(CronFieldName.YEAR, cronField);
      this.yearsValueGenerator = FieldValueGeneratorFactory.forCronField(cronField);
      return this;
   }

   protected ExecutionTimeBuilder forDaysOfWeekMatching(CronField cronField) {
      this.validate(CronFieldName.DAY_OF_WEEK, cronField);
      this.daysOfWeekCronField = cronField;
      return this;
   }

   protected ExecutionTimeBuilder forDaysOfMonthMatching(CronField cronField) {
      this.validate(CronFieldName.DAY_OF_MONTH, cronField);
      this.daysOfMonthCronField = cronField;
      return this;
   }

   protected ExecutionTimeBuilder forDaysOfYearMatching(CronField cronField) {
      this.validate(CronFieldName.DAY_OF_YEAR, cronField);
      this.daysOfYearCronField = cronField;
      return this;
   }

   protected ExecutionTime build() {
      boolean lowestAssigned = false;
      if (this.seconds == null) {
         this.seconds = this.timeNodeLowest(CronFieldName.SECOND, 0, 59);
      } else {
         lowestAssigned = true;
      }

      if (this.minutes == null) {
         this.minutes = lowestAssigned ? this.timeNodeAlways(CronFieldName.MINUTE, 0, 59) : this.timeNodeLowest(CronFieldName.MINUTE, 0, 59);
      } else {
         lowestAssigned = true;
      }

      if (this.hours == null) {
         this.hours = lowestAssigned ? this.timeNodeAlways(CronFieldName.HOUR, 0, 23) : this.timeNodeLowest(CronFieldName.HOUR, 0, 23);
      } else {
         lowestAssigned = true;
      }

      if (this.daysOfMonthCronField == null) {
         FieldConstraints constraints = this.getConstraint(CronFieldName.DAY_OF_MONTH);
         this.daysOfMonthCronField = lowestAssigned
            ? new CronField(CronFieldName.DAY_OF_MONTH, FieldExpression.always(), constraints)
            : new CronField(CronFieldName.DAY_OF_MONTH, new On(new IntegerFieldValue(1)), constraints);
      } else {
         lowestAssigned = true;
      }

      if (this.daysOfWeekCronField == null) {
         FieldConstraints constraints = this.getConstraint(CronFieldName.DAY_OF_WEEK);
         this.daysOfWeekCronField = lowestAssigned
            ? new CronField(CronFieldName.DAY_OF_WEEK, FieldExpression.always(), constraints)
            : new CronField(CronFieldName.DAY_OF_WEEK, new On(new IntegerFieldValue(1)), constraints);
      } else {
         lowestAssigned = true;
      }

      if (this.months == null) {
         this.months = lowestAssigned ? this.timeNodeAlways(CronFieldName.MONTH, 1, 12) : this.timeNodeLowest(CronFieldName.MONTH, 1, 12);
      }

      if (this.yearsValueGenerator == null) {
         this.yearsValueGenerator = FieldValueGeneratorFactory.forCronField(
            new CronField(CronFieldName.YEAR, FieldExpression.always(), this.getConstraint(CronFieldName.YEAR))
         );
      }

      if (this.daysOfYearCronField == null) {
         FieldConstraints constraints = this.getConstraint(CronFieldName.DAY_OF_YEAR);
         this.daysOfYearCronField = new CronField(
            CronFieldName.DAY_OF_YEAR, lowestAssigned ? FieldExpression.questionMark() : FieldExpression.always(), constraints
         );
      }

      return new SingleExecutionTime(
         this.cron.getCronDefinition(),
         this.cron.retrieve(CronFieldName.YEAR),
         this.daysOfWeekCronField,
         this.daysOfMonthCronField,
         this.daysOfYearCronField,
         this.months,
         this.hours,
         this.minutes,
         this.seconds
      );
   }

   private TimeNode timeNodeLowest(CronFieldName name, int lower, int higher) {
      FieldConstraints constraints = this.getConstraint(name);
      return new TimeNode(
         FieldValueGeneratorFactory.forCronField(new CronField(name, new On(new IntegerFieldValue(lower)), constraints)).generateCandidates(lower, higher)
      );
   }

   private TimeNode timeNodeAlways(CronFieldName name, int lower, int higher) {
      return new TimeNode(
         FieldValueGeneratorFactory.forCronField(new CronField(name, FieldExpression.always(), this.getConstraint(name))).generateCandidates(lower, higher)
      );
   }

   private void validate(CronFieldName name, CronField cronField) {
      Preconditions.checkNotNull(name, "Reference CronFieldName cannot be null");
      Preconditions.checkNotNull(cronField.getField(), "CronField's CronFieldName cannot be null");
      if (!name.equals(cronField.getField())) {
         throw new IllegalArgumentException(
            String.format("Invalid argument! Expected CronField instance for field %s but found %s", cronField.getField(), name)
         );
      }
   }

   private FieldConstraints getConstraint(CronFieldName name) {
      return this.cron.getCronDefinition().getFieldDefinition(name) != null
         ? this.cron.getCronDefinition().getFieldDefinition(name).getConstraints()
         : FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance();
   }
}
