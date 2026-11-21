package com.magmaguy.elitemobs.magmacore.initialization;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginInitializationContext {
   private static final boolean TIMING_ENABLED = Boolean.getBoolean("magmacore.inittiming");
   private final JavaPlugin plugin;
   private final PluginInitializationConfig config;
   private final String ownerToken;
   private final BooleanSupplier shutdownRequested;
   private final List<String> stepTimings = new ArrayList<>();
   private final long startNanos = System.nanoTime();
   private String currentStep = null;
   private long currentStepNanos = 0L;

   PluginInitializationContext(JavaPlugin plugin, PluginInitializationConfig config, String ownerToken, BooleanSupplier shutdownRequested) {
      this.plugin = plugin;
      this.config = config;
      this.ownerToken = ownerToken;
      this.shutdownRequested = shutdownRequested;
   }

   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   public PluginInitializationConfig getConfig() {
      return this.config;
   }

   public void step(String description) {
      if (!this.shutdownRequested.getAsBoolean()) {
         if (TIMING_ENABLED) {
            long now = System.nanoTime();
            if (this.currentStep != null) {
               this.stepTimings.add(String.format("%6d ms  %s", (now - this.currentStepNanos) / 1000000L, this.currentStep));
            }

            this.currentStep = description;
            this.currentStepNanos = now;
         }

         PluginInitializationProgressBar.step(this.plugin, this.ownerToken, description);
      }
   }

   void reportTimings() {
      if (TIMING_ENABLED) {
         long now = System.nanoTime();
         if (this.currentStep != null) {
            this.stepTimings.add(String.format("%6d ms  %s", (now - this.currentStepNanos) / 1000000L, this.currentStep));
            this.currentStep = null;
         }

         Logger.info("Initialization profile (total " + (now - this.startNanos) / 1000000L + " ms):");

         for (String timing : this.stepTimings) {
            Logger.info("  " + timing);
         }
      }
   }

   public void status(String description) {
      if (!this.shutdownRequested.getAsBoolean()) {
         PluginInitializationProgressBar.status(this.plugin, this.ownerToken, description);
      }
   }

   public boolean isShutdownRequested() {
      return this.shutdownRequested.getAsBoolean();
   }
}
