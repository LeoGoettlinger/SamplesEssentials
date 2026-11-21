package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.IntegerFieldValue;
import de.theredend2000.advancedhunt.lib.cronutils.utils.Preconditions;

public class Every extends FieldExpression {
   private static final long serialVersionUID = -1103196842332906994L;
   private final FieldExpression expression;
   private final IntegerFieldValue period;

   public Every(IntegerFieldValue time) {
      this(always(), time);
   }

   public Every(FieldExpression expression, IntegerFieldValue period) {
      this.expression = Preconditions.checkNotNull(expression, "Expression must not be null");
      this.period = period == null ? new IntegerFieldValue(1) : period;
   }

   public IntegerFieldValue getPeriod() {
      return this.period;
   }

   public FieldExpression getExpression() {
      return this.expression;
   }

   @Override
   public FieldExpression accept(FieldExpressionVisitor visitor) {
      return visitor.visit(this);
   }

   @Override
   public String asString() {
      String expressionAsString = this.expression.asString();
      return "*".equals(expressionAsString) && this.period.getValue() == 1 ? expressionAsString : String.format("%s/%s", expressionAsString, this.period);
   }
}
