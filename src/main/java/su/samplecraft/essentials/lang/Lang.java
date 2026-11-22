package su.samplecraft.essentials.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Language engine.
 * <p>
 * Layout (one file per module, grouped by categoria, plus one shared file):
 * <pre>
 * plugins/SamplesEssentials/lang/common.yml          <- prefix + generic answers
 * plugins/SamplesEssentials/lang/chat/chatclear.yml
 * plugins/SamplesEssentials/lang/rtp/ezrtp.yml
 * plugins/SamplesEssentials/lang/&lt;locale&gt;/...        <- optional per-language folders
 * </pre>
 * The active locale comes from {@code settings.language}. A lookup order is
 * used so nothing ever renders as "missing": requested locale file, then the
 * English file, then the packaged default from the jar.
 */
public final class Lang {

    private static final String DEFAULT_LOCALE = "en";

    private final SamplesEssentials plugin;
    private File root;
    private String locale = DEFAULT_LOCALE;

    /** categoria/module -> file, for the active locale */
    private final Map<String, YamlConfiguration> moduleFiles = new HashMap<>();
    /** categoria/module -> packaged fallback */
    private final Map<String, YamlConfiguration> moduleFallbacks = new HashMap<>();
    private YamlConfiguration common = new YamlConfiguration();
    private YamlConfiguration commonFallback = new YamlConfiguration();

    public Lang(SamplesEssentials plugin) {
        this.plugin = plugin;
    }

    public void load() {
        root = new File(plugin.getDataFolder(), "lang");
        String configured = plugin.getMainConfig() != null ? plugin.getMainConfig().language() : DEFAULT_LOCALE;
        locale = configured == null || configured.isBlank() ? DEFAULT_LOCALE
                : configured.toLowerCase(Locale.ROOT).trim();

        ensureDir(root);
        moduleFiles.clear();
        moduleFallbacks.clear();

        common = loadFile(locale, "common.yml");
        commonFallback = loadBundled("common.yml");
        if (common == null) {
            common = new YamlConfiguration();
        }

        for (EssentialsModule module : plugin.getModules().all().values()) {
            String relative = module.getCategoria() + "/" + module.getName() + ".yml";
            YamlConfiguration file = loadFile(locale, relative);
            if (file == null) {
                file = new YamlConfiguration();
            }
            moduleFiles.put(module.getKey(), file);
            moduleFallbacks.put(module.getKey(), loadBundled(relative));
        }
        migrateFlatFiles();
        plugin.getLogger().info("Language loaded: " + locale + " (" + moduleFiles.size() + " module files + common.yml)");
    }

    /** Older builds wrote lang/<locale>.yml - keep those users working. */
    private void migrateFlatFiles() {
        migrateLocaleLessModuleFiles();
        File legacy = new File(root, locale + ".yml");
        if (!legacy.isFile()) {
            return;
        }
        YamlConfiguration old = YamlConfiguration.loadConfiguration(legacy);
        if (!old.getKeys(false).isEmpty()) {
            for (String key : old.getKeys(false)) {
                if (!common.contains(key)) {
                    common.set(key, old.get(key));
                }
            }
            plugin.getLogger().info("Imported legacy lang/" + locale + ".yml keys into lang/common.yml");
        }
        File renamed = new File(root, locale + ".yml.imported");
        //noinspection ResultOfMethodCallIgnored
        legacy.renameTo(renamed);
    }

    /**
     * Older builds wrote module files without a locale folder
     * ({@code lang/<categoria>/<module>.yml}). Move them into {@code lang/en/...}
     * so upgraded servers keep their edits.
     */
    private void migrateLocaleLessModuleFiles() {
        for (EssentialsModule module : plugin.getModules().all().values()) {
            File legacy = new File(root, module.getCategoria() + "/" + module.getName() + ".yml");
            if (!legacy.isFile()) {
                continue;
            }
            File target = new File(new File(root, DEFAULT_LOCALE),
                    module.getCategoria() + "/" + module.getName() + ".yml");
            if (target.isFile()) {
                continue;
            }
            ensureDir(target.getParentFile());
            if (legacy.renameTo(target)) {
                plugin.getLogger().info("Moved legacy lang file " + legacy.getName()
                        + " into lang/" + DEFAULT_LOCALE + "/" + module.getCategoria() + "/");
            }
        }
    }

