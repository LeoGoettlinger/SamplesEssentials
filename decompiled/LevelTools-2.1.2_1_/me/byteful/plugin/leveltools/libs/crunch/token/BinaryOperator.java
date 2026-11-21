package me.byteful.plugin.leveltools.libs.crunch.token;

import java.util.function.DoubleBinaryOperator;

public enum BinaryOperator implements Token {
   BOOLEAN_OR("|", 0, (a, b) -> a != 1.0 && b != 1.0 ? 0.0 : 1.0),
   BOOLEAN_OR_ALT("||", 0, (a, b) -> a != 1.0 && b != 1.0 ? 0.0 : 1.0),
   BOOLEAN_AND("&", 0, (a, b) -> a == 1.0 && b == 1.0 ? 1.0 : 0.0),
   BOOLEAN_AND_ALT("&&", 0, (a, b) -> a == 1.0 && b == 1.0 ? 1.0 : 0.0),
   GREATER_THAN(">", 1, (a, b) -> a > b ? 1.0 : 0.0),
   LESS_THAN("<", 1, (a, b) -> a < b ? 1.0 : 0.0),
   EQUAL_TO("=", 1, (a, b) -> a == b ? 1.0 : 0.0),
   EQUAL_TO_ALT("==", 1, (a, b) -> a == b ? 1.0 : 0.0),
   NOT_EQUAL_TO("!=", 1, (a, b) -> a != b ? 1.0 : 0.0),
   GREATER_THAN_OR_EQUAL_TO(">=", 1, (a, b) -> a >= b ? 1.0 : 0.0),
   LESS_THAN_OR_EQUAL_TO("<=", 1, (a, b) -> a <= b ? 1.0 : 0.0),
   EXPONENT("^", 5, (a, b) -> Math.pow(a, b)),
   MULTIPLY("*", 4, (a, b) -> a * b),
   DIVIDE("/", 4, (a, b) -> a / b),
   MODULUS("%", 4, (a, b) -> a % b),
   ADD("+", 3, (a, b) -> a + b),
   SUBTRACT("-", 3, (a, b) -> a - b),
   SCIENTIFIC_NOTATION("E", 5, (a, b) -> a * Math.pow(10.0, b));

   private final String symbol;
   private final DoubleBinaryOperator operation;
   private final int priority;

   private BinaryOperator(String name, int priority, DoubleBinaryOperator operation) {
      this.symbol = name;
      this.operation = operation;
      this.priority = priority;
   }

   @Override
   public TokenType getType() {
      return TokenType.BINARY_OPERATOR;
   }

   @Override
   public String toString() {
      return this.symbol;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public DoubleBinaryOperator getOperation() {
      return this.operation;
   }

   public int getPriority() {
      return this.priority;
   }
}
