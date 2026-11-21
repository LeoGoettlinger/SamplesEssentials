package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;

public class ExpressionMask extends AbstractMask {
   private final Expression expression;
   private final IntSupplier timeout;

   public ExpressionMask(String expression) throws ExpressionException {
      this(Expression.compile((String)Preconditions.checkNotNull(expression), "x", "y", "z"));
   }

   public ExpressionMask(Expression expression) {
      this(expression, null);
   }

   public ExpressionMask(Expression expression, @Nullable IntSupplier timeout) {
      Preconditions.checkNotNull(expression);
      this.expression = expression;
      this.timeout = timeout;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      try {
         if (this.expression.getEnvironment() instanceof WorldEditExpressionEnvironment) {
            ((WorldEditExpressionEnvironment)this.expression.getEnvironment()).setCurrentBlock(vector.toVector3());
         }

         return this.timeout == null
            ? this.expression.evaluate(vector.x(), vector.y(), vector.z()) > 0.0
            : this.expression.evaluate(new double[]{vector.x(), vector.y(), vector.z()}, this.timeout.getAsInt()) > 0.0;
      } catch (EvaluationException var3) {
         return false;
      }
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return new ExpressionMask2D(this.expression, this.timeout);
   }

   @Override
   public Mask copy() {
      return new ExpressionMask(this.expression.clone(), this.timeout);
   }
}
