package com.magmaguy.elitemobs.magmacore.initialization;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class PluginInitializationLifecycle {
   private long nextGeneration;
   private PluginInitializationLifecycle.Attempt current;
   private CompletableFuture<Void> tail = CompletableFuture.completedFuture(null);
   private boolean shutdownRequested;

   PluginInitializationLifecycle.Attempt beginAttempt() {
      PluginInitializationLifecycle.AttemptStart start = this.prepareAttempt();
      if (start == null) {
         throw new IllegalStateException("Cannot start an initialization attempt after shutdown was requested.");
      } else {
         start.cancelPrevious();
         return start.attempt();
      }
   }

   synchronized PluginInitializationLifecycle.AttemptStart prepareAttempt() {
      if (this.shutdownRequested) {
         return null;
      } else {
         PluginInitializationLifecycle.Attempt previous = this.current;
         PluginInitializationLifecycle.Attempt attempt = new PluginInitializationLifecycle.Attempt(++this.nextGeneration, this.tail);
         this.current = attempt;
         this.tail = attempt.serializedCompletion();
         return new PluginInitializationLifecycle.AttemptStart(attempt, previous);
      }
   }

   PluginInitializationLifecycle.Attempt cancelCurrent() {
      PluginInitializationLifecycle.Attempt attempt;
      boolean publishCompletion;
      synchronized (this) {
         attempt = this.current;
         publishCompletion = attempt != null && attempt.markCancelled();
      }

      if (attempt != null) {
         attempt.publishCompletion(publishCompletion);
      }

      return attempt;
   }

   PluginInitializationLifecycle.Attempt requestShutdown() {
      PluginInitializationLifecycle.Attempt attempt;
      boolean publishCompletion;
      synchronized (this) {
         this.shutdownRequested = true;
         attempt = this.current;
         publishCompletion = attempt != null && attempt.markCancelled();
      }

      if (attempt != null) {
         attempt.publishCompletion(publishCompletion);
      }

      return attempt;
   }

   PluginInitializationLifecycle.Attempt shutdownCurrent() {
      PluginInitializationLifecycle.Attempt attempt;
      boolean publishCompletion;
      synchronized (this) {
         this.shutdownRequested = true;
         attempt = this.current;
         publishCompletion = attempt != null && attempt.markCancelled();
      }

      if (attempt == null) {
         return null;
      } else {
         attempt.publishCompletion(publishCompletion);
         return attempt;
      }
   }

   synchronized boolean isCurrent(PluginInitializationLifecycle.Attempt attempt) {
      return this.current == attempt && !attempt.isCancelled();
   }

   synchronized boolean isShutdownRequested() {
      return this.shutdownRequested || this.current != null && this.current.isCancelled();
   }

   synchronized void resume() {
      this.shutdownRequested = false;
   }

   static final class Attempt {
      private final long generation;
      private final String ownerToken = UUID.randomUUID().toString();
      private final CompletableFuture<Void> predecessor;
      private final CompletableFuture<Void> completion = new CompletableFuture<>();
      private final CompletableFuture<Void> serializedCompletion;
      private PluginInitializationLifecycle.Attempt.Phase phase = PluginInitializationLifecycle.Attempt.Phase.WAITING;
      private boolean cancelled;

      private Attempt(long generation, CompletableFuture<Void> predecessor) {
         this.generation = generation;
         this.predecessor = predecessor;
         this.serializedCompletion = predecessor.thenCompose(ignored -> this.completion);
      }

      long generation() {
         return this.generation;
      }

      String ownerToken() {
         return this.ownerToken;
      }

      CompletableFuture<Void> predecessor() {
         return this.predecessor;
      }

      CompletableFuture<Void> serializedCompletion() {
         return this.serializedCompletion;
      }

      boolean claimAsyncDispatch() {
         synchronized (this) {
            if (!this.cancelled && this.phase == PluginInitializationLifecycle.Attempt.Phase.WAITING) {
               this.phase = PluginInitializationLifecycle.Attempt.Phase.ASYNC_SCHEDULED;
               return true;
            } else {
               return false;
            }
         }
      }

      boolean startAsync() {
         boolean publishCompletion = false;
         synchronized (this) {
            if (!this.cancelled && this.phase == PluginInitializationLifecycle.Attempt.Phase.ASYNC_SCHEDULED) {
               this.phase = PluginInitializationLifecycle.Attempt.Phase.ASYNC_RUNNING;
               return true;
            }

            publishCompletion = this.markTerminal();
         }

         this.publishCompletion(publishCompletion);
         return false;
      }

      boolean scheduleSync(Runnable scheduler) {
         return this.scheduleSyncFrom(PluginInitializationLifecycle.Attempt.Phase.ASYNC_RUNNING, scheduler);
      }

      boolean scheduleFailureSync(Runnable scheduler) {
         PluginInitializationLifecycle.Attempt.Phase previousPhase = null;
         boolean publishCompletion = false;
         boolean rejected = false;
         synchronized (this) {
            if (!this.cancelled
               && this.phase != PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED
               && this.phase != PluginInitializationLifecycle.Attempt.Phase.SYNC_RUNNING
               && this.phase != PluginInitializationLifecycle.Attempt.Phase.TERMINAL) {
               previousPhase = this.phase;
               this.phase = PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED;
            } else {
               publishCompletion = this.markTerminal();
               rejected = true;
            }
         }

         this.publishCompletion(publishCompletion);
         if (rejected) {
            return false;
         } else {
            try {
               scheduler.run();
               return true;
            } catch (Throwable var9) {
               synchronized (this) {
                  if (this.phase == PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED) {
                     this.phase = previousPhase;
                  }
               }

               throw var9;
            }
         }
      }

      private boolean scheduleSyncFrom(PluginInitializationLifecycle.Attempt.Phase requiredPhase, Runnable scheduler) {
         boolean publishCompletion = false;
         boolean rejected = false;
         synchronized (this) {
            if (!this.cancelled && this.phase == requiredPhase) {
               this.phase = PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED;
            } else {
               publishCompletion = this.markTerminal();
               rejected = true;
            }
         }

         this.publishCompletion(publishCompletion);
         if (rejected) {
            return false;
         } else {
            try {
               scheduler.run();
               return true;
            } catch (Throwable var9) {
               synchronized (this) {
                  if (this.phase == PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED) {
                     this.phase = requiredPhase;
                  }
               }

               throw var9;
            }
         }
      }

      boolean startSync() {
         boolean publishCompletion = false;
         synchronized (this) {
            if (!this.cancelled && this.phase == PluginInitializationLifecycle.Attempt.Phase.SYNC_SCHEDULED) {
               this.phase = PluginInitializationLifecycle.Attempt.Phase.SYNC_RUNNING;
               return true;
            }

            publishCompletion = this.markTerminal();
         }

         this.publishCompletion(publishCompletion);
         return false;
      }

      void completeAsyncWithoutSync() {
         boolean publishCompletion = false;
         synchronized (this) {
            if (this.phase == PluginInitializationLifecycle.Attempt.Phase.ASYNC_RUNNING) {
               publishCompletion = this.markTerminal();
            }
         }

         this.publishCompletion(publishCompletion);
      }

      void completeSync() {
         boolean publishCompletion = false;
         synchronized (this) {
            if (this.phase == PluginInitializationLifecycle.Attempt.Phase.SYNC_RUNNING) {
               publishCompletion = this.markTerminal();
            }
         }

         this.publishCompletion(publishCompletion);
      }

      void completeTerminal() {
         boolean publishCompletion;
         synchronized (this) {
            publishCompletion = this.markTerminal();
         }

         this.publishCompletion(publishCompletion);
      }

      void cancel() {
         boolean publishCompletion = this.markCancelled();
         this.publishCompletion(publishCompletion);
      }

      synchronized boolean isCancelled() {
         return this.cancelled;
      }

      private boolean markTerminal() {
         if (this.phase == PluginInitializationLifecycle.Attempt.Phase.TERMINAL) {
            return false;
         } else {
            this.phase = PluginInitializationLifecycle.Attempt.Phase.TERMINAL;
            return true;
         }
      }

      private synchronized boolean markCancelled() {
         this.cancelled = true;
         return this.phase != PluginInitializationLifecycle.Attempt.Phase.ASYNC_RUNNING
            && this.phase != PluginInitializationLifecycle.Attempt.Phase.SYNC_RUNNING
            && this.markTerminal();
      }

      private void publishCompletion(boolean shouldPublish) {
         if (shouldPublish) {
            this.completion.complete(null);
         }
      }

      private static enum Phase {
         WAITING,
         ASYNC_SCHEDULED,
         ASYNC_RUNNING,
         SYNC_SCHEDULED,
         SYNC_RUNNING,
         TERMINAL;
      }
   }

   record AttemptStart(PluginInitializationLifecycle.Attempt attempt, PluginInitializationLifecycle.Attempt previous) {
      void cancelPrevious() {
         if (this.previous != null) {
            this.previous.cancel();
         }
      }
   }
}
