package com.yourname.friendz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FriendZ extends JavaPlugin implements Listener {
   private int dependencyHooks;
   private File friendDataFile;
   private FileConfiguration friendData;
   private String prefix;
   private Map<UUID, List<UUID>> msgReplies = new HashMap<>();
   private Map<UUID, Set<UUID>> friendRequests = new HashMap<>();
   private Map<UUID, UUID> chatToggle = new HashMap<>();
   private boolean notifyFriendJoin;
   private boolean enableGui;
   private boolean miniMessageEnabled;
   private int maxFriends;
   private int friendsPerPage;
   private FriendZGUI guiHandler;
   private String friendAddedSound;
   private String friendRemovedSound;
   private String messageReceivedSound;
   private String requestSentSound;
   private String requestReceivedSound;
   private MiniMessage miniMessage;

   public void onEnable() {
      this.saveDefaultConfig();
      this.loadConfig();
      if (this.miniMessageEnabled) {
         this.miniMessage = MiniMessage.miniMessage();
      }

      this.loadFriendData();
      if (this.enableGui) {
         this.guiHandler = new FriendZGUI(this);
      }

      this.getServer().getPluginManager().registerEvents(this, this);
      this.getCommand("friend").setExecutor(this);
      this.getCommand("msf").setExecutor(this);
      this.getCommand("friendz").setExecutor(this);
      this.getLogger()
         .info(
            "FriendZ has been enabled! GUI Mode: "
               + (this.enableGui ? "Enabled" : "Disabled")
               + ", MiniMessage: "
               + (this.miniMessageEnabled ? "Enabled" : "Disabled")
         );
   }

   public void onDisable() {
      this.saveFriendData();
      this.getLogger().info("FriendZ has been disabled!");
   }

   private void loadConfig() {
      FileConfiguration config = this.getConfig();
      String rawPrefix = config.getString("prefix", "<aqua>[FriendZ]</aqua> ");
      this.miniMessageEnabled = config.getBoolean("minimessage.enabled", true);
      this.prefix = this.formatMessage(rawPrefix);
      this.notifyFriendJoin = config.getBoolean("notify-friend-join", true);
      this.enableGui = config.getBoolean("enable-gui", true);
      this.maxFriends = config.getInt("max-friends", 100);
      this.friendsPerPage = config.getInt("list.per-page", 8);
      this.friendAddedSound = config.getString("sounds.friend-added", "ENTITY_EXPERIENCE_ORB_PICKUP");
      this.friendRemovedSound = config.getString("sounds.friend-removed", "BLOCK_NOTE_BLOCK_BASS");
      this.messageReceivedSound = config.getString("sounds.message-received", "ENTITY_PLAYER_LEVELUP");
      this.requestSentSound = config.getString("sounds.request-sent", "BLOCK_NOTE_BLOCK_PLING");
      this.requestReceivedSound = config.getString("sounds.request-received", "BLOCK_NOTE_BLOCK_CHIME");
   }

   private String formatMessage(String message) {
      this.dependencyHooks = this.setupDependencyHooks();
      if (this.miniMessageEnabled && this.miniMessage != null) {
         try {
            Component component = this.miniMessage.deserialize(message);
            return LegacyComponentSerializer.legacySection().serialize(component);
         } catch (Exception var3) {
            return ChatColor.translateAlternateColorCodes('&', message);
         }
      } else {
         return ChatColor.translateAlternateColorCodes('&', message);
      }
   }

   private void sendMessage(Player player, String message) {
      player.sendMessage(this.formatMessage(message));
   }

   private void loadFriendData() {
      this.friendDataFile = new File(this.getDataFolder(), "frienddata.yml");
      if (!this.friendDataFile.exists()) {
         this.friendDataFile.getParentFile().mkdirs();
         this.saveResource("frienddata.yml", false);
      }

      this.friendData = YamlConfiguration.loadConfiguration(this.friendDataFile);
   }

   private void saveFriendData() {
      try {
         this.friendData.save(this.friendDataFile);
      } catch (IOException var2) {
         this.getLogger().severe("Could not save friend data!");
         var2.printStackTrace();
      }
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      if (this.notifyFriendJoin) {
         Player joiningPlayer = event.getPlayer();
         UUID joiningPlayerUUID = joiningPlayer.getUniqueId();

         for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(joiningPlayer) && this.areFriends(onlinePlayer.getUniqueId(), joiningPlayerUUID)) {
               int level = this.getFriendshipLevel(onlinePlayer.getUniqueId(), joiningPlayerUUID);
               String levelColor = this.getFriendshipLevelColor(level);
               this.sendMessage(onlinePlayer, this.prefix + "<green>Your friend " + levelColor + joiningPlayer.getName() + "<green> just logged in!");
               this.sendMessage(joiningPlayer, this.prefix + "<green>Your friend " + levelColor + onlinePlayer.getName() + "<green> is currently online!");
            }
         }
      }
   }

   @EventHandler
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      Player player = event.getPlayer();
      if (this.chatToggle.containsKey(player.getUniqueId())) {
         UUID targetUUID = this.chatToggle.get(player.getUniqueId());
         Player targetPlayer = Bukkit.getPlayer(targetUUID);
         if (targetPlayer != null && targetPlayer.isOnline()) {
            event.setCancelled(true);
            String message = event.getMessage();
            if (!message.equalsIgnoreCase("!exit") && !message.equalsIgnoreCase("!quit") && !message.equalsIgnoreCase("!stop")) {
               Bukkit.getScheduler().runTask(this, () -> this.sendDirectMessage(player, targetPlayer, message));
               return;
            }

            this.chatToggle.remove(player.getUniqueId());
            this.sendMessage(player, this.prefix + "<yellow>Chat toggle disabled. You're now back to normal chat.");
            return;
         }

         this.chatToggle.remove(player.getUniqueId());
         this.sendMessage(player, this.prefix + "<red>Your chat friend went offline. Chat toggle disabled.");
      }

      if (this.enableGui && this.guiHandler != null && this.guiHandler.hasPendingInput(player)) {
         event.setCancelled(true);
         String inputType = this.guiHandler.getPendingInputType(player);
         String message = event.getMessage();
         Bukkit.getScheduler().runTask(this, () -> {
            if (inputType.equals("ADD_FRIEND")) {
               this.handleSendFriendRequest(player, message);
            } else if (inputType.equals("REMOVE_FRIEND")) {
               this.handleRemoveFriend(player, message);
            } else if (inputType.startsWith("MESSAGE:")) {
               String targetUUID = inputType.substring(8);
               this.handleMessageFriendGUI(player, targetUUID, message);
            }

            this.guiHandler.removePendingInput(player);
         });
      }
   }

   private boolean areFriends(UUID player1, UUID player2) {
      List<String> player1Friends = this.friendData.getStringList(player1.toString() + ".friends");
      return player1Friends.contains(player2.toString());
   }

   private int getFriendshipLevel(UUID player1, UUID player2) {
      return this.friendData.getInt(player1.toString() + ".levels." + player2.toString(), 1);
   }

   public String getFriendshipLevelColor(int level) {
      String colorTag;
      if (level >= 10) {
         colorTag = this.getConfig().getString("levels.colors.10", "<gold>");
      } else if (level >= 7) {
         colorTag = this.getConfig().getString("levels.colors.7-9", "<light_purple>");
      } else if (level >= 4) {
         colorTag = this.getConfig().getString("levels.colors.4-6", "<aqua>");
      } else {
         colorTag = this.getConfig().getString("levels.colors.1-3", "<green>");
      }

      return this.miniMessageEnabled
         ? colorTag
         : colorTag.replace("<green>", "&a").replace("<aqua>", "&b").replace("<light_purple>", "&d").replace("<gold>", "&6");
   }

   public ChatColor getFriendshipLevelColorLegacy(int level) {
      if (level >= 10) {
         return ChatColor.GOLD;
      } else if (level >= 7) {
         return ChatColor.LIGHT_PURPLE;
      } else {
         return level >= 4 ? ChatColor.AQUA : ChatColor.GREEN;
      }
   }

   private void playSound(Player player, String soundName) {
      if (soundName != null && !soundName.equalsIgnoreCase("NONE")) {
         try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
         } catch (IllegalArgumentException var4) {
         }
      }
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.formatMessage(this.prefix + "<red>This command can only be used by players."));
         return true;
      } else if (command.getName().equalsIgnoreCase("friend")) {
         if (this.enableGui && this.guiHandler != null) {
            this.guiHandler.openMainMenu(player);
            return true;
         } else {
            return this.handleFriendCommand(player, args);
         }
      } else if (command.getName().equalsIgnoreCase("msf")) {
         if (args.length == 0) {
            this.sendMessage(player, this.prefix + "<red>Usage: /msf <player> <message> OR /msf toggle <player>");
            return true;
         } else if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length < 2) {
               this.sendMessage(player, this.prefix + "<red>Usage: /msf toggle <player>");
               return true;
            } else {
               return this.handleChatToggle(player, args[1]);
            }
         } else if (args.length < 2) {
            this.sendMessage(player, this.prefix + "<red>Usage: /msf <player> <message>");
            return true;
         } else {
            String targetName = args[0];
            StringBuilder message = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
               message.append(args[i]).append(" ");
            }

            return this.handleMessageFriendCommand(player, targetName, message.toString().trim());
         }
      } else {
         if (command.getName().equalsIgnoreCase("friendz")) {
            if (args.length == 0) {
               this.sendMessage(player, "<aqua>===== FriendZ Plugin =====");
               this.sendMessage(player, "<green>Version: " + this.getDescription().getVersion());
               this.sendMessage(player, "<green>GUI Mode: " + (this.enableGui ? "Enabled" : "Disabled"));
               this.sendMessage(player, "<green>MiniMessage: " + (this.miniMessageEnabled ? "Enabled" : "Disabled"));
               if (this.enableGui) {
                  this.sendMessage(player, "<green>Commands: /friend (opens GUI), /msf <player> <message>, /msf toggle <player>");
                  this.sendMessage(player, "<green>Type '/friend' to open the GUI");
               } else {
                  this.sendMessage(player, "<green>Commands: /friend <add|remove|list> [player], /msf <player> <message>, /msf toggle <player>");
                  this.sendMessage(player, "<green>Type '/friend help' for detailed command usage");
               }

               return true;
            }

            if (args[0].equalsIgnoreCase("reload") && player.hasPermission("friendz.admin")) {
               this.reloadConfig();
               this.loadConfig();
               this.saveFriendData();
               this.loadFriendData();
               if (this.enableGui && this.guiHandler == null) {
                  this.guiHandler = new FriendZGUI(this);
               } else if (!this.enableGui) {
                  this.guiHandler = null;
               }

               this.sendMessage(player, this.prefix + "<green>FriendZ configuration reloaded!");
               return true;
            }
         }

         return false;
      }
   }

   private boolean handleFriendCommand(Player player, String[] args) {
      if (args.length == 0) {
         this.sendMessage(player, this.prefix + "<yellow>FriendZ Commands:");
         this.sendMessage(player, "<green>/friend add <player> <gray>- Send friend request");
         this.sendMessage(player, "<green>/friend remove <player> <gray>- Remove friend");
         this.sendMessage(player, "<green>/friend list [page] <gray>- View friend list");
         this.sendMessage(player, "<green>/friend requests <gray>- View friend requests");
         this.sendMessage(player, "<green>/friend accept <player> <gray>- Accept friend request");
         this.sendMessage(player, "<green>/friend deny <player> <gray>- Deny friend request");
         this.sendMessage(player, "<green>/friend level <player> <1-10> <gray>- Set friendship level");
         return true;
      } else {
         String subCommand = args[0].toLowerCase();
         switch (subCommand) {
            case "add":
               if (args.length < 2) {
                  this.sendMessage(player, this.prefix + "<red>Usage: /friend add <player>");
                  return true;
               }

               return this.handleSendFriendRequest(player, args[1]);
            case "remove":
               if (args.length < 2) {
                  this.sendMessage(player, this.prefix + "<red>Usage: /friend remove <player>");
                  return true;
               }

               return this.handleRemoveFriend(player, args[1]);
            case "list":
               int page = 1;
               if (args.length > 1) {
                  try {
                     page = Integer.parseInt(args[1]);
                  } catch (NumberFormatException var9) {
                     page = 1;
                  }
               }

               return this.handleListFriends(player, page);
            case "requests":
               return this.handleListRequests(player);
            case "accept":
               if (args.length < 2) {
                  this.sendMessage(player, this.prefix + "<red>Usage: /friend accept <player>");
                  return true;
               }

               return this.handleAcceptRequest(player, args[1]);
            case "deny":
               if (args.length < 2) {
                  this.sendMessage(player, this.prefix + "<red>Usage: /friend deny <player>");
                  return true;
               }

               return this.handleDenyRequest(player, args[1]);
            case "level":
               if (args.length < 3) {
                  this.sendMessage(player, this.prefix + "<red>Usage: /friend level <player> <1-10>");
                  return true;
               } else {
                  try {
                     int level = Integer.parseInt(args[2]);
                     return this.handleSetLevel(player, args[1], level);
                  } catch (NumberFormatException var8) {
                     this.sendMessage(player, this.prefix + "<red>Level must be a number between 1 and 10.");
                     return true;
                  }
               }
            default:
               this.sendMessage(player, this.prefix + "<red>Unknown command. Type '/friend' for help.");
               return true;
         }
      }
   }

   private boolean handleListFriends(Player player, int page) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      if (currentFriends.isEmpty()) {
         this.sendMessage(player, this.prefix + "<yellow>You don't have any friends added yet.");
         return true;
      } else {
         Map<String, Integer> friendLevels = new HashMap<>();

         for (String friendUUID : currentFriends) {
            int level = this.friendData.getInt(playerUUID.toString() + ".levels." + friendUUID, 1);
            friendLevels.put(friendUUID, level);
         }

         List<String> sortedFriends = currentFriends.stream().sorted(Comparator.comparing(friendLevels::get).reversed()).collect(Collectors.toList());
         int totalPages = (int)Math.ceil((double)sortedFriends.size() / this.friendsPerPage);
         if (page > totalPages) {
            page = totalPages;
         }

         if (page < 1) {
            page = 1;
         }

         this.sendMessage(player, "<aqua>===== Friends List (Page " + page + "/" + totalPages + ") =====");
         int startIndex = (page - 1) * this.friendsPerPage;
         int endIndex = Math.min(startIndex + this.friendsPerPage, sortedFriends.size());

         for (int i = startIndex; i < endIndex; i++) {
            String friendUUID = sortedFriends.get(i);
            int level = friendLevels.get(friendUUID);
            String name = this.friendData.getString(playerUUID.toString() + ".names." + friendUUID, "Unknown");
            String levelColor = this.getFriendshipLevelColor(level);
            boolean isOnline = Bukkit.getPlayer(UUID.fromString(friendUUID)) != null;
            String status = isOnline ? "<green>Online" : "<red>Offline";
            String stars = "";

            for (int j = 0; j < Math.min(level, 10); j++) {
               stars = stars + "★";
            }

            for (int j = level; j < 10; j++) {
               stars = stars + "☆";
            }

            this.sendMessage(player, levelColor + name + " <gray>(" + status + "<gray>) - Level " + levelColor + level + "<gray>/10 " + levelColor + stars);
         }

         if (page < totalPages) {
            this.sendMessage(player, "<yellow>Type <white>/friend list " + (page + 1) + "<yellow> to see more friends.");
         }

         return true;
      }
   }

   private boolean handleListRequests(Player player) {
      Set<UUID> requests = this.friendRequests.getOrDefault(player.getUniqueId(), new HashSet<>());
      if (requests.isEmpty()) {
         this.sendMessage(player, this.prefix + "<yellow>You have no pending friend requests.");
         return true;
      } else {
         this.sendMessage(player, "<aqua>===== Friend Requests (" + requests.size() + ") =====");

         for (UUID senderUUID : requests) {
            OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(senderUUID);
            String senderName = senderPlayer.getName() != null ? senderPlayer.getName() : "Unknown";
            boolean isOnline = Bukkit.getPlayer(senderUUID) != null;
            String status = isOnline ? "<green>Online" : "<red>Offline";
            this.sendMessage(
               player,
               "<yellow>" + senderName + " <gray>(" + status + "<gray>) - <green>/friend accept " + senderName + "<gray> | <red>/friend deny " + senderName
            );
         }

         return true;
      }
   }

   private boolean handleAcceptRequest(Player player, String senderName) {
      UUID senderUUID = this.findPlayerUUID(senderName);
      if (senderUUID == null) {
         this.sendMessage(player, this.prefix + "<red>Player not found.");
         return true;
      } else {
         return this.handleAcceptFriendRequest(player, senderUUID);
      }
   }

   private boolean handleDenyRequest(Player player, String senderName) {
      UUID senderUUID = this.findPlayerUUID(senderName);
      if (senderUUID == null) {
         this.sendMessage(player, this.prefix + "<red>Player not found.");
         return true;
      } else {
         return this.handleDenyFriendRequest(player, senderUUID);
      }
   }

   private boolean handleSetLevel(Player player, String friendName, int level) {
      if (level >= 1 && level <= 10) {
         UUID playerUUID = player.getUniqueId();
         List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
         String targetUUID = null;

         for (String friendUUID : currentFriends) {
            String savedName = this.friendData.getString(playerUUID.toString() + ".names." + friendUUID);
            if (savedName != null && savedName.equalsIgnoreCase(friendName)) {
               targetUUID = friendUUID;
               break;
            }
         }

         if (targetUUID == null) {
            this.sendMessage(player, this.prefix + "<red>Friend not found.");
            return true;
         } else {
            this.friendData.set(playerUUID.toString() + ".levels." + targetUUID, level);
            this.saveFriendData();
            String levelColor = this.getFriendshipLevelColor(level);
            this.sendMessage(
               player,
               this.prefix + "<green>You've set your friendship level with " + levelColor + friendName + "<green> to " + levelColor + level + "<green>."
            );
            return true;
         }
      } else {
         this.sendMessage(player, this.prefix + "<red>Level must be between 1 and 10.");
         return true;
      }
   }

   private UUID findPlayerUUID(String playerName) {
      Player targetPlayer = Bukkit.getPlayer(playerName);
      if (targetPlayer != null) {
         return targetPlayer.getUniqueId();
      } else {
         for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
               return offlinePlayer.getUniqueId();
            }
         }

         return null;
      }
   }

   public boolean handleSendFriendRequest(Player player, String targetName) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      if (currentFriends.size() >= this.maxFriends && !player.hasPermission("friendz.bypasslimit")) {
         this.sendMessage(player, this.prefix + "<red>You've reached the maximum number of friends (" + this.maxFriends + ").");
         return true;
      } else {
         UUID targetUUID = null;
         String exactName = null;
         Player targetPlayer = Bukkit.getPlayer(targetName);
         if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
            exactName = targetPlayer.getName();
         } else {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
               if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                  targetUUID = offlinePlayer.getUniqueId();
                  exactName = offlinePlayer.getName();
                  break;
               }
            }
         }

         if (targetUUID == null) {
            this.sendMessage(player, this.prefix + "<red>Player not found.");
            return true;
         } else if (targetUUID.equals(playerUUID)) {
            this.sendMessage(player, this.prefix + "<red>You cannot send a friend request to yourself.");
            return true;
         } else if (currentFriends.contains(targetUUID.toString())) {
            this.sendMessage(player, this.prefix + "<yellow>You are already friends with " + exactName + ".");
            return true;
         } else {
            Set<UUID> targetRequests = this.friendRequests.computeIfAbsent(targetUUID, k -> new HashSet<>());
            if (targetRequests.contains(playerUUID)) {
               this.sendMessage(player, this.prefix + "<yellow>You've already sent a friend request to " + exactName + ".");
               return true;
            } else {
               targetRequests.add(playerUUID);
               this.sendMessage(player, this.prefix + "<green>Friend request sent to <aqua>" + exactName + "<green>!");
               this.playSound(player, this.requestSentSound);
               if (targetPlayer != null && targetPlayer.isOnline()) {
                  this.sendMessage(targetPlayer, this.prefix + "<aqua>" + player.getName() + "<green> has sent you a friend request!");
                  if (this.enableGui) {
                     this.sendMessage(targetPlayer, this.prefix + "<yellow>Type <white>/friend<yellow> to accept or deny requests.");
                  } else {
                     this.sendMessage(
                        targetPlayer,
                        this.prefix
                           + "<yellow>Type <white>/friend accept "
                           + player.getName()
                           + "<yellow> or <white>/friend deny "
                           + player.getName()
                           + "<yellow>."
                     );
                  }

                  this.playSound(targetPlayer, this.requestReceivedSound);
               }

               return true;
            }
         }
      }
   }

   public boolean handleAcceptFriendRequest(Player player, UUID senderUUID) {
      UUID playerUUID = player.getUniqueId();
      Set<UUID> playerRequests = this.friendRequests.get(playerUUID);
      if (playerRequests != null && playerRequests.contains(senderUUID)) {
         playerRequests.remove(senderUUID);
         if (playerRequests.isEmpty()) {
            this.friendRequests.remove(playerUUID);
         }

         this.addFriendship(playerUUID, senderUUID);
         this.addFriendship(senderUUID, playerUUID);
         OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(senderUUID);
         String senderName = senderPlayer.getName() != null ? senderPlayer.getName() : "Unknown";
         this.sendMessage(player, this.prefix + "<green>You are now friends with <aqua>" + senderName + "<green>!");
         this.playSound(player, this.friendAddedSound);
         Player onlineSender = Bukkit.getPlayer(senderUUID);
         if (onlineSender != null && onlineSender.isOnline()) {
            this.sendMessage(onlineSender, this.prefix + "<aqua>" + player.getName() + "<green> accepted your friend request!");
            this.playSound(onlineSender, this.friendAddedSound);
         }

         return true;
      } else {
         this.sendMessage(player, this.prefix + "<red>No friend request found from this player.");
         return false;
      }
   }

   public boolean handleDenyFriendRequest(Player player, UUID senderUUID) {
      UUID playerUUID = player.getUniqueId();
      Set<UUID> playerRequests = this.friendRequests.get(playerUUID);
      if (playerRequests != null && playerRequests.contains(senderUUID)) {
         playerRequests.remove(senderUUID);
         if (playerRequests.isEmpty()) {
            this.friendRequests.remove(playerUUID);
         }

         OfflinePlayer senderPlayer = Bukkit.getOfflinePlayer(senderUUID);
         String senderName = senderPlayer.getName() != null ? senderPlayer.getName() : "Unknown";
         this.sendMessage(player, this.prefix + "<yellow>You denied the friend request from <aqua>" + senderName + "<yellow>.");
         Player onlineSender = Bukkit.getPlayer(senderUUID);
         if (onlineSender != null && onlineSender.isOnline()) {
            this.sendMessage(onlineSender, this.prefix + "<aqua>" + player.getName() + "<yellow> denied your friend request.");
         }

         return true;
      } else {
         this.sendMessage(player, this.prefix + "<red>No friend request found from this player.");
         return false;
      }
   }

   private void addFriendship(UUID player1, UUID player2) {
      List<String> friends = this.friendData.getStringList(player1.toString() + ".friends");
      if (!friends.contains(player2.toString())) {
         friends.add(player2.toString());
         this.friendData.set(player1.toString() + ".friends", friends);
         this.friendData.set(player1.toString() + ".levels." + player2.toString(), 1);
         OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player2);
         String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
         this.friendData.set(player1.toString() + ".names." + player2.toString(), name);
         this.saveFriendData();
      }
   }

   private boolean handleChatToggle(Player player, String targetName) {
      UUID playerUUID = player.getUniqueId();
      if (this.chatToggle.containsKey(playerUUID)) {
         this.chatToggle.remove(playerUUID);
         this.sendMessage(player, this.prefix + "<yellow>Chat toggle disabled. You're now back to normal chat.");
         return true;
      } else {
         Player targetPlayer = Bukkit.getPlayer(targetName);
         UUID targetUUID = null;
         if (targetPlayer != null && targetPlayer.isOnline()) {
            targetUUID = targetPlayer.getUniqueId();
         } else {
            for (String friendUUID : this.friendData.getStringList(playerUUID.toString() + ".friends")) {
               String savedName = this.friendData.getString(playerUUID.toString() + ".names." + friendUUID);
               if (savedName != null && savedName.equalsIgnoreCase(targetName)) {
                  Player possibleTarget = Bukkit.getPlayer(UUID.fromString(friendUUID));
                  if (possibleTarget != null && possibleTarget.isOnline()) {
                     targetPlayer = possibleTarget;
                     targetUUID = possibleTarget.getUniqueId();
                     break;
                  }
               }
            }
         }

         if (targetUUID == null) {
            this.sendMessage(player, this.prefix + "<red>Player not found or not online.");
            return true;
         } else {
            List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
            if (!currentFriends.contains(targetUUID.toString())) {
               this.sendMessage(player, this.prefix + "<red>You can only chat toggle with your friends.");
               return true;
            } else {
               this.chatToggle.put(playerUUID, targetUUID);
               int level = this.getFriendshipLevel(playerUUID, targetUUID);
               String levelColor = this.getFriendshipLevelColor(level);
               this.sendMessage(player, this.prefix + "<green>Chat toggle enabled with " + levelColor + targetPlayer.getName() + "<green>!");
               this.sendMessage(player, this.prefix + "<yellow>All your messages will now be sent to them. Type <white>!exit<yellow> to disable.");
               return true;
            }
         }
      }
   }

   private void sendDirectMessage(Player sender, Player target, String message) {
      UUID senderUUID = sender.getUniqueId();
      UUID targetUUID = target.getUniqueId();
      int level = this.getFriendshipLevel(senderUUID, targetUUID);
      String levelColor = this.getFriendshipLevelColor(level);
      this.sendMessage(sender, "<gray>[<aqua>You<gray> → " + levelColor + target.getName() + "<gray>] <white>" + message);
      this.sendMessage(target, "<gray>[" + levelColor + sender.getName() + "<gray> → <aqua>You<gray>] <white>" + message);
      this.playSound(target, this.messageReceivedSound);
      List<UUID> targetReplies = this.msgReplies.computeIfAbsent(targetUUID, k -> new ArrayList<>());
      targetReplies.add(senderUUID);
   }

   public boolean handleAddFriend(Player player, String targetName) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      if (currentFriends.size() >= this.maxFriends && !player.hasPermission("friendz.bypasslimit")) {
         this.sendMessage(player, this.prefix + "<red>You've reached the maximum number of friends (" + this.maxFriends + ").");
         return true;
      } else {
         UUID targetUUID = null;
         String exactName = null;
         Player targetPlayer = Bukkit.getPlayer(targetName);
         if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
            exactName = targetPlayer.getName();
         } else {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
               if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                  targetUUID = offlinePlayer.getUniqueId();
                  exactName = offlinePlayer.getName();
                  break;
               }
            }
         }

         if (targetUUID == null) {
            this.sendMessage(player, this.prefix + "<red>Player not found.");
            return true;
         } else if (targetUUID.equals(playerUUID)) {
            this.sendMessage(player, this.prefix + "<red>You cannot add yourself as a friend.");
            return true;
         } else if (currentFriends.contains(targetUUID.toString())) {
            this.sendMessage(player, this.prefix + "<yellow>You are already friends with " + exactName + ".");
            return true;
         } else {
            currentFriends.add(targetUUID.toString());
            this.friendData.set(playerUUID.toString() + ".friends", currentFriends);
            this.friendData.set(playerUUID.toString() + ".levels." + targetUUID.toString(), 1);
            if (this.friendData.getString(playerUUID.toString() + ".names." + targetUUID.toString()) == null) {
               this.friendData.set(playerUUID.toString() + ".names." + targetUUID.toString(), exactName);
            }

            this.saveFriendData();
            this.sendMessage(player, this.prefix + "<green>You are now friends with <aqua>" + exactName + "<green>!");
            this.playSound(player, this.friendAddedSound);
            if (targetPlayer != null && targetPlayer.isOnline()) {
               this.sendMessage(targetPlayer, this.prefix + "<aqua>" + player.getName() + "<green> has added you as a friend!");
               this.playSound(targetPlayer, this.friendAddedSound);
            }

            return true;
         }
      }
   }

   public boolean handleRemoveFriend(Player player, String targetName) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      UUID targetUUID = null;
      String exactName = null;

      for (String friendUUID : currentFriends) {
         String savedName = this.friendData.getString(playerUUID.toString() + ".names." + friendUUID);
         if (savedName != null && savedName.equalsIgnoreCase(targetName)) {
            targetUUID = UUID.fromString(friendUUID);
            exactName = savedName;
            break;
         }
      }

      if (targetUUID == null) {
         Player targetPlayer = Bukkit.getPlayer(targetName);
         if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
            exactName = targetPlayer.getName();
         } else {
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
               if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                  targetUUID = offlinePlayer.getUniqueId();
                  exactName = offlinePlayer.getName();
                  break;
               }
            }
         }
      }

      if (targetUUID == null) {
         this.sendMessage(player, this.prefix + "<red>Friend not found.");
         return true;
      } else if (!currentFriends.contains(targetUUID.toString())) {
         this.sendMessage(player, this.prefix + "<red>You are not friends with " + targetName + ".");
         return true;
      } else {
         currentFriends.remove(targetUUID.toString());
         this.friendData.set(playerUUID.toString() + ".friends", currentFriends);
         this.friendData.set(playerUUID.toString() + ".levels." + targetUUID.toString(), null);
         this.saveFriendData();
         this.sendMessage(player, this.prefix + "<yellow>You are no longer friends with <aqua>" + exactName + "<yellow>.");
         this.playSound(player, this.friendRemovedSound);
         Player targetPlayer = Bukkit.getPlayer(targetUUID);
         if (targetPlayer != null && targetPlayer.isOnline()) {
            this.sendMessage(targetPlayer, this.prefix + "<aqua>" + player.getName() + "<yellow> has removed you from their friends list.");
         }

         return true;
      }
   }

   public void handleRemoveFriendGUI(Player player, String targetUUID, String friendName) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      currentFriends.remove(targetUUID);
      this.friendData.set(playerUUID.toString() + ".friends", currentFriends);
      this.friendData.set(playerUUID.toString() + ".levels." + targetUUID, null);
      this.saveFriendData();
      this.sendMessage(player, this.prefix + "<yellow>You are no longer friends with <aqua>" + friendName + "<yellow>.");
      this.playSound(player, this.friendRemovedSound);
      Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetUUID));
      if (targetPlayer != null && targetPlayer.isOnline()) {
         this.sendMessage(targetPlayer, this.prefix + "<aqua>" + player.getName() + "<yellow> has removed you from their friends list.");
      }
   }

   public void handleRankFriendGUI(Player player, String targetUUID, int level) {
      UUID playerUUID = player.getUniqueId();
      this.friendData.set(playerUUID.toString() + ".levels." + targetUUID, level);
      this.saveFriendData();
      String friendName = this.friendData.getString(playerUUID.toString() + ".names." + targetUUID, "Unknown");
      String levelColor = this.getFriendshipLevelColor(level);
      this.sendMessage(
         player, this.prefix + "<green>You've set your friendship level with " + levelColor + friendName + "<green> to " + levelColor + level + "<green>."
      );
   }

   public void handleMessageFriendGUI(Player player, String targetUUID, String message) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      if (!currentFriends.contains(targetUUID)) {
         this.sendMessage(player, this.prefix + "<red>You are not friends with this player.");
      } else {
         Player targetPlayer = Bukkit.getPlayer(UUID.fromString(targetUUID));
         if (targetPlayer != null && targetPlayer.isOnline()) {
            int level = this.getFriendshipLevel(playerUUID, UUID.fromString(targetUUID));
            String levelColor = this.getFriendshipLevelColor(level);
            this.sendMessage(player, "<gray>[<aqua>You<gray> → " + levelColor + targetPlayer.getName() + "<gray>] <white>" + message);
            this.sendMessage(targetPlayer, "<gray>[" + levelColor + player.getName() + "<gray> → <aqua>You<gray>] <white>" + message);
            this.playSound(targetPlayer, this.messageReceivedSound);
            List<UUID> playerReplies = this.msgReplies.computeIfAbsent(UUID.fromString(targetUUID), k -> new ArrayList<>());
            playerReplies.add(playerUUID);
         } else {
            this.sendMessage(player, this.prefix + "<red>This friend is not online.");
         }
      }
   }

   private boolean handleMessageFriendCommand(Player player, String targetName, String message) {
      UUID playerUUID = player.getUniqueId();
      List<String> currentFriends = this.friendData.getStringList(playerUUID.toString() + ".friends");
      Player targetPlayer = null;
      if (targetName.equalsIgnoreCase("r")) {
         List<UUID> replyTargets = this.msgReplies.get(playerUUID);
         if (replyTargets == null || replyTargets.isEmpty()) {
            this.sendMessage(player, this.prefix + "<red>You have no one to reply to.");
            return true;
         }

         UUID lastMessageUUID = replyTargets.get(replyTargets.size() - 1);
         targetPlayer = Bukkit.getPlayer(lastMessageUUID);
         if (targetPlayer == null || !targetPlayer.isOnline()) {
            this.sendMessage(player, this.prefix + "<red>The player you last messaged is no longer online.");
            return true;
         }
      } else {
         targetPlayer = Bukkit.getPlayer(targetName);
         if (targetPlayer == null) {
            for (String friendUUID : currentFriends) {
               String savedName = this.friendData.getString(playerUUID.toString() + ".names." + friendUUID);
               if (savedName != null && savedName.equalsIgnoreCase(targetName)) {
                  Player possibleTarget = Bukkit.getPlayer(UUID.fromString(friendUUID));
                  if (possibleTarget != null && possibleTarget.isOnline()) {
                     targetPlayer = possibleTarget;
                     break;
                  }
               }
            }
         }
      }

      if (targetPlayer == null) {
         this.sendMessage(player, this.prefix + "<red>Player not found or not online.");
         return true;
      } else {
         UUID targetUUID = targetPlayer.getUniqueId();
         if (!currentFriends.contains(targetUUID.toString())) {
            this.sendMessage(player, this.prefix + "<red>You can only message your friends. Add them first with '/friend add " + targetPlayer.getName() + "'");
            return true;
         } else {
            int level = this.getFriendshipLevel(playerUUID, targetUUID);
            String levelColor = this.getFriendshipLevelColor(level);
            this.sendMessage(player, "<gray>[<aqua>You<gray> → " + levelColor + targetPlayer.getName() + "<gray>] <white>" + message);
            this.sendMessage(targetPlayer, "<gray>[" + levelColor + player.getName() + "<gray> → <aqua>You<gray>] <white>" + message);
            this.playSound(targetPlayer, this.messageReceivedSound);
            List<UUID> playerReplies = this.msgReplies.computeIfAbsent(targetUUID, k -> new ArrayList<>());
            playerReplies.add(playerUUID);
            return true;
         }
      }
   }

   public FileConfiguration getFriendData() {
      return this.friendData;
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void saveFriendDataPublic() {
      this.saveFriendData();
   }

   public boolean isGuiEnabled() {
      return this.enableGui;
   }

   public Set<UUID> getPendingRequests(UUID playerUUID) {
      return this.friendRequests.getOrDefault(playerUUID, new HashSet<>());
   }

   public boolean hasPendingRequests(UUID playerUUID) {
      Set<UUID> requests = this.friendRequests.get(playerUUID);
      return requests != null && !requests.isEmpty();
   }
}
