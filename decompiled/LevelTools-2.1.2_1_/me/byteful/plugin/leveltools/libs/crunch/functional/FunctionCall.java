package me.byteful.plugin.leveltools.libs.crunch.functional;

import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class FunctionCall implements Value {
   private final Value[] values;
   private final Function function;
   private final double[] numbers;

   public FunctionCall(Function function, Value[] values) {
      this.function = function;
      this.values = values;
      this.numbers = new double[function.getArgCount()];
   }

   @Override
   public TokenType getType() {
      return TokenType.FUNCTION_CALL;
   }

   @Override
   public double getValue(double[] variableValues) {
      for (int i = 0; i < this.values.length; i++) {
         this.numbers[i] = this.values[i].getValue(variableValues);
      }

      return this.function.call(this.numbers);
   }

   @Override
   public Value getClone() {
      Value[] clone = new Value[this.values.length];
      System.arraycopy(this.values, 0, clone, 0, this.values.length);
      return new FunctionCall(this.function, this.values);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(this.function.getName()).append('(');

      for (int i = 0; i < this.values.length; i++) {
         Value arg = this.values[i];
         builder.append(arg.toString());
         if (i != this.values.length - 1) {
            builder.append(", ");
         }
      }

      return builder.append(')').toString();
   }
}
