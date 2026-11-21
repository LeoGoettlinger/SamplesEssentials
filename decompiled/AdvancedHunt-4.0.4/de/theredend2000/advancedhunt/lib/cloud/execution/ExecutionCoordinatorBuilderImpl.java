package de.theredend2000.advancedhunt.lib.cloud.execution;

import java.util.Objects;
import java.util.concurrent.Executor;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
final class ExecutionCoordinatorBuilderImpl<C> implements ExecutionCoordinator.Builder<C> {
   @Nullable
   private Executor parsingExecutor;
   @Nullable
   private Executor suggestionsExecutor;
   @Nullable
   private Executor executionSchedulingExecutor;
   private boolean synchronizeExecution = false;

   @NonNull
   @Override
   public ExecutionCoordinator.Builder<C> parsingExecutor(@NonNull final Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.parsingExecutor = executor;
      return this;
   }

   @NonNull
   @Override
   public ExecutionCoordinator.Builder<C> suggestionsExecutor(@NonNull final Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.suggestionsExecutor = executor;
      return this;
   }

   @NonNull
   @Override
   public ExecutionCoordinator.Builder<C> executionSchedulingExecutor(@NonNull final Executor executor) {
      Objects.requireNonNull(executor, "executor");
      this.executionSchedulingExecutor = executor;
      return this;
   }

   @NonNull
   @Override
   public ExecutionCoordinator.Builder<C> synchronizeExecution(final boolean synchronizeExecution) {
      this.synchronizeExecution = synchronizeExecution;
      return this;
   }

   @NonNull
   @Override
   public ExecutionCoordinator<C> build() {
      return new ExecutionCoordinatorImpl<>(this.parsingExecutor, this.suggestionsExecutor, this.executionSchedulingExecutor, this.synchronizeExecution);
   }
}
