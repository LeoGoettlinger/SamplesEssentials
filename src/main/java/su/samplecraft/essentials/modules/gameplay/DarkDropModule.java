package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import su.samplecraft.essentials.module.EssentialsModule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code DarkDrop-1.0.0(2).jar} (com.samplecraft.darkdrop.DarkDrop).
 * <p>
 * Faithful port of the original {@code DropManager}:
 * <ul>
 *   <li>a {@link BlockDisplay} chest descends from {@code settings.spawn-height}
 *       blocks above the target, with the actionbar countdown;</li>
 *   <li>on landing a temporary vanilla chest is placed (facing the player),</li>
 *   <li>good loot is rolled into the chest, and the outcome is only revealed
 *       when a player <b>opens</b> the chest (good loot / bad-loot package),</li>
 *   <li>bad loot types match the original enum: bad item, fire, lightning,
 *       explosion, poison, void effect, wither, nothing;</li>
 *   <li>the chest despawns after 10 minutes (or 30 s after being opened) and
 *       drops its contents on the ground.</li>
 * </ul>
 */
public final class DarkDropModule extends EssentialsModule implements Listener {

    private static final long DESPAWN_TICKS = 12_000L;
    private static final long DESPAWN_AFTER_OPEN_TICKS = 600L;

    private final List<UUID> activeDisplays = new ArrayList<>();
    private final Map<String, PendingChest> pendingChests = new HashMap<>();

    private static final class PendingChest {
        final Location location;
        final boolean good;
        boolean consumed;
        int despawnTask = -1;

        PendingChest(Location location, boolean good) {
            this.location = location;
            this.good = good;
        }
    }

