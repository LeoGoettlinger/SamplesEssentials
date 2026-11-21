package de.oliver.fancynpcs.api;

import com.fancyinnovations.config.featureflags.FeatureFlagConfig;
import de.oliver.fancyanalytics.logger.ExtendedFancyLogger;
import de.oliver.fancylib.serverSoftware.schedulers.FancyScheduler;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.api.actions.ActionManager;
import de.oliver.fancynpcs.api.skins.SkinManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

public interface FancyNpcsPlugin {
   static FancyNpcsPlugin get() {
      PluginManager pluginManager = Bukkit.getPluginManager();
      if (pluginManager.isPluginEnabled("FancyNpcs")) {
         return (FancyNpcsPlugin)pluginManager.getPlugin("FancyNpcs");
      } else {
         throw new NullPointerException("Plugin is not enabled");
      }
   }

   JavaPlugin getPlugin();

   ExtendedFancyLogger getFancyLogger();

   ScheduledExecutorService getNpcThread();

   @ApiStatus.Internal
   Thread newThread(String var1, Runnable var2);

   FancyScheduler getScheduler();

   Function<NpcData, Npc> getNpcAdapter();

   FancyNpcsConfig getFancyNpcConfig();

   FeatureFlagConfig getFeatureFlagConfig();

   NpcManager getNpcManager();

   AttributeManager getAttributeManager();

   ActionManager getActionManager();

   SkinManager getSkinManager();

   Translator getTranslator();

   @ApiStatus.Internal
   void registerCommand(Object var1);
}
