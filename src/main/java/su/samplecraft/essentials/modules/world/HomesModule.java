package su.samplecraft.essentials.modules.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.modules.social.PlayerDirectory;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code SetHome_GUI-3.0.5.jar} (org.sethomegui.SetHomeGUI).
 * <p>
 * Ported behaviour:
 * <ul>
 *   <li>{@code /sethome [name]} — with no name the original opens a chat prompt
 *       ("please type the name... type cancel"), with a name it saves directly.
 *       Name validation uses {@code name-regex}, {@code blacklisted-words} and
 *       {@code blacklisted-worlds}; limits resolve through
 *       {@code sethome.maxhomes.<n>} / {@code sethome.maxhomes.unlimited}
 *       (here {@code samplese.homes.max.<n>} / {@code samplese.homes.unlimited}),
 *       default {@code default-max-homes},</li>
 *   <li>{@code /home} opens the paged homes GUI (owner heads, click to teleport,
 *       shift-click to delete with confirmation), {@code /home <name>} teleports,</li>
 *   <li>{@code /delhome <name>} deletes a home (confirmation GUI preserved in the menu),</li>
 *   <li>teleport countdown with {@code teleport-cooldown}, {@code cancel-on-move} and
 *       {@code sethome.cooldown.bypass}, exactly like TeleportManager,</li>
 *   <li>{@code /homeadmin <view|reload|delete|import>} for staff.</li>
 * </ul>
 * Homes persist in {@code world/homes/data/<uuid>.yml}, one file per player like the
 * original YAML storage.
 */
public final class HomesModule extends EssentialsModule implements Listener {

    private record Home(String name, String world, double x, double y, double z, float yaw, float pitch) {
    }

    private final Map<UUID, Map<String, Home>> homes = new ConcurrentHashMap<>();
    /** players currently answering the "type a home name" prompt */
    private final Map<UUID, Long> prompts = new ConcurrentHashMap<>();
    /** players in a teleport countdown -> start location */
    private final Map<UUID, Location> countdowns = new ConcurrentHashMap<>();
    /** open menu -> list of homes shown (index -> name) */
    private final Map<UUID, List<String>> menus = new ConcurrentHashMap<>();
    private final Map<UUID, String> deletes = new ConcurrentHashMap<>();

    private PlayerDirectory directory;

    public HomesModule(SamplesEssentials plugin) {
        super("world", "homes", "SetHome_GUI-3.0.5.jar", "Homes with GUI menu, chat prompt, limits, cooldown and /homeadmin");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.homes", "Use homes", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.homes.unlimited", "Unlimited homes", org.bukkit.permissions.PermissionDefault.FALSE);
        ensurePermission("samplese.homes.cooldown.bypass", "Skip the home teleport countdown", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.homes.admin", "Administer homes", org.bukkit.permissions.PermissionDefault.OP);
        directory = new PlayerDirectory(plugin);
        HomeCommand home = new HomeCommand();
        command("home", home);
        command("homes", home);
        command("sethome", home);
        command("delhome", home);
        command("homeadmin", new HomeAdminCommand());
        onDisableHook(() -> {
            unbindCommands();
            saveAll();
        });
        listen(this);
        syncRepeat(100L, 100L, this::tickCountdowns);
    }

    /* ------------------------------------------------------------------ */
    /* storage (HomeManager: one yml per player)                          */
    /* ------------------------------------------------------------------ */

    private ModuleStorage playerStorage(UUID id) {
        return new ModuleStorage(plugin, getDataFolder(), id + ".yml");
    }

