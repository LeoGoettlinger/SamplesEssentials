package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector2;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;

public class ExpressionMask2D extends AbstractMask2D {
   private final Expression expression;
   private final IntSupplier timeout;

   public ExpressionMask2D(String expression) throws ExpressionException {
      this(Expression.compile((String)Preconditions.checkNotNull(expression), "x", "z"));
   }

   public ExpressionMask2D(Expression expression) {
      this(expression, null);
   }

   public ExpressionMask2D(Expression expression, @Nullable IntSupplier timeout) {
      Preconditions.checkNotNull(expression);
      this.expression = expression;
      this.timeout = timeout;
   }

   @Override
   public boolean test(BlockVector2 vector) {
      try {
         return this.timeout == null
            ? this.expression.evaluate(vector.x(), 0.0, vector.z()) > 0.0
            : this.expression.evaluate(this.timeout.getAsInt(), vector.x(), 0.0, vector.z()) > 0.0;
      } catch (EvaluationException var3) {
         return false;
      }
   }

   @Override
   public Mask2D copy2D() {
      return new ExpressionMask2D(this.expression.clone(), this.timeout);
   }
}
