package su.samplecraft.essentials.modules.rtp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code EzRTP-3.4.3.jar} (com.skyblockexp.ezrtp.EzRtpPlugin).
 * <p>
 * Commands: {@code /rtp [world]}, {@code /forcertp [player]}. Cooldowns are
 * tracked per player and persisted in {@code rtp/ezrtp/data/cooldowns.yml},
 * so they survive restarts like the original limits.yml did.
 */
public final class EzRtpModule extends EssentialsModule implements Listener {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public EzRtpModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("rtp", "ezrtp", "EzRTP-3.4.3.jar", "Random teleport with cooldowns and /forcertp");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rtp", "Use random teleport", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.rtp.force", "Force random teleports", org.bukkit.permissions.PermissionDefault.OP);
        java.io.File file = new java.io.File(getDataFolder(), "cooldowns.yml");
        if (file.isFile()) {
            org.bukkit.configuration.file.YamlConfiguration yaml =
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
            for (String key : yaml.getKeys(false)) {
                try {
                    cooldowns.put(UUID.fromString(key), yaml.getLong(key));
                } catch (IllegalArgumentException ignored) {
                    // stale entry
                }
            }
        }
        command("rtp", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.rtp")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length > 0) {
                org.bukkit.World world = plugin.getServer().getWorld(args[0]);
                if (world != null && player.hasPermission("samplese.rtp.world")) {
                    player.teleportAsync(world.getSpawnLocation()).thenRun(() -> doRtp(player));
                    return true;
                }
            }
            doRtp(player);
            return true;
        });
        command("forcertp", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.rtp.force")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            Player target = sender instanceof Player p ? p : null;
            if (args.length > 0) {
                target = plugin.getServer().getPlayerExact(args[0]);
            }
            if (target == null) {
                sender.sendMessage(plugin.getLang().component("unknown-player",
                        "player", args.length > 0 ? args[0] : "?"));
                return true;
            }
            cooldowns.remove(target.getUniqueId());
            doRtp(target);
            sender.sendMessage(msgComponent("forced", "player", target.getName()));
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            saveCooldowns();
        });
        listen(this);
    }

    private void doRtp(Player player) {
        long cooldownMillis = Math.max(0, cfgInt("limits.cooldown-seconds", 60)) * 1000L;
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && cooldownMillis > 0) {
            long remaining = cooldownMillis - (System.currentTimeMillis() - last);
            if (remaining > 0 && !player.hasPermission("samplese.rtp.bypass")) {
                player.sendMessage(msgComponent("cooldown", "seconds", String.valueOf((remaining + 999) / 1000)));
                return;
            }
        }
        RandomTeleportEngine.Settings settings = new RandomTeleportEngine.Settings(
                cfgInt("rtp.min-distance", 200),
                cfgInt("rtp.max-distance", 3000),
                cfgInt("rtp.max-attempts", 40),
                cfgInt("rtp.border-margin", 64),
                cfgBool("rtp.avoid-liquid", true),
                cfgBool("rtp.avoid-dangerous", true),
                cfgInt("rtp.max-y", 300));
        player.sendMessage(msgComponent("searching"));
        RandomTeleportEngine.teleport(plugin, player, settings, (result, location) -> {
            switch (result) {
                case SUCCESS -> {
                    cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                    player.sendMessage(msgComponent("success",
                            "x", String.valueOf(location.getBlockX()),
                            "y", String.valueOf(location.getBlockY()),
                            "z", String.valueOf(location.getBlockZ()),
                            "world", location.getWorld() == null ? "?" : location.getWorld().getName()));
                }
                case NO_SAFE_SPOT -> player.sendMessage(msgComponent("failed"));
                case CANCELLED -> player.sendMessage(msgComponent("cancelled"));
            }
        });
    }

    private void saveCooldowns() {
        org.bukkit.configuration.file.YamlConfiguration yaml = new org.bukkit.configuration.file.YamlConfiguration();
        cooldowns.forEach((id, time) -> yaml.set(id.toString(), time));
        try {
            yaml.save(new java.io.File(getDataFolder(), "cooldowns.yml"));
        } catch (Exception e) {
            plugin.getLogger().warning("rtp/ezrtp: could not save cooldowns: " + e.getMessage());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // cooldowns intentionally survive logout (same as the original plugin)
    }
}
