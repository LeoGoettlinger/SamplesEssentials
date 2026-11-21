package vrx.economy.vRXClans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ClanAdminCommand implements CommandExecutor, TabCompleter {
   private static final List<String> ROOT_SUBCOMMANDS = Arrays.asList(
      "reload", "save", "setholo", "endseason", "spy", "disband", "rename", "setleader", "forcejoin", "setlevel", "addxp", "eco", "inspect", "editor", "vault"
   );
   private final VRXClans plugin;

   public ClanAdminCommand(VRXClans plugin) {
      this.plugin = plugin;
   }

   public List<String> getRootTabCompletions() {
      return ROOT_SUBCOMMANDS;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("vrxclans.admin")) {
         sender.sendMessage(this.plugin.getLang().get("msg.no-permission"));
         return true;
      } else if (args.length == 0) {
         this.sendAdminHelp(sender);
         return true;
      } else {
         String subCommand = args[0].toLowerCase();
         ClanManager manager = this.plugin.getClanManager();
         switch (subCommand) {
            case "reload":
               this.plugin.reloadPluginConfigs();
               sender.sendMessage(this.plugin.getLang().get("admin.reload"));
               break;
            case "save":
               manager.saveClansSync();
               sender.sendMessage(this.plugin.getLang().get("admin.save"));
               break;
            case "spy":
               if (!(sender instanceof Player p)) {
                  sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
                  return true;
               }

               boolean isNowSpying = manager.toggleSpy(p);
               if (isNowSpying) {
                  p.sendMessage(this.plugin.getLang().get("admin.spy-on"));
               } else {
                  p.sendMessage(this.plugin.getLang().get("admin.spy-off"));
               }
               break;
            case "setholo":
               if (!(sender instanceof Player)) {
                  sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
                  return true;
               }

               if (this.plugin.getHologramManager() == null) {
                  sender.sendMessage("§cDecentHolograms is not installed!");
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage("§cUsage: /clana setholo <kills|level|bank>");
                  return true;
               }

               String type = args[1].toLowerCase();
               if (!Arrays.asList("kills", "level", "bank").contains(type)) {
                  sender.sendMessage("§cTypen: kills, level, bank");
                  return true;
               }

               this.plugin.getHologramManager().setHologram(type, ((Player)sender).getLocation());
               sender.sendMessage("§aHologram for " + type + " has been set here!");
               break;
            case "endseason":
               List<Clan> allClans = new ArrayList<>(manager.getClans().values());
               if (allClans.isEmpty()) {
                  sender.sendMessage("§cNo Clans vorhanden.");
                  return true;
               }

               allClans.sort((a, b) -> Integer.compare(b.getKills(), a.getKills()));
               Clan winner = allClans.get(0);
               double reward = this.plugin.getConfig().getDouble("season-reward", 50000.0);
               winner.deposit(reward);
               String msg = "§8§m--------------------------------\n§6§l\ud83c\udfc6 SEASON END \ud83c\udfc6\n§7Der Clan §b"
                  + winner.getName()
                  + " §7hat die Season gewonnen!\n§7Reward: §a"
                  + reward
                  + "$ §7has been added to the clan bank.\n§cAll kill statistics have been reset!\n§8§m--------------------------------";
               Bukkit.broadcastMessage(msg);
               this.plugin.sendDiscordWebhook("war", "%winner%", winner.getName(), "%loser%", "allen anderen Clans", "%pot%", String.valueOf(reward));

               for (Clan c : manager.getClans().values()) {
                  c.setKills(0);
                  c.setDeaths(0);
               }

               manager.saveClansSync();
               if (this.plugin.getHologramManager() != null) {
                  this.plugin.getHologramManager().updateHolograms();
               }
               break;
            case "disband":
               if (args.length < 2) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin disband <ClanName>"));
                  return true;
               }

               Clan targetDisband = manager.getClanByName(args[1]);
               if (targetDisband == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               String disbandedName = targetDisband.getName();
               manager.disbandClan(targetDisband);
               Bukkit.broadcastMessage(this.plugin.getLang().get("admin.disband", "%clan%", disbandedName));
               break;
            case "rename":
               if (args.length < 3) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin rename <Alt> <Neu>"));
                  return true;
               }

               Clan targetRename = manager.getClanByName(args[1]);
               if (targetRename == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               String oldName = targetRename.getName();
               String newName = args[2];
               if (manager.renameClan(targetRename, newName)) {
                  Bukkit.broadcastMessage(this.plugin.getLang().get("admin.rename", "%old%", oldName, "%new%", newName));
               } else {
                  sender.sendMessage(this.plugin.getLang().get("msg.name-taken"));
               }
               break;
            case "setleader":
               if (args.length < 3) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin setleader <ClanName> <Player>"));
                  return true;
               }

               Clan targetLeaderClan = manager.getClanByName(args[1]);
               if (targetLeaderClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               Player newLeader = Bukkit.getPlayerExact(args[2]);
               if (newLeader == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               if (!targetLeaderClan.equals(manager.getClanByPlayer(newLeader))) {
                  manager.removePlayerFromClan(newLeader);
                  manager.addPlayerToClan(newLeader, targetLeaderClan);
               }

               for (Entry<UUID, Clan.ClanRank> entry : targetLeaderClan.getMembers().entrySet()) {
                  if (entry.getValue() == Clan.ClanRank.LEADER) {
                     targetLeaderClan.setRank(entry.getKey(), Clan.ClanRank.CO_LEADER);
                  }
               }

               targetLeaderClan.setRank(newLeader.getUniqueId(), Clan.ClanRank.LEADER);
               manager.logClanEvent(targetLeaderClan, ClanLogEntry.LogType.MEMBER_PROMOTE, sender.getName(), newLeader.getName(), 0.0, "Admin set the leader");
               sender.sendMessage(this.plugin.getLang().get("admin.setleader", "%player%", newLeader.getName(), "%clan%", targetLeaderClan.getName()));
               newLeader.sendMessage(this.plugin.getLang().get("admin.setleader-target"));
               break;
            case "forcejoin":
               if (args.length < 3) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin forcejoin <ClanName> <Player>"));
                  return true;
               }

               Clan targetForceClan = manager.getClanByName(args[1]);
               if (targetForceClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               Player targetForce = Bukkit.getPlayerExact(args[2]);
               if (targetForce == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               manager.removePlayerFromClan(targetForce);
               manager.setJoinCooldown(targetForce, 0);
               manager.addPlayerToClan(targetForce, targetForceClan);
               sender.sendMessage(this.plugin.getLang().get("admin.forcejoin", "%player%", targetForce.getName(), "%clan%", targetForceClan.getName()));
               targetForce.sendMessage(this.plugin.getLang().get("admin.forcejoin-target", "%clan%", targetForceClan.getName()));
               break;
            case "setlevel":
               if (args.length < 3) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin setlevel <Clan> <Level>"));
                  return true;
               }

               Clan targetLevelClan = manager.getClanByName(args[1]);
               if (targetLevelClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               try {
                  int lvl = Integer.parseInt(args[2]);
                  if (lvl < 1) {
                     sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                     return true;
                  }

                  targetLevelClan.setLevel(lvl);
                  sender.sendMessage(this.plugin.getLang().get("admin.setlevel", "%clan%", targetLevelClan.getName(), "%level%", String.valueOf(lvl)));
               } catch (NumberFormatException var35) {
                  sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
               }
               break;
            case "addxp":
               if (args.length < 3) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clanadmin addxp <Clan> <XP>"));
                  return true;
               }

               Clan targetXpClan = manager.getClanByName(args[1]);
               if (targetXpClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               try {
                  double xp = Double.parseDouble(args[2]);
                  if (Double.isFinite(xp) && !(xp <= 0.0)) {
                     if (targetXpClan.addXp(xp, this.plugin)) {
                        this.plugin.handleClanLevelUps(targetXpClan);
                     }

                     sender.sendMessage(this.plugin.getLang().get("admin.addxp", "%xp%", String.valueOf(xp), "%clan%", targetXpClan.getName()));
                     break;
                  }

                  sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  return true;
               } catch (NumberFormatException var37) {
                  sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  break;
               }
            case "eco":
               if (args.length < 4) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clana eco <Clan> <add|remove|set> <Amount>"));
                  return true;
               }

               Clan targetEcoClan = manager.getClanByName(args[1]);
               if (targetEcoClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               String action = args[2].toLowerCase();

               try {
                  double amount = Double.parseDouble(args[3]);
                  if (Double.isFinite(amount) && !(amount < 0.0)) {
                     if (action.equals("add")) {
                        targetEcoClan.deposit(amount);
                        manager.logClanEvent(targetEcoClan, ClanLogEntry.LogType.BANK_DEPOSIT, sender.getName(), "AdminEco", amount, "Admin-Einzahlung");
                        sender.sendMessage(this.plugin.getLang().get("admin.eco-add", "%amount%", String.valueOf(amount)));
                     } else if (action.equals("remove")) {
                        if (targetEcoClan.withdraw(amount)) {
                           manager.logClanEvent(targetEcoClan, ClanLogEntry.LogType.BANK_WITHDRAW, sender.getName(), "AdminEco", amount, "Admin-Auszahlung");
                           sender.sendMessage(this.plugin.getLang().get("admin.eco-remove", "%amount%", String.valueOf(amount)));
                        } else {
                           sender.sendMessage(this.plugin.getLang().get("msg.eco-not-enough"));
                        }
                     } else if (action.equals("set")) {
                        targetEcoClan.setBankBalance(amount);
                        manager.logClanEvent(
                           targetEcoClan, ClanLogEntry.LogType.BANK_DEPOSIT, sender.getName(), "AdminEco", amount, "Admin changed the bank balance"
                        );
                        sender.sendMessage(this.plugin.getLang().get("admin.eco-set", "%amount%", String.valueOf(amount)));
                     } else {
                        sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clana eco <Clan> <add|remove|set> <Amount>"));
                     }
                     break;
                  }

                  sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  return true;
               } catch (NumberFormatException var36) {
                  sender.sendMessage(this.plugin.getLang().get("msg.eco-invalid-number"));
                  break;
               }
            case "inspect":
               if (!(sender instanceof Player inspector)) {
                  sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
                  return true;
               }

               if (args.length >= 2) {
                  Clan inspectClan = manager.getClanByName(args[1]);
                  if (inspectClan == null) {
                     sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                     return true;
                  }

                  ClanGUI.openAdminInspectMenu(inspector, inspectClan, this.plugin);
               } else {
                  ClanGUI.openAdminInspectList(inspector, this.plugin);
               }
               break;
            case "editor":
               if (!(sender instanceof Player editor)) {
                  sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clana editor <Clan>"));
                  return true;
               }

               Clan editorClan = manager.getClanByName(args[1]);
               if (editorClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               ClanGUI.openAdminEditorMenu(editor, editorClan, this.plugin);
               break;
            case "vault":
               if (!(sender instanceof Player)) {
                  sender.sendMessage(this.plugin.getLang().get("msg.only-players"));
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.plugin.getLang().get("msg.invalid-args", "%usage%", "/clana vault <Clan>"));
                  return true;
               }

               Clan targetVaultClan = manager.getClanByName(args[1]);
               if (targetVaultClan == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.player-not-found"));
                  return true;
               }

               Inventory vault = targetVaultClan.getVault(this.plugin);
               if (vault == null) {
                  sender.sendMessage(this.plugin.getLang().get("msg.not-officer"));
                  return true;
               }

               ((Player)sender).openInventory(vault);
               sender.sendMessage(this.plugin.getLang().get("admin.vault", "%clan%", targetVaultClan.getName()));
               break;
            default:
               this.sendAdminHelp(sender);
         }

         return true;
      }
   }

   private void sendAdminHelp(CommandSender sender) {
      sender.sendMessage("§8§m--------§r §cVRX Clans Admin §8§m--------");
      sender.sendMessage("§e/clana reload §7- Reload Configs");
      sender.sendMessage("§e/clana save §7- Save Data");
      sender.sendMessage("§e/clana setholo <kills|level|bank> §7- Leaderboard");
      sender.sendMessage("§e/clana endseason §7- Reset Kills & Belohnung");
      sender.sendMessage("§e/clana spy §7- Spy Mode");
      sender.sendMessage("§e/clana disband <Clan> §7- Disband Clan");
      sender.sendMessage("§e/clana rename <Clan> <Neu> §7- Rename");
      sender.sendMessage("§e/clana setleader <Clan> <Player> §7- Set Leader");
      sender.sendMessage("§e/clana forcejoin <Clan> <Player> §7- Force Join");
      sender.sendMessage("§e/clana setlevel <Clan> <Level> §7- Set Level");
      sender.sendMessage("§e/clana addxp <Clan> <XP> §7- Add XP");
      sender.sendMessage("§e/clana eco <Clan> <add/remove/set> <Amount> §7- Eco");
      sender.sendMessage("§e/clana inspect [Clan] §7- Inspect GUI");
      sender.sendMessage("§e/clana vault <Clan> §7- Open Vault");
      sender.sendMessage("§8§m-----------------------------");
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (!sender.hasPermission("vrxclans.admin")) {
         return completions;
      } else {
         if (args.length == 1) {
            for (String subCmd : ROOT_SUBCOMMANDS) {
               if (subCmd.startsWith(args[0].toLowerCase())) {
                  completions.add(subCmd);
               }
            }
         } else if (args.length == 2) {
            if (Arrays.asList("disband", "rename", "setleader", "forcejoin", "setlevel", "addxp", "eco", "inspect", "editor", "vault")
               .contains(args[0].toLowerCase())) {
               for (String clanName : this.plugin.getClanManager().getClans().keySet()) {
                  if (clanName.toLowerCase().startsWith(args[1].toLowerCase())) {
                     completions.add(clanName);
                  }
               }
            } else if (args[0].equalsIgnoreCase("setholo")) {
               for (String type : Arrays.asList("kills", "level", "bank")) {
                  if (type.startsWith(args[1].toLowerCase())) {
                     completions.add(type);
                  }
               }
            }
         } else if (args.length == 3 && args[0].equalsIgnoreCase("eco")) {
            for (String action : Arrays.asList("add", "remove", "set")) {
               if (action.startsWith(args[2].toLowerCase())) {
                  completions.add(action);
               }
            }
         }

         return completions;
      }
   }
}
