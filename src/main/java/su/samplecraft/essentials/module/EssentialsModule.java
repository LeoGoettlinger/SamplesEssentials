package su.samplecraft.essentials.module;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitTask;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base class of every module.
 * <p>
 * Each legacy plugin from the old server became exactly one module. Modules are
 * grouped by categoria and each one owns a folder:
 * <pre>
 * plugins/SamplesEssentials/&lt;categoria&gt;/&lt;module&gt;/config.yml
 * plugins/SamplesEssentials/&lt;categoria&gt;/&lt;module&gt;/data/...
 * </pre>
 * A module is only enabled when the toggle in the main config is {@code true}.
 */
public abstract class EssentialsModule {

    private final String categoria;
    private final String name;
    private final String legacySource;
    private final String description;
    protected final SamplesEssentials plugin;

    private final List<Runnable> disableHooks = new ArrayList<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<PluginCommand> executors = new ArrayList<>();
    private YamlConfiguration config;
    private boolean enabled;

    protected EssentialsModule(String categoria, String name, String legacySource, String description) {
        this.categoria = categoria;
        this.name = name;
        this.legacySource = legacySource;
        this.description = description;
        this.plugin = SamplesEssentials.getInstance();
    }

    /* ------------------------------------------------------------------ */
    /* metadata                                                           */
    /* ------------------------------------------------------------------ */

    public String getCategoria() {
        return categoria;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return categoria + "/" + name;
    }

    /** Original jar this module was merged from - shown by {@code /samplese modules}. */
    public String getLegacySource() {
        return legacySource;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** plugins/SamplesEssentials/<categoria>/<module>/ */
    public File getModuleFolder() {
        File folder = new File(new File(plugin.getDataFolder(), categoria), name);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create module folder " + folder);
        }
        return folder;
    }

