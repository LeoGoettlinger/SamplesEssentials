package me.byteful.plugin.leveltools.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

public class UpdateChecker {
   @NotNull
   private final LevelToolsPlugin plugin;
   private final Scheduler scheduler;
   private String lastCheckedVersion = null;

   public UpdateChecker(@NotNull LevelToolsPlugin plugin, Scheduler scheduler) {
      this.plugin = plugin;
      this.scheduler = scheduler;
   }

   public void check() {
      this.plugin.getLogger().info("Checking for updates...");
      String currentVersion = this.plugin.getDescription().getVersion();
      if (currentVersion.contains("BETA")) {
         this.plugin.getLogger().info("Update check was cancelled because you are running a beta build!");
      } else {
         this.scheduler.asyncDelayed(() -> this.check0(currentVersion), 1L);
      }
   }

   private void check0(String currentVersion) {
      try {
         InputStream inputStream = new URL("https://api.byteful.me/leveltools").openStream();

         label79: {
            try {
               Scanner scanner = new Scanner(inputStream);

               label62: {
                  try {
                     if (scanner.hasNext()) {
                        String latestVersion = scanner.next();
                        if (currentVersion.equals(latestVersion)) {
                           this.plugin.getLogger().info("No new updates found.");
                        } else {
                           this.plugin
                              .getLogger()
                              .info("A new update was found. You are on " + currentVersion + " while the latest version is " + latestVersion + ".");
                           this.plugin
                              .getLogger()
                              .info(
                                 "Please install this update from: https://github.com/byteful/LevelTools/releases/download/v"
                                    + latestVersion
                                    + "/LevelTools-"
                                    + latestVersion
                                    + ".jar"
                              );
                        }

                        this.lastCheckedVersion = latestVersion;
                        break label62;
                     }
                  } catch (Throwable var8) {
                     try {
                        scanner.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }

                     throw var8;
                  }

                  scanner.close();
                  break label79;
               }

               scanner.close();
            } catch (Throwable var9) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (inputStream != null) {
               inputStream.close();
            }

            return;
         }

         if (inputStream != null) {
            inputStream.close();
         }
      } catch (IOException var10) {
         this.plugin.getLogger().info("Unable to check for updates: " + var10.getMessage());
      }
   }

   public String getLastCheckedVersion() {
      return this.lastCheckedVersion;
   }
}
