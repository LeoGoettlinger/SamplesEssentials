package me.byteful.plugin.leveltools.libs.crunch.token;

public interface Value extends Token, Cloneable {
   double getValue(double[] var1);

   Value getClone();
}
