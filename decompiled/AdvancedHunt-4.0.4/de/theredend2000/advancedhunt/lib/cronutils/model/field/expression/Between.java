package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.FieldValue;

public class Between extends FieldExpression {
   private static final long serialVersionUID = 549075258664100474L;
   private final FieldValue<?> from;
   private final FieldValue<?> to;

   public Between(Between between) {
      this(between.getFrom(), between.getTo());
   }

   public Between(FieldValue<?> from, FieldValue<?> to) {
      this.from = from;
      this.to = to;
   }

   public FieldValue<?> getFrom() {
      return this.from;
   }

   public FieldValue<?> getTo() {
      return this.to;
   }

   @Override
   public FieldExpression accept(FieldExpressionVisitor visitor) {
      return visitor.visit(this);
   }

   @Override
   public String asString() {
      return String.format("%s-%s", this.from, this.to);
   }
}
