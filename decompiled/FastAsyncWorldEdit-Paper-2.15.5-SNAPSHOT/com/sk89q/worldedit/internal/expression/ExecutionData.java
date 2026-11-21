package com.sk89q.worldedit.internal.expression;

import java.time.Instant;
import java.util.Objects;

public record ExecutionData(SlotTable slots, Functions functions, Instant deadline) {
   public static final ExecutionData CONSTANT_EVALUATOR = new ExecutionData(null, null, Instant.MAX);

   public SlotTable slots() {
      return Objects.requireNonNull(this.slots, "Cannot use variables in a constant");
   }

   public Functions functions() {
      return Objects.requireNonNull(this.functions, "Cannot use functions in a constant");
   }

   public void checkDeadline() {
      if (Instant.now().isAfter(this.deadline)) {
         throw new ExpressionTimeoutException("Calculations exceeded time limit.");
      }
   }
}
