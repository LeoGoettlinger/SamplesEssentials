package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.config.WaitStrategyType;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutBlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
final class WaitStrategies {
   private WaitStrategies() {
   }

   @NotNull
   static WaitStrategy resolve(@NotNull WaitStrategyType type, long timeoutBlockingMs) {
      return (WaitStrategy)(switch (type) {
         case BLOCKING -> new BlockingWaitStrategy();
         case TIMEOUT_BLOCKING -> new TimeoutBlockingWaitStrategy(Math.max(1L, timeoutBlockingMs), TimeUnit.MILLISECONDS);
         case SLEEPING -> new SleepingWaitStrategy();
         case YIELDING -> new YieldingWaitStrategy();
         case BUSY_SPIN -> new BusySpinWaitStrategy();
      });
   }
}
