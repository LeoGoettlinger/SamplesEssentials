package de.theredend2000.advancedhunt.lib.cronutils.mapper;

import de.theredend2000.advancedhunt.lib.cronutils.Function;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.CronType;
import de.theredend2000.advancedhunt.lib.cronutils.model.RebootCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.SingleCron;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronDefinitionBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.definition.CronNicknames;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraintsBuilder;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.DayOfWeekFieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.definition.FieldDefinition;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Always;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.And;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Between;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.Every;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.On;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.QuestionMark;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitorAdaptor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CronMapper {
   private final Map<CronFieldName, Function<CronField, CronField>> mappings;
   private final Function<Cron, Cron> cronRules;
   private final CronDefinition to;

   public CronMapper(CronDefinition from, CronDefinition to, Function<Cron, Cron> cronRules) {
      Preconditions.checkNotNull(from, "Source CronDefinition must not be null");
      this.to = Preconditions.checkNotNull(to, "Destination CronDefinition must not be null");
      this.cronRules = Preconditions.checkNotNull(cronRules, "CronRules must not be null");
      this.mappings = new EnumMap<>(CronFieldName.class);
      this.buildMappings(from, to);
   }

   public Cron map(Cron cron) {
      Preconditions.checkNotNull(cron, "Cron must not be null");
      if (cron instanceof RebootCron) {
         if (this.to.getCronNicknames().contains(CronNicknames.REBOOT)) {
            return new RebootCron(this.to);
         } else {
            throw new IllegalArgumentException("The target cron definition does not support @reboot nickname");
         }
      } else {
         List<CronField> fields = new ArrayList<>();

         for (CronFieldName name : CronFieldName.values()) {
            if (this.mappings.containsKey(name)) {
               CronField field = this.mappings.get(name).apply(cron.retrieve(name));
               if (field != null) {
                  fields.add(field);
               }
            }
         }

         return this.cronRules.apply(new SingleCron(this.to, fields)).validate();
      }
   }

   public static CronMapper fromCron4jToQuartz() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J), CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), setQuestionMark()
      );
   }

   public static CronMapper fromQuartzToCron4j() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), CronDefinitionBuilder.instanceDefinitionFor(CronType.CRON4J), sameCron()
      );
   }

   public static CronMapper fromQuartzToUnix() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX), sameCron()
      );
   }

   public static CronMapper fromUnixToQuartz() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX), CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), setQuestionMark()
      );
   }

   public static CronMapper fromQuartzToSpring() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING), setQuestionMark()
      );
   }

   public static CronMapper fromSpringToQuartz() {
      return new CronMapper(
         CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING), CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ), setQuestionMark()
      );
   }

   public static CronMapper sameCron(CronDefinition cronDefinition) {
      return new CronMapper(cronDefinition, cronDefinition, sameCron());
   }

   private static Function<Cron, Cron> sameCron() {
      return cron -> cron;
   }

   private static Function<Cron, Cron> setQuestionMark() {
      return cron -> {
         CronField dow = cron.retrieve(CronFieldName.DAY_OF_WEEK);
         CronField dom = cron.retrieve(CronFieldName.DAY_OF_MONTH);
         if (dow == null && dom == null) {
            return cron;
         } else if (!(dow.getExpression() instanceof QuestionMark) && !(dom.getExpression() instanceof QuestionMark)) {
            Map<CronFieldName, CronField> fields = new EnumMap<>(CronFieldName.class);
            fields.putAll(cron.retrieveFieldsAsMap());
            if (dow.getExpression() instanceof Always) {
               fields.put(
                  CronFieldName.DAY_OF_WEEK,
                  new CronField(CronFieldName.DAY_OF_WEEK, FieldExpression.questionMark(), fields.get(CronFieldName.DAY_OF_WEEK).getConstraints())
               );
            } else if (dom.getExpression() instanceof Always) {
               fields.put(
                  CronFieldName.DAY_OF_MONTH,
                  new CronField(CronFieldName.DAY_OF_MONTH, FieldExpression.questionMark(), fields.get(CronFieldName.DAY_OF_MONTH).getConstraints())
               );
            } else {
               cron.validate();
            }

            return new SingleCron(cron.getCronDefinition(), new ArrayList<>(fields.values()));
         } else {
            return cron;
         }
      };
   }

   private void buildMappings(CronDefinition from, CronDefinition to) {
      Map<CronFieldName, FieldDefinition> sourceFieldDefinitions = this.getFieldDefinitions(from);
      Map<CronFieldName, FieldDefinition> destFieldDefinitions = this.getFieldDefinitions(to);
      boolean startedDestMapping = false;
      boolean startedSourceMapping = false;

      for (CronFieldName name : CronFieldName.values()) {
         FieldDefinition destinationFieldDefinition = destFieldDefinitions.get(name);
         FieldDefinition sourceFieldDefinition = sourceFieldDefinitions.get(name);
         if (destinationFieldDefinition != null) {
            startedDestMapping = true;
         }

         if (sourceFieldDefinition != null) {
            startedSourceMapping = true;
         }

         if (startedDestMapping && destinationFieldDefinition == null) {
            break;
         }

         if (!startedSourceMapping && destinationFieldDefinition != null) {
            this.mappings.put(name, returnOnZeroExpression(name));
         }

         if (startedSourceMapping && sourceFieldDefinition == null && destinationFieldDefinition != null) {
            this.mappings.put(name, returnAlwaysExpression(name));
         }

         if (sourceFieldDefinition != null && destinationFieldDefinition != null) {
            if (CronFieldName.DAY_OF_WEEK.equals(name)) {
               this.mappings.put(name, dayOfWeekMapping((DayOfWeekFieldDefinition)sourceFieldDefinition, (DayOfWeekFieldDefinition)destinationFieldDefinition));
            } else if (CronFieldName.DAY_OF_MONTH.equals(name)) {
               this.mappings.put(name, dayOfMonthMapping(sourceFieldDefinition, destinationFieldDefinition));
            } else {
               this.mappings.put(name, returnSameExpression());
            }
         }
      }
   }

   private Map<CronFieldName, FieldDefinition> getFieldDefinitions(CronDefinition from) {
      Map<CronFieldName, FieldDefinition> result = new EnumMap<>(CronFieldName.class);

      for (FieldDefinition fieldDefinition : from.getFieldDefinitions()) {
         result.put(fieldDefinition.getFieldName(), fieldDefinition);
      }

      return result;
   }

   @VisibleForTesting
   static Function<CronField, CronField> returnSameExpression() {
      return field -> field;
   }

   @VisibleForTesting
   static Function<CronField, CronField> returnOnZeroExpression(CronFieldName name) {
      return field -> {
         FieldConstraints constraints = FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance();
         return new CronField(name, new On(new IntegerFieldValue(0)), constraints);
      };
   }

   @VisibleForTesting
   static Function<CronField, CronField> returnAlwaysExpression(CronFieldName name) {
      return field -> new CronField(name, FieldExpression.always(), FieldConstraintsBuilder.instance().forField(name).createConstraintsInstance());
   }

   private static IntegerFieldValue mapDayOfWeek(DayOfWeekFieldDefinition sourceDef, DayOfWeekFieldDefinition targetDef, IntegerFieldValue fieldValue) {
      return new IntegerFieldValue(ConstantsMapper.weekDayMapping(sourceDef.getMondayDoWValue(), targetDef.getMondayDoWValue(), fieldValue.getValue()));
   }

   private static FieldValue<?> mapDayOfWeek(DayOfWeekFieldDefinition sourceDef, DayOfWeekFieldDefinition targetDef, FieldValue<?> fieldValue) {
      return (FieldValue<?>)(fieldValue instanceof IntegerFieldValue ? mapDayOfWeek(sourceDef, targetDef, (IntegerFieldValue)fieldValue) : fieldValue);
   }

   @VisibleForTesting
   static Function<CronField, CronField> dayOfWeekMapping(DayOfWeekFieldDefinition sourceDef, DayOfWeekFieldDefinition targetDef) {
      return field -> {
         FieldExpression expression = field.getExpression();
         FieldExpression dest = null;
         dest = expression.accept(
            new FieldExpressionVisitorAdaptor() {
               @Override
               public FieldExpression visit(Every every) {
                  return new Every(every.getExpression().accept(this), every.getPeriod());
               }

               @Override
               public FieldExpression visit(On on) {
                  return new On(CronMapper.mapDayOfWeek(sourceDef, targetDef, on.getTime()), on.getSpecialChar());
               }

               @Override
               public FieldExpression visit(Between between) {
                  return new Between(
                     CronMapper.mapDayOfWeek(sourceDef, targetDef, between.getFrom()), CronMapper.mapDayOfWeek(sourceDef, targetDef, between.getTo())
                  );
               }

               @Override
               public FieldExpression visit(And and) {
                  And newAnd = new And();

                  for (FieldExpression expr : and.getExpressions()) {
                     newAnd.and(expr.accept(this));
                  }

                  return newAnd;
               }
            }
         );
         if (expression instanceof QuestionMark && !targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)) {
            dest = FieldExpression.always();
         }

         return new CronField(CronFieldName.DAY_OF_WEEK, dest, targetDef.getConstraints());
      };
   }

   @VisibleForTesting
   static Function<CronField, CronField> dayOfMonthMapping(FieldDefinition sourceDef, FieldDefinition targetDef) {
      return field -> {
         FieldExpression expression = field.getExpression();
         FieldExpression dest = expression;
         if (expression instanceof QuestionMark && !targetDef.getConstraints().getSpecialChars().contains(SpecialChar.QUESTION_MARK)) {
            dest = FieldExpression.always();
         }

         return new CronField(CronFieldName.DAY_OF_MONTH, dest, targetDef.getConstraints());
      };
   }
}
