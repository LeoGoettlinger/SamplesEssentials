package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code BiggerEnderChest1.2.1.jar} (com.Sw3ys.bigenderchest).
 * <p>
 * Faithful port of the original manager/listener/command trio:
 * <ul>
 *   <li>right-clicking a vanilla ender chest opens the per-player big chest
 *       (sneak-click stays vanilla, disabled worlds are respected);</li>
 *   <li>slots 0-26 mirror the vanilla ender chest, 27+ are extra space that is
 *       stored per player, so nothing can be duplicated;</li>
 *   <li>size ladder from the original: 27, then {@code enderchest.slot.36},
 *       {@code .45}, {@code .54} permissions;</li>
 *   <li>{@code /ec} opens your own chest, {@code /ec see} and {@code /ecsee
 *       <player>} open the selector GUI / another player's chest.</li>
 * </ul>
 */
public final class BiggerEnderChestModule extends EssentialsModule implements Listener {

    private static final int VANILLA_SIZE = 27;

    private final Map<UUID, UUID> viewing = new HashMap<>();
    private final Map<UUID, Inventory> openInventories = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    /** players currently looking at the selector GUI -> page */
    private final Map<UUID, Integer> selectorPages = new HashMap<>();
    /** players currently looking at the selector GUI -> the listed offline player ids */
    private final Map<UUID, List<UUID>> selectorLists = new HashMap<>();

    private ModuleStorage storage;