    private static void ensureDir(File dir) {
        if (dir != null && !dir.isDirectory() && !dir.mkdirs()) {
            // best effort - the caller logs the concrete failure
        }
    }

    /**
     * Loads {@code lang/<locale>/<relative>}, seeding a missing file with the
     * packaged English default. Lookup order ({@link #lookup(String)}) is the
     * active locale file, then the jar's English copy, so a locale folder only
     * needs the keys it actually translates.
     */
    private YamlConfiguration loadFile(String locale, String relative) {
        File file = new File(new File(root, locale), relative);
        if (file.isFile()) {
            return YamlConfiguration.loadConfiguration(file);
        }
        try (InputStream in = plugin.getResource("lang/" + DEFAULT_LOCALE + "/" + relative)) {
            if (in == null) {
                return null;
            }
            ensureDir(file.getParentFile());
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            plugin.getLogger().warning("Could not extract lang/" + locale + "/" + relative + ": " + e.getMessage());
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private YamlConfiguration loadBundled(String relative) {
        try (InputStream in = plugin.getResource("lang/" + DEFAULT_LOCALE + "/" + relative)) {
            if (in == null) {
                return new YamlConfiguration();
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new YamlConfiguration();
        }
    }

    /** Catalogs the languages available in the folder (folders + legacy flat files). */
    public List<String> installedLocales() {
        List<String> out = new ArrayList<>();
        File[] entries = root == null ? null : root.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isDirectory() && !entry.getName().equals(DEFAULT_LOCALE)) {
                    out.add(entry.getName());
                } else if (entry.getName().endsWith(".yml") && entry.isFile()
                        && !entry.getName().equals("common.yml")) {
                    out.add(entry.getName().substring(0, entry.getName().length() - 4));
                }
            }
        }
        if (!out.contains(DEFAULT_LOCALE)) {
            out.add(0, DEFAULT_LOCALE);
        }
        return out;
    }

    public String locale() {
        return locale;
    }

    /* ------------------------------------------------------------------ */
    /* lookups                                                             */
    /* ------------------------------------------------------------------ */

    private String lookup(String key) {
        // common.yml first for top-level keys, then categoria/module files
        String value = common.getString(key);
        if (value != null) {
            return value;
        }
        int slash = key.indexOf('.');
        if (slash > 0) {
            String categoria = key.substring(0, slash);
            String rest = key.substring(slash + 1);
            int slash2 = rest.indexOf('.');
            if (slash2 > 0) {
                String module = rest.substring(0, slash2);
                String moduleKey = categoria + "/" + module;
                String inner = rest.substring(slash2 + 1);
                YamlConfiguration file = moduleFiles.get(moduleKey);
                if (file != null && file.getString(inner) != null) {
                    return file.getString(inner);
                }
                YamlConfiguration fallback = moduleFallbacks.get(moduleKey);
                if (fallback != null && fallback.getString(inner) != null) {
                    return fallback.getString(inner);
                }
            }
        }
        // packaged English fallback for generic keys
        if (commonFallback.getString(key) != null) {
            return commonFallback.getString(key);
        }
        return "<missing:" + key + ">";
    }

    /** Resolved text with {prefix} expanded. */
    public String get(String key, String... kv) {
        String value = lookup(key);
        if (!key.equals("common.prefix")) {
            value = value.replace("{prefix}", prefix());
        }
        return Text.applyPairs(value, kv);
    }

    public String raw(String key) {
        return lookup(key);
    }

    public String prefix() {
        String prefix = common.getString("prefix");
        if (prefix == null) {
            prefix = commonFallback.getString("prefix", "<gradient:#24d1ff:#7d5cff><bold>Samples</bold></gradient><dark_gray>» <gray>");
        }
        return prefix;
    }

    public net.kyori.adventure.text.Component component(String key, String... kv) {
        return Text.parse(get(key, kv));
    }

    /** Convenience alias used by the module base class. */
    public net.kyori.adventure.text.Component render(String key, String... kv) {
        return component(key, kv);
    }

    public List<String> list(String key) {
        List<String> values = common.getStringList(key);
        if (values.isEmpty()) {
            values = commonFallback.getStringList(key);
        }
        return values;
    }
}
