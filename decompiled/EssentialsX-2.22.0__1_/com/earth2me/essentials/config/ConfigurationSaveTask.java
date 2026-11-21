package com.earth2me.essentials.config;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.yaml.YamlConfigurationLoader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ConfigurationSaveTask implements Runnable {
   private final YamlConfigurationLoader loader;
   private final Supplier<CommentedConfigurationNode> nodeSupplier;
   private final AtomicInteger pendingWrites;

   public ConfigurationSaveTask(
      final YamlConfigurationLoader loader, final Supplier<CommentedConfigurationNode> nodeSupplier, final AtomicInteger pendingWrites
   ) {
      this.loader = loader;
      this.nodeSupplier = nodeSupplier;
      this.pendingWrites = pendingWrites;
   }

   @Override
   public void run() {
      if (this.pendingWrites.decrementAndGet() <= 0) {
         synchronized (this.loader) {
            try {
               CommentedConfigurationNode node = this.nodeSupplier.get();
               this.loader.save(node);
            } catch (ConfigurateException var4) {
               Essentials.getWrappedLogger().log(Level.SEVERE, var4.getMessage(), (Throwable)var4);
            }
         }
      }
   }
}
