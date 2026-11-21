package me.chancesd.pvpmanager.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.listener.DebugEntityListener;
import me.chancesd.pvpmanager.manager.UpdateManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.command.ArgumentType;
import me.chancesd.pvpmanager.sdutils.command.BaseCommand;
import me.chancesd.pvpmanager.sdutils.command.CommandArgument;
import me.chancesd.pvpmanager.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Locale;
import me.chancesd.pvpmanager.setting.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

public class PM extends BaseCommand {
   private final PvPManager plugin;
   private DebugEntityListener damageListener;
   private final Help helpCommand;

   public PM(PluginCommand pluginCommand, PvPManager plugin) {
      super(pluginCommand);
      this.plugin = plugin;
      this.description("PvPManager administration commands").usage("/pmr [subcommand]").argument("page", ArgumentType.INTEGER).endArgument();
      this.subCommand("reload", new PM.ReloadCommand());
      this.subCommand("update", new PM.UpdateCommand());
      this.subCommand("cleanup", new PM.CleanupCommand());
      this.subCommand("convert", new PM.ConvertCommand());
      this.subCommand("debug", new PM.DebugCommand());
      this.subCommand("locale", new PM.LocaleCommand());
      this.subCommand("worlds", new Worlds(plugin));
      this.helpCommand = new Help();
   }

   @Override
   public void execute(CommandSender sender, String label, List<CommandArgument> args) {
      if (args.isEmpty()) {
         this.helpCommand.helpMenu(sender, 1);
      } else if (args.size() == 1) {
         int page = this.getArgument(args, "page").getAsInt();
         if (page > 0) {
            this.helpCommand.helpMenu(sender, page);
         }
      }
   }

   private void reload(boolean silent) {
      Log.setSilent(silent);
      this.plugin.setReloading(true);
      this.plugin.getUpdateManager().setUpdateAvailable(false);
      this.plugin.onDisable();
      HandlerList.unregisterAll(this.plugin);
      this.plugin.onPluginLoad();
      this.plugin.onEnable();
      this.plugin.setReloading(false);
      if (silent) {
         Log.setSilent(false);
      }
   }

   private class CleanupCommand extends BaseCommand {
      public CleanupCommand() {
         this.displayName("Cleanup")
            .description("Cleanup inactive users")
            .usage("/pmr cleanup <days>")
            .permission(Permissions.ADMIN.getPermission())
            .argument("days", ArgumentType.INTEGER)
            .endArgument();
      }

