package su.samplecraft.essentials.modules.world;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code protect-3.4.1-all.jar} (net.thenextlvl.protect).
 * <p>
 * Cuboid land claims with owners, members and per-area flags. Kept faithful to the
 * original model:
 * <ul>
 *   <li>areas are stored as YAML in {@code areas/<name>.yml} with
 *       {@code world, min/max corner, owner, members, priority, flags},</li>
 *   <li>higher {@code priority} wins where areas overlap,</li>
 *   <li>owner/member checks plus the {@code protect.bypass} permission,</li>
 *   <li>flags are tri-state: {@code true} = allowed, {@code false} = denied,
 *       unset = inherit the vanilla behaviour,</li>
 *   <li>greetings/farewell messages ({@code chat}, {@code actionbar}, {@code title})
 *       fire on enter/leave, driven by a movement check.</li>
 * </ul>
 * Ported flags: build/break/place, interact, physical_interact, player_attack_player,
 * player_attack_entity, entity_attack_player, damage, explosions, entity_item_drop,
 * entity_item_pickup, player_item_drop, liquid_flow, hungers, enter/leave,
 * greetings/farewell + variants. Flags the port does not evaluate (physics, growth,
 * weather, time, cauldrons, ...) are still stored and shown by {@code /area info},
 * so a config copied from the original never loses data.
 * <p>
 * Commands: {@code /area create|delete|list|info|select|redefine|teleport|priority|
 * owner|member|flag|rename} and aliases {@code /protect}.
 */
public final class ProtectModule extends EssentialsModule implements Listener {

    /** One claim. Nullable flags mean "inherit vanilla". */
    private static final class Area {
        String name;
        String world;
        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;
        UUID owner;
        String ownerName = "?";
        final Set<UUID> members = new HashSet<>();
        int priority = 0;
        final Map<String, Object> flags = new LinkedHashMap<>();

        boolean contains(Location location) {
            if (location.getWorld() == null || !location.getWorld().getName().equals(world)) {
                return false;
            }
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        Boolean flag(String name) {
            Object value = flags.get(name);
            if (value instanceof Boolean bool) {
                return bool;
            }
            return null;
        }

        String stringFlag(String name) {
            Object value = flags.get(name);
            return value instanceof String string && !string.isBlank() ? string : null;
        }

        boolean trusted(Player player) {
            return owner != null && owner.equals(player.getUniqueId())
                    || members.contains(player.getUniqueId())
                    || player.hasPermission("samplese.protect.bypass");
        }
    }

    /** Default flags for a freshly created area (mirrors Protect's defaults). */
    private static final Map<String, Boolean> DEFAULT_FLAGS = new LinkedHashMap<>();

    static {
        DEFAULT_FLAGS.put("build", false);
        DEFAULT_FLAGS.put("break", false);
        DEFAULT_FLAGS.put("place", false);
        DEFAULT_FLAGS.put("interact", false);
        DEFAULT_FLAGS.put("physical_interact", false);
        DEFAULT_FLAGS.put("player_attack_player", false);
        DEFAULT_FLAGS.put("player_attack_entity", false);
        DEFAULT_FLAGS.put("entity_attack_player", false);
        DEFAULT_FLAGS.put("damage", false);
        DEFAULT_FLAGS.put("explosions", false);
        DEFAULT_FLAGS.put("entity_item_drop", false);
        DEFAULT_FLAGS.put("entity_item_pickup", false);
        DEFAULT_FLAGS.put("player_item_drop", false);
        DEFAULT_FLAGS.put("liquid_flow", false);
        DEFAULT_FLAGS.put("hungers", false);
        DEFAULT_FLAGS.put("enter", true);
        DEFAULT_FLAGS.put("leave", true);
    }

    private final Map<String, Area> areas = new LinkedHashMap<>();
    private final Map<UUID, String> selectionWorld = new HashMap<>();
    private final Map<UUID, Location[]> selections = new HashMap<>();
    /** area name the player stands in, for enter/leave detection */
    private final Map<UUID, String> lastArea = new HashMap<>();

