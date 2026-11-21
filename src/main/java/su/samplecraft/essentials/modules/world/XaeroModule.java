package su.samplecraft.essentials.modules.world;

import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Merged from {@code XaeroForceDisabler-1.5.jar} (FairXaero).
 * <p>
 * The Xaero minimap mod watches chat for magic strings and reacts to them, so the
 * original simply runs {@code tellraw <player> "§..."; } from the console. This port
 * sends the identical plain-text components per player instead of building console
 * commands, which keeps the mod-facing byte sequence unchanged:
 * <ul>
 *   <li>{@code §n§o§m§i§n§i§m§a§p} – disable the minimap,</li>
 *   <li>{@code §f§a§i§r§x§a§e§r§o} – fair mode (cave mode),</li>
 *   <li>{@code §r§e§s§e§t§x§a§e§r§o} – reset back to normal,</li>
 *   <li>nether cave mode uses {@code §...netherisfair} + {@code ...netherisfair} extras.</li>
 * </ul>
 * World settings live under {@code global} and per-world {@code overrides} with
 * inheritance (unset keys fall back to global). Priority: disable &gt; fair-mode &gt; reset.
 * Permissions {@code xaero.ignoredisable} / {@code xaero.ignorefair} exempt players.
 */
public final class XaeroModule extends EssentialsModule implements Listener {

    private static final String PACKET_DISABLE = "\u00a7n\u00a7o\u00a7m\u00a7i\u00a7n\u00a7i\u00a7m\u00a7a\u00a7p";
    private static final String PACKET_FAIR = "\u00a7f\u00a7a\u00a7i\u00a7r\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o";
    private static final String PACKET_RESET = "\u00a7r\u00a7e\u00a7s\u00a7e\u00a7t\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o";
    private static final String PACKET_FAIR_NETHER_WORLD =
            "\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o\u00a7w\u00a7m\u00a7n\u00a7e\u00a7t\u00a7h\u00a7e\u00a7r\u00a7i\u00a7s\u00a7f\u00a7a\u00a7i\u00a7r";
    private static final String PACKET_FAIR_NETHER_MINIMAP =
            "\u00a7x\u00a7a\u00a7e\u00a7r\u00a7o\u00a7m\u00a7m\u00a7n\u00a7e\u00a7t\u00a7h\u00a7e\u00a7r\u00a7i\u00a7s\u00a7f\u00a7a\u00a7i\u00a7r";

    /** {@code null} means "inherit the global value". */
    private record Settings(Boolean fairMode, Boolean disable) {
        Settings with(Boolean fair, Boolean disable) {
            return new Settings(fair, disable);
        }
    }

    private final Map<String, Settings> overrides = new HashMap<>();
    private Settings global = new Settings(false, false);
    private boolean debug;

