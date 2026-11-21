package me.byteful.plugin.leveltools.libs.crunch.token;

public class BinaryOperation implements Value {
   private final BinaryOperator operator;
   private final Value first;
   private final Value second;

   public BinaryOperation(BinaryOperator operator, Value first, Value second) {
      this.operator = operator;
      this.first = first;
      this.second = second;
   }

   public BinaryOperator getOperator() {
      return this.operator;
   }

   public Value[] getValues() {
      return new Value[]{this.first, this.second};
   }

   @Override
   public double getValue(double[] variableValues) {
      return this.operator.getOperation().applyAsDouble(this.first.getValue(variableValues), this.second.getValue(variableValues));
   }

   @Override
   public TokenType getType() {
      return TokenType.BINARY_OPERATION;
   }

   @Override
   public String toString() {
      return "(" + this.first.toString() + this.operator.getSymbol() + this.second.toString() + ")";
   }

   public BinaryOperation getClone() {
      return new BinaryOperation(this.operator, this.first.getClone(), this.second.getClone());
   }
}
