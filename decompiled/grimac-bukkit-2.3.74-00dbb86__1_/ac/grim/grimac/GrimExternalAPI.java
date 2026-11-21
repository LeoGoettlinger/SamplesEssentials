package ac.grim.grimac;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.alerts.AlertManager;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.event.events.GrimReloadEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.manager.config.ConfigManagerFileImpl;
import ac.grim.grimac.manager.init.start.StartableInitable;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.common.ConfigReloadObserver;
import ac.grim.grimac.utils.common.PropertiesUtil;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Generated;
import org.bukkit.entity.Player;

public class GrimExternalAPI implements GrimAbstractAPI, ConfigReloadObserver, StartableInitable {
   private final GrimAPI api;
   private final Map<String, Function<GrimUser, String>> variableReplacements = new ConcurrentHashMap<>();
   private final Map<String, String> staticReplacements = new ConcurrentHashMap<>();
   private final Map<String, Function<Object, Object>> functions = new ConcurrentHashMap<>();
   private final ConfigManagerFileImpl configManagerFile = new ConfigManagerFileImpl();
   private final String grimVersion;
   private ConfigManager configManager = null;
   private boolean started = false;

   public GrimExternalAPI(GrimAPI api) {
      this.api = api;
      this.grimVersion = resolveGrimVersion(api);
   }

   @NotNull
   @Override
   public EventBus getEventBus() {
      return this.api.getEventBus();
   }

   @Nullable
   @Override
   public GrimUser getGrimUser(Player player) {
      return this.getGrimUser(player.getUniqueId());
   }

   @Nullable
   @Override
   public GrimUser getGrimUser(UUID uuid) {
      return this.api.getPlayerDataManager().getPlayer(uuid);
   }

   @Override
   public void registerVariable(String string, Function<GrimUser, String> replacement) {
      if (replacement == null) {
         this.variableReplacements.remove(string);
      } else {
         this.variableReplacements.put(string, replacement);
      }
   }

   @Override
   public void registerVariable(String variable, String replacement) {
      if (replacement == null) {
         this.staticReplacements.remove(variable);
      } else {
         this.staticReplacements.put(variable, replacement);
      }
   }

   @Override
   public String getGrimVersion() {
      return this.grimVersion;
   }

   private static String resolveGrimVersion(GrimAPI api) {
      try {
         Properties properties = PropertiesUtil.readProperties(GrimExternalAPI.class, "grimac.properties");
         String buildVersion = properties.getProperty("build.version");
         if (buildVersion != null && !buildVersion.isBlank() && !buildVersion.startsWith("${")) {
            return buildVersion;
         }
      } catch (RuntimeException var4) {
      }

      try {
         return api.getGrimPlugin().getDescription().getVersion();
      } catch (RuntimeException var3) {
         return "unknown";
      }
   }

   @Override
   public void registerFunction(String key, Function<Object, Object> function) {
      if (function == null) {
         this.functions.remove(key);
      } else {
         this.functions.put(key, function);
      }
   }

   @Override
   public Function<Object, Object> getFunction(String key) {
      return this.functions.get(key);
   }

   @Override
   public AlertManager getAlertManager() {
      return GrimAPI.INSTANCE.getAlertManager();
   }

   @Override
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Override
   public boolean hasStarted() {
      return this.started;
   }

   @Override
   public int getCurrentTick() {
      return GrimAPI.INSTANCE.getTickManager().currentTick;
   }

   @NotNull
   @Override
   public GrimPlugin getGrimPlugin(@NotNull Object o) {
      return this.api.getExtensionManager().getPlugin(o);
   }

   @NotNull
   @Override
   public BackendRegistry getBackendRegistry() {
      return this.api.getBackendRegistry();
   }

   public void load() {
      this.reload((ConfigManager)this.configManagerFile);
      this.api.getLoader().registerAPIService();
   }

