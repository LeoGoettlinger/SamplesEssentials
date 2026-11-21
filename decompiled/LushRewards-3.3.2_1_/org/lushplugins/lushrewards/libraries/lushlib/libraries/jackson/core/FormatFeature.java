package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.core.util.JacksonFeature;

public interface FormatFeature extends JacksonFeature {
   @Override
   boolean enabledByDefault();

   @Override
   int getMask();

   @Override
   boolean enabledIn(int var1);
}
