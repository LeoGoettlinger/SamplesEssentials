package su.samplecraft.essentials.modules.gameplay;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
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
 * Merged from {@code GravesX-2026.4.9.1.jar} (com.ranull.graves.Graves).
 * <p>
 * Ported behaviour of the original grave plugin:
 * <ul>
 *   <li>on death the player's inventory drops are stored in a grave (no item loss),
 *       placed at a safe location near the death spot (ground / lava-top / water-bottom /
 *       smart handling / falling back to the death coordinates),</li>
 *   <li>the grave is a {@code PLAYER_HEAD} block (configurable to CHEST/BEACON) with an
 *       armour-stand hologram showing owner, remaining time and killer,</li>
 *   <li>right-click opens the grave inventory, all items are returned to the owner,
 *       the block and hologram disappear; only the owner (or an admin with
 *       {@code samplese.graves.other}) may open graves, protected by
 *       {@code grave.protection.open.other},</li>
 *   <li>graves expire after {@code grave.time} seconds, are breakable/explodable only
 *       when configured, can be auto-looted by walking over them and are removed when
 *       an older grave is replaced ({%code replace-oldest} / {@code max}),</li>
 *   <li>{@code /grave list}, {@code /grave teleport|tp [index]}, {@code /grave open},
 *       {@code /grave count}, {@code /grave help} with the original aliases.</li>
 * </ul>
 * Data persists in {@code gameplay/graves/data/graves.yml} (records) and
 * {@code gameplay/graves/data/inventories.yml} (item stacks, Base64 like the original H2 store).
 */
public final class GravesModule extends EssentialsModule implements Listener {

    private record GraveRecord(UUID id, UUID owner, String ownerName, String world,
                               int x, int y, int z, long created, long expires, UUID killer,
                               String killerName, int experience) {
    }

    private final Map<UUID, GraveRecord> graves = new ConcurrentHashMap<>();
    /** grave id -> inventory contents (restored to the owner on open) */
    private final Map<UUID, List<ItemStack>> inventories = new ConcurrentHashMap<>();
    /** grave id -> hologram armour stands */
    private final Map<UUID, List<UUID>> holograms = new ConcurrentHashMap<>();

    private ModuleStorage storage;
    private ModuleStorage inventoryStorage;

