package com.skyblockexp.ezrtp.platform;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public final class PlatformSenderBridgeRegistry {
   private static final AtomicReference<PlatformSenderBridge> BRIDGE = new AtomicReference<>();
   private static final List<PlatformSenderBridgeProvider> PROVIDERS = new CopyOnWriteArrayList<>();

   private PlatformSenderBridgeRegistry() {
   }

   public static void register(PlatformSenderBridge bridge) {
      BRIDGE.set(bridge);
   }

   public static void unregister() {
      BRIDGE.set(null);
   }

   public static PlatformSenderBridge get() {
      return BRIDGE.get();
   }

   public static void registerProvider(PlatformSenderBridgeProvider provider) {
      if (provider != null) {
         if (!hasProviderClass(provider.getClass())) {
            PROVIDERS.add(provider);
         }
      }
   }

   static boolean hasProviderClass(Class<?> providerClass) {
      if (providerClass == null) {
         return false;
      } else {
         for (PlatformSenderBridgeProvider provider : PROVIDERS) {
            if (provider.getClass().equals(providerClass)) {
               return true;
            }
         }

         return false;
      }
   }

   static int providerCount() {
      return PROVIDERS.size();
   }

   public static void clearProviders() {
      PROVIDERS.clear();
   }

   public static boolean loadAndRegister(Plugin plugin, Logger logger) {
      PlatformSenderBridgeProvider selected = null;

      for (PlatformSenderBridgeProvider provider : PROVIDERS) {
         if (provider.supports(plugin) && (selected == null || provider.priority() > selected.priority())) {
            selected = provider;
         }
      }

      if (selected == null) {
         return false;
      } else {
         PlatformSenderBridge bridge = selected.create(plugin);
         if (bridge == null) {
            return false;
         } else {
            register(bridge);
            if (logger != null) {
               logger.fine("Registered PlatformSenderBridge provider: " + selected.getClass().getName());
            }

            return true;
         }
      }
   }

   public static void closeAndUnregister() {
      PlatformSenderBridge bridge = BRIDGE.getAndSet(null);
      if (bridge != null) {
         bridge.close();
      }
   }
}
