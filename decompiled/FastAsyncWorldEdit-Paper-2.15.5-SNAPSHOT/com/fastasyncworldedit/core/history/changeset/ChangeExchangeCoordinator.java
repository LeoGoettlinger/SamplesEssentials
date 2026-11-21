package com.fastasyncworldedit.core.history.changeset;

import com.sk89q.worldedit.history.change.Change;
import java.lang.Thread.Builder.OfVirtual;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ChangeExchangeCoordinator implements AutoCloseable {
   private static final OfVirtual UNDO_VIRTUAL_THREAD_BUILDER = Thread.ofVirtual().name("FAWE undo", 0L);
   private final Exchanger<Change[]> exchanger;
   private final BiConsumer<Exchanger<Change[]>, Change[]> runnerTask;
   private boolean started = false;
   private Thread runner;

   public ChangeExchangeCoordinator(BiConsumer<Exchanger<Change[]>, Change[]> runner) {
      this.runnerTask = runner;
      this.exchanger = new Exchanger<>();
   }

   public Change[] take(Change[] consumed) {
      if (!this.started) {
         this.started = true;
         int length = consumed.length;
         this.runner = UNDO_VIRTUAL_THREAD_BUILDER.start(() -> this.runnerTask.accept(this.exchanger, new Change[length]));
      }

      try {
         return this.exchanger.exchange(consumed, 30L, TimeUnit.SECONDS);
      } catch (TimeoutException | InterruptedException var3) {
         this.runner.interrupt();
         Thread.currentThread().interrupt();
         return null;
      }
   }

   @Override
   public void close() {
      if (this.runner != null) {
         this.runner.interrupt();
      }
   }
}
