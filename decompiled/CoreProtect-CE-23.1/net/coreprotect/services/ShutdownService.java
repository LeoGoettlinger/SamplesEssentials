package net.coreprotect.services;

import java.util.Iterator;
import java.util.Map.Entry;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.consumer.process.Process;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.player.PlayerQuitListener;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Teleport;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ShutdownService {
   private static final long ALERT_INTERVAL_MS = 30000L;
   private static final long MAX_SHUTDOWN_WAIT_MS = 900000L;
   private static final long DB_UNREACHABLE_TIMEOUT_MS = 300000L;

   private ShutdownService() {
      throw new IllegalStateException("Utility class");
   }

   public static void safeShutdown(Plugin plugin) {
      try {
         if (ConfigHandler.serverRunning && PaperAdapter.ADAPTER.isStopping(plugin.getServer())) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
               PlayerQuitListener.queuePlayerQuit(player);
            }
         }

         if (!ConfigHandler.isFolia) {
            revertTeleportBlocks();
         }

         ConfigHandler.serverRunning = false;
         long shutdownTime = System.currentTimeMillis();
         long nextAlertTime = shutdownTime + 30000L;
         if (ConfigHandler.converterRunning) {
            Chat.console(Phrase.build(Phrase.FINISHING_CONVERSION));
         } else {
            Chat.console(Phrase.build(Phrase.FINISHING_LOGGING));
         }

         if (ConfigHandler.migrationRunning) {
            ConfigHandler.purgeRunning = false;
         }

         waitForPendingOperations(shutdownTime, nextAlertTime);
         ConfigHandler.performDisable();
         Chat.console(Phrase.build(Phrase.DISABLE_SUCCESS, "CoreProtect v" + plugin.getDescription().getVersion()));
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   private static void waitForPendingOperations(long shutdownTime, long nextAlertTime) throws InterruptedException {
      for (; (Consumer.isRunning() || ConfigHandler.converterRunning) && !ConfigHandler.purgeRunning; Thread.sleep(100L)) {
         long currentTime = System.currentTimeMillis();
         if (currentTime >= nextAlertTime) {
            if (!ConfigHandler.converterRunning) {
               int consumerId = Consumer.currentConsumer == 1 ? 1 : 0;
               int consumerCount = Consumer.getConsumerSize(consumerId) + Process.getCurrentConsumerSize();
               Chat.console(Phrase.build(Phrase.LOGGING_ITEMS, String.format("%,d", consumerCount)));
            }

            nextAlertTime = currentTime + 30000L;
         } else {
            if (!ConfigHandler.databaseReachable && currentTime - shutdownTime >= 300000L) {
               Chat.console(Phrase.build(Phrase.DATABASE_UNREACHABLE));
               break;
            }

            if (currentTime - shutdownTime >= 900000L) {
               Chat.console(Phrase.build(Phrase.LOGGING_TIME_LIMIT));
               break;
            }
         }
      }
   }

   private static void revertTeleportBlocks() {
      Iterator<Entry<Location, BlockData>> iterator = Teleport.revertBlocks.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Location, BlockData> entry = iterator.next();
         entry.getKey().getBlock().setBlockData(entry.getValue());
         iterator.remove();
      }
   }
}
