package org.sethomegui.Commands;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Listeners.ChatPromptListener;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.Utils.Utils;

public class SetHomeCommand implements CommandExecutor {
   private final SetHomeGUI plugin;

   public SetHomeCommand(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         String onlyPlayersMsg = this.plugin
            .getMainConfig()
            .getString("messages.only-players", "&#ef6603[SetHomeGUI+] &cError: Only players can execute this command.");
         sender.sendMessage(Utils.color(onlyPlayersMsg));
         return true;
      } else {
         String basePath = "messages.home-creation-messages.";
         YamlDocument config = this.plugin.getMainConfig();
         String currentWorld = player.getWorld().getName();
         List<String> blacklistedWorlds = config.getStringList("blacklisted-worlds");
         if (blacklistedWorlds != null && blacklistedWorlds.contains(currentWorld)) {
            String worldMsg = config.getString(basePath + "world-blacklisted", "&#ef6603[SetHomeGUI+] &cYou cannot set a home in the world &f%world%&#ef6603!");
            worldMsg = worldMsg.replace("%world%", currentWorld);
            player.sendMessage(Utils.setPlaceholders(player, worldMsg, this.plugin));
            return true;
         } else {
            int currentHomes = this.plugin.getHomeManager().getHomeCount(player.getUniqueId());
            int maxHomes = this.plugin.getHomeManager().getPlayerMaxHomes(player);
            if (args.length == 0) {
               if (maxHomes != -1 && currentHomes >= maxHomes) {
                  String limitMsg = config.getString(
                     basePath + "limit-reached", "&#ef6603[SetHomeGUI+] &cYou have reached your maximum limit of &f%max% &chomes!"
                  );
                  limitMsg = limitMsg.replace("%max%", String.valueOf(maxHomes));
                  player.sendMessage(Utils.setPlaceholders(player, limitMsg, this.plugin));
                  return true;
               } else {
                  String cancelWord = config.getString(basePath + "cancel-word", "cancel");
                  String promptMsg = config.getString(
                     basePath + "name-prompt",
                     "&#ef6603[SetHomeGUI+] &#f9a805Please type the name for your home in the chat... Or type &f%cancel_word% &#f9a805to exit."
                  );
                  promptMsg = promptMsg.replace("%cancel_word%", cancelWord);
                  player.sendMessage(Utils.setPlaceholders(player, promptMsg, this.plugin));
                  ChatPromptListener.startPrompt(player);
                  return true;
               }
            } else {
               StringBuilder builder = new StringBuilder();

               for (int i = 0; i < args.length; i++) {
                  builder.append(args[i]);
                  if (i < args.length - 1) {
                     builder.append(" ");
                  }
               }

               String rawHomeName = builder.toString().trim();
               rawHomeName = ChatColor.stripColor(rawHomeName);
               rawHomeName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rawHomeName));
               String homeName = rawHomeName;
               List<String> blacklist = this.plugin.getMainConfig().getStringList("blacklisted-words");
               if (blacklist != null) {
                  for (String word : blacklist) {
                     if (homeName.toLowerCase().contains(word.toLowerCase())) {
                        String blacklistMsg = this.plugin
                           .getMainConfig()
                           .getString(
                              "messages.home-creation-messages.blacklisted-name", "&#ef6603[SetHomeGUI+] &cError: The home name contains a blacklisted word!"
                           );
                        player.sendMessage(Utils.color(blacklistMsg));
                        return true;
                     }
                  }
               }

               String regex = config.getString("name-regex", "^[A-Za-z0-9\\s]{1,32}$");
               if (homeName.matches(regex) && !homeName.isEmpty()) {
                  boolean homeExists = this.plugin.getHomeManager().getPlayerFile(player.getUniqueId()).getStringList("homes").contains(homeName);
                  if (!homeExists && maxHomes != -1 && currentHomes >= maxHomes) {
                     String limitMsg = config.getString(
                        basePath + "limit-reached", "&#ef6603[SetHomeGUI+] &cYou have reached your maximum limit of &f%max% &chomes!"
                     );
                     limitMsg = limitMsg.replace("%max%", String.valueOf(maxHomes));
                     player.sendMessage(Utils.setPlaceholders(player, limitMsg, this.plugin));
                     return true;
                  } else {
                     boolean isNewRegistration = !homeExists;
                     Bukkit.getRegionScheduler()
                        .execute(
                           this.plugin,
                           player.getLocation(),
                           () -> {
                              this.plugin.getHomeManager().saveHome(player.getUniqueId(), homeName, player.getLocation());
                              if (isNewRegistration && this.plugin.getAdminGUIManager() != null) {
                                 this.plugin.getAdminGUIManager().registerPlayerInCache(player);
                              }

                              String successMsg = config.getString(
                                 basePath + "home-saved", "&#ef6603[SetHomeGUI+] &#f9a805Home &f%name% &#f9a805has been successfully saved!"
                              );
                              successMsg = successMsg.replace("%name%", homeName);
                              player.sendMessage(Utils.setPlaceholders(player, successMsg, this.plugin));
                           }
                        );
                     return true;
                  }
               } else {
                  String errorMsg = config.getString(
                     basePath + "invalid-name",
                     "&#ef6603[SetHomeGUI+] &cInvalid name! &#f9a805Please avoid using spaces, dots, or slashes. Try again or type &f%cancel_word% &#f9a805to exit."
                  );
                  String cancelWord = config.getString(basePath + "cancel-word", "cancel");
                  errorMsg = errorMsg.replace("%cancel_word%", cancelWord);
                  player.sendMessage(Utils.setPlaceholders(player, errorMsg, this.plugin));
                  return true;
               }
            }
         }
      }
   }
}
