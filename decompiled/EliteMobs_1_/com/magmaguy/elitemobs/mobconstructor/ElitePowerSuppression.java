package com.magmaguy.elitemobs.mobconstructor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class ElitePowerSuppression implements AutoCloseable {
   private final Map<ElitePowerPauseReason, Integer> leaseCounts = new EnumMap<>(ElitePowerPauseReason.class);
   private final ElitePowerSuppression.TransitionListener transitionListener;
   private boolean closed;

   ElitePowerSuppression(ElitePowerSuppression.TransitionListener transitionListener) {
      this.transitionListener = Objects.requireNonNull(transitionListener, "transitionListener");
   }

   public ElitePowerSuppression.Lease acquire(ElitePowerPauseReason reason) {
      Objects.requireNonNull(reason, "reason");
      if (this.closed) {
         throw new IllegalStateException("Elite power suppression is closed");
      } else {
         int current = this.leaseCounts.getOrDefault(reason, 0);
         if (current == Integer.MAX_VALUE) {
            throw new IllegalStateException("Elite power suppression lease count is exhausted");
         } else {
            this.leaseCounts.put(reason, current + 1);
            if (current == 0) {
               try {
                  this.transitionListener.changed(reason, true);
               } catch (RuntimeException var4) {
                  this.leaseCounts.remove(reason);
                  throw var4;
               }
            }

            return new ElitePowerSuppression.LeaseImpl(reason);
         }
      }
   }

   public boolean isSuppressed() {
      return !this.leaseCounts.isEmpty();
   }

   public boolean isSuppressed(ElitePowerPauseReason reason) {
      return this.leaseCounts.containsKey(Objects.requireNonNull(reason, "reason"));
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         RuntimeException failure = null;

         for (ElitePowerPauseReason reason : new ArrayList<>(this.leaseCounts.keySet())) {
            this.leaseCounts.remove(reason);

            try {
               this.transitionListener.changed(reason, false);
            } catch (RuntimeException var5) {
               if (failure == null) {
                  failure = var5;
               } else {
                  failure.addSuppressed(var5);
               }
            }
         }

         if (failure != null) {
            throw failure;
         }
      }
   }

   private void release(ElitePowerPauseReason reason) {
      Integer current = this.leaseCounts.get(reason);
      if (current != null) {
         if (current > 1) {
            this.leaseCounts.put(reason, current - 1);
         } else {
            this.leaseCounts.remove(reason);
            this.transitionListener.changed(reason, false);
         }
      }
   }

   public interface Lease extends AutoCloseable {
      @Override
      void close();
   }

   private final class LeaseImpl implements ElitePowerSuppression.Lease {
      private final ElitePowerPauseReason reason;
      private boolean released;

      private LeaseImpl(ElitePowerPauseReason reason) {
         this.reason = reason;
      }

      @Override
      public void close() {
         if (!this.released) {
            this.released = true;
            ElitePowerSuppression.this.release(this.reason);
         }
      }
   }

   @FunctionalInterface
   interface TransitionListener {
      void changed(ElitePowerPauseReason var1, boolean var2);
   }
}