    public ProtectModule(SamplesEssentials plugin) {
        super("world", "protect", "protect-3.4.1-all.jar",
                "Cuboid land claims with owners, members and per-area flags");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.protect", "Use /area", PermissionDefault.TRUE);
        ensurePermission("samplese.protect.admin", "Manage all areas", PermissionDefault.OP);
        ensurePermission("samplese.protect.bypass", "Bypass all area protection", PermissionDefault.OP);
        ensurePermission("samplese.protect.unlimited", "Create unlimited areas", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        loadAreas();
        command("area", new AreaCommand());
        command("protect", new AreaCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
        syncRepeat(5L, 5L, this::checkEnterLeave);
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private File areasFolder() {
        File folder = new File(getModuleFolder(), "areas");
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    private File areaFile(String name) {
        return new File(areasFolder(), name.toLowerCase(Locale.ROOT) + ".yml");
    }

    private void loadAreas() {
        areas.clear();
        File[] files = areasFolder().listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            Area area = new Area();
            area.name = yaml.getString("name", file.getName().replace(".yml", ""));
            area.world = yaml.getString("world");
            area.minX = yaml.getInt("min.x");
            area.minY = yaml.getInt("min.y");
            area.minZ = yaml.getInt("min.z");
            area.maxX = yaml.getInt("max.x");
            area.maxY = yaml.getInt("max.y");
            area.maxZ = yaml.getInt("max.z");
            String owner = yaml.getString("owner");
            if (owner != null) {
                try {
                    area.owner = UUID.fromString(owner);
                } catch (IllegalArgumentException ignored) {
                    // keep null owner for console-created areas
                }
            }
            area.ownerName = yaml.getString("owner-name", "?");
            for (String member : yaml.getStringList("members")) {
                try {
                    area.members.add(UUID.fromString(member));
                } catch (IllegalArgumentException ignored) {
                    // skip malformed member entries
                }
            }
            area.priority = yaml.getInt("priority", 0);
            var flags = yaml.getConfigurationSection("flags");
            if (flags != null) {
                for (String key : flags.getKeys(false)) {
                    area.flags.put(key, flags.get(key));
                }
            }
            if (area.world != null) {
                areas.put(area.name.toLowerCase(Locale.ROOT), area);
            }
        }
        plugin.getLogger().info("Protect areas loaded: " + areas.size());
    }

    private void saveArea(Area area) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", area.name);
        yaml.set("world", area.world);
        yaml.set("min.x", area.minX);
        yaml.set("min.y", area.minY);
        yaml.set("min.z", area.minZ);
        yaml.set("max.x", area.maxX);
        yaml.set("max.y", area.maxY);
        yaml.set("max.z", area.maxZ);
        if (area.owner != null) {
            yaml.set("owner", area.owner.toString());
        }
        yaml.set("owner-name", area.ownerName);
        List<String> members = new ArrayList<>();
        for (UUID member : area.members) {
            members.add(member.toString());
        }
        yaml.set("members", members);
        yaml.set("priority", area.priority);
        for (Map.Entry<String, Object> flag : area.flags.entrySet()) {
            yaml.set("flags." + flag.getKey(), flag.getValue());
        }
        try {
            yaml.save(areaFile(area.name));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save area " + area.name + ": " + e.getMessage());
        }
    }

    private void deleteAreaFile(String name) {
        // noinspection ResultOfMethodCallIgnored
        areaFile(name).delete();
    }

    /* ------------------------------------------------------------------ */
    /* lookup                                                             */
    /* ------------------------------------------------------------------ */

    /** Highest-priority area containing the location, or {@code null}. */
    private Area areaAt(Location location) {
        Area best = null;
        for (Area area : areas.values()) {
            if (!area.contains(location)) {
                continue;
            }
            if (best == null || area.priority > best.priority) {
                best = area;
            }
        }
        return best;
    }

    private Area areaNamed(String name) {
        return name == null ? null : areas.get(name.toLowerCase(Locale.ROOT));
    }

    /**
     * Applies a flag. {@code true} = denied for untrusted players, {@code false} =
     * explicitly allowed, unset = vanilla. Returns {@code true} when the action must
     * be blocked.
     */
    private boolean blocked(Player player, String flag, Location location) {
        Area area = areaAt(location);
        if (area == null || area.trusted(player)) {
            return false;
        }
        Boolean value = area.flag(flag);
        if (value == null) {
            return false;
        }
        if (!value) {
            notify(player, area);
        }
        return !value;
    }

    private void notify(Player player, Area area) {
        Boolean notify = area.flag("notify_failed_interactions");
        if (notify != null && !notify) {
            return;
        }
        if (notify == null && !cfgBool("notify-failed-interactions", false)) {
            return;
        }
        reply(player, "flag-denied", "area", area.name);
    }

    private void checkEnterLeave() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Area area = areaAt(player.getLocation());
            String current = area == null ? null : area.name.toLowerCase(Locale.ROOT);
            String previous = lastArea.put(player.getUniqueId(), current);
            if (java.util.Objects.equals(current, previous)) {
                continue;
            }
            Area old = previous == null ? null : areas.get(previous);
            if (old != null && Boolean.TRUE.equals(old.flag("leave"))) {
                sendMessageFlags(player, old, "farewell");
            }
            if (area != null && Boolean.TRUE.equals(area.flag("enter"))) {
                sendMessageFlags(player, area, "greetings");
            }
        }
    }