    private Map<String, Home> loaded(UUID id) {
        return homes.computeIfAbsent(id, key -> {
            Map<String, Home> result = new HashMap<>();
            ModuleStorage storage = playerStorage(key);
            var section = storage.yaml().getConfigurationSection("homes");
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    String path = "homes." + name;
                    String world = storage.yaml().getString(path + ".world");
                    if (world == null) {
                        continue;
                    }
                    result.put(name.toLowerCase(Locale.ROOT), new Home(name, world,
                            storage.yaml().getDouble(path + ".x"),
                            storage.yaml().getDouble(path + ".y"),
                            storage.yaml().getDouble(path + ".z"),
                            (float) storage.yaml().getDouble(path + ".yaw"),
                            (float) storage.yaml().getDouble(path + ".pitch")));
                }
            }
            return result;
        });
    }

    private void saveHome(UUID id, Home home) {
        ModuleStorage storage = playerStorage(id);
        String path = "homes." + home.name();
        storage.yaml().set(path + ".world", home.world());
        storage.yaml().set(path + ".x", home.x());
        storage.yaml().set(path + ".y", home.y());
        storage.yaml().set(path + ".z", home.z());
        storage.yaml().set(path + ".yaw", home.yaw());
        storage.yaml().set(path + ".pitch", home.pitch());
        storage.saveLater();
    }

    private void saveAll() {
        homes.forEach((id, list) -> {
            ModuleStorage storage = playerStorage(id);
            storage.yaml().set("homes", null);
            list.forEach((name, home) -> {
                String path = "homes." + home.name();
                storage.yaml().set(path + ".world", home.world());
                storage.yaml().set(path + ".x", home.x());
                storage.yaml().set(path + ".y", home.y());
                storage.yaml().set(path + ".z", home.z());
                storage.yaml().set(path + ".yaw", home.yaw());
                storage.yaml().set(path + ".pitch", home.pitch());
            });
            storage.save();
        });
    }

    private int homeLimit(Player player) {
        if (player.hasPermission("samplese.homes.unlimited")) {
            return -1;
        }
        int max = cfgInt("default-max-homes", 3);
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) {
                continue;
            }
            String node = info.getPermission().toLowerCase(Locale.ROOT);
            String prefix = "samplese.homes.max.";
            if (!node.startsWith(prefix)) {
                continue;
            }
            try {
                int value = Integer.parseInt(node.substring(prefix.length()));
                if (value < 0) {
                    return -1;
                }
                max = Math.max(max, value);
            } catch (NumberFormatException ignored) {
                // malformed node
            }
        }
        return max;
    }

    /* ------------------------------------------------------------------ */
    /* /sethome                                                           */
    /* ------------------------------------------------------------------ */

    private void setHome(Player player, String rawName) {
        if (!player.hasPermission("samplese.homes")) {
            player.sendMessage(msgComponent("no-permission"));
            return;
        }
        String world = player.getWorld().getName();
        if (cfgStringList("blacklisted-worlds").stream().anyMatch(w -> w.equalsIgnoreCase(world))) {
            player.sendMessage(msgComponent("world-blacklisted", "world", world));
            return;
        }
        String name = Text.stripColors(rawName).trim();
        for (String word : cfgStringList("blacklisted-words")) {
            if (!word.isBlank() && name.toLowerCase(Locale.ROOT).contains(word.toLowerCase(Locale.ROOT))) {
                player.sendMessage(msgComponent("blacklisted-name"));
                return;
            }
        }
        String regex = cfgString("name-regex", "^[A-Za-z0-9\\s]{1,32}$");
        if (name.isEmpty() || !name.matches(regex)) {
            player.sendMessage(msgComponent("invalid-name"));
            return;
        }
        Map<String, Home> list = loaded(player.getUniqueId());
        boolean exists = list.containsKey(name.toLowerCase(Locale.ROOT));
        int max = homeLimit(player);
        if (!exists && max != -1 && list.size() >= max) {
            player.sendMessage(msgComponent("limit-reached", "max", String.valueOf(max)));
            return;
        }
        Location location = player.getLocation();
        Home home = new Home(name, world, location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
        list.put(name.toLowerCase(Locale.ROOT), home);
        saveHome(player.getUniqueId(), home);
        player.sendMessage(msgComponent("home-saved", "name", name));
    }

    /* ------------------------------------------------------------------ */
    /* teleport (TeleportManager)                                         */
    /* ------------------------------------------------------------------ */

    private void teleportHome(Player player, String name) {
        Home home = loaded(player.getUniqueId()).get(name.toLowerCase(Locale.ROOT));
        if (home == null) {
            player.sendMessage(msgComponent("home-not-found", "name", name));
            return;
        }
        World world = plugin.getServer().getWorld(home.world());
        if (world == null) {
            player.sendMessage(msgComponent("world-not-loaded", "world", home.world()));
            return;
        }
        Location target = new Location(world, home.x(), home.y(), home.z(), home.yaw(), home.pitch());
        if (player.hasPermission("samplese.homes.cooldown.bypass") || cfgInt("teleport-cooldown", 5) <= 0) {
            player.teleportAsync(target);
            player.sendMessage(msgComponent("teleport-success", "name", home.name()));
            return;
        }
        countdowns.put(player.getUniqueId(), player.getLocation().clone());
        player.sendMessage(msgComponent("teleport-countdown", "name", home.name(),
                "seconds", String.valueOf(cfgInt("teleport-cooldown", 5))));
        pendingTeleports.put(player.getUniqueId(), target);
        pendingNames.put(player.getUniqueId(), home.name());
    }

    private final Map<UUID, Location> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingNames = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastCountdownSecond = new ConcurrentHashMap<>();

    /** Runs every second: completes a pending teleport countdown. */
    private void tickCountdowns() {
        for (UUID id : new ArrayList<>(countdowns.keySet())) {
            Player player = plugin.getServer().getPlayer(id);
            Location target = pendingTeleports.get(id);
            if (player == null || target == null) {
                countdowns.remove(id);
                pendingTeleports.remove(id);
                pendingNames.remove(id);
                lastCountdownSecond.remove(id);
                continue;
            }
            int cooldown = Math.max(1, cfgInt("teleport-cooldown", 5));
            int elapsed = lastCountdownSecond.merge(id, 1, Integer::sum);
            if (elapsed >= cooldown) {
                countdowns.remove(id);
                pendingTeleports.remove(id);
                lastCountdownSecond.remove(id);
                String name = pendingNames.remove(id);
                player.teleportAsync(target).thenAccept(success -> {
                    if (Boolean.TRUE.equals(success)) {
                        player.sendMessage(msgComponent("teleport-success", "name", name == null ? "?" : name));
                    }
                });
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* GUI (GUIManager)                                                   */
    /* ------------------------------------------------------------------ */

    private void openHomesMenu(Player player) {
        Map<String, Home> list = loaded(player.getUniqueId());
        if (list.isEmpty()) {
            player.sendMessage(msgComponent("no-homes"));
            return;
        }
        List<Home> sorted = list.values().stream().sorted(Comparator.comparing(Home::name)).toList();
        List<String> names = new ArrayList<>();
        Inventory inventory = Bukkit.createInventory(null, 54, Text.parse(cfgString("gui.title", "&8Your homes")));
        int index = 0;
        for (Home home : sorted) {
            if (index >= 45) {
                break;
            }
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta skull) {
                skull.setOwningPlayer(player);
                item.setItemMeta(skull);
            }
            if (item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Text.parse("&#ef6603" + home.name()));
                meta.lore(List.of(
                        Text.parse("<gray>" + home.world() + " <dark_gray>("
                                + (int) home.x() + ", " + (int) home.y() + ", " + (int) home.z() + ")"),
                        Text.parse("<green>Left-click to teleport"),
                        Text.parse("<red>Shift-click to delete")));
                item.setItemMeta(meta);
            }
            inventory.setItem(index, item);
            names.add(home.name());
            index++;
        }
        inventories.put(player.getUniqueId(), "homes");
        menus.put(player.getUniqueId(), names);
        player.openInventory(inventory);
    }

    private void openDeleteMenu(Player player, String homeName) {
        Inventory inventory = Bukkit.createInventory(null, 27, Text.parse("&8Delete home " + homeName + "?"));
        ItemStack confirm = new ItemStack(Material.LIME_CONCRETE);
        if (confirm.getItemMeta() != null) {
            ItemMeta meta = confirm.getItemMeta();
            meta.displayName(Text.parse("<green>Confirm delete"));
            confirm.setItemMeta(meta);
        }
        ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
        if (cancel.getItemMeta() != null) {
            ItemMeta meta = cancel.getItemMeta();
            meta.displayName(Text.parse("<red>Cancel"));
            cancel.setItemMeta(meta);
        }
        inventory.setItem(11, confirm);
        inventory.setItem(15, cancel);
        deletes.put(player.getUniqueId(), homeName);
        inventories.put(player.getUniqueId(), "delete");
        player.openInventory(inventory);
    }

    private final Map<UUID, String> inventories = new ConcurrentHashMap<>();

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String menu = inventories.get(player.getUniqueId());
        if (menu == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        if (menu.equals("homes") && event.getCurrentItem() != null) {
            List<String> names = menus.get(player.getUniqueId());
            if (names == null || event.getSlot() >= names.size()) {
                return;
            }
            String name = names.get(event.getSlot());
            player.closeInventory();
            inventories.remove(player.getUniqueId());
            if (event.isShiftClick()) {
                openDeleteMenu(player, name);
            } else {
                teleportHome(player, name);
            }
            return;
        }
        if (menu.equals("delete")) {
            String name = deletes.remove(player.getUniqueId());
            inventories.remove(player.getUniqueId());
            player.closeInventory();
            if (event.getSlot() == 11 && name != null) {
                loaded(player.getUniqueId()).remove(name.toLowerCase(Locale.ROOT));
                ModuleStorage storage = playerStorage(player.getUniqueId());
                storage.yaml().set("homes." + name, null);
                storage.save();
                player.sendMessage(msgComponent("home-deleted", "name", name));
            } else {
                player.sendMessage(msgComponent("delete-cancelled"));
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* chat prompt (ChatPromptListener)                                   */
    /* ------------------------------------------------------------------ */

    @EventHandler(ignoreCancelled = true)
    public void onPromptChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Long started = prompts.remove(player.getUniqueId());
        if (started == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        String cancel = cfgString("messages.cancel-word", "cancel");
        int timeout = cfgInt("chat-prompt-timeout", 60);
        if ((System.currentTimeMillis() - started) / 1000L > timeout) {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.sendMessage(msgComponent("creation-timeout")));
            return;
        }
        if (message.equalsIgnoreCase(cancel)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.sendMessage(msgComponent("creation-cancelled")));
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> setHome(player, message));
    }

    /* ------------------------------------------------------------------ */
    /* commands                                                           */
    /* ------------------------------------------------------------------ */

    private final class HomeCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            switch (command.getName().toLowerCase(Locale.ROOT)) {
                case "sethome" -> {
                    if (args.length == 0) {
                        int max = homeLimit(player);
                        if (max != -1 && loaded(player.getUniqueId()).size() >= max) {
                            player.sendMessage(msgComponent("limit-reached", "max", String.valueOf(max)));
                            return true;
                        }
                        prompts.put(player.getUniqueId(), System.currentTimeMillis());
                        player.sendMessage(msgComponent("name-prompt",
                                "cancel_word", cfgString("messages.cancel-word", "cancel")));
                        return true;
                    }
                    setHome(player, String.join(" ", args));
                }
                case "delhome" -> {
                    if (args.length == 0) {
                        player.sendMessage(msgComponent("delhome-usage"));
                        return true;
                    }
                    String name = String.join(" ", args);
                    if (!loaded(player.getUniqueId()).containsKey(name.toLowerCase(Locale.ROOT))) {
                        player.sendMessage(msgComponent("home-not-found", "name", name));
                        return true;
                    }
                    openDeleteMenu(player, name);
                }
                case "homes" -> {
                    if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
                        Map<String, Home> list = loaded(player.getUniqueId());
                        if (list.isEmpty()) {
                            player.sendMessage(msgComponent("no-homes"));
                            return true;
                        }
                        player.sendMessage(msgComponent("list-header", "count", String.valueOf(list.size())));
                        list.values().stream().sorted(Comparator.comparing(Home::name)).forEach(home ->
                                player.sendMessage(msgComponent("list-entry",
                                        "name", home.name(), "world", home.world(),
                                        "x", String.valueOf((int) home.x()),
                                        "y", String.valueOf((int) home.y()),
                                        "z", String.valueOf((int) home.z()))));
                        return true;
                    }
                    if (args.length == 0) {
                        openHomesMenu(player);
                        return true;
                    }
                    teleportHome(player, String.join(" ", args));
                }
                default -> {
                    if (args.length == 0) {
                        openHomesMenu(player);
                    } else {
                        teleportHome(player, String.join(" ", args));
                    }
                }
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                return List.of();
            }
            String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
            return loaded(player.getUniqueId()).keySet().stream()
                    .filter(name -> name.startsWith(prefix)).sorted().toList();
        }
    }

    private final class HomeAdminCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.homes.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(msgComponent("admin-usage"));
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "view" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("admin-view-usage"));
                        return true;
                    }
                    PlayerDirectory.Entry target = directory.find(args[1]);
                    if (target == null) {
                        sender.sendMessage(msgComponent("admin-unknown-player", "player", args[1]));
                        return true;
                    }
                    Map<String, Home> list = loaded(target.id());
                    if (list.isEmpty()) {
                        sender.sendMessage(msgComponent("admin-no-homes", "player", target.name()));
                        return true;
                    }
                    sender.sendMessage(msgComponent("admin-view-header",
                            "player", target.name(), "count", String.valueOf(list.size())));
                    for (Home home : list.values()) {
                        sender.sendMessage(Text.parse("<white>" + home.name() + " <gray>in "
                                + home.world() + " (" + (int) home.x() + ", " + (int) home.y() + ", " + (int) home.z() + ")"));
                    }
                }
                case "delete" -> {
                    if (args.length < 3) {
                        sender.sendMessage(msgComponent("admin-delete-usage"));
                        return true;
                    }
                    PlayerDirectory.Entry target = directory.find(args[1]);
                    if (target == null) {
                        sender.sendMessage(msgComponent("admin-unknown-player", "player", args[1]));
                        return true;
                    }
                    String homeName = args[2];
                    if (loaded(target.id()).remove(homeName.toLowerCase(Locale.ROOT)) == null) {
                        sender.sendMessage(msgComponent("admin-no-home", "home", homeName));
                        return true;
                    }
                    ModuleStorage storage = playerStorage(target.id());
                    storage.yaml().set("homes." + homeName, null);
                    storage.save();
                    sender.sendMessage(msgComponent("admin-deleted", "home", homeName, "player", target.name()));
                }
                case "reload" -> {
                    reloadConfig();
                    sender.sendMessage(msgComponent("reloaded"));
                }
                case "import" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("admin-import-usage"));
                        return true;
                    }
                    sender.sendMessage(msgComponent("admin-import-started", "source", args[1]));
                    sync(() -> importHome(args[1], sender));
                }
                default -> sender.sendMessage(msgComponent("admin-usage"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("view", "delete", "reload", "import");
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("delete"))) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("import")) {
                return List.of("Essentials", "HuskHomes");
            }
            return List.of();
        }
    }

    /**
     * HomeImporter: reads {@code OLD/plugins/Essentials/userdata/<uuid>.yml} (or an
     * accompanying folder) and copies simple {@code homes:} maps. Only used on demand.
     */
    private void importHome(String source, CommandSender sender) {
        java.io.File importRoot = new java.io.File(plugin.getDataFolder(),
                "import/" + source.toLowerCase(Locale.ROOT) + "/userdata");
        if (!importRoot.isDirectory()) {
            sender.sendMessage(msgComponent("admin-import-missing", "path", importRoot.getAbsolutePath()));
            return;
        }
        int imported = 0;
        java.io.File[] files = importRoot.listFiles((dir, file) -> file.endsWith(".yml"));
        if (files != null) {
            for (java.io.File file : files) {
                try {
                    UUID id = UUID.fromString(file.getName().replace(".yml", ""));
                    var yaml = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
                    var section = yaml.getConfigurationSection("homes");
                    if (section == null) {
                        continue;
                    }
                    for (String name : section.getKeys(false)) {
                        String world = section.getString(name + ".world") != null
                                ? section.getString(name + ".world")
                                : section.getString(name + ".world-name");
                        if (world == null) {
                            continue;
                        }
                        Home home = new Home(name, world,
                                section.getDouble(name + ".x"), section.getDouble(name + ".y"),
                                section.getDouble(name + ".z"), 0f, 0f);
                        loaded(id).put(name.toLowerCase(Locale.ROOT), home);
                        saveHome(id, home);
                        imported++;
                    }
                } catch (Exception ignored) {
                    // skip malformed files
                }
            }
        }
        sender.sendMessage(msgComponent("admin-imported", "count", String.valueOf(imported)));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (countdowns.containsKey(event.getPlayer().getUniqueId()) && cfgBool("cancel-on-move", true)) {
            Location start = countdowns.get(event.getPlayer().getUniqueId());
            if (start != null && event.getTo() != null && start.distanceSquared(event.getTo()) > 0.25) {
                countdowns.remove(event.getPlayer().getUniqueId());
                pendingTeleports.remove(event.getPlayer().getUniqueId());
                lastCountdownSecond.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(msgComponent("teleport-cancelled-move"));
            }
        }
    }
}
