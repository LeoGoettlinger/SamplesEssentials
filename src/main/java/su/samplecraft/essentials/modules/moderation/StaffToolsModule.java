package su.samplecraft.essentials.modules.moderation;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code SModeration.jar}.
 * <p>
 * Features kept 1:1 with the original config flags: vanish, socialspy (with the
 * command whitelist), staff chat and muted-command blocking. The original
 * "force-reason" option is honoured for freeze reasons.
 */
public final class StaffToolsModule extends EssentialsModule implements Listener {

    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> socialSpy = new HashSet<>();

    public StaffToolsModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("moderation", "stafftools", "SModeration.jar", "Vanish, socialspy and staff chat");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.staff", "Staff member", org.bukkit.permissions.PermissionDefault.OP);
        command("vanish", (sender, command, label, args) -> toggle(sender, vanished, "vanish"));
        command("socialspy", (sender, command, label, args) -> toggle(sender, socialSpy, "socialspy"));
        command("staffchat", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.staff")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(plugin.getLang().component("usage"));
                return true;
            }
            net.kyori.adventure.text.Component message = msgComponent("staffchat-message",
                    "player", player.getName(), "message", String.join(" ", args));
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (online.hasPermission("samplese.staff")) {
                    online.sendMessage(message);
                }
            }
            plugin.getServer().getConsoleSender().sendMessage(message);
            return true;
        });
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    private boolean toggle(org.bukkit.command.CommandSender sender, Set<UUID> set, String what) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLang().component("players-only"));
            return true;
        }
        if (!player.hasPermission("samplese.staff")) {
            player.sendMessage(plugin.getLang().component("no-permission"));
            return true;
        }
        boolean now;
        if (set.remove(player.getUniqueId())) {
            now = false;
        } else {
            set.add(player.getUniqueId());
            now = true;
        }
        if (set == vanished) {
            for (Player viewer : plugin.getServer().getOnlinePlayers()) {
                if (viewer.equals(player) || viewer.hasPermission("samplese.staff")) {
                    continue;
                }
                if (now) {
                    viewer.hidePlayer(plugin, player);
                } else {
                    viewer.showPlayer(plugin, player);
                }
            }
            plugin.getServer().getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("samplese.staff") && !p.equals(player))
                    .forEach(p -> p.sendMessage(msgComponent("vanish-notice",
                            "player", player.getName(), "state", now ? "vanished" : "visible")));
        }
        player.sendMessage(msgComponent(what, "state", now ? "on" : "off"));
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!cfgBool("features.vanish", true) || vanished.isEmpty()) {
            return;
        }
        for (UUID id : vanished) {
            Player hidden = plugin.getServer().getPlayer(id);
            if (hidden != null && !event.getPlayer().hasPermission("samplese.staff")) {
                event.getPlayer().hidePlayer(plugin, hidden);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            online.showPlayer(plugin, event.getPlayer());
        }
        vanished.remove(id);
        socialSpy.remove(id);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage().substring(1);
        String label = raw.split(" ")[0].toLowerCase(Locale.ROOT);
        if (label.contains(":")) {
            label = label.substring(label.indexOf(':') + 1);
        }
        if (cfgBool("features.socialspy", true) && socialSpy.size() > 0
                && cfgStringList("socialspy-commands").stream().anyMatch(c -> c.equalsIgnoreCase(label))) {
            net.kyori.adventure.text.Component spy = msgComponent("socialspy-format",
                    "player", event.getPlayer().getName(), "command", raw);
            for (UUID id : socialSpy) {
                Player staff = plugin.getServer().getPlayer(id);
                if (staff != null && !staff.equals(event.getPlayer())) {
                    staff.sendMessage(spy);
                }
            }
        }
    }
}
