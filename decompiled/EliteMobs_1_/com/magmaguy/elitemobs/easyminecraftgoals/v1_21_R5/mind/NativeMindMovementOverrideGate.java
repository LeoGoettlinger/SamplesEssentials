package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.mind;

final class NativeMindMovementOverrideGate {
   private int leases;

   NativeMindMovementOverrideGate.Lease acquire() {
      if (this.leases == Integer.MAX_VALUE) {
         throw new IllegalStateException("Native Mind movement override lease count is exhausted");
      } else {
         this.leases++;
         return new NativeMindMovementOverrideGate.Lease();
      }
   }

   boolean isOverridden() {
      return this.leases > 0;
   }

   final class Lease implements AutoCloseable {
      private boolean closed;

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;
            if (NativeMindMovementOverrideGate.this.leases <= 0) {
               throw new IllegalStateException("Native Mind movement override lease underflow");
            } else {
               NativeMindMovementOverrideGate.this.leases--;
            }
         }
      }
   }
}
