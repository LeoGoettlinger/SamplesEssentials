package com.fastasyncworldedit.core.function.pattern;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.expression.EvaluationException;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

public class ExpressionPattern extends AbstractPattern {
   private final Expression expression;

   public ExpressionPattern(String input) throws ExpressionException {
      Preconditions.checkNotNull(input);
      this.expression = Expression.compile(input, "x", "y", "z");
   }

   public ExpressionPattern(Expression expression) {
      Preconditions.checkNotNull(expression);
      this.expression = expression;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 vector) {
      try {
         if (this.expression.getEnvironment() instanceof WorldEditExpressionEnvironment) {
            ((WorldEditExpressionEnvironment)this.expression.getEnvironment()).setCurrentBlock(vector.toVector3());
         }

         double combined = this.expression.evaluate(vector.x(), vector.y(), vector.z());
         return BlockState.getFromOrdinal((int)combined).toBaseBlock();
      } catch (EvaluationException var4) {
         var4.printStackTrace();
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      }
   }

   @Override
   public Pattern fork() {
      return new ExpressionPattern(this.expression.clone());
   }
}
