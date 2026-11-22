package su.samplecraft.essentials.modules.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Merged from {@code InventorySave-0.1.jar} (dev.huey.inventorysave).
 * <p>
 * Save inventories by id and load them onto players, plus list and delete.
 * Faithful to the original:
 * <ul>
 *   <li>the id must match {@code ^[a-zA-Z0-9_]+$},</li>
 *   <li>{@code /inventory save <id>} stores the sender's inventory,</li>
 *   <li>{@code /inventory load <id> [player]} supports entity selectors in the
 *       second argument and reports how many players were loaded,</li>
 *   <li>{@code /inventory list} and {@code /inventory delete <id>}.</li>
 * </ul>
 * Storage is one YAML file per id ({@code inventories/<id>.yml}) holding the full
 * player inventory, matching the original's per-file layout.
 */
public final class InventorySaveModule extends EssentialsModule {

    public InventorySaveModule(SamplesEssentials plugin) {
        super("general", "inventorysave", "InventorySave-0.1.jar",
                "Save and load player inventories by id");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.inventorysave", "Use /inventory", PermissionDefault.OP);
        ensurePermission("samplese.inventorysave.others", "Load inventories onto other players",
                PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        command("inventory", new InventoryCommand());
        onDisableHook(this::unbindCommands);
    }

    private File folder() {
        File folder = new File(getModuleFolder(), "inventories");
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    private File fileFor(String id) {
        return new File(folder(), id.toLowerCase(Locale.ROOT) + ".yml");
    }

    private boolean validId(String id) {
        return id.matches("^[a-zA-Z0-9_]+$");
    }

    private void saveInventory(String id, PlayerInventory inventory) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("contents", Arrays.asList(inventory.getContents()));
        yaml.set("armor", Arrays.asList(inventory.getArmorContents()));
        yaml.set("extra", Arrays.asList(inventory.getExtraContents()));
        yaml.set("held-slot", inventory.getHeldItemSlot());
        try {
            yaml.save(fileFor(id));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save inventory " + id + ": " + e.getMessage());
        }
    }

    private boolean loadInventory(String id, Player target) {
        File file = fileFor(id);
        if (!file.isFile()) {
            return false;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<ItemStack> contents = stackList(yaml.getList("contents"));
        target.getInventory().setContents(contents.toArray(new ItemStack[0]));
        List<ItemStack> armor = stackList(yaml.getList("armor"));
        target.getInventory().setArmorContents(armor.toArray(new ItemStack[0]));
        List<ItemStack> extra = stackList(yaml.getList("extra"));
        if (!extra.isEmpty()) {
            target.getInventory().setExtraContents(extra.toArray(new ItemStack[0]));
        }
        target.getInventory().setHeldItemSlot(
                Math.min(8, Math.max(0, yaml.getInt("held-slot", 0))));
        return true;
    }

    private List<ItemStack> stackList(List<?> raw) {
        List<ItemStack> items = new ArrayList<>();
        if (raw == null) {
            return items;
        }
        for (Object entry : raw) {
            items.add(entry instanceof ItemStack stack ? stack : null);
        }
        return items;
    }

    private final class InventoryCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.inventorysave")) {
                reply(sender, "no-permission");
                return true;
            }
            if (args.length == 0) {
                reply(sender, "usage");
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "save" -> save(sender, args);
                case "load" -> load(sender, args);
                case "delete", "remove" -> delete(sender, args);
                case "list" -> list(sender);
                default -> reply(sender, "usage");
            }
            return true;
        }

        private void save(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage");
                return;
            }
            if (!validId(args[1])) {
                reply(sender, "invalid-id");
                return;
            }
            saveInventory(args[1], player.getInventory());
            reply(sender, "saved", "id", args[1]);
        }

        private void load(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage");
                return;
            }
            if (!validId(args[1])) {
                reply(sender, "invalid-id");
                return;
            }
            List<Player> targets = new ArrayList<>();
            if (args.length >= 3) {
                if (!sender.hasPermission("samplese.inventorysave.others")) {
                    reply(sender, "no-permission");
                    return;
                }
                try {
                    for (org.bukkit.entity.Entity entity : org.bukkit.Bukkit.selectEntities(sender, args[2])) {
                        if (entity instanceof Player player) {
                            targets.add(player);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    reply(sender, "no-entity");
                    return;
                }
                if (targets.isEmpty()) {
                    reply(sender, "no-entity");
                    return;
                }
            } else if (sender instanceof Player player) {
                targets.add(player);
            } else {
                reply(sender, "player-only");
                return;
            }
            int count = 0;
            for (Player target : targets) {
                if (loadInventory(args[1], target)) {
                    count++;
                } else {
                    reply(sender, "not-found", "id", args[1]);
                    return;
                }
            }
            reply(sender, "loaded", "id", args[1], "count", String.valueOf(count));
        }

        private void delete(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage");
                return;
            }
            if (!validId(args[1])) {
                reply(sender, "invalid-id");
                return;
            }
            File file = fileFor(args[1]);
            if (!file.isFile() || !file.delete()) {
                reply(sender, "not-found", "id", args[1]);
                return;
            }
            reply(sender, "deleted", "id", args[1]);
        }

        private void list(CommandSender sender) {
            File[] files = folder().listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null || files.length == 0) {
                reply(sender, "list-empty");
                return;
            }
            List<String> ids = new ArrayList<>();
            for (File file : files) {
                ids.add(file.getName().replace(".yml", ""));
            }
            ids.sort(String::compareTo);
            reply(sender, "list-header", "count", String.valueOf(ids.size()));
            sender.sendMessage(Text.parse(msg("list-entry", "ids", String.join(", ", ids))));
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("save", "load", "delete", "list");
            }
            if (args.length == 2 && !args[0].equalsIgnoreCase("save")
                    && !args[0].equalsIgnoreCase("list")) {
                File[] files = folder().listFiles((dir, name) -> name.endsWith(".yml"));
                if (files == null) {
                    return List.of();
                }
                List<String> ids = new ArrayList<>();
                for (File file : files) {
                    ids.add(file.getName().replace(".yml", ""));
                }
                return ids;
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
