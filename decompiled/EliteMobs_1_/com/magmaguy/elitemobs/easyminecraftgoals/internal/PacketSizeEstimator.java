package com.magmaguy.elitemobs.easyminecraftgoals.internal;

public final class PacketSizeEstimator {
   private static volatile PacketSizeEstimator.PacketSizeFunction impl;

   private PacketSizeEstimator() {
   }

   public static void setImpl(PacketSizeEstimator.PacketSizeFunction function) {
      impl = function;
   }

   public static boolean hasImpl() {
      return impl != null;
   }

   public static int sizeOf(Object packet) {
      PacketSizeEstimator.PacketSizeFunction function = impl;
      if (function == null) {
         return -1;
      } else {
         try {
            return function.sizeOf(packet);
         } catch (Throwable var3) {
            return -1;
         }
      }
   }

   public interface PacketSizeFunction {
      int sizeOf(Object var1);
   }
}
