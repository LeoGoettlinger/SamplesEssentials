package su.samplecraft.essentials.modules.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code BuildPastePlugin-2.02.jar} (de.mistrx.buildpaste).
 * <p>
 * The original copied a cuboid selection into a remote Firebase build service and
 * pasted it back by id. That service no longer exists, so this port keeps the same
 * commands but stores builds locally in {@code builds/<id>.yml} (a block palette
 * plus one string per layer, so files stay diffable and editable):
 * <pre>
 * /setfirstpos | /setsecondpos   - select the two corners with the selector wand
 * /setbuild &lt;id&gt;                - save the selection as &lt;id&gt;
 * /paste &lt;id&gt; [direction]        - paste at your feet in the given direction
 * /sharebuild &lt;id&gt;              - tell everyone the id
 * /removepos                    - clear your selection
 * /undo                         - undo your last paste
 * /construct                    - list your builds
 * </pre>
 * Direction mapping matches the original ({@code north/east/south/west}, derived
 * from the player's look direction). Undo keeps up to
 * {@code undo-history-size} pastes per player.
 */
public final class BuildPasteModule extends EssentialsModule {

    private record SavedBlock(int x, int y, int z, String data) {
    }

    private static final class Selection {
        Location first;
        Location second;
    }

    private static final class Undo {
        final String world;
        final int originX;
        final int originY;
        final int originZ;
        final List<SavedBlock> before;

        Undo(String world, int originX, int originY, int originZ, List<SavedBlock> before) {
            this.world = world;
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.before = before;
        }
    }

    private final Map<UUID, Selection> selections = new HashMap<>();
    private final Map<UUID, Deque<Undo>> undos = new HashMap<>();

    public BuildPasteModule(SamplesEssentials plugin) {
        super("world", "buildpaste", "BuildPastePlugin-2.02.jar",
                "Save cuboid builds to files, share them by id and paste them back with undo");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.buildpaste.paste", "Paste builds", PermissionDefault.OP);
        ensurePermission("samplese.buildpaste.setbuild", "Save builds", PermissionDefault.OP);
        ensurePermission("samplese.buildpaste.share", "Share builds", PermissionDefault.OP);
        ensurePermission("samplese.buildpaste.undo", "Undo pastes", PermissionDefault.OP);
        ensurePermission("samplese.buildpaste.admin", "Manage all builds", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        for (String label : List.of("setfirstpos", "setsecondpos", "setbuild", "paste",
                "sharebuild", "removepos", "undo", "construct")) {
            command(label, new BuildCommand(label));
        }
        onDisableHook(this::unbindCommands);
        listen(new WandListener());
    }

    /* ------------------------------------------------------------------ */
    /* selection                                                          */
    /* ------------------------------------------------------------------ */

    private Selection selection(Player player) {
        return selections.computeIfAbsent(player.getUniqueId(), uuid -> new Selection());
    }

    private File buildsFolder() {
        File folder = new File(getModuleFolder(), "builds");
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    private File buildFile(String id) {
        String safe = id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "_");
        return new File(buildsFolder(), safe + ".yml");
    }

    private List<String> buildIds() {
        List<String> ids = new ArrayList<>();
        File[] files = buildsFolder().listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                ids.add(file.getName().substring(0, file.getName().length() - 4));
            }
        }
        return ids;
    }

    /* ------------------------------------------------------------------ */
    /* save / paste                                                       */
    /* ------------------------------------------------------------------ */

    private String save(Player player, String id) {
        Selection selection = selection(player);
        if (selection.first == null || selection.second == null) {
            reply(player, "no-selection");
            return null;
        }
        if (!selection.first.getWorld().equals(selection.second.getWorld())) {
            reply(player, "different-worlds");
            return null;
        }
        int maxSize = Math.max(1, cfgInt("max-size", 64));
        int minX = Math.min(selection.first.getBlockX(), selection.second.getBlockX());
        int maxX = Math.max(selection.first.getBlockX(), selection.second.getBlockX());
        int minY = Math.min(selection.first.getBlockY(), selection.second.getBlockY());
        int maxY = Math.max(selection.first.getBlockY(), selection.second.getBlockY());
        int minZ = Math.min(selection.first.getBlockZ(), selection.second.getBlockZ());
        int maxZ = Math.max(selection.first.getBlockZ(), selection.second.getBlockZ());
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX > maxSize || sizeY > maxSize || sizeZ > maxSize) {
            reply(player, "too-large", "max", String.valueOf(maxSize),
                    "size", sizeX + "x" + sizeY + "x" + sizeZ);
            return null;
        }
        World world = selection.first.getWorld();
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("id", id);
        yaml.set("size.x", sizeX);
        yaml.set("size.y", sizeY);
        yaml.set("size.z", sizeZ);
        yaml.set("owner", player.getUniqueId().toString());
        yaml.set("owner-name", player.getName());
        // palette keeps the file compact: repeated block data is written once
        Map<String, Integer> palette = new HashMap<>();
        List<String> layers = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            List<String> rows = new ArrayList<>();
            for (int z = 0; z < sizeZ; z++) {
                StringBuilder row = new StringBuilder(sizeX);
                for (int x = 0; x < sizeX; x++) {
                    Block block = world.getBlockAt(minX + x, minY + y, minZ + z);
                    String data = block.getBlockData().getAsString(true);
                    Integer index = palette.get(data);
                    if (index == null) {
                        index = palette.size();
                        palette.put(data, index);
                        yaml.set("palette." + index, data);
                    }
                    row.append((char) ('A' + index % 26));
                    if (index >= 26) {
                        // rare palettes: fall back to a decimal marker
                        row.setLength(row.length() - 1);
                        row.append('#').append(index).append(';');
                    }
                }
                rows.add(row.toString());
            }
            layers.addAll(rows);
        }
        yaml.set("rows", layers);
        try {
            yaml.save(buildFile(id));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save build " + id + ": " + e.getMessage());
            reply(player, "save-failed");
            return null;
        }
        reply(player, "saved", "id", id, "size", sizeX + "x" + sizeY + "x" + sizeZ,
                "blocks", String.valueOf(sizeX * sizeY * sizeZ));
        return id;
    }

    /** @return number of blocks placed, or -1 on failure */
    private int paste(Player player, String id, String direction) {
        File file = buildFile(id);
        if (!file.isFile()) {
            reply(player, "not-found", "id", id);
            return -1;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int sizeX = yaml.getInt("size.x", 0);
        int sizeY = yaml.getInt("size.y", 0);
        int sizeZ = yaml.getInt("size.z", 0);
        List<String> rows = yaml.getStringList("rows");
        List<String> palette = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection("palette");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                palette.add(section.getString(key));
            }
        }
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0 || rows.isEmpty() || palette.isEmpty()) {
            reply(player, "corrupt", "id", id);
            return -1;
        }
        World world = player.getWorld();
        int originX = player.getLocation().getBlockX();
        int originY = player.getLocation().getBlockY();
        int originZ = player.getLocation().getBlockZ();
        if (cfgBool("place-at-player", true)) {
            originY = Math.max(world.getMinHeight(), originY);
        }
        boolean swap = "east".equals(direction) || "west".equals(direction);
        int mapX = swap ? sizeZ : sizeX;
        int mapZ = swap ? sizeX : sizeZ;
        // remember the previous blocks for /undo
        List<SavedBlock> before = new ArrayList<>();
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < mapZ; z++) {
                for (int x = 0; x < mapX; x++) {
                    Block block = world.getBlockAt(originX + x, originY + y, originZ + z);
                    if (before.size() < 20000) {
                        before.add(new SavedBlock(x, y, z, block.getBlockData().getAsString(true)));
                    }
                }
            }
        }
        int placed = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                String row = rows.get(y * sizeZ + z);
                int[] cursor = {0};
                for (int x = 0; x < sizeX; x++) {
                    Integer index = readIndex(row, cursor);
                    if (index == null || index >= palette.size()) {
                        continue;
                    }
                    String data = palette.get(index);
                    if (data == null || data.startsWith("minecraft:air")) {
                        continue;
                    }
                    int[] target = rotatePoint(x, z, sizeX, sizeZ, direction);
                    Block block = world.getBlockAt(originX + target[0], originY + y, originZ + target[1]);
                    try {
                        block.setBlockData(plugin.getServer().createBlockData(data), false);
                        placed++;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Build " + id + ": bad block data " + data);
                    }
                }
            }
        }
        Deque<Undo> history = undos.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayDeque<>());
        history.push(new Undo(world.getName(), originX, originY, originZ, before));
        int limit = Math.max(1, cfgInt("undo-history-size", 5));
        while (history.size() > limit) {
            history.removeLast();
        }
        return placed;
    }

    /** A/B/C… palette decoding; {@code #n;} markers handle palettes over 26 entries. */
    private Integer readIndex(String row, int[] cursor) {
        if (cursor[0] >= row.length()) {
            return null;
        }
        char c = row.charAt(cursor[0]);
        if (c == '#') {
            int end = row.indexOf(';', cursor[0]);
            if (end < 0) {
                return null;
            }
            String number = row.substring(cursor[0] + 1, end);
            cursor[0] = end + 1;
            try {
                return Integer.parseInt(number);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        cursor[0]++;
        int value = c - 'A';
        return value >= 0 && value < 26 ? value : null;
    }

    /** Maps a source coordinate into the pasted orientation. */
    private int[] rotatePoint(int x, int z, int sizeX, int sizeZ, String direction) {
        int outX;
        int outZ;
        switch (direction) {
            case "south" -> {
                outX = sizeX - 1 - x;
                outZ = sizeZ - 1 - z;
            }
            case "east" -> {
                outX = z;
                outZ = sizeX - 1 - x;
            }
            case "west" -> {
                outX = sizeZ - 1 - z;
                outZ = x;
            }
            default -> {
                outX = x;
                outZ = z;
            }
        }
        return new int[]{outX, outZ};
    }

    private void undo(Player player) {
        Deque<Undo> history = undos.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            reply(player, "nothing-to-undo");
            return;
        }
        Undo undo = history.pop();
        World world = plugin.getServer().getWorld(undo.world);
        if (world == null) {
            reply(player, "world-missing");
            return;
        }
        int restored = 0;
        for (SavedBlock block : undo.before) {
            try {
                world.getBlockAt(undo.originX + block.x(), undo.originY + block.y(), undo.originZ + block.z())
                        .setBlockData(plugin.getServer().createBlockData(block.data()), false);
                restored++;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Undo: bad block data " + block.data());
            }
        }
        reply(player, "undo-done", "count", String.valueOf(restored));
    }

    private String lookDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        if (yaw >= 45 && yaw < 135) {
            return "west";
        }
        if (yaw >= 135 && yaw < 225) {
            return "north";
        }
        if (yaw >= 225 && yaw < 315) {
            return "east";
        }
        return "south";
    }

    /* ------------------------------------------------------------------ */
    /* items                                                              */
    /* ------------------------------------------------------------------ */

    private ItemStack selectorWand() {
        ItemStack item = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(msg("wand-name")));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(Text.parse(msg("wand-lore-1")));
        lore.add(Text.parse(msg("wand-lore-2")));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "buildpaste_wand"),
                PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.GOLDEN_AXE && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(plugin, "buildpaste_wand"), PersistentDataType.BOOLEAN);
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class BuildCommand implements CommandExecutor, TabCompleter {
        private final String label;

        BuildCommand(String label) {
            this.label = label;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String alias, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return true;
            }
            switch (label.toLowerCase(Locale.ROOT)) {
                case "setfirstpos" -> {
                    if (!require(player, "samplese.buildpaste.setbuild")) {
                        return true;
                    }
                    selection(player).first = player.getLocation().toBlockLocation();
                    player.getInventory().addItem(selectorWand());
                    reply(player, "pos1", "location", formatLocation(selection(player).first));
                }
                case "setsecondpos" -> {
                    if (!require(player, "samplese.buildpaste.setbuild")) {
                        return true;
                    }
                    selection(player).second = player.getLocation().toBlockLocation();
                    player.getInventory().addItem(selectorWand());
                    reply(player, "pos2", "location", formatLocation(selection(player).second));
                }
                case "removepos" -> {
                    selections.remove(player.getUniqueId());
                    reply(player, "selection-cleared");
                }
                case "setbuild" -> {
                    if (!require(player, "samplese.buildpaste.setbuild")) {
                        return true;
                    }
                    if (!requireId(sender, args)) {
                        return true;
                    }
                    save(player, args[0]);
                }
                case "paste" -> {
                    if (!require(player, "samplese.buildpaste.paste")) {
                        return true;
                    }
                    if (!requireId(sender, args)) {
                        return true;
                    }
                    String direction = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : lookDirection(player);
                    int placed = paste(player, args[0], direction);
                    if (placed >= 0) {
                        reply(player, "pasted", "count", String.valueOf(placed), "direction", direction);
                    }
                }
                case "sharebuild" -> {
                    if (!require(player, "samplese.buildpaste.share")) {
                        return true;
                    }
                    if (!requireId(sender, args)) {
                        return true;
                    }
                    if (!buildFile(args[0]).isFile()) {
                        reply(player, "not-found", "id", args[0]);
                        return true;
                    }
                    String message = msg("shared", "id", args[0], "player", player.getName());
                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                        if (online.hasPermission("samplese.buildpaste.paste")) {
                            online.sendMessage(Text.parse(message));
                        }
                    }
                }
                case "undo" -> {
                    if (!require(player, "samplese.buildpaste.undo")) {
                        return true;
                    }
                    undo(player);
                }
                case "construct" -> {
                    List<String> ids = buildIds();
                    reply(player, "list-header", "count", String.valueOf(ids.size()));
                    for (String id : ids) {
                        player.sendMessage(Text.parse(msg("list-entry", "id", id)));
                    }
                }
                default -> reply(player, "usage");
            }
            return true;
        }

        private boolean require(CommandSender sender, String permission) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return false;
            }
            return true;
        }

        private boolean requireId(CommandSender sender, String[] args) {
            if (args.length == 0) {
                reply(sender, "usage");
                return false;
            }
            return true;
        }

        private String formatLocation(Location location) {
            return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1 && List.of("setbuild", "paste", "sharebuild", "construct")
                    .contains(label.toLowerCase(Locale.ROOT))) {
                List<String> ids = buildIds();
                String prefix = args[0].toLowerCase(Locale.ROOT);
                ids.removeIf(id -> !id.startsWith(prefix));
                return ids;
            }
            if (args.length == 2 && label.equalsIgnoreCase("paste")) {
                return List.of("north", "east", "south", "west");
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }

    /** Selector wand clicks set the two corners. */
    private final class WandListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
            if (event.getClickedBlock() == null || !isWand(event.getItem())) {
                return;
            }
            event.setCancelled(true);
            Player player = event.getPlayer();
            Selection selection = selection(player);
            if (event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) {
                selection.first = event.getClickedBlock().getLocation();
                reply(player, "pos1", "location", event.getClickedBlock().getX() + ", "
                        + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ());
            } else if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                selection.second = event.getClickedBlock().getLocation();
                reply(player, "pos2", "location", event.getClickedBlock().getX() + ", "
                        + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ());
            }
        }
    }
}
