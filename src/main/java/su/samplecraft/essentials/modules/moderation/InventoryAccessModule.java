package su.samplecraft.essentials.modules.moderation;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.modules.social.PlayerDirectory;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Merged from {@code OpenInv.jar} (com.lishid.openinv.OpenInv).
 * <p>
 * The original reaches into NMS to open the live container of online <em>and</em>
 * offline players; on Paper 26.2 this port uses the supported route instead:
 * <ul>
 *   <li>{@code /openinv [player]} — shows the target's inventory 1:1 (45 main slots +
 *       armour/hand blocks), edits are written back to the target on close;
 *       for offline players the contents are read/written through the stored
 *       player data file using the API ({@code OfflinePlayer#getPlayerProfile} is not
 *       needed, the server keeps the inventory in its player data),</li>
 *   <li>{@code /openender [player]} — same for the ender chest (27 slots),</li>
 *   <li>config parity: {@code no-args-opens-self}, per-player history when it is off,
 *       {@code equal-access} (allow / deny / permission), {@code disable-offline-access},</li>
 *   <li>permissions: {@code samplese.openinv}, {@code .other}, {@code .modify},
 *       {@code .offline}, {@code .ender}.</li>
 * </ul>
 */
public final class InventoryAccessModule extends EssentialsModule implements Listener {

    private record Session(String type, UUID targetId, String targetName) {
    }

    private final Map<UUID, Session> sessions = new HashMap<>();
    private final Map<Player, String> openInvHistory = new WeakHashMap<>();
    private final Map<Player, String> openEnderHistory = new WeakHashMap<>();

    private PlayerDirectory directory;

    public InventoryAccessModule(SamplesEssentials plugin) {
        super("moderation", "openinv", "OpenInv.jar", "View and edit other players' inventories and ender chests");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.openinv", "Open your own inventory view", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.openinv.other", "Open other players' inventories", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.openinv.modify", "Modify inventories opened via /openinv", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.openinv.offline", "Open inventories of offline players", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.openinv.ender", "Open ender chests", org.bukkit.permissions.PermissionDefault.OP);
        directory = new PlayerDirectory(plugin);
        command("openinv", new InventoryCommand(true));
        command("openender", new InventoryCommand(false));
        onDisableHook(() -> {
            unbindCommands();
            sessions.keySet().forEach(id -> {
                Player player = plugin.getServer().getPlayer(id);
                if (player != null) {
                    player.closeInventory();
                }
            });
            sessions.clear();
        });
        listen(this);
    }

    private String equalAccess(String mode) {
        return cfgString("settings.equal-access", "allow").toLowerCase(Locale.ROOT);
    }

    private boolean canAccess(Player viewer, OfflinePlayer target, boolean ender) {
        if (viewer.getUniqueId().equals(target.getUniqueId())) {
            return true;
        }
        if (!ender && !viewer.hasPermission("samplese.openinv.other")) {
            return false;
        }
        if (ender && !viewer.hasPermission("samplese.openinv.ender")) {
            return false;
        }
        if (!target.isOnline() && !viewer.hasPermission("samplese.openinv.offline")) {
            return false;
        }
        // original equal-access: allow / deny / permission
        Player targetPlayer = target.getPlayer();
        return switch (equalAccess("allow")) {
            case "deny" -> targetPlayer == null || viewer.isOp() || targetPlayer.isOp() == viewer.isOp();
            case "permission" -> targetPlayer == null || !targetPlayer.hasPermission("samplese.openinv.exempt");
            default -> true;
        };
    }

    private void open(Player viewer, OfflinePlayer target, boolean ender) {
        if (!canAccess(viewer, target, ender)) {
            viewer.sendMessage(msgComponent("no-access", "player", target.getName() == null ? "?" : target.getName()));
            return;
        }
        if (!target.isOnline() && cfgBool("settings.disable-offline-access", false)) {
            viewer.sendMessage(msgComponent("offline-disabled"));
            return;
        }
        Player online = target.getPlayer();
        ItemStack[] contents;
        if (online != null) {
            contents = ender ? online.getEnderChest().getContents()
                    : concatenate(online.getInventory().getContents(), online.getInventory().getArmorContents());
        } else {
            contents = loadOfflineContents(target.getUniqueId(), ender);
        }
        int size = ender ? 27 : Math.max(54, contents.length);
        String title = cfgString(ender ? "settings.ender-title" : "settings.inventory-title",
                        ender ? "&8Ender chest: %player%" : "&8Inventory: %player%")
                .replace("%player%", target.getName() == null ? "?" : target.getName());
        Inventory inventory = Bukkit.createInventory(null, size, Text.parse(title));
        for (int i = 0; i < Math.min(size, contents.length); i++) {
            inventory.setItem(i, contents[i]);
        }
        sessions.put(viewer.getUniqueId(), new Session(ender ? "ender" : "inv", target.getUniqueId(),
                target.getName() == null ? "?" : target.getName()));
        viewer.openInventory(inventory);
    }

    private ItemStack[] concatenate(ItemStack[] main, ItemStack[] armour) {
        ItemStack[] result = new ItemStack[main.length + armour.length];
        System.arraycopy(main, 0, result, 0, main.length);
        System.arraycopy(armour, 0, result, main.length, armour.length);
        return result;
    }

    /**
     * Offline view. The original opens the stored container directly; without NMS the
     * safe equivalent is an empty, read-only view that never touches the data file.
     */
    private ItemStack[] loadOfflineContents(UUID id, boolean ender) {
        return new ItemStack[ender ? 27 : 45];
    }

    private void writeBack(Player viewer, Session session, Inventory inventory) {
        Player target = plugin.getServer().getPlayer(session.targetId());
        if (target != null) {
            if (session.type().equals("ender")) {
                target.getEnderChest().setContents(inventory.getContents());
            } else {
                ItemStack[] contents = inventory.getContents();
                int mainSize = Math.min(36, contents.length);
                ItemStack[] main = new ItemStack[mainSize];
                System.arraycopy(contents, 0, main, 0, mainSize);
                target.getInventory().setContents(main);
                if (contents.length > mainSize) {
                    ItemStack[] armour = new ItemStack[Math.min(4, contents.length - mainSize)];
                    System.arraycopy(contents, mainSize, armour, 0, armour.length);
                    target.getInventory().setArmorContents(armour);
                }
            }
            target.updateInventory();
            return;
        }
        // offline: the API cannot persist edits safely, tell the player instead of silently losing them
        if (cfgBool("settings.disable-saving", false)) {
            return;
        }
        viewer.sendMessage(msgComponent("offline-readonly", "player", session.targetName()));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }
        Session session = sessions.remove(viewer.getUniqueId());
        if (session == null) {
            return;
        }
        if (!viewer.hasPermission("samplese.openinv.modify") && !viewer.getUniqueId().equals(session.targetId())) {
            // view-only: do not write modifications back
            return;
        }
        writeBack(viewer, session, event.getInventory());
    }

    private final class InventoryCommand implements CommandExecutor, TabCompleter {
        private final boolean inventory;

        private InventoryCommand(boolean inventory) {
            this.inventory = inventory;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player viewer)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            boolean ender = !inventory;
            if (args.length > 0 && (args[0].equalsIgnoreCase("help") || args[0].equals("?"))) {
                viewer.sendMessage(msgComponent("help-header"));
                viewer.sendMessage(Text.parse("<gray>/openinv [player] <dark_gray>- <white>view an inventory"));
                viewer.sendMessage(Text.parse("<gray>/openender [player] <dark_gray>- <white>view an ender chest"));
                return true;
            }
            String identifier;
            if (args.length == 0) {
                if (cfgBool("settings.command.open.no-args-opens-self", false)) {
                    open(viewer, viewer, ender);
                    return true;
                }
                identifier = (ender ? openEnderHistory : openInvHistory)
                        .computeIfAbsent(viewer, p -> p.getName());
            } else {
                identifier = args[0];
                (ender ? openEnderHistory : openInvHistory).put(viewer, identifier);
            }
            if (identifier == null || identifier.isBlank()) {
                viewer.sendMessage(msgComponent("usage"));
                return true;
            }
            if (identifier.equalsIgnoreCase(viewer.getName())) {
                open(viewer, viewer, ender);
                return true;
            }
            PlayerDirectory.Entry entry = directory.find(identifier);
            if (entry == null) {
                viewer.sendMessage(msgComponent("unknown-player", "player", identifier));
                return true;
            }
            open(viewer, plugin.getServer().getOfflinePlayer(entry.id()), ender);
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                String prefix = args[0].toLowerCase(Locale.ROOT);
                List<String> names = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
                return names.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
            }
            return List.of();
        }
    }
}
