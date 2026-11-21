package net.coreprotect;

import java.io.File;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.services.PluginInitializationService;
import net.coreprotect.services.ShutdownService;
import net.coreprotect.utility.Chat;
import org.bukkit.plugin.java.JavaPlugin;

public final class CoreProtect extends JavaPlugin {
   private int pluginBridge;
   private static CoreProtect instance;
   private boolean advancedChestsEnabled = false;
   private final CoreProtectAPI api = new CoreProtectAPI();

   public static CoreProtect getInstance() {
      return instance;
   }

   public CoreProtectAPI getAPI() {
      return this.api;
   }

   public void onEnable() {
      this.pluginBridge = this.preparePluginBridge();
      instance = this;
      ConfigHandler.path = this.getDataFolder().getPath() + File.separator;
      this.advancedChestsEnabled = this.getServer().getPluginManager().getPlugin("AdvancedChests") != null;
      boolean initialized = PluginInitializationService.initializePlugin(this);
      if (!initialized) {
         Chat.console(Phrase.build(Phrase.ENABLE_FAILED, ConfigHandler.EDITION_NAME));
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void onDisable() {
      ShutdownService.safeShutdown(this);
   }

   public boolean isAdvancedChestsEnabled() {
      return this.advancedChestsEnabled;
   }
}
