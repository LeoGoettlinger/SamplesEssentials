package org.lushplugins.lushrewards.storage;

import com.google.gson.JsonObject;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.module.UserDataModule;
import org.lushplugins.lushrewards.storage.type.JsonStorage;
import org.lushplugins.lushrewards.storage.type.MySQLStorage;
import org.lushplugins.lushrewards.storage.type.PostgreSQLStorage;
import org.lushplugins.lushrewards.storage.type.SQLiteStorage;

public class StorageManager {
   private final ExecutorService threads = Executors.newFixedThreadPool(1);
   private Storage storage;

   public StorageManager() {
      this.reload();
   }

   public void reload() {
      this.disable();
      FileConfiguration config = LushRewards.getInstance().getConfigResource("storage.yml");
      String storageType = config.getString("type", "null");
      String outdated = config.getString("type", "null");
      switch (outdated) {
         case "mysql":
         case "mariadb":
            this.storage = new MySQLStorage();
            break;
         case "postgres":
            this.storage = new PostgreSQLStorage();
            break;
         case "sqlite":
            this.storage = new SQLiteStorage();
            break;
         case "json":
            this.storage = new JsonStorage();
            break;
         default:
            this.storage = new JsonStorage();
            LushRewards.getInstance().getLogger().severe("'" + storageType + "' is not a valid storage type, default to json storage.");
      }

      boolean outdated = config.contains("mysql");
      if (outdated) {
         LushRewards.getInstance().getLogger().warning("Deprecated: The 'mysql' section in the storage.yml has been renamed to 'storage'");
      }

      LushRewards.getInstance().getLogger().info("Setting up '" + storageType + "' database");
      ConfigurationSection storageSection = outdated ? config.getConfigurationSection("mysql") : config.getConfigurationSection("storage");
      this.storage.enable(storageSection);
   }

   public void disable() {
      if (this.storage != null) {
         this.runAsync(this.storage::disable);
      }
   }

   public CompletableFuture<JsonObject> loadModuleUserData(UUID uuid, String moduleId) {
      return this.runAsync(() -> this.storage.loadModuleUserDataJson(uuid, moduleId));
   }

   public CompletableFuture<Void> saveModuleUserData(UserDataModule.UserData userData) {
      return this.runAsync(() -> this.storage.saveModuleUserData(userData));
   }

   private <T> CompletableFuture<T> runAsync(Callable<T> callable) {
      CompletableFuture<T> future = new CompletableFuture<>();
      this.threads.submit(() -> {
         try {
            future.complete(callable.call());
         } catch (Throwable var3) {
            LushRewards.getInstance().getLogger().log(Level.WARNING, "Caught unhandled storage error: ", var3);
            future.completeExceptionally(var3);
         }
      });
      return future;
   }

   private CompletableFuture<Void> runAsync(Runnable runnable) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      this.threads.submit(() -> {
         try {
            runnable.run();
            future.complete(null);
         } catch (Throwable var3) {
            LushRewards.getInstance().getLogger().log(Level.WARNING, "Caught unhandled storage error: ", var3);
            future.completeExceptionally(var3);
         }
      });
      return future;
   }
}
