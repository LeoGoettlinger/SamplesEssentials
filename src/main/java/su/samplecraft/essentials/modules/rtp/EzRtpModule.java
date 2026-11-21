package su.samplecraft.essentials.modules.rtp;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code EzRTP-3.4.3.jar} (com.skyblockexp.ezrtp).
 * <p>
 * Ported behaviour:
 * <ul>
 *   <li>all five candidate strategies of the original
 *       ({@code random/uniform}, {@code circle}, {@code square}, {@code triangle},
 *       {@code diamond}) with the exact sampling math,</li>
 *   <li>per-world / per-permission-group limits ({@code rtp-limits.worlds.<world>.group.vip},
 *       {@code group.staff}, world {@code default}, global {@code default}) with
 *       {@code cooldown-seconds}, {@code daily-limit}, {@code weekly-limit} and {@code cost},</li>
 *   <li>daily reset at midnight and weekly reset on Sunday midnight
 *       (UsageResetScheduler), usage persisted in {@code data/usage.yml},</li>
 *   <li>surface scan (solid block with two passable blocks above, nether capped at y=126,
 *       {@code max-surface-scan-depth} / {@code -nether}), water-surface rescue,</li>
 *   <li>countdown with bossbar and particles, cancel-on-move and move warning,</li>
 *   <li>{@code /rtp} (GUI-less), {@code /rtp <center>}, {@code /rtp reload|stats|setcenter|addcenter},
 *       {@code /forcertp [player]}, on-join RTP, bypass permissions.</li>
 * </ul>
 */
public final class EzRtpModule extends EssentialsModule implements Listener {

