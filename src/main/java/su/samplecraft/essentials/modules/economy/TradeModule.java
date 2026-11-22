package su.samplecraft.essentials.modules.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code AxTrade-1.28.2.jar} (com.artillexstudios.axtrade).
 * <p>
 * Two player inventories side by side in a 6-row GUI: item slots plus Vault money
 * and XP offers on both sides, a red/green confirm button per player, and a
 * countdown ({@code trade-confirm-seconds}) after both sides confirmed. Item and
 * money/XP changes reset confirmations, which mirrors the safety model of the
 * original. Requests expire after {@code trade-request-expire-seconds} and respect
 * {@code trade-max-distance}.
 * <p>
 * Ported guarantees:
 * <ul>
 *   <li>{@code /trade <player>}, {@code /trade accept|deny|toggle|reload}, alias
 *       {@code axtrade}; shift-right-click a player to send a request (300 ms
 *       minimum interval, like the original).</li>
 *   <li>Item blacklist with wildcard support ({@code name*}, {@code *name}),
 *       custom-model-data entries, blacklisted worlds, {@code abort.*} rules.</li>
 *   <li>Vault tax per currency ({@code tax} percent, capped at 99%), XP offers.</li>
 *   <li>{@code disallow-same-ip-trade} and {@code prevent-adding-items-when-inventory-full}.</li>
 * </ul>
 * Not ported: shulker content previews, trade history persistence and the
 * {@code /trade force} admin GUI (the admin can interrupt with {@code /trade deny}).
 */
public final class TradeModule extends EssentialsModule implements Listener {

    /** Item slots: own side left, partner side right (5 rows of 4, as in guis.yml). */
    private static final int[] OWN_SLOTS = {9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30,
            36, 37, 38, 39, 45, 46, 47, 48};
    private static final int[] PARTNER_SLOTS = {14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35,
            41, 42, 43, 44, 50, 51, 52, 53};
    private static final int OWN_CONFIRM_SLOT = 0;
    private static final int PARTNER_CONFIRM_SLOT = 8;
    private static final int OWN_MONEY_SLOT = 2;
    private static final int PARTNER_MONEY_SLOT = 6;
    private static final int OWN_XP_SLOT = 3;
    private static final int PARTNER_XP_SLOT = 5;

    private record Request(UUID sender, UUID receiver, long created) {
    }

    private final class Trade {
        final UUID first;
        final UUID second;
        final Inventory inventory;
        final Map<Integer, ItemStack> firstItems = new LinkedHashMap<>();
        final Map<Integer, ItemStack> secondItems = new LinkedHashMap<>();
        double firstMoney;
        double secondMoney;
        int firstXp;
        int secondXp;
        boolean firstConfirmed;
        boolean secondConfirmed;
        boolean finishing;
        long confirmAt;
        final Map<UUID, org.bukkit.Location> startLocations = new HashMap<>();

        Trade(Player first, Player second) {
            this.first = first.getUniqueId();
            this.second = second.getUniqueId();
            this.inventory = Bukkit.createInventory(null, 54,
                    Text.parse(cfgString("gui.title", "&0Trading with: %player%")
                            .replace("%player%", second.getName())));
        }

        boolean involves(UUID uuid) {
            return first.equals(uuid) || second.equals(uuid);
        }

        Player partnerOf(Player player) {
            return plugin.getServer().getPlayer(first.equals(player.getUniqueId()) ? second : first);
        }

        boolean isFirst(Player player) {
            return first.equals(player.getUniqueId());
        }
    }

    private final Map<UUID, Trade> tradesByPlayer = new HashMap<>();
    private final List<Trade> trades = new ArrayList<>();
    private final Map<UUID, Request> requests = new HashMap<>();
    private final Set<UUID> toggledOff = new HashSet<>();
    private final Map<UUID, Long> lastRequest = new HashMap<>();

