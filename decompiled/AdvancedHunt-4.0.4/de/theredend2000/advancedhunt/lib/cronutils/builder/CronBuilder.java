package de.theredend2000.advancedhunt.lib.cronutils.builder;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.RebootCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.SingleCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class CronBuilder {
   private final Map<CronFieldName, CronField> fields = new EnumMap<>(CronFieldName.class);
   private final CronDefinition definition;

   private CronBuilder(CronDefinition definition) {
      this.definition = definition;
   }

   public static CronBuilder cron(CronDefinition definition) {
      return new CronBuilder(definition);
   }

   public CronBuilder withDoY(FieldExpression expression) {
      return this.addField(CronFieldName.DAY_OF_YEAR, expression);
   }

   public CronBuilder withYear(FieldExpression expression) {
      return this.addField(CronFieldName.YEAR, expression);
   }

   public CronBuilder withDoM(FieldExpression expression) {
      return this.addField(CronFieldName.DAY_OF_MONTH, expression);
   }

   public CronBuilder withMonth(FieldExpression expression) {
      return this.addField(CronFieldName.MONTH, expression);
   }

   public CronBuilder withDoW(FieldExpression expression) {
      return this.addField(CronFieldName.DAY_OF_WEEK, expression);
   }

   public CronBuilder withHour(FieldExpression expression) {
      return this.addField(CronFieldName.HOUR, expression);
   }

   public CronBuilder withMinute(FieldExpression expression) {
      return this.addField(CronFieldName.MINUTE, expression);
   }

   public CronBuilder withSecond(FieldExpression expression) {
      return this.addField(CronFieldName.SECOND, expression);
   }

   public Cron instance() {
      return new SingleCron(this.definition, new ArrayList<>(this.fields.values())).validate();
   }

   public static Cron yearly(CronDefinition definition) {
      CronBuilder builder = new CronBuilder(definition);
      if (definition.containsFieldDefinition(CronFieldName.SECOND)) {
         builder = builder.withSecond(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MINUTE)) {
         builder = builder.withMinute(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.HOUR)) {
         builder = builder.withHour(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_MONTH)) {
         builder = builder.withDoM(new On(new IntegerFieldValue(1)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MONTH)) {
         builder = builder.withMonth(new On(new IntegerFieldValue(1)));
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_WEEK)) {
         builder = builder.withDoW(FieldExpression.always());
      }

      return builder.instance();
   }

   public static Cron annually(CronDefinition definition) {
      return yearly(definition);
   }

   public static Cron monthly(CronDefinition definition) {
      CronBuilder builder = new CronBuilder(definition);
      if (definition.containsFieldDefinition(CronFieldName.SECOND)) {
         builder = builder.withSecond(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MINUTE)) {
         builder = builder.withMinute(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.HOUR)) {
         builder = builder.withHour(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_MONTH)) {
         builder = builder.withDoM(new On(new IntegerFieldValue(1)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MONTH)) {
         builder = builder.withMonth(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_WEEK)) {
         builder = builder.withDoW(FieldExpression.always());
      }

      return builder.instance();
   }

   public static Cron weekly(CronDefinition definition) {
      CronBuilder builder = new CronBuilder(definition);
      if (definition.containsFieldDefinition(CronFieldName.SECOND)) {
         builder = builder.withSecond(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MINUTE)) {
         builder = builder.withMinute(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.HOUR)) {
         builder = builder.withHour(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_MONTH)) {
         builder = builder.withDoM(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.MONTH)) {
         builder = builder.withMonth(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_WEEK)) {
         builder = builder.withDoW(new On(new IntegerFieldValue(0)));
      }

      return builder.instance();
   }

   public static Cron daily(CronDefinition definition) {
      CronBuilder builder = new CronBuilder(definition);
      if (definition.containsFieldDefinition(CronFieldName.SECOND)) {
         builder = builder.withSecond(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MINUTE)) {
         builder = builder.withMinute(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.HOUR)) {
         builder = builder.withHour(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_MONTH)) {
         builder = builder.withDoM(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.MONTH)) {
         builder = builder.withMonth(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_WEEK)) {
         builder = builder.withDoW(FieldExpression.always());
      }

      return builder.instance();
   }

   public static Cron midnight(CronDefinition definition) {
      return daily(definition);
   }

   public static Cron hourly(CronDefinition definition) {
      CronBuilder builder = new CronBuilder(definition);
      if (definition.containsFieldDefinition(CronFieldName.SECOND)) {
         builder = builder.withSecond(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.MINUTE)) {
         builder = builder.withMinute(new On(new IntegerFieldValue(0)));
      }

      if (definition.containsFieldDefinition(CronFieldName.HOUR)) {
         builder = builder.withHour(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_MONTH)) {
         builder = builder.withDoM(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.MONTH)) {
         builder = builder.withMonth(FieldExpression.always());
      }

      if (definition.containsFieldDefinition(CronFieldName.DAY_OF_WEEK)) {
         builder = builder.withDoW(FieldExpression.always());
      }

      return builder.instance();
   }

   public static Cron reboot(CronDefinition definition) {
      return new RebootCron(definition);
   }

   @VisibleForTesting
   CronBuilder addField(CronFieldName name, FieldExpression expression) {
      Preconditions.checkState(this.definition != null, "CronBuilder not initialized.");
      FieldDefinition fieldDefinition = this.definition.getFieldDefinition(name);
      Preconditions.checkState(fieldDefinition != null, "Cron field definition does not exist: %s", name);
      FieldConstraints constraints = fieldDefinition.getConstraints();
      expression.accept(new ValidationFieldExpressionVisitor(constraints));
      this.fields.put(name, new CronField(name, expression, constraints));
      return this;
   }
}