    /** RtpLimitSettings of the original. */
    private record LimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost) {
        static final LimitSettings FALLBACK = new LimitSettings(300, 10, 50, null);
    }

    /** Active countdown for a player. */
    private static final class Countdown {
        final Location origin;
        final BossBar bar;
        final Runnable onFinish;
        final String worldName;
        boolean finished;

        Countdown(Location origin, BossBar bar, Runnable onFinish, String worldName) {
            this.origin = origin;
            this.bar = bar;
            this.onFinish = onFinish;
            this.worldName = worldName;
        }
    }

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Countdown> countdowns = new ConcurrentHashMap<>();
    /** uuid -> world -> count */
    private final Map<UUID, Map<String, Integer>> daily = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> weekly = new ConcurrentHashMap<>();

    private ModuleStorage usage;
    private Map<String, Map<String, LimitSettings>> worldLimits = new LinkedHashMap<>();
    private LimitSettings defaultLimits = LimitSettings.FALLBACK;
    private List<String> bypassPermissions = List.of();
    private Map<String, NamedCenter> centers = new LinkedHashMap<>();

    private record NamedCenter(String world, int x, int z) {
    }

    public EzRtpModule(SamplesEssentials plugin) {
        super("rtp", "ezrtp", "EzRTP-3.4.3.jar", "Random teleport with limits, countdown, search patterns and /forcertp");
    }

    /* ------------------------------------------------------------------ */
    /* lifecycle                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rtp", "Use random teleport", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.rtp.force", "Force random teleports", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.rtp.bypass", "Bypass RTP cooldowns and limits", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.rtp.setcenter", "Manage RTP centers", org.bukkit.permissions.PermissionDefault.OP);
        usage = new ModuleStorage(plugin, getDataFolder(), "data/usage.yml");
        loadUsage();
        loadLimits();
        loadCenters();

        command("rtp", new RtpCommand());
        command("forcertp", new ForceRtpCommand());
        onDisableHook(() -> {
            unbindCommands();
            countdowns.forEach((id, c) -> {
                Player online = plugin.getServer().getPlayer(id);
                if (online != null) {
                    online.hideBossBar(c.bar);
                }
            });
            countdowns.clear();
            saveUsage();
        });
        listen(this);
        scheduleUsageResets();
    }

    /* ------------------------------------------------------------------ */
    /* limits (config/teleport/RtpLimitSettings + EzRtpConfiguration)      */
    /* ------------------------------------------------------------------ */

    private void loadLimits() {
        worldLimits = new LinkedHashMap<>();
        bypassPermissions = cfgStringList("limits.bypass-permissions");
        defaultLimits = parseLimit(config().getConfigurationSection("limits.default"), LimitSettings.FALLBACK);
        var worlds = config().getConfigurationSection("limits.worlds");
        if (worlds != null) {
            for (String world : worlds.getKeys(false)) {
                var worldSection = worlds.getConfigurationSection(world);
                if (worldSection == null) {
                    continue;
                }
                Map<String, LimitSettings> groupMap = new LinkedHashMap<>();
                for (String group : worldSection.getKeys(false)) {
                    groupMap.put(group, parseLimit(worldSection.getConfigurationSection(group), LimitSettings.FALLBACK));
                }
                worldLimits.put(world, groupMap);
            }
        }
    }

    private LimitSettings parseLimit(org.bukkit.configuration.ConfigurationSection section, LimitSettings def) {
        if (section == null) {
            return def;
        }
        int cooldown = section.getInt("cooldown-seconds", def.cooldownSeconds());
        int dailyLimit = section.getInt("daily-limit", def.dailyLimit());
        int weeklyLimit = section.getInt("weekly-limit", def.weeklyLimit());
        Double cost = section.contains("cost") ? section.getDouble("cost") : null;
        return new LimitSettings(cooldown, dailyLimit, weeklyLimit, cost);
    }

    /** resolveGroup: first group.<perm> entry the player has permission for. */
    private String resolveGroup(Player player, String world) {
        Map<String, LimitSettings> groupMap = worldLimits.get(world);
        if (groupMap == null) {
            return null;
        }
        for (String key : groupMap.keySet()) {
            if (key.startsWith("group.")) {
                String groupName = key.substring("group.".length());
                if (!groupName.isEmpty() && player.hasPermission("samplese.rtp.group." + groupName)) {
                    return key;
                }
            }
        }
        return null;
    }

    private LimitSettings limitFor(Player player, String world) {
        Map<String, LimitSettings> groupMap = worldLimits.get(world);
        if (groupMap != null) {
            String group = resolveGroup(player, world);
            if (group != null && groupMap.containsKey(group)) {
                return groupMap.get(group);
            }
            if (groupMap.containsKey("default")) {
                return groupMap.get("default");
            }
        }
        return defaultLimits;
    }

    private boolean bypasses(Player player, String bypassPermission) {
        if (player.hasPermission("samplese.rtp.bypass")) {
            return true;
        }
        for (String permission : bypassPermissions) {
            String mapped = permission.startsWith("ezrtp.") ? "samplese.rtp." + permission.substring("ezrtp.".length()) : permission;
            if (player.hasPermission(mapped)) {
                return true;
            }
        }
        return bypassPermission != null && !bypassPermission.isBlank() && player.hasPermission(bypassPermission);
    }

    /* ------------------------------------------------------------------ */
    /* usage storage                                                      */
    /* ------------------------------------------------------------------ */

    private void loadUsage() {
        daily.clear();
        weekly.clear();
        for (String scope : List.of("daily", "weekly")) {
            var section = usage.yaml().getConfigurationSection(scope);
            if (section == null) {
                continue;
            }
            Map<UUID, Map<String, Integer>> target = scope.equals("daily") ? daily : weekly;
            for (String uuid : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(uuid);
                    Map<String, Integer> perWorld = new HashMap<>();
                    var worldSection = section.getConfigurationSection(uuid);
                    if (worldSection != null) {
                        for (String world : worldSection.getKeys(false)) {
                            perWorld.put(world, worldSection.getInt(world));
                        }
                    }
                    target.put(id, perWorld);
                } catch (IllegalArgumentException ignored) {
                    // stale key
                }
            }
        }
    }

    private void saveUsage() {
        for (String scope : List.of("daily", "weekly")) {
            usage.yaml().set(scope, null);
            Map<UUID, Map<String, Integer>> source = scope.equals("daily") ? daily : weekly;
            source.forEach((id, perWorld) -> perWorld.forEach((world, count) ->
                    usage.yaml().set(scope + "." + id + "." + world, count)));
        }
        usage.save();
    }

    private int used(Map<UUID, Map<String, Integer>> store, UUID id, String world) {
        return store.getOrDefault(id, Map.of()).getOrDefault(world, 0);
    }

    private void increment(Map<UUID, Map<String, Integer>> store, UUID id, String world) {
        store.computeIfAbsent(id, k -> new HashMap<>()).merge(world, 1, Integer::sum);
    }

    /** UsageResetScheduler: daily at 00:00, weekly Sunday 00:00. */
    private void scheduleUsageResets() {
        scheduleRepeatingAtNextMidnight(false);
        scheduleRepeatingAtNextMidnight(true);
    }

    private void scheduleRepeatingAtNextMidnight(boolean weeklyReset) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime next;
        long period;
        if (weeklyReset) {
            next = now.with(DayOfWeek.SUNDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (!next.isAfter(now)) {
                next = next.plusWeeks(1);
            }
            period = 12_096_000L;
        } else {
            next = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (!next.isAfter(now)) {
                next = next.plusDays(1);
            }
            period = 1_728_000L;
        }
        long delay = Duration.between(now, next).getSeconds() * 20L;
        long finalPeriod = period;
        syncLater(delay, () -> syncRepeat(finalPeriod, finalPeriod, () -> {
            if (weeklyReset) {
                weekly.clear();
                saveUsage();
                plugin.getLogger().info("[ezrtp] Weekly RTP usage counts reset.");
            } else {
                daily.clear();
                saveUsage();
                plugin.getLogger().info("[ezrtp] Daily RTP usage counts reset.");
            }
        }));
    }

    /* ------------------------------------------------------------------ */
    /* named centers                                                      */
    /* ------------------------------------------------------------------ */

    private void loadCenters() {
        centers = new LinkedHashMap<>();
        var named = config().getConfigurationSection("centers.named");
        if (named == null) {
            return;
        }
        for (String key : named.getKeys(false)) {
            String world = named.getString(key + ".world");
            if (world == null) {
                continue;
            }
            centers.put(key.toLowerCase(Locale.ROOT), new NamedCenter(world,
                    named.getInt(key + ".center.x"), named.getInt(key + ".center.z")));
        }
    }

    private void saveCenter(String name, String world, int x, int z) {
        config().set("centers.named." + name + ".world", world);
        config().set("centers.named." + name + ".center.x", x);
        config().set("centers.named." + name + ".center.z", z);
        saveConfig();
        centers.put(name, new NamedCenter(world, x, z));
    }

    private void saveConfig() {
        try {
            config().save(getConfigFile());
        } catch (Exception e) {
            plugin.getLogger().warning("[ezrtp] could not save config: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* /rtp                                                               */
    /* ------------------------------------------------------------------ */

    private final class RtpCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@org.jetbrains.annotations.NotNull org.bukkit.command.CommandSender sender,
                                 @org.jetbrains.annotations.NotNull org.bukkit.command.Command command,
                                 @org.jetbrains.annotations.NotNull String label,
                                 String @org.jetbrains.annotations.NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.rtp")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length > 0) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "reload" -> {
                        if (!sender.hasPermission("samplese.rtp.setcenter")) {
                            player.sendMessage(plugin.getLang().component("no-permission"));
                            return true;
                        }
                        reloadConfig();
                        loadLimits();
                        loadCenters();
                        player.sendMessage(msgComponent("reloaded"));
                        return true;
                    }
                    case "stats" -> {
                        player.sendMessage(msgComponent("stats",
                                "daily", String.valueOf(used(daily, player.getUniqueId(), player.getWorld().getName())),
                                "weekly", String.valueOf(used(weekly, player.getUniqueId(), player.getWorld().getName())),
                                "daily-limit", String.valueOf(limitFor(player, player.getWorld().getName()).dailyLimit()),
                                "weekly-limit", String.valueOf(limitFor(player, player.getWorld().getName()).weeklyLimit())));
                        return true;
                    }
                    case "setcenter" -> {
                        if (!sender.hasPermission("samplese.rtp.setcenter")) {
                            player.sendMessage(plugin.getLang().component("no-permission"));
                            return true;
                        }
                        config().set("world", player.getWorld().getName());
                        config().set("center.x", player.getLocation().getBlockX());
                        config().set("center.z", player.getLocation().getBlockZ());
                        saveConfig();
                        player.sendMessage(msgComponent("center-set"));
                        return true;
                    }
                    case "addcenter" -> {
                        if (!sender.hasPermission("samplese.rtp.setcenter")) {
                            player.sendMessage(plugin.getLang().component("no-permission"));
                            return true;
                        }
                        if (args.length != 2 || args[1].isBlank()) {
                            player.sendMessage(msgComponent("addcenter-usage"));
                            return true;
                        }
                        String name = args[1].trim().toLowerCase(Locale.ROOT);
                        if (!name.matches("[a-z0-9_-]+")) {
                            player.sendMessage(msgComponent("addcenter-invalid"));
                            return true;
                        }
                        saveCenter(name, player.getWorld().getName(),
                                player.getLocation().getBlockX(), player.getLocation().getBlockZ());
                        player.sendMessage(msgComponent("center-added", "name", name));
                        return true;
                    }
                    default -> {
                        NamedCenter center = centers.get(args[0].toLowerCase(Locale.ROOT));
                        if (center != null) {
                            startTeleport(player, center.world(), center.x(), center.z(), false);
                            return true;
                        }
                        startTeleport(player, null, null, null, false);
                        return true;
                    }
                }
            }
            startTeleport(player, null, null, null, false);
            return true;
        }

        @Override
        public List<String> onTabComplete(@org.jetbrains.annotations.NotNull org.bukkit.command.CommandSender sender,
                                          @org.jetbrains.annotations.NotNull org.bukkit.command.Command command,
                                          @org.jetbrains.annotations.NotNull String alias,
                                          String @org.jetbrains.annotations.NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("stats", "reload", "setcenter", "addcenter"));
                options.addAll(centers.keySet());
                return options.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            return List.of();
        }
    }

    private final class ForceRtpCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@org.jetbrains.annotations.NotNull org.bukkit.command.CommandSender sender,
                                 @org.jetbrains.annotations.NotNull org.bukkit.command.Command command,
                                 @org.jetbrains.annotations.NotNull String label,
                                 String @org.jetbrains.annotations.NotNull [] args) {
            if (!sender.hasPermission("samplese.rtp.force")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            Player target = sender instanceof Player p ? p : null;
            if (args.length > 0) {
                target = plugin.getServer().getPlayerExact(args[0]);
            }
            if (target == null) {
                sender.sendMessage(msgComponent("forcertp-target-missing"));
                return true;
            }
            String world = args.length > 1 ? args[1] : null;
            cooldowns.remove(target.getUniqueId());
            startTeleport(target, world, null, null, true);
            sender.sendMessage(msgComponent("forcertp-success", "player", target.getName()));
            if (sender != target) {
                target.sendMessage(msgComponent("forcertp-target-notify"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@org.jetbrains.annotations.NotNull org.bukkit.command.CommandSender sender,
                                          @org.jetbrains.annotations.NotNull org.bukkit.command.Command command,
                                          @org.jetbrains.annotations.NotNull String alias,
                                          String @org.jetbrains.annotations.NotNull [] args) {
            if (args.length == 1) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)))
                        .toList();
            }
            if (args.length == 2) {
                return plugin.getServer().getWorlds().stream().map(World::getName).toList();
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* teleport flow (RandomTeleportService + TeleportPreCheckHandler)     */
    /* ------------------------------------------------------------------ */

    private void startTeleport(Player player, String worldName, Integer centerX, Integer centerZ, boolean forced) {
        World world = worldName == null ? player.getWorld() : plugin.getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage(msgComponent("world-missing", "world", worldName == null ? "?" : worldName));
            return;
        }
        LimitSettings limits = limitFor(player, world.getName());
        boolean bypass = bypasses(player, null);

        if (!forced) {
            Long last = cooldowns.get(player.getUniqueId());
            long cooldownMillis = Math.max(0, limits.cooldownSeconds()) * 1000L;
            if (last != null && cooldownMillis > 0 && !bypass) {
                long remaining = cooldownMillis - (System.currentTimeMillis() - last);
                if (remaining > 0) {
                    player.sendMessage(msgComponent("cooldown", "seconds",
                            cfgBool("limits.human-readable-cooldown", true)
                                    ? humanReadable(remaining / 1000L)
                                    : String.valueOf((remaining + 999) / 1000)));
                    return;
                }
            }
            if (!bypass) {
                int dailyLimit = limits.dailyLimit();
                if (dailyLimit >= 0 && hasDailyLimitEnabled() && used(daily, player.getUniqueId(), world.getName()) >= dailyLimit) {
                    player.sendMessage(msgComponent("limit-daily"));
                    return;
                }
                int weeklyLimit = limits.weeklyLimit();
                if (weeklyLimit >= 0 && used(weekly, player.getUniqueId(), world.getName()) >= weeklyLimit) {
                    player.sendMessage(msgComponent("limit-weekly"));
                    return;
                }
            }
            if (limits.cost() != null && limits.cost() > 0) {
                var economy = economy();
                if (economy == null) {
                    player.sendMessage(msgComponent("insufficient-funds", "cost", String.format(Locale.US, "%.2f", limits.cost())));
                    return;
                }
                if (!economy.has(player, limits.cost())) {
                    player.sendMessage(msgComponent("insufficient-funds", "cost", String.format(Locale.US, "%.2f", limits.cost())));
                    return;
                }
                economy.withdrawPlayer(player, limits.cost());
            }
        }

        int countdownSeconds = Math.max(0, cfgInt("countdown-seconds", 5));
        Runnable search = () -> searchAndTeleport(player, world, centerX, centerZ, limits, bypass, forced);
        if (countdownSeconds == 0) {
            search.run();
        } else {
            startCountdown(player, countdownSeconds, search);
        }
    }

    private boolean hasDailyLimitEnabled() {
        return !cfgBool("limits.disable-daily-limit", false);
    }

    private void startCountdown(Player player, int seconds, Runnable onFinish) {
        Component title = Text.parse(cfgString("countdown.bossbar.title",
                "<yellow>Teleporting in <white><seconds></white> seconds...</yellow>")
                .replace("<seconds>", String.valueOf(seconds)));
        BossBar bar = BossBar.bossBar(title, 1f,
                parseBossBarColor(cfgString("countdown.bossbar.color", "YELLOW")),
                parseBossBarOverlay(cfgString("countdown.bossbar.style", "SOLID")));
        Countdown countdown = new Countdown(player.getLocation().clone(), bar, onFinish, player.getWorld().getName());
        countdowns.put(player.getUniqueId(), countdown);
        if (cfgBool("countdown.bossbar.enabled", true)) {
            player.showBossBar(bar);
        }
        tickCountdown(player, countdown, seconds, seconds);
    }

    private void tickCountdown(Player player, Countdown countdown, int remaining, int total) {
        if (countdown.finished) {
            return;
        }
        if (!player.isOnline() || countdowns.get(player.getUniqueId()) != countdown) {
            finishCountdown(player, countdown, false);
            return;
        }
        if (remaining <= 0) {
            finishCountdown(player, countdown, true);
            return;
        }
        if (cfgBool("countdown.bossbar.enabled", true)) {
            countdown.bar.name(Text.parse(cfgString("countdown.bossbar.title", "Teleporting in <seconds>...")
                    .replace("<seconds>", String.valueOf(remaining))));
            countdown.bar.progress(Math.max(0f, Math.min(1f, (float) remaining / total)));
        }
        if (cfgBool("countdown.particles.enabled", true)) {
            spawnCountdownParticles(player);
        }
        syncLater(20L, () -> tickCountdown(player, countdown, remaining - 1, total));
    }

    private void spawnCountdownParticles(Player player) {
        Particle particle;
        try {
            particle = Particle.valueOf(cfgString("countdown.particles.type", "ENCHANTMENT_TABLE")
                    .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return;
        }
        int points = Math.max(1, cfgInt("countdown.particles.points", 12));
        double radius = cfgDouble("countdown.particles.radius", 1.2);
        Location base = player.getLocation();
        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2 / points;
            Location at = base.clone().add(Math.cos(angle) * radius,
                    cfgDouble("countdown.particles.height-offset", 0.8), Math.sin(angle) * radius);
            player.getWorld().spawnParticle(particle, at, 1, 0, 0, 0, cfgDouble("countdown.particles.extra", 0));
            if (i % 4 == 0) {
                try {
                    Particle secondary = Particle.valueOf(cfgString("countdown.particles.secondary-particle", "PORTAL")
                            .toUpperCase(Locale.ROOT));
                    player.getWorld().spawnParticle(secondary, at, cfgInt("countdown.particles.secondary-count", 6) / 4,
                            cfgDouble("countdown.particles.secondary-offset", 0.35),
                            cfgDouble("countdown.particles.secondary-offset", 0.35),
                            cfgDouble("countdown.particles.secondary-offset", 0.35), 0);
                } catch (IllegalArgumentException ignored) {
                    // unknown secondary particle
                }
            }
        }
    }

    private void finishCountdown(Player player, Countdown countdown, boolean run) {
        if (countdown.finished) {
            return;
        }
        countdown.finished = true;
        countdowns.remove(player.getUniqueId(), countdown);
        player.hideBossBar(countdown.bar);
        if (run) {
            countdown.onFinish.run();
        }
    }

    private static BossBar.Color parseBossBarColor(String raw) {
        try {
            return BossBar.Color.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.YELLOW;
        }
    }

    private static BossBar.Overlay parseBossBarOverlay(String raw) {
        try {
            return BossBar.Overlay.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private void searchAndTeleport(Player player, World world, Integer centerX, Integer centerZ,
                                   LimitSettings limits, boolean bypass, boolean forced) {
        if (!player.isOnline()) {
            return;
        }
        if (!cfgBool("messages.suppress-player", false)) {
            player.sendMessage(msgComponent("searching"));
        }
        SearchPattern pattern = SearchPattern.parse(cfgString("search-pattern", "random"));
        int minRadius = cfgInt("radius.min", 500);
        int maxRadius = cfgInt("radius.max", 20000);
        int attempts = Math.max(1, cfgInt("max-attempts", 16));
        double spawnX = centerX != null ? centerX : cfgDouble("center.x", 0);
        double spawnZ = centerZ != null ? centerZ : cfgDouble("center.z", 0);
        long start = System.currentTimeMillis();
        attempt(player, world, pattern, (int) spawnX, (int) spawnZ, minRadius, maxRadius, attempts, 0, start,
                limits, bypass, forced);
    }

    private void attempt(Player player, World world, SearchPattern pattern, int centerX, int centerZ,
                         int minRadius, int maxRadius, int attempts, int attempt, long start,
                         LimitSettings limits, boolean bypass, boolean forced) {
        if (!player.isOnline()) {
            return;
        }
        if (attempt >= attempts) {
            player.sendMessage(msgComponent("failed"));
            return;
        }
        int[] coords = pattern.candidate(centerX, centerZ, minRadius, maxRadius);
        int x = coords[0];
        int z = coords[1];
        if (!insideBorder(world, x, z, cfgInt("border-margin", 0))) {
            attempt(player, world, pattern, centerX, centerZ, minRadius, maxRadius, attempts, attempt + 1, start,
                    limits, bypass, forced);
            return;
        }
        world.getChunkAtAsync(x >> 4, z >> 4, true).thenAccept(chunk ->
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    Location safe = resolveCandidate(world, x, z);
                    if (safe == null || !isSafe(safe)) {
                        debugReject(safe, "unsafe candidate");
                        attempt(player, world, pattern, centerX, centerZ, minRadius, maxRadius, attempts, attempt + 1,
                                start, limits, bypass, forced);
                        return;
                    }
                    if (cfgInt("max-y", 0) > 0 && safe.getBlockY() > cfgInt("max-y", 320)) {
                        debugReject(safe, "above max-y");
                        attempt(player, world, pattern, centerX, centerZ, minRadius, maxRadius, attempts, attempt + 1,
                                start, limits, bypass, forced);
                        return;
                    }
                    player.teleportAsync(safe).thenAccept(success -> {
                        if (!Boolean.TRUE.equals(success)) {
                            player.sendMessage(msgComponent("failed"));
                            return;
                        }
                        if (!forced && !bypass) {
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                            increment(daily, player.getUniqueId(), world.getName());
                            increment(weekly, player.getUniqueId(), world.getName());
                            usage.saveLater();
                        }
                        player.sendMessage(msgComponent("success",
                                "x", String.valueOf(safe.getBlockX()),
                                "z", String.valueOf(safe.getBlockZ()),
                                "world", world.getName(),
                                "milliseconds", String.valueOf(System.currentTimeMillis() - start)));
                    });
                }));
    }

    /* ------------------------------------------------------------------ */
    /* LocationFinder + LocationValidator                                  */
    /* ------------------------------------------------------------------ */

    private Location resolveCandidate(World world, int x, int z) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        if (world.getEnvironment() == World.Environment.NETHER) {
            maxY = Math.min(maxY, 126);
        }
        int highestY = Math.min(world.getHighestBlockYAt(x, z), maxY);
        return highestY <= minY ? null : findPassableLocation(world, x, z, highestY, minY);
    }

    private Location findPassableLocation(World world, int x, int z, int startY, int minY) {
        int scanDepth = world.getEnvironment() == World.Environment.NETHER
                ? cfgInt("safety.recovery.max-surface-scan-depth-nether", 128)
                : cfgInt("safety.recovery.max-surface-scan-depth", 20);
        scanDepth = Math.max(1, Math.min(scanDepth, 128));
        int searchMinY = Math.max(minY, startY - scanDepth);
        for (int y = startY; y > searchMinY; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (!block.getType().isSolid()) {
                continue;
            }
            Block above = block.getRelative(BlockFace.UP);
            Block twoAbove = above.getRelative(BlockFace.UP);
            if (!isPassable(above) || !isPassable(twoAbove)) {
                continue;
            }
            if (!above.isLiquid()) {
                return new Location(world, x + 0.5, block.getY() + 1.0, z + 0.5);
            }
            Location surface = findSurfaceAboveLiquid(world, x, z, above.getY());
            if (surface != null) {
                return surface;
            }
        }
        return null;
    }

    private Location findSurfaceAboveLiquid(World world, int x, int z, int startY) {
        int maxY = world.getMaxHeight() - 1;
        int y = startY;
        while (y <= maxY && world.getBlockAt(x, y, z).isLiquid()) {
            y++;
        }
        if (y > maxY) {
            return null;
        }
        Block surface = world.getBlockAt(x, y, z);
        Block above = surface.getRelative(BlockFace.UP);
        return isPassable(surface) && isPassable(above) ? new Location(world, x + 0.5, surface.getY(), z + 0.5) : null;
    }

    private boolean isSafe(Location location) {
        if (location == null) {
            return false;
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }
        Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        Block destination = location.getBlock();
        Material typeBelow = blockBelow.getType();
        int y = location.getBlockY();
        int minY = cfgInt("min-y", world.getMinHeight());
        int maxY = cfgInt("max-y", world.getMaxHeight());
        if (y < minY || y > maxY) {
            return false;
        }
        if (destination.isLiquid()) {
            return false;
        }
        Block blockAbove = location.clone().add(0, 1, 0).getBlock();
        boolean unsafeBelow = unsafeBlocks().contains(typeBelow) && !(typeBelow == Material.WATER && !destination.isLiquid());
        if (unsafeBelow) {
            return false;
        }
        if (!blockBelow.getType().isSolid() && !(typeBelow == Material.WATER && !destination.isLiquid())) {
            return false;
        }
        if (blockAbove.isLiquid()) {
            if (world.getEnvironment() == World.Environment.NETHER) {
                return false;
            }
            if (world.getEnvironment() == World.Environment.NORMAL && blockAbove.getType() == Material.WATER) {
                return false;
            }
        }
        return insideBorder(world, location.getBlockX(), location.getBlockZ(), 0);
    }

    private List<Material> unsafeBlocks() {
        List<Material> materials = new ArrayList<>();
        for (String raw : cfgStringList("unsafe-blocks")) {
            try {
                materials.add(Material.valueOf(raw.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // unknown material
            }
        }
        return materials;
    }

    private static boolean isPassable(Block block) {
        Material type = block.getType();
        return type.isAir() || !type.isOccluding() && type != Material.LAVA && type != Material.WATER;
    }

    private boolean insideBorder(World world, int x, int z, int margin) {
        var border = world.getWorldBorder();
        if (border == null) {
            return true;
        }
        Location center = border.getCenter();
        double half = border.getSize() / 2.0 - Math.max(0, margin);
        return Math.abs(x - center.getX()) <= half && Math.abs(z - center.getZ()) <= half;
    }

    private void debugReject(Location candidate, String reason) {
        if (cfgBool("debug-rejection-logging", false) && candidate != null) {
            plugin.getLogger().info("[ezrtp] rejected " + candidate.getBlockX() + "," + candidate.getBlockY()
                    + "," + candidate.getBlockZ() + ": " + reason);
        }
    }

    /* ------------------------------------------------------------------ */
    /* events: countdown cancel-on-move, join tp, quit                     */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Countdown countdown = countdowns.get(event.getPlayer().getUniqueId());
        if (countdown == null) {
            return;
        }
        if (!cfgBool("countdown.cancel-on-move", true)) {
            return;
        }
        double cancelDistance = cfgDouble("countdown.cancel-distance", 2.0);
        if (event.getFrom().getWorld() != event.getTo().getWorld()
                || event.getFrom().distanceSquared(event.getTo()) > 0.0) {
            double moved = countdown.origin.distanceSquared(event.getTo());
            if (moved >= cancelDistance * cancelDistance) {
                finishCountdown(event.getPlayer(), countdown, false);
                event.getPlayer().sendMessage(msgComponent("countdown-move-cancel"));
                return;
            }
            double warnDistance = cfgDouble("countdown.warn-distance", 1.0);
            if (warnDistance > 0 && moved >= warnDistance * warnDistance) {
                event.getPlayer().sendActionBar(msgComponent("countdown-move-warn"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Countdown countdown = countdowns.remove(event.getPlayer().getUniqueId());
        if (countdown != null) {
            countdown.finished = true;
            event.getPlayer().hideBossBar(countdown.bar);
        }
        usage.saveLater();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!cfgBool("on-join.enabled", false)) {
            return;
        }
        Player player = event.getPlayer();
        if (cfgBool("on-join.only-first-join", false) && player.hasPlayedBefore()) {
            return;
        }
        String bypass = cfgString("on-join.bypass-permission", "");
        if (!bypass.isBlank() && player.hasPermission(bypass)) {
            return;
        }
        long delay = Math.max(0, (long) cfgInt("on-join.delay-ticks", 40));
        String worldName = cfgString("world", "auto");
        String targetWorld = "auto".equalsIgnoreCase(worldName) ? null : worldName;
        syncLater(delay, () -> {
            if (player.isOnline()) {
                player.sendMessage(msgComponent("join-searching"));
                startTeleport(player, targetWorld, null, null, true);
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private static String humanReadable(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long rest = seconds % 60;
        if (minutes < 60) {
            return rest == 0 ? minutes + "m" : minutes + "m " + rest + "s";
        }
        long hours = minutes / 60;
        long restMinutes = minutes % 60;
        return restMinutes == 0 ? hours + "h" : hours + "h " + restMinutes + "m";
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }
}
