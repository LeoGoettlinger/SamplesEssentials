package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code DarkDrop-1.0.0(2).jar} (com.samplecraft.darkdrop.DarkDrop).
 * <p>
 * A mysterious loot drop descends from {@code spawn-height} blocks above the
 * target, announced through the actionbar (height + remaining seconds). At
 * the landing point the drop either gives good loot (configurable item list)
 * or triggers the bad-loot package (small explosion, fire, lightning, potion
 * effects) - exactly the good/bad chance split of the original config.
 */
public final class DarkDropModule extends EssentialsModule {

    private final List<UUIDHolder> running = new ArrayList<>();

    private record UUIDHolder(java.util.UUID id) {
    }

    public DarkDropModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "darkdrop", "DarkDrop-1.0.0(2).jar",
                "Descending loot drop with good/bad loot outcomes and actionbar tracking");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.darkdrop", "Start a DarkDrop", org.bukkit.permissions.PermissionDefault.OP);
        command("darkdrop", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.darkdrop")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            Location target;
            if (args.length >= 4 && args[0].equalsIgnoreCase("spawn")) {
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    World fallback = sender instanceof Player p
                            ? p.getWorld()
                            : plugin.getServer().getWorlds().getFirst();
                    World world = args.length >= 5
                            ? plugin.getServer().getWorld(args[4])
                            : fallback;
                    if (world == null) {
                        sender.sendMessage(plugin.getLang().component("unknown-world", "world", args[4]));
                        return true;
                    }
                    target = new Location(world, x, y, z);
                } catch (NumberFormatException e) {
                    sender.sendMessage(msgComponent("invalid-coordinates"));
                    return true;
                }
            } else if (sender instanceof Player player) {
                target = randomAround(player);
                if (target == null) {
                    sender.sendMessage(msgComponent("random-failed"));
                    return true;
                }
            } else {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            start(target, sender);
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            for (UUIDHolder holder : running) {
                Entity entity = plugin.getServer().getEntity(holder.id());
                if (entity != null) {
                    entity.remove();
                }
            }
            running.clear();
        });
    }

    private Location randomAround(Player player) {
        int min = cfgInt("random-spawn.min-distance", 30);
        int max = cfgInt("random-spawn.max-distance", 200);
        int margin = cfgInt("random-spawn.world-border-margin", 64);
        World world = player.getWorld();
        for (int attempt = 0; attempt < 24; attempt++) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double angle = random.nextDouble(Math.PI * 2);
            double distance = random.nextInt(Math.max(min, 1), Math.max(min + 1, max));
            int x = (int) (player.getLocation().getX() + Math.cos(angle) * distance);
            int z = (int) (player.getLocation().getZ() + Math.sin(angle) * distance);
            if (world.getWorldBorder() != null) {
                Location center = world.getWorldBorder().getCenter();
                double half = world.getWorldBorder().getSize() / 2.0 - margin;
                if (Math.abs(x - center.getX()) > half || Math.abs(z - center.getZ()) > half) {
                    continue;
                }
            }
            world.getChunkAt(x >> 4, z >> 4);
            int y = Math.max(world.getHighestBlockYAt(x, z) + 1, cfgInt("random-spawn.min-safe-y", 60));
            if (y > cfgInt("random-spawn.max-safe-y", 200)) {
                continue;
            }
            return new Location(world, x + 0.5, y, z + 0.5);
        }
        return null;
    }

    private void start(Location landing, org.bukkit.command.CommandSender initiator) {
        World world = landing.getWorld();
        if (world == null) {
            return;
        }
        int spawnHeight = Math.max(20, cfgInt("settings.spawn-height", 100));
        Location start = landing.clone().add(0, spawnHeight, 0);

        TextDisplay display = world.spawn(start, TextDisplay.class, entity -> {
            entity.text(Text.MINI.deserialize("<gradient:#b967ff:#01cdfe><bold>DARKDROP</bold></gradient>"));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(true);
            entity.setViewRange(2.0f);
            entity.setTransformation(new Transformation(
                    new org.joml.Vector3f(0f, 0f, 0f),
                    new org.joml.Quaternionf(),
                    new org.joml.Vector3f(2.5f, 2.5f, 2.5f),
                    new org.joml.Quaternionf()));
        });
        running.add(new UUIDHolder(display.getUniqueId()));

        broadcast(msgComponent("drop-start-chat", "height", String.valueOf(spawnHeight)));
        world.playSound(start, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.6f);

        double blocksPerSecond = Math.max(0.5, cfgDouble("settings.descend-speed", 5.0));
        int interval = Math.max(1, cfgInt("settings.update-interval-ticks", 4));
        double perTick = blocksPerSecond / 20.0 * interval;

        new Descent(display, landing.clone(), perTick, interval).schedule();
    }

    private final class Descent {
        private final TextDisplay display;
        private final Location landing;
        private final double perTick;
        private final int interval;
        private boolean landed;

        Descent(TextDisplay display, Location landing, double perTick, int interval) {
            this.display = display;
            this.landing = landing;
            this.perTick = perTick;
            this.interval = interval;
        }

        void schedule() {
            syncRepeat(interval, interval, this::tick);
        }

        private void tick() {
            if (landed || !display.isValid()) {
                return;
            }
            Location next = display.getLocation().subtract(0, perTick, 0);
            if (next.getY() <= landing.getY()) {
                landed = true;
                display.remove();
                running.removeIf(holder -> holder.id().equals(display.getUniqueId()));
                finish(landing);
                return;
            }
            display.teleport(next);
            if (next.getWorld() != null) {
                next.getWorld().spawnParticle(Particle.PORTAL, next, 12, 0.4, 0.4, 0.4, 0.05);
            }
            int heightLeft = Math.max(0, (int) Math.ceil(next.getY() - landing.getY()));
            double secondsLeft = heightLeft / Math.max(1.0, cfgDouble("settings.descend-speed", 5.0));
            String actionBar = msg("actionbar-descending",
                    "height", String.valueOf(heightLeft),
                    "seconds", String.valueOf(Math.round(secondsLeft)));
            var component = Text.parse(actionBar);
            for (Player player : landing.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(landing) < 128 * 128) {
                    player.sendActionBar(component);
                }
            }
        }
    }

    private void finish(Location landing) {
        World world = landing.getWorld();
        if (world == null) {
            return;
        }
        world.strikeLightningEffect(landing);
        world.spawnParticle(Particle.EXPLOSION, landing.clone().add(0, 1, 0), 4, 1, 1, 1, 0.1);
        world.playSound(landing, Sound.ENTITY_GENERIC_EXPLODE, 1.4f, 0.7f);

        double goodChance = Math.max(0, cfgDouble("loot.good-chance", 60.0));
        boolean good = ThreadLocalRandom.current().nextDouble(100.0) < goodChance;
        broadcast(msgComponent(good ? "loot-good-chat" : "loot-bad-chat"));

        if (good) {
            List<String> loot = cfgStringList("loot.good-items");
            if (loot.isEmpty()) {
                loot = List.of("DIAMOND:3", "GOLDEN_APPLE:2", "EXPERIENCE_BOTTLE:8");
            }
            for (String entry : loot) {
                String[] parts = entry.split(":", 2);
                Material material = Material.matchMaterial(parts[0].trim().toUpperCase(Locale.ROOT));
                if (material == null || !material.isItem()) {
                    continue;
                }
                int amount = parts.length > 1 ? parseInt(parts[1], 1) : 1;
                world.dropItemNaturally(landing.clone().add(0, 0.5, 0), new ItemStack(material, Math.max(1, amount)));
            }
            return;
        }

        double damage = cfgDouble("bad-loot.explosion-damage", 4.0);
        if (damage > 0) {
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(landing) < 6 * 6) {
                    player.damage(damage);
                }
            }
        }
        int fireSeconds = cfgInt("bad-loot.fire-duration-seconds", 3);
        int fireRadius = cfgInt("bad-loot.fire-radius", 2);
        if (fireSeconds > 0) {
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(landing) < (fireRadius + 2) * (fireRadius + 2)) {
                    player.setFireTicks(Math.max(player.getFireTicks(), fireSeconds * 20));
                }
            }
        }
        int effectSeconds = cfgInt("bad-loot.effect-duration-seconds", 8);
        int amplifier = cfgInt("bad-loot.effect-amplifier", 1);
        PotionEffectType[] pool = {PotionEffectType.SLOWNESS, PotionEffectType.NAUSEA,
                PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS};
        PotionEffectType chosen = pool[ThreadLocalRandom.current().nextInt(pool.length)];
        if (effectSeconds > 0) {
            for (Player player : world.getPlayers()) {
                if (player.getLocation().distanceSquared(landing) < 12 * 12) {
                    player.addPotionEffect(new PotionEffect(chosen, effectSeconds * 20, Math.max(0, amplifier), false, true));
                }
            }
        }
    }

    private int parseInt(String raw, int def) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void broadcast(net.kyori.adventure.text.Component component) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(component);
        }
        plugin.getServer().getConsoleSender().sendMessage(component);
    }
}
