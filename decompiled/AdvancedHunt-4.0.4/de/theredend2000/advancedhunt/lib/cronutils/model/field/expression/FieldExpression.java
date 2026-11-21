package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import java.io.Serializable;

public abstract class FieldExpression implements Serializable {
   private static final long serialVersionUID = 5138279438874391617L;

   public And and(FieldExpression exp) {
      return new And().and(this).and(exp);
   }

   public abstract String asString();

   public abstract FieldExpression accept(FieldExpressionVisitor var1);

   public static FieldExpression always() {
      return Always.INSTANCE;
   }

   public static FieldExpression questionMark() {
      return QuestionMark.INSTANCE;
   }
}
