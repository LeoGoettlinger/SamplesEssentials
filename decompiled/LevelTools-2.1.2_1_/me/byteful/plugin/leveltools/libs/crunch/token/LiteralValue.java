package me.byteful.plugin.leveltools.libs.crunch.token;

public class LiteralValue implements Value {
   private final double value;

   public LiteralValue(double value) {
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
   public String toString() {
      return this.value + "";
   }

   public LiteralValue getClone() {
      return new LiteralValue(this.value);
   }
}