    private void sendMessageFlags(Player player, Area area, String prefix) {
        String chat = area.stringFlag(prefix);
        if (chat != null) {
            player.sendMessage(Text.parse(chat.replace("<area>", area.name)
                    .replace("{area}", area.name)
                    .replace("<player>", player.getName())));
        }
        String actionbar = area.stringFlag(prefix + "_actionbar");
        if (actionbar != null) {
            player.sendActionBar(Text.parse(actionbar.replace("<area>", area.name)));
        }
        String title = area.stringFlag(prefix + "_title");
        if (title != null) {
            player.showTitle(net.kyori.adventure.title.Title.title(
                    Text.parse(title.replace("<area>", area.name)),
                    Component.empty(),
                    net.kyori.adventure.title.Title.Times.times(
                            java.time.Duration.ZERO, java.time.Duration.ofMillis(2000),
                            java.time.Duration.ofMillis(500))));
        }
    }

    /* ------------------------------------------------------------------ */
    /* protection listeners                                               */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (blocked(event.getPlayer(), "break", event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (blocked(event.getPlayer(), "place", event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        boolean physical = event.getAction() == org.bukkit.event.block.Action.PHYSICAL;
        String flag = physical ? "physical_interact" : "interact";
        if (blocked(event.getPlayer(), flag, event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (blocked(event.getPlayer(), "place", event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (blocked(event.getPlayer(), "break", event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = null;
        if (event.getDamager() instanceof Player direct) {
            attacker = direct;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null) {
            // environment damage inside a protected area
            if (blocked(victim, "damage", victim.getLocation())) {
                event.setCancelled(true);
            }
            return;
        }
        if (blocked(attacker, "player_attack_player", victim.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (blocked(attacker, "player_attack_entity", event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Area area = areaAt(block.getLocation());
            if (area == null) {
                return false;
            }
            Boolean value = area.flag("explosions");
            return value != null && !value;
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (blocked(event.getPlayer(), "player_item_drop", event.getPlayer().getLocation())) {
            event.setCancelled(true);
        }
    }

    /* ------------------------------------------------------------------ */
    /* selection wand                                                     */
    /* ------------------------------------------------------------------ */

    private ItemStack wand() {
        ItemStack item = new ItemStack(Material.GOLDEN_SHOVEL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(msg("wand-name")));
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "protect_wand"),
                PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.GOLDEN_SHOVEL && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(plugin, "protect_wand"), PersistentDataType.BOOLEAN);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWandClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !isWand(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        Location[] corners = selections.computeIfAbsent(player.getUniqueId(), uuid -> new Location[2]);
        Block block = event.getClickedBlock();
        selectionWorld.put(player.getUniqueId(), block.getWorld().getName());
        if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
            corners[0] = block.getLocation();
            reply(player, "pos1", "location", formatLocation(block.getLocation()));
        } else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            corners[1] = block.getLocation();
            reply(player, "pos2", "location", formatLocation(block.getLocation()));
        }
    }

    private String formatLocation(Location location) {
        return location.getWorld().getName() + " " + location.getBlockX() + ", "
                + location.getBlockY() + ", " + location.getBlockZ();
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class AreaCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "create" -> create(sender, args);
                case "delete", "remove" -> delete(sender, args);
                case "list" -> list(sender);
                case "info" -> info(sender, args);
                case "select", "wand" -> select(sender);
                case "redefine" -> redefine(sender, args);
                case "teleport", "tp" -> teleport(sender, args);
                case "priority" -> priority(sender, args);
                case "owner", "setowner" -> owner(sender, args);
                case "member", "members" -> member(sender, args);
                case "flag", "flags" -> flag(sender, args);
                case "rename" -> rename(sender, args);
                default -> sendHelp(sender);
            }
            return true;
        }

        private void sendHelp(CommandSender sender) {
            for (String line : cfgStringList("help-lines")) {
                sender.sendMessage(Text.parse(line));
            }
        }

        private boolean admin(CommandSender sender, Area area) {
            if (sender.hasPermission("samplese.protect.admin")) {
                return true;
            }
            if (area.owner != null && sender instanceof Player player
                    && area.owner.equals(player.getUniqueId())) {
                return true;
            }
            reply(sender, "not-owner");
            return false;
        }

        private void create(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/area create <name>");
                return;
            }
            String name = args[1];
            if (areaNamed(name) != null) {
                reply(sender, "already-exists", "area", name);
                return;
            }
            if (!player.hasPermission("samplese.protect.unlimited")) {
                int limit = Math.max(1, cfgInt("max-areas-per-player", 5));
                long owned = areas.values().stream()
                        .filter(area -> player.getUniqueId().equals(area.owner)).count();
                if (owned >= limit) {
                    reply(sender, "limit-reached", "limit", String.valueOf(limit));
                    return;
                }
            }
            Location[] corners = selections.get(player.getUniqueId());
            if (corners == null || corners[0] == null || corners[1] == null) {
                reply(player, "no-selection");
                return;
            }
            Area area = new Area();
            area.name = name;
            area.world = corners[0].getWorld().getName();
            area.minX = Math.min(corners[0].getBlockX(), corners[1].getBlockX());
            area.maxX = Math.max(corners[0].getBlockX(), corners[1].getBlockX());
            area.minY = Math.min(corners[0].getBlockY(), corners[1].getBlockY());
            area.maxY = Math.max(corners[0].getBlockY(), corners[1].getBlockY());
            area.minZ = Math.min(corners[0].getBlockZ(), corners[1].getBlockZ());
            area.maxZ = Math.max(corners[0].getBlockZ(), corners[1].getBlockZ());
            area.owner = player.getUniqueId();
            area.ownerName = player.getName();
            area.flags.putAll(DEFAULT_FLAGS);
            areas.put(name.toLowerCase(Locale.ROOT), area);
            saveArea(area);
            selections.remove(player.getUniqueId());
            reply(player, "created", "area", name,
                    "size", (area.maxX - area.minX + 1) + "x" + (area.maxY - area.minY + 1)
                            + "x" + (area.maxZ - area.minZ + 1));
        }

        private void delete(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/area delete <name>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            areas.remove(area.name.toLowerCase(Locale.ROOT));
            deleteAreaFile(area.name);
            reply(sender, "deleted", "area", area.name);
        }

        private void list(CommandSender sender) {
            reply(sender, "list-header", "count", String.valueOf(areas.size()));
            for (Area area : areas.values()) {
                sender.sendMessage(Text.parse(msg("list-entry",
                        "area", area.name,
                        "owner", area.ownerName,
                        "world", area.world)));
            }
        }

        private void info(CommandSender sender, String[] args) {
            if (args.length < 2) {
                if (sender instanceof Player player) {
                    Area current = areaAt(player.getLocation());
                    if (current == null) {
                        reply(sender, "none-here");
                        return;
                    }
                    showInfo(sender, current);
                    return;
                }
                reply(sender, "usage", "usage", "/area info <name>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            showInfo(sender, area);
        }

        private void showInfo(CommandSender sender, Area area) {
            reply(sender, "info-header", "area", area.name);
            sender.sendMessage(Text.parse(msg("info-owner", "owner", area.ownerName)));
            sender.sendMessage(Text.parse(msg("info-world", "world", area.world)));
            sender.sendMessage(Text.parse(msg("info-bounds",
                    "min", area.minX + ", " + area.minY + ", " + area.minZ,
                    "max", area.maxX + ", " + area.maxY + ", " + area.maxZ)));
            sender.sendMessage(Text.parse(msg("info-size",
                    "size", (area.maxX - area.minX + 1) + "x" + (area.maxY - area.minY + 1)
                            + "x" + (area.maxZ - area.minZ + 1),
                    "priority", String.valueOf(area.priority),
                    "members", String.valueOf(area.members.size()))));
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Object> flag : area.flags.entrySet()) {
                parts.add(flag.getKey() + "=" + flag.getValue());
            }
            if (!parts.isEmpty()) {
                sender.sendMessage(Text.parse(msg("info-flags", "flags", String.join(", ", parts))));
            }
        }

        private void select(CommandSender sender) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (!player.hasPermission("samplese.protect")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return;
            }
            player.getInventory().addItem(wand());
            reply(player, "wand-given");
        }

        private void redefine(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/area redefine <name>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            Location[] corners = selections.get(player.getUniqueId());
            if (corners == null || corners[0] == null || corners[1] == null) {
                reply(player, "no-selection");
                return;
            }
            if (!corners[0].getWorld().equals(corners[1].getWorld())) {
                reply(player, "different-worlds");
                return;
            }
            area.world = corners[0].getWorld().getName();
            area.minX = Math.min(corners[0].getBlockX(), corners[1].getBlockX());
            area.maxX = Math.max(corners[0].getBlockX(), corners[1].getBlockX());
            area.minY = Math.min(corners[0].getBlockY(), corners[1].getBlockY());
            area.maxY = Math.max(corners[0].getBlockY(), corners[1].getBlockY());
            area.minZ = Math.min(corners[0].getBlockZ(), corners[1].getBlockZ());
            area.maxZ = Math.max(corners[0].getBlockZ(), corners[1].getBlockZ());
            saveArea(area);
            selections.remove(player.getUniqueId());
            reply(player, "redefined", "area", area.name);
        }

        private void teleport(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/area tp <name>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            World world = plugin.getServer().getWorld(area.world);
            if (world == null) {
                reply(sender, "world-missing", "world", area.world);
                return;
            }
            Location center = new Location(world,
                    (area.minX + area.maxX) / 2.0 + 0.5,
                    (area.minY + area.maxY) / 2.0 + 0.5,
                    (area.minZ + area.maxZ) / 2.0 + 0.5);
            player.teleportAsync(center);
            reply(player, "teleported", "area", area.name);
        }

        private void priority(CommandSender sender, String[] args) {
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/area priority <name> <value>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            try {
                area.priority = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                reply(sender, "usage", "usage", "/area priority <name> <value>");
                return;
            }
            saveArea(area);
            reply(sender, "priority-set", "area", area.name, "value", String.valueOf(area.priority));
        }

        private void owner(CommandSender sender, String[] args) {
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/area owner <name> <player>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!sender.hasPermission("samplese.protect.admin")) {
                reply(sender, "not-owner");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[2]));
                return;
            }
            area.owner = target.getUniqueId();
            area.ownerName = target.getName();
            saveArea(area);
            reply(sender, "owner-set", "area", area.name, "player", target.getName());
        }

        private void member(CommandSender sender, String[] args) {
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/area member <name> <add|remove|list> [player]");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            String action = args[2].toLowerCase(Locale.ROOT);
            if (action.equals("list")) {
                reply(sender, "member-list", "area", area.name, "count", String.valueOf(area.members.size()));
                for (UUID uuid : area.members) {
                    String name = plugin.getServer().getOfflinePlayer(uuid).getName();
                    sender.sendMessage(Text.parse(msg("member-entry", "player", name == null ? uuid.toString() : name)));
                }
                return;
            }
            if (args.length < 4) {
                reply(sender, "usage", "usage", "/area member <name> <add|remove> <player>");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[3]);
            if (target == null) {
                sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[3]));
                return;
            }
            if (action.equals("add")) {
                area.members.add(target.getUniqueId());
                saveArea(area);
                reply(sender, "member-added", "area", area.name, "player", target.getName());
            } else if (action.equals("remove")) {
                area.members.remove(target.getUniqueId());
                saveArea(area);
                reply(sender, "member-removed", "area", area.name, "player", target.getName());
            } else {
                reply(sender, "usage", "usage", "/area member <name> <add|remove|list> [player]");
            }
        }

        private void flag(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/area flag <name> [flag] [value]");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            if (args.length == 2) {
                for (Map.Entry<String, Object> entry : area.flags.entrySet()) {
                    sender.sendMessage(Text.parse(msg("flag-line",
                            "flag", entry.getKey(), "value", String.valueOf(entry.getValue()))));
                }
                return;
            }
            String flag = args[2].toLowerCase(Locale.ROOT);
            if (!area.flags.containsKey(flag) && !DEFAULT_FLAGS.containsKey(flag)) {
                reply(sender, "unknown-flag", "flag", flag);
                return;
            }
            if (args.length == 3) {
                reply(sender, "flag-value", "flag", flag,
                        "value", String.valueOf(area.flags.get(flag)));
                return;
            }
            String raw = args[3].toLowerCase(Locale.ROOT);
            Object value;
            if (raw.equals("true") || raw.equals("false")) {
                value = Boolean.valueOf(raw);
            } else if (raw.equals("unset") || raw.equals("null")) {
                value = null;
            } else {
                value = args[3];
            }
            if (value == null) {
                area.flags.remove(flag);
            } else {
                area.flags.put(flag, value);
            }
            saveArea(area);
            reply(sender, "flag-set", "area", area.name, "flag", flag,
                    "value", value == null ? "unset" : value.toString());
        }

        private void rename(CommandSender sender, String[] args) {
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/area rename <name> <new>");
                return;
            }
            Area area = areaNamed(args[1]);
            if (area == null) {
                reply(sender, "not-found", "area", args[1]);
                return;
            }
            if (!admin(sender, area)) {
                return;
            }
            if (areaNamed(args[2]) != null) {
                reply(sender, "already-exists", "area", args[2]);
                return;
            }
            areas.remove(area.name.toLowerCase(Locale.ROOT));
            deleteAreaFile(area.name);
            area.name = args[2];
            areas.put(area.name.toLowerCase(Locale.ROOT), area);
            saveArea(area);
            reply(sender, "renamed", "old", args[1], "new", area.name);
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("create", "delete", "list", "info",
                        "select", "redefine", "tp", "priority", "owner", "member", "flag", "rename"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2) {
                String sub = args[0].toLowerCase(Locale.ROOT);
                if (!sub.equals("create") && !sub.equals("select")) {
                    List<String> names = new ArrayList<>();
                    areas.values().forEach(area -> names.add(area.name));
                    return names;
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("flag")) {
                return new ArrayList<>(DEFAULT_FLAGS.keySet());
            }
            if (args.length == 4 && args[0].equalsIgnoreCase("flag")) {
                return List.of("true", "false", "unset");
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
