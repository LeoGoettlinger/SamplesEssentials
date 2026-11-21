package me.byteful.plugin.leveltools.libs.crunch;

import me.byteful.plugin.leveltools.libs.crunch.functional.ExpressionEnv;

public final class Crunch {
   private static final ExpressionEnv DEFAULT_EVALUATION_ENVIRONMENT = new ExpressionEnv();

   private Crunch() {
   }

   public static CompiledExpression compileExpression(String expression) {
      return compileExpression(expression, DEFAULT_EVALUATION_ENVIRONMENT);
   }

   public static CompiledExpression compileExpression(String expression, ExpressionEnv env) {
      return new ExpressionParser(expression, env).parse();
   }

   public static double evaluateExpression(String expression, double... varValues) {
      return compileExpression(expression).evaluate(varValues);
   }
}
