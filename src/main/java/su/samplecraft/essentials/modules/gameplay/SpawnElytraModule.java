package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code spawnelytra-1.5.jar} (com.blaxk.spawnelytra.Main).
 * <p>
 * The original lets players glide inside a configured spawn area without a
 * real elytra, with a configurable boost (forward/upward) and a max boost
 * count per flight. This port keeps the world/area/boost structure from the
 * old config: per-world {@code spawn_area} (circular or rectangular),
 * {@code activation_mode} (double_jump / sneak_jump), boost strength,
 * direction and cooldown.
 */
public final class SpawnElytraModule extends EssentialsModule implements Listener {

    private final Map<UUID, Integer> boostsUsed = new HashMap<>();

    public SpawnElytraModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "spawnelytra", "spawnelytra-1.5.jar", "Elytra gliding inside configured spawn areas with boosts");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.spawnelytra", "Manage the spawn elytra", org.bukkit.permissions.PermissionDefault.OP);
        command("spawnelytra", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.spawnelytra")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("visualize")) {
                visualize(player);
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                player.sendMessage(plugin.getLang().component("reloaded"));
            } else {
                player.sendMessage(msgComponent("usage"));
            }
            return true;
        });
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    private ConfigurationSection worldConfig(Player player) {
        String world = player.getWorld().getName();
        ConfigurationSection section = config().getConfigurationSection("worlds." + world);
        if (section == null) {
            section = config().getConfigurationSection("worlds.default");
        }
        return section != null && section.getBoolean("enabled", true) ? section : null;
    }

    private boolean inArea(Player player, ConfigurationSection world) {
        ConfigurationSection area = world.getConfigurationSection("spawn_area");
        if (area == null) {
            return false;
        }
        Location location = player.getLocation();
        if ("circular".equalsIgnoreCase(area.getString("area_type", "circular"))
                && area.getDouble("x2", 0) == 0 && area.getDouble("y2", 0) == 0 && area.getDouble("z2", 0) == 0) {
            Location center = new Location(player.getWorld(),
                    area.getDouble("x", player.getWorld().getSpawnLocation().getX()),
                    area.getDouble("y", player.getWorld().getSpawnLocation().getY()),
                    area.getDouble("z", player.getWorld().getSpawnLocation().getZ()));
            double radius = world.getDouble("radius", 100);
            return center.distanceSquared(location) <= radius * radius;
        }
        double minX = Math.min(area.getDouble("x", 0), area.getDouble("x2", 0));
        double maxX = Math.max(area.getDouble("x", 0), area.getDouble("x2", 0));
        double minY = Math.min(area.getDouble("y", 0), area.getDouble("y2", 0));
        double maxY = Math.max(area.getDouble("y", 0), area.getDouble("y2", 0));
        double minZ = Math.min(area.getDouble("z", 0), area.getDouble("z2", 0));
        double maxZ = Math.max(area.getDouble("z", 0), area.getDouble("z2", 0));
        return location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    private void activate(Player player, ConfigurationSection world) {
        if (player.getGameMode() == GameMode.CREATIVE && cfgBool("game_modes.disable_in_creative", true)) {
            return;
        }
        if (player.getGameMode() == GameMode.ADVENTURE && cfgBool("game_modes.disable_in_adventure", false)) {
            return;
        }
        player.setAllowFlight(true);
        player.setFlying(true);
        boostsUsed.put(player.getUniqueId(), 0);
        player.sendActionBar(msgComponent("activated"));
        if (world.getBoolean("boost.enabled", true) && world.getConfigurationSection("boost") != null
                && world.getConfigurationSection("boost").getBoolean("show_hint", true)) {
            player.sendMessage(msgComponent("press-to-boost"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isFlying() || player.isGliding()) {
            return;
        }
        if (player.getAllowFlight()) {
            return;
        }
        if (!player.isOnGround() || event.getTo() == null) {
            return;
        }
        if (player.isSneaking() && "sneak_jump".equalsIgnoreCase(activationMode(player))) {
            ConfigurationSection world = worldConfig(player);
            if (world != null && inArea(player, world)) {
                activate(player, world);
            }
        }
    }

    private String activationMode(Player player) {
        ConfigurationSection world = worldConfig(player);
        return world == null ? "double_jump" : world.getString("activation_mode", "double_jump");
    }

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.isGliding()) {
            return;
        }
        ConfigurationSection world = worldConfig(player);
        if (world != null && inArea(player, world)) {
            // keep the spawn elytra "gliding" state alive while flying inside the area
            boostsUsed.putIfAbsent(player.getUniqueId(), 0);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }
        Player player = event.getPlayer();
        ConfigurationSection world = worldConfig(player);
        if (world == null || !inArea(player, world)) {
            return;
        }
        if (!player.isFlying() && !player.isGliding()) {
            return;
        }
        ConfigurationSection boost = world.getConfigurationSection("boost");
        if (boost == null || !boost.getBoolean("enabled", true)) {
            return;
        }
        int used = boostsUsed.getOrDefault(player.getUniqueId(), 0);
        int max = Math.max(1, boost.getInt("max_boosts", 1));
        if (used >= max) {
            return;
        }
        double strength = boost.getDouble("strength", 4.0);
        Vector direction = "upward".equalsIgnoreCase(boost.getString("direction", "forward"))
                ? new Vector(0, strength, 0)
                : player.getLocation().getDirection().multiply(strength);
        player.setVelocity(direction);
        boostsUsed.put(player.getUniqueId(), used + 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.2f, 1.1f);
        player.getWorld().spawnParticle(Particle.FIREWORK, player.getLocation(), 24, 0.4, 0.4, 0.4, 0.08);
        if (cfgBool("messages.show_boost_activated", true)) {
            player.sendActionBar(msgComponent("boost-activated"));
        }
    }

    private void visualize(Player player) {
        ConfigurationSection world = worldConfig(player);
        if (world == null) {
            player.sendMessage(msgComponent("not-enabled-here"));
            return;
        }
        ConfigurationSection area = world.getConfigurationSection("spawn_area");
        if (area == null) {
            return;
        }
        int vertical = Math.max(4, cfgInt("visualization.vertical_range", 20));
        int step = Math.max(2, cfgInt("visualization.update_frequency", 10));
        double x1 = area.getDouble("x", player.getLocation().getX());
        double y1 = area.getDouble("y", player.getLocation().getY());
        double z1 = area.getDouble("z", player.getLocation().getZ());
        double x2 = area.getDouble("x2", x1 + 50);
        double z2 = area.getDouble("z2", z1 + 50);
        syncRepeat(0L, step, () -> {
            if (!player.isOnline()) {
                return;
            }
            for (double t = 0; t <= 1.0; t += 0.05) {
                double x = x1 + (x2 - x1) * t;
                double z = z1 + (z2 - z1) * t;
                for (int dy = -vertical; dy <= vertical; dy += 4) {
                    player.spawnParticle(Particle.END_ROD, x, y1 + dy, z1, 1, 0, 0, 0, 0);
                    player.spawnParticle(Particle.END_ROD, x1, y1 + dy, z, 1, 0, 0, 0, 0);
                    player.spawnParticle(Particle.END_ROD, x2, y1 + dy, z, 1, 0, 0, 0, 0);
                    player.spawnParticle(Particle.END_ROD, x, y1 + dy, z2, 1, 0, 0, 0, 0);
                }
            }
        });
    }
}
