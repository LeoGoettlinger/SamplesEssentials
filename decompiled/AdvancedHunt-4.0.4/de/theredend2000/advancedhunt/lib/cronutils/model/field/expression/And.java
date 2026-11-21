package de.theredend2000.advancedhunt.lib.cronutils.model.field.expression;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.expression.visitor.FieldExpressionVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class And extends FieldExpression {
   private static final long serialVersionUID = -3406340596495131941L;
   private final List<FieldExpression> expressions = new ArrayList<>();

   @Override
   public And and(FieldExpression exp) {
      this.expressions.add(exp);
      return this;
   }

   @Override
   public FieldExpression accept(FieldExpressionVisitor visitor) {
      return visitor.visit(this);
   }

   @Override
   public String asString() {
      StringBuilder builder = new StringBuilder();

      for (int j = 0; j < this.expressions.size() - 1; j++) {
         builder.append(this.expressions.get(j).asString());
         builder.append(",");
      }

      if (this.expressions.size() > 0) {
         builder.append(this.expressions.get(this.expressions.size() - 1).asString());
      }

      return builder.toString();
   }

   public List<FieldExpression> getExpressions() {
      return Collections.unmodifiableList(this.expressions);
   }
}