      @Override
      public void execute(final CommandSender sender, String label, List<CommandArgument> args) {
         if (args.isEmpty()) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555Usage: &#FFFFFF/pmr cleanup <days>"));
            sender.sendMessage(
               ChatUtils.colorize("&#999999This command will remove users from the database that haven't logged in during the last &#FFFF55x &#999999days.")
            );
            sender.sendMessage(
               ChatUtils.colorize(
                  "&cThis means that if they come back their remaining newbie protection(if they had any) and their previous PvP state will be default."
               )
            );
         } else {
            int daysValue = this.getArgument(args, "days").getAsInt();
            final long days = TimeUnit.DAYS.toMillis(daysValue);
            sender.sendMessage(ChatUtils.colorize("&#999999Cleaning users that haven't logged in the past &#FFFF55" + daysValue + " &#999999days"));
            sender.sendMessage(ChatUtils.colorize("&#999999This might take a while depending on the size of your database"));
            (new BukkitRunnable() {
               public void run() {
                  ArrayList<UUID> ids = new ArrayList<>();

                  for (Map<String, Object> userData : PM.this.plugin.getStorageManager().getStorage().getAllUserData()) {
                     String id = (String)userData.get("uuid");
                     UUID uuid = UUID.fromString(id);
                     OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                     if (!p.isOnline() && System.currentTimeMillis() - p.getLastPlayed() > days) {
                        ids.add(uuid);
                     }
                  }

                  ids.forEach(PM.this.plugin.getStorageManager().getStorage()::removeUserData);
                  sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#55FF55Finished. Cleaned up " + ids.size() + " inactive users."));
               }
            }).runTaskAsynchronously(PM.this.plugin);
         }
      }
   }

   private class ConvertCommand extends BaseCommand {
      public ConvertCommand() {
         this.displayName("Convert")
            .description("Convert database (SQLITE/MYSQL)")
            .usage("/pmr convert <databaseType>")
            .permission(Permissions.ADMIN.getPermission())
            .argument("databaseType", ArgumentType.STRING)
            .tabComplete("SQLITE", "MYSQL")
            .endArgument();
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (args.isEmpty()) {
            sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555&lUsage: &e/pmr convert <databaseType>"));
            sender.sendMessage(
               ChatUtils.colorize(
                  Lang.PREFIX
                     + " &cCurrently the database types are: &7"
                     + Arrays.<DatabaseConfigBuilder.DatabaseType>asList(DatabaseConfigBuilder.DatabaseType.values())
               )
            );
         } else {
            String dbType = args.get(0).getValue();

            DatabaseConfigBuilder.DatabaseType databaseType;
            try {
               databaseType = DatabaseConfigBuilder.DatabaseType.valueOf(dbType.toUpperCase());
            } catch (IllegalArgumentException var7) {
               sender.sendMessage(
                  ChatUtils.colorize(
                     Lang.PREFIX
                        + " &#FF5555Invalid database type. Available types are: &7"
                        + Arrays.<DatabaseConfigBuilder.DatabaseType>asList(DatabaseConfigBuilder.DatabaseType.values())
                  )
               );
               return;
            }

            DatabaseConfigBuilder.DatabaseType currentType = PM.this.plugin.getStorageManager().getStorage().getDatabaseType();
            if (currentType == databaseType) {
               sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555Can't convert. You are already running on " + databaseType));
            } else {
               Bukkit.getScheduler()
                  .runTaskAsynchronously(
                     PM.this.plugin,
                     () -> {
                        sender.sendMessage(ChatUtils.colorize("&#00AA00Starting database conversion from " + currentType + " to " + databaseType));

                        try {
                           PM.this.plugin.getStorageManager().convertFromCurrent(databaseType, sender, System.currentTimeMillis());
                        } catch (Exception var5x) {
                           sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#FF5555Error! Make sure you entered the correct MySQL details in the config"));
                           return;
                        }

                        PM.this.plugin.getConfig().set("Database.Type", databaseType.toString());
                        PM.this.plugin.saveConfig();
                        PM.this.reload(false);
                        sender.sendMessage(
                           ChatUtils.colorize(
                              Lang.PREFIX + " &#00AA00You are now running on " + PM.this.plugin.getStorageManager().getStorage().getDatabaseType()
                           )
                        );
                     }
                  );
            }
         }
      }
   }

   private class DebugCommand extends BaseCommand {
      public DebugCommand() {
         this.displayName("Debug")
            .description("Debug utilities")
            .usage("/pmr debug <subcommand> [player]")
            .permission(Permissions.ADMIN.getPermission())
            .argument("subcommand", ArgumentType.STRING)
            .tabComplete("toggle", "damagedebug", "attack", "players")
            .endArgument()
            .argument("player", ArgumentType.PLAYER)
            .endArgument();
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (args.isEmpty()) {
            sender.sendMessage(ChatUtils.colorize("&#FF5555&lUsage: &#FFFFFF/pmr debug <subcommand>"));
            sender.sendMessage(ChatUtils.colorize("&cAvailable subcommands: &7toggle, damagedebug, attack, players"));
         } else {
            String subcommand = this.getArgument(args, "subcommand").getValue().toLowerCase();
            CommandArgument argument = this.getArgument(args, "player");
            CombatPlayer targetPlayer = null;
            if (argument != null) {
               targetPlayer = PM.this.plugin.getPlayerManager().get(argument.getAsPlayer());
            } else if (sender instanceof Player commandSender) {
               targetPlayer = PM.this.plugin.getPlayerManager().get(commandSender);
            }

            switch (subcommand) {
               case "toggle":
                  this.toggleDebugMode(sender);
                  break;
               case "damagedebug":
                  this.toggleDamageDebug(sender);
                  break;
               case "players":
                  this.listPlayers(sender);
                  break;
               case "attack":
                  if (targetPlayer != null) {
                     this.attackPlayer(sender, targetPlayer);
                  } else {
                     ChatUtils.send(sender, "&#FF5555Player not specified");
                  }
                  break;
               default:
                  ChatUtils.send(sender, "&#FF5555Invalid subcommand. Use &7/pmr debug <toggle|damagedebug|players|attack> [player]");
            }
         }
      }

      private void toggleDebugMode(CommandSender sender) {
         Conf.setDebug(!Conf.DEBUG_MODE.asBool());
         Log.info("Debug mode: " + Conf.DEBUG_MODE.asBool());
         ChatUtils.send(sender, "Debug mode: " + Conf.DEBUG_MODE.asBool());
      }

      private void toggleDamageDebug(CommandSender sender) {
         if (PM.this.damageListener == null) {
            ChatUtils.send(sender, "&#AA0000Warning &#FFFFFF- Some plugin features are disabled while in this mode");
            ChatUtils.send(sender, "Enabling a damage listener for debugging, check the console for details on every entity hit");
            ChatUtils.send(sender, "&#FF5555Run this command again &#FFFFFFafter you are done to disable debugging or reload the plugin");
            Conf.setDebug(true);
            PM.this.damageListener = new DebugEntityListener(PM.this.plugin.getPlayerManager());
            HandlerList.unregisterAll(PM.this.plugin.getEntityListener());
            Bukkit.getServer().getPluginManager().registerEvents(PM.this.damageListener, PM.this.plugin);
         } else {
            HandlerList.unregisterAll(PM.this.damageListener);
            Bukkit.getServer().getPluginManager().registerEvents(PM.this.plugin.getEntityListener(), PM.this.plugin);
            PM.this.damageListener = null;
            Conf.setDebug(false);
            sender.sendMessage("Debug damage listener disabled");
         }
      }

      private void listPlayers(CommandSender sender) {
         for (CombatPlayer player : PM.this.plugin.getPlayerManager().getPlayers().values()) {
            if (!Bukkit.getOnlinePlayers().contains(player.getPlayer())) {
               ChatUtils.send(sender, "UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
               Log.info("UUID: " + player.getUUID() + " - Name: " + player.getName() + " - Metadata: " + player.getPlayer().hasMetadata("NPC"));
            }
         }

         ChatUtils.send(sender, "&aPlayers: &c" + PM.this.plugin.getPlayerManager().getPlayers().size() + "&a/" + Bukkit.getOnlinePlayers().size());
         Log.info("Players: " + PM.this.plugin.getPlayerManager().getPlayers().size() + "/" + Bukkit.getOnlinePlayers().size());
      }

      private void attackPlayer(CommandSender sender, CombatPlayer targetPlayer) {
         targetPlayer.getPlayer().damage(5.0);
         ChatUtils.send(sender, "Attacked player with 5 damage");
      }
   }

   private class LocaleCommand extends BaseCommand {
      public LocaleCommand() {
         this.displayName("Locale")
            .description("Change plugin language")
            .usage("/pmr locale [language]")
            .permission(Permissions.ADMIN.getPermission())
            .argument("language", ArgumentType.STRING)
            .tabComplete(Locale.asStringList())
            .endArgument();
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (args.isEmpty()) {
            ChatUtils.send(sender, Lang.PREFIX + " &7Your current Locale is: &#FF5555" + Lang.getLocale());
            ChatUtils.send(sender, Lang.PREFIX + " &7Available languages are: &#FF5555" + Locale.asStringList());
         } else {
            Locale locale;
            try {
               locale = Locale.valueOf(args.get(0).getValue().toUpperCase());
            } catch (IllegalArgumentException var6) {
               ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Invalid Locale. &7Available languages are: &e" + Locale.asStringList());
               return;
            }

            if (Lang.getLocale() == locale) {
               ChatUtils.send(sender, Lang.PREFIX + " &#FF5555Can't change Locale. You are already using " + locale);
            } else {
               Conf.LOCALE.set(locale.name());
               this.changeConfigSetting("General Settings.Locale", locale.name());
               Lang.setup(PM.this.plugin);
               ChatUtils.send(sender, Lang.PREFIX + " &aLanguage changed to &e" + Lang.getLocale() + "&a - &7Filename: " + Lang.getLocale().fileName());
            }
         }
      }

      private void changeConfigSetting(String path, String value) {
         PM.this.plugin.reloadConfig();
         PM.this.plugin.getConfig().set(path, value);
         PM.this.plugin.saveConfig();
      }
   }

   private class ReloadCommand extends BaseCommand {
      public ReloadCommand() {
         this.displayName("Reload").description("Reload PvPManager").usage("/pmr reload").permission(Permissions.ADMIN.getPermission());
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         PM.this.reload(false);
         sender.sendMessage(ChatUtils.colorize(Lang.PREFIX + " &#55FF55PvPManager reloaded!"));
      }
   }

   private class UpdateCommand extends BaseCommand {
      public UpdateCommand() {
         this.displayName("Update").description("Update to latest version").usage("/pmr update").permission(Permissions.ADMIN.getPermission());
      }

      @Override
      public void execute(CommandSender sender, String label, List<CommandArgument> args) {
         if (Conf.CHECK_UPDATES.asBool()) {
            UpdateManager updateManager = PM.this.plugin.getUpdateManager();
            if (updateManager.hasUpdateAvailable()) {
               if (updateManager.getUpdater().downloadFile()) {
                  sender.sendMessage(ChatUtils.colorize("&#00AA00Update Successful. On next restart you will have &#FFFF55" + updateManager.getNewVersion()));
               } else {
                  sender.sendMessage(ChatUtils.colorize("&#FF5555An error ocurred while updating, please report to the developer"));
               }
            } else {
               sender.sendMessage(ChatUtils.colorize("&#00AA00You have the latest version: &#FFFF55PvPManager v" + updateManager.getCurrentversion()));
            }
         } else {
            sender.sendMessage(ChatUtils.colorize("&#FF5555Update Checking is disabled, enable it in the Config file"));
         }
      }
   }
}
