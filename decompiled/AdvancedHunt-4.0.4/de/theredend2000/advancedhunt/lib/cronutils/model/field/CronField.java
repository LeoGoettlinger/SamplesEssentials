package de.theredend2000.advancedhunt.lib.cronutils.model.field;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.FieldExpression;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;
import java.io.Serializable;
import java.util.Comparator;

public class CronField implements Serializable {
   private static final long serialVersionUID = -4042499846855256325L;
   private final CronFieldName field;
   private final FieldExpression expression;
   private final FieldConstraints constraints;

   public CronField(CronFieldName field, FieldExpression expression, FieldConstraints constraints) {
      this.field = field;
      this.expression = Preconditions.checkNotNull(expression, "FieldExpression must not be null");
      this.constraints = Preconditions.checkNotNull(constraints, "FieldConstraints must not be null");
   }

   public CronFieldName getField() {
      return this.field;
   }

   public FieldExpression getExpression() {
      return this.expression;
   }

   public FieldConstraints getConstraints() {
      return this.constraints;
   }

   public static Comparator<CronField> createFieldComparator() {
      return Comparator.comparingInt(o -> o.getField().getOrder());
   }

   @Override
   public String toString() {
      return "CronField{field=" + this.field + '}';
   }
}
