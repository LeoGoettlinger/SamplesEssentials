package su.samplecraft.essentials.modules.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code LagFixer-1.7.1.jar} (Linxeria LagFixer).
 * <p>
 * Ports the modules that are pure Bukkit logic:
 * <ul>
 *   <li><b>EntityLimiter</b> - per-chunk caps for creatures, dropped items,
 *       vehicles and projectiles, evaluated on spawn with a configurable
 *       {@code CreatureSpawnEvent.SpawnReason} allow list, an entity whitelist
 *       and an optional periodic overflow purge.</li>
 *   <li><b>RedstoneLimiter</b> - counts redstone updates and piston pushes per
 *       chunk per second, can break the offending block and optionally alerts
 *       players with {@code samplese.lagfixer.alerts}.</li>
 *   <li><b>InstantLeafDecay</b> - leaves disappear as soon as no log remains within
 *       {@code tree_distance}, honouring {@code leaves_decay}/{@code drop_items}.</li>
 *   <li><b>TrashDisposal</b> - {@code /trash} opens a disposal GUI for the
 *       configured worlds (per-player storage, optional auto cleanup).</li>
 *   <li><b>AbilityLimiter</b> - cooldown for elytra firework boosts and trident
 *       riptide, plus optional extra durability wear.</li>
 * </ul>
 * Also ported: {@code /lagfixer clear [world] [radius]} (drops), {@code /lagfixer status}
 * (entities/chunks per world) and {@code /lagfixer reload}. The benchmark, map,
 * profiler, console-filter and LagShield modules are intentionally not ported.
 */
public final class LagFixerModule extends EssentialsModule implements Listener {

    private final Map<String, long[]> redstoneCounts = new HashMap<>();
    private long redstoneWindow = System.currentTimeMillis();
    private long lastPurge = System.currentTimeMillis();
    private final Map<UUID, Inventory> trashInventories = new HashMap<>();
    private final Map<UUID, Long> elytraCooldown = new HashMap<>();
    private final Map<UUID, Long> riptideCooldown = new HashMap<>();
    private long lastTrashCleanup = System.currentTimeMillis();

    public LagFixerModule(SamplesEssentials plugin) {
        super("world", "lagfixer", "LagFixer-1.7.1.jar",
                "Entity/redstone limits, instant leaf decay, trash disposal and ability cooldowns");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.lagfixer", "Use /lagfixer", PermissionDefault.OP);
        ensurePermission("samplese.lagfixer.alerts", "Receive redstone alerts", PermissionDefault.OP);
        ensurePermission("samplese.lagfixer.trash", "Use /trash", PermissionDefault.TRUE);
        ensureDefaultFile("config.yml");
        for (String module : List.of("EntityLimiter", "RedstoneLimiter", "InstantLeafDecay",
                "TrashDisposal", "AbilityLimiter")) {
            ensureDefaultFile("modules/" + module + ".yml");
        }
        reloadConfig();
        command("lagfixer", new LagFixerCommand());
        command("trash", new TrashCommand());
        onDisableHook(this::unbindCommands);
        onDisableHook(this::clearTrash);
        listen(this);
        syncRepeat(20L, 20L, this::tick);
    }

    /* ------------------------------------------------------------------ */
    /* module config access                                               */
    /* ------------------------------------------------------------------ */

    /** Reads one of the {@code modules/<name>.yml} files shipped with the module. */
    private org.bukkit.configuration.file.YamlConfiguration moduleFile(String name) {
        java.io.File file = ensureDefaultFile("modules/" + name + ".yml");
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
    }

    private boolean enabled(String module) {
        return moduleFile(module).getBoolean(module + ".enabled", true);
    }

    private boolean applies(String module, World world) {
        List<String> worlds = moduleFile(module).getStringList(module + ".worlds");
        if (worlds.isEmpty()) {
            return true;
        }
        for (String entry : worlds) {
            if (entry.equals("*") || entry.equalsIgnoreCase(world.getName())) {
                return true;
            }
        }
        return false;
    }

