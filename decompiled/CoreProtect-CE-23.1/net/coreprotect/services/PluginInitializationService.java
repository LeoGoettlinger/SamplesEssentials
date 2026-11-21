package net.coreprotect.services;

import java.io.File;
import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.Metrics;
import net.coreprotect.command.CommandHandler;
import net.coreprotect.command.TabHandler;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.language.Language;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.ListenerHandler;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.thread.NetworkHandler;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginInitializationService {
   private PluginInitializationService() {
      throw new IllegalStateException("Utility class");
   }

   public static boolean initializePlugin(CoreProtect plugin) {
      Language.loadPhrases();
      boolean start = VersionCheckService.performVersionChecks();
      if (!start) {
         return false;
      } else {
         try {
            Consumer.initialize();
            new ListenerHandler(plugin);
            registerCommands(plugin);
            createDataDirectory();
            start = ConfigHandler.performInitialization(true);
         } catch (Exception var3) {
            var3.printStackTrace();
            return false;
         }

         if (start) {
            displayStartupMessages(plugin);
            startBackgroundServices(plugin);
            enableMetrics(plugin);
         }

         return start;
      }
   }

   private static void registerCommands(JavaPlugin plugin) {
      plugin.getCommand("coreprotect").setExecutor(CommandHandler.getInstance());
      plugin.getCommand("coreprotect").setTabCompleter(new TabHandler());
      plugin.getCommand("core").setExecutor(CommandHandler.getInstance());
      plugin.getCommand("core").setTabCompleter(new TabHandler());
      plugin.getCommand("co").setExecutor(CommandHandler.getInstance());
      plugin.getCommand("co").setTabCompleter(new TabHandler());
   }

   private static void createDataDirectory() {
      boolean exists = new File(ConfigHandler.path).exists();
      if (!exists) {
         new File(ConfigHandler.path).mkdir();
      }
   }

   private static void displayStartupMessages(JavaPlugin plugin) {
      PluginDescriptionFile pluginDescription = plugin.getDescription();
      ChatUtils.sendConsoleComponentStartup(Bukkit.getServer().getConsoleSender(), Phrase.build(Phrase.ENABLE_SUCCESS, ConfigHandler.EDITION_NAME));
      if (Config.getGlobal().MYSQL) {
         Chat.console(Phrase.build(Phrase.USING_MYSQL));
      } else {
         Chat.console(Phrase.build(Phrase.USING_SQLITE));
      }

      Chat.console("--------------------");
      Chat.console(Phrase.build(Phrase.ENJOY_COREPROTECT, pluginDescription.getName()));
      Chat.console(Phrase.build(Phrase.LINK_DISCORD, "www.coreprotect.net/discord/"));
      Chat.console("--------------------");
   }

   private static void startBackgroundServices(CoreProtect plugin) {
      Scheduler.scheduleSyncDelayedTask(plugin, () -> {
         try {
            Thread networkHandler = new Thread(new NetworkHandler(true, true));
            networkHandler.start();
         } catch (Exception var1x) {
            var1x.printStackTrace();
         }
      }, 0);
      Thread cacheCleanUpThread = new Thread(new CacheHandler());
      cacheCleanUpThread.start();
      Consumer.startConsumer();
   }

   private static void enableMetrics(JavaPlugin plugin) {
      try {
         new Metrics(plugin, 2876);
      } catch (Exception var2) {
      }
   }
}
