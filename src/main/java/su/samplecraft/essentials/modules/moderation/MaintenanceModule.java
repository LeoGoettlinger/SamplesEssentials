package su.samplecraft.essentials.modules.moderation;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code Maintenance-Paper-5.1.0.jar} (eu.kennytv.maintenance).
 * <p>
 * Ported behaviour:
 * <ul>
 *   <li>{@code /maintenance} toggles maintenance mode, {@code on}/{@code off}/{@code status}
 *       explicit subcommands, players without {@code samplese.maintenance.bypass} are kicked
 *       (and cannot log in) with the configured kick message,</li>
 *   <li>while active the server-list MOTD is replaced by a random entry of
 *       {@code ping-message.messages.default} (PaperServerListPingEvent), the version
 *       string and player count can be overridden like the original settings,</li>
 *   <li>whitelist: {@code /maintenance whitelist add|remove|<player>} with the
 *       {@code maintenance-whitelist} behaviour — whitelisted players may join during
 *       maintenance; UUIDs persisted in {@code moderation/maintenance/whitelist.yml},</li>
 *   <li>timers: {@code starttimer <minutes>} / {@code endtimer <minutes>} (original
 *       seconds-based commands accept the same suffixes), {@code aborttimer},
 *       {@code schedule <time> [day]} saves recurring schedules, a broadcast at the
 *       start and end of each timer,</li>
 *   <li>{@code setmotd}, {@code setreason}, {@code reload}, {@code dump} (status output).</li>
 * </ul>
 */
public final class MaintenanceModule extends EssentialsModule implements Listener {

    private final Set<UUID> whitelist = ConcurrentHashMap.newKeySet();
    private final Map<DayOfWeek, List<LocalTime>> schedules = new HashMap<>();
    private final Random random = new Random();

    private long maintenanceTimerEnd = -1;
    private boolean maintenanceTimerIsStart = false;

