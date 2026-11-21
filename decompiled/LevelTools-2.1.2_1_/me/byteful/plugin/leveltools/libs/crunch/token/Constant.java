package me.byteful.plugin.leveltools.libs.crunch.token;

import java.util.Locale;

public enum Constant implements Value {
   PI(Math.PI),
   E(Math.E),
   TRUE(1.0),
   FALSE(0.0);

   private final double value;

   private Constant(double value) {
      this.value = value;
   }

   @Override
   public TokenType getType() {
      return TokenType.LITERAL_VALUE;
   }

   @Override
   public double getValue(double[] variableValues) {
      return this.value;
   }

   @Override
   public Value getClone() {
      return this;
   }

   @Override
   public String toString() {
      return super.toString().toLowerCase(Locale.ROOT);
   }
}
