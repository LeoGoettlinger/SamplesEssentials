package su.samplecraft.essentials.modules.social;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code FriendZ-1.1.jar} (com.yourname.friendz.FriendZ).
 * <p>
 * Full port of the original behaviour and data model:
 * <ul>
 *   <li>{@code /friend add|remove|list [page]|requests|accept|deny|level}</li>
 *   <li>{@code /msf <player> <message>} direct messages with reply routing and
 *       {@code /msf toggle <player>} chat mode ({@code !exit} leaves it)</li>
 *   <li>friendship levels 1-10 with the level colours from the config</li>
 *   <li>join notifications for online friends and the sound set</li>
 *   <li>data stored in {@code social/friends/data/frienddata.yml} with the same
 *       {@code <uuid>.friends / .levels / .names} layout as the original, so an
 *       existing frienddata.yml can be copied over</li>
 * </ul>
 */
public final class FriendsModule extends EssentialsModule implements Listener {

    private final Map<UUID, List<UUID>> msgReplies = new HashMap<>();
    private final Map<UUID, Set<UUID>> friendRequests = new HashMap<>();
    private final Map<UUID, UUID> chatToggle = new HashMap<>();

    private ModuleStorage storage;
    private PlayerDirectory directory;

    public FriendsModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("social", "friends", "FriendZ-1.1.jar", "Friends, friend requests, /msf direct messages and friendship levels");
    }

    @Override
    protected void onEnable() {
        storage = new ModuleStorage(plugin, getDataFolder(), "frienddata.yml");
        directory = new PlayerDirectory(plugin);
        ensurePermission("samplese.friends", "Use the friend system", org.bukkit.permissions.PermissionDefault.TRUE);
        command("friend", new FriendCommand());
        command("msf", new MsfCommand());
        command("friendz", new InfoCommand());
        onDisableHook(() -> {
            unbindCommands();
            storage.save();
        });
        listen(this);
    }

    /* ------------------------------------------------------------------ */
    /* data helpers (same layout as the original frienddata.yml)           */
    /* ------------------------------------------------------------------ */

    private YamlConfiguration data() {
        return storage.yaml();
    }

    private List<String> friendIds(UUID player) {
        return new ArrayList<>(data().getStringList(player + ".friends"));
    }

    private void setFriendIds(UUID player, List<String> ids) {
        data().set(player + ".friends", ids);
        storage.saveLater();
    }

    private int level(UUID player, UUID friend) {
        return data().getInt(player + ".levels." + friend, 1);
    }

    private String levelColor(int level) {
        ConfigurationSection colors = config().getConfigurationSection("levels.colors");
        if (colors == null) {
            return level <= 3 ? "&a" : level <= 6 ? "&b" : level <= 9 ? "&d" : "&6";
        }
        // keys look like "1-3", "10" ...
        String best = null;
        for (String key : colors.getKeys(false)) {
            if (key.contains("-")) {
                String[] parts = key.split("-", 2);
                try {
                    int low = Integer.parseInt(parts[0].trim());
                    int high = Integer.parseInt(parts[1].trim());
                    if (level >= low && level <= high) {
                        return colors.getString(key, "&a");
                    }
                } catch (NumberFormatException ignored) {
                    // skip malformed key
                }
            } else {
                try {
                    if (Integer.parseInt(key.trim()) == level) {
                        return colors.getString(key, "&6");
                    }
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
            best = key;
        }
        return best == null ? "&a" : colors.getString(best, "&a");
    }

    private int maxFriends() {
        return Math.max(1, cfgInt("max-friends", 100));
    }

    private int perPage() {
        return Math.max(1, cfgInt("list.per-page", 8));
    }

    private String prefix() {
        return cfgString("prefix", "&2FriendZ &8▸ ");
    }

    private void msg(Player player, String raw) {
        player.sendMessage(Text.parse(prefix() + raw));
    }

    private void play(Player player, String path, String fallback) {
        String name = cfgString("sounds." + path, fallback);
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase(Locale.ROOT)), 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // unknown sound name in config
        }
    }

    /* ------------------------------------------------------------------ */
    /* /friend                                                             */
    /* ------------------------------------------------------------------ */

    private final class FriendCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.friends")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                msg(player, "<yellow>FriendZ Commands:");
                msg(player, "<green>/friend add <player> <gray>- Send friend request");
                msg(player, "<green>/friend remove <player> <gray>- Remove friend");
                msg(player, "<green>/friend list [page] <gray>- View friend list");
                msg(player, "<green>/friend requests <gray>- View friend requests");
                msg(player, "<green>/friend accept <player> <gray>- Accept friend request");
                msg(player, "<green>/friend deny <player> <gray>- Deny friend request");
                msg(player, "<green>/friend level <player> <1-10> <gray>- Set friendship level");
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "add" -> {
                    if (args.length < 2) {
                        msg(player, "<red>Usage: /friend add <player>");
                        return true;
                    }
                    sendRequest(player, args[1]);
                }
                case "remove" -> {
                    if (args.length < 2) {
                        msg(player, "<red>Usage: /friend remove <player>");
                        return true;
                    }
                    removeFriend(player, args[1]);
                }
                case "list" -> {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ignored) {
                            page = 1;
                        }
                    }
                    listFriends(player, page);
                }
                case "requests" -> listRequests(player);
                case "accept" -> {
                    if (args.length < 2) {
                        msg(player, "<red>Usage: /friend accept <player>");
                        return true;
                    }
                    accept(player, args[1]);
                }
                case "deny" -> {
                    if (args.length < 2) {
                        msg(player, "<red>Usage: /friend deny <player>");
                        return true;
                    }
                    deny(player, args[1]);
                }
                case "level" -> {
                    if (args.length < 3) {
                        msg(player, "<red>Usage: /friend level <player> <1-10>");
                        return true;
                    }
                    try {
                        setLevel(player, args[1], Integer.parseInt(args[2]));
                    } catch (NumberFormatException e) {
                        msg(player, "<red>Level must be a number between 1 and 10.");
                    }
                }
                default -> msg(player, "<red>Unknown command. Type '/friend' for help.");
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("add", "remove", "list", "requests", "accept", "deny", "level");
            }
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("level")) {
                return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
            }
            return List.of();
        }
    }

    private void listFriends(Player player, int page) {
        UUID id = player.getUniqueId();
        List<String> friends = friendIds(id);
        if (friends.isEmpty()) {
            msg(player, "<yellow>You don't have any friends added yet.");
            return;
        }
        Map<String, Integer> levels = new HashMap<>();
        for (String friend : friends) {
            levels.put(friend, level(id, UUID.fromString(friend)));
        }
        List<String> sorted = new ArrayList<>(friends);
        sorted.sort(Comparator.comparing(levels::get).reversed());
        int totalPages = (int) Math.ceil((double) sorted.size() / perPage());
        int current = Math.max(1, Math.min(page, Math.max(1, totalPages)));
        msg(player, "<aqua>===== Friends List (Page " + current + "/" + Math.max(1, totalPages) + ") =====");
        int start = (current - 1) * perPage();
        int end = Math.min(start + perPage(), sorted.size());
        for (int i = start; i < end; i++) {
            String friendId = sorted.get(i);
            int friendLevel = levels.getOrDefault(friendId, 1);
            String name = data().getString(id + ".names." + friendId, "Unknown");
            String color = levelColor(friendLevel);
            boolean online = directory.isOnline(UUID.fromString(friendId));
            StringBuilder stars = new StringBuilder();
            stars.append("★".repeat(Math.min(friendLevel, 10)));
            stars.append("☆".repeat(Math.max(0, 10 - friendLevel)));
            msg(player, color + name + " <gray>(" + (online ? "<green>Online" : "<red>Offline")
                    + "<gray>) - Level " + color + friendLevel + "<gray>/10 " + color + stars);
        }
        if (current < totalPages) {
            msg(player, "<yellow>Type <white>/friend list " + (current + 1) + "<yellow> to see more friends.");
        }
    }

    private void listRequests(Player player) {
        Set<UUID> requests = friendRequests.getOrDefault(player.getUniqueId(), Set.of());
        if (requests.isEmpty()) {
            msg(player, "<yellow>You have no pending friend requests.");
            return;
        }
        msg(player, "<aqua>===== Friend Requests (" + requests.size() + ") =====");
        for (UUID sender : requests) {
            String name = directory.nameOf(sender);
            if (name == null) {
                name = "Unknown";
            }
            String status = directory.isOnline(sender) ? "<green>Online" : "<red>Offline";
            msg(player, "<yellow>" + name + " <gray>(" + status + "<gray>) - <green>/friend accept " + name
                    + "<gray> | <red>/friend deny " + name);
        }
    }

    private void sendRequest(Player player, String targetName) {
        UUID id = player.getUniqueId();
        if (friendIds(id).size() >= maxFriends() && !player.hasPermission("samplese.friends.bypasslimit")) {
            msg(player, "<red>You've reached the maximum number of friends (" + maxFriends() + ").");
            return;
        }
        PlayerDirectory.Entry target = directory.find(targetName);
        if (target == null) {
            msg(player, "<red>Player not found.");
            return;
        }
        if (target.id().equals(id)) {
            msg(player, "<red>You cannot send a friend request to yourself.");
            return;
        }
        if (friendIds(id).contains(target.id().toString())) {
            msg(player, "<yellow>You are already friends with " + target.name() + ".");
            return;
        }
        Set<UUID> pending = friendRequests.computeIfAbsent(target.id(), key -> new HashSet<>());
        if (pending.contains(id)) {
            msg(player, "<yellow>You've already sent a friend request to " + target.name() + ".");
            return;
        }
        pending.add(id);
        msg(player, "<green>Friend request sent to <aqua>" + target.name() + "<green>!");
        play(player, "request-sent", "BLOCK_NOTE_BLOCK_PLING");
        Player online = plugin.getServer().getPlayer(target.id());
        if (online != null && online.isOnline()) {
            msg(online, "<aqua>" + player.getName() + "<green> has sent you a friend request!");
            msg(online, "<yellow>Type <white>/friend accept " + player.getName()
                    + "<yellow> or <white>/friend deny " + player.getName() + "<yellow>.");
            play(online, "request-received", "BLOCK_NOTE_BLOCK_CHIME");
        }
    }

    private void accept(Player player, String senderName) {
        PlayerDirectory.Entry sender = directory.find(senderName);
        if (sender == null) {
            msg(player, "<red>Player not found.");
            return;
        }
        Set<UUID> pending = friendRequests.get(player.getUniqueId());
        if (pending == null || !pending.contains(sender.id())) {
            msg(player, "<red>No friend request found from this player.");
            return;
        }
        pending.remove(sender.id());
        if (pending.isEmpty()) {
            friendRequests.remove(player.getUniqueId());
        }
        addFriendship(player.getUniqueId(), sender.id());
        addFriendship(sender.id(), player.getUniqueId());
        msg(player, "<green>You are now friends with <aqua>" + sender.name() + "<green>!");
        play(player, "friend-added", "ENTITY_EXPERIENCE_ORB_PICKUP");
        Player online = plugin.getServer().getPlayer(sender.id());
        if (online != null && online.isOnline()) {
            msg(online, "<aqua>" + player.getName() + "<green> accepted your friend request!");
            play(online, "friend-added", "ENTITY_EXPERIENCE_ORB_PICKUP");
        }
    }

    private void deny(Player player, String senderName) {
        PlayerDirectory.Entry sender = directory.find(senderName);
        if (sender == null) {
            msg(player, "<red>Player not found.");
            return;
        }
        Set<UUID> pending = friendRequests.get(player.getUniqueId());
        if (pending == null || !pending.contains(sender.id())) {
            msg(player, "<red>No friend request found from this player.");
            return;
        }
        pending.remove(sender.id());
        if (pending.isEmpty()) {
            friendRequests.remove(player.getUniqueId());
        }
        msg(player, "<yellow>You denied the friend request from <aqua>" + sender.name() + "<yellow>.");
        Player online = plugin.getServer().getPlayer(sender.id());
        if (online != null && online.isOnline()) {
            msg(online, "<aqua>" + player.getName() + "<yellow> denied your friend request.");
        }
    }

    private void setLevel(Player player, String friendName, int level) {
        if (level < 1 || level > 10) {
            msg(player, "<red>Level must be between 1 and 10.");
            return;
        }
        UUID id = player.getUniqueId();
        for (String friendId : friendIds(id)) {
            String saved = data().getString(id + ".names." + friendId);
            if (saved != null && saved.equalsIgnoreCase(friendName)) {
                data().set(id + ".levels." + friendId, level);
                storage.saveLater();
                String color = levelColor(level);
                msg(player, "<green>You've set your friendship level with " + color + friendName
                        + "<green> to " + color + level + "<green>.");
                return;
            }
        }
        msg(player, "<red>Friend not found.");
    }

    private void removeFriend(Player player, String friendName) {
        UUID id = player.getUniqueId();
        List<String> friends = friendIds(id);
        for (String friendId : new ArrayList<>(friends)) {
            String saved = data().getString(id + ".names." + friendId);
            if (saved != null && saved.equalsIgnoreCase(friendName)) {
                friends.remove(friendId);
                setFriendIds(id, friends);
                data().set(id + ".levels." + friendId, null);
                data().set(id + ".names." + friendId, null);
                try {
                    UUID other = UUID.fromString(friendId);
                    List<String> otherFriends = friendIds(other);
                    otherFriends.remove(id.toString());
                    data().set(other + ".friends", otherFriends);
                } catch (IllegalArgumentException ignored) {
                    // malformed uuid
                }
                storage.saveLater();
                msg(player, "<green>Removed <aqua>" + saved + "<green> from your friends.");
                play(player, "friend-removed", "BLOCK_NOTE_BLOCK_BASS");
                return;
            }
        }
        msg(player, "<red>Friend not found.");
    }

    private void addFriendship(UUID player, UUID friend) {
        List<String> friends = friendIds(player);
        if (!friends.contains(friend.toString())) {
            friends.add(friend.toString());
            data().set(player + ".friends", friends);
            data().set(player + ".levels." + friend, 1);
            String name = directory.nameOf(friend);
            data().set(player + ".names." + friend, name == null ? "Unknown" : name);
            storage.saveLater();
        }
    }

    /* ------------------------------------------------------------------ */
    /* /msf direct messages                                                */
    /* ------------------------------------------------------------------ */

    private final class MsfCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.friends")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                msg(player, "<red>Usage: /msf <player> <message> OR /msf toggle <player>");
                return true;
            }
            if (args[0].equalsIgnoreCase("toggle")) {
                if (args.length < 2) {
                    msg(player, "<red>Usage: /msf toggle <player>");
                    return true;
                }
                toggleChat(player, args[1]);
                return true;
            }
            if (args.length < 2) {
                msg(player, "<red>Usage: /msf <player> <message>");
                return true;
            }
            directMessage(player, args[0], String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>(plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
                names.add("toggle");
                return names;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return List.of();
        }
    }

    private void directMessage(Player sender, String targetName, String message) {
        PlayerDirectory.Entry target = directory.find(targetName);
        if (target == null) {
            msg(sender, "<red>Player not found.");
            return;
        }
        Player online = plugin.getServer().getPlayer(target.id());
        if (online == null || !online.isOnline()) {
            msg(sender, "<red>" + target.name() + " is not online.");
            return;
        }
        int friendLevel = level(sender.getUniqueId(), target.id());
        String color = levelColor(friendLevel);
        msg(sender, "<gray>[<aqua>You<gray> -> " + color + online.getName() + "<gray>] <white>" + message);
        msg(online, "<gray>[" + color + sender.getName() + "<gray> -> <aqua>You<gray>] <white>" + message);
        play(online, "message-received", "ENTITY_PLAYER_LEVELUP");
        msgReplies.computeIfAbsent(target.id(), key -> new ArrayList<>()).add(sender.getUniqueId());
    }

    private void toggleChat(Player player, String targetName) {
        UUID id = player.getUniqueId();
        if (chatToggle.containsKey(id)) {
            chatToggle.remove(id);
            msg(player, "<yellow>Chat toggle disabled. You're now back to normal chat.");
            return;
        }
        PlayerDirectory.Entry target = directory.find(targetName);
        if (target == null || !directory.isOnline(target.id())) {
            msg(player, "<red>Player not found or not online.");
            return;
        }
        if (!friendIds(id).contains(target.id().toString())) {
            msg(player, "<red>You can only chat toggle with your friends.");
            return;
        }
        chatToggle.put(id, target.id());
        String color = levelColor(level(id, target.id()));
        msg(player, "<green>Chat toggle enabled with " + color + target.name() + "<green>!");
        msg(player, "<yellow>All your messages will now be sent to them. Type <white>!exit<yellow> to disable.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID target = chatToggle.get(player.getUniqueId());
        if (target == null) {
            return;
        }
        String message = event.getMessage();
        event.setCancelled(true);
        if (message.equalsIgnoreCase("!exit")) {
            chatToggle.remove(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin, () -> msg(player,
                    "<yellow>Chat toggle disabled. You're now back to normal chat."));
            return;
        }
        Player online = plugin.getServer().getPlayer(target);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (online != null && online.isOnline()) {
                directMessage(player, online.getName(), message);
            } else {
                msg(player, "<red>Your chat partner is no longer online. Chat toggle disabled.");
                chatToggle.remove(player.getUniqueId());
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!cfgBool("notify-friend-join", true)) {
            return;
        }
        Player joined = event.getPlayer();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(joined)) {
                continue;
            }
            if (friendIds(online.getUniqueId()).contains(joined.getUniqueId().toString())) {
                msg(online, "<green>Your friend <aqua>" + joined.getName() + "<green> joined the server!");
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* /friendz                                                            */
    /* ------------------------------------------------------------------ */

    private final class InfoCommand implements org.bukkit.command.CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload") && player.hasPermission("samplese.admin")) {
                reloadConfig();
                storage.reload();
                player.sendMessage(plugin.getLang().component("reloaded"));
                return true;
            }
            msg(player, "<aqua>===== SamplesEssentials Friends =====");
            msg(player, "<green>Version: 1.0.0 (merged from FriendZ 1.1)");
            msg(player, "<green>Commands: /friend ..., /msf <player> <message>, /msf toggle <player>");
            return true;
        }
    }
}
