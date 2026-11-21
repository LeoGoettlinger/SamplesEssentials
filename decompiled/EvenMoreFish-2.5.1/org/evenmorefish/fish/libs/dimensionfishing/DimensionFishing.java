package org.evenmorefish.fish.libs.dimensionfishing;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.evenmorefish.fish.libs.dimensionfishing.common.FishingListener;
import org.evenmorefish.fish.libs.dimensionfishing.common.HookManager;
import org.evenmorefish.fish.libs.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.fish.libs.dimensionfishing.state.FishingState;
import org.evenmorefish.fish.libs.dimensionfishing.stats.bukkit.Metrics;
import org.evenmorefish.fish.libs.dimensionfishing.stats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DimensionFishing {
   private static DimensionFishing INSTANCE;
   private final JavaPlugin plugin;
   private final Metrics metrics;
   private final Logger logger;
   private final DimensionFishingConfigProvider configProvider;
   private boolean hasMcMMODependency;

   public DimensionFishing(@NotNull JavaPlugin plugin, @NotNull DimensionFishingConfigProvider configProvider) {
      if (INSTANCE != null) {
         throw new UnsupportedOperationException(this.getClass().getName() + " has already been assigned!");
      } else {
         INSTANCE = this;
         this.plugin = plugin;
         this.configProvider = configProvider;
         this.logger = Logger.getLogger("DimensionFishing via " + plugin.getName());
         this.metrics = new Metrics(plugin, 32045);
         this.setupMetrics();

         try {
            Class.forName("com.gmail.nossr50.mcMMO");
            this.hasMcMMODependency = true;
         } catch (ClassNotFoundException var4) {
            this.hasMcMMODependency = false;
         }
      }
   }

   @NotNull
   public static DimensionFishing getInstance() {
      return INSTANCE;
   }

   private void setupMetrics() {
      this.metrics.addCustomChart(new SimplePie("typesEnabled", () -> {
         boolean lavaEnabled = this.configProvider.isLavaEnabled();
         boolean voidEnabled = this.configProvider.isVoidEnabled();
         if (lavaEnabled && voidEnabled) {
            return "Both";
         } else if (lavaEnabled) {
            return "Lava";
         } else {
            return voidEnabled ? "Void" : "None";
         }
      }));
   }

   @NotNull
   public JavaPlugin getPlugin() {
      return this.plugin;
   }

   @NotNull
   public DimensionFishingConfigProvider getConfigProvider() {
      return this.configProvider;
   }

   public void load() {
   }

   public void enable() {
      HookManager.getInstance().load();
      Bukkit.getPluginManager().registerEvents(new FishingListener(), this.plugin);
   }

   public void disable() {
      HookManager.getInstance().shutdown();
   }

   public void reload(@Nullable CommandSender sender) {
      if (sender instanceof Player player) {
         FishingState.LAVA.getLureParticles().show(player.getLocation());
         FishingState.VOID.getLureParticles().show(player.getLocation());
      }
   }

   public Logger getLogger() {
      return this.logger;
   }

   public boolean hasMcMMODependency() {
      return this.hasMcMMODependency;
   }
}