   @Override
   public void start() {
      this.started = true;

      try {
         GrimAPI.INSTANCE.getConfigManager().start();
      } catch (Exception var2) {
         LogUtil.error("Failed to start config manager.", var2);
      }
   }

   public void reload(ConfigManager config) {
      if (config.isLoadedAsync() && this.started) {
         GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> this.successfulReload(config));
      } else {
         this.successfulReload(config);
      }
   }

   public CompletableFuture<Boolean> reloadAsync(ConfigManager config) {
      if (config.isLoadedAsync() && this.started) {
         CompletableFuture<Boolean> future = new CompletableFuture<>();
         GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> future.complete(this.successfulReload(config)));
         return future;
      } else {
         return CompletableFuture.completedFuture(this.successfulReload(config));
      }
   }

   private boolean successfulReload(ConfigManager config) {
      try {
         config.reload();
         GrimAPI.INSTANCE.getConfigManager().load(config);
         if (this.started) {
            GrimAPI.INSTANCE.getConfigManager().start();
         }

         this.onReload(config);
         if (this.started) {
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> GrimExternalAPI.Channels.RELOAD.fire(true));
         }

         return true;
      } catch (Exception var3) {
         LogUtil.error("Failed to reload config", var3);
         if (this.started) {
            GrimAPI.INSTANCE.getScheduler().getAsyncScheduler().runNow(GrimAPI.INSTANCE.getGrimPlugin(), () -> GrimExternalAPI.Channels.RELOAD.fire(false));
         }

         return false;
      }
   }

   @Override
   public void onReload(ConfigManager newConfig) {
      if (newConfig == null) {
         LogUtil.warn("ConfigManager not set. Using default config file manager.");
         this.configManager = this.configManagerFile;
      } else {
         this.configManager = newConfig;
      }

      this.updateVariables();
      GrimAPI.INSTANCE.getAlertManager().reload(this.configManager);
      GrimAPI.INSTANCE.getDiscordManager().reload();
      GrimAPI.INSTANCE.getSpectateManager().reload();
      if (this.started) {
         if (GrimAPI.INSTANCE.getDataStoreLifecycle() != null) {
            GrimAPI.INSTANCE.getDataStoreLifecycle().reload();
         }

         for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            player.runSafely(() -> player.reload(this.configManager));
         }
      }
   }

   private void updateVariables() {
      this.variableReplacements.putIfAbsent("%player%", GrimUser::getName);
      this.variableReplacements.putIfAbsent("%uuid%", user -> user.getUniqueId().toString());
      this.variableReplacements.putIfAbsent("%ping%", user -> user.getTransactionPing() + "");
      this.variableReplacements.putIfAbsent("%brand%", GrimUser::getBrand);
      this.variableReplacements.putIfAbsent("%h_sensitivity%", user -> (int)Math.round(user.getHorizontalSensitivity() * 200.0) + "");
      this.variableReplacements.putIfAbsent("%v_sensitivity%", user -> (int)Math.round(user.getVerticalSensitivity() * 200.0) + "");
      this.variableReplacements.putIfAbsent("%fast_math%", user -> !user.isVanillaMath() + "");
      this.variableReplacements.putIfAbsent("%tps%", user -> String.format("%.2f", GrimAPI.INSTANCE.getPlatformServer().getTPS()));
      this.variableReplacements.putIfAbsent("%version%", GrimUser::getVersionName);
      this.staticReplacements.put("%prefix%", MessageUtil.translateAlternateColorCodes('&', GrimAPI.INSTANCE.getConfigManager().getPrefix()));
      this.staticReplacements.putIfAbsent("%grim_version%", this.getGrimVersion());
   }

   @Generated
   public Map<String, Function<GrimUser, String>> getVariableReplacements() {
      return this.variableReplacements;
   }

   @Generated
   public Map<String, String> getStaticReplacements() {
      return this.staticReplacements;
   }

   private static final class Channels {
      static final GrimReloadEvent.Channel RELOAD = GrimAPI.INSTANCE.getEventBus().get(GrimReloadEvent.class);
   }
}
