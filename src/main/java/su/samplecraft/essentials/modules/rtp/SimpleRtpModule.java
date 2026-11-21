package su.samplecraft.essentials.modules.rtp;

import org.bukkit.entity.Player;
import su.samplecraft.essentials.module.EssentialsModule;

/**
 * Merged from {@code Simple_RTP.jar}.
 * <p>
 * The lightweight variant: {@code /tpr} with no cooldown, a small radius and
 * no economy hooks. It shares the safe-landing engine with the other RTP
 * modules, so the behaviour is identical - only the numbers differ.
 */
public final class SimpleRtpModule extends EssentialsModule {

    public SimpleRtpModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("rtp", "simplertp", "Simple_RTP.jar", "Lightweight /tpr random teleport without cooldown");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rtp", "Use random teleport", org.bukkit.permissions.PermissionDefault.TRUE);
        command("tpr", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.rtp")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            RandomTeleportEngine.Settings settings = new RandomTeleportEngine.Settings(
                    cfgInt("min-distance", 100),
                    cfgInt("max-distance", 1000),
                    cfgInt("max-attempts", 25),
                    cfgInt("border-margin", 32),
                    cfgBool("avoid-liquid", true),
                    cfgBool("avoid-dangerous", true),
                    cfgInt("max-y", 300));
            player.sendMessage(msgComponent("searching"));
            RandomTeleportEngine.teleport(plugin, player, settings, (result, location) -> {
                if (result == RandomTeleportEngine.Result.SUCCESS) {
                    player.sendMessage(msgComponent("success", "x", String.valueOf(location.getBlockX()),
                            "z", String.valueOf(location.getBlockZ())));
                } else {
                    player.sendMessage(msgComponent("failed"));
                }
            });
            return true;
        });
        onDisableHook(this::unbindCommands);
    }
}
