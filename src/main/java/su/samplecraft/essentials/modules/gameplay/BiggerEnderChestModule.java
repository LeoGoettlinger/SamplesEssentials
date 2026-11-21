package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code BiggerEnderChest1.2.1.jar} (com.Sw3ys.bigenderchest).
 * <p>
 * Replaces ender chests with per-player inventories of a configurable size
 * (27 by default, upgradeable to 54/63 via the {@code enderchest.slot.<size>}
 * permission slot system of the original plugin). Contents are stored in
 * {@code gameplay/biggerenderchest/data/enderchests.yml} as base64 serialised
 * ItemStacks, which survives version upgrades cleanly.
 * <p>
 * Commands: {@code /biggerenderchest} (own chest), {@code /biggerenderchest see <player>}.
 */
public final class BiggerEnderChestModule extends EssentialsModule implements Listener {

    private final Map<UUID, Inventory> open = new HashMap<>();
    private ModuleStorage storage;

    public BiggerEnderChestModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "biggerenderchest", "BiggerEnderChest1.2.1.jar",
                "Per-player bigger ender chests with slot upgrades and /ecsee");
    }

    @Override
    protected void onEnable() {
        storage = new ModuleStorage(plugin, getDataFolder(), "enderchests.yml");
        ensurePermission("samplese.biggerenderchest", "Open your bigger ender chest",
                org.bukkit.permissions.PermissionDefault.TRUE);
        command("biggerenderchest", (sender, command, label, args) -> {
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
                player.openInventory(inventoryFor(target));
                open.put(player.getUniqueId(), inventoryFor(target));
                trackOwner(player, target);
                return true;
            }
            if (!player.hasPermission("samplese.biggerenderchest")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (cfgStringList("disabled-worlds").stream().anyMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) {
                player.sendMessage(msgComponent("world-disabled"));
                return true;
            }
            Inventory inventory = inventoryFor(player);
            player.openInventory(inventory);
            open.put(player.getUniqueId(), inventory);
            return true;
        });
        onDisableHook(this::unbindCommands);
        if (cfgBool("features.enable-vanilla-hook", true)) {
            listen(this);
        }
    }

    private final Map<UUID, UUID> viewedOwner = new HashMap<>();

    private void trackOwner(Player viewer, Player owner) {
        viewedOwner.put(viewer.getUniqueId(), owner.getUniqueId());
    }

    /**
     * Size ladder straight from the original plugin: default 27, unlockable
     * 36/45/54 through the permission nodes listed under {@code slots:} in
     * config.yml (e.g. {@code enderchest.slot.54}).
     */
    private int slotSize(Player player) {
        int best = Math.max(9, cfgInt("enderchest.default-size", 27));
        ConfigurationSection slots = config().getConfigurationSection("slots");
        if (slots != null) {
            for (String key : slots.getKeys(false)) {
                String node = key.replace("enderchest.slot.", "");
                int size;
                try {
                    size = Integer.parseInt(node);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (size > best && size <= 54 && player.hasPermission(key)) {
                    best = size;
                }
            }
        }
        return Math.min(54, best - best % 9);
    }

    private Inventory inventoryFor(Player owner) {
        UUID id = owner.getUniqueId();
        Inventory cached = open.get(id);
        if (cached != null) {
            return cached;
        }
        int size = slotSize(owner);
        String title = cfgString("gui.enderchest-title", "&5Ender Chest");
        Inventory inventory = Bukkit.createInventory(null, size,
                Text.parse(title.replace("{player}", owner.getName())));
        ConfigurationSection section = storage.yaml().getConfigurationSection("chests." + id);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String data = section.getString(key);
                if (data == null) {
                    continue;
                }
                try {
                    int slot = Integer.parseInt(key);
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, ItemStack.deserializeBytes(java.util.Base64.getDecoder().decode(data)));
                    }
                } catch (Exception ignored) {
                    // corrupted entry, skip
                }
            }
        }
        open.put(id, inventory);
        return inventory;
    }

    private void persist(UUID owner, Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                storage.yaml().set("chests." + owner + "." + slot, null);
            } else {
                storage.yaml().set("chests." + owner + "." + slot,
                        java.util.Base64.getEncoder().encodeToString(item.serializeAsBytes()));
            }
        }
        storage.saveLater();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("samplese.biggerenderchest")) {
            return;
        }
        if (cfgStringList("disabled-worlds").stream().anyMatch(w -> w.equalsIgnoreCase(player.getWorld().getName()))) {
            return;
        }
        event.setCancelled(true);
        Inventory inventory = inventoryFor(player);
        player.openInventory(inventory);
        open.put(player.getUniqueId(), inventory);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        UUID viewer = player.getUniqueId();
        UUID owner = viewedOwner.remove(viewer);
        if (owner != null && !owner.equals(viewer)) {
            persist(owner, event.getInventory());
            return;
        }
        if (open.get(viewer) == event.getInventory()) {
            persist(viewer, event.getInventory());
            open.remove(viewer);
        }
    }

    @Override
    protected void onDisable() {
        open.forEach(this::persist);
        open.clear();
        storage.save();
    }
}
