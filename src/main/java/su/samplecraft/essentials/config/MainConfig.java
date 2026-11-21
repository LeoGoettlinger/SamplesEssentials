package su.samplecraft.essentials.config;

import org.bukkit.configuration.file.YamlConfiguration;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;

import java.io.File;
import java.util.Locale;

/**
 * Main configuration, {@code plugins/SamplesEssentials/config.yml}.
 * <p>
 * Structure:
 * <pre>
 * settings:
 *   language: en
 * modules:
 *   chat:
 *     chatclear: true
 *     chatformatter: true
 *   economy:
 *     ...
 * </pre>
 * The {@code modules} section is generated from the module registry, so every
 * former plugin always has exactly one toggle - grouped in its categoria.
 */
public final class MainConfig {

    private final SamplesEssentials plugin;
    private YamlConfiguration cfg = new YamlConfiguration();

    public MainConfig(SamplesEssentials plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        boolean fresh = !file.isFile();
        cfg = YamlConfiguration.loadConfiguration(file);

        if (fresh) {
            cfg.options().setHeader(java.util.List.of(
                    "SamplesEssentials " + plugin.getPluginMeta().getVersion() + " by SampleCraft",
                    "Paper 26.2-26.3 all-in-one suite - every legacy plugin is now a module.",
                    "",
                    "settings.language  : file name (without .yml) inside plugins/SamplesEssentials/lang/",
                    "modules.<categoria>.<module> : true/false toggle for each module.",
                    "Per-module settings: plugins/SamplesEssentials/<categoria>/<module>/config.yml",
                    "Runtime control    : /samplese status | /samplese toggle <categoria/module> | /samplese reload"));
        }

        if (!cfg.isString("settings.language")) {
            cfg.set("settings.language", "en");
        }
        if (!cfg.isBoolean("settings.console-module-log")) {
            cfg.set("settings.console-module-log", true);
        }

        boolean changed = fresh;
        for (EssentialsModule module : plugin.getModules().all().values()) {
            String path = "modules." + module.getCategoria() + "." + module.getName();
            if (!cfg.isBoolean(path)) {
                cfg.set(path, true);
                changed = true;
            }
        }
        if (changed || !file.isFile()) {
            save();
        }
    }

    public boolean isModuleEnabled(String categoria, String name) {
        return cfg.getBoolean("modules." + categoria + "." + name, true);
    }

    public void setModuleEnabled(String categoria, String name, boolean state) {
        cfg.set("modules." + categoria + "." + name, state);
        save();
    }

    public String language() {
        return cfg.getString("settings.language", "en").toLowerCase(Locale.ROOT);
    }

    public boolean consoleModuleLog() {
        return cfg.getBoolean("settings.console-module-log", true);
    }

    private void save() {
        try {
            File file = new File(plugin.getDataFolder(), "config.yml");
            cfg.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }
}