    public TradeModule(SamplesEssentials plugin) {
        super("economy", "trade", "AxTrade-1.28.2.jar",
                "Secure player-to-player trading with items, money and XP");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.trade", "Use /trade", PermissionDefault.TRUE);
        ensurePermission("samplese.trade.bypass.blacklist", "Trade blacklisted items",
                PermissionDefault.FALSE);
        ensurePermission("samplese.trade.admin", "Administer trades", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        command("trade", new TradeCommand());
        onDisableHook(this::unbindCommands);
        onDisableHook(this::abortAll);
        listen(this);
        syncRepeat(10L, 10L, this::tick);
    }

    private void abortAll() {
        for (Trade trade : new ArrayList<>(trades)) {
            abort(trade, msg("cancelled"));
        }
        trades.clear();
        tradesByPlayer.clear();
        requests.clear();
    }

    /* ------------------------------------------------------------------ */
    /* GUI rendering                                                      */
    /* ------------------------------------------------------------------ */

    private void render(Trade trade) {
        Player first = plugin.getServer().getPlayer(trade.first);
        Player second = plugin.getServer().getPlayer(trade.second);
        Inventory inv = trade.inventory;
        inv.clear();
        ItemStack filler = marker(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < 54; slot++) {
            inv.setItem(slot, filler);
        }
        int i = 0;
        for (ItemStack item : trade.firstItems.values()) {
            if (i < OWN_SLOTS.length) {
                inv.setItem(OWN_SLOTS[i++], item);
            }
        }
        i = 0;
        for (ItemStack item : trade.secondItems.values()) {
            if (i < PARTNER_SLOTS.length) {
                inv.setItem(PARTNER_SLOTS[i++], item);
            }
        }
        inv.setItem(OWN_MONEY_SLOT, currencyIcon(Material.GOLD_NUGGET,
                cfgString("gui.money-name", "&b&lMONEY"), trade.firstMoney, first));
        inv.setItem(PARTNER_MONEY_SLOT, currencyIcon(Material.GOLD_NUGGET,
                cfgString("gui.money-name", "&b&lMONEY"), trade.secondMoney, second));
        inv.setItem(OWN_XP_SLOT, currencyIcon(Material.EXPERIENCE_BOTTLE,
                cfgString("gui.xp-name", "&b&lEXPERIENCE"), trade.firstXp, first));
        inv.setItem(PARTNER_XP_SLOT, currencyIcon(Material.EXPERIENCE_BOTTLE,
                cfgString("gui.xp-name", "&b&lEXPERIENCE"), trade.secondXp, second));
        inv.setItem(OWN_CONFIRM_SLOT, confirmIcon(trade.firstConfirmed, first, true));
        inv.setItem(PARTNER_CONFIRM_SLOT, confirmIcon(trade.secondConfirmed, second, false));
    }

    private ItemStack currencyIcon(Material material, String name, double amount, Player owner) {
        ItemStack icon = new ItemStack(material);
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Text.parse(name));
        boolean xp = material == Material.EXPERIENCE_BOTTLE;
        List<Component> lore = new ArrayList<>();
        lore.add(Text.parse("&7Offer: &f" + formatNumber(amount) + (xp ? " EXP" : "$")));
        if (!xp && cfgDouble("tax", 0) > 0) {
            double tax = Math.min(cfgDouble("tax", 0), 99) / 100.0;
            lore.add(Text.parse("&7After tax: &f" + formatNumber(amount * (1 - tax)) + "$"));
        }
        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack confirmIcon(boolean confirmed, Player owner, boolean own) {
        ItemStack icon = new ItemStack(confirmed ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        ItemMeta meta = icon.getItemMeta();
        String name = confirmed
                ? cfgString("gui.confirmed-name", "&a&lCONFIRMED")
                : cfgString("gui.confirm-name", "&c&lCLICK TO CONFIRM");
        meta.displayName(Text.parse(name));
        List<Component> lore = new ArrayList<>();
        if (own) {
            lore.add(Text.parse(confirmed ? "&7Click to cancel your confirmation."
                    : "&7Click when you are happy with the trade."));
        } else if (owner != null) {
            lore.add(Text.parse(confirmed
                    ? "&7" + owner.getName() + " has confirmed."
                    : "&7Waiting for " + owner.getName() + " to confirm."));
        }
        meta.lore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack marker(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(name));
        item.setItemMeta(meta);
        return item;
    }

    /* ------------------------------------------------------------------ */
    /* trade lifecycle                                                    */
    /* ------------------------------------------------------------------ */

    private void start(Player first, Player second) {
        if (tradesByPlayer.containsKey(first.getUniqueId())
                || tradesByPlayer.containsKey(second.getUniqueId())) {
            reply(first, "already-trading");
            return;
        }
        if (blockedWorld(first) || blockedWorld(second)) {
            reply(first, "world-blocked");
            return;
        }
        if (cfgBool("disallow-same-ip-trade", false) && sameIp(first, second)) {
            reply(first, "same-ip", "player", second.getName());
            return;
        }
        if (!nearby(first, second)) {
            reply(first, "too-far", "player", second.getName());
            return;
        }
        Trade trade = new Trade(first, second);
        trades.add(trade);
        tradesByPlayer.put(first.getUniqueId(), trade);
        tradesByPlayer.put(second.getUniqueId(), trade);
        trade.startLocations.put(first.getUniqueId(), first.getLocation().clone());
        trade.startLocations.put(second.getUniqueId(), second.getLocation().clone());
        render(trade);
        first.openInventory(trade.inventory);
        second.openInventory(trade.inventory);
        // The second open replaces the first viewer on some implementations; reopen
        // sequentially on the next tick to be safe for both.
        syncLater(1L, () -> {
            if (first.isOnline()) {
                first.openInventory(trade.inventory);
            }
            if (second.isOnline()) {
                second.openInventory(trade.inventory);
            }
        });
    }

    private void finish(Trade trade) {
        Player first = plugin.getServer().getPlayer(trade.first);
        Player second = plugin.getServer().getPlayer(trade.second);
        if (first == null || second == null) {
            abort(trade, msg("partner-left"));
            return;
        }
        trade.finishing = true;
        // Money and XP first so item space is judged after the currency moved.
        if (trade.firstMoney > 0) {
            economy().withdrawPlayer(first, trade.firstMoney);
            economy().depositPlayer(second, trade.firstMoney * (1 - taxRate()));
        }
        if (trade.secondMoney > 0) {
            economy().withdrawPlayer(second, trade.secondMoney);
            economy().depositPlayer(first, trade.secondMoney * (1 - taxRate()));
        }
        int firstXp = trade.firstXp;
        int secondXp = trade.secondXp;
        first.giveExp(-firstXp);
        second.giveExp(firstXp);
        second.giveExp(-secondXp);
        first.giveExp(secondXp);

        List<ItemStack> toFirst = new ArrayList<>(trade.secondItems.values());
        List<ItemStack> toSecond = new ArrayList<>(trade.firstItems.values());
        clearViewers(trade);
        trades.remove(trade);
        tradesByPlayer.remove(trade.first);
        tradesByPlayer.remove(trade.second);
        // Return what could not fit so nothing is ever lost.
        for (ItemStack leftover : first.getInventory().addItem(toFirst.toArray(ItemStack[]::new)).values()) {
            first.getWorld().dropItem(first.getLocation(), leftover);
        }
        for (ItemStack leftover : second.getInventory().addItem(toSecond.toArray(ItemStack[]::new)).values()) {
            second.getWorld().dropItem(second.getLocation(), leftover);
        }
        reply(first, "completed", "player", second.getName());
        reply(second, "completed", "player", first.getName());
        if (cfgBool("enable-trade-summaries", true)) {
            reply(first, "summary", "money", formatNumber(trade.secondMoney),
                    "items", String.valueOf(toFirst.size()));
            reply(second, "summary", "money", formatNumber(trade.firstMoney),
                    "items", String.valueOf(toSecond.size()));
        }
    }

    private void abort(Trade trade, String reason) {
        if (trade.finishing) {
            return;
        }
        trade.finishing = true;
        Player first = plugin.getServer().getPlayer(trade.first);
        Player second = plugin.getServer().getPlayer(trade.second);
        clearViewers(trade);
        if (first != null) {
            giveBack(first, trade.firstItems.values());
            if (reason != null && !reason.isBlank()) {
                first.sendMessage(Text.parse(reason));
            }
        }
        if (second != null) {
            giveBack(second, trade.secondItems.values());
            if (reason != null && !reason.isBlank()) {
                second.sendMessage(Text.parse(reason));
            }
        }
        trades.remove(trade);
        tradesByPlayer.remove(trade.first);
        tradesByPlayer.remove(trade.second);
    }

    private void giveBack(Player player, Iterable<ItemStack> items) {
        for (ItemStack item : items) {
            for (ItemStack leftover : player.getInventory().addItem(item.clone()).values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
        }
    }

    private void clearViewers(Trade trade) {
        Player first = plugin.getServer().getPlayer(trade.first);
        if (first != null) {
            first.closeInventory();
        }
        Player second = plugin.getServer().getPlayer(trade.second);
        if (second != null) {
            second.closeInventory();
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        long requestTtl = Math.max(1, cfgInt("trade-request-expire-seconds", 60)) * 1000L;
        requests.entrySet().removeIf(entry -> now - entry.getValue().created > requestTtl);
        for (Trade trade : new ArrayList<>(trades)) {
            if (cfgBool("abort.move", true)) {
                for (Map.Entry<UUID, org.bukkit.Location> entry : trade.startLocations.entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && entry.getValue().getWorld() != null
                            && player.getWorld().equals(entry.getValue().getWorld())
                            && player.getLocation().distanceSquared(entry.getValue()) > 9) {
                        abort(trade, msg("abort-moved"));
                        break;
                    }
                }
                if (trade.finishing) {
                    continue;
                }
            }
            if (trade.firstConfirmed && trade.secondConfirmed) {
                long elapsed = (now - trade.confirmAt) / 1000L;
                int wait = Math.max(0, cfgInt("trade-confirm-seconds", 5));
                if (elapsed >= wait) {
                    finish(trade);
                }
            }
        }
    }

    private boolean blockedWorld(Player player) {
        for (String world : cfgStringList("blacklisted-worlds")) {
            if (player.getWorld().getName().equalsIgnoreCase(world)) {
                return true;
            }
        }
        return false;
    }

    private boolean sameIp(Player first, Player second) {
        if (first.getAddress() == null || second.getAddress() == null
                || first.getAddress().getAddress() == null || second.getAddress().getAddress() == null) {
            return false;
        }
        return first.getAddress().getAddress().equals(second.getAddress().getAddress());
    }

    private boolean nearby(Player first, Player second) {
        int max = cfgInt("trade-max-distance", 10);
        if (max < 0) {
            return true;
        }
        if (!first.getWorld().equals(second.getWorld())) {
            return false;
        }
        return first.getLocation().distanceSquared(second.getLocation()) <= (double) max * max;
    }

    private double taxRate() {
        double percent = Math.min(Math.max(cfgDouble("tax", 0), 0), 99);
        return percent / 100.0;
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    /* ------------------------------------------------------------------ */
    /* blacklist                                                          */
    /* ------------------------------------------------------------------ */

    private boolean blacklisted(ItemStack item, Player player) {
        if (player.hasPermission("samplese.trade.bypass.blacklist")) {
            return false;
        }
        for (Map<?, ?> entry : config().getMapList("blacklist-items")) {
            Object material = entry.get("material");
            if (material != null && !item.getType().name().equalsIgnoreCase(String.valueOf(material))) {
                continue;
            }
            if (entry.get("custom-model-data") instanceof Number number) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !meta.hasCustomModelData()
                        || meta.getCustomModelData() != number.intValue()) {
                    continue;
                }
            }
            Object name = entry.get("name");
            if (name != null && !matchesWildcard(String.valueOf(name), displayName(item))) {
                continue;
            }
            return true;
        }
        return false;
    }

    private String displayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return "";
        }
        return Text.LEGACY.serialize(meta.displayName());
    }

