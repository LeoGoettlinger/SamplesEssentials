package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg;

public interface ConfigFeature {
   boolean enabledByDefault();

   int getMask();

   boolean enabledIn(int var1);
}