    public File getDataFolder() {
        File folder = new File(getModuleFolder(), "data");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create data folder " + folder);
        }
        return folder;
    }

    public File getConfigFile() {
        return new File(getModuleFolder(), "config.yml");
    }

    /* ------------------------------------------------------------------ */
    /* lifecycle                                                          */
    /* ------------------------------------------------------------------ */

    public final void enable() {
        if (enabled) {
            return;
        }
        enabled = true;
        try {
            this.config = loadConfig();
            onEnable();
        } catch (Throwable t) {
            plugin.getLogger().severe("Module '" + getKey() + "' failed to enable: " + t);
            t.printStackTrace();
            // never leave a half-initialised module behind
            try {
                disable();
            } catch (Throwable ignored) {
                // already failing
            }
        }
    }

    public final void disable() {
        if (!enabled) {
            return;
        }
        for (BukkitTask task : tasks) {
            try {
                task.cancel();
            } catch (Exception ignored) {
                // scheduler already shut down
            }
        }
        tasks.clear();
        for (Runnable hook : disableHooks) {
            try {
                hook.run();
            } catch (Exception ignored) {
                // best effort
            }
        }
        disableHooks.clear();
        try {
            onDisable();
        } catch (Throwable t) {
            plugin.getLogger().warning("Module '" + getKey() + "' errored while disabling: " + t);
        }
        enabled = false;
    }

    protected abstract void onEnable();

    protected void onDisable() {
        // optional
    }

    /* ------------------------------------------------------------------ */
    /* config                                                             */
    /* ------------------------------------------------------------------ */

    private YamlConfiguration loadConfig() {
        File file = getConfigFile();
        if (!file.isFile()) {
            // extract the packaged default: defaults/<categoria>/<module>/config.yml
            String resource = "defaults/" + categoria + "/" + name + "/config.yml";
            try (InputStream in = plugin.getResource(resource)) {
                if (in != null) {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.isDirectory()) {
                        parent.mkdirs();
                    }
                    Files.copy(in, file.toPath());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not extract default config for " + getKey() + ": " + e.getMessage());
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Copies {@code defaults/<categoria>/<module>/<fileName>} into the module folder
     * when it does not exist yet and returns the file (e.g. tags.yml, messages.yml).
     */
    protected File ensureDefaultFile(String fileName) {
        File file = new File(getModuleFolder(), fileName);
        if (file.isFile()) {
            return file;
        }
        String resource = "defaults/" + categoria + "/" + name + "/" + fileName;
        try (InputStream in = plugin.getResource(resource)) {
            if (in == null) {
                return file;
            }
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            Files.copy(in, file.toPath());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not extract default " + resource + ": " + e.getMessage());
        }
        return file;
    }

    /** Per-module config.yml, reloaded by /samplese reload. */
    protected YamlConfiguration config() {
        return config;
    }

    protected void reloadConfig() {
        this.config = YamlConfiguration.loadConfiguration(getConfigFile());
    }

    protected boolean cfgBool(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    protected int cfgInt(String path, int def) {
        return config.getInt(path, def);
    }

    protected double cfgDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    protected String cfgString(String path, String def) {
        return config.getString(path, def);
    }

    protected List<String> cfgStringList(String path) {
        return config.getStringList(path);
    }

    protected void saveConfigValue(String path, Object value) {
        config.set(path, value);
        try {
            config.save(getConfigFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save " + getConfigFile() + ": " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers for subclasses                                             */
    /* ------------------------------------------------------------------ */

    protected void listen(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    /** Registers a command declared in plugin.yml and binds it to the handler. */
    protected void command(String label, org.bukkit.command.CommandExecutor executor) {
        PluginCommand cmd = plugin.getCommand(label);
        if (cmd == null) {
            plugin.getLogger().warning("Command /" + label + " is missing from plugin.yml (module " + getKey() + ")");
            return;
        }
        cmd.setExecutor(executor);
        if (executor instanceof org.bukkit.command.TabCompleter completer) {
            cmd.setTabCompleter(completer);
        }
        executors.add(cmd);
    }

    /** Unbinds commands so a disabled module can no longer be executed. */
    protected void unbindCommands() {
        for (PluginCommand cmd : executors) {
            cmd.setExecutor(null);
            cmd.setTabCompleter(null);
        }
        executors.clear();
    }

    protected BukkitTask sync(Runnable r) {
        return plugin.getServer().getScheduler().runTask(plugin, r);
    }

    protected BukkitTask syncLater(long ticks, Runnable r) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, r, ticks);
    }

    protected BukkitTask syncRepeat(long delayTicks, long periodTicks, Runnable r) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, r, delayTicks, periodTicks);
    }

    protected BukkitTask async(Runnable r) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, r);
    }

    protected BukkitTask asyncRepeat(long delayTicks, long periodTicks, Runnable r) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, r, delayTicks, periodTicks);
    }

    protected void onDisableHook(Runnable r) {
        disableHooks.add(r);
    }

    protected void ensurePermission(String node, String description, PermissionDefault def) {
        if (plugin.getServer().getPluginManager().getPermission(node) == null) {
            plugin.getServer().getPluginManager().addPermission(
                    new org.bukkit.permissions.Permission(node, description, def));
        }
    }

    /* ------------------------------------------------------------------ */
    /* messaging                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Resolves a key from this module's language file
     * ({@code lang/<locale>/<categoria>/<module>.yml}). Falls back to
     * {@code common.yml} and finally to the packaged English default.
     */
    protected String msg(String key, String... kv) {
        return plugin.getLang().get(categoria + "." + name + "." + key, kv);
    }

    protected net.kyori.adventure.text.Component msgComponent(String key, String... kv) {
        return Text.parse(msg(key, kv));
    }

    /** Generic message from {@code common.yml} (prefix, no-permission, ...). */
    protected String common(String key, String... kv) {
        return plugin.getLang().get(key, kv);
    }

    protected void send(org.bukkit.command.CommandSender to, String key, String... kv) {
        if (to != null) {
            to.sendMessage(msg(key, kv));
        }
    }

    /** Sends a common.yml message. */
    protected void sendCommon(org.bukkit.command.CommandSender to, String key, String... kv) {
        if (to != null) {
            to.sendMessage(common(key, kv));
        }
    }

    protected String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }
}