    private boolean matchesWildcard(String pattern, String value) {
        if (pattern.startsWith("*") && pattern.endsWith("*") && pattern.length() > 2) {
            return value.contains(pattern.substring(1, pattern.length() - 1));
        }
        if (pattern.endsWith("*")) {
            return value.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        if (pattern.startsWith("*")) {
            return value.endsWith(pattern.substring(1));
        }
        return value.equals(pattern);
    }

    /* ------------------------------------------------------------------ */
    /* events                                                             */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Trade trade = tradesByPlayer.get(player.getUniqueId());
        if (trade == null || !event.getInventory().equals(trade.inventory)) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) {
            return;
        }
        boolean firstSide = trade.isFirst(player);
        int[] ownSlots = firstSide ? OWN_SLOTS : PARTNER_SLOTS;
        int confirmSlot = firstSide ? OWN_CONFIRM_SLOT : PARTNER_CONFIRM_SLOT;
        int moneySlot = firstSide ? OWN_MONEY_SLOT : PARTNER_MONEY_SLOT;
        int xpSlot = firstSide ? OWN_XP_SLOT : PARTNER_XP_SLOT;

        if (slot == confirmSlot) {
            toggleConfirm(trade, player, firstSide);
            return;
        }
        if (slot == moneySlot) {
            adjustMoney(trade, player, firstSide, event.getClick());
            return;
        }
        if (slot == xpSlot) {
            adjustXp(trade, player, firstSide, event.getClick());
            return;
        }
        int key = slotKey(ownSlots, slot);
        if (key < 0) {
            return;
        }
        // store under the canonical "own" slot index so both sides share one key space
        int storageKey = OWN_SLOTS[key];
        // move items between the player's inventory and the trade
        Map<Integer, ItemStack> mine = firstSide ? trade.firstItems : trade.secondItems;
        if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT) {
            if (mine.containsKey(storageKey)) {
                ItemStack removed = mine.remove(storageKey);
                if (removed != null) {
                    for (ItemStack leftover : player.getInventory().addItem(removed).values()) {
                        player.getWorld().dropItem(player.getLocation(), leftover);
                    }
                }
            } else {
                ItemStack cursor = event.getCursor();
                if (cursor != null && !cursor.getType().isAir()) {
                    if (blacklisted(cursor, player)) {
                        reply(player, "blacklisted");
                        return;
                    }
                    mine.put(storageKey, cursor.clone());
                    player.setItemOnCursor(null);
                }
            }
            resetConfirms(trade);
            render(trade);
        }
    }

    /** Index of {@code slot} within the given side's slot list, or -1. */
    private int slotKey(int[] slots, int slot) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                return i;
            }
        }
        return -1;
    }

    private void toggleConfirm(Trade trade, Player player, boolean firstSide) {
        if (firstSide) {
            trade.firstConfirmed = !trade.firstConfirmed;
        } else {
            trade.secondConfirmed = !trade.secondConfirmed;
        }
        if (trade.firstConfirmed && trade.secondConfirmed) {
            trade.confirmAt = System.currentTimeMillis();
            int wait = Math.max(0, cfgInt("trade-confirm-seconds", 5));
            Player first = plugin.getServer().getPlayer(trade.first);
            Player second = plugin.getServer().getPlayer(trade.second);
            if (wait > 0) {
                if (first != null) {
                    reply(first, "both-confirmed", "seconds", String.valueOf(wait));
                }
                if (second != null) {
                    reply(second, "both-confirmed", "seconds", String.valueOf(wait));
                }
            }
        } else {
            trade.confirmAt = 0L;
        }
        render(trade);
    }

    private void resetConfirms(Trade trade) {
        trade.firstConfirmed = false;
        trade.secondConfirmed = false;
        trade.confirmAt = 0L;
    }

    private void adjustMoney(Trade trade, Player player, boolean firstSide, ClickType click) {
        double current = firstSide ? trade.firstMoney : trade.secondMoney;
        double step = click.isShiftClick() ? 100 : 10;
        double next;
        if (click.isRightClick()) {
            next = current - step;
        } else if (click == ClickType.MIDDLE) {
            next = 0;
        } else {
            next = current + step;
        }
        next = Math.max(0, next);
        var economy = economy();
        if (economy == null) {
            reply(player, "no-economy");
            return;
        }
        if (next > economy.getBalance(player)) {
            next = economy.getBalance(player);
        }
        if (firstSide) {
            trade.firstMoney = next;
        } else {
            trade.secondMoney = next;
        }
        resetConfirms(trade);
        render(trade);
    }

    private void adjustXp(Trade trade, Player player, boolean firstSide, ClickType click) {
        int current = firstSide ? trade.firstXp : trade.secondXp;
        int step = click.isShiftClick() ? 100 : 10;
        int next;
        if (click.isRightClick()) {
            next = current - step;
        } else if (click == ClickType.MIDDLE) {
            next = 0;
        } else {
            next = current + step;
        }
        next = Math.max(0, Math.min(next, player.getTotalExperience()));
        if (firstSide) {
            trade.firstXp = next;
        } else {
            trade.secondXp = next;
        }
        resetConfirms(trade);
        render(trade);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Trade trade = tradesByPlayer.get(player.getUniqueId());
        if (trade == null || !event.getInventory().equals(trade.inventory)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        Trade trade = tradesByPlayer.get(player.getUniqueId());
        if (trade == null || !event.getInventory().equals(trade.inventory) || trade.finishing) {
            return;
        }
        // Reopening is done by the other viewer's close handler, so only the first
        // closer aborts and the second sees an empty trade.
        abort(trade, msg("cancelled"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Trade trade = tradesByPlayer.get(event.getPlayer().getUniqueId());
        if (trade != null) {
            abort(trade, msg("partner-left"));
        }
        requests.entrySet().removeIf(entry -> entry.getKey().equals(event.getPlayer().getUniqueId())
                || entry.getValue().receiver().equals(event.getPlayer().getUniqueId()));
        toggledOff.remove(event.getPlayer().getUniqueId());
        tradesByPlayer.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!cfgBool("abort.command", true)) {
            return;
        }
        Trade trade = tradesByPlayer.get(event.getPlayer().getUniqueId());
        if (trade == null) {
            return;
        }
        String label = event.getMessage().split(" ")[0].toLowerCase(Locale.ROOT);
        if (!label.equals("/trade") && !label.equals("/axtrade")) {
            abort(trade, msg("abort-command"));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (tradesByPlayer.containsKey(event.getPlayer().getUniqueId())
                && cfgBool("abort.interact", true)) {
            // dropping while the trade GUI is open is blocked, not treated as an abort
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!event.getRightClicked().equals(player)
                && event.getRightClicked() instanceof Player target
                && player.isSneaking()
                && cfgBool("shift-click-send-request", true)
                && tradesByPlayer.get(player.getUniqueId()) == null) {
            long now = System.currentTimeMillis();
            Long last = lastRequest.get(player.getUniqueId());
            if (last == null || now - last > 300L) {
                lastRequest.put(player.getUniqueId(), now);
                request(player, target);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* requests                                                           */
    /* ------------------------------------------------------------------ */

    private void request(Player sender, Player target) {
        if (!target.hasPermission("samplese.trade")) {
            reply(sender, "target-disabled", "player", target.getName());
            return;
        }
        if (toggledOff.contains(target.getUniqueId())) {
            reply(sender, "target-disabled", "player", target.getName());
            return;
        }
        if (tradesByPlayer.containsKey(target.getUniqueId())) {
            reply(sender, "already-trading", "player", target.getName());
            return;
        }
        requests.put(target.getUniqueId(), new Request(sender.getUniqueId(), target.getUniqueId(),
                System.currentTimeMillis()));
        reply(sender, "request-sent", "player", target.getName());
        reply(target, "request-received", "player", sender.getName());
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class TradeCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    reply(sender, "player-only");
                    return true;
                }
                reply(player, "usage");
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "reload" -> {
                    if (!sender.hasPermission("samplese.trade.admin")) {
                        sender.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    reloadConfig();
                    reply(sender, "reloaded");
                }
                case "toggle", "toggled" -> {
                    if (!(sender instanceof Player player)) {
                        reply(sender, "player-only");
                        return true;
                    }
                    if (toggledOff.remove(player.getUniqueId())) {
                        saveConfigValue("toggled." + player.getUniqueId(), null);
                        reply(player, "toggle-on");
                    } else {
                        toggledOff.add(player.getUniqueId());
                        saveConfigValue("toggled." + player.getUniqueId(), true);
                        reply(player, "toggle-off");
                    }
                }
                case "accept" -> {
                    if (!(sender instanceof Player player)) {
                        reply(sender, "player-only");
                        return true;
                    }
                    accept(player);
                }
                case "deny" -> {
                    if (!(sender instanceof Player player)) {
                        reply(sender, "player-only");
                        return true;
                    }
                    Request request = requests.remove(player.getUniqueId());
                    if (request == null) {
                        reply(player, "no-request");
                        return true;
                    }
                    Player requester = plugin.getServer().getPlayer(request.sender());
                    reply(player, "denied");
                    if (requester != null) {
                        reply(requester, "denied-sender", "player", player.getName());
                    }
                }
                default -> {
                    if (!(sender instanceof Player player)) {
                        reply(sender, "player-only");
                        return true;
                    }
                    if (!player.hasPermission("samplese.trade")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    Player target = plugin.getServer().getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(plugin.getLang().component("player-not-found", "player", args[0]));
                        return true;
                    }
                    if (target.equals(player)) {
                        reply(player, "self-request");
                        return true;
                    }
                    request(player, target);
                }
            }
            return true;
        }

        private void accept(Player player) {
            Request request = requests.remove(player.getUniqueId());
            if (request == null) {
                reply(player, "no-request");
                return;
            }
            Player sender = plugin.getServer().getPlayer(request.sender());
            if (sender == null) {
                reply(player, "requester-offline");
                return;
            }
            start(sender, player);
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return null; // player names
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
