package com.github.jikoo.openinv.lib.nahu.scheduler-wrapper;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.util.LazyValue;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class FoliaWrappedJavaPlugin extends WrappedJavaPlugin {
   private final LazyValue<WrappedScheduler> scheduler = new LazyValue<>(() -> WrappedSchedulerBuilder.builder().plugin(this).build());

   @NotNull
   @Override
   public WrappedScheduler getScheduler() {
      return Objects.requireNonNull(this.scheduler.getValue(), "WrappedScheduler has not been initialized!");
   }
}
