package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util;

public interface JacksonFeature {
   boolean enabledByDefault();

   int getMask();

   boolean enabledIn(int var1);
}
