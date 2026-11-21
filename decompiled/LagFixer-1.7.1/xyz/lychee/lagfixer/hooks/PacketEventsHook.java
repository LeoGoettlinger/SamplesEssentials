package xyz.lychee.lagfixer.hooks;

import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.modules.AFKOptimizerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class PacketEventsHook extends AbstractHook {
   private PacketEventsBridge bridge;

   public PacketEventsHook(LagFixer plugin, HookManager manager) {
      super(plugin, "packetevents", manager);
   }

   @Override
   public void load() {
      this.bridge = new PacketEventsImplementation();
   }

   public void register(AFKOptimizerModule module) {
      if (this.bridge != null) {
         this.bridge.registerAfk(module);
      }
   }

   public void unregisterAfkOptimizer() {
      if (this.bridge != null) {
         this.bridge.unregisterAfk();
      }
   }

   @Override
   public void disable() {
      this.unregisterAfkOptimizer();
   }
}
