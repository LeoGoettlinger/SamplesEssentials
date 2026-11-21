package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public final class ServerOwnershipGate {
   private final boolean enforced;
   private volatile boolean open;
   private volatile long deadlineNanos;
   @Nullable
   private volatile UUID startupId;
   @Nullable
   private volatile UUID fence;
   @NotNull
   private volatile String closeReason = "not-open";

   public ServerOwnershipGate(boolean enforced) {
      this.enforced = enforced;
      this.open = !enforced;
      this.deadlineNanos = Long.MAX_VALUE;
   }

   @NotNull
   public static ServerOwnershipGate disabled() {
      return new ServerOwnershipGate(false);
   }

   public boolean enforced() {
      return this.enforced;
   }

   public void open(@NotNull UUID startupId, @NotNull UUID fence, long ttlMs, long safetyMarginMs) {
      if (this.enforced) {
         this.startupId = startupId;
         this.fence = fence;
         this.deadlineNanos = deadlineFromNow(ttlMs, safetyMarginMs);
         this.closeReason = "open";
         this.open = true;
      }
   }

   public void extend(@NotNull UUID startupId, @NotNull UUID fence, long ttlMs, long safetyMarginMs) {
      if (this.enforced) {
         if (startupId.equals(this.startupId) && fence.equals(this.fence)) {
            this.deadlineNanos = deadlineFromNow(ttlMs, safetyMarginMs);
            this.closeReason = "open";
            this.open = true;
         } else {
            this.close("ownership-token-mismatch");
         }
      }
   }

   public void close(@NotNull String reason) {
      if (this.enforced) {
         this.open = false;
         this.deadlineNanos = 0L;
         this.closeReason = reason;
      }
   }

   public boolean allowWrites() {
      if (!this.enforced) {
         return true;
      } else if (!this.open) {
         return false;
      } else if (System.nanoTime() <= this.deadlineNanos) {
         return true;
      } else {
         this.close("local-lease-deadline-expired");
         return false;
      }
   }

   @NotNull
   public String closeReason() {
      return this.closeReason;
   }

   private static long deadlineFromNow(long ttlMs, long safetyMarginMs) {
      long usableMs = Math.max(1L, ttlMs - Math.max(0L, safetyMarginMs));
      long now = System.nanoTime();

      long delta;
      try {
         delta = Math.multiplyExact(usableMs, 1000000L);
      } catch (ArithmeticException var12) {
         return Long.MAX_VALUE;
      }

      long deadline = now + delta;
      return deadline < 0L ? Long.MAX_VALUE : deadline;
   }
}
