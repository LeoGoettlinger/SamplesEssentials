package su.samplecraft.essentials.modules.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code AdvancedHunt-4.0.4.jar} (de.theredend2000.advancedhunt).
 * <p>
 * Treasure-hunt minigame: admins define <em>collections</em>, place treasure
 * blocks inside them, and players hunt them down for rewards. Kept from the
 * original:
 * <ul>
 *   <li>collection model (name, enabled, single-player-find, hide-after-found),</li>
 *   <li>treasure = world block + member rewards,</li>
 *   <li>place mode ({@code /treasure place <collection>}) turning blocks into treasures,</li>
 *   <li>interaction-based finding with per-player progress, rewards and broadcast,</li>
 *   <li>particle markers so unfound treasures stay visible,</li>
 *   <li>hint system revealing an X or Z coordinate of a nearby unfound treasure,</li>
 *   <li>leaderboard per collection.</li>
 * </ul>
 * Storage is YAML ({@code collections.yml}, {@code players.yml}) instead of the
 * original SQLite/MySQL stack, which keeps the module dependency-free.
 * <p>
 * Commands ({@code /treasure}, aliases {@code ah}, {@code ahunt}):
 * {@code create/delete/list/enable/disable/place/remove/info/stats/top/hint/particles}.
 */
public final class AdvancedHuntModule extends EssentialsModule implements Listener {

    private static final class Treasure {
        final String collection;
        final String world;
        final int x;
        final int y;
        final int z;
        final Material material;

        Treasure(String collection, Location location) {
            this.collection = collection;
            this.world = location.getWorld().getName();
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
            this.material = location.getBlock().getType();
        }

        Treasure(String collection, String world, int x, int y, int z, Material material) {
            this.collection = collection;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }

        String key() {
            return world + ":" + x + ":" + y + ":" + z;
        }

        Location location() {
            World bukkit = org.bukkit.Bukkit.getWorld(world);
            return bukkit == null ? null : new Location(bukkit, x, y, z);
        }
    }

    private static final class Collection {
        String name;
        boolean enabled = true;
        boolean singlePlayerFind = false;
        boolean hideAfterFound = false;
        final List<String> commands = new ArrayList<>();
        final List<String> messages = new ArrayList<>();

        Collection(String name) {
            this.name = name;
        }
    }

    private final Map<String, Collection> collections = new LinkedHashMap<>();
    /** treasure key -> treasure */
    private final Map<String, Treasure> treasures = new LinkedHashMap<>();
    /** collection name -> player uuid -> found keys */
    private final Map<String, Map<UUID, Set<String>>> found = new HashMap<>();
    private final Map<UUID, String> placeMode = new HashMap<>();
    private final Map<UUID, Boolean> particles = new HashMap<>();
    private final Map<UUID, Long> hintCooldown = new HashMap<>();

