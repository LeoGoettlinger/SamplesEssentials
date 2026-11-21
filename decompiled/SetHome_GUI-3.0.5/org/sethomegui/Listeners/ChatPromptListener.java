package org.sethomegui.Listeners;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Utils.Utils;

public class ChatPromptListener implements Listener {
   private static SetHomeGUI plugin;
   private static final Map<UUID, Long> awaitingHomeName = new ConcurrentHashMap<>();
   private static final Pattern HEX_CLEAN_PATTERN = Pattern.compile("&#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{6}");

   public ChatPromptListener(SetHomeGUI plugin) {
      ChatPromptListener.plugin = plugin;
   }

   public static void startPrompt(Player player) {
      UUID uuid = player.getUniqueId();
      long startTime = System.currentTimeMillis();
      awaitingHomeName.put(uuid, startTime);
      long timeoutSeconds = plugin.getMainConfig().getLong("chat-prompt-timeout", 60L);
      Bukkit.getAsyncScheduler()
         .runDelayed(
            plugin,
            task -> {
               if (awaitingHomeName.containsKey(uuid) && awaitingHomeName.get(uuid) == startTime) {
                  awaitingHomeName.remove(uuid);
                  if (!player.isOnline()) {
                     return;
                  }

                  String timeoutMsg = plugin.getMainConfig()
                     .getString("messages.home-creation-messages.creation-timeout", "&#ef6603[SetHomeGUI] &cTime expired! Home creation process cancelled.");
                  player.sendMessage(Utils.setPlaceholders(player, timeoutMsg, plugin));
               }
            },
            timeoutSeconds,
            TimeUnit.SECONDS
         );
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      UUID uuid = player.getUniqueId();
      if (awaitingHomeName.containsKey(uuid)) {
         event.setCancelled(true);
         String rawMessage = event.getMessage().trim();
         rawMessage = HEX_CLEAN_PATTERN.matcher(rawMessage).replaceAll("");
         rawMessage = ChatColor.stripColor(rawMessage);
         rawMessage = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', rawMessage));
         String message = rawMessage;
         String basePath = "messages.home-creation-messages.";
         String cancelWord = plugin.getMainConfig().getString(basePath + "cancel-word", "cancel");
         if (rawMessage.equalsIgnoreCase(cancelWord)) {
            awaitingHomeName.remove(uuid);
            String cancelMsg = plugin.getMainConfig().getString(basePath + "creation-cancelled");
            player.sendMessage(Utils.setPlaceholders(player, cancelMsg, plugin));
         } else {
            String regex = plugin.getMainConfig().getString("name-regex", "^[A-Za-z0-9\\s]{1,32}$");
            if (rawMessage.matches(regex) && !rawMessage.isEmpty()) {
               List<String> blacklistedWords = plugin.getMainConfig().getStringList("blacklisted-words");
               if (blacklistedWords != null) {
                  for (String word : blacklistedWords) {
                     if (message.toLowerCase().contains(word.toLowerCase())) {
                        String blacklistMsg = plugin.getMainConfig()
                           .getString(basePath + "blacklisted-name", "&#ef6603[SetHomeGUI] &cError: The home name contains a blacklisted word!");
                        player.sendMessage(Utils.setPlaceholders(player, blacklistMsg, plugin));
                        return;
                     }
                  }
               }

               String currentWorld = player.getWorld().getName();
               List<String> blacklistedWorlds = plugin.getMainConfig().getStringList("blacklisted-worlds");
               if (blacklistedWorlds != null && blacklistedWorlds.contains(currentWorld)) {
                  String worldMsg = plugin.getMainConfig().getString(basePath + "world-blacklisted");
                  worldMsg = worldMsg.replace("%world%", "&f" + currentWorld);
                  player.sendMessage(Utils.setPlaceholders(player, worldMsg, plugin));
                  awaitingHomeName.remove(uuid);
               } else {
                  int currentHomes = plugin.getHomeManager().getHomeCount(uuid);
                  int maxHomes = plugin.getHomeManager().getPlayerMaxHomes(player);
                  boolean homeExists = plugin.getHomeManager().getPlayerFile(uuid).getStringList("homes").contains(message);
                  if (!homeExists && maxHomes != -1 && currentHomes >= maxHomes) {
                     String limitMsg = plugin.getMainConfig().getString(basePath + "limit-reached");
                     limitMsg = limitMsg.replace("%max%", "&f" + maxHomes);
                     player.sendMessage(Utils.setPlaceholders(player, limitMsg, plugin));
                  } else {
                     awaitingHomeName.remove(uuid);
                     Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
                        plugin.getHomeManager().saveHome(uuid, message, player.getLocation());
                        String successMsg = plugin.getMainConfig().getString(basePath + "home-saved");
                        successMsg = successMsg.replace("%name%", "&f" + message);
                        player.sendMessage(Utils.setPlaceholders(player, successMsg, plugin));
                     });
                  }
               }
            } else {
               String errorMsg = plugin.getMainConfig().getString(basePath + "invalid-name");
               errorMsg = errorMsg.replace("%cancel_word%", "&f" + cancelWord);
               player.sendMessage(Utils.setPlaceholders(player, errorMsg, plugin));
            }
         }
      }
   }
}