    public MaintenanceModule(SamplesEssentials plugin) {
        super("moderation", "maintenance", "Maintenance-Paper-5.1.0.jar", "Maintenance mode with whitelist, timers and MOTD");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.maintenance", "Toggle maintenance mode", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.maintenance.bypass", "Join during maintenance", org.bukkit.permissions.PermissionDefault.OP);
        loadWhitelist();
        loadSchedules();
        command("maintenance", new MaintenanceCommand());
        onDisableHook(() -> {
            unbindCommands();
            saveWhitelist();
        });
        listen(this);
        if (isMaintenanceEnabled()) {
            syncLater(40L, this::kickNonWhitelisted);
        }
        syncRepeat(100L, 100L, this::tickSchedule);
    }

    /* ------------------------------------------------------------------ */
    /* state                                                              */
    /* ------------------------------------------------------------------ */

    private boolean isMaintenanceEnabled() {
        return cfgBool("maintenance-enabled", false);
    }

    private void setMaintenance(boolean enabled) {
        saveConfigValue("maintenance-enabled", enabled);
        if (enabled) {
            kickNonWhitelisted();
        }
    }

    private void kickNonWhitelisted() {
        Component kick = Text.parse(cfgString("kick-message",
                "<red>This server is currently under maintenance!\n<yellow>Please check back later."));
        for (Player player : new ArrayList<>(plugin.getServer().getOnlinePlayers())) {
            if (!canJoin(player)) {
                player.kick(kick);
            }
        }
    }

    private boolean canJoin(Player player) {
        return !isMaintenanceEnabled()
                || player.hasPermission("samplese.maintenance.bypass")
                || whitelist.contains(player.getUniqueId());
    }

    /* ------------------------------------------------------------------ */
    /* whitelist                                                          */
    /* ------------------------------------------------------------------ */

    private void loadWhitelist() {
        whitelist.clear();
        for (String raw : cfgStringList("whitelisted-players")) {
            try {
                whitelist.add(UUID.fromString(raw));
            } catch (IllegalArgumentException ignored) {
                // names are resolved lazily below
            }
        }
        var section = config().getConfigurationSection("whitelist-names");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                try {
                    whitelist.add(UUID.fromString(section.getString(name, "")));
                } catch (Exception ignored) {
                    // skip
                }
            }
        }
    }

    private void saveWhitelist() {
        List<String> raw = new ArrayList<>();
        whitelist.forEach(id -> raw.add(id.toString()));
        saveConfigValue("whitelisted-players", raw);
    }

    /* ------------------------------------------------------------------ */
    /* schedules / timers                                                 */
    /* ------------------------------------------------------------------ */

    private void loadSchedules() {
        schedules.clear();
        ConfigurationSection section = config().getConfigurationSection("scheduled-maintenance");
        if (section == null) {
            return;
        }
        for (String dayKey : section.getKeys(false)) {
            try {
                DayOfWeek day = DayOfWeek.valueOf(dayKey.toUpperCase(Locale.ROOT));
                List<LocalTime> times = new ArrayList<>();
                for (String raw : section.getStringList(dayKey)) {
                    times.add(LocalTime.parse(raw));
                }
                schedules.put(day, times);
            } catch (IllegalArgumentException ignored) {
                // unknown day
            }
        }
    }

    private void tickSchedule() {
        if (maintenanceTimerEnd > 0) {
            long remaining = maintenanceTimerEnd - System.currentTimeMillis();
            if (remaining <= 0) {
                boolean enable = maintenanceTimerIsStart;
                maintenanceTimerEnd = -1;
                setMaintenance(enable);
                broadcast(enable
                        ? cfgString("timers.start-now", "<green>Maintenance mode is now enabled.")
                        : cfgString("timers.end-now", "<green>Maintenance mode is now disabled."));
            }
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<LocalTime> today = schedules.get(now.getDayOfWeek());
        if (today == null) {
            return;
        }
        for (LocalTime time : today) {
            if (time.getHour() == now.getHour() && time.getMinute() == now.getMinute()) {
                boolean enable = !isMaintenanceEnabled();
                setMaintenance(enable);
                broadcast(enable
                        ? cfgString("timers.start-now", "<green>Maintenance mode is now enabled.")
                        : cfgString("timers.end-now", "<green>Maintenance mode is now disabled."));
            }
        }
    }

    private void broadcast(String raw) {
        Component component = Text.parse(raw);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(component);
        }
        plugin.getLogger().info(Text.plain(component));
    }

    private long parseDurationSeconds(String raw) {
        try {
            if (raw.endsWith("s")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1));
            }
            if (raw.endsWith("m")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1)) * 60L;
            }
            if (raw.endsWith("h")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1)) * 3600L;
            }
            if (raw.endsWith("d")) {
                return Long.parseLong(raw.substring(0, raw.length() - 1)) * 86400L;
            }
            return Long.parseLong(raw) * 60L; // original "starttimer <minutes>"
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatRemaining(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (canJoin(event.getPlayer())) {
            return;
        }
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER,
                Text.parse(cfgString("kick-message", "<red>This server is currently under maintenance!")));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(PaperServerListPingEvent event) {
        if (!isMaintenanceEnabled()) {
            return;
        }
        if (cfgBool("ping-message.enabled", true)) {
            List<String> messages = cfgStringList("ping-message.messages.default");
            if (messages.isEmpty()) {
                messages = List.of(cfgString("ping-message.default",
                        "<red>Currently under maintenance<br><gradient:#fbffc2:#0fffff>We will be back soon!"));
            }
            String motd = messages.get(random.nextInt(messages.size()));
            if (cfgBool("ping-message.replace-reason-placeholder", true)) {
                motd = motd.replace("%REASON%", cfgString("ping-message.reason", "Maintenance"));
            }
            event.motd(Text.parse(motd));
        }
        if (cfgBool("ping-message.custom-player-count", false)) {
            event.setProtocolVersion(-1);
            event.setVersion(cfgString("ping-message.player-count-message", "Maintenance"));
        }
        List<String> hover = cfgStringList("ping-message.player-count-hover");
        if (!hover.isEmpty()) {
            List<PaperServerListPingEvent.ListedPlayerInfo> listed = event.getListedPlayers();
            listed.clear();
            for (String line : hover) {
                listed.add(new PaperServerListPingEvent.ListedPlayerInfo(
                        Text.plain(Text.parse(line)), UUID.randomUUID()));
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class MaintenanceCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.maintenance")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                boolean enable = !isMaintenanceEnabled();
                setMaintenance(enable);
                sender.sendMessage(msgComponent(enable ? "enabled" : "disabled"));
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "on", "enable", "true" -> {
                    setMaintenance(true);
                    sender.sendMessage(msgComponent("enabled"));
                }
                case "off", "disable", "false" -> {
                    setMaintenance(false);
                    sender.sendMessage(msgComponent("disabled"));
                }
                case "status" -> sender.sendMessage(msgComponent("status",
                        "state", isMaintenanceEnabled() ? "enabled" : "disabled",
                        "timer", maintenanceTimerEnd > 0
                                ? formatRemaining(Math.max(0, (maintenanceTimerEnd - System.currentTimeMillis()) / 1000))
                                : "none",
                        "whitelisted", String.valueOf(whitelist.size())));
                case "starttimer", "endtimer" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("timer-usage"));
                        return true;
                    }
                    long seconds = parseDurationSeconds(args[1]);
                    if (seconds <= 0) {
                        sender.sendMessage(msgComponent("timer-invalid"));
                        return true;
                    }
                    boolean start = args[0].equalsIgnoreCase("starttimer");
                    maintenanceTimerEnd = System.currentTimeMillis() + seconds * 1000L;
                    maintenanceTimerIsStart = start;
                    broadcast(cfgString(start ? "timers.start-scheduled" : "timers.end-scheduled",
                            start ? "<yellow>Maintenance will start in <white>%TIME%</white>!"
                                    : "<yellow>Maintenance will end in <white>%TIME%</white>!")
                            .replace("%TIME%", formatRemaining(seconds)));
                    sender.sendMessage(msgComponent("timer-set", "time", formatRemaining(seconds)));
                }
                case "aborttimer", "abort" -> {
                    maintenanceTimerEnd = -1;
                    sender.sendMessage(msgComponent("timer-aborted"));
                }
                case "schedule" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("schedule-usage"));
                        return true;
                    }
                    try {
                        LocalTime time = LocalTime.parse(args[1].length() == 5 ? args[1] : args[1] + ":00");
                        DayOfWeek day = args.length > 2
                                ? DayOfWeek.valueOf(args[2].toUpperCase(Locale.ROOT))
                                : LocalDateTime.now().getDayOfWeek();
                        List<LocalTime> times = schedules.computeIfAbsent(day, d -> new ArrayList<>());
                        times.add(time);
                        List<String> raw = new ArrayList<>();
                        for (LocalTime entry : times) {
                            raw.add(entry.format(DateTimeFormatter.ofPattern("HH:mm")));
                        }
                        saveConfigValue("scheduled-maintenance." + day.name(), raw);
                        sender.sendMessage(msgComponent("schedule-added",
                                "time", time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                "day", day.name()));
                    } catch (Exception e) {
                        sender.sendMessage(msgComponent("schedule-invalid"));
                    }
                }
                case "whitelist", "wl" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("whitelist-usage"));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("list")) {
                        sender.sendMessage(msgComponent("whitelist-header", "count", String.valueOf(whitelist.size())));
                        for (UUID id : whitelist) {
                            String name = plugin.getServer().getOfflinePlayer(id).getName();
                            sender.sendMessage(Text.parse("<gray>- <white>" + (name == null ? id : name) + "</white>"));
                        }
                        return true;
                    }
                    boolean remove = args[1].equalsIgnoreCase("remove");
                    if (remove && args.length < 3) {
                        sender.sendMessage(msgComponent("whitelist-usage"));
                        return true;
                    }
                    String targetName = args[args.length - 1];
                    var target = plugin.getServer().getOfflinePlayerIfCached(targetName);
                    UUID id;
                    if (target == null || !target.hasPlayedBefore()) {
                        id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + targetName).getBytes());
                    } else {
                        id = target.getUniqueId();
                    }
                    if (remove) {
                        whitelist.remove(id);
                        sender.sendMessage(msgComponent("whitelist-removed", "player", targetName));
                    } else {
                        whitelist.add(id);
                        sender.sendMessage(msgComponent("whitelist-added", "player", targetName));
                    }
                    saveWhitelist();
                }
                case "setmotd" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("setmotd-usage"));
                        return true;
                    }
                    String motd = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    saveConfigValue("ping-message.messages.default", List.of(motd));
                    sender.sendMessage(msgComponent("motd-set"));
                }
                case "setreason" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("setreason-usage"));
                        return true;
                    }
                    saveConfigValue("ping-message.reason", String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
                    sender.sendMessage(msgComponent("reason-set"));
                }
                case "reload" -> {
                    reloadConfig();
                    loadWhitelist();
                    loadSchedules();
                    sender.sendMessage(msgComponent("reloaded"));
                }
                case "dump" -> sender.sendMessage(msgComponent("dump",
                        "state", isMaintenanceEnabled() ? "enabled" : "disabled",
                        "whitelisted", String.valueOf(whitelist.size()),
                        "schedules", String.valueOf(schedules.values().stream().mapToInt(List::size).sum())));
                default -> sender.sendMessage(msgComponent("usage"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("on", "off", "status", "starttimer", "endtimer", "aborttimer",
                                "schedule", "whitelist", "setmotd", "setreason", "reload", "dump").stream()
                        .filter(sub -> sub.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("whitelist")) {
                return List.of("add", "remove", "list");
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("starttimer") || args[0].equalsIgnoreCase("endtimer"))) {
                return List.of("5m", "10m", "1h", "2h");
            }
            return List.of();
        }
    }
}