    public AdvancedHuntModule(SamplesEssentials plugin) {
        super("world", "treasure", "AdvancedHunt-4.0.4.jar",
                "Treasure-hunt collections with place mode, particles, hints and rewards");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.treasure", "Play the treasure hunt", PermissionDefault.TRUE);
        ensurePermission("samplese.treasure.admin", "Manage treasure collections", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDefaultFile("collections.yml");
        ensureDefaultFile("players.yml");
        load();
        command("treasure", new TreasureCommand());
        command("ah", new TreasureCommand());
        command("ahunt", new TreasureCommand());
        onDisableHook(() -> {
            unbindCommands();
            save();
        });
        listen(this);
        long ticks = Math.max(20L, cfgInt("particles.interval-ticks", 20));
        syncRepeat(ticks, ticks, this::drawParticles);
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private File collectionsFile() {
        return new File(getModuleFolder(), "collections.yml");
    }

    private File playersFile() {
        return new File(getModuleFolder(), "players.yml");
    }

    private void load() {
        collections.clear();
        treasures.clear();
        found.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(collectionsFile());
        ConfigurationSection root = yaml.getConfigurationSection("collections");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection section = root.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                Collection collection = new Collection(section.getString("name", key));
                collection.enabled = section.getBoolean("enabled", true);
                collection.singlePlayerFind = section.getBoolean("single-player-find", false);
                collection.hideAfterFound = section.getBoolean("hide-after-found", false);
                collection.commands.addAll(section.getStringList("rewards.commands"));
                collection.messages.addAll(section.getStringList("rewards.messages"));
                collections.put(collection.name.toLowerCase(Locale.ROOT), collection);
                ConfigurationSection treasureRoot = section.getConfigurationSection("treasures");
                if (treasureRoot == null) {
                    continue;
                }
                for (String treasureKey : treasureRoot.getKeys(false)) {
                    ConfigurationSection treasure = treasureRoot.getConfigurationSection(treasureKey);
                    if (treasure == null) {
                        continue;
                    }
                    Material material;
                    try {
                        material = Material.valueOf(treasure.getString("material", "CHEST"));
                    } catch (IllegalArgumentException e) {
                        material = Material.CHEST;
                    }
                    Treasure entry = new Treasure(collection.name,
                            treasure.getString("world"), treasure.getInt("x"), treasure.getInt("y"),
                            treasure.getInt("z"), material);
                    treasures.put(entry.key(), entry);
                }
            }
        }
        YamlConfiguration players = YamlConfiguration.loadConfiguration(playersFile());
        ConfigurationSection foundRoot = players.getConfigurationSection("found");
        if (foundRoot != null) {
            for (String collectionName : foundRoot.getKeys(false)) {
                ConfigurationSection perPlayer = foundRoot.getConfigurationSection(collectionName);
                if (perPlayer == null) {
                    continue;
                }
                Map<UUID, Set<String>> map = new HashMap<>();
                for (String uuidText : perPlayer.getKeys(false)) {
                    try {
                        map.put(UUID.fromString(uuidText),
                                new LinkedHashSet<>(perPlayer.getStringList(uuidText)));
                    } catch (IllegalArgumentException ignored) {
                        // skip malformed uuid
                    }
                }
                found.put(collectionName, map);
            }
        }
        plugin.getLogger().info("Treasure collections loaded: " + collections.size()
                + " (" + treasures.size() + " treasures)");
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Collection collection : collections.values()) {
            String base = "collections." + collection.name.toLowerCase(Locale.ROOT) + ".";
            yaml.set(base + "name", collection.name);
            yaml.set(base + "enabled", collection.enabled);
            yaml.set(base + "single-player-find", collection.singlePlayerFind);
            yaml.set(base + "hide-after-found", collection.hideAfterFound);
            yaml.set(base + "rewards.commands", collection.commands);
            yaml.set(base + "rewards.messages", collection.messages);
        }
        for (Treasure treasure : treasures.values()) {
            String base = "collections." + treasure.collection.toLowerCase(Locale.ROOT)
                    + ".treasures." + treasure.key().replace(":", "_") + ".";
            yaml.set(base + "world", treasure.world);
            yaml.set(base + "x", treasure.x);
            yaml.set(base + "y", treasure.y);
            yaml.set(base + "z", treasure.z);
            yaml.set(base + "material", treasure.material.name());
        }
        try {
            yaml.save(collectionsFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save collections: " + e.getMessage());
        }
        YamlConfiguration players = new YamlConfiguration();
        for (Map.Entry<String, Map<UUID, Set<String>>> entry : found.entrySet()) {
            for (Map.Entry<UUID, Set<String>> perPlayer : entry.getValue().entrySet()) {
                players.set("found." + entry.getKey() + "." + perPlayer.getKey(),
                        new ArrayList<>(perPlayer.getValue()));
            }
        }
        try {
            players.save(playersFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save player data: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* hunt logic                                                         */
    /* ------------------------------------------------------------------ */

    private Collection collectionNamed(String name) {
        return name == null ? null : collections.get(name.toLowerCase(Locale.ROOT));
    }

    private Set<String> foundOf(String collection, UUID player) {
        return found.computeIfAbsent(collection.toLowerCase(Locale.ROOT), key -> new HashMap<>())
                .computeIfAbsent(player, key -> new LinkedHashSet<>());
    }

    private boolean alreadyFound(Treasure treasure, UUID player) {
        Collection collection = collectionNamed(treasure.collection);
        if (collection != null && collection.singlePlayerFind) {
            // global: once any player found it, it is gone for everybody
            Map<UUID, Set<String>> map = found.get(treasure.collection.toLowerCase(Locale.ROOT));
            if (map == null) {
                return false;
            }
            for (Set<String> keys : map.values()) {
                if (keys.contains(treasure.key())) {
                    return true;
                }
            }
            return false;
        }
        return foundOf(treasure.collection, player).contains(treasure.key());
    }

    private int collected(String collection, UUID player) {
        return foundOf(collection, player).size();
    }

    private int total(String collection) {
        Collection target = collectionNamed(collection);
        if (target == null) {
            return 0;
        }
        int count = 0;
        for (Treasure treasure : treasures.values()) {
            if (treasure.collection.equalsIgnoreCase(target.name)) {
                count++;
            }
        }
        return count;
    }

    private Treasure treasureAt(Block block) {
        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY()
                + ":" + block.getZ();
        return treasures.get(key);
    }

    private void drawParticles() {
        if (!cfgBool("particles.enabled", true)) {
            return;
        }
        double viewDistance = cfgDouble("particles.view-distance", 32);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!particles.getOrDefault(player.getUniqueId(), true)) {
                continue;
            }
            for (Treasure treasure : treasures.values()) {
                Location location = treasure.location();
                if (location == null || !location.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (location.distanceSquared(player.getLocation()) > viewDistance * viewDistance) {
                    continue;
                }
                Collection collection = collectionNamed(treasure.collection);
                if (collection == null || !collection.enabled) {
                    continue;
                }
                boolean isFound = alreadyFound(treasure, player.getUniqueId());
                if (isFound && collection.hideAfterFound) {
                    continue;
                }
                player.spawnParticle(isFound ? Particle.HAPPY_VILLAGER : Particle.CRIT,
                        location.clone().add(0.5, 0.7, 0.5), isFound ? 2 : 3, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    private void complete(Player player, Treasure treasure, Collection collection) {
        Set<String> keys = foundOf(collection.name, player.getUniqueId());
        if (!keys.add(treasure.key())) {
            return;
        }
        reply(player, "found", "collection", collection.name,
                "found", String.valueOf(keys.size()), "total", String.valueOf(total(collection.name)));
        for (String message : collection.messages) {
            player.sendMessage(Text.parse(message.replace("%player%", player.getName())
                    .replace("%collection%", collection.name)));
        }
        for (String command : collection.commands) {
            String parsed = command.replace("%player%", player.getName())
                    .replace("%collection%", collection.name);
            try {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
            } catch (Exception e) {
                plugin.getLogger().warning("Treasure reward command failed: " + parsed);
            }
        }
        if (collection.hideAfterFound) {
            Material replacement = Material.matchMaterial(
                    cfgString("treasure-settings.replace-block", "BARRIER"));
            Location location = treasure.location();
            if (location != null) {
                location.getBlock().setType(replacement == null ? Material.BARRIER : replacement, false);
            }
            treasures.remove(treasure.key());
            save();
        }
        if (cfgBool("broadcast-found", true)) {
            plugin.getServer().broadcast(Text.parse(msg("broadcast-found",
                    "player", player.getName(), "collection", collection.name)));
        }
        save();
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        String pending = placeMode.get(player.getUniqueId());
        if (pending != null) {
            event.setCancelled(true);
            Collection collection = collectionNamed(pending);
            if (collection == null) {
                placeMode.remove(player.getUniqueId());
                reply(player, "collection-missing", "collection", pending);
                return;
            }
            Treasure treasure = new Treasure(collection.name, block.getLocation());
            treasures.put(treasure.key(), treasure);
            save();
            reply(player, "placed", "collection", collection.name,
                    "location", treasure.world + " " + treasure.x + ", " + treasure.y + ", " + treasure.z);
            if (!cfgBool("place-mode.stay", false)) {
                placeMode.remove(player.getUniqueId());
                reply(player, "place-mode-off");
            }
            return;
        }

        Treasure treasure = treasureAt(block);
        if (treasure == null) {
            return;
        }
        Collection collection = collectionNamed(treasure.collection);
        if (collection == null || !collection.enabled) {
            reply(player, "collection-disabled", "collection", treasure.collection);
            return;
        }
        if (alreadyFound(treasure, player.getUniqueId())) {
            reply(player, "already-found");
            return;
        }
        event.setCancelled(true);
        complete(player, treasure, collection);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Treasure treasure = treasureAt(event.getBlock());
        if (treasure == null) {
            return;
        }
        if (!event.getPlayer().hasPermission("samplese.treasure.admin")) {
            reply(event.getPlayer(), "no-break");
            event.setCancelled(true);
            return;
        }
        treasures.remove(treasure.key());
        save();
        reply(event.getPlayer(), "removed", "collection", treasure.collection);
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class TreasureCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "create" -> create(sender, args);
                case "delete", "remove" -> delete(sender, args);
                case "list", "collections" -> list(sender);
                case "enable" -> toggle(sender, args, true);
                case "disable" -> toggle(sender, args, false);
                case "place" -> place(sender, args);
                case "cancel" -> cancel(sender);
                case "addreward" -> addReward(sender, args);
                case "info" -> info(sender, args);
                case "stats" -> stats(sender, args);
                case "top", "leaderboard" -> top(sender, args);
                case "hint" -> hint(sender);
                case "particles" -> particles(sender);
                case "reload" -> {
                    if (!admin(sender)) {
                        return true;
                    }
                    reloadConfig();
                    load();
                    reply(sender, "reloaded");
                }
                default -> sendHelp(sender);
            }
            return true;
        }

        private void sendHelp(CommandSender sender) {
            for (String line : cfgStringList("help-lines")) {
                sender.sendMessage(Text.parse(line));
            }
        }

        private boolean admin(CommandSender sender) {
            if (sender.hasPermission("samplese.treasure.admin")) {
                return true;
            }
            reply(sender, "no-permission");
            return false;
        }

        private void create(CommandSender sender, String[] args) {
            if (!admin(sender)) {
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/treasure create <name>");
                return;
            }
            if (collectionNamed(args[1]) != null) {
                reply(sender, "collection-exists", "collection", args[1]);
                return;
            }
            collections.put(args[1].toLowerCase(Locale.ROOT), new Collection(args[1]));
            save();
            reply(sender, "collection-created", "collection", args[1]);
        }

        private void delete(CommandSender sender, String[] args) {
            if (!admin(sender)) {
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/treasure delete <name>");
                return;
            }
            Collection collection = collectionNamed(args[1]);
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args[1]);
                return;
            }
            collections.remove(collection.name.toLowerCase(Locale.ROOT));
            treasures.values().removeIf(treasure ->
                    treasure.collection.equalsIgnoreCase(collection.name));
            found.remove(collection.name.toLowerCase(Locale.ROOT));
            save();
            reply(sender, "collection-deleted", "collection", collection.name);
        }

        private void list(CommandSender sender) {
            reply(sender, "list-header", "count", String.valueOf(collections.size()));
            for (Collection collection : collections.values()) {
                sender.sendMessage(Text.parse(msg("list-entry",
                        "collection", collection.name,
                        "treasures", String.valueOf(total(collection.name)),
                        "state", collection.enabled ? msg("state-on") : msg("state-off"))));
            }
        }

        private void toggle(CommandSender sender, String[] args, boolean state) {
            if (!admin(sender)) {
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/treasure " + (state ? "enable" : "disable") + " <name>");
                return;
            }
            Collection collection = collectionNamed(args[1]);
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args[1]);
                return;
            }
            collection.enabled = state;
            save();
            reply(sender, state ? "collection-enabled" : "collection-disabled-ok",
                    "collection", collection.name);
        }

        private void place(CommandSender sender, String[] args) {
            if (!admin(sender)) {
                return;
            }
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/treasure place <collection>");
                return;
            }
            Collection collection = collectionNamed(args[1]);
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args[1]);
                return;
            }
            placeMode.put(player.getUniqueId(), collection.name);
            reply(player, "place-mode-on", "collection", collection.name);
        }

        private void cancel(CommandSender sender) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            placeMode.remove(player.getUniqueId());
            reply(player, "place-mode-off");
        }

        private void addReward(CommandSender sender, String[] args) {
            if (!admin(sender)) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/treasure addreward <collection> <command|message> <value>");
                return;
            }
            Collection collection = collectionNamed(args[1]);
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args[1]);
                return;
            }
            String value = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            if (args[2].equalsIgnoreCase("command")) {
                collection.commands.add(value);
            } else if (args[2].equalsIgnoreCase("message")) {
                collection.messages.add(value);
            } else {
                reply(sender, "usage", "usage", "/treasure addreward <collection> <command|message> <value>");
                return;
            }
            save();
            reply(sender, "reward-added", "collection", collection.name);
        }

        private void info(CommandSender sender, String[] args) {
            Collection collection;
            if (args.length >= 2) {
                collection = collectionNamed(args[1]);
            } else if (sender instanceof Player player) {
                collection = collectionNamed(nearestCollection(player.getLocation()));
            } else {
                reply(sender, "usage", "usage", "/treasure info <collection>");
                return;
            }
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args.length >= 2 ? args[1] : "?");
                return;
            }
            reply(sender, "info-header", "collection", collection.name);
            sender.sendMessage(Text.parse(msg("info-treasures",
                    "treasures", String.valueOf(total(collection.name)))));
            sender.sendMessage(Text.parse(msg("info-state",
                    "state", collection.enabled ? msg("state-on") : msg("state-off"))));
            sender.sendMessage(Text.parse(msg("info-rewards",
                    "commands", String.valueOf(collection.commands.size()),
                    "messages", String.valueOf(collection.messages.size()))));
            if (sender instanceof Player player) {
                sender.sendMessage(Text.parse(msg("info-progress",
                        "found", String.valueOf(collected(collection.name, player.getUniqueId())),
                        "total", String.valueOf(total(collection.name)))));
            }
        }

        private String nearestCollection(Location location) {
            String best = null;
            double bestDistance = Double.MAX_VALUE;
            for (Treasure treasure : treasures.values()) {
                Location treasureLocation = treasure.location();
                if (treasureLocation == null || !treasureLocation.getWorld().equals(location.getWorld())) {
                    continue;
                }
                double distance = treasureLocation.distanceSquared(location);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = treasure.collection;
                }
            }
            return best;
        }

        private void stats(CommandSender sender, String[] args) {
            Player target = sender instanceof Player self ? self : null;
            if (args.length >= 2) {
                target = plugin.getServer().getPlayerExact(args[1]);
            }
            if (target == null) {
                reply(sender, "player-only");
                return;
            }
            reply(sender, "stats-header", "player", target.getName());
            for (Collection collection : collections.values()) {
                sender.sendMessage(Text.parse(msg("stats-entry",
                        "collection", collection.name,
                        "found", String.valueOf(collected(collection.name, target.getUniqueId())),
                        "total", String.valueOf(total(collection.name)))));
            }
        }

        private void top(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/treasure top <collection>");
                return;
            }
            Collection collection = collectionNamed(args[1]);
            if (collection == null) {
                reply(sender, "collection-missing", "collection", args[1]);
                return;
            }
            Map<UUID, Set<String>> map = found.getOrDefault(
                    collection.name.toLowerCase(Locale.ROOT), Map.of());
            List<Map.Entry<UUID, Set<String>>> ranking = new ArrayList<>(map.entrySet());
            ranking.sort(Comparator.comparingInt((Map.Entry<UUID, Set<String>> entry) ->
                    entry.getValue().size()).reversed());
            int limit = Math.max(1, cfgInt("leaderboard.display-limit", 10));
            reply(sender, "top-header", "collection", collection.name);
            int place = 1;
            for (Map.Entry<UUID, Set<String>> entry : ranking) {
                if (place > limit) {
                    break;
                }
                String name = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                sender.sendMessage(Text.parse(msg("top-entry",
                        "place", String.valueOf(place),
                        "player", name == null ? entry.getKey().toString() : name,
                        "found", String.valueOf(entry.getValue().size()))));
                place++;
            }
        }

        private void hint(CommandSender sender) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (!cfgBool("hint.enabled", true)) {
                reply(sender, "hint-disabled");
                return;
            }
            int cooldown = Math.max(0, cfgInt("hint.cooldown-seconds", 300));
            long now = System.currentTimeMillis();
            long last = hintCooldown.getOrDefault(player.getUniqueId(), 0L);
            long remaining = cooldown - (now - last) / 1000L;
            if (remaining > 0 && !player.hasPermission("samplese.treasure.admin")) {
                reply(player, "hint-cooldown", "time", String.valueOf(remaining));
                return;
            }
            double range = cfgDouble("hint.proximity-range", 50);
            List<Treasure> candidates = new ArrayList<>();
            for (Treasure treasure : treasures.values()) {
                Location location = treasure.location();
                if (location == null || !location.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (location.distanceSquared(player.getLocation()) > range * range) {
                    continue;
                }
                Collection collection = collectionNamed(treasure.collection);
                if (collection == null || !collection.enabled
                        || alreadyFound(treasure, player.getUniqueId())) {
                    continue;
                }
                candidates.add(treasure);
            }
            if (candidates.isEmpty()) {
                reply(player, "hint-none");
                return;
            }
            Treasure treasure = candidates.get(java.util.concurrent.ThreadLocalRandom.current()
                    .nextInt(candidates.size()));
            String axis = cfgString("hint.reveal-coordinate", "RANDOM").toUpperCase(Locale.ROOT);
            if (axis.equals("RANDOM")) {
                axis = java.util.concurrent.ThreadLocalRandom.current().nextBoolean() ? "X" : "Z";
            }
            boolean revealX = axis.equals("X");
            reply(player, "hint-result",
                    "axis", revealX ? "X" : "Z",
                    "value", String.valueOf(revealX ? treasure.x : treasure.z),
                    "distance", String.valueOf((int) Math.round(
                            treasure.location().distance(player.getLocation()))),
                    "collection", treasure.collection);
            hintCooldown.put(player.getUniqueId(), now);
        }

        private void particles(CommandSender sender) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            boolean state = !particles.getOrDefault(player.getUniqueId(), true);
            particles.put(player.getUniqueId(), state);
            reply(player, state ? "particles-on" : "particles-off");
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("create", "delete", "list", "enable",
                        "disable", "place", "cancel", "addreward", "info", "stats", "top", "hint",
                        "particles", "reload"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2) {
                String sub = args[0].toLowerCase(Locale.ROOT);
                if (!sub.equals("create") && !sub.equals("stats") && !sub.equals("particles")) {
                    List<String> names = new ArrayList<>();
                    collections.values().forEach(collection -> names.add(collection.name));
                    return names;
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("addreward")) {
                return List.of("command", "message");
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