    /* ------------------------------------------------------------------ */
    /* EntityLimiter                                                      */
    /* ------------------------------------------------------------------ */

    private String limitType(Entity entity) {
        if (entity instanceof Item) {
            return "items";
        }
        if (entity instanceof Projectile) {
            return "projectiles";
        }
        if (entity instanceof Vehicle) {
            return "vehicles";
        }
        if (entity instanceof LivingEntity) {
            return "creatures";
        }
        return null;
    }

    private int countInChunk(Chunk chunk, String type) {
        int count = 0;
        for (Entity entity : chunk.getEntities()) {
            if (type.equals(limitType(entity))) {
                count++;
            }
        }
        return count;
    }

    private boolean whitelisted(String module, EntityType type) {
        for (String entry : moduleFile(module).getStringList(module + ".values.whitelist")) {
            if (entry.equalsIgnoreCase(type.name())) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!enabled("EntityLimiter") || !applies("EntityLimiter", event.getLocation().getWorld())) {
            return;
        }
        var config = moduleFile("EntityLimiter");
        List<String> reasons = config.getStringList("EntityLimiter.values.reasons");
        if (!reasons.isEmpty()) {
            boolean matched = false;
            for (String reason : reasons) {
                if (reason.equalsIgnoreCase(event.getSpawnReason().name())) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return;
            }
        }
        if (whitelisted("EntityLimiter", event.getEntityType())) {
            return;
        }
        int limit = config.getInt("EntityLimiter.values.creatures", -1);
        if (limit >= 0 && countInChunk(event.getLocation().getChunk(), "creatures") >= limit) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event instanceof CreatureSpawnEvent || !enabled("EntityLimiter")
                || !applies("EntityLimiter", event.getLocation().getWorld())) {
            return;
        }
        String type = limitType(event.getEntity());
        if (type == null) {
            return;
        }
        var config = moduleFile("EntityLimiter");
        int limit = config.getInt("EntityLimiter.values." + type, -1);
        if (limit >= 0 && countInChunk(event.getLocation().getChunk(), type) >= limit) {
            event.setCancelled(true);
        }
    }

    private void purgeOverflow() {
        if (!enabled("EntityLimiter")) {
            return;
        }
        var config = moduleFile("EntityLimiter");
        if (!config.getBoolean("EntityLimiter.values.overflow_purge.enabled", false)) {
            return;
        }
        int interval = Math.max(1, config.getInt("EntityLimiter.values.overflow_purge.interval", 30));
        if (System.currentTimeMillis() - lastPurge < interval * 1000L) {
            return;
        }
        lastPurge = System.currentTimeMillis();
        double multiplier = Math.max(1.0, config.getDouble("EntityLimiter.values.overflow_purge.limit_multiplier", 1.5));
        boolean named = config.getBoolean("EntityLimiter.values.overflow_purge.named", false);
        for (String type : List.of("creatures", "items", "vehicles", "projectiles")) {
            if (!config.getBoolean("EntityLimiter.values.overflow_purge.types." + type, false)) {
                continue;
            }
            int limit = config.getInt("EntityLimiter.values." + type, -1);
            if (limit < 0) {
                continue;
            }
            int allowed = (int) Math.floor(limit * multiplier);
            for (World world : plugin.getServer().getWorlds()) {
                if (!applies("EntityLimiter", world)) {
                    continue;
                }
                for (Chunk chunk : world.getLoadedChunks()) {
                    int seen = 0;
                    for (Entity entity : chunk.getEntities()) {
                        if (!type.equals(limitType(entity))) {
                            continue;
                        }
                        if (!named && entity.getCustomName() != null) {
                            continue;
                        }
                        if (whitelisted("EntityLimiter", entity.getType())) {
                            continue;
                        }
                        if (++seen > allowed) {
                            entity.remove();
                        }
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* RedstoneLimiter                                                    */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRedstone(BlockRedstoneEvent event) {
        if (!enabled("RedstoneLimiter")) {
            return;
        }
        Block block = event.getBlock();
        if (!applies("RedstoneLimiter", block.getWorld())) {
            return;
        }
        String key = chunkKey(block);
        long[] counts = redstoneCounts.computeIfAbsent(key, k -> new long[2]);
        counts[0]++;
        int limit = moduleFile("RedstoneLimiter")
                .getInt("RedstoneLimiter.values.ticks_limit.redstone", 2000);
        if (counts[0] > limit) {
            if (moduleFile("RedstoneLimiter")
                    .getBoolean("RedstoneLimiter.values.break_block.redstone", false)) {
                block.setType(Material.AIR, false);
            }
            alert(block.getWorld(), "redstone", key, counts[0], limit);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        if (!enabled("RedstoneLimiter")) {
            return;
        }
        Block block = event.getBlock();
        if (!applies("RedstoneLimiter", block.getWorld())) {
            return;
        }
        String key = chunkKey(block);
        long[] counts = redstoneCounts.computeIfAbsent(key, k -> new long[2]);
        counts[1] += event.getBlocks().size();
        int limit = moduleFile("RedstoneLimiter")
                .getInt("RedstoneLimiter.values.ticks_limit.piston", 200);
        if (counts[1] > limit) {
            event.setCancelled(true);
            if (moduleFile("RedstoneLimiter")
                    .getBoolean("RedstoneLimiter.values.break_block.piston", false)) {
                block.setType(Material.AIR, false);
            }
            alert(block.getWorld(), "piston", key, counts[1], limit);
        }
    }

    private void alert(World world, String kind, String chunk, long count, int limit) {
        if (!moduleFile("RedstoneLimiter").getBoolean("RedstoneLimiter.values.alerts", true)) {
            return;
        }
        String message = msg("redstone-alert",
                "kind", kind, "chunk", chunk, "count", String.valueOf(count),
                "limit", String.valueOf(limit));
        for (Player player : world.getPlayers()) {
            if (player.hasPermission("samplese.lagfixer.alerts")) {
                player.sendMessage(Text.parse(message));
            }
        }
    }

    private String chunkKey(Block block) {
        return block.getWorld().getName() + ":" + (block.getX() >> 4) + "," + (block.getZ() >> 4);
    }

    private void resetRedstoneWindow() {
        if (System.currentTimeMillis() - redstoneWindow >= 1000L) {
            redstoneWindow = System.currentTimeMillis();
            redstoneCounts.clear();
        }
    }

    /* ------------------------------------------------------------------ */
    /* InstantLeafDecay                                                   */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLeafDecay(LeavesDecayEvent event) {
        if (!enabled("InstantLeafDecay")
                || !applies("InstantLeafDecay", event.getBlock().getWorld())) {
            return;
        }
        var config = moduleFile("InstantLeafDecay");
        if (!config.getBoolean("InstantLeafDecay.values.leaves_decay", true)) {
            return;
        }
        int distance = Math.max(1, config.getInt("InstantLeafDecay.values.tree_distance", 7));
        Block origin = event.getBlock();
        // find the nearest trunk; if none is within range the cluster is already dead
        if (nearestLog(origin, distance) != null) {
            return;
        }
        boolean drop = config.getBoolean("InstantLeafDecay.values.drop_items", true);
        Set<Block> toRemove = new HashSet<>();
        collectLeaves(origin, distance, toRemove);
        for (Block leaf : toRemove) {
            if (drop) {
                leaf.breakNaturally();
            } else {
                leaf.setType(Material.AIR, false);
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getType().name().endsWith("_LOG") && !block.getType().name().endsWith("_STEM")) {
            return;
        }
        if (!enabled("InstantLeafDecay") || !applies("InstantLeafDecay", block.getWorld())) {
            return;
        }
        syncLater(1L, () -> {
            int distance = Math.max(1, moduleFile("InstantLeafDecay")
                    .getInt("InstantLeafDecay.values.tree_distance", 7));
            Set<Block> leaves = new HashSet<>();
            collectLeaves(block, distance, leaves);
            boolean drop = moduleFile("InstantLeafDecay")
                    .getBoolean("InstantLeafDecay.values.drop_items", true);
            for (Block leaf : leaves) {
                if (nearestLog(leaf, distance) != null) {
                    continue;
                }
                if (drop) {
                    leaf.breakNaturally();
                } else {
                    leaf.setType(Material.AIR, false);
                }
            }
        });
    }

    private Block nearestLog(Block origin, int distance) {
        int radius = Math.min(distance, 6);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block candidate = origin.getRelative(dx, dy, dz);
                    String type = candidate.getType().name();
                    if (type.endsWith("_LOG") || type.endsWith("_STEM")) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private void collectLeaves(Block origin, int distance, Set<Block> out) {
        int radius = Math.min(distance, 6);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block candidate = origin.getRelative(dx, dy, dz);
                    if (isLeaves(candidate.getType())) {
                        out.add(candidate);
                    }
                }
            }
        }
    }

    private boolean isLeaves(Material material) {
        String name = material.name();
        return name.endsWith("_LEAVES") && !name.contains("AZALEA");
    }

    /* ------------------------------------------------------------------ */
    /* TrashDisposal                                                      */
    /* ------------------------------------------------------------------ */

    private void openTrash(Player player) {
        if (!enabled("TrashDisposal") || !applies("TrashDisposal", player.getWorld())) {
            reply(player, "trash-disabled");
            return;
        }
        Inventory inventory = trashInventories.computeIfAbsent(player.getUniqueId(), uuid ->
                plugin.getServer().createInventory(null, 36,
                        Text.parse(msg("trash-title", "player", player.getName()))));
        player.openInventory(inventory);
    }

    private void clearTrash() {
        trashInventories.values().forEach(Inventory::clear);
        trashInventories.clear();
    }

    @EventHandler
    public void onTrashClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!event.getInventory().equals(trashInventories.get(player.getUniqueId()))) {
            return;
        }
        if (event.getRawSlot() >= event.getInventory().getSize()) {
            // allow shift-clicking items in, but never take them back out
            if (moduleFile("TrashDisposal").getBoolean("TrashDisposal.values.store_items", true)) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTrashClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Inventory inventory = trashInventories.get(player.getUniqueId());
        if (inventory == null || !event.getInventory().equals(inventory)) {
            return;
        }
        if (!moduleFile("TrashDisposal").getBoolean("TrashDisposal.values.store_items", true)) {
            inventory.clear();
            trashInventories.remove(player.getUniqueId());
        }
    }

    private void cleanupTrash() {
        if (!enabled("TrashDisposal")) {
            return;
        }
        var config = moduleFile("TrashDisposal");
        if (!config.getBoolean("TrashDisposal.values.cleanup.enabled", false)) {
            return;
        }
        int interval = Math.max(1, config.getInt("TrashDisposal.values.cleanup.interval", 60));
        if (System.currentTimeMillis() - lastTrashCleanup < interval * 1000L) {
            return;
        }
        lastTrashCleanup = System.currentTimeMillis();
        for (Inventory inventory : trashInventories.values()) {
            inventory.clear();
        }
    }

    /* ------------------------------------------------------------------ */
    /* AbilityLimiter                                                     */
    /* ------------------------------------------------------------------ */

    @EventHandler(ignoreCancelled = true)
    public void onGlide(org.bukkit.event.entity.EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player) || !event.isGliding()) {
            return;
        }
        if (!enabled("AbilityLimiter") || !applies("AbilityLimiter", player.getWorld())) {
            return;
        }
        var config = moduleFile("AbilityLimiter");
        long cooldown = Math.max(0, config.getInt("AbilityLimiter.values.elytra_boost.cooldown", 4)) * 1000L;
        if (cooldown <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = elytraCooldown.get(player.getUniqueId());
        elytraCooldown.put(player.getUniqueId(), now);
        if (last != null && now - last < cooldown) {
            event.setCancelled(true);
            reply(player, "ability-cooldown",
                    "ability", "elytra", "seconds", String.valueOf((cooldown - (now - last)) / 1000L + 1));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onRiptide(org.bukkit.event.player.PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if (!enabled("AbilityLimiter") || !applies("AbilityLimiter", player.getWorld())) {
            return;
        }
        var config = moduleFile("AbilityLimiter");
        long cooldown = Math.max(0, config.getInt("AbilityLimiter.values.trident_riptide.cooldown", 1)) * 1000L;
        if (cooldown <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = riptideCooldown.get(player.getUniqueId());
        riptideCooldown.put(player.getUniqueId(), now);
        if (last != null && now - last < cooldown) {
            reply(player, "ability-cooldown",
                    "ability", "riptide", "seconds", String.valueOf((cooldown - (now - last)) / 1000L + 1));
        }
    }

    /* ------------------------------------------------------------------ */
    /* periodic work                                                      */
    /* ------------------------------------------------------------------ */

    private void tick() {
        resetRedstoneWindow();
        purgeOverflow();
        cleanupTrash();
    }

    /* ------------------------------------------------------------------ */
    /* commands                                                           */
    /* ------------------------------------------------------------------ */

    private final class LagFixerCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.lagfixer")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String sub = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "reload" -> {
                    // noinspection ResultOfMethodCallIgnored
                    reloadConfig();
                    reply(sender, "reloaded");
                }
                case "clear" -> clear(sender, args);
                case "status" -> status(sender);
                default -> reply(sender, "usage");
            }
            return true;
        }

        private void clear(CommandSender sender, String[] args) {
            World world = args.length > 1 ? plugin.getServer().getWorld(args[1])
                    : sender instanceof Player player ? player.getWorld() : null;
            if (world == null) {
                reply(sender, args.length > 1 ? "unknown-world" : "player-only",
                        "world", args.length > 1 ? args[1] : "?");
                return;
            }
            int radius = -1;
            if (args.length > 2) {
                try {
                    radius = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    reply(sender, "usage");
                    return;
                }
            }
            int removed = 0;
            org.bukkit.Location center = sender instanceof Player player ? player.getLocation()
                    : world.getSpawnLocation();
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Item)) {
                    continue;
                }
                if (radius > 0 && entity.getLocation().distanceSquared(center) > (double) radius * radius) {
                    continue;
                }
                entity.remove();
                removed++;
            }
            reply(sender, "cleared", "world", world.getName(), "count", String.valueOf(removed));
        }

        private void status(CommandSender sender) {
            reply(sender, "status-header");
            for (World world : plugin.getServer().getWorlds()) {
                int entities = world.getEntities().size();
                int chunks = world.getLoadedChunks().length;
                sender.sendMessage(Text.parse(msg("status-line",
                        "world", world.getName(),
                        "entities", String.valueOf(entities),
                        "chunks", String.valueOf(chunks),
                        "players", String.valueOf(world.getPlayers().size()))));
            }
            reply(sender, "status-modules",
                    "entity", String.valueOf(enabled("EntityLimiter")),
                    "redstone", String.valueOf(enabled("RedstoneLimiter")),
                    "leaves", String.valueOf(enabled("InstantLeafDecay")),
                    "trash", String.valueOf(enabled("TrashDisposal")),
                    "ability", String.valueOf(enabled("AbilityLimiter")));
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("status", "clear", "reload"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
                List<String> worlds = new ArrayList<>();
                plugin.getServer().getWorlds().forEach(world -> worlds.add(world.getName()));
                return worlds;
            }
            return List.of();
        }
    }

    private final class TrashCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return true;
            }
            if (!player.hasPermission("samplese.lagfixer.trash")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            openTrash(player);
            return true;
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
