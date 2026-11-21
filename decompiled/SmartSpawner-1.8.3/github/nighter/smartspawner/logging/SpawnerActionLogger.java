package github.nighter.smartspawner.logging;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.logging.discord.DiscordEmbedConfigManager;
import github.nighter.smartspawner.logging.discord.DiscordWebhookConfig;
import github.nighter.smartspawner.logging.discord.DiscordWebhookLogger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class SpawnerActionLogger {
   private final SmartSpawner plugin;
   private final LoggingConfig config;
   private final Queue<SpawnerLogEntry> logQueue;
   private final AtomicBoolean isShuttingDown;
   private Scheduler.Task logTask;
   private DiscordWebhookLogger discordLogger;
   private File currentLogFile;
   private static final ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

   public SpawnerActionLogger(SmartSpawner plugin, LoggingConfig config) {
      this.plugin = plugin;
      this.config = config;
      this.logQueue = new ConcurrentLinkedQueue<>();
      this.isShuttingDown = new AtomicBoolean(false);
      if (config.isEnabled()) {
         this.setupLogDirectory();
         this.startLoggingTask();
      }

      DiscordWebhookConfig discordConfig = new DiscordWebhookConfig(plugin);
      if (discordConfig.isEnabled()) {
         DiscordEmbedConfigManager embedManager = new DiscordEmbedConfigManager(plugin, discordConfig);
         this.discordLogger = new DiscordWebhookLogger(plugin, discordConfig, embedManager);
      }
   }

   public void log(SpawnerLogEntry entry) {
      if (this.config.isEnabled() && !this.config.isEventEnabled(entry.getEventType())) {
         if (this.config.isConsoleOutput()) {
            this.plugin.getLogger().info("[SpawnerLog] " + entry.toReadableString());
         }

         this.logQueue.offer(entry);
         if (this.discordLogger != null) {
            this.discordLogger.queueWebhook(entry);
         }
      }
   }

   public void log(SpawnerEventType eventType, SpawnerActionLogger.LogEntryConsumer consumer) {
      if (this.config.isEnabled() && !this.config.isEventEnabled(eventType)) {
         SpawnerLogEntry.Builder builder = new SpawnerLogEntry.Builder(eventType);
         consumer.accept(builder);
         this.log(builder.build());
      }
   }

   private void setupLogDirectory() {
      try {
         Path logPath = Paths.get(this.plugin.getDataFolder().getAbsolutePath(), this.config.getLogDirectory());
         Files.createDirectories(logPath);
         String fileName = "spawner-" + dateFormat.get().format(new Date()) + (this.config.isJsonFormat() ? ".json" : ".log");
         this.currentLogFile = logPath.resolve(fileName).toFile();
         this.rotateLogsIfNeeded();
      } catch (IOException var3) {
         this.plugin.getLogger().log(Level.SEVERE, "Failed to setup log directory", (Throwable)var3);
      }
   }

   private void startLoggingTask() {
      this.logTask = Scheduler.runTaskTimerAsync(() -> {
         if (!this.isShuttingDown.get()) {
            this.processLogQueue();
         }
      }, 40L, 40L);
   }

   private void processLogQueue() {
      if (!this.logQueue.isEmpty()) {
         List<SpawnerLogEntry> entries = new ArrayList<>();

         SpawnerLogEntry entry;
         while ((entry = this.logQueue.poll()) != null) {
            entries.add(entry);
         }

         if (!entries.isEmpty()) {
            this.writeLogEntries(entries);
         }
      }
   }

   private void writeLogEntries(List<SpawnerLogEntry> entries) {
      if (this.currentLogFile != null && !entries.isEmpty()) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.currentLogFile, true))) {
            for (SpawnerLogEntry entry : entries) {
               String logLine = this.config.isJsonFormat() ? entry.toJson() : entry.toReadableString();
               writer.write(logLine);
               writer.newLine();
            }

            writer.flush();
            this.checkAndRotateLog();
         } catch (IOException var8) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to write log entries", (Throwable)var8);
         }
      }
   }

   private void checkAndRotateLog() {
      if (this.currentLogFile != null && this.currentLogFile.exists()) {
         long fileSizeBytes = this.currentLogFile.length();
         long maxSizeBytes = this.config.getMaxLogSizeMB() * 1024L * 1024L;
         if (fileSizeBytes > maxSizeBytes) {
            this.rotateLog();
         }
      }
   }

   private void rotateLog() {
      try {
         String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
         String extension = this.config.isJsonFormat() ? ".json" : ".log";
         Path logPath = Paths.get(this.plugin.getDataFolder().getAbsolutePath(), this.config.getLogDirectory());
         File rotatedFile = logPath.resolve("spawner-" + timestamp + extension).toFile();
         Files.move(this.currentLogFile.toPath(), rotatedFile.toPath());
         String fileName = "spawner-" + dateFormat.get().format(new Date()) + extension;
         this.currentLogFile = logPath.resolve(fileName).toFile();
         this.plugin.getLogger().info("Rotated spawner log to: " + rotatedFile.getName());
         this.cleanupOldLogs();
      } catch (IOException var6) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to rotate log file", (Throwable)var6);
      }
   }

   private void rotateLogsIfNeeded() {
      try {
         Path logPath = Paths.get(this.plugin.getDataFolder().getAbsolutePath(), this.config.getLogDirectory());
         File[] logFiles = logPath.toFile().listFiles((dir, name) -> name.startsWith("spawner-") && (name.endsWith(".log") || name.endsWith(".json")));
         if (logFiles != null && logFiles.length > this.config.getMaxLogFiles()) {
            Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified));
            int filesToDelete = logFiles.length - this.config.getMaxLogFiles();

            for (int i = 0; i < filesToDelete; i++) {
               if (logFiles[i].delete()) {
                  this.plugin.getLogger().info("Deleted old log file: " + logFiles[i].getName());
               }
            }
         }
      } catch (Exception var5) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to rotate old logs", (Throwable)var5);
      }
   }

   private void cleanupOldLogs() {
      this.rotateLogsIfNeeded();
   }

   public void reloadDiscord() {
      DiscordWebhookConfig newDiscordConfig = new DiscordWebhookConfig(this.plugin);
      DiscordEmbedConfigManager newEmbedManager = new DiscordEmbedConfigManager(this.plugin, newDiscordConfig);
      if (this.discordLogger != null) {
         this.discordLogger.reload(newDiscordConfig, newEmbedManager);
      } else if (newDiscordConfig.isEnabled()) {
         this.discordLogger = new DiscordWebhookLogger(this.plugin, newDiscordConfig, newEmbedManager);
      }
   }

   public void shutdown() {
      this.isShuttingDown.set(true);
      if (this.logTask != null) {
         this.logTask.cancel();
      }

      this.processLogQueue();
      if (this.discordLogger != null) {
         this.discordLogger.shutdown();
      }
   }

   @FunctionalInterface
   public interface LogEntryConsumer {
      void accept(SpawnerLogEntry.Builder var1);
   }
}
