package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;

public final class QuestionMark extends FieldExpression {
   private static final long serialVersionUID = -3043597498019873616L;
   static final QuestionMark INSTANCE = new QuestionMark();

   private QuestionMark() {
   }

   @Override
   public FieldExpression accept(FieldExpressionVisitor visitor) {
      return visitor.visit(this);
   }

   @Override
   public String asString() {
      return "?";
   }

   @Override
   public String toString() {
      return "QuestionMark{}";
   }
}
