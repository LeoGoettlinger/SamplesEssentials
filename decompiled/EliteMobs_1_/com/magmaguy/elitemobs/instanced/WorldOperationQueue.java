package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldOperationQueue {
   private static final Queue<WorldOperationQueue.WorldOperation> operationQueue = new ConcurrentLinkedQueue<>();
   private static final AtomicBoolean isProcessing = new AtomicBoolean(false);
   private static final AtomicInteger operationGeneration = new AtomicInteger(0);
   private static volatile WorldOperationQueue.WorldOperation activeOperation;

   public static void queueOperation(Player player, Supplier<Boolean> asyncOperation, Runnable syncOperation, String operationName) {
      queueOperation(player, asyncOperation, syncOperation, operationName, () -> {});
   }

   public static void queueOperation(Player player, Supplier<Boolean> asyncOperation, Runnable syncOperation, String operationName, Runnable terminalCallback) {
      if (!Bukkit.isPrimaryThread()) {
         Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> queueOperation(player, asyncOperation, syncOperation, operationName, terminalCallback));
      } else {
         WorldOperationQueue.WorldOperation operation = new WorldOperationQueue.WorldOperation(
            player, asyncOperation, syncOperation, operationName, terminalCallback, operationGeneration.get(), new AtomicBoolean(false)
         );
         if (!MetadataHandler.shutdownRequested && player.isOnline()) {
            operationQueue.add(operation);
            int queuePosition = operationQueue.size() + (isProcessing.get() ? 1 : 0);
            if (queuePosition > 1) {
               player.sendMessage(DungeonsConfig.getDungeonPreparingQueueMessage().replace("$position", String.valueOf(queuePosition)));
            } else {
               player.sendMessage(DungeonsConfig.getDungeonPreparingMessage());
            }

            processNextOperation();
         } else {
            runTerminalCallback(operation);
         }
      }
   }

   private static void processNextOperation() {
      if (isProcessing.compareAndSet(false, true)) {
         final WorldOperationQueue.WorldOperation next;
         for (next = operationQueue.poll(); next != null && (!next.player.isOnline() || MetadataHandler.shutdownRequested); next = operationQueue.poll()) {
            runTerminalCallback(next);
         }

         if (next == null) {
            isProcessing.set(false);
         } else {
            activeOperation = next;
            notifyQueuePositions();
            (new BukkitRunnable() {
               public void run() {
                  boolean success;
                  try {
                     success = next.asyncOperation.get();
                  } catch (Exception var3) {
                     Logger.warn("World operation '" + next.operationName + "' failed during async phase: " + var3.getMessage());
                     var3.printStackTrace();
                     success = false;
                  }

                  if (next.generation == WorldOperationQueue.operationGeneration.get() && !MetadataHandler.shutdownRequested) {
                     final boolean asyncSuccess = success;
                     (new BukkitRunnable() {
                        public void run() {
                           WorldOperationQueue.completeAsyncPhase(next, asyncSuccess);
                        }
                     }).runTask(MetadataHandler.PLUGIN);
                  }
               }
            }).runTaskAsynchronously(MetadataHandler.PLUGIN);
         }
      }
   }

   private static void completeAsyncPhase(final WorldOperationQueue.WorldOperation operation, boolean success) {
      if (operation.generation == operationGeneration.get() && !MetadataHandler.shutdownRequested) {
         if (!success) {
            if (operation.player.isOnline()) {
               operation.player.sendMessage(DungeonsConfig.getDungeonCopyFailedMessage());
               operation.player.sendMessage(DungeonsConfig.getDungeonPrepareFailedMessage());
            }

            runTerminalCallback(operation);
            isProcessing.set(false);
            processNextOperation();
         } else {
            try {
               operation.syncOperation.run();
            } catch (Exception var6) {
               Logger.warn("World operation '" + operation.operationName + "' failed during sync phase: " + var6.getMessage());
               var6.printStackTrace();
               if (operation.player.isOnline()) {
                  operation.player.sendMessage(DungeonsConfig.getDungeonLoadFailedMessage());
               }
            } finally {
               runTerminalCallback(operation);
               isProcessing.set(false);
               if (operation.generation != operationGeneration.get() || MetadataHandler.shutdownRequested) {
                  return;
               }

               (new BukkitRunnable() {
                  public void run() {
                     if (operation.generation == WorldOperationQueue.operationGeneration.get() && !MetadataHandler.shutdownRequested) {
                        WorldOperationQueue.processNextOperation();
                     }
                  }
               }).runTaskLater(MetadataHandler.PLUGIN, 10L);
            }
         }
      }
   }

   private static void notifyQueuePositions() {
      int position = 1;

      for (WorldOperationQueue.WorldOperation op : operationQueue) {
         if (op.player.isOnline()) {
            op.player.sendMessage(DungeonsConfig.getDungeonPreparingQueueMessage().replace("$position", String.valueOf(position)));
         }

         position++;
      }
   }

   public static int getQueueSize() {
      return operationQueue.size() + (isProcessing.get() ? 1 : 0);
   }

   public static void shutdown() {
      operationGeneration.incrementAndGet();
      WorldOperationQueue.WorldOperation currentOperation = activeOperation;
      if (currentOperation != null) {
         runTerminalCallback(currentOperation);
      }

      for (WorldOperationQueue.WorldOperation op : operationQueue) {
         if (op.player.isOnline()) {
            op.player.sendMessage(DungeonsConfig.getDungeonCancelledShutdownMessage());
         }

         runTerminalCallback(op);
      }

      operationQueue.clear();
      activeOperation = null;
      isProcessing.set(false);
   }

   private static void runTerminalCallback(WorldOperationQueue.WorldOperation operation) {
      if (operation.terminalized.compareAndSet(false, true)) {
         try {
            operation.terminalCallback.run();
         } catch (RuntimeException var5) {
            Logger.warn("World operation '" + operation.operationName + "' terminal callback failed: " + var5.getMessage());
         } finally {
            if (activeOperation == operation) {
               activeOperation = null;
            }
         }
      }
   }

   private record WorldOperation(
      Player player,
      Supplier<Boolean> asyncOperation,
      Runnable syncOperation,
      String operationName,
      Runnable terminalCallback,
      int generation,
      AtomicBoolean terminalized
   ) {
   }
}
