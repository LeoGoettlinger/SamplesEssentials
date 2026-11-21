package me.byteful.plugin.leveltools.libs.crunch.functional;

import java.util.function.ToDoubleFunction;
import me.byteful.plugin.leveltools.libs.crunch.token.Token;
import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;

public class Function implements Token {
   private final String name;
   private final int argCount;
   private final ToDoubleFunction<double[]> function;

   public Function(String name, int argCount, ToDoubleFunction<double[]> function) {
      this.function = function;
      this.name = name;
      this.argCount = argCount;
   }

   public String getName() {
      return this.name;
   }

   public int getArgCount() {
      return this.argCount;
   }

   public double call(double[] values) {
      return this.function.applyAsDouble(values);
   }

   @Override
   public TokenType getType() {
      return TokenType.FUNCTION;
   }
}