    public BiggerEnderChestModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "biggerenderchest", "BiggerEnderChest1.2.1.jar",
                "Per-player bigger ender chests with /ec, /ecsee and selector GUI");
    }

    @Override
    protected void onEnable() {
        storage = new ModuleStorage(plugin, getDataFolder(), "enderchests.yml");
        ensurePermission("samplese.biggerenderchest", "Open your bigger ender chest",
                org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("enderchest.slot.36", "36 slot ender chest", org.bukkit.permissions.PermissionDefault.FALSE);
        ensurePermission("enderchest.slot.45", "45 slot ender chest", org.bukkit.permissions.PermissionDefault.FALSE);
        ensurePermission("enderchest.slot.54", "54 slot ender chest", org.bukkit.permissions.PermissionDefault.FALSE);
        ensurePermission("enderchest.admin", "Bigger ender chest admin", org.bukkit.permissions.PermissionDefault.OP);

        command("biggerenderchest", new ECCommand());
        command("ec", new ECCommand());
        command("ecsee", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.biggerenderchest")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                openSelector(player, 0);
                return true;
            }
            Player target = plugin.getServer().getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getLang().component("unknown-player", "player", args[0]));
                return true;
            }
            open(player, target.getUniqueId());
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            for (UUID target : new ArrayList<>(openInventories.keySet())) {
                writeInventory(target, openInventories.get(target));
            }
            openInventories.clear();
            viewing.clear();
            storage.save();
        });
        listen(this);
    }

    private final class ECCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                                 String label, String[] args) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("enderchest.admin")) {
                    sender.sendMessage(plugin.getLang().component("no-permission"));
                    return true;
                }
                reloadConfig();
                sender.sendMessage(plugin.getLang().component("reloaded"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length > 0 && (args[0].equalsIgnoreCase("see") || args[0].equalsIgnoreCase("open"))) {
                if (!player.hasPermission("samplese.biggerenderchest.see")) {
                    player.sendMessage(plugin.getLang().component("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(msgComponent("search-prompt"));
                    return true;
                }
                Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage(plugin.getLang().component("unknown-player", "player", args[1]));
                    return true;
                }
                open(player, target.getUniqueId());
                return true;
            }
            if (!player.hasPermission("samplese.biggerenderchest")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            long cooldownMillis = Math.max(0, cfgInt("cooldown-seconds", 0)) * 1000L;
            Long last = cooldowns.get(player.getUniqueId());
            if (cooldownMillis > 0 && last != null
                    && System.currentTimeMillis() - last < cooldownMillis
                    && !player.hasPermission("enderchest.admin")) {
                player.sendMessage(msgComponent("cooldown",
                        "seconds", String.valueOf((cooldownMillis - (System.currentTimeMillis() - last)) / 1000)));
                return true;
            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            open(player, player.getUniqueId());
            return true;
        }

        @Override
        public java.util.List<String> onTabComplete(org.bukkit.command.CommandSender sender,
                                                    org.bukkit.command.Command command,
                                                    String alias, String[] args) {
            if (args.length == 1) {
                return java.util.List.of("see", "reload");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("see")) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return java.util.List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* inventory management (original EnderChestManager)                   */
    /* ------------------------------------------------------------------ */

    private int sizeFor(Player player) {
        if (player.hasPermission("enderchest.slot.54")) {
            return 54;
        }
        if (player.hasPermission("enderchest.slot.45")) {
            return 45;
        }
        return player.hasPermission("enderchest.slot.36") ? 36 : VANILLA_SIZE;
    }

    private void open(Player viewer, UUID targetId) {
        Player targetOnline = plugin.getServer().getPlayer(targetId);
        int size = targetOnline != null ? sizeFor(targetOnline)
                : Math.max(VANILLA_SIZE, Math.min(54, storage.yaml().getInt("sizes." + targetId, VANILLA_SIZE)));
        Inventory inventory = openInventories.get(targetId);
        if (inventory == null || inventory.getSize() != size) {
            inventory = Bukkit.createInventory(null, size, Text.parse(cfgString("gui.enderchest-title", "&5Ender Chest")));
            fill(targetId, targetOnline, inventory);
            openInventories.put(targetId, inventory);
        }
        viewing.put(viewer.getUniqueId(), targetId);
        viewer.openInventory(inventory);
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.8f, 1.0f);
        syncLater(1L, viewer::updateInventory);
    }

    private void fill(UUID targetId, Player targetOnline, Inventory inventory) {
        ConfigurationSection section = storage.yaml().getConfigurationSection("chests." + targetId);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = null;
            if (targetOnline != null && slot < VANILLA_SIZE) {
                ItemStack[] vanilla = targetOnline.getEnderChest().getContents();
                item = slot < vanilla.length ? vanilla[slot] : null;
            } else if (section != null) {
                String data = section.getString(String.valueOf(slot));
                if (data != null) {
                    try {
                        item = ItemStack.deserializeBytes(java.util.Base64.getDecoder().decode(data));
                    } catch (Exception ignored) {
                        // corrupted entry
                    }
                }
            }
            inventory.setItem(slot, item);
        }
    }

    private void writeInventory(UUID targetId, Inventory inventory) {
        Player targetOnline = plugin.getServer().getPlayer(targetId);
        if (targetOnline != null && inventory.getSize() >= VANILLA_SIZE) {
            ItemStack[] vanilla = new ItemStack[VANILLA_SIZE];
            for (int i = 0; i < VANILLA_SIZE; i++) {
                vanilla[i] = inventory.getItem(i);
            }
            targetOnline.getEnderChest().setContents(vanilla);
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            boolean mirror = targetOnline != null && slot < VANILLA_SIZE;
            String path = "chests." + targetId + "." + slot;
            ItemStack item = inventory.getItem(slot);
            if (mirror || item == null || item.getType().isAir()) {
                storage.yaml().set(path, null);
            } else {
                storage.yaml().set(path, java.util.Base64.getEncoder().encodeToString(item.serializeAsBytes()));
            }
        }
        storage.yaml().set("sizes." + targetId, inventory.getSize());
        storage.saveLater();
    }

    /* ------------------------------------------------------------------ */
    /* selector GUI (original SelectorGUI)                                 */
    /* ------------------------------------------------------------------ */

    private void openSelector(Player player, int page) {
        int perPage = 45;
        List<org.bukkit.OfflinePlayer> players = new ArrayList<>(List.of(Bukkit.getOfflinePlayers()));
        if (cfgBool("selector.sort-alphabetically", true)) {
            players.sort(Comparator.comparing(org.bukkit.OfflinePlayer::getName,
                    Comparator.nullsLast(String::compareToIgnoreCase)));
        }
        Inventory inventory = Bukkit.createInventory(null, 54,
                Text.parse(cfgString("gui.selector-title", "&dSelect Player")));
        int start = page * perPage;
        int end = Math.min(start + perPage, players.size());
        int slot = 0;
        for (int i = start; i < end && slot < perPage; i++) {
            org.bukkit.OfflinePlayer target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta meta) {
                meta.setOwningPlayer(target);
                meta.displayName(Text.parse("<green>" + (target.getName() == null ? target.getUniqueId() : target.getName())));
                head.setItemMeta(meta);
            }
            inventory.setItem(slot++, head);
        }
        inventory.setItem(47, named(Material.ARROW, "&cPrevious Page"));
        inventory.setItem(49, named(Material.OAK_SIGN, "&aSearch Player"));
        inventory.setItem(51, named(Material.ARROW, "&aNext Page"));
        selectorPages.put(player.getUniqueId(), page);
        selectorLists.put(player.getUniqueId(), players.stream().map(org.bukkit.OfflinePlayer::getUniqueId).toList());
        player.openInventory(inventory);
    }

    private ItemStack named(Material material, String legacyName) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() != null) {
            var meta = item.getItemMeta();
            meta.displayName(Text.parse(legacyName));
            item.setItemMeta(meta);
        }
        return item;
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                           */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getClickedBlock() == null
                || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return; // sneak-click keeps vanilla behaviour, like the original
        }
        if (!cfgBool("features.enable-vanilla-hook", true)) {
            return;
        }
        if (!player.hasPermission("samplese.biggerenderchest")) {
            return;
        }
        if (!player.hasPermission("enderchest.world.bypass")
                && cfgStringList("disabled-worlds").contains(player.getWorld().getName())) {
            player.sendMessage(msgComponent("world-disabled"));
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        open(player, player.getUniqueId());
    }

    @EventHandler
    public void onSelectorClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)
                || !selectorPages.containsKey(player.getUniqueId())) {
            return;
        }
        if (event.getView().getTopInventory() != event.getClickedInventory()) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();
        int page = selectorPages.getOrDefault(player.getUniqueId(), 0);
        List<UUID> listed = selectorLists.getOrDefault(player.getUniqueId(), List.of());
        if (slot == 47 && page > 0) {
            openSelector(player, page - 1);
        } else if (slot == 51 && (page + 1) * 45 < listed.size()) {
            openSelector(player, page + 1);
        } else if (slot == 49) {
            player.closeInventory();
            player.sendMessage(msgComponent("search-prompt"));
        } else if (slot < 45) {
            int index = page * 45 + slot;
            if (index < listed.size()) {
                player.closeInventory();
                open(player, listed.get(index));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID viewer = player.getUniqueId();
        selectorPages.remove(viewer);
        selectorLists.remove(viewer);
        UUID target = viewing.remove(viewer);
        if (target == null) {
            return;
        }
        Inventory inventory = openInventories.get(target);
        if (inventory != null) {
            writeInventory(target, inventory);
            // keep the inventory open-able for the owner (cache stays warm)
            if (plugin.getServer().getPlayer(target) == null) {
                openInventories.remove(target);
            }
        }
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.8f, 1.0f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
