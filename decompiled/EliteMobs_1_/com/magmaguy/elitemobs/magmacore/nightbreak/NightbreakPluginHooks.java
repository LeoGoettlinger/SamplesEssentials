package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.initialization.PluginInitializationContext;

public interface NightbreakPluginHooks {
   void asyncInitialization(PluginInitializationContext var1);

   void syncInitialization(PluginInitializationContext var1);

   default void onInitializationSuccess() {
   }

   default void onInitializationFailure(Throwable throwable) {
   }
}
