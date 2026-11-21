package com.fastasyncworldedit.core;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.fastasyncworldedit.core.util.CachedTextureUtil;
import com.fastasyncworldedit.core.util.CleanTextureUtil;
import com.fastasyncworldedit.core.util.FaweTimer;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.RandomTextureUtil;
import com.fastasyncworldedit.core.util.TaskManager;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.fastasyncworldedit.core.util.WEManager;
import com.fastasyncworldedit.core.util.task.KeyQueuedExecutorService;
import com.fastasyncworldedit.core.util.task.UUIDKeyQueuedThreadFactory;
import com.github.luben.zstd.Zstd;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.NotificationEmitter;
import net.jpountz.lz4.LZ4Factory;
import org.apache.logging.log4j.Logger;

public class Fawe {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static Fawe instance;
   private final FaweTimer timer;
   private final IFawe implementation;
   private final KeyQueuedExecutorService<UUID> uuidKeyQueuedExecutorService;
   private FaweVersion version;
   private TextureUtil textures;
   private QueueHandler queueHandler;
   private Thread thread;

   private Fawe(final IFawe implementation) {
      instance = this;
      this.implementation = implementation;
      this.thread = Thread.currentThread();
      this.setupConfigs();
      FaweLimit.MAX.CONFIRM_LARGE = Settings.settings().LIMITS.get("default").CONFIRM_LARGE || Settings.settings().GENERAL.LIMIT_UNLIMITED_CONFIRMS;
      TaskManager.IMP = this.implementation.getTaskManager();
      TaskManager.taskManager()
         .async(
            () -> {
               MainUtil.deleteOlder(
                  MainUtil.getFile(this.implementation.getDirectory(), Settings.settings().PATHS.HISTORY),
                  TimeUnit.DAYS.toMillis(Settings.settings().HISTORY.DELETE_AFTER_DAYS),
                  false
               );
               MainUtil.deleteOlder(
                  MainUtil.getFile(this.implementation.getDirectory(), Settings.settings().PATHS.CLIPBOARD),
                  TimeUnit.DAYS.toMillis(Settings.settings().CLIPBOARD.DELETE_AFTER_DAYS),
                  false
               );
            }
         );
      this.setupMemoryListener();
      this.timer = new FaweTimer();
      TaskManager.taskManager().later(() -> {
         try {
            WEManager.weManager().addManagers(this.implementation.getMaskManagers());
         } catch (Throwable var2) {
         }
      }, 0);
      TaskManager.taskManager().repeatAsync(MemUtil::checkAndSetApproachingLimit, 1);
      TaskManager.taskManager().repeat(this.timer, 1);
      this.uuidKeyQueuedExecutorService = new KeyQueuedExecutorService<>(
         new ThreadPoolExecutor(
            1, Settings.settings().QUEUE.PARALLEL_THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new UUIDKeyQueuedThreadFactory()
         )
      );
   }

   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static <T extends IFawe> T imp() {
      return (T)(instance != null ? instance.implementation : null);
   }

   public static <T extends IFawe> T platform() {
      return (T)(instance != null ? instance.implementation : null);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.0.0"
   )
   public static Fawe get() {
      return instance;
   }

   public static Fawe instance() {
      return instance;
   }

   public static void set(final IFawe implementation) throws InstanceAlreadyExistsException, IllegalArgumentException {
      if (instance != null) {
         throw new InstanceAlreadyExistsException("FAWE has already been initialized with: " + instance.implementation);
      } else if (implementation == null) {
         throw new IllegalArgumentException("Implementation may not be null.");
      } else {
         instance = new Fawe(implementation);
      }
   }

   public static void setupInjector() {
      boolean x86OS = System.getProperty("sun.arch.data.model").contains("32");
      boolean x86JVM = System.getProperty("os.arch").contains("32");
      if (x86OS != x86JVM) {
         LOGGER.info("You are running 32-bit Java on a 64-bit machine. Please upgrade to 64-bit Java.");
      }
   }

   public static boolean isMainThread() {
      return instance == null || instance.thread == Thread.currentThread();
   }

   public static void handleFaweException(boolean[] faweExceptionReasonsUsed, FaweException e, final Logger logger) {
      FaweException.Type type = e.getType();
      switch (type) {
         case OTHER:
            logger.catching(e);
            throw e;
         case PLAYER_ONLY:
         case ACTOR_REQUIRED:
         case LOW_MEMORY:
            if (!faweExceptionReasonsUsed[type.ordinal()]) {
               logger.warn("FaweException: " + e.getMessage());
               faweExceptionReasonsUsed[type.ordinal()] = true;
               throw e;
            }
         case MAX_TILES:
         case NO_REGION:
         case MAX_CHECKS:
         case MAX_CHANGES:
         case MAX_ENTITIES:
         case MAX_ITERATIONS:
         case OUTSIDE_REGION:
         case CLIPBOARD:
            if (!faweExceptionReasonsUsed[type.ordinal()]) {
               faweExceptionReasonsUsed[type.ordinal()] = true;
               throw e;
            }

            return;
         default:
            if (!faweExceptionReasonsUsed[type.ordinal()]) {
               faweExceptionReasonsUsed[type.ordinal()] = true;
               logger.warn("FaweException: " + e.getMessage());
            }
      }
   }

