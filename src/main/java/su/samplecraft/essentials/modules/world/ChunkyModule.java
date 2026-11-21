package su.samplecraft.essentials.modules.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Merged from {@code Chunky-Bukkit-1.4.40.jar} (org.popcraft.chunky).
 * <p>
 * Pregenerates chunks around a centre. The original walks a shape iterator
 * (circle / square / diamond / pentagon / hexagon / star / rectangle / ellipse /
 * triangle) with a bounded working set and reports progress + ETA.
 * <p>
 * This port keeps the same user-facing surface:
 * {@code /chunky help|world|center|radius|corners|spawn|shape|pattern|selection|
 * start|pause|continue|cancel|progress|silent|quiet|reload}.
 * Generation runs on the main thread in {@code chunks-per-tick} batches so it is
 * server-safe; the task state is saved to {@code data/tasks.yml} so
 * {@code continue} survives restarts (when {@code continue-on-restart} is on).
 * {@code trim} deletes region files outside the selection is intentionally not
 * ported — deleting world data is destructive and belongs to WorldEdit; the
 * command reports that instead of silently doing nothing.
 */
public final class ChunkyModule extends EssentialsModule {

    private enum Shape {
        CIRCLE, SQUARE, DIAMOND, PENTAGON, HEXAGON, STAR, RECTANGLE, ELLIPSE, TRIANGLE;

        static Shape parse(String raw) {
            for (Shape shape : values()) {
                if (shape.name().equalsIgnoreCase(raw)) {
                    return shape;
                }
            }
            return null;
        }
    }

    /** Selection being edited before a task starts. */
    private static final class Selection {
        String world;
        double centerX;
        double centerZ;
        double radiusX;
        double radiusZ;
        Shape shape = Shape.SQUARE;

        Selection copy() {
            Selection copy = new Selection();
            copy.world = world;
            copy.centerX = centerX;
            copy.centerZ = centerZ;
            copy.radiusX = radiusX;
            copy.radiusZ = radiusZ;
            copy.shape = shape;
            return copy;
        }
    }

    /** A running (or paused) generation task for one world. */
    private final class Task {
        final Selection selection;
        private final Iterator<long[]> chunks;
        private final long total;
        long processed;
        long started;
        long lastSample;
        long lastSampleChunks;
        double rate;
        long etaSeconds;

        Task(Selection selection) {
            this.selection = selection.copy();
            this.chunks = chunkIterator(this.selection);
            this.total = countChunks(this.selection);
            this.started = System.currentTimeMillis();
            this.lastSample = started;
        }

        boolean done() {
            return !chunks.hasNext();
        }

        long[] next() {
            return chunks.next();
        }
    }

    private final Selection selection = new Selection();
    private final Map<String, Task> tasks = new LinkedHashMap<>();
    private boolean silent;
    private int quietInterval;
    private long nextQuietAt;

    public ChunkyModule(SamplesEssentials plugin) {
        super("world", "chunky", "Chunky-Bukkit-1.4.40.jar",
                "Pre-generates chunks around a centre with progress and ETA");
    }

    @Override
    protected void onEnable() {
        if (ensureDefaultFile("config.yml").isFile()) {
            reloadConfig();
        }
        silent = cfgBool("silent", false);
        quietInterval = cfgInt("quiet-interval", 0);
        nextQuietAt = 0L;
        updateIntervalMillis = Math.max(1, cfgInt("update-interval", 1)) * 1000L;
        chunksPerTick = Math.max(1, cfgInt("chunks-per-tick", 25));
        command("chunky", new ChunkyCommand());
        onDisableHook(this::unbindCommands);
        onDisableHook(this::saveTasks);
        if (cfgBool("continue-on-restart", false)) {
            syncLater(40L, this::restoreTasks);
        }
        scheduleTicker();
    }

    private long updateIntervalMillis = 1000L;
    private int chunksPerTick = 25;
    private org.bukkit.scheduler.BukkitTask ticker;

