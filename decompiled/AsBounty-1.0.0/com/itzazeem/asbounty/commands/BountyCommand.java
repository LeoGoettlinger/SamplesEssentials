package com.itzazeem.asbounty.commands;

import com.itzazeem.asbounty.AsBounty;
import com.itzazeem.asbounty.gui.BountyGUI;
import com.itzazeem.asbounty.gui.ConfirmGUI;
import com.itzazeem.asbounty.manager.SignManager;
import com.itzazeem.asbounty.utils.MessageUtils;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class BountyCommand implements CommandExecutor, TabCompleter {
   private final AsBounty plugin;
   private SignManager signManager;

   public BountyCommand(AsBounty plugin) {
      this.plugin = plugin;
   }

   public void setSignManager(SignManager signManager) {
      this.signManager = signManager;
   }

   private String formatAmountWithAbbreviation(double amount) {
      boolean abbreviationsEnabled = this.plugin.getConfig().getBoolean("economy.abbreviations.enabled", true);
      if (!abbreviationsEnabled) {
         return MessageUtils.formatAmount(amount);
      } else {
         List<String> formats = this.plugin.getConfig().getStringList("economy.abbreviations.formats");
         if (amount >= 1.0E12 && formats.contains("T")) {
            return String.format("%.1fT", amount / 1.0E12);
         } else if (amount >= 1.0E9 && formats.contains("B")) {
            return String.format("%.1fB", amount / 1.0E9);
         } else if (amount >= 1000000.0 && formats.contains("M")) {
            return String.format("%.1fM", amount / 1000000.0);
         } else {
            return amount >= 1000.0 && formats.contains("K") ? String.format("%.1fK", amount / 1000.0) : MessageUtils.formatAmount(amount);
         }
      }
   }

   private double parseAmountWithAbbreviation(String input) throws NumberFormatException {
      if (input != null && !input.isEmpty()) {
         boolean abbreviationsEnabled = this.plugin.getConfig().getBoolean("economy.abbreviations.enabled", true);
         if (!abbreviationsEnabled) {
            return Double.parseDouble(input);
         } else {
            List<String> formats = this.plugin.getConfig().getStringList("economy.abbreviations.formats");
            String upperInput = input.toUpperCase();
            double multiplier = 1.0;
            String numberPart = input;

            for (String format : formats) {
               if (upperInput.endsWith(format)) {
                  switch (format) {
                     case "K":
                        multiplier = 1000.0;
                        break;
                     case "M":
                        multiplier = 1000000.0;
                        break;
                     case "B":
                        multiplier = 1.0E9;
                        break;
                     case "T":
                        multiplier = 1.0E12;
                  }

                  numberPart = input.substring(0, input.length() - format.length());
                  break;
               }
            }

            double number = Double.parseDouble(numberPart);
            return number * multiplier;
         }
      } else {
         throw new NumberFormatException("Empty input");
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length == 0) {
         if (sender instanceof Player player) {
            new BountyGUI(this.plugin, player, this.signManager).open();
         } else {
            sender.sendMessage(MessageUtils.colorize(this.plugin.getConfigManager().getMessage("player-only")));
         }

         return true;
      } else {
         String var6 = args[0].toLowerCase();
         switch (var6) {
            case "add":
               if (!(sender instanceof Player player)) {
                  sender.sendMessage(MessageUtils.colorize(this.plugin.getConfigManager().getMessage("player-only")));
                  return true;
               }

               if (!player.hasPermission("asbounty.use") && !player.hasPermission("bounty.use")) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("no-permission"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  this.plugin.getSoundManager().playSound(player, "no-perm");
                  return true;
               }

               if (args.length < 3) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     "&cUsage: /" + label + " add <player> <amount>",
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               String targetName = args[1];

               double amount;
               try {
                  amount = this.parseAmountWithAbbreviation(args[2]);
               } catch (NumberFormatException var21) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("invalid-amount"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               Player targetOnline = Bukkit.getPlayer(targetName);
               UUID targetUUID = null;
               if (targetOnline != null && targetOnline.isOnline()) {
                  targetUUID = targetOnline.getUniqueId();
                  String var27 = targetOnline.getName();
               } else {
                  OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
                  if (offlineTarget.hasPlayedBefore()) {
                     targetUUID = offlineTarget.getUniqueId();
                     if (offlineTarget.getName() != null) {
                        String var26 = offlineTarget.getName();
                     }
                  } else {
                     try (PreparedStatement stmt = this.plugin
                           .getHeadDatabaseConnection()
                           .prepareStatement("SELECT uuid, name FROM player_heads WHERE name LIKE ? OR uuid LIKE ? LIMIT 1")) {
                        stmt.setString(1, "%" + targetName + "%");
                        stmt.setString(2, "%" + targetName + "%");

                        try (ResultSet rs = stmt.executeQuery()) {
                           if (rs.next()) {
                              targetUUID = UUID.fromString(rs.getString("uuid"));
                              String finalTargetName = rs.getString("name");
                           }
                        }
                     } catch (SQLException var24) {
                        var24.printStackTrace();
                     }
                  }
               }

               if (targetUUID == null) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("player-not-found"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               if (player.getUniqueId().equals(targetUUID)) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("cannot-add-bounty-to-yourself"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               if (amount < this.plugin.getConfigManager().getMinAmount()) {
                  String msg = this.plugin
                     .getConfigManager()
                     .getMessage("amount-below-min")
                     .replace("%min_amount_format%", this.formatAmountWithAbbreviation(this.plugin.getConfigManager().getMinAmount()));
                  MessageUtils.sendMessageWithPrefix(player, msg, this.plugin.getConfigManager().isPrefixEnabled(), this.plugin.getConfigManager().getPrefix());
                  return true;
               }

               if (amount > this.plugin.getConfigManager().getMaxAmount()) {
                  String msg = this.plugin
                     .getConfigManager()
                     .getMessage("amount-above-max")
                     .replace("%max_amount_format%", this.formatAmountWithAbbreviation(this.plugin.getConfigManager().getMaxAmount()));
                  MessageUtils.sendMessageWithPrefix(player, msg, this.plugin.getConfigManager().isPrefixEnabled(), this.plugin.getConfigManager().getPrefix());
                  return true;
               }

               if (!this.plugin.hasEconomy()) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     "&cEconomy system is currently unavailable! Please ask an administrator to install an economy plugin (like EssentialsX).",
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               if (!this.plugin.getEconomy().has(player, amount)) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("not-enough-balance"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
                  return true;
               }

               if (targetOnline != null) {
                  new ConfirmGUI(this.plugin, player, targetOnline, amount).open();
               } else {
                  new ConfirmGUI(this.plugin, player, targetUUID, amount).open();
               }
               break;
            case "reload":
               if (!sender.hasPermission("asbounty.reload") && !sender.hasPermission("bounty.reload")) {
                  if (sender instanceof Player player) {
                     MessageUtils.sendMessageWithPrefix(
                        player,
                        this.plugin.getConfigManager().getMessage("no-permission"),
                        this.plugin.getConfigManager().isPrefixEnabled(),
                        this.plugin.getConfigManager().getPrefix()
                     );
                     this.plugin.getSoundManager().playSound(player, "no-perm");
                  } else {
                     sender.sendMessage(MessageUtils.colorize(this.plugin.getConfigManager().getMessage("no-permission")));
                  }

                  return true;
               }

               this.plugin.getConfigManager().reload();
               if (sender instanceof Player player) {
                  MessageUtils.sendMessageWithPrefix(
                     player,
                     this.plugin.getConfigManager().getMessage("reload"),
                     this.plugin.getConfigManager().isPrefixEnabled(),
                     this.plugin.getConfigManager().getPrefix()
                  );
               } else {
                  sender.sendMessage(MessageUtils.colorize(this.plugin.getConfigManager().getMessage("reload")));
               }

               this.plugin.getLogger().info("Reloaded configuration successfully.");
               break;
            default:
               if (sender instanceof Player player) {
                  new BountyGUI(this.plugin, player, this.signManager).open();
               } else {
                  sender.sendMessage(MessageUtils.colorize(this.plugin.getConfigManager().getMessage("player-only")));
               }
         }

         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("add");
         completions.add("reload");
         return completions.stream().filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());
      } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
         String input = args[1].toLowerCase();

         for (Player player : Bukkit.getOnlinePlayers()) {
            completions.add(player.getName());
         }

         try (
            PreparedStatement stmt = this.plugin.getHeadDatabaseConnection().prepareStatement("SELECT name FROM player_heads ORDER BY name ASC LIMIT 100");
            ResultSet rs = stmt.executeQuery();
         ) {
            while (rs.next()) {
               String name = rs.getString("name");
               if (!completions.contains(name)) {
                  completions.add(name);
               }
            }
         } catch (SQLException var15) {
         }

         return completions.stream().filter(namex -> namex.toLowerCase().startsWith(input)).distinct().limit(50L).collect(Collectors.toList());
      } else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
         List<String> suggestions = List.of("100", "500", "1000", "5000", "10k", "50k", "100k", "1m");
         return suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
      } else {
         return completions;
      }
   }
}