   public void onDisable() {
      if (platform().getPreloader(false) != null) {
         platform().getPreloader(false).cancel();
      }
   }

   public QueueHandler getQueueHandler() {
      if (this.queueHandler == null) {
         synchronized (this) {
            if (this.queueHandler == null) {
               this.queueHandler = this.implementation.getQueueHandler();
            }
         }
      }

      return this.queueHandler;
   }

   public TextureUtil getCachedTextureUtil(boolean randomize, int min, int max) {
      TextureUtil tu = this.getTextureUtil();

      try {
         tu = (TextureUtil)(min == 0 && max == 100 ? tu : new CleanTextureUtil(tu, min, max));
         tu = (TextureUtil)(randomize ? new RandomTextureUtil(tu) : new CachedTextureUtil(tu));
      } catch (FileNotFoundException var6) {
         var6.printStackTrace();
      }

      return tu;
   }

   public TextureUtil getTextureUtil() {
      TextureUtil tmp = this.textures;
      if (tmp == null) {
         synchronized (this) {
            tmp = this.textures;
            if (tmp == null) {
               try {
                  this.textures = tmp = new TextureUtil();
                  tmp.loadModTextures();
               } catch (IOException var5) {
                  throw new RuntimeException(var5);
               }
            }
         }
      }

      return tmp;
   }

   public FaweTimer getTimer() {
      return this.timer;
   }

   @Nullable
   public FaweVersion getVersion() {
      return this.version;
   }

   public double getTPS() {
      return this.timer.getTPS();
   }

   public void setupConfigs() {
      MainUtil.copyFile(MainUtil.getJarFile(), "lang/strings.json", null);
      File file = new File(this.implementation.getDirectory(), "config.yml");
      Settings.settings().PLATFORM = this.implementation.getPlatform().replace("\"", "");

      try (
         InputStream stream = this.getClass().getResourceAsStream("/fawe.properties");
         BufferedReader br = new BufferedReader(new InputStreamReader(stream));
      ) {
         String versionString = br.readLine();
         String commitString = br.readLine();
         String dateString = br.readLine();
         br.close();
         this.version = FaweVersion.tryParse(versionString, commitString, dateString);
         Settings.settings().DATE = new Date(100 + this.version.year, this.version.month, this.version.day).toString();
         Settings.settings().BUILD = "https://ci.athion.net/job/FastAsyncWorldEdit/" + this.version.build;
         Settings.settings().COMMIT = "https://github.com/IntellectualSites/FastAsyncWorldEdit/commit/" + Integer.toHexString(this.version.hash);
      } catch (Throwable var14) {
      }

      try {
         Settings.settings().reload(file);
      } catch (Throwable var8) {
         LOGGER.error("Failed to load config.", var8);
      }

      Settings.settings().QUEUE.TARGET_SIZE = Math.max(Settings.settings().QUEUE.TARGET_SIZE, Settings.settings().QUEUE.PARALLEL_THREADS);
      if (Settings.settings().QUEUE.TARGET_SIZE < 4 * Settings.settings().QUEUE.PARALLEL_THREADS) {
         LOGGER.error(
            "queue.target-size is {}, and queue.parallel_threads is {}. It is HIGHLY recommended that queue.target-size be at least four times queue.parallel-threads or greater.",
            Settings.settings().QUEUE.TARGET_SIZE,
            Settings.settings().QUEUE.PARALLEL_THREADS
         );
      }

      if (Settings.settings().HISTORY.DELETE_DISK_ON_LOGOUT && Settings.settings().HISTORY.USE_DATABASE) {
         LOGGER.warn(
            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n!!!                                                                !!!\n!!!    Using history database whilst deleting disk history!        !!!\n!!!    You will not be able to rollback edits after a user logs    !!!\n!!!    out, recommended to disable delete-disk-on-logout if you    !!!\n!!!    want to have full history rollback functionality.           !!!\n!!!    Disable use-database if you do not need to have rollback    !!!\n!!!    functionality and wish to disable this warning.             !!!\n!!!                                                                !!!\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
         );
      }

      try {
         byte[] in = new byte[0];
         byte[] compressed = LZ4Factory.fastestJavaInstance().fastCompressor().compress(in);
         byte[] ob = new byte[100];

         assert LZ4Factory.fastestJavaInstance().fastDecompressor().decompress(ob, compressed) == 0;

         LOGGER.info("LZ4 Compression Binding loaded successfully");
      } catch (Throwable var7) {
         LOGGER.error("LZ4 Compression Binding Not Found.\nFAWE will still work but compression will be slower.", var7);
      }

      try {
         byte[] in = new byte[0];
         byte[] compressed = Zstd.compress(in);
         byte[] ob = new byte[100];

         assert Zstd.decompress(ob, compressed) == 0L;

         LOGGER.info("ZSTD Compression Binding loaded successfully");
      } catch (Throwable var12) {
         if (Settings.settings().CLIPBOARD.COMPRESSION_LEVEL > 6 || Settings.settings().HISTORY.COMPRESSION_LEVEL > 6) {
            Settings.settings().CLIPBOARD.COMPRESSION_LEVEL = Math.min(6, Settings.settings().CLIPBOARD.COMPRESSION_LEVEL);
            Settings.settings().HISTORY.COMPRESSION_LEVEL = Math.min(6, Settings.settings().HISTORY.COMPRESSION_LEVEL);
            LOGGER.error("ZSTD Compression Binding Not Found.\nFAWE will still work but compression won't work as well.", var12);
         }
      }

      Settings.settings().save(file);
   }

