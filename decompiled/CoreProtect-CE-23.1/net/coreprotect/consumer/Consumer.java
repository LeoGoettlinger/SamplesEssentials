package net.coreprotect.consumer;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.CoreProtect;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.process.Process;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

public class Consumer extends Process implements Runnable, UncaughtExceptionHandler {
   private static Thread consumerThread = null;
   public static volatile int currentConsumer = 0;
   public static volatile boolean isPaused = false;
   public static volatile boolean transacting = false;
   public static volatile boolean interrupt = false;
   protected static volatile boolean pausedSuccess = false;
   public static ConcurrentHashMap<Integer, ArrayList<Object[]>> consumer = new ConcurrentHashMap<>(4, 0.75F, 2);
   public static Map<Integer, Integer[]> consumer_id = Collections.synchronizedMap(new HashMap<>());
   public static ConcurrentHashMap<Integer, Map<Integer, String[]>> consumerUsers = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, String>> consumerStrings = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, Object[]>> consumerSigns = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, ItemStack[]>> consumerContainers = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, Object>> consumerInventories = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, List<BlockState>>> consumerBlockList = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, List<Object[]>>> consumerObjectArrayList = new ConcurrentHashMap<>(4, 0.75F, 2);
   @Deprecated
   public static ConcurrentHashMap<Integer, Map<Integer, List<Object>>> consumerObjectList = new ConcurrentHashMap<>(4, 0.75F, 2);
   public static ConcurrentHashMap<Integer, Map<Integer, Object>> consumerObjects = new ConcurrentHashMap<>(4, 0.75F, 2);

   private static void errorDelay() {
      try {
         Thread.sleep(30000L);
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }

   protected static int newConsumerId(int consumer) {
      int id = consumer_id.get(consumer)[0];
      consumer_id.put(consumer, new Integer[]{id + 1, 1});
      return id;
   }

   public static int getConsumerSize(int id) {
      return id != 0 && id != 1 ? 0 : consumer.get(id).size();
   }

   public static void initialize() {
      consumer.put(0, new ArrayList<>());
      consumer.put(1, new ArrayList<>());
      consumer_id.put(0, new Integer[]{0, 0});
      consumer_id.put(1, new Integer[]{0, 0});
      consumerUsers.put(0, new HashMap<>());
      consumerUsers.put(1, new HashMap<>());
      consumerObjects.put(0, new HashMap<>());
      consumerObjects.put(1, new HashMap<>());
      consumerStrings.put(0, new HashMap<>());
      consumerStrings.put(1, new HashMap<>());
      consumerSigns.put(0, new HashMap<>());
      consumerSigns.put(1, new HashMap<>());
      consumerInventories.put(0, new HashMap<>());
      consumerInventories.put(1, new HashMap<>());
      consumerBlockList.put(0, new HashMap<>());
      consumerBlockList.put(1, new HashMap<>());
      consumerObjectArrayList.put(0, new HashMap<>());
      consumerObjectArrayList.put(1, new HashMap<>());
      consumerObjectList.put(0, new HashMap<>());
      consumerObjectList.put(1, new HashMap<>());
      consumerContainers.put(0, new HashMap<>());
      consumerContainers.put(1, new HashMap<>());
   }

   public static boolean isRunning() {
      return consumerThread != null && consumerThread.isAlive();
   }

   private static void pauseConsumer(int process_id) {
      try {
         while (
            (ConfigHandler.serverRunning || ConfigHandler.converterRunning || ConfigHandler.migrationRunning)
               && (isPaused || ConfigHandler.pauseConsumer || ConfigHandler.purgeRunning || consumer_id.get(process_id)[1] == 1)
         ) {
            pausedSuccess = true;
            Thread.sleep(100L);
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      pausedSuccess = false;
   }

   @Override
   public void run() {
      boolean lastRun = false;

      while (ConfigHandler.serverRunning || ConfigHandler.converterRunning || !lastRun) {
         if (!ConfigHandler.serverRunning && !ConfigHandler.converterRunning) {
            lastRun = true;
         }

         try {
            int process_id = 0;
            if (currentConsumer == 0) {
               currentConsumer = 1;
            } else {
               process_id = 1;
               currentConsumer = 0;
            }

            Thread.sleep(500L);
            pauseConsumer(process_id);
            Process.processConsumer(process_id, lastRun);
         } catch (Exception var3) {
            var3.printStackTrace();
            errorDelay();
         }
      }
   }

   @Override
   public void uncaughtException(Thread thread, Throwable e) {
      e.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(CoreProtect.getInstance());
   }

   public static void startConsumer() {
      if (!isRunning()) {
         consumerThread = new Thread(new Consumer());
         consumerThread.setUncaughtExceptionHandler(new Consumer());
         consumerThread.start();
      }
   }
}
