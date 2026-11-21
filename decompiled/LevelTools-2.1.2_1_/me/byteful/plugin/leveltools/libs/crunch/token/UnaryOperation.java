package me.byteful.plugin.leveltools.libs.crunch.token;

public class UnaryOperation implements Value {
   private final UnaryOperator operator;
   private final Value first;

   public UnaryOperation(UnaryOperator operator, Value value) {
      this.operator = operator;
      this.first = value;
   }

   public UnaryOperator getOperator() {
      return this.operator;
   }

   public Value getChild() {
      return this.first;
   }

   @Override
   public double getValue(double[] variableValues) {
      return this.operator.getOperation().applyAsDouble(this.first.getValue(variableValues));
   }

   @Override
   public TokenType getType() {
      return TokenType.UNARY_OPERATOR;
   }

   @Override
   public String toString() {
      return "(" + this.operator.getSymbol() + this.first.toString() + ")";
   }

   public UnaryOperation getClone() {
      return new UnaryOperation(this.operator, this.first.getClone());
   }
}
