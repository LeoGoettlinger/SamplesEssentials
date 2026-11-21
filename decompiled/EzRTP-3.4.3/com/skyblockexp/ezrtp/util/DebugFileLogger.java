package com.skyblockexp.ezrtp.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import org.bukkit.plugin.java.JavaPlugin;

public final class DebugFileLogger {
   private static final String LOG_DIRECTORY = "logs";
   private static final String LOG_FILE = "search-debug.log";
   private final JavaPlugin plugin;
   private final Object lock = new Object();

   public DebugFileLogger(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void log(String message) {
      if (this.plugin != null && message != null && !message.isBlank()) {
         synchronized (this.lock) {
            try {
               Path folder = this.plugin.getDataFolder().toPath().resolve("logs");
               Files.createDirectories(folder);
               Path file = folder.resolve("search-debug.log");
               String payload = "[" + Instant.now() + "] " + message + System.lineSeparator();
               Files.writeString(file, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            } catch (IOException var7) {
               this.plugin.getLogger().fine("Failed to write debug log file: " + var7.getMessage());
            }
         }
      }
   }
}