    public XaeroModule(SamplesEssentials plugin) {
        super("world", "xaero", "XaeroForceDisabler-1.5.jar",
                "Forces fair-mode / disables the Xaero minimap per world");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.xaero", "Edit Xaero minimap settings", PermissionDefault.OP);
        ensurePermission("xaero.ignoredisable", "Ignore disable packets", PermissionDefault.FALSE);
        ensurePermission("xaero.ignorefair", "Ignore fair-mode packets", PermissionDefault.FALSE);
        load();
        command("xaero", new XaeroCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
        syncLater(40L, this::applyAll);
    }

    private void load() {
        File file = ensureDefaultFile("config.yml");
        if (!file.isFile()) {
            return;
        }
        reloadConfig();
        debug = cfgBool("debug", false);
        global = new Settings(cfgBool("global.fair-mode", false), cfgBool("global.disable", false));
        overrides.clear();
        var section = config().getConfigurationSection("overrides");
        if (section == null) {
            return;
        }
        for (String world : section.getKeys(false)) {
            var entry = section.getConfigurationSection(world);
            if (entry == null) {
                continue;
            }
            Boolean fair = entry.contains("fair-mode") ? entry.getBoolean("fair-mode") : null;
            Boolean disable = entry.contains("disable") ? entry.getBoolean("disable") : null;
            if (fair != null || disable != null) {
                overrides.put(world, new Settings(fair, disable));
            }
        }
    }

    private void applyAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            apply(player);
        }
    }

    private void apply(Player player) {
        Settings override = overrides.get(player.getWorld().getName());
        boolean disable = global.disable() != null && global.disable();
        boolean fair = global.fairMode() != null && global.fairMode();
        if (override != null) {
            if (override.disable() != null) {
                disable = override.disable();
            }
            if (override.fairMode() != null) {
                fair = override.fairMode();
            }
        }
        if (disable && player.hasPermission("xaero.ignoredisable")) {
            disable = false;
        }
        if (fair && player.hasPermission("xaero.ignorefair")) {
            fair = false;
        }
        if (debug) {
            plugin.getLogger().info("Xaero: " + player.getName() + " in " + player.getWorld().getName()
                    + " | disable=" + disable + " fair=" + fair);
        }
        boolean nether = player.getWorld().getEnvironment() == World.Environment.NETHER;
        boolean globalFair = Boolean.TRUE.equals(global.fairMode());
        if (!disable && !fair) {
            if (nether && globalFair) {
                send(player, PACKET_FAIR);
                send(player, PACKET_FAIR_NETHER_MINIMAP);
                send(player, PACKET_FAIR_NETHER_WORLD);
            } else {
                send(player, PACKET_RESET);
            }
            return;
        }
        if (disable) {
            send(player, PACKET_DISABLE);
        }
        if (fair) {
            send(player, PACKET_FAIR);
        }
    }

    /** Same payload {@code tellraw <player> "<§-sequence>"} produced. */
    private void send(Player player, String packet) {
        player.sendMessage(Component.text(packet));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        syncLater(20L, () -> {
            if (event.getPlayer().isOnline()) {
                apply(event.getPlayer());
            }
        });
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        syncLater(20L, () -> {
            if (event.getPlayer().isOnline()) {
                apply(event.getPlayer());
            }
        });
    }

    private final class XaeroCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.isOp() && !sender.hasPermission("samplese.xaero")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(msgComponent("usage-header"));
                sender.sendMessage(msgComponent("usage-scopes"));
                sender.sendMessage(msgComponent("usage-settings"));
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> {
                    load();
                    applyAll();
                    sender.sendMessage(msgComponent("reloaded"));
                }
                case "manual-disable" -> {
                    sendToAll(PACKET_DISABLE, sender, "disable");
                }
                case "manual-fair" -> {
                    sendToAll(PACKET_FAIR, sender, "fair");
                }
                case "manual-reset" -> {
                    sendToAll(PACKET_RESET, sender, "reset");
                }
                case "manual-fairnether" -> {
                    sendToAll(PACKET_FAIR_NETHER_WORLD, sender, "fair-nether");
                }
                default -> handleSetting(sender, args);
            }
            return true;
        }

        private void sendToAll(String packet, CommandSender sender, String name) {
            plugin.getServer().getOnlinePlayers().forEach(player -> send(player, packet));
            sender.sendMessage(msgComponent("manual-sent", "packet", name));
        }

        private void handleSetting(CommandSender sender, String @NotNull [] args) {
            String scope = args[0];
            if (args.length < 2) {
                showSettings(sender, scope);
                return;
            }
            String setting = args[1].toLowerCase(Locale.ROOT);
            if (setting.equals("fair")) {
                setting = "fair-mode";
            }
            if (!setting.equals("fair-mode") && !setting.equals("disable")) {
                sender.sendMessage(msgComponent("unknown-setting", "setting", setting));
                return;
            }
            if (args.length == 2) {
                showSettings(sender, scope);
                return;
            }
            String raw = args[2].toLowerCase(Locale.ROOT);
            Boolean value;
            if (raw.equals("true")) {
                value = true;
            } else if (raw.equals("false")) {
                value = false;
            } else if (raw.equals("null") || raw.equals("unset")) {
                value = null;
            } else {
                sender.sendMessage(msgComponent("invalid-value"));
                return;
            }
            change(sender, scope, setting, value);
        }

        private void change(CommandSender sender, String scope, String setting, Boolean value) {
            boolean isGlobal = scope.equalsIgnoreCase("global");
            if (isGlobal && value == null) {
                sender.sendMessage(msgComponent("global-unset"));
                value = false;
            }
            String path = (isGlobal ? "global." : "overrides." + scope + ".") + setting;
            saveConfigValue(path, value);
            // Keep the in-memory model in sync without re-reading the file.
            if (isGlobal) {
                global = "disable".equals(setting) ? global.with(global.fairMode(), value)
                        : global.with(value, global.disable());
            } else {
                Settings current = overrides.getOrDefault(scope, new Settings(null, null));
                overrides.put(scope, "disable".equals(setting) ? current.with(current.fairMode(), value)
                        : current.with(value, current.disable()));
            }
            sender.sendMessage(msgComponent("changed",
                    "scope", scope, "setting", setting,
                    "value", value == null ? "unset" : value.toString()));
            applyAll();
        }

        private void showSettings(CommandSender sender, String scope) {
            sender.sendMessage(msgComponent("show-header", "scope", scope));
            if (scope.equalsIgnoreCase("global") || !overrides.containsKey(scope)) {
                if (!scope.equalsIgnoreCase("global")) {
                    sender.sendMessage(msgComponent("show-inherits"));
                }
                sender.sendMessage(msgComponent("show-line",
                        "setting", "disable", "value", String.valueOf(global.disable()),
                        "note", scope.equalsIgnoreCase("global") ? "" : " (global)"));
                sender.sendMessage(msgComponent("show-line",
                        "setting", "fair-mode", "value", String.valueOf(global.fairMode()),
                        "note", scope.equalsIgnoreCase("global") ? "" : " (global)"));
                return;
            }
            Settings override = overrides.get(scope);
            sender.sendMessage(msgComponent("show-line",
                    "setting", "disable",
                    "value", String.valueOf(override.disable() != null ? override.disable() : global.disable()),
                    "note", override.disable() != null ? "" : " (global)"));
            sender.sendMessage(msgComponent("show-line",
                    "setting", "fair-mode",
                    "value", String.valueOf(override.fairMode() != null ? override.fairMode() : global.fairMode()),
                    "note", override.fairMode() != null ? "" : " (global)"));
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("global", "reload",
                        "manual-disable", "manual-fair", "manual-reset", "manual-fairnether"));
                options.addAll(overrides.keySet());
                return filter(options, args[0]);
            }
            if (args.length == 2) {
                return filter(List.of("fair-mode", "disable"), args[1]);
            }
            if (args.length == 3) {
                return filter(List.of("true", "false", "unset"), args[2]);
            }
            return List.of();
        }

        private List<String> filter(List<String> options, String prefix) {
            String lower = prefix.toLowerCase(Locale.ROOT);
            List<String> result = new ArrayList<>();
            for (String option : options) {
                if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                    result.add(option);
                }
            }
            return result;
        }
    }
}