    private void scheduleTicker() {
        if (ticker != null) {
            ticker.cancel();
        }
        ticker = syncRepeat(1L, 1L, this::tickTasks);
    }

    /* ------------------------------------------------------------------ */
    /* chunk math                                                         */
    /* ------------------------------------------------------------------ */

    private Iterator<long[]> chunkIterator(Selection selection) {
        List<long[]> chunks = new ArrayList<>();
        int minChunkX = (int) Math.floor((selection.centerX - selection.radiusX) / 16.0);
        int maxChunkX = (int) Math.floor((selection.centerX + selection.radiusX) / 16.0);
        int minChunkZ = (int) Math.floor((selection.centerZ - selection.radiusZ) / 16.0);
        int maxChunkZ = (int) Math.floor((selection.centerZ + selection.radiusZ) / 16.0);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                int blockX = (chunkX << 4) + 8;
                int blockZ = (chunkZ << 4) + 8;
                if (inside(selection, blockX, blockZ)) {
                    chunks.add(new long[]{chunkX, chunkZ});
                }
            }
        }
        return chunks.iterator();
    }

    private long countChunks(Selection selection) {
        Iterator<long[]> iterator = chunkIterator(selection);
        long count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /** Shape test, mirroring Chunky's {@code Shape#isBounding}. */
    private boolean inside(Selection selection, double x, double z) {
        double dx = x - selection.centerX;
        double dz = z - selection.centerZ;
        double radiusX = Math.max(1.0E-4, selection.radiusX);
        double radiusZ = Math.max(1.0E-4, selection.radiusZ);
        double nx = dx / radiusX;
        double nz = dz / radiusZ;
        return switch (selection.shape) {
            case SQUARE, RECTANGLE -> Math.abs(dx) <= radiusX && Math.abs(dz) <= radiusZ;
            case CIRCLE, ELLIPSE -> nx * nx + nz * nz <= 1.0;
            case DIAMOND -> Math.abs(nx) + Math.abs(nz) <= 1.0;
            case PENTAGON -> insidePolygon(5, x, z, selection, 1.0);
            case HEXAGON -> insidePolygon(6, x, z, selection, 1.0);
            case TRIANGLE -> insidePolygon(3, x, z, selection, 1.0);
            case STAR -> insidePolygon(5, x, z, selection, 0.42);
            default -> false;
        };
    }

    /** Regular n-gon (or star) test in the scaled unit space used by Chunky. */
    private boolean insidePolygon(int points, double x, double z, Selection selection, double innerRatio) {
        double radiusX = Math.max(1.0E-4, selection.radiusX);
        double radiusZ = Math.max(1.0E-4, selection.radiusZ);
        double px = (x - selection.centerX) / radiusX;
        double pz = (z - selection.centerZ) / radiusZ;
        double distance = Math.hypot(px, pz);
        double angle = Math.atan2(pz, px);
        double step = (Math.PI * 2.0) / points;
        // distance from the centre to the polygon edge along this angle
        double unit = Math.cos(Math.PI / points) / Math.cos(((angle % step) + step) % step - Math.PI / points);
        double limit = unit;
        if (innerRatio < 1.0) {
            // star: alternate between outer and inner radius vertices
            double half = step / 2.0;
            double phase = ((angle % step) + step) % step;
            double interpolated = phase < half
                    ? interpolate(unit, unit * innerRatio, phase / half)
                    : interpolate(unit * innerRatio, unit, (phase - half) / half);
            limit = interpolated;
        }
        return distance <= limit;
    }

    private double interpolate(double from, double to, double progress) {
        return from + (to - from) * progress;
    }

    /* ------------------------------------------------------------------ */
    /* task ticking                                                       */
    /* ------------------------------------------------------------------ */

    private void tickTasks() {
        if (tasks.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Task>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Task> entry = iterator.next();
            Task task = entry.getValue();
            World world = plugin.getServer().getWorld(entry.getKey());
            if (world == null) {
                continue;
            }
            for (int i = 0; i < chunksPerTick && !task.done(); i++) {
                long[] chunk = task.next();
                // getChunkAt blocks only for that chunk and generates it if missing.
                world.getChunkAt((int) chunk[0], (int) chunk[1]);
                task.processed++;
            }
            updateStats(task, now);
            if (task.done()) {
                iterator.remove();
                broadcast(msg("task_done", "world", entry.getKey(),
                        "chunks", String.valueOf(task.processed),
                        "percent", "100",
                        "hours", String.valueOf(task.etaSeconds / 3600),
                        "minutes", String.valueOf((task.etaSeconds % 3600) / 60),
                        "seconds", String.valueOf(task.etaSeconds % 60)));
                saveTasks();
                continue;
            }
            boolean quiet = quietInterval > 0 && now < nextQuietAt;
            if (!silent && !quiet && now - task.lastSample >= updateIntervalMillis) {
                task.lastSample = now;
                broadcast(msg("task_update", "world", entry.getKey(),
                        "chunks", String.valueOf(task.processed),
                        "percent", String.format(Locale.ROOT, "%.2f", percent(task)),
                        "rate", String.format(Locale.ROOT, "%.2f", task.rate),
                        "hours", String.valueOf(task.etaSeconds / 3600),
                        "minutes", String.valueOf((task.etaSeconds % 3600) / 60),
                        "seconds", String.valueOf(task.etaSeconds % 60)));
            }
        }
    }

    private void updateStats(Task task, long now) {
        long elapsed = now - task.started;
        long delta = now - task.lastSample;
        if (delta >= 1000L) {
            long chunks = task.processed - task.lastSampleChunks;
            task.rate = chunks / (delta / 1000.0);
            task.lastSample = now;
            task.lastSampleChunks = task.processed;
        } else if (task.rate <= 0 && elapsed > 0) {
            task.rate = task.processed / (elapsed / 1000.0);
        }
        long remaining = task.total - task.processed;
        task.etaSeconds = task.rate > 0.01 ? (long) (remaining / task.rate) : -1L;
    }

    private double percent(Task task) {
        return task.total <= 0 ? 100.0 : 100.0 * task.processed / task.total;
    }

    private void broadcast(String message) {
        plugin.getServer().getConsoleSender().sendMessage(Text.parse(message));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("samplese.chunky")) {
                player.sendMessage(Text.parse(message));
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private void saveTasks() {
        File file = new File(taskFolder(), "tasks.yml");
        org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
            String path = "tasks." + entry.getKey();
            Task task = entry.getValue();
            config.set(path + ".center-x", task.selection.centerX);
            config.set(path + ".center-z", task.selection.centerZ);
            config.set(path + ".radius-x", task.selection.radiusX);
            config.set(path + ".radius-z", task.selection.radiusZ);
            config.set(path + ".shape", task.selection.shape.name());
            config.set(path + ".processed", task.processed);
        }
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().warning("Chunky: could not save tasks: " + e.getMessage());
        }
    }

    private void restoreTasks() {
        File file = new File(taskFolder(), "tasks.yml");
        if (!file.isFile()) {
            return;
        }
        org.bukkit.configuration.file.YamlConfiguration config =
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        var section = config.getConfigurationSection("tasks");
        if (section == null) {
            return;
        }
        for (String worldName : section.getKeys(false)) {
            var entry = section.getConfigurationSection(worldName);
            if (entry == null || tasks.containsKey(worldName)) {
                continue;
            }
            Selection restored = new Selection();
            restored.world = worldName;
            restored.centerX = entry.getDouble("center-x");
            restored.centerZ = entry.getDouble("center-z");
            restored.radiusX = entry.getDouble("radius-x");
            restored.radiusZ = entry.getDouble("radius-z");
            restored.shape = Shape.parse(entry.getString("shape", "SQUARE"));
            Task task = new Task(restored);
            // Fast-forward past already generated chunks without touching them again.
            long skip = entry.getLong("processed");
            while (skip-- > 0 && task.chunks.hasNext()) {
                task.chunks.next();
                task.processed++;
            }
            task.lastSampleChunks = task.processed;
            tasks.put(worldName, task);
            plugin.getLogger().info("Chunky: resumed task for " + worldName);
        }
    }

    /** Folder used for the task file, next to the module config. */
    private File taskFolder() {
        File folder = new File(getModuleFolder(), "data");
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class ChunkyCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.chunky")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                showHelp(sender, 1);
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "help" -> showHelp(sender, args.length > 1 ? parseInt(args[1], 1) : 1);
                case "world" -> setWorld(sender, args);
                case "center" -> setCenter(sender, args);
                case "radius" -> setRadius(sender, args);
                case "corners" -> setCorners(sender, args);
                case "spawn" -> setSpawn(sender);
                case "shape" -> setShape(sender, args);
                case "pattern" -> send(sender, "format_pattern_unsupported");
                case "selection" -> showSelection(sender);
                case "start" -> start(sender);
                case "pause" -> pause(sender);
                case "continue" -> resume(sender);
                case "cancel" -> cancel(sender, args);
                case "progress" -> progress(sender);
                case "silent" -> {
                    silent = !silent;
                    send(sender, silent ? "format_silent_on" : "format_silent_off");
                }
                case "quiet" -> setQuiet(sender, args);
                case "reload" -> {
                    if (!tasks.isEmpty()) {
                        send(sender, "format_reload_tasks_running");
                        return true;
                    }
                    reloadConfig();
                    updateIntervalMillis = Math.max(1, cfgInt("update-interval", 1)) * 1000L;
                    chunksPerTick = Math.max(1, cfgInt("chunks-per-tick", 25));
                    quietInterval = cfgInt("quiet-interval", 0);
                    send(sender, "format_reloaded");
                }
                case "trim" -> send(sender, "format_trim_unsupported");
                default -> send(sender, "format_unknown_command");
            }
            return true;
        }

        private void showHelp(CommandSender sender, int page) {
            send(sender, "help_menu");
            List<String> keys = List.of("help_world", "help_center", "help_radius", "help_corners",
                    "help_spawn", "help_shape", "help_selection", "help_start", "help_pause",
                    "help_continue", "help_cancel", "help_progress", "help_silent", "help_quiet",
                    "help_reload");
            int perPage = 9;
            int pages = (keys.size() + perPage - 1) / perPage;
            int current = Math.max(1, Math.min(page, pages));
            for (int i = (current - 1) * perPage; i < Math.min(keys.size(), current * perPage); i++) {
                sender.sendMessage(Text.parse(msg(keys.get(i))));
            }
            if (current < pages) {
                sender.sendMessage(Text.parse(msg("help_more", "command", "/chunky help " + (current + 1))));
            }
        }

        private void setWorld(CommandSender sender, String[] args) {
            if (args.length < 2) {
                send(sender, "help_world");
                return;
            }
            World world = plugin.getServer().getWorld(args[1]);
            if (world == null) {
                send(sender, "format_invalid_world", "world", args[1]);
                return;
            }
            if (selection.world == null) {
                selection.centerX = Math.floor(world.getSpawnLocation().getX());
                selection.centerZ = Math.floor(world.getSpawnLocation().getZ());
            }
            selection.world = world.getName();
            send(sender, "format_world", "world", world.getName());
        }

        private World currentWorld(CommandSender sender) {
            World world = selection.world == null ? null : plugin.getServer().getWorld(selection.world);
            if (world == null) {
                send(sender, "format_no_world");
            }
            return world;
        }

        private void setCenter(CommandSender sender, String[] args) {
            World world = currentWorld(sender);
            if (world == null) {
                return;
            }
            if (args.length < 3) {
                send(sender, "help_center");
                return;
            }
            Double x = parseDouble(args[1]);
            Double z = parseDouble(args[2]);
            if (x == null || z == null) {
                send(sender, "help_center");
                return;
            }
            selection.centerX = x;
            selection.centerZ = z;
            send(sender, "format_center", "x", formatNumber(x), "z", formatNumber(z));
        }

        private void setRadius(CommandSender sender, String[] args) {
            if (args.length < 2) {
                send(sender, "help_radius");
                return;
            }
            Double radius = parseDouble(args[1]);
            if (radius == null) {
                send(sender, "help_radius");
                return;
            }
            selection.radiusX = radius;
            selection.radiusZ = args.length > 2 && parseDouble(args[2]) != null ? parseDouble(args[2]) : radius;
            send(sender, "format_radius", "radius", formatNumber(radius));
        }

        private void setCorners(CommandSender sender, String[] args) {
            World world = currentWorld(sender);
            if (world == null) {
                return;
            }
            if (args.length < 5) {
                send(sender, "help_corners");
                return;
            }
            Double x1 = parseDouble(args[1]);
            Double z1 = parseDouble(args[2]);
            Double x2 = parseDouble(args[3]);
            Double z2 = parseDouble(args[4]);
            if (x1 == null || z1 == null || x2 == null || z2 == null) {
                send(sender, "help_corners");
                return;
            }
            selection.centerX = (x1 + x2) / 2.0;
            selection.centerZ = (z1 + z2) / 2.0;
            selection.radiusX = Math.abs(x2 - x1) / 2.0;
            selection.radiusZ = Math.abs(z2 - z1) / 2.0;
            send(sender, "format_center", "x", formatNumber(selection.centerX), "z",
                    formatNumber(selection.centerZ));
            send(sender, "format_radius", "radius", formatNumber(Math.max(selection.radiusX, selection.radiusZ)));
        }

        private void setSpawn(CommandSender sender) {
            World world = currentWorld(sender);
            if (world == null) {
                return;
            }
            Location spawn = world.getSpawnLocation();
            selection.centerX = Math.floor(spawn.getX());
            selection.centerZ = Math.floor(spawn.getZ());
            send(sender, "format_center", "x", formatNumber(selection.centerX),
                    "z", formatNumber(selection.centerZ));
        }

        private void setShape(CommandSender sender, String[] args) {
            if (args.length < 2) {
                send(sender, "help_shape");
                return;
            }
            Shape shape = Shape.parse(args[1]);
            if (shape == null) {
                send(sender, "help_shape");
                return;
            }
            selection.shape = shape;
            send(sender, "format_shape", "shape", shape.name().toLowerCase(Locale.ROOT));
        }

        private void showSelection(CommandSender sender) {
            if (selection.world == null) {
                send(sender, "format_no_world");
                return;
            }
            send(sender, "format_selection_world", "world", selection.world);
            send(sender, "format_selection_center", "x", formatNumber(selection.centerX),
                    "z", formatNumber(selection.centerZ));
            if (selection.radiusX == selection.radiusZ) {
                send(sender, "format_selection_radius", "radius", formatNumber(selection.radiusX));
            } else {
                send(sender, "format_selection_radii", "radiusX", formatNumber(selection.radiusX),
                        "radiusZ", formatNumber(selection.radiusZ));
            }
            send(sender, "format_selection_shape",
                    "shape", selection.shape.name().toLowerCase(Locale.ROOT));
        }

        private void start(CommandSender sender) {
            if (argsWorld(sender) == null) {
                return;
            }
            if (tasks.containsKey(selection.world)) {
                send(sender, "format_started_already", "world", selection.world);
                return;
            }
            Task task = new Task(selection);
            tasks.put(selection.world, task);
            send(sender, "format_started", "world", selection.world, "chunks", String.valueOf(task.total));
            saveTasks();
        }

        /** Validates the selection world exists and fills it from the sender's world. */
        private World argsWorld(CommandSender sender) {
            if (selection.world == null) {
                if (sender instanceof Player player) {
                    selection.world = player.getWorld().getName();
                    selection.centerX = Math.floor(player.getLocation().getX());
                    selection.centerZ = Math.floor(player.getLocation().getZ());
                    selection.radiusX = 100.0D;
                    selection.radiusZ = 100.0D;
                } else {
                    send(sender, "format_no_world");
                    return null;
                }
            }
            World world = plugin.getServer().getWorld(selection.world);
            if (world == null) {
                send(sender, "format_no_world");
            }
            return world;
        }

        private void pause(CommandSender sender) {
            if (tasks.isEmpty()) {
                send(sender, "format_pause_no_tasks");
                return;
            }
            saveTasks();
            tasks.clear();
            send(sender, "format_paused");
        }

        private void resume(CommandSender sender) {
            if (!tasks.isEmpty()) {
                send(sender, "format_continue_running");
                return;
            }
            restoreTasks();
            if (tasks.isEmpty()) {
                send(sender, "format_continue_no_tasks");
            } else {
                send(sender, "format_continued", "tasks", String.valueOf(tasks.size()));
            }
        }

        private void cancel(CommandSender sender, String[] args) {
            if (tasks.isEmpty()) {
                send(sender, "format_cancel_no_tasks");
                return;
            }
            if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
                for (String worldName : tasks.keySet()) {
                    send(sender, "format_cancel_confirm_world", "world", worldName,
                            "command", "/chunky cancel confirm");
                }
                return;
            }
            List<String> worlds = new ArrayList<>(tasks.keySet());
            tasks.clear();
            saveTasks();
            for (String worldName : worlds) {
                send(sender, "format_cancel", "world", worldName);
            }
        }

        private void progress(CommandSender sender) {
            if (tasks.isEmpty()) {
                send(sender, "format_progress_no_tasks");
                return;
            }
            for (Map.Entry<String, Task> entry : tasks.entrySet()) {
                Task task = entry.getValue();
                send(sender, "format_progress_line",
                        "world", entry.getKey(),
                        "chunks", String.valueOf(task.processed),
                        "total", String.valueOf(task.total),
                        "percent", String.format(Locale.ROOT, "%.2f", percent(task)),
                        "rate", String.format(Locale.ROOT, "%.2f", task.rate),
                        "eta", task.etaSeconds < 0 ? "?" : String.format(Locale.ROOT, "%d:%02d:%02d",
                                task.etaSeconds / 3600, (task.etaSeconds % 3600) / 60, task.etaSeconds % 60));
            }
        }

        private void setQuiet(CommandSender sender, String[] args) {
            if (args.length < 2) {
                send(sender, "help_quiet");
                return;
            }
            Integer interval = parseIntOrNull(args[1]);
            if (interval == null) {
                send(sender, "help_quiet");
                return;
            }
            quietInterval = Math.max(0, interval);
            nextQuietAt = quietInterval <= 0 ? 0L : System.currentTimeMillis() + quietInterval * 1000L;
            saveConfigValue("quiet-interval", quietInterval);
            send(sender, "format_quiet", "interval", String.valueOf(quietInterval));
        }

        private Integer parseIntOrNull(String raw) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private int parseInt(String raw, int fallback) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                return fallback;
            }
        }

        private Double parseDouble(String raw) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private String formatNumber(double value) {
            return value == Math.floor(value) ? String.valueOf((long) value)
                    : String.format(Locale.ROOT, "%.1f", value);
        }

        private void send(CommandSender sender, String key, String... kv) {
            sender.sendMessage(Text.parse(msg(key, kv)));
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("help", "world", "center", "radius",
                        "corners", "spawn", "shape", "selection", "start", "pause", "continue",
                        "cancel", "progress", "silent", "quiet", "reload"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
                List<String> worlds = new ArrayList<>();
                plugin.getServer().getWorlds().forEach(world -> worlds.add(world.getName()));
                return worlds;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("shape")) {
                List<String> shapes = new ArrayList<>();
                for (Shape shape : Shape.values()) {
                    shapes.add(shape.name().toLowerCase(Locale.ROOT));
                }
                return shapes;
            }
            return List.of();
        }
    }
}
