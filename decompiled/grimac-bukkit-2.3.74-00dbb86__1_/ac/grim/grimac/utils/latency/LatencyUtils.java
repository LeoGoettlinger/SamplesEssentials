package ac.grim.grimac.utils.latency;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.common.arguments.CommonGrimArguments;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

public class LatencyUtils implements ILatencyUtils {
   private final ArrayDeque<LatencyUtils.TransactionTask> transactionMap = new ArrayDeque<>();
   private final GrimPlayer player;
   private final ArrayList<Runnable> tasksToRun = new ArrayList<>();

   public LatencyUtils(GrimPlayer player) {
      this.player = player;
   }

   @Override
   public void addRealTimeTask(int transaction, Runnable runnable) {
      this.addRealTimeTaskInternal(transaction, false, runnable);
   }

   @Override
   public void addRealTimeTaskAsync(int transaction, Runnable runnable) {
      this.addRealTimeTaskInternal(transaction, true, runnable);
   }

   private void addRealTimeTaskInternal(int transactionId, boolean async, Runnable runnable) {
      if (this.player.lastTransactionReceived.get() >= transactionId) {
         if (async) {
            this.player.runSafely(runnable);
         } else {
            runnable.run();
         }
      } else {
         synchronized (this.transactionMap) {
            this.transactionMap.add(new LatencyUtils.TransactionTask(transactionId, runnable));
         }
      }
   }

   @Override
   public void handleNettySyncTransaction(int receivedTransactionId) {
      synchronized (this.transactionMap) {
         this.tasksToRun.clear();
         Iterator<LatencyUtils.TransactionTask> iterator = this.transactionMap.iterator();

         while (iterator.hasNext()) {
            LatencyUtils.TransactionTask taskEntry = iterator.next();
            int taskTransactionId = taskEntry.transactionId();
            if (receivedTransactionId + 1 < taskTransactionId) {
               break;
            }

            if (receivedTransactionId != taskTransactionId - 1) {
               this.tasksToRun.add(taskEntry.task());
               iterator.remove();
            }
         }

         for (Runnable runnable : this.tasksToRun) {
            try {
               runnable.run();
            } catch (Exception var8) {
               this.handleRunnableError(var8);
            }
         }
      }
   }

   private void handleRunnableError(Exception e) {
      LogUtil.error("An error has occurred when running transactions for player: " + this.player.user.getName(), e);
      if (CommonGrimArguments.KICK_ON_TRANSACTION_ERRORS.value()) {
         this.player
            .disconnect(MessageUtil.miniMessage(MessageUtil.replacePlaceholders(this.player, GrimAPI.INSTANCE.getConfigManager().getDisconnectPacketError())));
      }
   }

   private record TransactionTask(int transactionId, Runnable task) {
   }
}
