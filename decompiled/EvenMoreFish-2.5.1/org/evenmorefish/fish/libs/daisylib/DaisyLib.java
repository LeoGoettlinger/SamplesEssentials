package org.evenmorefish.fish.libs.daisylib;

import java.util.function.Supplier;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.evenmorefish.fish.libs.daisylib.events.brush.BrushEventListener;
import org.evenmorefish.fish.libs.daisylib.events.move.MoveEventListener;
import org.evenmorefish.fish.libs.daisylib.external.vault.VaultWrapper;
import org.evenmorefish.fish.libs.daisylib.logging.Logging;
import org.evenmorefish.fish.libs.daisylib.utils.CommonUtils;
import org.evenmorefish.fish.libs.daisylib.version.MinecraftVersion;
import org.jspecify.annotations.NonNull;

public class DaisyLib {
   private static final DaisyLib INSTANCE = new DaisyLib();
   public static final boolean IS_FOLIA = CommonUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
   private Plugin plugin;
   private Logging logging = Logging.logging("DaisyLib");

   private DaisyLib() {
      if (MinecraftVersion.getCurrentVersion().isOlderThan(MinecraftVersion.V1_21_1)) {
         throw new UnsupportedOperationException("Unsupported Minecraft version. Only 1.21.1 and above are supported.");
      }
   }

   @NonNull
   public static DaisyLib get() {
      return INSTANCE;
   }

   public void init(@NonNull Plugin plugin) {
      if (this.plugin != null) {
         this.getLogging().info("Skipping initialization attempt from " + plugin.getName());
      } else {
         this.plugin = plugin;
         this.logging = Logging.logging("DaisyLib via " + plugin.getName());
         VaultWrapper.get().load();
         this.registerListeners(plugin);
         this.logging.info("DaisyLib initialized successfully.");
      }
   }

   @NonNull
   public Plugin getPlugin() {
      if (this.plugin == null) {
         this.logging.error("DaisyLib has not been initialized. You must call DaisyLib#init(Plugin).");
         throw new IllegalStateException();
      } else {
         return this.plugin;
      }
   }

   @NonNull
   public Logging getLogging() {
      return this.logging;
   }

   private void registerListeners(@NonNull Plugin plugin) {
      PluginManager pm = plugin.getServer().getPluginManager();
      if (DaisyLib.Settings.ENABLE_MOVE_EVENTS.get()) {
         pm.registerEvents(new MoveEventListener(), plugin);
      }

      if (DaisyLib.Settings.ENABLE_BRUSH_EVENT.get()) {
         pm.registerEvents(new BrushEventListener(), plugin);
      }
   }

   public static class Settings {
      @NonNull
      public static Supplier<Boolean> ENABLE_DEBUG = () -> false;
      @NonNull
      public static Supplier<Boolean> ALLOW_LEGACY_MESSAGES = () -> false;
      @NonNull
      public static Supplier<Boolean> ENABLE_MOVE_EVENTS = () -> false;
      @NonNull
      public static Supplier<Boolean> ENABLE_BRUSH_EVENT = () -> false;
   }
}
