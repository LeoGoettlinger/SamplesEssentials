package eu.raidersheaven.rhsignitem;

import eu.raidersheaven.bstats.Metrics;
import eu.raidersheaven.rhsignitem.commands.CommandNode;
import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import eu.raidersheaven.rhsignitem.handlers.LocaleHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin implements Listener {
   private static Main instance;
   private final ConfigFile config = new ConfigFile(this);
   private static Economy vaultEconomy = null;
   MiniMessage miniMessage = MiniMessage.miniMessage();

   public void onEnable() {
      instance = this;
      this.getPlugins();
      this.saveDefaultConfig();
      this.config.set();
      LocaleHandler.init(this);
      this.loadMetrics();
      this.registerCommands();
      Bukkit.getConsoleSender().sendMessage(this.miniMessage.deserialize(" "));
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold>   __          __ _              _____ _                 </bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold>  /__\\  /\\  /\\/ _(_) __ _ _ __   \\_   \\ |_ ___ _ __ ___  </bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold> / \\// / /_/ /\\ \\| |/ _` | '_ \\   / /\\/ __/ _ \\ '_ ` _ \\ </bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold>/ _  \\/ __  / _\\ \\ | (_| | | | /\\/ /_ | | | __/ | | | | |</bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold>\\/ \\_/\\/ /_/  \\__/_|\\__, |_| |_\\____/  \\__\\___|_| |_| |_|</bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(
            this.miniMessage.deserialize("<gradient:#FA6F02:#EA034E><bold>                    |___/                                </bold></gradient>")
         );
      Bukkit.getConsoleSender()
         .sendMessage(this.miniMessage.deserialize("<gray>                                                (" + this.getPluginMeta().getVersion() + "<gray>)"));
      Bukkit.getConsoleSender().sendMessage(this.miniMessage.deserialize("<dark_gray>«<white>*<dark_gray>» Adds a personal item signature ☄️ Customizable!"));
      Bukkit.getConsoleSender().sendMessage(this.miniMessage.deserialize("<dark_gray>«<white>*<dark_gray>» Made with \ud83d\udc95 by <gray>X0R3"));
      Bukkit.getConsoleSender().sendMessage(this.miniMessage.deserialize(" "));
      if (!this.setupEconomy()) {
         Bukkit.getConsoleSender().sendMessage(this.miniMessage.deserialize("<gray>Vault <dark_gray>not found! Disabling economy integration."));
      }
   }

   public void onDisable() {
      Bukkit.getConsoleSender()
         .sendMessage(this.miniMessage.deserialize("<dark_gray>Disabling <gradient:#FA6F02:#EA034E><bold>RHSignItem</bold></gradient><dark_gray>."));
   }

   private void registerCommands() {
      this.getLifecycleManager()
         .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> ((Commands)event.registrar()).register(CommandNode.createSign(this))));
      this.getLifecycleManager()
         .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> ((Commands)event.registrar()).register(CommandNode.createRename(this))));
   }

   public static Main get() {
      return instance;
   }

   public void loadMetrics() {
      int pluginId = 11372;
      new Metrics(this, pluginId);
   }

   private void getPlugins() {
      Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
      Bukkit.getPluginManager().getPlugin("Vault");
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            vaultEconomy = (Economy)rsp.getProvider();
            return true;
         }
      }
   }

   public static Economy getEconomy() {
      return vaultEconomy;
   }
}
