package com.sk89q.worldedit.internal.expression;

public class ExpressionTimeoutException extends EvaluationException {
   public ExpressionTimeoutException(String message) {
      super(-1, message);
   }
}
