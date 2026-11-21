package me.byteful.plugin.leveltools.libs.crunch;

import java.util.ArrayDeque;
import java.util.Deque;
import me.byteful.plugin.leveltools.libs.crunch.token.BinaryOperation;
import me.byteful.plugin.leveltools.libs.crunch.token.BinaryOperator;
import me.byteful.plugin.leveltools.libs.crunch.token.LiteralValue;
import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class ShuntingYard {
   private final Deque<BinaryOperator> operators = new ArrayDeque<>();
   private final Deque<Value> stack = new ArrayDeque<>();

   public void addOperator(BinaryOperator operator) {
      while (!this.operators.isEmpty() && operator.getPriority() <= this.operators.getLast().getPriority()) {
         this.createOperation();
      }

      this.operators.add(operator);
   }

   public void addValue(Value value) {
      this.stack.add(value);
   }

   private void createOperation() {
      BinaryOperator op = this.operators.removeLast();
      Value right = this.stack.removeLast();
      Value left = this.stack.removeLast();
      if (right.getType() == TokenType.LITERAL_VALUE && left.getType() == TokenType.LITERAL_VALUE) {
         this.stack.add(new LiteralValue(op.getOperation().applyAsDouble(left.getValue(new double[0]), right.getValue(new double[0]))));
      } else {
         this.stack.add(new BinaryOperation(op, left, right));
      }
   }

   public Value finish() {
      while (this.stack.size() > 1) {
         this.createOperation();
      }

      return this.stack.removeLast();
   }
}
