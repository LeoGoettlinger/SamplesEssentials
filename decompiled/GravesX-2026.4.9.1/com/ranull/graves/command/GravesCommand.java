package com.ranull.graves.command;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.HologramData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.compatibility.CompatibilityTeleport;
import dev.cwhead.GravesX.util.PluginDownloadUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class GravesCommand implements CommandExecutor, TabCompleter {
   private final Graves plugin;
   private final Set<UUID> pendingImports = new HashSet<>();
   private final Set<UUID> pendingHologramMigrations = new HashSet<>();
   private boolean consolePendingImport = false;
   private boolean consolePendingHologramMigration = false;

   public GravesCommand(Graves plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, String[] args) {
      if (args.length < 1) {
         if (commandSender instanceof Player player) {
            if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", player.getPlayer())) {
               this.plugin.getGUIManager().openGraveList(player);
            } else {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", player.getPlayer());
            }
         } else {
            this.sendHelpMenu(commandSender);
         }
      } else {
         String var7 = args[0].toLowerCase(Locale.ROOT);
         switch (var7) {
            case "list":
            case "gui":
               this.handleListGuiCommand(commandSender, args);
               break;
            case "teleport":
            case "tp":
               this.handleTeleportCommand(commandSender, args);
               break;
            case "virtual":
            case "open":
               this.handleVirtualCommand(commandSender, args);
               break;
            case "givetoken":
               this.handleGiveTokenCommand(commandSender, args);
               break;
            case "reload":
               this.handleReloadCommand(commandSender);
               break;
            case "dump":
               this.handleDumpCommand(commandSender);
               break;
            case "debug":
               this.handleDebugCommand(commandSender, args);
               break;
            case "cleanup":
               this.handleCleanupCommand(commandSender);
               break;
            case "purge":
               this.handlePurgeCommand(commandSender, args);
               break;
            case "import":
               this.handleImportCommand(commandSender, args);
               break;
            case "migration":
            case "migrate":
               this.handleMigrationCommand(commandSender, args);
               break;
            case "addons":
            case "addon":
               this.handleAddonCommand(commandSender, args);
               break;
            case "count":
               this.handleCountCommand(commandSender, args);
               break;
            case "help":
               this.sendHelpMenu(commandSender);
               break;
            default:
               this.sendHelpMenu(commandSender);
         }
      }

      return true;
   }

   public void sendHelpMenu(CommandSender sender) {
      sender.sendMessage(
         ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RED + this.plugin.getName() + " " + ChatColor.DARK_GRAY + "v" + this.plugin.getVersion()
      );
      if (sender instanceof Player player) {
         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Graves GUI");
         } else {
            sender.sendMessage(ChatColor.RED + "/graves " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Main Command");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves list " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Player graves");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui.other", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves list {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " View player graves");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves teleport " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Teleport to your latest grave");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.others", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves teleport {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Teleport to latest player grave");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves virtual " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Open your latest grave virtually");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command.others", player.getPlayer())) {
            sender.sendMessage(
               ChatColor.RED + "/graves virtual {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Open latest player grave virtually"
            );
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.givetoken", player.getPlayer())
            && this.plugin.getConfig().getBoolean("settings.token")) {
            sender.sendMessage(ChatColor.RED + "/graves givetoken {player} {amount} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Give grave token");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.reload", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves reload " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Reload plugin");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.dump", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves dump " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Dump server information");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.debug", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves debug {level} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Change debug level");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.import", player.getPlayer())) {
            sender.sendMessage(
               ChatColor.RED + "/graves import {plugin} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Imports grave data from another grave plugin"
            );
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.migration", player.getPlayer())) {
            sender.sendMessage(
               ChatColor.RED
                  + "/graves migration holograms "
                  + ChatColor.DARK_GRAY
                  + "-"
                  + ChatColor.RESET
                  + " Migrate ArmorStand holograms to TextDisplay holograms"
            );
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.purge", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves purge {type} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Purges based on type");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.download.addons", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves addon {addon} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Downloads addon (Restart Required)");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.cleanup", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves cleanup " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Purges all graves");
         }

         if (this.plugin.getPermissionManager().hasGrantedPermission("graves.count", player.getPlayer())) {
            sender.sendMessage(ChatColor.RED + "/graves count world {world} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Count graves in a world");
            sender.sendMessage(
               ChatColor.RED + "/graves count player {player} {world} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Count player graves in a world"
            );
         }
      } else {
         sender.sendMessage(ChatColor.RED + "/graves " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Main Command");
         sender.sendMessage(ChatColor.RED + "/graves list {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " View player graves");
         sender.sendMessage(ChatColor.RED + "/graves teleport {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Teleport to latest grave");
         sender.sendMessage(ChatColor.RED + "/graves virtual {player} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Open latest grave virtually");
         if (this.plugin.getConfig().getBoolean("settings.token")) {
            sender.sendMessage(ChatColor.RED + "/graves givetoken {player} {amount} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Give grave token");
         }

         sender.sendMessage(ChatColor.RED + "/graves reload " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Reload plugin");
         sender.sendMessage(ChatColor.RED + "/graves dump " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Dump server information");
         sender.sendMessage(ChatColor.RED + "/graves debug {level} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Change debug level");
         sender.sendMessage(
            ChatColor.RED + "/graves import {plugin} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Imports grave data from another grave plugin"
         );
         sender.sendMessage(
            ChatColor.RED
               + "/graves migration holograms "
               + ChatColor.DARK_GRAY
               + "-"
               + ChatColor.RESET
               + " Migrate ArmorStand holograms to TextDisplay holograms"
         );
         sender.sendMessage(ChatColor.RED + "/graves purge {type} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Purges based on type");
         sender.sendMessage(ChatColor.RED + "/graves addon {addon} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Downloads addon (Restart Required)");
         sender.sendMessage(ChatColor.RED + "/graves cleanup " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Purges all graves");
         sender.sendMessage(ChatColor.RED + "/graves count world {world} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Count graves in a world");
         sender.sendMessage(
            ChatColor.RED + "/graves count player {player} {world} " + ChatColor.DARK_GRAY + "-" + ChatColor.RESET + " Count player graves in a world"
         );
      }

      sender.sendMessage(ChatColor.DARK_GRAY + "Author: " + ChatColor.RED + "Ranull");
      sender.sendMessage(ChatColor.DARK_GRAY + "Maintainers: " + ChatColor.RED + "JaySmethers and Legoman99573");
   }

   public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String string, @NotNull String[] args) {
      List<String> stringList = new ArrayList<>();
      if (args.length == 1) {
         stringList.add("help");
         if (commandSender instanceof Player player && this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command", player.getPlayer())) {
            stringList.add("teleport");
            stringList.add("tp");
            stringList.add("tpoffline");
            stringList.add("teleportoffline");
         }

         if (commandSender instanceof Player player && this.plugin.getPermissionManager().hasGrantedPermission("graves.import", player.getPlayer())) {
            stringList.add("import");
         }

         if (!(commandSender instanceof Player)
            || this.plugin.getPermissionManager().hasGrantedPermission("graves.migration", ((Player)commandSender).getPlayer())) {
            stringList.add("migration");
            stringList.add("migrate");
         }

         if (!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", ((Player)commandSender).getPlayer())) {
            stringList.add("list");
            stringList.add("gui");
         }

         if (!(commandSender instanceof Player)
            || this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command", ((Player)commandSender).getPlayer())) {
            stringList.add("virtual");
            stringList.add("open");
         }

         if (!(commandSender instanceof Player)
            || this.plugin.getPermissionManager().hasGrantedPermission("graves.reload", ((Player)commandSender).getPlayer())) {
            stringList.add("reload");
         }

         if (!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.dump", ((Player)commandSender).getPlayer())) {
            stringList.add("dump");
         }

         if (!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.debug", ((Player)commandSender).getPlayer())
            )
          {
            stringList.add("debug");
         }

         if (!(commandSender instanceof Player)
            || this.plugin.getPermissionManager().hasGrantedPermission("graves.cleanup", ((Player)commandSender).getPlayer())) {
            stringList.add("cleanup");
         }

         if (!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.purge", ((Player)commandSender).getPlayer())
            )
          {
            stringList.add("purge");
         }

         if (!(commandSender instanceof Player)
            || this.plugin.getPermissionManager().hasGrantedPermission("graves.download.addons", ((Player)commandSender).getPlayer())) {
            stringList.add("addon");
            stringList.add("addons");
         }

         if (!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.count", ((Player)commandSender).getPlayer())
            )
          {
            stringList.add("count");
         }

         if (this.plugin.getRecipeManager() != null
            && (
               !(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.givetoken", ((Player)commandSender).getPlayer())
            )) {
            stringList.add("givetoken");
         }
      } else if (args.length > 1) {
         String sub = args[0].toLowerCase(Locale.ROOT);
         switch (sub) {
            case "list":
            case "gui":
               if ((!(commandSender instanceof Player) || this.plugin.getPermissionManager().hasGrantedPermission("graves.gui.other", (Player)commandSender))
                  && args.length == 2) {
                  String partialInput = args[1].trim().toLowerCase(Locale.ROOT);
                  if (partialInput.length() < 3) {
                     stringList.add("3 characters required");
                  } else {
                     for (OfflinePlayer offlinePlayerxxxx : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayerxxxx.hasPlayedBefore() && offlinePlayerxxxx.getName() != null) {
                           if (stringList.size() >= 25) {
                              break;
                           }

                           String playerName = offlinePlayerxxxx.getName();
                           if (playerName.toLowerCase(Locale.ROOT).contains(partialInput)) {
                              stringList.add(playerName);
                           }
                        }
                     }
                  }
               }
               break;
            case "teleport":
            case "tp":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.others", (Player)commandSender)) {
                  this.plugin.getServer().getOnlinePlayers().forEach(player -> stringList.add(player.getName()));
               }
               break;
            case "tpoffline":
            case "teleportoffline":
               if ((
                     !(commandSender instanceof Player)
                        || this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.others", (Player)commandSender)
                  )
                  && args.length == 2) {
                  String partialInput = args[1].trim().toLowerCase(Locale.ROOT);
                  if (partialInput.length() < 3) {
                     stringList.add("3 characters required");
                  } else {
                     for (OfflinePlayer offlinePlayerxxx : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayerxxx.hasPlayedBefore() && offlinePlayerxxx.getName() != null) {
                           if (stringList.size() >= 25) {
                              break;
                           }

                           String playerName = offlinePlayerxxx.getName();
                           if (playerName.toLowerCase(Locale.ROOT).contains(partialInput)) {
                              stringList.add(playerName);
                           }
                        }
                     }
                  }
               }
               break;
            case "virtual":
            case "open":
               if ((
                     !(commandSender instanceof Player)
                        || this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command.others", (Player)commandSender)
                  )
                  && args.length == 2) {
                  String partialInput = args[1].trim().toLowerCase(Locale.ROOT);
                  if (partialInput.length() < 3) {
                     stringList.add("3 characters required");
                  } else {
                     for (OfflinePlayer offlinePlayerxx : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayerxx.hasPlayedBefore() && offlinePlayerxx.getName() != null) {
                           if (stringList.size() >= 25) {
                              break;
                           }

                           String playerName = offlinePlayerxx.getName();
                           if (playerName.toLowerCase(Locale.ROOT).contains(partialInput)) {
                              stringList.add(playerName);
                           }
                        }
                     }
                  }
               }
               break;
            case "debug":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.debug", ((Player)commandSender).getPlayer())) {
                  stringList.add("0");
                  stringList.add("1");
                  stringList.add("2");
               }
               break;
            case "import":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.import", ((Player)commandSender).getPlayer())) {
                  stringList.add("AngelChest");
                  stringList.add("angelchest");
                  stringList.add("confirm");
               }
               break;
            case "migration":
            case "migrate":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.migration", ((Player)commandSender).getPlayer())) {
                  if (args.length == 2) {
                     stringList.add("holograms");
                     stringList.add("hologram");
                  } else if (args.length == 3 && (args[1].equalsIgnoreCase("holograms") || args[1].equalsIgnoreCase("hologram"))) {
                     stringList.add("confirm");
                  }
               }
               break;
            case "addon":
            case "addons":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.download.addon", ((Player)commandSender).getPlayer())) {
                  stringList.add("LandProtection");
               }
               break;
            case "purge":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.debug", ((Player)commandSender).getPlayer())) {
                  if (args.length == 2) {
                     stringList.add("graves");
                     stringList.add("grave");
                     stringList.add("player");
                     stringList.add("offline-player");
                     stringList.add("holograms");
                     stringList.add("hologram");
                     stringList.add("grave-specific");
                     stringList.add("grave-uuid");
                     stringList.add("abandon");
                     stringList.add("abandoned");
                  }

                  if (args.length == 3 && args[1].equals("offline-player") || args.length == 3 && args[1].equals("player")) {
                     String partialInput = args[2].trim().toLowerCase(Locale.ROOT);
                     if (partialInput.length() < 3) {
                        stringList.add("<3 characters required>");
                        return stringList;
                     }

                     for (OfflinePlayer offlinePlayerx : Bukkit.getOfflinePlayers()) {
                        if (offlinePlayerx.hasPlayedBefore() && offlinePlayerx.getName() != null) {
                           if (stringList.size() >= 25) {
                              break;
                           }

                           String playerName = offlinePlayerx.getName();
                           if (playerName.toLowerCase(Locale.ROOT).contains(partialInput)) {
                              stringList.add(playerName);
                           }
                        }
                     }
                  }

                  if (args.length == 3 && args[1].equals("grave-specific") || args.length == 3 && args[1].equals("grave-uuid")) {
                     String partialInput = args[2];

                     for (Grave grave : new ArrayList<>(this.plugin.getCacheManager().getGraveMap().values())) {
                        String uuid = grave.getUUID().toString();
                        if (uuid.startsWith(partialInput)) {
                           stringList.add(grave.getUUID().toString());
                        }
                     }
                  }
               }
               break;
            case "givetoken":
               if (this.plugin.getRecipeManager() != null
                  && (
                     !(commandSender instanceof Player)
                        || this.plugin.getPermissionManager().hasGrantedPermission("graves.givetoken", ((Player)commandSender).getPlayer())
                  )) {
                  if (args.length == 2) {
                     this.plugin.getServer().getOnlinePlayers().forEach(player -> stringList.add(player.getName()));
                  } else if (args.length == 3) {
                     stringList.addAll(this.plugin.getRecipeManager().getTokenList());
                  }
               }
               break;
            case "count":
               if (!(commandSender instanceof Player)
                  || this.plugin.getPermissionManager().hasGrantedPermission("graves.count", ((Player)commandSender).getPlayer())) {
                  if (args.length == 2) {
                     stringList.add("world");
                     stringList.add("player");
                  } else if (args.length == 3) {
                     if (args[1].equalsIgnoreCase("world")) {
                        String partialInput = args[2].toLowerCase(Locale.ROOT);

                        for (World world : Bukkit.getWorlds()) {
                           String worldName = world.getName();
                           if (worldName.toLowerCase(Locale.ROOT).startsWith(partialInput)) {
                              stringList.add(worldName);
                           }
                        }
                     } else if (args[1].equalsIgnoreCase("player")) {
                        String partialInput = args[2].trim().toLowerCase(Locale.ROOT);
                        if (partialInput.length() < 3) {
                           stringList.add("<3 characters required>");
                        } else {
                           for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                              if (offlinePlayer.hasPlayedBefore() && offlinePlayer.getName() != null) {
                                 if (stringList.size() >= 25) {
                                    break;
                                 }

                                 String playerName = offlinePlayer.getName();
                                 if (playerName.toLowerCase(Locale.ROOT).contains(partialInput)) {
                                    stringList.add(playerName);
                                 }
                              }
                           }
                        }
                     }
                  } else if (args.length == 4 && args[1].equalsIgnoreCase("player")) {
                     String partialInput = args[3].toLowerCase(Locale.ROOT);

                     for (World worldx : Bukkit.getWorlds()) {
                        String worldName = worldx.getName();
                        if (worldName.toLowerCase(Locale.ROOT).startsWith(partialInput)) {
                           stringList.add(worldName);
                        }
                     }
                  }
               }
         }
      }

      return stringList;
   }

   private void handleListGuiCommand(CommandSender commandSender, String[] args) {
      if (commandSender instanceof Player player) {
         if (args.length == 1) {
            if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui", player.getPlayer())) {
               this.plugin.getGUIManager().openGraveList(player);
            } else {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", player.getPlayer());
            }
         } else if (args.length == 2) {
            if (this.plugin.getPermissionManager().hasGrantedPermission("graves.gui.other", player.getPlayer())) {
               OfflinePlayer otherPlayer = this.plugin.getServer().getOfflinePlayer(args[1]);
               if (!this.plugin.getGraveManager().getGraveList(otherPlayer).isEmpty()) {
                  this.plugin.getGUIManager().openGraveList(player, otherPlayer.getUniqueId());
               } else {
                  commandSender.sendMessage(
                     ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RED + args[1] + ChatColor.RESET + " has no graves."
                  );
               }
            } else {
               this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
            }
         }
      } else {
         this.sendHelpMenu(commandSender);
      }
   }

   private void handleAddonCommand(CommandSender commandSender, String[] args) {
      if (args.length == 2) {
         if (commandSender instanceof Player player && this.plugin.getPermissionManager().hasGrantedPermission("graves.download.addons", player.getPlayer())) {
            String var8 = args[1].toUpperCase(Locale.ROOT);
            byte var5 = -1;
            switch (var8.hashCode()) {
               case 981034756:
                  if (var8.equals("LANDPROTECTION")) {
                     var5 = 0;
                  }
               default:
                  switch (var5) {
                     case 0:
                        try {
                           PluginDownloadUtil.downloadAndReplacePlugin(120633L, "GravesXAddon-LandProtection", "plugins", commandSender);
                        } catch (IOException var7) {
                           commandSender.sendMessage(
                              ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "An error occurred while running command. Check console."
                           );
                           this.plugin.getLogger().warning("An issue occurred while running this command.");
                           this.plugin.logStackTrace((Exception)var7);
                        }
                        break;
                     default:
                        commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Must enter a valid addon.");
                  }
            }
         } else if (commandSender instanceof Player player) {
            this.plugin.getEntityManager().sendMessage("message.permission-denied", player.getPlayer());
         }
      } else {
         this.sendHelpMenu(commandSender);
      }
   }

   private void handleVirtualCommand(CommandSender commandSender, String[] args) {
      if (!(commandSender instanceof Player player)) {
         this.sendHelpMenu(commandSender);
      } else if (args.length == 1 || args.length == 2 && args[1].equalsIgnoreCase(player.getName())) {
         if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command", player)) {
            this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
         } else {
            List<Grave> graves = this.plugin.getGraveManager().getGraveList(player);
            if (graves != null && !graves.isEmpty()) {
               Grave grave = graves.get(0);
               if (grave == null) {
                  player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "You have no graves.");
               } else {
                  Location deathLoc = grave.getLocationDeath();
                  if (deathLoc != null && deathLoc.getWorld() != null) {
                     this.plugin.getGraveManager().openGrave(player, grave.getLocationDeath(), grave);
                  } else {
                     player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Grave location is unavailable.");
                  }
               }
            } else {
               player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "You have no graves.");
            }
         }
      } else if (args.length != 2 || args[1].equalsIgnoreCase(player.getName())) {
         this.sendHelpMenu(commandSender);
      } else if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.virtual.command.others", player)) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
      } else {
         OfflinePlayer targetPl = this.plugin.getServer().getOfflinePlayer(args[1]);
         List<Grave> graves = this.plugin.getGraveManager().getGraveList(targetPl);
         if (graves != null && !graves.isEmpty()) {
            Grave grave = graves.get(0);
            if (grave == null) {
               player.sendMessage(
                  ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RED + args[1] + ChatColor.RESET + " has no graves."
               );
            } else {
               Location base = grave.getLocationDeath();
               if (base != null && base.getWorld() != null) {
                  this.plugin.getGraveManager().openGrave(player, grave.getLocationDeath(), grave);
               } else {
                  player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Grave location is unavailable.");
               }
            }
         } else {
            player.sendMessage(
               ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RED + args[1] + ChatColor.RESET + " has no graves."
            );
         }
      }
   }

   private void handleTeleportCommand(CommandSender commandSender, String[] args) {
      if (!(commandSender instanceof Player player)) {
         this.sendHelpMenu(commandSender);
      } else if (args.length == 1 || args.length == 2 && args[1].equalsIgnoreCase(player.getName())) {
         if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command", player)) {
            this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
         } else {
            List<Grave> graves = this.plugin.getGraveManager().getGraveList(player);
            if (graves.isEmpty()) {
               player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "You have no graves.");
            } else {
               Grave grave = graves.get(0);
               Location deathLoc = grave.getLocationDeath();
               if (deathLoc != null && deathLoc.getWorld() != null) {
                  if (this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.free", player)) {
                     CompatibilityTeleport.teleportSafely(player, deathLoc, this.plugin).thenAccept(ok -> {
                        if (!ok) {
                           player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Teleport failed.");
                        }
                     });
                  } else {
                     Location target = this.plugin.getGraveManager().getGraveLocationList(player.getLocation(), grave).get(0);
                     this.plugin.getEntityManager().teleportEntity(player, target, grave);
                  }
               } else {
                  player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Grave location is unavailable.");
               }
            }
         }
      } else if (args.length != 2 || args[1].equalsIgnoreCase(player.getName())) {
         this.sendHelpMenu(commandSender);
      } else if (!this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.others", player)) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
      } else {
         OfflinePlayer targetPl = this.plugin.getServer().getOfflinePlayer(args[1]);
         List<Grave> graves = this.plugin.getGraveManager().getGraveList(targetPl);
         if (graves.isEmpty()) {
            player.sendMessage(
               ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RED + args[1] + ChatColor.RESET + " has no graves."
            );
         } else {
            Grave grave = graves.get(0);
            Location base = grave.getLocationDeath();
            if (base != null && base.getWorld() != null) {
               if (this.plugin.getPermissionManager().hasGrantedPermission("graves.teleport.command.others.free", player)) {
                  Location target = this.plugin.getGraveManager().getGraveLocation(base.clone().add(1.0, 0.0, 1.0), grave);
                  CompatibilityTeleport.teleportSafely(player, target, this.plugin).thenAccept(ok -> {
                     if (!ok) {
                        player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Teleport failed.");
                     }
                  });
               } else {
                  Location target = this.plugin.getGraveManager().getGraveLocationList(player.getLocation(), grave).get(0);
                  this.plugin.getEntityManager().teleportEntity(player, target, grave);
               }
            } else {
               player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Grave location is unavailable.");
            }
         }
      }
   }

   private void handleGiveTokenCommand(CommandSender commandSender, String[] args) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.givetoken", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else if (args.length == 1) {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RESET + "/graves givetoken {player} {token}");
      } else if (args.length == 2) {
         if (commandSender instanceof Player player) {
            ItemStack itemStack = this.plugin.getRecipeManager().getToken(args[1].toLowerCase(Locale.ROOT));
            if (itemStack != null) {
               this.plugin.getEntityManager().sendMessage("message.give-token", player);
               player.getInventory().addItem(new ItemStack[]{itemStack});
            } else {
               player.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Token " + args[1] + " not found.");
            }
         } else {
            commandSender.sendMessage("Only players can run this command, try /graves givetoken {name} {token}");
         }
      } else if (args.length == 3) {
         Player playerx = this.plugin.getServer().getPlayer(args[1]);
         if (playerx != null) {
            ItemStack itemStack = this.plugin.getRecipeManager().getToken(args[2].toLowerCase(Locale.ROOT));
            if (itemStack != null) {
               this.plugin.getEntityManager().sendMessage("message.give-token", playerx);
               playerx.getInventory().addItem(new ItemStack[]{itemStack});
            } else {
               playerx.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Token " + args[2] + " not found.");
            }
         } else {
            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Player " + args[1] + " not found.");
         }
      } else if (args.length == 4) {
         Player playerx = this.plugin.getServer().getPlayer(args[1]);
         if (playerx != null) {
            ItemStack itemStack = this.plugin.getRecipeManager().getToken(args[2].toLowerCase(Locale.ROOT));
            if (itemStack != null) {
               try {
                  int amount = Integer.parseInt(args[3]);

                  for (int count = 0; count < amount; count++) {
                     playerx.getInventory().addItem(new ItemStack[]{itemStack});
                  }
               } catch (NumberFormatException var7) {
                  playerx.getInventory().addItem(new ItemStack[]{itemStack});
               }

               this.plugin.getEntityManager().sendMessage("message.give-token", playerx);
            } else {
               playerx.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Token " + args[2] + " not found.");
            }
         } else {
            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Player " + args[1] + " not found.");
         }
      }
   }

   private void handleReloadCommand(CommandSender commandSender) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.reload", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else {
         Plugin skriptPlugin = this.plugin.getServer().getPluginManager().getPlugin("Skript");
         if (skriptPlugin != null && skriptPlugin.isEnabled()) {
            this.plugin
               .getLogger()
               .warning("Skript v." + skriptPlugin.getDescription().getVersion() + " detected. Skript Integration option will only take effect on restart.");
         }

         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Reloading " + this.plugin.getName() + "...");
         long start = System.nanoTime();
         this.plugin.reload();
         long tookMs = (System.nanoTime() - start) / 1000000L;
         commandSender.sendMessage(
            ChatColor.RED
               + "☠"
               + ChatColor.DARK_GRAY
               + " » "
               + ChatColor.RESET
               + this.plugin.getName()
               + " reloaded. "
               + ChatColor.DARK_GRAY
               + "("
               + tookMs
               + "ms)"
         );
      }
   }

   private void handleDumpCommand(CommandSender commandSender) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.dump", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Running dump functions...");
         this.plugin.dumpServerInfo(commandSender);
      }
   }

   private void handleDebugCommand(CommandSender commandSender, String[] args) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.debug", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else if (args.length > 1) {
         try {
            this.plugin.getConfig().set("settings.debug.level", Integer.parseInt(args[1]));
            if (commandSender instanceof Player player) {
               List<String> stringList = this.plugin.getConfig().getStringList("settings.debug.user");
               stringList.add(player.getUniqueId().toString());
               commandSender.sendMessage(
                  ChatColor.RED
                     + "☠"
                     + ChatColor.DARK_GRAY
                     + " » "
                     + ChatColor.RESET
                     + ChatColor.RESET
                     + "Debug level changed to: ("
                     + args[1]
                     + "), User ("
                     + player.getName()
                     + ") added, This won't persist across restarts or reloads."
               );
            } else {
               this.plugin.getLogger().info("Debug level changed to: (" + args[1] + "), This won't persist across restarts or reloads.");
            }
         } catch (NumberFormatException var5) {
            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RESET + args[1] + " is not a valid int.");
         }
      } else {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + ChatColor.RESET + "/graves debug {level}");
      }
   }

   private void handleCleanupCommand(CommandSender commandSender) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.cleanup", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else {
         List<Grave> graveList = new ArrayList<>(this.plugin.getCacheManager().getGraveMap().values());

         for (Grave grave : graveList) {
            this.plugin.getGraveManager().removeGrave(grave);
         }

         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + graveList.size() + " graves cleaned up.");
      }
   }

   private void handlePurgeCommand(CommandSender commandSender, String[] args) {
      if (commandSender instanceof Player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.purge", ((Player)commandSender).getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", (Player)commandSender);
      } else {
         if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Please specify a subcommand.");
            return;
         }

         String subcommand = args[1].toLowerCase(Locale.ROOT);
         switch (subcommand) {
            case "holograms":
            case "hologram":
               int count = 0;

               for (World world : this.plugin.getServer().getWorlds()) {
                  for (Entity entity : world.getEntities()) {
                     if (entity.getScoreboardTags().contains("graveHologram")) {
                        entity.remove();
                        count++;
                     }
                  }
               }

               commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + count + " holograms purged.");
               break;
            case "player":
            case "offline-player":
               if (args.length < 3) {
                  commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Please specify an offline player's name.");
                  return;
               }

               String targetOfflinePlayerName = args[2];
               OfflinePlayer targetOfflinePlayer = Bukkit.getOfflinePlayer(targetOfflinePlayerName);
               if (!targetOfflinePlayer.hasPlayedBefore()) {
                  commandSender.sendMessage(
                     ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Player not found or has never played on this server."
                  );
                  return;
               }

               UUID offlinePlayerUUID = targetOfflinePlayer.getUniqueId();
               boolean offlineGraveFound = false;

               for (Grave grave : this.plugin.getCacheManager().getGraveMap().values()) {
                  if (grave.getOwnerUUID().equals(offlinePlayerUUID)) {
                     this.plugin.getGraveManager().removeGrave(grave);
                     offlineGraveFound = true;
                  }
               }

               if (offlineGraveFound) {
                  commandSender.sendMessage(
                     ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Graves of offline player " + targetOfflinePlayerName + " purged."
                  );
               } else {
                  commandSender.sendMessage(
                     ChatColor.RED
                        + "☠"
                        + ChatColor.DARK_GRAY
                        + " » "
                        + ChatColor.RESET
                        + "No graves found for offline player "
                        + targetOfflinePlayerName
                        + "."
                  );
               }
               break;
            case "grave-specific":
            case "grave-uuid":
               if (args.length < 3) {
                  commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Please specify a grave UUID.");
                  return;
               }

               UUID graveUUIDTarget;
               try {
                  graveUUIDTarget = UUID.fromString(args[2]);
               } catch (IllegalArgumentException var12) {
                  commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Invalid UUID format.");
                  return;
               }

               Grave target = null;

               for (Grave gravex : this.plugin.getCacheManager().getGraveMap().values()) {
                  if (graveUUIDTarget.equals(gravex.getUUID())) {
                     target = gravex;
                     break;
                  }
               }

               if (target == null) {
                  commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "No graves found for UUID " + args[2] + ".");
                  return;
               }

               this.plugin.getSchedulerManager().runTask(() -> {
                  this.plugin.getGraveManager().removeGrave(target);
                  commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Grave UUID " + graveUUIDTarget + " purged.");
               });
               break;
            case "abandon":
            case "abandoned":
               int purged = 0;
               List<Grave> gravesToPurge = new ArrayList<>();

               for (Grave gravexx : this.plugin.getCacheManager().getGraveMap().values()) {
                  if (this.plugin.getGraveManager().isGraveAbandoned(gravexx)) {
                     gravesToPurge.add(gravexx);
                  }
               }

               for (Grave gravexxx : gravesToPurge) {
                  this.plugin.getGraveManager().removeGrave(gravexxx);
                  purged++;
               }

               commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + purged + " abandoned grave(s) purged.");
               break;
            default:
               List<Grave> allGraves = new ArrayList<>(this.plugin.getCacheManager().getGraveMap().values());

               for (Grave gravexx : allGraves) {
                  this.plugin.getGraveManager().removeGrave(gravexx);
               }

               commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + allGraves.size() + " graves purged.");
         }
      }
   }

   private void handleImportCommand(CommandSender commandSender, String[] args) {
      if (args != null && args.length != 0) {
         String sub = args[1].toLowerCase(Locale.ROOT);
         boolean isConsole = !(commandSender instanceof Player);
         Player player = isConsole ? null : (Player)commandSender;
         if (!isConsole && !this.plugin.getPermissionManager().hasGrantedPermission("graves.import", player)) {
            this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
         } else if (sub.equals("angelchest")) {
            if (isConsole) {
               this.consolePendingImport = true;
            } else {
               this.pendingImports.add(player.getUniqueId());
            }

            this.plugin.debugMessage(this.plugin.getImportManager().countAngelChestStatusText(), 1);
            this.plugin.debugMessage(this.plugin.getImportManager().listAngelChestMissingWorldText(), 2);
            commandSender.sendMessage(
               ChatColor.RED
                  + "☠"
                  + ChatColor.DARK_GRAY
                  + " » "
                  + ChatColor.RESET
                  + "This will import "
                  + ChatColor.RED
                  + this.plugin.getImportManager().countAngelChestImportableOnly()
                  + ChatColor.RESET
                  + " graves from AngelChest. This may create many graves and cannot be undone.\nYou bear in mind that hex color codes may not convert over.\n"
                  + ChatColor.YELLOW
                  + "Type "
                  + ChatColor.RED
                  + "/graves import confirm"
                  + ChatColor.YELLOW
                  + " to proceed."
            );
         } else if (sub.equals("confirm")) {
            boolean allowed = isConsole ? this.consolePendingImport : this.pendingImports.remove(player.getUniqueId());
            if (!allowed) {
               commandSender.sendMessage(
                  ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "No pending import request. Run /graves import {plugin} first."
               );
            } else {
               if (isConsole) {
                  this.consolePendingImport = false;
               }

               List<Grave> graveList = this.plugin.getImportManager().importExternalPluginAngelChest();
               int placed = 0;

               for (Grave grave : graveList) {
                  this.plugin.getDataManager().addGrave(grave);
                  if (grave.getLocationDeath() != null) {
                     this.plugin.getGraveManager().placeGrave(grave.getLocationDeath(), grave);
                     placed++;
                  }
               }

               commandSender.sendMessage(
                  ChatColor.RED
                     + "☠"
                     + ChatColor.DARK_GRAY
                     + " » "
                     + ChatColor.RESET
                     + "Imported "
                     + graveList.size()
                     + " graves from AngelChest"
                     + (placed != graveList.size() ? " (" + placed + " placed, " + (graveList.size() - placed) + " skipped due to missing location)" : "")
                     + "."
               );
            }
         } else {
            commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves import {plugin}");
         }
      } else {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves import {plugin}");
      }
   }

   private void handleMigrationCommand(CommandSender commandSender, String[] args) {
      boolean isConsole = !(commandSender instanceof Player);
      Player player = isConsole ? null : (Player)commandSender;
      if (!isConsole && !this.plugin.getPermissionManager().hasGrantedPermission("graves.migration", player)) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
      } else if (args.length < 2) {
         this.sendMigrationList(commandSender);
      } else if (!args[1].equalsIgnoreCase("holograms") && !args[1].equalsIgnoreCase("hologram")) {
         this.sendMigrationList(commandSender);
      } else if (!this.plugin.getVersionManager().isHasTextDisplays()) {
         commandSender.sendMessage(
            ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "TextDisplay holograms are not supported on this server version."
         );
      } else if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
         boolean allowed = isConsole ? this.consolePendingHologramMigration : this.pendingHologramMigrations.remove(player.getUniqueId());
         if (!allowed) {
            commandSender.sendMessage(
               ChatColor.RED
                  + "☠"
                  + ChatColor.DARK_GRAY
                  + " » "
                  + ChatColor.RESET
                  + "No pending hologram migration request. Run /graves migration holograms first."
            );
         } else {
            if (isConsole) {
               this.consolePendingHologramMigration = false;
            }

            int migrated = this.migrateArmorStandHologramsToTextDisplays();
            commandSender.sendMessage(
               ChatColor.RED
                  + "☠"
                  + ChatColor.DARK_GRAY
                  + " » "
                  + ChatColor.RESET
                  + "Hologram migration complete. Graves migrated to the modern TextDisplay system: "
                  + ChatColor.RED
                  + migrated
                  + ChatColor.RESET
                  + "."
            );
         }
      } else if (args.length > 2) {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves migration holograms confirm");
      } else {
         Set<UUID> graveUUIDs = this.getArmorStandHologramGraveUUIDs();
         if (graveUUIDs.isEmpty()) {
            commandSender.sendMessage(
               ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "No ArmorStand grave holograms were found to migrate."
            );
         } else {
            if (isConsole) {
               this.consolePendingHologramMigration = true;
            } else {
               this.pendingHologramMigrations.add(player.getUniqueId());
            }

            commandSender.sendMessage(
               ChatColor.RED
                  + "☠"
                  + ChatColor.DARK_GRAY
                  + " » "
                  + ChatColor.RESET
                  + "This will migrate all ArmorStand grave holograms for "
                  + ChatColor.RED
                  + graveUUIDs.size()
                  + ChatColor.RESET
                  + " graves to TextDisplay holograms. This may take time.\n"
                  + ChatColor.YELLOW
                  + "Type "
                  + ChatColor.RED
                  + "/graves migration holograms confirm"
                  + ChatColor.YELLOW
                  + " to proceed."
            );
         }
      }
   }

   private int migrateArmorStandHologramsToTextDisplays() {
      Set<UUID> graveUUIDs = this.getArmorStandHologramGraveUUIDs();
      int migrated = 0;

      for (UUID graveUUID : graveUUIDs) {
         Grave grave = this.plugin.getCacheManager().getGraveMap().get(graveUUID);
         if (grave != null && grave.getLocationDeath() != null && grave.getLocationDeath().getWorld() != null) {
            boolean hasTextDisplay = this.hasHologramBackend(graveUUID, HologramData.Backend.TEXT_DISPLAY);
            this.plugin.getArmorStandManager().removeHologram(grave);
            this.plugin.getArmorStandManager().purgeWorldArmorStandHolograms(grave);
            if (!hasTextDisplay) {
               this.plugin.getTextDisplayManager().createHologram(grave.getLocationDeath(), grave);
            }

            migrated++;
         }
      }

      return migrated;
   }

   private void sendMigrationList(CommandSender commandSender) {
      commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Available migrations:");
      commandSender.sendMessage(
         ChatColor.RED
            + "/graves migration holograms "
            + ChatColor.DARK_GRAY
            + "-"
            + ChatColor.RESET
            + " Migrate ArmorStand grave holograms to TextDisplay holograms"
      );
   }

   private Set<UUID> getArmorStandHologramGraveUUIDs() {
      Set<UUID> graveUUIDs = new HashSet<>();

      for (EntityData entityData : new ArrayList<>(this.plugin.getCacheManager().getEntityMap().values())) {
         if (entityData instanceof HologramData hologramData && hologramData.getBackend() == HologramData.Backend.ARMOR_STAND) {
            UUID graveUUID = hologramData.getUUIDGrave();
            if (graveUUID != null && this.plugin.getCacheManager().getGraveMap().containsKey(graveUUID)) {
               graveUUIDs.add(graveUUID);
            }
         }
      }

      return graveUUIDs;
   }

   private boolean hasHologramBackend(UUID graveUUID, HologramData.Backend backend) {
      if (graveUUID != null && backend != null) {
         for (EntityData entityData : new ArrayList<>(this.plugin.getCacheManager().getEntityMap().values())) {
            if (entityData instanceof HologramData hologramData && graveUUID.equals(hologramData.getUUIDGrave()) && hologramData.getBackend() == backend) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void handleCountCommand(CommandSender commandSender, String[] args) {
      if (commandSender instanceof Player player && !this.plugin.getPermissionManager().hasGrantedPermission("graves.count", player.getPlayer())) {
         this.plugin.getEntityManager().sendMessage("message.permission-denied", player);
      } else if (args.length < 2) {
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves count world [world]");
         commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves count player <player> [world]");
      } else {
         String type = args[1].toLowerCase(Locale.ROOT);
         switch (type) {
            case "world":
               World targetWorld;
               if (args.length >= 3) {
                  targetWorld = Bukkit.getWorld(args[2]);
                  if (targetWorld == null) {
                     commandSender.sendMessage(
                        ChatColor.RED
                           + "☠"
                           + ChatColor.DARK_GRAY
                           + " » "
                           + ChatColor.RESET
                           + "World "
                           + ChatColor.RED
                           + args[2]
                           + ChatColor.RESET
                           + " not found."
                     );
                     return;
                  }
               } else {
                  if (!(commandSender instanceof Player player)) {
                     commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Console must specify a world.");
                     return;
                  }

                  targetWorld = player.getWorld();
               }

               int count = 0;

               for (Grave gravex : this.plugin.getCacheManager().getGraveMap().values()) {
                  Location location = gravex.getLocationDeath();
                  if (location != null && location.getWorld() != null && location.getWorld().getUID().equals(targetWorld.getUID())) {
                     count++;
                  }
               }

               commandSender.sendMessage(
                  ChatColor.RED
                     + "☠"
                     + ChatColor.DARK_GRAY
                     + " » "
                     + ChatColor.RESET
                     + "Found "
                     + ChatColor.RED
                     + count
                     + ChatColor.RESET
                     + " grave(s) in world "
                     + ChatColor.RED
                     + targetWorld.getName()
                     + ChatColor.RESET
                     + "."
               );
               break;
            case "player":
               OfflinePlayer targetPlayer;
               World targetWorld;
               if (args.length == 2) {
                  if (!(commandSender instanceof Player player)) {
                     commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Console must specify a player and world.");
                     return;
                  }

                  targetPlayer = player;
                  targetWorld = player.getWorld();
               } else if (args.length == 3) {
                  if (!(commandSender instanceof Player player)) {
                     commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Console must specify a world.");
                     return;
                  }

                  targetPlayer = Bukkit.getOfflinePlayer(args[2]);
                  if (targetPlayer.getName() == null && !targetPlayer.hasPlayedBefore()) {
                     commandSender.sendMessage(
                        ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Player not found or has never played on this server."
                     );
                     return;
                  }

                  targetWorld = player.getWorld();
               } else {
                  targetPlayer = Bukkit.getOfflinePlayer(args[2]);
                  if (targetPlayer.getName() == null && !targetPlayer.hasPlayedBefore()) {
                     commandSender.sendMessage(
                        ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Player not found or has never played on this server."
                     );
                     return;
                  }

                  targetWorld = Bukkit.getWorld(args[3]);
                  if (targetWorld == null) {
                     commandSender.sendMessage(
                        ChatColor.RED
                           + "☠"
                           + ChatColor.DARK_GRAY
                           + " » "
                           + ChatColor.RESET
                           + "World "
                           + ChatColor.RED
                           + args[3]
                           + ChatColor.RESET
                           + " not found."
                     );
                     return;
                  }
               }

               int count = 0;
               UUID targetUUID = targetPlayer.getUniqueId();

               for (Grave grave : this.plugin.getCacheManager().getGraveMap().values()) {
                  if (targetUUID.equals(grave.getOwnerUUID())) {
                     Location location = grave.getLocationDeath();
                     if (location != null && location.getWorld() != null && location.getWorld().getUID().equals(targetWorld.getUID())) {
                        count++;
                     }
                  }
               }

               String playerName = targetPlayer.getName() != null ? targetPlayer.getName() : targetUUID.toString();
               commandSender.sendMessage(
                  ChatColor.RED
                     + "☠"
                     + ChatColor.DARK_GRAY
                     + " » "
                     + ChatColor.RESET
                     + "Found "
                     + ChatColor.RED
                     + count
                     + ChatColor.RESET
                     + " grave(s) for "
                     + ChatColor.RED
                     + playerName
                     + ChatColor.RESET
                     + " in world "
                     + ChatColor.RED
                     + targetWorld.getName()
                     + ChatColor.RESET
                     + "."
               );
               break;
            default:
               commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves count world [world]");
               commandSender.sendMessage(ChatColor.RED + "☠" + ChatColor.DARK_GRAY + " » " + ChatColor.RESET + "Usage: /graves count player [player] [world]");
         }
      }
   }
}
