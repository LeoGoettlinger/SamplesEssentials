package su.samplecraft.essentials.config;

import org.bukkit.configuration.file.YamlConfiguration;
import su.samplecraft.essentials.SamplesEssentials;

import java.io.File;
import java.io.IOException;

/**
 * Small helper for per-module YAML data files (player storages, caches, ...).
 * Data lives in {@code <categoria>/<module>/data/<name>.yml}.
 */
public final class ModuleStorage {

    private final SamplesEssentials plugin;
    private final File file;
    private YamlConfiguration cfg;

    public ModuleStorage(SamplesEssentials plugin, File folder, String fileName) {
        this.plugin = plugin;
        this.file = new File(folder, fileName);
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration yaml() {
        return cfg;
    }

    public File file() {
        return file;
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.isDirectory()) {
                parent.mkdirs();
            }
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }

    public void saveLater() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::save);
    }

    public void reload() {
        this.cfg = YamlConfiguration.loadConfiguration(file);
    }
}