   public WorldEdit getWorldEdit() {
      return WorldEdit.getInstance();
   }

   private void setupMemoryListener() {
      if (Settings.settings().MAX_MEMORY_PERCENT >= 1 && Settings.settings().MAX_MEMORY_PERCENT <= 99) {
         try {
            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            NotificationEmitter ne = (NotificationEmitter)memBean;
            AtomicLong lastWarn = new AtomicLong(System.currentTimeMillis());
            ne.addNotificationListener((notification, handback) -> {
               long heapSize = Runtime.getRuntime().totalMemory();
               long heapMaxSize = Runtime.getRuntime().maxMemory();
               if (heapSize >= heapMaxSize) {
                  long time = System.currentTimeMillis();
                  if (time > lastWarn.get() + TimeUnit.SECONDS.toMillis(30L)) {
                     lastWarn.set(time);
                     LOGGER.warn("High memory usage detected, FAWE will attempt to slow operations to prevent a crash.");
                  }

                  MemUtil.memoryLimitedTask();
               }
            }, null, null);

            for (MemoryPoolMXBean mp : ManagementFactory.getMemoryPoolMXBeans()) {
               if (mp.isCollectionUsageThresholdSupported()) {
                  MemoryUsage mu = mp.getUsage();
                  long max = mu.getMax();
                  if (max >= 0L) {
                     long alert = max * Settings.settings().MAX_MEMORY_PERCENT / 100L;
                     mp.setCollectionUsageThreshold(alert);
                  }
               }
            }
         } catch (Throwable var12) {
            LOGGER.error(
               "FAWE encountered an error trying to listen to JVM memory.\nPlease change your Java security settings or disable this message bychanging 'max-memory-percent' in the config files to '-1'."
            );
         }
      }
   }

   public Thread getMainThread() {
      return this.thread;
   }

   public Thread setMainThread() {
      return this.thread = Thread.currentThread();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.12.1"
   )
   public KeyQueuedExecutorService<UUID> getClipboardExecutor() {
      return this.uuidKeyQueuedExecutorService;
   }

   public Future<?> submitUUIDKeyQueuedTask(UUID uuid, Runnable runnable) {
      if (Thread.currentThread() instanceof UUIDKeyQueuedThreadFactory.UUIDKeyQueuedThread) {
         runnable.run();
         return CompletableFuture.completedFuture(null);
      } else {
         return this.uuidKeyQueuedExecutorService.submit(uuid, runnable);
      }
   }

   public <T> Future<T> submitUUIDKeyQueuedTask(UUID uuid, Runnable runnable, T result) {
      if (Thread.currentThread() instanceof UUIDKeyQueuedThreadFactory.UUIDKeyQueuedThread) {
         runnable.run();
         return CompletableFuture.completedFuture(result);
      } else {
         return this.uuidKeyQueuedExecutorService.submit(uuid, runnable, result);
      }
   }

   public <T> Future<T> submitUUIDKeyQueuedTask(UUID uuid, Callable<T> callable) {
      if (Thread.currentThread() instanceof UUIDKeyQueuedThreadFactory.UUIDKeyQueuedThread) {
         try {
            return CompletableFuture.completedFuture(callable.call());
         } catch (Throwable var4) {
            return CompletableFuture.failedFuture(var4);
         }
      } else {
         return this.uuidKeyQueuedExecutorService.submit(uuid, callable);
      }
   }
}
