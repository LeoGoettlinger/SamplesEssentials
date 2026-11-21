package xyz.lychee.lagfixer.hooks;

import xyz.lychee.lagfixer.modules.AFKOptimizerModule;

interface PacketEventsBridge {
   void registerAfk(AFKOptimizerModule var1);

   void unregisterAfk();
}
