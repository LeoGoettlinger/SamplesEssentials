package de.theredend2000.advancedhunt.lib.cronutils.parser;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronField;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.CronFieldName;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.util.Comparator;

public class CronParserField {
   private final CronFieldName field;
   private final FieldConstraints constraints;
   private final FieldParser parser;
   private final boolean optional;

   public CronParserField(CronFieldName fieldName, FieldConstraints constraints) {
      this(fieldName, constraints, false);
   }

   public CronParserField(CronFieldName fieldName, FieldConstraints constraints, boolean optional) {
      this.field = Preconditions.checkNotNull(fieldName, "CronFieldName must not be null");
      this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
      this.parser = new FieldParser(constraints);
      this.optional = optional;
   }

   public CronFieldName getField() {
      return this.field;
   }

   public final boolean isOptional() {
      return this.optional;
   }

   public CronField parse(String expression) {
      String newExpression = expression;
      if (this.getField().equals(CronFieldName.DAY_OF_WEEK) && expression.endsWith("L")) {
         Integer value = this.constraints.getStringMappingValue(expression.substring(0, expression.length() - 1));
         if (value != null) {
            newExpression = value + "L";
         }
      }

      return new CronField(this.field, this.parser.parse(newExpression), this.constraints);
   }

   public static Comparator<CronParserField> createFieldTypeComparator() {
      return Comparator.comparingInt(o -> o.getField().getOrder());
   }

   @Override
   public String toString() {
      return "CronParserField{field=" + this.field + '}';
   }
}
