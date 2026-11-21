package com.magmaguy.elitemobs.easyminecraftgoals.internal;

public final class PacketSendObserver {
   private static volatile PacketSendObserver.Observer impl;

   private PacketSendObserver() {
   }

   public static void setImpl(PacketSendObserver.Observer observer) {
      impl = observer;
   }

   public static boolean isActive() {
      return impl != null;
   }

   public static void observe(Object packet, int viewerCount) {
      PacketSendObserver.Observer observer = impl;
      if (observer != null && packet != null && viewerCount > 0) {
         try {
            observer.onSend(packet, viewerCount);
         } catch (Throwable var4) {
         }
      }
   }

   public interface Observer {
      void onSend(Object var1, int var2);
   }
}
