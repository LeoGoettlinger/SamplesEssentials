package su.samplecraft.essentials.modules.moderation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.samplecraft.essentials.module.EssentialsModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code SimplePlayerFreeze-2.3.0.jar} (simplexity.simpleplayerfreeze.SimplePlayerFreeze).
 * <p>
 * Commands kept from the original: {@code /freeze <player|all>},
 * {@code /unfreeze <player|all>}, {@code /freezelist}. Frozen players cannot
 * move, chat or run commands (except the configured whitelist) and are
 * reminded with an actionbar. State persists across restarts.
 */
public final class FreezeModule extends EssentialsModule implements Listener {

    private final Set<UUID> frozen = new HashSet<>();
    private File store;

    public FreezeModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("moderation", "freeze", "SimplePlayerFreeze-2.3.0.jar", "Freeze players, with /freezeall and /freezelist");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.freeze", "Freeze players", org.bukkit.permissions.PermissionDefault.OP);
        store = new File(getDataFolder(), "frozen.txt");
        load();
        command("freeze", (sender, command, label, args) -> handle(sender, args, true));
        command("unfreeze", (sender, command, label, args) -> handle(sender, args, false));
        command("freezelist", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.freeze")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (frozen.isEmpty()) {
                sender.sendMessage(msgComponent("list-empty"));
                return true;
            }
            sender.sendMessage(msgComponent("list-header", "count", String.valueOf(frozen.size())));
            for (UUID id : frozen) {
                Player online = plugin.getServer().getPlayer(id);
                String name = online != null ? online.getName() : id.toString();
                sender.sendMessage(net.kyori.adventure.text.Component.text(" - " + name));
            }
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            save();
        });
        listen(this);
    }

    private boolean handle(org.bukkit.command.CommandSender sender, String[] args, boolean freeze) {
        if (!sender.hasPermission("samplese.freeze")) {
            sender.sendMessage(plugin.getLang().component("no-permission"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(plugin.getLang().component("usage"));
            return true;
        }
        if (args[0].equalsIgnoreCase("all")) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                setFrozen(online, freeze, sender.getName(), "");
            }
            sender.sendMessage(msgComponent(freeze ? "froze-all" : "unfroze-all",
                    "count", String.valueOf(plugin.getServer().getOnlinePlayers().size())));
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getLang().component("unknown-player", "player", args[0]));
            return true;
        }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "";
        setFrozen(target, freeze, sender.getName(), reason);
        sender.sendMessage(msgComponent(freeze ? "done" : "undone",
                "player", target.getName(), "reason", reason));
        return true;
    }

    private void setFrozen(Player target, boolean freeze, String by, String reason) {
        if (freeze) {
            frozen.add(target.getUniqueId());
            target.sendMessage(msgComponent("you-frozen", "by", by, "reason", reason.isEmpty() ? "-" : reason));
        } else {
            frozen.remove(target.getUniqueId());
            target.sendMessage(msgComponent("you-unfrozen", "by", by));
        }
        save();
    }

    private void load() {
        if (!store.isFile()) {
            return;
        }
        try {
            for (String line : Files.readAllLines(store.toPath())) {
                if (!line.isBlank()) {
                    frozen.add(UUID.fromString(line.trim()));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("moderation/freeze: could not read " + store.getName() + ": " + e.getMessage());
        }
    }

    private void save() {
        try {
            File parent = store.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            Files.write(store.toPath(), frozen.stream().map(UUID::toString).toList());
        } catch (IOException e) {
            plugin.getLogger().warning("moderation/freeze: could not save: " + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!frozen.contains(event.getPlayer().getUniqueId()) || !event.hasChangedBlock()) {
            return;
        }
        Location from = event.getFrom();
        Location to = from.clone().setDirection(event.getTo().getDirection());
        event.setTo(to);
        if (cfgBool("actionbar-reminder", true)) {
            event.getPlayer().sendActionBar(msgComponent("reminder"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (frozen.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msgComponent("cannot-chat"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!frozen.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        String label = event.getMessage().substring(1).split(" ")[0].toLowerCase(Locale.ROOT);
        boolean whitelisted = cfgStringList("allowed-commands").stream().anyMatch(c -> c.equalsIgnoreCase(label));
        if (!whitelisted) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(msgComponent("cannot-command"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // frozen state stays on disk, nothing to do
    }
}