    public GravesModule(SamplesEssentials plugin) {
        super("gameplay", "graves", "GravesX-2026.4.9.1.jar", "Death graves with holograms, protection, teleport and expiry");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.graves", "Use graves", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.graves.other", "Open and loot other players' graves", org.bukkit.permissions.PermissionDefault.OP);
        storage = new ModuleStorage(plugin, getDataFolder(), "data/graves.yml");
        inventoryStorage = new ModuleStorage(plugin, getDataFolder(), "data/inventories.yml");
        loadGraves();
        command("grave", new GraveCommand());
        command("graves", new GraveCommand());
        onDisableHook(() -> {
            unbindCommands();
            removeAllHolograms();
            save();
        });
        listen(this);
        syncRepeat(200L, 200L, this::tickGraves);
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private void loadGraves() {
        var section = storage.yaml().getConfigurationSection("graves");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    graves.put(id, new GraveRecord(id,
                            UUID.fromString(section.getString(key + ".owner", id.toString())),
                            section.getString(key + ".owner-name", "?"),
                            section.getString(key + ".world", "world"),
                            section.getInt(key + ".x"), section.getInt(key + ".y"), section.getInt(key + ".z"),
                            section.getLong(key + ".created"),
                            section.getLong(key + ".expires"),
                            section.getString(key + ".killer") == null ? null : UUID.fromString(section.getString(key + ".killer")),
                            section.getString(key + ".killer-name"),
                            section.getInt(key + ".experience")));
                } catch (IllegalArgumentException ignored) {
                    // stale key
                }
            }
        }
        var invSection = inventoryStorage.yaml().getConfigurationSection("inventories");
        if (invSection != null) {
            for (String key : invSection.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    List<ItemStack> items = new ArrayList<>();
                    for (String encoded : invSection.getStringList(key)) {
                        ItemStack item = ItemStack.deserializeBytes(java.util.Base64.getDecoder().decode(encoded));
                        if (item != null) {
                            items.add(item);
                        }
                    }
                    inventories.put(id, items);
                } catch (Exception ignored) {
                    // corrupted entry
                }
            }
        }
        graves.values().forEach(this::spawnHologram);
    }

    private void save() {
        storage.yaml().set("graves", null);
        graves.forEach((id, g) -> {
            String path = "graves." + id;
            storage.yaml().set(path + ".owner", g.owner().toString());
            storage.yaml().set(path + ".owner-name", g.ownerName());
            storage.yaml().set(path + ".world", g.world());
            storage.yaml().set(path + ".x", g.x());
            storage.yaml().set(path + ".y", g.y());
            storage.yaml().set(path + ".z", g.z());
            storage.yaml().set(path + ".created", g.created());
            storage.yaml().set(path + ".expires", g.expires());
            storage.yaml().set(path + ".killer", g.killer() == null ? null : g.killer().toString());
            storage.yaml().set(path + ".killer-name", g.killerName());
            storage.yaml().set(path + ".experience", g.experience());
        });
        storage.save();
        inventoryStorage.yaml().set("inventories", null);
        inventories.forEach((id, items) -> {
            List<String> encoded = new ArrayList<>();
            for (ItemStack item : items) {
                if (item != null && item.getType() != Material.AIR) {
                    encoded.add(java.util.Base64.getEncoder().encodeToString(item.serializeAsBytes()));
                }
            }
            inventoryStorage.yaml().set("inventories." + id, encoded);
        });
        inventoryStorage.save();
    }

    /* ------------------------------------------------------------------ */
    /* death -> grave                                                     */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!player.hasPermission("samplese.graves") || !worldEnabled(player.getWorld().getName())) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        if (drops.isEmpty()) {
            return;
        }
        Location graveLocation = findGraveLocation(player.getLocation());
        if (graveLocation == null) {
            // failure-keep-inventory: true
            return;
        }
        UUID id = UUID.randomUUID();
        long created = System.currentTimeMillis();
        long time = Math.max(0, cfgLong("grave.time", 10800)) * 1000L;
        Player killer = player.getKiller();
        GraveRecord record = new GraveRecord(id, player.getUniqueId(), player.getName(),
                graveLocation.getWorld().getName(), graveLocation.getBlockX(), graveLocation.getBlockY(),
                graveLocation.getBlockZ(), created, time <= 0 ? Long.MAX_VALUE : created + time,
                killer == null ? null : killer.getUniqueId(), killer == null ? null : killer.getName(), 0);
        graves.put(id, record);
        inventories.put(id, drops);
        event.getDrops().clear();
        placeGraveBlock(record);
        spawnHologram(record);
        save();
        player.sendMessage(msgComponent("grave-created",
                "x", String.valueOf(record.x()), "y", String.valueOf(record.y()), "z", String.valueOf(record.z())));
        if (cfgBool("grave.replace-oldest", false)) {
            int max = cfgInt("grave.max", 18);
            List<GraveRecord> owned = ownedGraves(player.getUniqueId());
            while (owned.size() > max) {
                removeGrave(owned.get(0), false);
                owned = ownedGraves(player.getUniqueId());
            }
        }
    }

    private boolean worldEnabled(String world) {
        List<String> worlds = cfgStringList("grave.world");
        return worlds.isEmpty() || worlds.contains("ALL") || worlds.stream().anyMatch(w -> w.equalsIgnoreCase(world));
    }

    /** Simplified placement of GraveManager#findGraveLocation. */
    private Location findGraveLocation(Location death) {
        World world = death.getWorld();
        if (world == null) {
            return null;
        }
        Block feet = death.getBlock();
        if (cfgBool("grave.ground", true) && isReplaceable(feet) && isReplaceable(feet.getRelative(BlockFace.UP))) {
            return feet.getLocation();
        }
        for (int dy = 1; dy <= 4; dy++) {
            Block up = death.clone().add(0, dy, 0).getBlock();
            if (isReplaceable(up) && isReplaceable(up.getRelative(BlockFace.UP))) {
                return up.getLocation();
            }
            Block down = death.clone().subtract(0, dy, 0).getBlock();
            if (isReplaceable(down) && isReplaceable(down.getRelative(BlockFace.UP))) {
                return down.getLocation();
            }
        }
        if (cfgBool("grave.void", true)) {
            return feet.getLocation();
        }
        return null;
    }

    private boolean isReplaceable(Block block) {
        Material type = block.getType();
        return type.isAir() || type == Material.SHORT_GRASS || type == Material.TALL_GRASS
                || type == Material.FERN || type == Material.LARGE_FERN || type == Material.SNOW
                || type == Material.LAVA && cfgBool("grave.lava-top", true)
                || type == Material.WATER && cfgBool("grave.water-bottom", true);
    }

    private void placeGraveBlock(GraveRecord record) {
        if (!cfgBool("grave.block.enabled", true)) {
            return;
        }
        World world = Bukkit.getWorld(record.world());
        if (world == null) {
            return;
        }
        Material material = graveMaterial();
        Block block = world.getBlockAt(record.x(), record.y(), record.z());
        block.setType(material, false);
        if (material == Material.PLAYER_HEAD && block.getState() instanceof org.bukkit.block.Skull skull) {
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(record.owner()));
            skull.update(true, false);
        }
    }

    private Material graveMaterial() {
        String raw = cfgString("grave.block.material", "PLAYER_HEAD").toUpperCase(Locale.ROOT);
        try {
            Material material = Material.valueOf(raw);
            return material.isBlock() ? material : Material.PLAYER_HEAD;
        } catch (IllegalArgumentException e) {
            return Material.PLAYER_HEAD;
        }
    }

    /* ------------------------------------------------------------------ */
    /* hologram (GraveManager#createHologram)                             */
    /* ------------------------------------------------------------------ */

    private void spawnHologram(GraveRecord record) {
        if (!cfgBool("grave.hologram.enabled", true)) {
            return;
        }
        World world = Bukkit.getWorld(record.world());
        if (world == null) {
            return;
        }
        double heightLine = cfgDouble("holo.height-line", cfgDouble("grave.hologram.height-line", 0.28));
        List<String> lines = cfgStringList("grave.hologram.line");
        if (lines.isEmpty()) {
            lines = List.of("&7%owner_name_display%&r's Grave &c☠",
                    "&7%time_alive_remaining_formatted% &r| %state_protection%",
                    "&fKilled By &c%killer_name_display%");
        }
        double offsetX = cfgDouble("grave.hologram.offset.x", 0);
        double offsetY = cfgDouble("grave.hologram.offset.y", -0.28);
        double offsetZ = cfgDouble("grave.hologram.offset.z", 0);
        List<UUID> stands = new ArrayList<>();
        int index = 0;
        for (String line : lines) {
            Component text = Text.parse(applyPlaceholders(line, record));
            Location at = new Location(world, record.x() + 0.5 + offsetX,
                    record.y() + 1.2 + offsetY + (lines.size() - index - 1) * heightLine, record.z() + 0.5 + offsetZ);
            ArmorStand stand = world.spawn(at, ArmorStand.class, entity -> {
                entity.setVisible(false);
                entity.setMarker(cfgBool("grave.hologram.marker", true));
                entity.setGravity(false);
                entity.setInvulnerable(true);
                entity.setSmall(true);
                entity.setCustomNameVisible(true);
                entity.customName(text);
            });
            stands.add(stand.getUniqueId());
            index++;
        }
        holograms.put(record.id(), stands);
    }

    private String applyPlaceholders(String line, GraveRecord record) {
        long remaining = Math.max(0, record.expires() - System.currentTimeMillis()) / 1000L;
        String protection = remaining > 0 ? "&aProtected" : "&cUnprotected";
        return line
                .replace("%owner_name_display%", record.ownerName())
                .replace("%owner_name%", record.ownerName())
                .replace("%killer_name_display%", record.killerName() == null ? "Unknown" : record.killerName())
                .replace("%killer_name%", record.killerName() == null ? "Unknown" : record.killerName())
                .replace("%time_alive_remaining_formatted%", formatDuration(remaining))
                .replace("%state_protection%", protection);
    }

    private void removeHologram(UUID graveId) {
        List<UUID> stands = holograms.remove(graveId);
        if (stands == null) {
            return;
        }
        for (UUID id : stands) {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    private void removeAllHolograms() {
        holograms.keySet().forEach(this::removeHologram);
    }

    private void refreshHolograms() {
        if (!cfgBool("grave.hologram.enabled", true)) {
            return;
        }
        graves.values().forEach(record -> {
            removeHologram(record.id());
            spawnHologram(record);
        });
    }

    /* ------------------------------------------------------------------ */
    /* opening / looting                                                  */
    /* ------------------------------------------------------------------ */

    private void openGrave(Player player, GraveRecord record) {
        List<ItemStack> items = inventories.get(record.id());
        if (items == null) {
            player.sendMessage(msgComponent("grave-missing"));
            return;
        }
        boolean owner = record.owner().equals(player.getUniqueId());
        boolean otherAllowed = cfgBool("grave.protection.open.other", false);
        if (!owner && !otherAllowed && !player.hasPermission("samplese.graves.other")) {
            player.sendMessage(msgComponent("not-your-grave"));
            return;
        }
        int size = Math.min(54, Math.max(9, ((items.size() - 1) / 9 + 1) * 9));
        Inventory inventory = Bukkit.createInventory(null, size, Text.parse(
                cfgString("grave.title", "&8Grave of %owner%").replace("%owner%", record.ownerName())));
        for (int i = 0; i < Math.min(size, items.size()); i++) {
            inventory.setItem(i, items.get(i));
        }
        graveInventories.put(player.getUniqueId(), record.id());
        player.openInventory(inventory);
        if (items.size() > 54) {
            player.sendMessage(msgComponent("grave-too-full"));
        }
        if (items.size() > size) {
            // contents that do not fit the view stay in the grave and are returned on close
            limitLeftovers.put(player.getUniqueId(), new ArrayList<>(items.subList(size, items.size())));
        }
    }

    private final Map<UUID, UUID> graveInventories = new ConcurrentHashMap<>();
    private final Map<UUID, List<ItemStack>> limitLeftovers = new ConcurrentHashMap<>();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID graveId = graveInventories.remove(player.getUniqueId());
        if (graveId == null) {
            return;
        }
        GraveRecord record = graves.get(graveId);
        List<ItemStack> stored = inventories.remove(graveId);
        if (record == null || stored == null) {
            return;
        }
        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                leftovers.add(item);
            }
        }
        List<ItemStack> overflow = limitLeftovers.remove(player.getUniqueId());
        if (overflow != null) {
            leftovers.addAll(overflow);
        }
        removeGrave(record, false);
        if (leftovers.isEmpty()) {
            return;
        }
        if (player.getUniqueId().equals(record.owner())) {
            giveItems(player, leftovers);
            player.sendMessage(msgComponent("grave-looted"));
        } else {
            // A stranger looted the grave: the owner keeps nothing, exactly like the original
            giveItems(player, leftovers);
        }
    }

    private void giveItems(Player player, List<ItemStack> items) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(items.toArray(new ItemStack[0]));
        for (ItemStack item : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    private void removeGrave(GraveRecord record, boolean dropItems) {
        graves.remove(record.id());
        removeHologram(record.id());
        World world = Bukkit.getWorld(record.world());
        if (world != null) {
            Block block = world.getBlockAt(record.x(), record.y(), record.z());
            if (block.getType() == graveMaterial()) {
                block.setType(Material.AIR, false);
            }
        }
        if (dropItems) {
            List<ItemStack> items = inventories.remove(record.id());
            if (items != null && world != null) {
                for (ItemStack item : items) {
                    world.dropItemNaturally(new Location(world, record.x() + 0.5, record.y() + 0.5, record.z() + 0.5), item);
                }
            }
        }
        save();
    }

    private void tickGraves() {
        long now = System.currentTimeMillis();
        List<GraveRecord> expired = graves.values().stream()
                .filter(record -> record.expires() <= now)
                .toList();
        for (GraveRecord record : expired) {
            Player owner = Bukkit.getPlayer(record.owner());
            if (owner != null) {
                Player online = owner;
                sync(() -> {
                    removeGrave(record, true);
                    online.sendMessage(msgComponent("grave-expired"));
                });
            } else {
                removeGrave(record, true);
            }
        }
    }

    private List<GraveRecord> ownedGraves(UUID owner) {
        return graves.values().stream()
                .filter(record -> record.owner().equals(owner))
                .sorted(Comparator.comparingLong(GraveRecord::created))
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        GraveRecord record = graveAt(block);
        if (record == null) {
            return;
        }
        event.setCancelled(true);
        openGrave(event.getPlayer(), record);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!cfgBool("grave.block.walk-over", false) || event.getTo() == null) {
            return;
        }
        GraveRecord record = graveAt(event.getTo().getBlock());
        if (record == null || !record.owner().equals(event.getPlayer().getUniqueId())) {
            return;
        }
        Player player = event.getPlayer();
        sync(() -> {
            List<ItemStack> items = inventories.remove(record.id());
            removeGrave(record, false);
            if (items != null) {
                giveItems(player, items);
            }
            player.sendMessage(msgComponent("grave-looted"));
        });
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        GraveRecord record = graveAt(event.getBlock());
        if (record == null) {
            return;
        }
        boolean owner = record.owner().equals(event.getPlayer().getUniqueId());
        if (!cfgBool("grave.break", true) && !(owner || event.getPlayer().hasPermission("samplese.graves.other"))) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msgComponent("cannot-break"));
            return;
        }
        event.setDropItems(false);
        List<ItemStack> items = inventories.remove(record.id());
        removeGrave(record, false);
        if (items != null) {
            giveItems(event.getPlayer(), items);
        }
        event.getPlayer().sendMessage(msgComponent("grave-looted"));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (cfgBool("grave.explode", false)) {
            return;
        }
        event.blockList().removeIf(block -> graveAt(block) != null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        if (!cfgBool("grave.piston-extend", true)
                && event.getBlocks().stream().anyMatch(block -> graveAt(block) != null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (cfgBool("grave.notify-on-join", true)) {
            List<GraveRecord> owned = ownedGraves(event.getPlayer().getUniqueId());
            if (!owned.isEmpty()) {
                event.getPlayer().sendMessage(msgComponent("grave-pending", "count", String.valueOf(owned.size())));
            }
        }
    }

    private GraveRecord graveAt(Block block) {
        Location location = block.getLocation();
        for (GraveRecord record : graves.values()) {
            if (record.world().equals(location.getWorld().getName())
                    && record.x() == location.getBlockX()
                    && record.y() == location.getBlockY()
                    && record.z() == location.getBlockZ()) {
                return record;
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
    /* command (GravesCommand)                                            */
    /* ------------------------------------------------------------------ */

    private final class GraveCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull org.bukkit.command.Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "list", "gui" -> {
                    List<GraveRecord> owned = ownedGraves(player.getUniqueId());
                    if (owned.isEmpty()) {
                        player.sendMessage(msgComponent("grave-list-empty"));
                        return true;
                    }
                    player.sendMessage(msgComponent("grave-list-header", "count", String.valueOf(owned.size())));
                    int index = 1;
                    for (GraveRecord record : owned) {
                        long remaining = Math.max(0, record.expires() - System.currentTimeMillis()) / 1000L;
                        player.sendMessage(msgComponent("grave-list-entry",
                                "index", String.valueOf(index++),
                                "world", record.world(),
                                "x", String.valueOf(record.x()),
                                "y", String.valueOf(record.y()),
                                "z", String.valueOf(record.z()),
                                "time", formatDuration(remaining)));
                    }
                }
                case "teleport", "tp" -> {
                    List<GraveRecord> owned = ownedGraves(player.getUniqueId());
                    if (owned.isEmpty()) {
                        player.sendMessage(msgComponent("grave-list-empty"));
                        return true;
                    }
                    int index = 1;
                    if (args.length > 1) {
                        try {
                            index = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            player.sendMessage(msgComponent("grave-invalid-index"));
                            return true;
                        }
                    }
                    if (index < 1 || index > owned.size()) {
                        player.sendMessage(msgComponent("grave-invalid-index"));
                        return true;
                    }
                    GraveRecord record = owned.get(index - 1);
                    World world = Bukkit.getWorld(record.world());
                    if (world == null) {
                        player.sendMessage(msgComponent("grave-world-missing", "world", record.world()));
                        return true;
                    }
                    player.teleportAsync(new Location(world, record.x() + 0.5, record.y() + 1, record.z() + 0.5))
                            .thenAccept(success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    player.sendMessage(msgComponent("grave-teleported"));
                                }
                            });
                }
                case "open", "virtual" -> {
                    List<GraveRecord> owned = ownedGraves(player.getUniqueId());
                    if (owned.isEmpty()) {
                        player.sendMessage(msgComponent("grave-list-empty"));
                        return true;
                    }
                    openGrave(player, owned.get(0));
                }
                case "count" -> player.sendMessage(msgComponent("grave-count",
                        "count", String.valueOf(graves.size()),
                        "own", String.valueOf(ownedGraves(player.getUniqueId()).size())));
                case "reload" -> {
                    if (!player.hasPermission("samplese.graves.other")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    reloadConfig();
                    refreshHolograms();
                    player.sendMessage(msgComponent("reloaded"));
                }
                default -> {
                    player.sendMessage(msgComponent("grave-help-header"));
                    for (String line : new String[]{"/grave list - your graves", "/grave tp [n] - teleport to a grave",
                            "/grave open - open your newest grave", "/grave count - grave statistics"}) {
                        player.sendMessage(Text.parse("<gray>" + line + "</gray>"));
                    }
                }
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                          @NotNull org.bukkit.command.Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("list", "tp", "open", "count", "reload").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private long cfgLong(String path, long def) {
        return config().getLong(path, def);
    }

    private static String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "0s";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder out = new StringBuilder();
        if (days > 0) {
            out.append(days).append("d ");
        }
        if (hours > 0) {
            out.append(hours).append("h ");
        }
        if (minutes > 0) {
            out.append(minutes).append("m ");
        }
        if (secs > 0 || out.isEmpty()) {
            out.append(secs).append("s");
        }
        return out.toString().trim();
    }
}