    public DarkDropModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "darkdrop", "DarkDrop-1.0.0(2).jar",
                "Descending chest loot drop with good/bad outcome on open");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.darkdrop", "Start a DarkDrop", org.bukkit.permissions.PermissionDefault.OP);
        command("darkdrop", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.darkdrop")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            Location landing;
            if (args.length >= 4 && args[0].equalsIgnoreCase("spawn")) {
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    World world = args.length >= 5 ? plugin.getServer().getWorld(args[4]) : player.getWorld();
                    if (world == null) {
                        player.sendMessage(plugin.getLang().component("unknown-world", "world", args[4]));
                        return true;
                    }
                    landing = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    player.sendMessage(msgComponent("invalid-coordinates"));
                    return true;
                }
            } else {
                landing = randomAround(player);
                if (landing == null) {
                    player.sendMessage(msgComponent("random-failed"));
                    return true;
                }
            }
            startDrop(player, landing);
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            cancelAll();
        });
        listen(this);
    }

    /* ------------------------------------------------------------------ */
    /* target selection (original PositionUtil / DropTarget logic)         */
    /* ------------------------------------------------------------------ */

    private Location randomAround(Player player) {
        int min = cfgInt("random-spawn.min-distance", 30);
        int max = cfgInt("random-spawn.max-distance", 200);
        int margin = cfgInt("random-spawn.world-border-margin", 64);
        int minY = cfgInt("random-spawn.min-safe-y", 60);
        int maxY = cfgInt("random-spawn.max-safe-y", 200);
        World world = player.getWorld();
        for (int attempt = 0; attempt < 24; attempt++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double angle = random.nextDouble(Math.PI * 2);
            int distance = random.nextInt(Math.max(1, min), Math.max(min + 1, max));
            int x = (int) Math.round(player.getLocation().getX() + Math.cos(angle) * distance);
            int z = (int) Math.round(player.getLocation().getZ() + Math.sin(angle) * distance);
            if (world.getWorldBorder() != null) {
                Location center = world.getWorldBorder().getCenter();
                double half = world.getWorldBorder().getSize() / 2.0 - margin;
                if (Math.abs(x - center.getX()) > half || Math.abs(z - center.getZ()) > half) {
                    continue;
                }
            }
            world.getChunkAt(x >> 4, z >> 4);
            int y = Math.max(world.getHighestBlockYAt(x, z) + 1, minY);
            if (world.getBlockAt(x, y, z).getType().isAir() && y <= maxY) {
                return new Location(world, x + 0.5, y, z + 0.5);
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
    /* descent (original DropManager.SinkTask)                             */
    /* ------------------------------------------------------------------ */

    private void startDrop(Player origin, Location landing) {
        World world = landing.getWorld();
        if (world == null) {
            return;
        }
        int minBuild = world.getMinHeight() + 5;
        int maxBuild = world.getMaxHeight() - 5;
        landing.setY(Math.max(minBuild, Math.min(maxBuild, landing.getY())));

        int spawnHeight = Math.max(10, cfgInt("settings.spawn-height", 100));
        Location start = landing.clone();
        start.setY(Math.min(maxBuild, landing.getY() + spawnHeight));

        BlockDisplay display = world.spawn(start, BlockDisplay.class, entity -> {
            entity.setBlock(Material.CHEST.createBlockData());
            entity.setGravity(false);
            entity.setPersistent(false);
        });
        activeDisplays.add(display.getUniqueId());

        origin.showTitle(net.kyori.adventure.title.Title.title(
                msgComponent("drop-start-title"),
                msgComponent("drop-start-subtitle"),
                net.kyori.adventure.title.Title.Times.times(
                        Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(600))));
        origin.sendMessage(msgComponent("drop-start-chat", "height", String.valueOf(spawnHeight)));
        world.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.7f);
        world.spawnParticle(Particle.END_ROD, start, 30, 0.5, 0.5, 0.5, 0.1);

        new SinkTask(display, landing, origin.getUniqueId()).schedule();
    }

    private final class SinkTask {
        private final BlockDisplay display;
        private final Location landing;
        private final UUID originId;
        private final int interval = Math.max(1, cfgInt("settings.update-interval-ticks", 4));
        private final double speed = Math.max(0.5, cfgDouble("settings.descend-speed", 5.0));
        private final double stepPerTick = speed / 20.0 * interval;
        private double currentY;

        SinkTask(BlockDisplay display, Location landing, UUID originId) {
            this.display = display;
            this.landing = landing;
            this.originId = originId;
            this.currentY = display.getLocation().getY();
        }

        void schedule() {
            syncRepeat(0L, interval, this::run);
        }

        private void run() {
            if (display.isValid() && !display.isDead()) {
                double remaining = currentY - landing.getY();
                if (remaining <= stepPerTick) {
                    display.teleport(landing);
                    display.remove();
                    activeDisplays.remove(display.getUniqueId());
                    onLand();
                    return;
                }
                currentY -= stepPerTick;
                Location next = new Location(landing.getWorld(), landing.getX(), currentY, landing.getZ());
                display.teleport(next);
                Player origin = plugin.getServer().getPlayer(originId);
                if (origin != null && origin.isOnline()) {
                    int seconds = Math.max(0, (int) (remaining / speed));
                    origin.sendActionBar(msgComponent("actionbar-descending",
                            "height", String.valueOf((int) Math.max(0, remaining)),
                            "seconds", String.valueOf(seconds)));
                }
                if (landing.getWorld() != null) {
                    landing.getWorld().spawnParticle(Particle.DRAGON_BREATH, next, 5, 0.2, 0.2, 0.2, 0.01);
                }
            } else {
                activeDisplays.remove(display.getUniqueId());
            }
        }

        private void onLand() {
            Player origin = plugin.getServer().getPlayer(originId);
            if (origin != null && origin.isOnline()) {
                origin.showTitle(net.kyori.adventure.title.Title.title(
                        msgComponent("drop-land-title"),
                        msgComponent("drop-land-subtitle"),
                        net.kyori.adventure.title.Title.Times.times(
                                Duration.ofMillis(200), Duration.ofMillis(1500), Duration.ofMillis(400))));
                origin.sendMessage(msgComponent("drop-land-chat"));
            }
            World world = landing.getWorld();
            if (world == null) {
                return;
            }
            world.playSound(landing, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.6f);
            world.spawnParticle(Particle.EXPLOSION, landing, 8, 1.0, 1.0, 1.0, 0.0);
            boolean good = ThreadLocalRandom.current().nextDouble()
                    < cfgDouble("loot.good-chance", 60.0) / 100.0;
            placeChest(landing, good, originId);
        }
    }

    /* ------------------------------------------------------------------ */
    /* chest placement and outcome (original placeChest / onChestOpen)      */
    /* ------------------------------------------------------------------ */

    private void placeChest(Location landing, boolean good, UUID originId) {
        World world = landing.getWorld();
        if (world == null) {
            return;
        }
        int x = landing.getBlockX();
        int z = landing.getBlockZ();
        int targetY = landing.getBlockY();
        for (int dy = 0; dy <= 5; dy++) {
            Material type = world.getBlockAt(x, targetY + dy, z).getType();
            if (type.isAir() || type == Material.WATER || type == Material.LAVA
                    || type == Material.SHORT_GRASS || type == Material.TALL_GRASS || type == Material.SNOW) {
                targetY += dy;
                break;
            }
        }
        Block chestBlock = world.getBlockAt(x, targetY, z);
        chestBlock.setType(Material.CHEST);
        if (chestBlock.getBlockData() instanceof Directional directional) {
            Player origin = plugin.getServer().getPlayer(originId);
            if (origin != null) {
                directional.setFacing(yawToFace(origin.getLocation().getYaw()));
                chestBlock.setBlockData(directional);
            }
        }
        if (good && chestBlock.getState() instanceof Chest chest) {
            List<String> loot = cfgStringList("loot.good-items");
            int amount = Math.min(64, Math.max(1, cfgInt("loot.good-amount", 3)));
            if (!loot.isEmpty()) {
                int slot = 0;
                for (int i = 0; i < amount && slot < chest.getInventory().getSize(); i++) {
                    String entry = loot.get(ThreadLocalRandom.current().nextInt(loot.size()));
                    ItemStack template = parseItem(entry);
                    if (template != null) {
                        chest.getInventory().setItem(slot++, template);
                    }
                }
            }
            chest.update();
        }
        Location chestLoc = new Location(world, x + 0.5, targetY, z + 0.5);
        PendingChest pending = new PendingChest(chestLoc, good);
        pendingChests.put(blockKey(chestLoc), pending);
        pending.despawnTask = syncLater(DESPAWN_TICKS, () -> despawnChest(pending)).getTaskId();
        world.playSound(chestLoc, Sound.BLOCK_CHEST_LOCKED, 1.2f, 0.8f);
        world.spawnParticle(Particle.DRAGON_BREATH, chestLoc, 20, 0.4, 0.4, 0.4, 0.02);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.CHEST) {
            return;
        }
        PendingChest pending = pendingChests.get(blockKey(event.getClickedBlock().getLocation()));
        if (pending == null || pending.consumed) {
            return;
        }
        pending.consumed = true;
        if (pending.despawnTask != -1) {
            plugin.getServer().getScheduler().cancelTask(pending.despawnTask);
        }
        Player opener = event.getPlayer();
        if (pending.good) {
            opener.sendMessage(msgComponent("loot-good-chat"));
            World world = pending.location.getWorld();
            if (world != null) {
                world.playSound(pending.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                world.spawnParticle(Particle.HAPPY_VILLAGER, pending.location, 50, 1.0, 1.0, 1.0, 0.5);
            }
        } else {
            opener.sendMessage(msgComponent("loot-bad-chat"));
            // remove the (empty) chest immediately, the trap triggers below the player
            Location loot = pending.location.clone();
            despawnChest(pending);
            triggerBadLoot(loot);
        }
        pending.despawnTask = syncLater(DESPAWN_AFTER_OPEN_TICKS, () -> despawnChest(pending)).getTaskId();
    }

    private void despawnChest(PendingChest pending) {
        if (pending == null) {
            return;
        }
        if (pending.despawnTask != -1) {
            plugin.getServer().getScheduler().cancelTask(pending.despawnTask);
            pending.despawnTask = -1;
        }
        pendingChests.remove(blockKey(pending.location));
        World world = pending.location.getWorld();
        if (world == null) {
            return;
        }
        Block block = pending.location.getBlock();
        if (block.getType() == Material.CHEST && block.getState() instanceof Chest chest) {
            for (ItemStack item : chest.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    world.dropItemNaturally(block.getLocation(), item);
                }
            }
            chest.getInventory().clear();
        }
        block.setType(Material.AIR);
        world.playSound(block.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.6f, 0.6f);
    }

    /* ------------------------------------------------------------------ */
    /* bad loot (original triggerBadLoot switch)                           */
    /* ------------------------------------------------------------------ */

    private void triggerBadLoot(Location land) {
        World world = land.getWorld();
        if (world == null) {
            return;
        }
        String type = cfgString("bad-loot.type", "WITHER").toUpperCase(Locale.ROOT);
        int amount = Math.max(1, cfgInt("bad-loot.amount", 1));
        int effectTicks = Math.max(1, cfgInt("bad-loot.effect-duration-seconds", 8)) * 20;
        int amplifier = Math.max(0, cfgInt("bad-loot.effect-amplifier", 1));
        switch (type) {
            case "BAD_ITEM" -> world.dropItemNaturally(land, new ItemStack(Material.DEAD_BUSH, amount));
            case "FIRE" -> {
                int radius = Math.max(1, cfgInt("bad-loot.fire-radius", 2));
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        Block block = world.getBlockAt(land.clone().add(dx, 0, dz));
                        if (block.getType().isAir()) {
                            block.setType(Material.FIRE);
                        }
                    }
                }
                for (Player nearby : world.getNearbyPlayers(land, 4.0, 4.0, 4.0)) {
                    nearby.setFireTicks(cfgInt("bad-loot.fire-duration-seconds", 3) * 20);
                }
                world.playSound(land, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            }
            case "LIGHTNING" -> {
                double damage = cfgDouble("bad-loot.lightning-damage", 0.0);
                for (int i = 0; i < amount; i++) {
                    Location strike = land.clone().add(
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0, 0,
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0);
                    world.strikeLightningEffect(strike);
                    if (damage > 0) {
                        for (Player nearby : world.getNearbyPlayers(strike, 3.0, 3.0, 3.0)) {
                            nearby.damage(damage);
                        }
                    }
                }
                world.playSound(land, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
            }
            case "EXPLOSION" -> {
                double damage = cfgDouble("bad-loot.explosion-damage", 4.0);
                world.createExplosion(land, 2.0f, false, false);
                if (damage > 0) {
                    for (Player nearby : world.getNearbyPlayers(land, 5.0, 5.0, 5.0)) {
                        nearby.damage(damage);
                    }
                }
                world.playSound(land, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
            case "POISON" -> {
                for (Player nearby : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.POISON, effectTicks, amplifier, false, true, true));
                }
                world.playSound(land, Sound.ENTITY_WITCH_DRINK, 1.0f, 1.0f);
            }
            case "VOID_EFFECT" -> {
                for (Player nearby : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, effectTicks, amplifier, false, true, true));
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, effectTicks, amplifier, false, true, true));
                }
                world.playSound(land, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.5f);
            }
            case "NOTHING" -> world.playSound(land, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
            default -> { // WITHER
                for (Player nearby : world.getNearbyPlayers(land, 6.0, 6.0, 6.0)) {
                    nearby.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, effectTicks, amplifier, false, true, true));
                }
                world.playSound(land, Sound.ENTITY_WITHER_AMBIENT, 1.0f, 1.0f);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private ItemStack parseItem(String entry) {
        String[] parts = entry.split(":", 2);
        Material material = Material.matchMaterial(parts[0].trim().toUpperCase(Locale.ROOT));
        if (material == null || !material.isItem()) {
            return null;
        }
        int amount = parts.length > 1 ? Math.max(1, parseInt(parts[1], 1)) : 1;
        return new ItemStack(material, Math.min(amount, material.getMaxStackSize()));
    }

    private int parseInt(String raw, int def) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static org.bukkit.block.BlockFace yawToFace(float yaw) {
        float normalized = (yaw % 360f + 360f) % 360f;
        if (normalized < 45f || normalized >= 315f) {
            return org.bukkit.block.BlockFace.SOUTH;
        }
        if (normalized < 135f) {
            return org.bukkit.block.BlockFace.WEST;
        }
        return normalized < 225f ? org.bukkit.block.BlockFace.NORTH : org.bukkit.block.BlockFace.EAST;
    }

    private static String blockKey(Location location) {
        World world = location.getWorld();
        return (world == null ? "?" : world.getUID()) + ":" + location.getBlockX()
                + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private void cancelAll() {
        for (PendingChest pending : new ArrayList<>(pendingChests.values())) {
            despawnChest(pending);
        }
        pendingChests.clear();
        for (UUID id : new ArrayList<>(activeDisplays)) {
            Entity entity = plugin.getServer().getEntity(id);
            if (entity != null) {
                entity.remove();
            }
        }
        activeDisplays.clear();
    }
}
