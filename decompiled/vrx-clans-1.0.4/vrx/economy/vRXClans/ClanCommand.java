package vrx.economy.vRXClans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ClanCommand implements CommandExecutor, TabCompleter {
   public static final List<String> ROOT_SUBCOMMANDS = List.of(
      "help",
      "create",
      "rename",
      "setbase",
      "base",
      "shop",
      "war",
      "list",
      "info",
      "bank",
      "deposit",
      "withdraw",
      "invite",
      "join",
      "kick",
      "promote",
      "demote",
      "disband",
      "leave",
      "ally",
      "rival",
      "neutral",
      "logs",
      "top",
      "tag",
      "tagcolor",
      "sethome",
      "home",
      "delhome",
      "homes",
      "achievements",
      "warhistory",
      "apply",
      "applications",
      "accept",
      "deny",
      "profile",
      "desc",
      "icon",
      "motd",
      "mail",
      "shopupgrade"
   );
   public static final List<String> TOP_CATEGORIES = List.of("level", "bank", "kills", "kdr");
   private static final List<String> DIPLOMACY_ACTIONS = List.of("accept", "deny");
   private static final List<String> AMOUNT_SUGGESTIONS = List.of("100", "1000", "10000");
   private final VRXClans plugin;

   public ClanCommand(VRXClans plugin) {
      this.plugin = plugin;
   }

   public List<String> getRootTabCompletions() {
      return ROOT_SUBCOMMANDS;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
         return true;
      } else {
         ClanManager manager = this.plugin.getClanManager();
         if (args.length == 0) {
            ClanGUI.openMainMenu(player, this.plugin);
            return true;
         } else {
            String subCommand = args[0].toLowerCase();
            Clan playerClan = manager.getClanByPlayer(player);
            switch (subCommand) {
               case "help":
                  ClanGUI.openMainMenu(player, this.plugin);
                  break;
               case "list":
                  ClanGUI.openClanList(player, this.plugin);
                  break;
               case "top":
                  this.showTop(player, args);
                  break;
               case "logs":
                  this.showClanLogs(player, playerClan);
                  break;
               case "shop":
                  if (playerClan == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
                     return true;
                  }

                  ClanGUI.openShopMenu(player, playerClan, this.plugin);
                  break;
               case "setbase":
                  if (playerClan == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
                     return true;
                  }

                  if (!playerClan.hasPermission(player.getUniqueId(), "base")) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
                     return true;
                  }

                  if (this.plugin.isLocationInWorldGuardRegion(player.getLocation())) {
                     player.sendMessage(this.plugin.getLang().get("msg.base-wg-error"));
                     return true;
                  }

                  playerClan.setBaseLocation(player.getLocation());
                  manager.updateBaseIndex(playerClan);
                  player.sendMessage(this.plugin.getLang().get("msg.base-set"));
                  break;
               case "base":
                  if (playerClan == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
                     return true;
                  }

                  if (playerClan.getBaseLocation() == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.base-not-set"));
                     return true;
                  }

                  if (manager.isTeleporting(player)) {
                     player.sendMessage(this.plugin.getLang().get("msg.base-tp-already"));
                     return true;
                  }

                  double calculatedTpCost = this.plugin.getConfig().getDouble("base-teleport-cost", 10.0);
                  if (playerClan.getTeleportCostReductionPercent() > 0.0) {
                     calculatedTpCost = Math.max(0.0, calculatedTpCost * (1.0 - playerClan.getTeleportCostReductionPercent() / 100.0));
                  }

                  double tpCost = calculatedTpCost;
                  if (tpCost > 0.0 && !VRXClans.getEconomy().has(player, tpCost)) {
                     player.sendMessage(this.plugin.getLang().get("msg.need-money", "%amount%", String.valueOf(tpCost)));
                     return true;
                  }

                  int delay = Math.max(0, this.plugin.getConfig().getInt("base-teleport-delay", 3) - playerClan.getTeleportDelayReduction());
                  if (delay > 0) {
                     player.sendMessage(this.plugin.getLang().get("msg.base-tp-delay", "%time%", String.valueOf(delay)));
                     BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                        manager.cancelTeleport(player);
                        if (tpCost > 0.0) {
                           VRXClans.getEconomy().withdrawPlayer(player, tpCost);
                           player.sendMessage(this.plugin.getLang().get("msg.base-tp-paid", "%amount%", String.valueOf(tpCost)));
                        }

                        player.teleport(playerClan.getBaseLocation());
                        player.sendMessage(this.plugin.getLang().get("msg.base-tp-welcome"));
                     }, delay * 20L);
                     manager.addTeleport(player, task);
                  } else {
                     if (tpCost > 0.0) {
                        VRXClans.getEconomy().withdrawPlayer(player, tpCost);
                        player.sendMessage(this.plugin.getLang().get("msg.base-tp-paid", "%amount%", String.valueOf(tpCost)));
                     }

                     player.teleport(playerClan.getBaseLocation());
                     player.sendMessage(this.plugin.getLang().get("msg.base-tp-welcome"));
                  }
                  break;
               case "create":
                  if (args.length < 2) {
                     player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan create <Name>"));
                     return true;
                  }

                  long createCooldown = manager.getJoinCooldownSeconds(player);
                  if (createCooldown > 0L) {
                     player.sendMessage(this.plugin.getLang().get("msg.cooldown", "%time%", this.formatTime(createCooldown)));
                     return true;
                  }

                  String newClanName = args[1];
                  if (!this.isValidName(player, newClanName)) {
                     return true;
                  }

                  double creationCost = this.plugin.getConfig().getDouble("clan-creation-cost");
                  if (creationCost > 0.0 && !VRXClans.getEconomy().has(player, creationCost)) {
                     player.sendMessage(this.plugin.getLang().get("msg.need-money", "%amount%", String.valueOf(creationCost)));
                     return true;
                  }

                  if (manager.createClan(newClanName, player)) {
                     if (creationCost > 0.0) {
                        VRXClans.getEconomy().withdrawPlayer(player, creationCost);
                     }

                     player.sendMessage(this.plugin.getLang().get("msg.clan-created", "%clan%", newClanName));
                  } else {
                     player.sendMessage(this.plugin.getLang().get("msg.clan-exists"));
                  }
                  break;
               case "rename":
                  if (playerClan == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
                     return true;
                  }

                  if (playerClan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
                     player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
                     return true;
                  }

                  if (args.length < 2) {
                     player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan rename <Name>"));
                     return true;
                  }

                  String newName = args[1];
                  if (!this.isValidName(player, newName)) {
                     return true;
                  }

                  double renameCost = this.plugin.getConfig().getDouble("clan-rename-cost");
                  if (renameCost > 0.0 && !VRXClans.getEconomy().has(player, renameCost)) {
                     player.sendMessage(this.plugin.getLang().get("msg.need-money", "%amount%", String.valueOf(renameCost)));
                     return true;
                  }

                  String oldName = playerClan.getName();
                  if (manager.renameClan(playerClan, newName)) {
                     if (renameCost > 0.0) {
                        VRXClans.getEconomy().withdrawPlayer(player, renameCost);
                     }

                     Bukkit.broadcastMessage(this.plugin.getLang().get("msg.clan-renamed", "%old%", oldName, "%new%", newName));
                  } else {
                     player.sendMessage(this.plugin.getLang().get("msg.name-taken"));
                  }
                  break;
               case "info":
                  Clan targetClan = args.length == 1 ? playerClan : manager.getClanByName(args[1]);
                  if (targetClan == null) {
                     player.sendMessage(args.length == 1 ? this.plugin.getLang().get("msg.not-in-clan") : this.plugin.getLang().get("msg.player-not-found"));
                     return true;
                  }

                  LanguageManager l = this.plugin.getLang();
                  player.sendMessage(l.get("misc.info-header"));
                  player.sendMessage(l.get("misc.info-title", "%clan%", targetClan.getName()));
                  player.sendMessage(
                     l.get("misc.info-level", "%level%", String.valueOf(targetClan.getLevel()), "%xp%", String.valueOf((int)targetClan.getXp()))
                  );
                  player.sendMessage(
                     l.get(
                        "misc.info-members",
                        "%members%",
                        String.valueOf(targetClan.getMembers().size()),
                        "%max%",
                        String.valueOf(this.plugin.getConfig().getInt("max-members"))
                     )
                  );
                  player.sendMessage(l.get("misc.info-bank", "%balance%", String.valueOf(targetClan.getBankBalance())));
                  player.sendMessage(
                     l.get(
                        "misc.info-stats",
                        "%kills%",
                        String.valueOf(targetClan.getKills()),
                        "%deaths%",
                        String.valueOf(targetClan.getDeaths()),
                        "%kdr%",
                        targetClan.getKDR()
                     )
                  );
                  if (targetClan.getActiveWar() != null) {
                     int target = this.plugin.getConfig().getInt("war-kill-target", 50);
                     player.sendMessage(
                        l.get(
                           "misc.info-war-active",
                           "%enemy%",
                           targetClan.getActiveWar(),
                           "%kills%",
                           String.valueOf(targetClan.getWarKills()),
                           "%target%",
                           String.valueOf(target)
                        )
                     );
                  } else {
                     player.sendMessage(l.get("misc.info-war-none"));
                  }

                  if (targetClan.getBaseLocation() != null) {
                     player.sendMessage(l.get("misc.info-radius", "%radius%", String.valueOf(targetClan.getDynamicRadius(this.plugin))));
                  } else {
                     player.sendMessage(l.get("misc.info-nobase"));
                  }

                  if (!targetClan.getAllies().isEmpty()) {
                     player.sendMessage(l.get("misc.info-allies", "%allies%", String.join(", ", targetClan.getAllies())));
                  }

                  if (!targetClan.getRivals().isEmpty()) {
                     player.sendMessage(l.get("misc.info-rivals", "%rivals%", String.join(", ", targetClan.getRivals())));
                  }

                  player.sendMessage(l.get("misc.info-header"));
                  break;
               case "tag":
                  this.handleTag(player, playerClan, args);
                  break;
               case "tagcolor":
                  this.handleTagColor(player, playerClan, args);
                  break;
               case "sethome":
               case "home":
               case "delhome":
               case "homes":
                  this.handleHomes(player, playerClan, subCommand, args);
                  break;
               case "achievements":
                  this.showAchievements(player, playerClan);
                  break;
               case "warhistory":
                  this.showWarHistory(player, args, playerClan);
                  break;
               case "profile":
                  this.showProfile(player, args, playerClan);
                  break;
               case "desc":
                  this.handleDescription(player, playerClan, args);
                  break;
               case "icon":
                  this.handleIcon(player, playerClan, args);
                  break;
               case "motd":
                  this.handleMotd(player, playerClan, args);
                  break;
               case "mail":
                  this.handleMail(player, playerClan, args);
                  break;
               case "shopupgrade":
                  this.handleShopUpgrade(player, playerClan, args);
                  break;
               case "apply":
                  this.handleApply(player, playerClan, args);
                  break;
               case "applications":
                  this.handleApplications(player, playerClan);
                  break;
               case "accept":
                  this.handleApplicationDecision(player, playerClan, args, true);
                  break;
               case "deny":
                  this.handleApplicationDecision(player, playerClan, args, false);
                  break;
               case "war":
                  this.handleWarCommands(player, playerClan, manager, args);
                  break;
               case "bank":
               case "deposit":
               case "withdraw":
                  this.handleBankCommands(player, playerClan, subCommand, args);
                  break;
               case "ally":
               case "rival":
               case "neutral":
                  this.handleDiplomacy(player, playerClan, manager, subCommand, args);
                  break;
               case "invite":
               case "join":
               case "kick":
               case "promote":
               case "demote":
               case "disband":
               case "leave":
                  this.handleMemberCommands(player, playerClan, manager, subCommand, args);
                  break;
               default:
                  player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan"));
            }

            return true;
         }
      }
   }

   private void showTop(Player player, String[] args) {
      String category = args.length >= 2 ? args[1].toLowerCase() : "level";
      String label;
      switch (category) {
         case "bank":
            label = "Bank";
            break;
         case "kills":
            label = "Kills";
            break;
         case "kdr":
            label = "KDR";
            break;
         case "level":
            label = "Level";
            break;
         default:
            player.sendMessage("§cUsage: /clan top <level|bank|kills|kdr>");
            return;
      }

      List<Clan> clans = this.plugin.getClanManager().getTopClans(category, 10);
      player.sendMessage("§8§m--------§r §bTop Clans §7(" + label + ") §8§m--------");
      if (clans.isEmpty()) {
         player.sendMessage("§7No Clans vorhanden.");
      } else {
         for (int i = 0; i < Math.min(10, clans.size()); i++) {
            Clan clan = clans.get(i);
            player.sendMessage("§e#" + (i + 1) + " §b" + clan.getName() + " §8» §f" + this.topValue(clan, category));
         }
      }
   }

   private String topValue(Clan clan, String category) {
      return switch (category) {
         case "bank" -> clan.getBankBalance() + "$";
         case "kills" -> clan.getKills() + " Kills";
         case "kdr" -> clan.getKDR() + " KDR";
         default -> "Level " + clan.getLevel() + " (" + (int)clan.getXp() + " XP)";
      };
   }

   private double kdrValue(Clan clan) {
      return clan.getDeaths() == 0 ? clan.getKills() : (double)clan.getKills() / clan.getDeaths();
   }

   private boolean isValidName(Player player, String name) {
      int min = this.plugin.getConfig().getInt("name-length-min");
      int max = this.plugin.getConfig().getInt("name-length-max");
      if (name.length() >= min && name.length() <= max) {
         if (!name.matches("^[a-zA-Z0-9]+$")) {
            player.sendMessage(this.plugin.getLang().get("msg.invalid-name-chars"));
            return false;
         } else {
            for (String blocked : this.plugin.getConfig().getStringList("blocked-names")) {
               if (name.equalsIgnoreCase(blocked)) {
                  player.sendMessage(this.plugin.getLang().get("msg.blocked-name"));
                  return false;
               }
            }

            return true;
         }
      } else {
         player.sendMessage(this.plugin.getLang().get("msg.invalid-name-length", "%min%", String.valueOf(min), "%max%", String.valueOf(max)));
         return false;
      }
   }

   private String formatTime(long seconds) {
      long hours = seconds / 3600L;
      long minutes = seconds % 3600L / 60L;
      long secs = seconds % 60L;
      if (hours > 0L) {
         return hours + "h " + minutes + "m " + secs + "s";
      } else {
         return minutes > 0L ? minutes + "m " + secs + "s" : secs + "s";
      }
   }

   private void showClanLogs(Player player, Clan playerClan) {
      if (playerClan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         player.sendMessage("§8§m--------§r §bClan-Logs §8§m--------");
         Bukkit.getScheduler()
            .runTaskAsynchronously(
               this.plugin,
               () -> {
                  List<ClanLogEntry> logs = this.plugin.getClanManager().getClanLogs(playerClan, 10);
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           if (logs.isEmpty()) {
                              player.sendMessage("§7Noch keine Logs vorhanden.");
                           } else {
                              SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");

                              for (ClanLogEntry log : logs) {
                                 String time = format.format(new Date(log.timestamp()));
                                 String amount = log.amount() > 0.0 ? " §8(" + log.amount() + "$)" : "";
                                 player.sendMessage(
                                    "§8[§7"
                                       + time
                                       + "§8] §e"
                                       + log.type().name()
                                       + " §7"
                                       + this.safe(log.actor())
                                       + " §8→ §7"
                                       + this.safe(log.target())
                                       + amount
                                       + " §8- §f"
                                       + this.safe(log.message())
                                 );
                              }
                           }
                        }
                     );
               }
            );
      }
   }

   private String safe(String input) {
      return input != null && !input.isBlank() ? input : "-";
   }

   private void handleTag(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
      } else if (!this.plugin.getConfig().getBoolean("clan-tags.enabled", true)) {
         player.sendMessage("§cClan-Tags sind deaktiviert.");
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan tag <Tag>");
      } else {
         String tag = args[1].toUpperCase(Locale.ROOT);
         int min = this.plugin.getConfig().getInt("clan-tags.min-length", 2);
         int max = this.plugin.getConfig().getInt("clan-tags.max-length", 5);
         if (tag.length() >= min && tag.length() <= max && tag.matches("[A-Z0-9]+")) {
            for (String blocked : this.plugin.getConfig().getStringList("clan-tags.blocked-tags")) {
               if (tag.equalsIgnoreCase(blocked)) {
                  player.sendMessage("§cDieser Clan-Tag ist blockiert.");
                  return;
               }
            }

            clan.setTag(tag);
            this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.TAG_UPDATE, player.getName(), tag, 0.0, "Clan tag changed");

            for (Player online : Bukkit.getOnlinePlayers()) {
               this.plugin.getClanManager().updatePlayerClanVisuals(online);
            }

            player.sendMessage("§aClan tag has been set to §b" + tag + " §ahas been set.");
         } else {
            player.sendMessage("§cDer Tag muss aus " + min + "-" + max + " Buchstaben/Zahlen bestehen.");
         }
      }
   }

   private void handleTagColor(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan tagcolor <&a|&b|&c...>");
      } else {
         String color = args[1].replace("§", "&");
         if (!this.plugin.getConfig().getStringList("clan-tags.allowed-colors").contains(color)) {
            player.sendMessage("§cThis color is not allowed.");
         } else {
            clan.setTagColor(color);

            for (Player online : Bukkit.getOnlinePlayers()) {
               this.plugin.getClanManager().updatePlayerClanVisuals(online);
            }

            player.sendMessage("§aClan tag color has been changed.");
         }
      }
   }

   private void handleHomes(Player player, Clan clan, String subCommand, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!this.plugin.getConfig().getBoolean("homes.enabled", true)) {
         player.sendMessage("§cClan-Homes sind deaktiviert.");
      } else if (subCommand.equals("homes")) {
         if (clan.getHomes().isEmpty()) {
            player.sendMessage("§7No clan homes have been set.");
         } else {
            player.sendMessage("§8§m--------§r §bClan-Homes §8§m--------");
            clan.getHomes().keySet().forEach(homex -> player.sendMessage("§8- §e" + homex));
         }
      } else {
         String name = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "main";
         if (!name.matches("[a-z0-9_-]{1,16}")) {
            player.sendMessage("§cInvalid home name.");
         } else if (subCommand.equals("sethome")) {
            if (!clan.hasPermission(player.getUniqueId(), "base")) {
               player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
            } else {
               int maxHomes = this.getMaxHomes(clan);
               if (!clan.getHomes().containsKey(name) && clan.getHomes().size() >= maxHomes) {
                  player.sendMessage("§cEuer Clan kann aktuell maximal " + maxHomes + " Homes setzen.");
               } else {
                  clan.setHome(name, player.getLocation());
                  this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.HOME_SET, player.getName(), name, 0.0, "Clan home set");
                  player.sendMessage("§aClan home §e" + name + " §ahas been set.");
               }
            }
         } else if (subCommand.equals("delhome")) {
            if (!clan.hasPermission(player.getUniqueId(), "base")) {
               player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
            } else {
               if (clan.removeHome(name)) {
                  this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.HOME_DELETE, player.getName(), name, 0.0, "Clan home deleted");
                  player.sendMessage("§aClan home §e" + name + " §ahas been deleted.");
               } else {
                  player.sendMessage("§cThis clan home does not exist.");
               }
            }
         } else {
            if (subCommand.equals("home")) {
               Location home = clan.getHome(name);
               if (home == null) {
                  player.sendMessage("§cThis clan home does not exist.");
                  return;
               }

               player.teleport(home);
               player.sendMessage("§aTeleportiert zu Clan-Home §e" + name + "§a.");
            }
         }
      }
   }

   private int getMaxHomes(Clan clan) {
      int max = this.plugin.getConfig().getInt("homes.default-max", 1);
      ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("homes.level-unlocks");
      if (section == null) {
         return max;
      } else {
         for (String levelRaw : section.getKeys(false)) {
            try {
               int level = Integer.parseInt(levelRaw);
               if (clan.getLevel() >= level) {
                  max = Math.max(max, section.getInt(levelRaw));
               }
            } catch (NumberFormatException var7) {
            }
         }

         return max + clan.getExtraHomeSlots();
      }
   }

   private void showAchievements(Player player, Clan clan) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         this.plugin.getClanManager().evaluateAchievements(clan);
         player.sendMessage("§8§m--------§r §bClan-Achievements §8§m--------");
         ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("achievements.definitions");
         if (section == null) {
            player.sendMessage("§7No Achievements konfiguriert.");
         } else {
            for (String id : section.getKeys(false)) {
               String name = this.plugin.getConfig().getString("achievements.definitions." + id + ".name", id);
               boolean done = clan.hasAchievement(id);
               player.sendMessage((done ? "§a✔ " : "§c✘ ") + "§e" + name);
            }
         }
      }
   }

   private void showWarHistory(Player player, String[] args, Clan playerClan) {
      Clan target = args.length >= 2 ? this.plugin.getClanManager().getClanByName(args[1]) : playerClan;
      if (target == null) {
         player.sendMessage(args.length >= 2 ? this.plugin.getLang().get("msg.player-not-found") : this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         Bukkit.getScheduler()
            .runTaskAsynchronously(
               this.plugin,
               () -> {
                  List<WarHistoryEntry> history = this.plugin.getClanManager().getWarHistory(target, 10);
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> {
                           player.sendMessage("§8§m--------§r §bWar-History: §e" + target.getName() + " §8§m--------");
                           if (history.isEmpty()) {
                              player.sendMessage("§7No War-History vorhanden.");
                           } else {
                              SimpleDateFormat format = new SimpleDateFormat("dd.MM HH:mm");

                              for (WarHistoryEntry entry : history) {
                                 player.sendMessage(
                                    "§8[§7"
                                       + format.format(new Date(entry.timestamp()))
                                       + "§8] §a"
                                       + entry.winner()
                                       + " §7besiegte §c"
                                       + entry.loser()
                                       + " §8("
                                       + entry.winnerKills()
                                       + ":"
                                       + entry.loserKills()
                                       + ", Pot "
                                       + entry.pot()
                                       + "$)"
                                 );
                              }
                           }
                        }
                     );
               }
            );
      }
   }

   private void handleApply(Player player, Clan playerClan, String[] args) {
      if (playerClan != null) {
         player.sendMessage("§cDu bist bereits in einem Clan.");
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan apply <Clan>");
      } else {
         Clan target = this.plugin.getClanManager().getClanByName(args[1]);
         if (target == null) {
            player.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
         } else if (target.getMembers().size() >= this.plugin.getConfig().getInt("max-members")) {
            player.sendMessage("§cDieser Clan ist voll.");
         } else if (!this.plugin.getClanManager().addApplication(player, target)) {
            player.sendMessage("§cYou already have an open application or applications are disabled.");
         } else {
            this.plugin
               .getClanManager()
               .logClanEvent(target, ClanLogEntry.LogType.APPLICATION_CREATE, player.getName(), target.getName(), 0.0, "Application created");
            player.sendMessage("§aDu hast dich bei §e" + target.getName() + " §abeworben.");

            for (Player online : Bukkit.getOnlinePlayers()) {
               Clan c = this.plugin.getClanManager().getClanByPlayer(online);
               if (c != null && c.getName().equals(target.getName()) && c.hasPermission(online.getUniqueId(), "invite")) {
                  online.sendMessage("§8[§bClan§8] §e" + player.getName() + " §7hat sich bei eurem Clan beworben. §8(/clan applications)");
               }
            }
         }
      }
   }

   private void handleApplications(Player player, Clan clan) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!clan.hasPermission(player.getUniqueId(), "invite")) {
         player.sendMessage(this.plugin.getLang().get("msg.not-officer"));
      } else {
         List<ClanApplication> apps = this.plugin.getClanManager().getApplications(clan);
         player.sendMessage("§8§m--------§r §bClan Applications §8§m--------");
         if (apps.isEmpty()) {
            player.sendMessage("§7There are no open applications.");
         } else {
            for (ClanApplication app : apps) {
               player.sendMessage("§8- §e" + app.applicantName() + " §7(/clan accept " + app.applicantName() + ")");
            }
         }
      }
   }

   private void handleApplicationDecision(Player player, Clan clan, String[] args, boolean accept) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!clan.hasPermission(player.getUniqueId(), "invite")) {
         player.sendMessage(this.plugin.getLang().get("msg.not-officer"));
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan " + (accept ? "accept" : "deny") + " <Player>");
      } else {
         ClanApplication app = this.plugin.getClanManager().findApplication(clan, args[1]);
         if (app == null) {
            player.sendMessage("§cNo application from this player was found.");
         } else {
            this.plugin.getClanManager().removeApplication(clan, app.applicantUuid());
            if (!accept) {
               this.plugin
                  .getClanManager()
                  .logClanEvent(clan, ClanLogEntry.LogType.APPLICATION_DENY, player.getName(), app.applicantName(), 0.0, "Application denied");
               player.sendMessage("§cApplication from §e" + app.applicantName() + " §cdenied.");
               Player target = Bukkit.getPlayer(app.applicantUuid());
               if (target != null) {
                  target.sendMessage("§cYour application to §e" + clan.getName() + " §chas been denied.");
               }
            } else if (clan.getMembers().size() >= this.plugin.getConfig().getInt("max-members")) {
               player.sendMessage("§cDieser Clan ist voll.");
            } else {
               Player target = Bukkit.getPlayer(app.applicantUuid());
               if (target == null) {
                  player.sendMessage("§cThe player must be online to be accepted.");
               } else if (this.plugin.getClanManager().getClanByPlayer(target) != null) {
                  player.sendMessage("§cThe player is already in a clan.");
               } else {
                  this.plugin.getClanManager().addPlayerToClan(target, clan);
                  this.plugin
                     .getClanManager()
                     .logClanEvent(clan, ClanLogEntry.LogType.APPLICATION_ACCEPT, player.getName(), target.getName(), 0.0, "Application accepted");
                  player.sendMessage("§aApplication from §e" + target.getName() + " §aaccepted.");
                  target.sendMessage("§aYour application to §e" + clan.getName() + " §ahas been accepted.");
               }
            }
         }
      }
   }

   private void showProfile(Player player, String[] args, Clan playerClan) {
      Clan target = args.length >= 2 ? this.plugin.getClanManager().getClanByName(args[1]) : playerClan;
      if (target == null) {
         player.sendMessage("§cClan not found.");
      } else {
         player.sendMessage("§8§m--------§r §bClan-Profil §8§m--------");
         player.sendMessage(
            "§7Name: §b" + target.getName() + " §8[" + ChatColor.translateAlternateColorCodes('&', target.getTagColor() + target.getTag()) + "§8]"
         );
         player.sendMessage("§7Beschreibung: §f" + (target.getDescription().isBlank() ? "-" : target.getDescription()));
         player.sendMessage("§7Icon: §e" + target.getIconMaterial());
         player.sendMessage("§7MOTD: §f" + (target.getMotd().isBlank() ? "-" : target.getMotd()));
         player.sendMessage("§7Level: §e" + target.getLevel() + " §7| Bank: §a" + target.getBankBalance() + "$ §7| KDR: §e" + target.getKDR());
         player.sendMessage("§7Mitglieder: §e" + target.getMembers().size() + "§7/§e" + this.plugin.getClanManager().getMaxMembers(target));
      }
   }

   private void handleDescription(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
      } else if (!this.plugin.getConfig().getBoolean("profiles.enabled", true)) {
         player.sendMessage("§cClan profiles are disabled.");
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan desc <Beschreibung>");
      } else {
         String desc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
         clan.setDescription(desc);
         this.plugin
            .getClanManager()
            .logClanEvent(clan, ClanLogEntry.LogType.PROFILE_UPDATE, player.getName(), "Description", 0.0, "Profile description changed");
         player.sendMessage("§aClan description has been updated.");
      }
   }

   private void handleIcon(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan icon <Material>");
      } else {
         Material material = Material.matchMaterial(args[1].toUpperCase(Locale.ROOT));
         if (material != null && material.isItem()) {
            clan.setIconMaterial(material.name());
            this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.PROFILE_UPDATE, player.getName(), material.name(), 0.0, "Clan icon changed");
            player.sendMessage("§aClan icon has been set to §e" + material.name() + " §ahas been set.");
         } else {
            player.sendMessage("§cInvalid item material.");
         }
      }
   }

   private void handleMotd(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (args.length == 1) {
         player.sendMessage("§7Clan-MOTD: §f" + (clan.getMotd().isBlank() ? "-" : clan.getMotd()));
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER && clan.getRank(player.getUniqueId()) != Clan.ClanRank.CO_LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
      } else if (args[1].equalsIgnoreCase("clear")) {
         clan.setMotd("");
         player.sendMessage("§aClan MOTD has been cleared.");
      } else if (args[1].equalsIgnoreCase("set") && args.length >= 3) {
         clan.setMotd(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
         this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.MOTD_UPDATE, player.getName(), "MOTD", 0.0, "MOTD changed");
         player.sendMessage("§aClan MOTD has been set.");
      } else {
         player.sendMessage("§cUsage: /clan motd set <text> or /clan motd clear");
      }
   }

   private void handleMail(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!this.plugin.getConfig().getBoolean("mail.enabled", true)) {
         player.sendMessage("§cClan mail is disabled.");
      } else if (args.length == 1 || args[1].equalsIgnoreCase("read")) {
         player.sendMessage("§8§m--------§r §bClan-Mail §8§m--------");
         if (clan.getMailMessages().isEmpty()) {
            player.sendMessage("§7There are no clan mail messages.");
         } else {
            int i = 1;

            for (String mail : clan.getMailMessages()) {
               player.sendMessage("§e#" + i++ + " §7" + mail);
            }
         }
      } else if (clan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER && clan.getRank(player.getUniqueId()) != Clan.ClanRank.CO_LEADER) {
         player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
      } else if (args[1].equalsIgnoreCase("clear")) {
         clan.clearMailMessages();
         player.sendMessage("§aClan mail has been cleared.");
      } else if (args[1].equalsIgnoreCase("send") && args.length >= 3) {
         String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
         clan.addMailMessage("[" + player.getName() + "] " + message);
         this.plugin.clanBroadcast(clan, "§8[§bClan-Mail§8] §f" + message);
         this.plugin.getClanManager().logClanEvent(clan, ClanLogEntry.LogType.MAIL_SEND, player.getName(), "Clan", 0.0, "Clan mail sent");
      } else {
         player.sendMessage("§cUsage: /clan mail read|send <text>|clear");
      }
   }

   private void handleShopUpgrade(Player player, Clan clan, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!clan.hasPermission(player.getUniqueId(), "shop")) {
         player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
      } else if (args.length < 2) {
         player.sendMessage("§cUsage: /clan shopupgrade <members|homes|vault|allies|rivals|teleport>");
      } else {
         this.buyExtendedShopUpgrade(player, clan, args[1].toLowerCase(Locale.ROOT));
      }
   }

   private void buyExtendedShopUpgrade(Player player, Clan clan, String type) {
      String path = "shop.extended." + type;
      if (!this.plugin.getConfig().getBoolean(path + ".enabled", false)) {
         player.sendMessage("§cThis upgrade is disabled.");
      } else {
         int maxPurchases = this.plugin.getConfig().getInt(path + ".max-purchases", 10);

         int current = switch (type) {
            case "members" -> clan.getExtraMemberSlots();
            case "homes" -> clan.getExtraHomeSlots();
            case "vault" -> clan.getExtraVaultRows();
            case "allies" -> clan.getExtraAllySlots();
            case "rivals" -> clan.getExtraRivalSlots();
            case "teleport" -> clan.getTeleportDelayReduction();
            default -> -1;
         };
         if (current < 0) {
            player.sendMessage("§cUnknown upgrade.");
         } else if (current >= maxPurchases) {
            player.sendMessage("§cDieses Upgrade ist bereits maximal ausgebaut.");
         } else {
            double cost = this.plugin.getConfig().getDouble(path + ".cost", 1000.0);
            if (!clan.withdraw(cost)) {
               player.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
            } else {
               int add = this.plugin.getConfig().getInt(path + ".add", 1);
               switch (type) {
                  case "members":
                     clan.addExtraMemberSlots(add);
                     break;
                  case "homes":
                     clan.addExtraHomeSlots(add);
                     break;
                  case "vault":
                     clan.addExtraVaultRows(add);
                     break;
                  case "allies":
                     clan.addExtraAllySlots(add);
                     break;
                  case "rivals":
                     clan.addExtraRivalSlots(add);
                     break;
                  case "teleport":
                     clan.addTeleportDelayReduction(add);
               }

               this.plugin
                  .getClanManager()
                  .logClanEvent(clan, ClanLogEntry.LogType.SHOP_UPGRADE, player.getName(), type, cost, "Erweitertes Shop-Upgrade gekauft");
               player.sendMessage("§aUpgrade purchased: §e" + type + " §7(-" + cost + "$)");
            }
         }
      }
   }

   private void handleWarCommands(Player player, Clan clan, ClanManager manager, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!clan.hasPermission(player.getUniqueId(), "war")) {
         player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
      } else {
         if (args.length == 2 && args[1].equalsIgnoreCase("accept")) {
            ClanManager.WarRequest req = manager.getWarReq(clan.getName());
            if (req == null) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-no-request"));
               return;
            }

            Clan reqClan = manager.getClanByName(req.requester);
            if (reqClan == null) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-clan-missing"));
               return;
            }

            if (clan.getActiveWar() != null) {
               player.sendMessage(this.plugin.getLang().get("msg.war-already-in-war"));
               return;
            }

            if (reqClan.getActiveWar() != null) {
               player.sendMessage(this.plugin.getLang().get("msg.war-target-in-war"));
               return;
            }

            if (clan.getBankBalance() < req.wager) {
               player.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
               return;
            }

            if (reqClan.getBankBalance() < req.wager) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-clan-missing"));
               return;
            }

            clan.withdraw(req.wager);
            reqClan.withdraw(req.wager);
            clan.setActiveWar(reqClan.getName());
            clan.setWarWager(req.wager);
            clan.setWarKills(0);
            reqClan.setActiveWar(clan.getName());
            reqClan.setWarWager(req.wager);
            reqClan.setWarKills(0);
            manager.beginWar(clan, reqClan);
            manager.clearWarReq(clan.getName());
            int target = this.plugin.getConfig().getInt("war-kill-target", 50);
            Bukkit.broadcastMessage(
               this.plugin
                  .getLang()
                  .get(
                     "msg.war-started",
                     "%clan1%",
                     clan.getName(),
                     "%clan2%",
                     reqClan.getName(),
                     "%target%",
                     String.valueOf(target),
                     "%pot%",
                     String.valueOf(req.wager * 2.0)
                  )
            );
            manager.logClanEvent(clan, ClanLogEntry.LogType.WAR_START, player.getName(), reqClan.getName(), req.wager, "Krieg gestartet");
            manager.logClanEvent(reqClan, ClanLogEntry.LogType.WAR_START, req.requester, clan.getName(), req.wager, "Krieg gestartet");
         } else if (args.length == 2 && args[1].equalsIgnoreCase("deny")) {
            manager.clearWarReq(clan.getName());
            player.sendMessage(this.plugin.getLang().get("msg.diplomacy-deny"));
         } else if (args.length == 3) {
            String targetName = args[1];
            Clan targetClan = manager.getClanByName(targetName);
            if (targetClan == null) {
               player.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
               return;
            }

            if (targetClan.getName().equals(clan.getName())) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-cannot-self"));
               return;
            }

            if (clan.getActiveWar() != null) {
               player.sendMessage(this.plugin.getLang().get("msg.war-already-in-war"));
               return;
            }

            if (targetClan.getActiveWar() != null) {
               player.sendMessage(this.plugin.getLang().get("msg.war-target-in-war"));
               return;
            }

            try {
               double wager = Double.parseDouble(args[2]);
               if (!Double.isFinite(wager) || wager <= 0.0) {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  return;
               }

               double minWager = this.plugin.getConfig().getDouble("war-min-wager", 1000.0);
               if (wager < minWager) {
                  player.sendMessage(this.plugin.getLang().get("msg.war-min-wager", "%amount%", String.valueOf(minWager)));
                  return;
               }

               if (clan.getBankBalance() < wager) {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
                  return;
               }

               manager.setWarReq(targetClan.getName(), clan.getName(), wager);
               manager.logClanEvent(clan, ClanLogEntry.LogType.WAR_REQUEST, player.getName(), targetClan.getName(), wager, "Krieg angefragt");
               player.sendMessage(this.plugin.getLang().get("msg.war-req-sent", "%clan%", targetClan.getName(), "%wager%", String.valueOf(wager)));

               for (Player p : Bukkit.getOnlinePlayers()) {
                  Clan pClan = manager.getClanByPlayer(p);
                  if (pClan != null && pClan.getName().equals(targetClan.getName()) && pClan.hasPermission(p.getUniqueId(), "war")) {
                     p.sendMessage(this.plugin.getLang().get("msg.war-req-received", "%clan%", clan.getName(), "%wager%", String.valueOf(wager)));
                  }
               }
            } catch (NumberFormatException var14) {
               player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
            }
         } else {
            player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan war <Clan> <Einsatz>"));
         }
      }
   }

   private void handleBankCommands(Player player, Clan playerClan, String subCommand, String[] args) {
      if (playerClan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         if (subCommand.equals("bank")) {
            ClanGUI.openBankMenu(player, playerClan, this.plugin);
         } else if (subCommand.equals("deposit")) {
            if (args.length < 2) {
               player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan deposit <amount>"));
               return;
            }

            try {
               double amount = Double.parseDouble(args[1]);
               if (!Double.isFinite(amount) || amount <= 0.0) {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  return;
               }

               if (VRXClans.getEconomy().has(player, amount)) {
                  VRXClans.getEconomy().withdrawPlayer(player, amount);
                  playerClan.deposit(amount);
                  this.plugin.getClanManager().logClanEvent(playerClan, ClanLogEntry.LogType.BANK_DEPOSIT, player.getName(), "ClanBank", amount, "Einzahlung");
                  player.sendMessage(this.plugin.getLang().get("msg.eco-deposit", "%amount%", String.valueOf(amount)));
                  if (!playerClan.isQuestBankDone()) {
                     playerClan.setQuestMoney(playerClan.getQuestMoney() + amount);
                     double target = this.plugin.getConfig().getDouble("quests.bank.target", 10000.0);
                     if (playerClan.getQuestMoney() >= target) {
                        playerClan.setQuestBankDone(true);
                        double reward = this.plugin.getConfig().getDouble("quests.bank.reward-xp", 2000.0);
                        if (playerClan.addXp(reward, this.plugin)) {
                           this.plugin.handleClanLevelUps(playerClan);
                        }

                        this.plugin
                           .clanBroadcast(
                              playerClan,
                              this.plugin
                                 .getLang()
                                 .get("msg.quest-completed", "%quest%", this.plugin.getLang().get("quests.name-bank"), "%reward%", String.valueOf(reward))
                           );
                     }
                  }
               } else {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
               }
            } catch (NumberFormatException var11) {
               player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
            }
         } else if (subCommand.equals("withdraw")) {
            if (!playerClan.hasPermission(player.getUniqueId(), "bank")) {
               player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
               return;
            }

            if (args.length < 2) {
               player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan withdraw <amount>"));
               return;
            }

            try {
               double amountx = Double.parseDouble(args[1]);
               if (!Double.isFinite(amountx) || amountx <= 0.0) {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  return;
               }

               if (playerClan.withdraw(amountx)) {
                  VRXClans.getEconomy().depositPlayer(player, amountx);
                  this.plugin
                     .getClanManager()
                     .logClanEvent(playerClan, ClanLogEntry.LogType.BANK_WITHDRAW, player.getName(), "ClanBank", amountx, "Auszahlung");
                  player.sendMessage(this.plugin.getLang().get("msg.eco-withdraw", "%amount%", String.valueOf(amountx)));
               } else {
                  player.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
               }
            } catch (NumberFormatException var12) {
               player.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
            }
         }
      }
   }

   private void handleMemberCommands(Player player, Clan playerClan, ClanManager manager, String subCommand, String[] args) {
      int maxMembers = playerClan == null ? this.plugin.getConfig().getInt("max-members") : this.plugin.getClanManager().getMaxMembers(playerClan);
      int joinCooldownTime = this.plugin.getConfig().getInt("join-cooldown");
      if (subCommand.equals("join")) {
         if (playerClan != null) {
            player.sendMessage(this.plugin.getLang().get("msg.clan-exists"));
         } else {
            long cdSeconds = manager.getJoinCooldownSeconds(player);
            if (cdSeconds > 0L) {
               player.sendMessage(this.plugin.getLang().get("msg.cooldown", "%time%", this.formatTime(cdSeconds)));
            } else {
               String invitedClanName = manager.getPendingInvite(player);
               if (invitedClanName == null) {
                  player.sendMessage(this.plugin.getLang().get("msg.join-no-invite"));
               } else {
                  Clan clanToJoin = manager.getClanByName(invitedClanName);
                  if (clanToJoin == null) {
                     player.sendMessage(this.plugin.getLang().get("msg.join-clan-missing"));
                     manager.clearInvite(player);
                  } else if (clanToJoin.getMembers().size() >= maxMembers) {
                     player.sendMessage(this.plugin.getLang().get("msg.join-clan-full", "%max%", String.valueOf(maxMembers)));
                  } else {
                     manager.addPlayerToClan(player, clanToJoin);
                     manager.clearInvite(player);
                     player.sendMessage(this.plugin.getLang().get("msg.join-success", "%clan%", clanToJoin.getName()));
                  }
               }
            }
         }
      } else if (playerClan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else {
         if (subCommand.equals("leave")) {
            if (playerClan.getRank(player.getUniqueId()) == Clan.ClanRank.LEADER) {
               player.sendMessage(this.plugin.getLang().get("msg.leave-leader"));
               return;
            }

            manager.removePlayerFromClan(player);
            manager.setJoinCooldown(player, joinCooldownTime);
            player.sendMessage(this.plugin.getLang().get("msg.leave-success", "%time%", this.formatTime(joinCooldownTime)));
         } else if (subCommand.equals("disband")) {
            if (playerClan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
               player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
               return;
            }

            String disbandedName = playerClan.getName();
            manager.disbandClan(playerClan);
            Bukkit.broadcastMessage(this.plugin.getLang().get("msg.disband-success", "%clan%", disbandedName));
         } else if (subCommand.equals("invite")) {
            if (!playerClan.hasPermission(player.getUniqueId(), "invite")) {
               player.sendMessage(this.plugin.getLang().get("msg.not-officer"));
               return;
            }

            if (playerClan.getMembers().size() >= maxMembers) {
               player.sendMessage(this.plugin.getLang().get("msg.join-clan-full", "%max%", String.valueOf(maxMembers)));
               return;
            }

            if (args.length < 2) {
               player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan invite <Player>"));
               return;
            }

            Player targetInvite = Bukkit.getPlayerExact(args[1]);
            if (targetInvite == null) {
               player.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
               return;
            }

            if (manager.getClanByPlayer(targetInvite) != null) {
               player.sendMessage(this.plugin.getLang().get("msg.invite-already-in-clan"));
               return;
            }

            int timeout = this.plugin.getConfig().getInt("invite-timeout");
            manager.addInvite(targetInvite, playerClan, timeout);
            player.sendMessage(this.plugin.getLang().get("msg.invite-sent", "%player%", targetInvite.getName(), "%time%", this.formatTime(timeout)));
            targetInvite.sendMessage(this.plugin.getLang().get("msg.invite-received", "%clan%", playerClan.getName(), "%time%", this.formatTime(timeout)));
         } else if (subCommand.equals("kick")) {
            Clan.ClanRank kickerRank = playerClan.getRank(player.getUniqueId());
            if (kickerRank != Clan.ClanRank.LEADER && kickerRank != Clan.ClanRank.CO_LEADER) {
               player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
               return;
            }

            if (args.length < 2) {
               player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan kick <Player>"));
               return;
            }

            Player targetKick = Bukkit.getPlayerExact(args[1]);
            if (targetKick == null || !playerClan.equals(manager.getClanByPlayer(targetKick))) {
               player.sendMessage(this.plugin.getLang().get("msg.kick-not-in-clan"));
               return;
            }

            Clan.ClanRank targetKickRank = playerClan.getRank(targetKick.getUniqueId());
            if (targetKick.equals(player)
               || targetKickRank == Clan.ClanRank.LEADER
               || kickerRank == Clan.ClanRank.CO_LEADER && targetKickRank == Clan.ClanRank.CO_LEADER) {
               player.sendMessage(this.plugin.getLang().get("msg.kick-cannot-kick"));
               return;
            }

            manager.logClanEvent(playerClan, ClanLogEntry.LogType.MEMBER_KICK, player.getName(), targetKick.getName(), 0.0, "Mitglied gekickt");
            manager.removePlayerFromClan(targetKick);
            manager.setJoinCooldown(targetKick, joinCooldownTime);
            player.sendMessage(this.plugin.getLang().get("msg.kick-success", "%player%", targetKick.getName()));
            targetKick.sendMessage(this.plugin.getLang().get("msg.kick-target", "%time%", this.formatTime(joinCooldownTime)));
         } else if (subCommand.equals("promote") || subCommand.equals("demote")) {
            if (playerClan.getRank(player.getUniqueId()) != Clan.ClanRank.LEADER) {
               player.sendMessage(this.plugin.getLang().get("msg.not-leader"));
               return;
            }

            if (args.length < 2) {
               player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan " + subCommand + " <Player>"));
               return;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null || !playerClan.equals(manager.getClanByPlayer(target))) {
               player.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
               return;
            }

            Clan.ClanRank currentRank = playerClan.getRank(target.getUniqueId());
            if (subCommand.equals("promote")) {
               if (currentRank == Clan.ClanRank.CO_LEADER || currentRank == Clan.ClanRank.LEADER) {
                  player.sendMessage(this.plugin.getLang().get("msg.promote-max"));
                  return;
               }

               Clan.ClanRank newRank = Clan.ClanRank.values()[currentRank.ordinal() + 1];
               playerClan.setRank(target.getUniqueId(), newRank);
               String rankName = playerClan.getRankDisplayName(newRank);
               manager.logClanEvent(playerClan, ClanLogEntry.LogType.MEMBER_PROMOTE, player.getName(), target.getName(), 0.0, "Promoted to " + rankName);
               player.sendMessage(this.plugin.getLang().get("msg.promote-success", "%player%", target.getName(), "%rank%", rankName));
               target.sendMessage(this.plugin.getLang().get("msg.promote-target", "%rank%", rankName));
            } else {
               if (currentRank == Clan.ClanRank.RECRUIT || currentRank == Clan.ClanRank.LEADER) {
                  player.sendMessage(this.plugin.getLang().get("msg.demote-min"));
                  return;
               }

               Clan.ClanRank newRank = Clan.ClanRank.values()[currentRank.ordinal() - 1];
               playerClan.setRank(target.getUniqueId(), newRank);
               String rankName = playerClan.getRankDisplayName(newRank);
               manager.logClanEvent(playerClan, ClanLogEntry.LogType.MEMBER_DEMOTE, player.getName(), target.getName(), 0.0, "Degradiert zu " + rankName);
               player.sendMessage(this.plugin.getLang().get("msg.demote-success", "%player%", target.getName(), "%rank%", rankName));
               target.sendMessage(this.plugin.getLang().get("msg.demote-target", "%rank%", rankName));
            }
         }
      }
   }

   private void handleDiplomacy(Player player, Clan clan, ClanManager manager, String type, String[] args) {
      if (clan == null) {
         player.sendMessage(this.plugin.getLang().get("msg.not-in-clan"));
      } else if (!clan.hasPermission(player.getUniqueId(), "war")) {
         player.sendMessage(this.plugin.getLang().get("msg.not-co-leader"));
      } else if (args.length < 2) {
         player.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clan " + type + " <Clan|accept|deny>"));
      } else {
         String action = args[1].toLowerCase();
         int maxAllies = this.plugin.getClanManager().getMaxAllies(clan);
         int maxRivals = this.plugin.getClanManager().getMaxRivals(clan);
         if (action.equals("accept")) {
            String reqName = type.equals("ally")
               ? manager.getAllyReq(clan.getName())
               : (type.equals("rival") ? manager.getRivalReq(clan.getName()) : manager.getNeutralReq(clan.getName()));
            if (reqName == null) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-no-request"));
               return;
            }

            Clan reqClan = manager.getClanByName(reqName);
            if (reqClan == null) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-clan-missing"));
               return;
            }

            if (type.equals("ally")) {
               if (clan.getAllies().size() >= maxAllies) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-self", "%limit%", String.valueOf(maxAllies)));
                  return;
               }

               if (reqClan.getAllies().size() >= maxAllies) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-target", "%limit%", String.valueOf(maxAllies)));
                  return;
               }

               clan.addAlly(reqName);
               reqClan.addAlly(clan.getName());
               clan.removeRival(reqName);
               reqClan.removeRival(clan.getName());
               manager.clearAllyReq(clan.getName());
               manager.logClanEvent(clan, ClanLogEntry.LogType.DIPLOMACY_ALLY, player.getName(), reqName, 0.0, "Allianz akzeptiert");
               manager.logClanEvent(reqClan, ClanLogEntry.LogType.DIPLOMACY_ALLY, reqName, clan.getName(), 0.0, "Allianz akzeptiert");
               Bukkit.broadcastMessage(this.plugin.getLang().get("msg.diplomacy-ally-success", "%clan1%", clan.getName(), "%clan2%", reqName));
            } else if (type.equals("rival")) {
               if (clan.getRivals().size() >= maxRivals) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-self", "%limit%", String.valueOf(maxRivals)));
                  return;
               }

               if (reqClan.getRivals().size() >= maxRivals) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-target", "%limit%", String.valueOf(maxRivals)));
                  return;
               }

               clan.addRival(reqName);
               reqClan.addRival(clan.getName());
               clan.removeAlly(reqName);
               reqClan.removeAlly(clan.getName());
               manager.clearRivalReq(clan.getName());
               manager.logClanEvent(clan, ClanLogEntry.LogType.DIPLOMACY_RIVAL, player.getName(), reqName, 0.0, "Rivalry accepted");
               manager.logClanEvent(reqClan, ClanLogEntry.LogType.DIPLOMACY_RIVAL, reqName, clan.getName(), 0.0, "Rivalry accepted");
               Bukkit.broadcastMessage(this.plugin.getLang().get("msg.diplomacy-rival-success", "%clan1%", clan.getName(), "%clan2%", reqName));
            } else if (type.equals("neutral")) {
               clan.removeAlly(reqName);
               reqClan.removeAlly(clan.getName());
               clan.removeRival(reqName);
               reqClan.removeRival(clan.getName());
               manager.clearNeutralReq(clan.getName());
               Bukkit.broadcastMessage(this.plugin.getLang().get("msg.diplomacy-neutral-success", "%clan1%", clan.getName(), "%clan2%", reqName));
            }
         } else if (action.equals("deny")) {
            if (type.equals("ally")) {
               manager.clearAllyReq(clan.getName());
            } else if (type.equals("rival")) {
               manager.clearRivalReq(clan.getName());
            } else {
               manager.clearNeutralReq(clan.getName());
            }

            player.sendMessage(this.plugin.getLang().get("msg.diplomacy-deny"));
         } else {
            Clan target = manager.getClanByName(args[1]);
            if (target == null) {
               player.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
               return;
            }

            if (target.getName().equals(clan.getName())) {
               player.sendMessage(this.plugin.getLang().get("msg.diplomacy-cannot-self"));
               return;
            }

            if (type.equals("ally")) {
               if (clan.getAllies().size() >= maxAllies) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-self", "%limit%", String.valueOf(maxAllies)));
                  return;
               }

               if (target.getAllies().size() >= maxAllies) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-target", "%limit%", String.valueOf(maxAllies)));
                  return;
               }

               if (clan.isAlliedWith(target.getName())) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-already-allied"));
                  return;
               }

               double allyCost = this.plugin.getConfig().getDouble("ally-creation-cost");
               if (allyCost > 0.0) {
                  if (!VRXClans.getEconomy().has(player, allyCost)) {
                     player.sendMessage(this.plugin.getLang().get("msg.need-money", "%amount%", String.valueOf(allyCost)));
                     return;
                  }

                  VRXClans.getEconomy().withdrawPlayer(player, allyCost);
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-money-deducted", "%amount%", String.valueOf(allyCost)));
               }

               manager.setAllyReq(target.getName(), clan.getName());
            } else if (type.equals("rival")) {
               if (clan.getRivals().size() >= maxRivals) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-self", "%limit%", String.valueOf(maxRivals)));
                  return;
               }

               if (target.getRivals().size() >= maxRivals) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-limit-reached-target", "%limit%", String.valueOf(maxRivals)));
                  return;
               }

               if (clan.isRivalWith(target.getName())) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-already-rivals"));
                  return;
               }

               manager.setRivalReq(target.getName(), clan.getName());
            } else if (type.equals("neutral")) {
               if (!clan.isAlliedWith(target.getName()) && !clan.isRivalWith(target.getName())) {
                  player.sendMessage(this.plugin.getLang().get("msg.diplomacy-already-neutral"));
                  return;
               }

               manager.setNeutralReq(target.getName(), clan.getName());
            }

            player.sendMessage(this.plugin.getLang().get("msg.diplomacy-request-sent", "%clan%", target.getName()));

            for (Player p : Bukkit.getOnlinePlayers()) {
               Clan targetPlayerClan = manager.getClanByPlayer(p);
               if (targetPlayerClan != null && targetPlayerClan.getName().equals(target.getName()) && targetPlayerClan.hasPermission(p.getUniqueId(), "war")) {
                  p.sendMessage(this.plugin.getLang().get("msg.diplomacy-request-received", "%clan%", clan.getName(), "%type%", type));
               }
            }
         }
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (!(sender instanceof Player player)) {
         return List.of();
      } else if (args.length == 0) {
         return ROOT_SUBCOMMANDS;
      } else {
         List<String> completions = new ArrayList<>();
         String current = args[args.length - 1].toLowerCase();
         if (args.length == 1) {
            addMatching(completions, ROOT_SUBCOMMANDS, current);
            return completions;
         } else {
            String subCmd = args[0].toLowerCase();
            Clan playerClan = this.plugin.getClanManager().getClanByPlayer(player);
            if (args.length == 2) {
               switch (subCmd) {
                  case "top":
                     addMatching(completions, TOP_CATEGORIES, current);
                     break;
                  case "tagcolor":
                     addMatching(completions, this.plugin.getConfig().getStringList("clan-tags.allowed-colors"), current);
                     break;
                  case "home":
                  case "delhome":
                     if (playerClan != null) {
                        addMatching(completions, new ArrayList<>(playerClan.getHomes().keySet()), current);
                     }
                     break;
                  case "apply":
                  case "warhistory":
                     this.addClanNames(completions, current, playerClan == null ? null : playerClan.getName());
                     break;
                  case "accept":
                  case "deny":
                     if (playerClan != null) {
                        for (ClanApplication app : this.plugin.getClanManager().getApplications(playerClan)) {
                           if (app.applicantName().toLowerCase().startsWith(current)) {
                              completions.add(app.applicantName());
                           }
                        }
                     }
                     break;
                  case "deposit":
                  case "withdraw":
                     addMatching(completions, AMOUNT_SUGGESTIONS, current);
                     break;
                  case "invite":
                     this.addOnlinePlayers(completions, current, p -> this.plugin.getClanManager().getClanByPlayer(p) == null && !p.equals(player));
                     break;
                  case "kick":
                  case "promote":
                  case "demote":
                     this.addClanMembers(completions, playerClan, current, player.getName());
                     break;
                  case "info":
                  case "ally":
                  case "rival":
                  case "neutral":
                  case "war":
                     if (subCmd.equals("ally") || subCmd.equals("rival") || subCmd.equals("neutral") || subCmd.equals("war")) {
                        addMatching(completions, DIPLOMACY_ACTIONS, current);
                     }

                     this.addClanNames(completions, current, playerClan == null ? null : playerClan.getName());
               }

               return completions;
            } else {
               if (args.length == 3 && subCmd.equals("war")) {
                  addMatching(completions, AMOUNT_SUGGESTIONS, current);
               }

               return completions;
            }
         }
      }
   }

   private void addClanNames(List<String> completions, String current, String ownClanName) {
      for (String clanName : this.plugin.getClanManager().getClans().keySet()) {
         if ((ownClanName == null || !ownClanName.equalsIgnoreCase(clanName)) && clanName.toLowerCase().startsWith(current)) {
            completions.add(clanName);
         }
      }
   }

   private void addClanMembers(List<String> completions, Clan clan, String current, String executorName) {
      if (clan != null) {
         for (UUID uuid : clan.getMembers().keySet()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            String name = member.getName();
            if (name != null && !name.equalsIgnoreCase(executorName) && name.toLowerCase().startsWith(current)) {
               completions.add(name);
            }
         }
      }
   }

   private void addOnlinePlayers(List<String> completions, String current, Predicate<Player> filter) {
      for (Player online : Bukkit.getOnlinePlayers()) {
         if (filter.test(online)) {
            String name = online.getName();
            if (name.toLowerCase().startsWith(current)) {
               completions.add(name);
            }
         }
      }
   }

   private static void addMatching(List<String> completions, List<String> values, String current) {
      for (String value : values) {
         if (value.toLowerCase().startsWith(current)) {
            completions.add(value);
         }
      }
   }
}
