package me.byteful.plugin.leveltools.libs.crunch;

import me.byteful.plugin.leveltools.libs.crunch.token.TokenType;
import me.byteful.plugin.leveltools.libs.crunch.token.Value;

public class Variable implements Value {
   private final int index;

   public Variable(int index) {
      this.index = index;
   }

   public int getIndex() {
      return this.index;
   }

   @Override
   public double getValue(double[] variableValues) {
      return variableValues[this.index];
   }

   @Override
   public TokenType getType() {
      return TokenType.VARIABLE;
   }

   @Override
   public String toString() {
      return "$" + (this.index + 1);
   }

   public Variable getClone() {
      return new Variable(this.index);
   }
}
