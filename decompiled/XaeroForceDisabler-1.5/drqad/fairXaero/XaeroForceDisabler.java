package drqad.fairXaero;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class XaeroForceDisabler extends JavaPlugin implements Listener, TabCompleter {
   private int playerCache;
   private XaeroForceDisabler.Settings globalSettings;
   private final Map<String, XaeroForceDisabler.Settings> worldOverrides = new HashMap<>();
   private volatile boolean debugEnabled = false;
   private Logger logger;
   private volatile boolean isReloading = false;
   private static final String PACKET_DISABLE = "§n§o§m§i§n§i§m§a§p";
   private static final String PACKET_FAIR = "§f§a§i§r§x§a§e§r§o";
   private static final String PACKET_RESET = "§r§e§s§e§t§x§a§e§r§o";
   private static final String PACKET_FAIRNETHER = "§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r";
   private static final String PACKET_FAIRNETHER_MINIMAP = "§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r";

   public void onEnable() {
      this.logger = this.getLogger();

      try {
         this.saveDefaultConfig();
         this.loadConfiguration();
         if (this.debugEnabled) {
            this.logger.info("Debugging is enabled");
         }

         this.registerCommands();
         this.getServer().getPluginManager().registerEvents(this, this);
         this.logger.info("FairXaero has been enabled successfully!");
      } catch (Exception var2) {
         this.logger.severe("Failed to enable FairXaero: " + var2.getMessage());
         var2.printStackTrace();
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void onDisable() {
      try {
         HandlerList.unregisterAll(this);
         this.saveConfig();
         this.logger.info("FairXaero has been disabled successfully!");
      } catch (Exception var2) {
         this.logger.severe("Error during plugin disable: " + var2.getMessage());
      }
   }

   private void loadConfiguration() {
      this.playerCache = this.initializePlayerCache();
      this.reloadConfig();
      FileConfiguration config = this.getConfig();
      String currentVersion = config.getString("config-version", "1.0");
      if (config.contains("auto-settings")) {
         this.logger.info("Old configuration detected. Migrating to new format...");
         boolean oldAutoDisable = config.getBoolean("auto-settings.auto-disable", false);
         boolean oldAutoFair = config.getBoolean("auto-settings.auto-fair", false);
         boolean oldDebug = config.getBoolean("debug", false);
         File configFile = new File(this.getDataFolder(), "config.yml");
         if (configFile.exists()) {
            configFile.delete();
         }

         this.saveDefaultConfig();
         this.reloadConfig();
         config = this.getConfig();
         config.set("global.disable", oldAutoDisable);
         config.set("global.fair-mode", oldAutoFair);
         config.set("debug", oldDebug);
         this.saveConfig();
         this.logger.info("Migration complete. Old settings preserved.");
      }

      this.debugEnabled = config.getBoolean("debug", false);
      boolean gFair = config.getBoolean("global.fair-mode", false);
      boolean gDisable = config.getBoolean("global.disable", false);
      this.globalSettings = new XaeroForceDisabler.Settings(gFair, gDisable);
      this.worldOverrides.clear();
      ConfigurationSection overrides = config.getConfigurationSection("overrides");
      if (overrides != null) {
         for (String key : overrides.getKeys(false)) {
            ConfigurationSection section = overrides.getConfigurationSection(key);
            if (section != null) {
               Boolean fair = section.contains("fair-mode") ? section.getBoolean("fair-mode") : null;
               Boolean disable = section.contains("disable") ? section.getBoolean("disable") : null;
               if (fair != null || disable != null) {
                  this.worldOverrides.put(key, new XaeroForceDisabler.Settings(fair, disable));
               }
            }
         }
      }
   }

   private void registerCommands() {
      if (this.getCommand("xaero") != null) {
         this.getCommand("xaero").setTabCompleter(this);
      } else {
         this.logger.severe("Failed to register 'xaero' command!");
      }
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!cmd.getName().equalsIgnoreCase("xaero")) {
         return false;
      } else if (!sender.isOp() && !sender.hasPermission("xaero.edit")) {
         sender.sendMessage("§cYou don't have permission to use this command.");
         return true;
      } else if (args.length == 0) {
         this.sendCommandUsage(sender);
         return true;
      } else {
         if (this.debugEnabled) {
            this.logger.info("Command received: " + String.join(" ", args));
         }

         String arg0 = args[0].toLowerCase();
         switch (arg0) {
            case "reload":
               this.handleReload(sender);
               return true;
            case "manual-disable":
               this.sendTellrawAsync("§n§o§m§i§n§i§m§a§p");
               return true;
            case "manual-fair":
               this.sendTellrawAsync("§f§a§i§r§x§a§e§r§o");
               return true;
            case "manual-reset":
               this.sendTellrawAsync("§r§e§s§e§t§x§a§e§r§o");
               return true;
            case "manual-fairnether":
               this.sendTellrawAsync("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r");
               return true;
            default:
               String scope = args[0];
               if (args.length < 2) {
                  this.showSettings(sender, scope);
                  return true;
               } else {
                  String setting = args[1].toLowerCase();
                  if (setting.equals("fair")) {
                     setting = "fair-mode";
                  }

                  if (!setting.equals("fair-mode") && !setting.equals("disable")) {
                     sender.sendMessage("§cUnknown setting: " + setting + ". Valid: fair-mode, disable");
                     return true;
                  } else if (args.length == 2) {
                     this.showSpecificSetting(sender, scope, setting);
                     return true;
                  } else {
                     String valueStr = args[2].toLowerCase();
                     Boolean value = null;
                     if (valueStr.equals("true")) {
                        value = true;
                     } else if (valueStr.equals("false")) {
                        value = false;
                     } else {
                        if (!valueStr.equals("null") && !valueStr.equals("unset")) {
                           sender.sendMessage("§cInvalid value. Use true, false, or unset/null");
                           return true;
                        }

                        value = null;
                     }

                     this.handleConfigSet(sender, scope, setting, value);
                     return true;
                  }
               }
         }
      }
   }

   private void handleConfigSet(CommandSender sender, String scope, String setting, Boolean value) {
      boolean isGlobal = scope.equalsIgnoreCase("global");
      FileConfiguration config = this.getConfig();
      String path;
      if (isGlobal) {
         path = "global." + setting;
         if (value == null) {
            sender.sendMessage("§cCannot unset global value. defaulting to false.");
            value = false;
         }
      } else {
         path = "overrides." + scope + "." + setting;
      }

      config.set(path, value);
      this.saveConfig();
      this.loadConfiguration();
      if (value == null) {
         sender.sendMessage("§aUnset " + setting + " in " + scope);
      } else {
         sender.sendMessage("§aSet " + setting + " to " + value + " in " + scope);
      }

      for (Player p : Bukkit.getOnlinePlayers()) {
         this.handleWorldAction(p, p.getWorld().getName());
      }
   }

   private void showSettings(CommandSender sender, String scope) {
      boolean isGlobal = scope.equalsIgnoreCase("global");
      sender.sendMessage("§6Settings for " + scope + ":");
      if (isGlobal) {
         sender.sendMessage("§e disable: " + this.globalSettings.disable);
         sender.sendMessage("§e fair-mode: " + this.globalSettings.fairMode);
      } else {
         XaeroForceDisabler.Settings ov = this.worldOverrides.get(scope);
         if (ov == null) {
            sender.sendMessage("§7 (Inherits Global)");
            sender.sendMessage("§7 disable: " + this.globalSettings.disable + " (global)");
            sender.sendMessage("§7 fair-mode: " + this.globalSettings.fairMode + " (global)");
         } else {
            sender.sendMessage("§e disable: " + (ov.disable != null ? ov.disable : "§7" + this.globalSettings.disable + " (global)"));
            sender.sendMessage("§e fair-mode: " + (ov.fairMode != null ? ov.fairMode : "§7" + this.globalSettings.fairMode + " (global)"));
         }
      }
   }

   private void showSpecificSetting(CommandSender sender, String scope, String setting) {
      this.showSettings(sender, scope);
   }

   private void sendCommandUsage(CommandSender sender) {
      sender.sendMessage("§6XaeroUtils Usage:");
      sender.sendMessage("§e/xaero <scope> <setting> [value]");
      sender.sendMessage("§7Scopes: global, world_nether, etc.");
      sender.sendMessage("§7Settings: disable, fair-mode");
   }

   public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
      List<String> suggestions = new ArrayList<>();
      if (!sender.isOp() && !sender.hasPermission("xaero.edit")) {
         return suggestions;
      } else {
         if (cmd.getName().equalsIgnoreCase("xaero")) {
            if (args.length == 1) {
               suggestions.add("global");
               suggestions.add("reload");

               for (World col : Bukkit.getWorlds()) {
                  suggestions.add(col.getName());
               }
            } else if (args.length == 2) {
               String arg0 = args[0].toLowerCase();
               if (!arg0.equals("reload") && !arg0.startsWith("manual-")) {
                  suggestions.add("disable");
                  suggestions.add("fair-mode");
               }
            } else if (args.length == 3) {
               String arg0 = args[0].toLowerCase();
               if (!arg0.equals("reload") && !arg0.startsWith("manual-")) {
                  suggestions.add("true");
                  suggestions.add("false");
                  if (!arg0.equals("global")) {
                     suggestions.add("null");
                  }
               }
            }
         }

         Collections.sort(suggestions);
         return suggestions;
      }
   }

   private void sendTellrawAsync(String message) {
      CompletableFuture.runAsync(
         () -> Bukkit.getScheduler().runTask(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a \"" + message + "\""))
      );
   }

   private synchronized void handleReload(CommandSender sender) {
      if (this.isReloading) {
         sender.sendMessage("§cPlugin is already reloading!");
      } else {
         try {
            this.isReloading = true;
            this.reloadConfig();
            this.loadConfiguration();

            for (Player p : Bukkit.getOnlinePlayers()) {
               this.handleWorldAction(p, p.getWorld().getName());
            }

            sender.sendMessage("§aConfiguration reloaded successfully!");
            if (this.debugEnabled) {
               this.logger.info("Configuration reloaded");
            }
         } catch (Exception var7) {
            this.logger.severe("Error reloading configuration: " + var7.getMessage());
            sender.sendMessage("§cError reloading configuration!");
         } finally {
            this.isReloading = false;
         }
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Bukkit.getScheduler().runTaskLater(this, () -> {
         Player player = event.getPlayer();
         if (player.isOnline()) {
            this.handleWorldAction(player, player.getWorld().getName());
         }
      }, 20L);
   }

   @EventHandler
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      Bukkit.getScheduler().runTaskLater(this, () -> {
         Player player = event.getPlayer();
         if (player.isOnline()) {
            this.handleWorldAction(player, player.getWorld().getName());
         }
      }, 20L);
   }

   private void handleWorldAction(Player player, String worldName) {
      XaeroForceDisabler.Settings override = this.worldOverrides.get(worldName);
      boolean effectiveDisable = this.globalSettings.disable != null ? this.globalSettings.disable : false;
      boolean effectiveFair = this.globalSettings.fairMode != null ? this.globalSettings.fairMode : false;
      if (override != null) {
         if (override.disable != null) {
            effectiveDisable = override.disable;
         }

         if (override.fairMode != null) {
            effectiveFair = override.fairMode;
         }
      }

      if (this.debugEnabled) {
         this.logger.info("Applying settings for " + player.getName() + " in " + worldName + " | Disable: " + effectiveDisable + " | Fair: " + effectiveFair);
      }

      this.applyMode(player, effectiveDisable, effectiveFair);
   }

   private void applyMode(Player player, boolean disable, boolean fair) {
      if (disable && player.hasPermission("xaero.ignoredisable")) {
         if (this.debugEnabled) {
            this.logger.info("Player " + player.getName() + " has ignoredisable permission. Skipping disable packet.");
         }

         disable = false;
      }

      if (fair && player.hasPermission("xaero.ignorefair")) {
         if (this.debugEnabled) {
            this.logger.info("Player " + player.getName() + " has ignorefair permission. Skipping fair packet.");
         }

         fair = false;
      }

      boolean finalDisable = disable;
      boolean finalFair = fair;
      Bukkit.getScheduler().runTask(this, () -> {
         boolean isNether = player.getWorld().getEnvironment() == Environment.NETHER;
         boolean globalFair = this.globalSettings.fairMode != null && this.globalSettings.fairMode;
         if (!finalDisable && !finalFair) {
            if (isNether && globalFair) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§f§a§i§r§x§a§e§r§o\"");
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§x§a§e§r§o§m§m§n§e§t§h§e§r§i§s§f§a§i§r\"");
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r\"");
            } else {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§r§e§s§e§t§x§a§e§r§o\"");
            }
         } else {
            if (finalDisable) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§n§o§m§i§n§i§m§a§p\"");
            }

            if (finalFair) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " \"§f§a§i§r§x§a§e§r§o\"");
            }
         }
      });
   }

   private static final class Settings {
      Boolean fairMode;
      Boolean disable;

      Settings(Boolean fairMode, Boolean disable) {
         this.fairMode = fairMode;
         this.disable = disable;
      }
   }
}
