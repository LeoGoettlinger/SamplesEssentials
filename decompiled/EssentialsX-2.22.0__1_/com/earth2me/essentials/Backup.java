package com.earth2me.essentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class Backup implements Runnable {
   private final transient Server server;
   private final transient net.ess3.api.IEssentials ess;
   private final AtomicBoolean pendingShutdown = new AtomicBoolean(false);
   private transient boolean running = false;
   private transient int taskId = -1;
   private transient boolean active = false;
   private transient CompletableFuture<Object> taskLock = null;

   public Backup(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      this.server = ess.getServer();
      if (!ess.getOnlinePlayers().isEmpty() || ess.getSettings().isAlwaysRunBackup()) {
         ess.runTaskAsynchronously(this::startTask);
      }
   }

   public void onPlayerJoin() {
      this.startTask();
   }

   public synchronized void stopTask() {
      this.running = false;
      if (this.taskId != -1) {
         this.server.getScheduler().cancelTask(this.taskId);
      }

      this.taskId = -1;
   }

   private synchronized void startTask() {
      if (!this.running) {
         long interval = this.ess.getSettings().getBackupInterval() * 1200L;
         if (interval < 1200L) {
            return;
         }

         this.taskId = this.ess.scheduleSyncRepeatingTask(this, interval, interval);
         this.running = true;
      }
   }

   public CompletableFuture<Object> getTaskLock() {
      return this.taskLock;
   }

   public void setPendingShutdown(final boolean shutdown) {
      this.pendingShutdown.set(shutdown);
   }

   @Override
   public void run() {
      if (!this.active) {
         String command = this.ess.getSettings().getBackupCommand();
         if (command != null && !"".equals(command)) {
            this.active = true;
            this.taskLock = new CompletableFuture<>();
            if ("save-all".equalsIgnoreCase(command)) {
               CommandSender cs = this.server.getConsoleSender();
               this.server.dispatchCommand(cs, "save-all");
               this.active = false;
               this.taskLock.complete(new Object());
            } else {
               this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("backupStarted")));
               final CommandSender cs = this.server.getConsoleSender();
               this.server.dispatchCommand(cs, "save-all");
               this.server.dispatchCommand(cs, "save-off");

               class BackupEnableSaveTask implements Runnable {
                  @Override
                  public void run() {
                     Backup.this.server.dispatchCommand(cs, "save-on");
                     if (!Backup.this.ess.getSettings().isAlwaysRunBackup() && Backup.this.ess.getOnlinePlayers().isEmpty()) {
                        Backup.this.stopTask();
                     }

                     Backup.this.active = false;
                     Backup.this.taskLock.complete(new Object());
                     Backup.this.ess.getLogger().log(Level.INFO, Backup.this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("backupFinished")));
                  }
               }

               this.ess.runTaskAsynchronously(() -> {
                  try {
                     ProcessBuilder childBuilder = new ProcessBuilder(command.split(" "));
                     childBuilder.redirectErrorStream(true);
                     childBuilder.directory(this.ess.getDataFolder().getParentFile().getParentFile());
                     Process child = childBuilder.start();
                     this.ess.runTaskAsynchronously(() -> {
                        try {
                           BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));

                           String line;
                           try {
                              do {
                                 line = reader.readLine();
                                 if (line != null) {
                                    this.ess.getLogger().log(Level.INFO, line);
                                 }
                              } while (line != null);
                           } catch (Throwable var6) {
                              try {
                                 reader.close();
                              } catch (Throwable var5) {
                                 var6.addSuppressed(var5);
                              }

                              throw var6;
                           }

                           reader.close();
                        } catch (IOException var7) {
                           this.ess.getLogger().log(Level.SEVERE, "An error occurred while reading backup child process", (Throwable)var7);
                        }
                     });
                     child.waitFor();
                  } catch (IOException | InterruptedException var8) {
                     this.ess.getLogger().log(Level.SEVERE, "An error occurred while building the backup child process", (Throwable)var8);
                  } finally {
                     if (!this.pendingShutdown.get()) {
                        this.ess.scheduleSyncDelayedTask(new BackupEnableSaveTask());
                     }
                  }
               });
            }
         }
      }
   }
}
