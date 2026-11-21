package su.samplecraft.essentials.modules.rtp;

import org.bukkit.entity.Player;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code RexonRTP.jar}.
 * <p>
 * Premium variant of the random teleport: dedicated world, own min/max
 * distance, optional cost through the Vault economy, and a separate
 * cooldown. Uses the same safe-landing engine as {@code /rtp}.
 */
public final class PremiumRtpModule extends EssentialsModule {

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public PremiumRtpModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("rtp", "premiumrtp", "RexonRTP.jar", "Premium random teleport with cost and own world bounds");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rtp.premium", "Use premium random teleport", org.bukkit.permissions.PermissionDefault.OP);
        command("premiumrtp", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.rtp.premium")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String worldName = cfgString("world", "");
            if (!worldName.isEmpty() && !worldName.equalsIgnoreCase(player.getWorld().getName())) {
                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    player.sendMessage(msgComponent("no-world", "world", worldName));
                    return true;
                }
                player.teleportAsync(world.getSpawnLocation()).thenRun(() -> run(player));
                return true;
            }
            run(player);
            return true;
        });
        onDisableHook(this::unbindCommands);
    }

    private void run(Player player) {
        long cooldownMillis = Math.max(0, cfgInt("cooldown-seconds", 30)) * 1000L;
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && cooldownMillis > 0) {
            long remaining = cooldownMillis - (System.currentTimeMillis() - last);
            if (remaining > 0 && !player.hasPermission("samplese.rtp.bypass")) {
                player.sendMessage(msgComponent("cooldown", "seconds", String.valueOf((remaining + 999) / 1000)));
                return;
            }
        }
        double cost = cfgDouble("cost", 0.0);
        if (cost > 0 && !charge(player, cost)) {
            return;
        }
        RandomTeleportEngine.Settings settings = new RandomTeleportEngine.Settings(
                cfgInt("min-distance", 1000),
                cfgInt("max-distance", 5000),
                cfgInt("max-attempts", 60),
                cfgInt("border-margin", 64),
                true,
                true,
                300);
        player.sendMessage(msgComponent("searching"));
        RandomTeleportEngine.teleport(plugin, player, settings, (result, location) -> {
            if (result == RandomTeleportEngine.Result.SUCCESS) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                player.sendMessage(msgComponent("success", "x", String.valueOf(location.getBlockX()),
                        "z", String.valueOf(location.getBlockZ()), "cost", String.valueOf(cost)));
            } else {
                player.sendMessage(msgComponent(result == RandomTeleportEngine.Result.NO_SAFE_SPOT
                        ? "failed" : "cancelled"));
                if (cost > 0) {
                    refund(player, cost);
                }
            }
        });
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private boolean charge(Player player, double cost) {
        net.milkbowl.vault.economy.Economy economy = economy();
        if (economy == null) {
            player.sendMessage(msgComponent("no-economy"));
            return false;
        }
        if (!economy.has(player, cost)) {
            player.sendMessage(msgComponent("not-enough-money", "cost", economy.format(cost)));
            return false;
        }
        economy.withdrawPlayer(player, cost);
        return true;
    }

    private void refund(Player player, double cost) {
        net.milkbowl.vault.economy.Economy economy = economy();
        if (economy != null) {
            economy.depositPlayer(player, cost);
        }
    }
}
