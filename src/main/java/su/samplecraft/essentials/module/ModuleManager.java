package su.samplecraft.essentials.module;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import su.samplecraft.essentials.SamplesEssentials;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry and lifecycle engine for all modules.
 * <p>
 * Registration order defines the order in the main config and the order in
 * which modules are enabled, so infrastructure-ish modules (vault bridge,
 * chat formatter) come first.
 */
public final class ModuleManager {

    private final SamplesEssentials plugin;
    private final Map<String, EssentialsModule> modules = new LinkedHashMap<>();

    public ModuleManager(SamplesEssentials plugin) {
        this.plugin = plugin;
    }

    public void register(EssentialsModule module) {
        modules.put(module.getKey().toLowerCase(Locale.ROOT), module);
    }

    /** Creates the folder layout and enables everything the main config allows. */
    public void enableConfiguredModules() {
        for (EssentialsModule module : modules.values()) {
            module.getModuleFolder();
        }
        for (EssentialsModule module : modules.values()) {
            boolean toggle = plugin.getMainConfig().isModuleEnabled(module.getCategoria(), module.getName());
            if (toggle) {
                module.enable();
                plugin.getLogger().info("  [ON ] " + module.getKey() + "  <- " + module.getLegacySource());
            } else {
                plugin.getLogger().info("  [OFF] " + module.getKey() + "  (disabled in config.yml)");
            }
        }
    }

    public void disableAll() {
        List<EssentialsModule> reversed = new ArrayList<>(modules.values());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            EssentialsModule module = reversed.get(i);
            if (module.isEnabled()) {
                module.disable();
            }
        }
    }

    public void reloadAll() {
        disableAll();
        plugin.getMainConfig().load();
        plugin.getLang().load();
        enableConfiguredModules();
    }

    public int getActiveCount() {
        int count = 0;
        for (EssentialsModule module : modules.values()) {
            if (module.isEnabled()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return modules.size();
    }

    public Map<String, EssentialsModule> all() {
        return modules;
    }

    public List<String> categorias() {
        List<String> out = new ArrayList<>();
        for (EssentialsModule module : modules.values()) {
            if (!out.contains(module.getCategoria())) {
                out.add(module.getCategoria());
            }
        }
        return out;
    }

    public List<EssentialsModule> modulesIn(String categoria) {
        List<EssentialsModule> out = new ArrayList<>();
        for (EssentialsModule module : modules.values()) {
            if (module.getCategoria().equalsIgnoreCase(categoria)) {
                out.add(module);
            }
        }
        return out;
    }

    /** Exact key match ("rtp/ezrtp") or bare module name ("ezrtp"). */
    public EssentialsModule find(String query) {
        if (query == null) {
            return null;
        }
        EssentialsModule direct = modules.get(query.toLowerCase(Locale.ROOT));
        if (direct != null) {
            return direct;
        }
        for (EssentialsModule module : modules.values()) {
            if (module.getName().equalsIgnoreCase(query)) {
                return module;
            }
        }
        return null;
    }

    /** /samplese - status, reload, toggle, modules, version. */
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("samplese.admin")) {
            sender.sendMessage(plugin.getLang().component("no-permission"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage(plugin.getLang().component("status-header",
                    "active", String.valueOf(getActiveCount()),
                    "total", String.valueOf(getTotalCount()),
                    "locale", plugin.getLang().locale()));
            for (EssentialsModule module : modules.values()) {
                String state = module.isEnabled() ? "<green>[ON ]" : "<red>[OFF]";
                sender.sendMessage(su.samplecraft.essentials.util.Text.MINI.deserialize(
                        state + " <gray>" + module.getCategoria() + "<dark_gray>/<white>" + module.getName()
                                + "<dark_gray> - <gray>" + module.getDescription()));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("modules")) {
            for (String categoria : categorias()) {
                sender.sendMessage(plugin.getLang().component("modules-header", "categoria", categoria));
                for (EssentialsModule module : modulesIn(categoria)) {
                    String state = module.isEnabled() ? "<green>ON " : "<red>OFF";
                    sender.sendMessage(su.samplecraft.essentials.util.Text.MINI.deserialize(
                            "  " + state + " <white>" + module.getName() + "<dark_gray> ("
                                    + module.getLegacySource() + ")"));
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            reloadAll();
            sender.sendMessage(plugin.getLang().component("reloaded"));
            return true;
        }
        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage(plugin.getLang().component("version",
                    "version", plugin.getPluginMeta().getVersion()));
            return true;
        }
        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getLang().component("usage"));
                return true;
            }
            EssentialsModule module = find(args[1]);
            if (module == null) {
                sender.sendMessage(plugin.getLang().component("unknown-module", "module", args[1]));
                return true;
            }
            boolean newState = !module.isEnabled();
            plugin.getMainConfig().setModuleEnabled(module.getCategoria(), module.getName(), newState);
            if (newState) {
                module.enable();
            } else {
                module.disable();
            }
            sender.sendMessage(plugin.getLang().component("module-toggled",
                    "module", module.getKey(),
                    "state", newState ? "enabled" : "disabled"));
            return true;
        }
        sender.sendMessage(plugin.getLang().component("usage"));
        return true;
    }
}
