package su.samplecraft.essentials.modules.general;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Merged from {@code LocalInventory-1.0.jar} (com.cyux.localinv.LocalInventory).
 * <p>
 * Exact behaviour of the original three commands:
 * <ul>
 *   <li>{@code /sli} — saves the current inventory (main, armour, off hand) and clears it;
 *       when a save already exists the original asks "Overwrite current save?" with a
 *       clickable {@code [YES]} that runs {@code /sli confirm},</li>
 *   <li>{@code /lli} — loads the saved inventory back and deletes the save,</li>
 *   <li>{@code /ali <player>} — staff open another player's save (read/write view).</li>
 * </ul>
 * Saves persist in {@code general/localinventory/data/saves.yml} under the player UUID
 * (the original used the plugin config itself as the store).
 */
public final class LocalInventoryModule extends EssentialsModule {

    private ModuleStorage storage;

    public LocalInventoryModule(SamplesEssentials plugin) {
        super("general", "localinventory", "LocalInventory-1.0.jar", "Save, clear and restore your inventory (/sli, /lli, /ali)");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.localinv", "Use your own inventory saves", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.localinv.admin", "Open other players' saves", org.bukkit.permissions.PermissionDefault.OP);
        storage = new ModuleStorage(plugin, getDataFolder(), "saves.yml");
        command("sli", new SaveCommand());
        command("lli", new LoadCommand());
        command("ali", new AdminCommand());
        onDisableHook(() -> {
            unbindCommands();
            storage.save();
        });
    }

    /* ------------------------------------------------------------------ */
    /* storage helpers                                                    */
    /* ------------------------------------------------------------------ */

    private void store(Player player, UUID owner, String world, String type, ItemStack[] contents) {
        String base = owner + "." + type;
        List<String> encoded = new java.util.ArrayList<>();
        Base64.Encoder encoder = Base64.getEncoder();
        for (ItemStack item : contents) {
            if (item == null || item.getType().isAir()) {
                encoded.add("");
            } else {
                encoded.add(encoder.encodeToString(item.serializeAsBytes()));
            }
        }
        storage.yaml().set(base, encoded);
        storage.yaml().set(owner + ".world", world);
        storage.yaml().set(owner + ".saved-by", player.getName());
        storage.yaml().set(owner + ".timestamp", System.currentTimeMillis());
        storage.save();
    }

    private ItemStack[] read(UUID owner, String type, int size) {
        List<String> encoded = storage.yaml().getStringList(owner + "." + type);
        ItemStack[] contents = new ItemStack[size];
        Base64.Decoder decoder = Base64.getDecoder();
        for (int i = 0; i < Math.min(size, encoded.size()); i++) {
            String raw = encoded.get(i);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            try {
                contents[i] = ItemStack.deserializeBytes(decoder.decode(raw));
            } catch (Exception ignored) {
                // skip broken entry
            }
        }
        return contents;
    }

    private boolean hasSave(UUID owner) {
        return storage.yaml().isConfigurationSection(owner.toString())
                || storage.yaml().contains(owner + ".main");
    }

    private void clearSave(UUID owner) {
        storage.yaml().set(owner.toString(), null);
        storage.save();
    }

    /* ------------------------------------------------------------------ */
    /* /sli                                                               */
    /* ------------------------------------------------------------------ */

    private final class SaveCommand implements org.bukkit.command.CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
                saveAndClear(player);
                return true;
            }
            if (hasSave(player.getUniqueId())) {
                player.sendMessage(msgComponent("overwrite-question"));
                player.sendMessage(Text.parse("<green><bold>[YES] </bold></green><green>")
                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/sli confirm")));
            } else {
                saveAndClear(player);
            }
            return true;
        }
    }

    private void saveAndClear(Player player) {
        if (!player.hasPermission("samplese.localinv")) {
            player.sendMessage(plugin.getLang().component("no-permission"));
            return;
        }
        store(player, player.getUniqueId(), player.getWorld().getName(), "main",
                player.getInventory().getContents());
        store(player, player.getUniqueId(), player.getWorld().getName(), "armor",
                player.getInventory().getArmorContents());
        store(player, player.getUniqueId(), player.getWorld().getName(), "extra",
                player.getInventory().getExtraContents());
        player.getInventory().clear();
        player.sendMessage(msgComponent("saved"));
    }

    /* ------------------------------------------------------------------ */
    /* /lli                                                               */
    /* ------------------------------------------------------------------ */

    private final class LoadCommand implements org.bukkit.command.CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!hasSave(player.getUniqueId())) {
                player.sendMessage(msgComponent("no-save"));
                return true;
            }
            try {
                player.getInventory().setContents(read(player.getUniqueId(), "main", 36));
                player.getInventory().setArmorContents(read(player.getUniqueId(), "armor", 4));
                player.getInventory().setExtraContents(read(player.getUniqueId(), "extra", 1));
                clearSave(player.getUniqueId());
                player.sendMessage(msgComponent("loaded"));
            } catch (Exception e) {
                player.sendMessage(msgComponent("load-error"));
            }
            return true;
        }
    }

    /* ------------------------------------------------------------------ */
    /* /ali                                                               */
    /* ------------------------------------------------------------------ */

    private final class AdminCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.localinv.admin")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(msgComponent("admin-usage"));
                return true;
            }
            OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(args[0]);
            if (target == null) {
                target = Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + args[0]).getBytes()));
            }
            if (!hasSave(target.getUniqueId())) {
                player.sendMessage(msgComponent("target-no-save", "player", args[0]));
                return true;
            }
            Inventory inventory = Bukkit.createInventory(null, 54,
                    Text.parse(cfgString("admin-title", "&8Saved inventory: %player%")
                            .replace("%player%", args[0])));
            ItemStack[] main = read(target.getUniqueId(),
                    storage.yaml().contains(target.getUniqueId() + ".main-admin") ? "main-admin" : "main", 36);
            ItemStack[] armor = read(target.getUniqueId(),
                    storage.yaml().contains(target.getUniqueId() + ".armor-admin") ? "armor-admin" : "armor", 4);
            ItemStack[] extra = read(target.getUniqueId(), "extra", 1);
            for (int i = 0; i < main.length; i++) {
                inventory.setItem(i, main[i]);
            }
            for (int i = 0; i < armor.length; i++) {
                inventory.setItem(36 + i, armor[i]);
            }
            inventory.setItem(45, extra.length > 0 ? extra[0] : null);
            openAdminSaves.put(player.getUniqueId(), target.getUniqueId());
            player.openInventory(inventory);
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                String prefix = args[0].toLowerCase(java.util.Locale.ROOT);
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(prefix))
                        .toList();
            }
            return List.of();
        }
    }

    private final java.util.Map<UUID, UUID> openAdminSaves = new java.util.concurrent.ConcurrentHashMap<>();

    @org.bukkit.event.EventHandler
    public void onAdminClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }
        UUID targetId = openAdminSaves.remove(viewer.getUniqueId());
        if (targetId == null || !viewer.hasPermission("samplese.localinv.admin")) {
            return;
        }
        ItemStack[] contents = event.getInventory().getContents();
        ItemStack[] main = new ItemStack[36];
        ItemStack[] armor = new ItemStack[4];
        System.arraycopy(contents, 0, main, 0, Math.min(36, contents.length));
        System.arraycopy(contents, 36, armor, 0, Math.min(4, contents.length - 36));
        store(viewer, targetId, "admin", "main-admin", main);
        store(viewer, targetId, "admin", "armor-admin", armor);
        viewer.sendMessage(msgComponent("admin-saved"));
    }
}
