package su.samplecraft.essentials.modules.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code DonutOrders-1.9.2.jar} (com.donutorders).
 * <p>
 * Buy-order market ported from the decompiled plugin:
 * <ul>
 *   <li>{@code /order} opens your active orders, {@code /order history} your history,
 *       {@code /order new} starts the create flow, {@code /order browse} lists public orders,</li>
 *   <li>create: {@code /order create <amount> <price>} while holding the wanted item -
 *       the buyer pays {@code price * amount * (1 + tax)}, the order is stored with an
 *       expiry of {@code default-expiry-days} and broadcast (original
 *       {@code broadcast-on-create}),</li>
 *   <li>deliver: {@code /order deliver <id>} while holding the items pays the seller
 *       {@code price * delivered} minus {@code tax-percent}, tracks partial fulfilment
 *       and moves leftovers into the buyer's stash when a delivery over-fills an order,
 *       exactly like {@code OrderManager#fulfillOrder},</li>
 *   <li>{@code /order cancel <id>} refunds the remaining amount to the buyer,
 *       {@code /order collect <id>} moves the stash items into the buyer's inventory,</li>
 *   <li>{@code /orderadmin list|purge|refund} for staff, order limit resolved through
 *       {@code donutorders.limit.<n>} (original LuckPerms limit listener), expiry
 *       check every minute like {@code runExpiryCheck}.</li>
 * </ul>
 * Allowed items: when {@code orders.allowed-items} is not empty only those materials
 * can be ordered (original AllowedItemsManager).
 */
public final class OrdersModule extends EssentialsModule {

    private enum Status { ACTIVE, COMPLETED, CANCELLED, EXPIRED }

    private record Order(UUID id, UUID buyer, String buyerName, ItemStack template, int amount,
                         int amountFulfilled, double pricePerItem, double total, long createdAt,
                         long expiresAt, Status status) {
        int remaining() {
            return amount - amountFulfilled;
        }

        Order withFulfilled(int fulfilled, Status newStatus) {
            return new Order(id, buyer, buyerName, template, amount, fulfilled, pricePerItem, total,
                    createdAt, expiresAt, newStatus);
        }
    }

    private final Map<UUID, Order> orders = new java.util.concurrent.ConcurrentHashMap<>();
    /** orders waiting for a stash collection: order id -> item stacks */
    private final Map<UUID, List<ItemStack>> stashes = new java.util.concurrent.ConcurrentHashMap<>();

    private ModuleStorage storage;

    public OrdersModule(SamplesEssentials plugin) {
        super("economy", "orders", "DonutOrders-1.9.2.jar", "Buy-order market with delivery, stash, tax and expiry");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.orders", "Use the order market", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.orders.admin", "Administer the order market", org.bukkit.permissions.PermissionDefault.OP);
        storage = new ModuleStorage(plugin, getDataFolder(), "orders.yml");
        load();
        OrdersCommand handler = new OrdersCommand();
        command("order", handler);
        command("orders", handler);
        command("orderadmin", new OrderAdminCommand());
        onDisableHook(() -> {
            unbindCommands();
            save();
        });
        // runExpiryCheck: every minute
        syncRepeat(1200L, 1200L, this::expireOrders);
    }

    /* ------------------------------------------------------------------ */
    /* storage                                                            */
    /* ------------------------------------------------------------------ */

    private void load() {
        var section = storage.yaml().getConfigurationSection("orders");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    String itemPath = "orders." + key + ".item";
                    ItemStack template = storage.yaml().getItemStack(itemPath);
                    if (template == null) {
                        continue;
                    }
                    orders.put(id, new Order(id,
                            UUID.fromString(storage.yaml().getString("orders." + key + ".buyer", id.toString())),
                            storage.yaml().getString("orders." + key + ".buyer-name", "?"),
                            template,
                            storage.yaml().getInt("orders." + key + ".amount"),
                            storage.yaml().getInt("orders." + key + ".fulfilled"),
                            storage.yaml().getDouble("orders." + key + ".price"),
                            storage.yaml().getDouble("orders." + key + ".total"),
                            storage.yaml().getLong("orders." + key + ".created"),
                            storage.yaml().getLong("orders." + key + ".expires"),
                            Status.valueOf(storage.yaml().getString("orders." + key + ".status", "ACTIVE"))));
                } catch (Exception ignored) {
                    // stale or corrupted order
                }
            }
        }
        var stashSection = storage.yaml().getConfigurationSection("stashes");
        if (stashSection != null) {
            for (String key : stashSection.getKeys(false)) {
                try {
                    UUID id = UUID.fromString(key);
                    List<ItemStack> items = new ArrayList<>();
                    for (Object raw : storage.yaml().getList("stashes." + key, List.of())) {
                        if (raw instanceof ItemStack item) {
                            items.add(item);
                        } else if (raw instanceof Map<?, ?> map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typed = (Map<String, Object>) map;
                            items.add(ItemStack.deserialize(typed));
                        }
                    }
                    stashes.put(id, items);
                } catch (Exception ignored) {
                    // corrupted stash
                }
            }
        }
        // expire orders whose time ran out while the server was offline
        expireOrders();
    }

    private void save() {
        storage.yaml().set("orders", null);
        orders.forEach((id, order) -> {
            String path = "orders." + id;
            storage.yaml().set(path + ".buyer", order.buyer().toString());
            storage.yaml().set(path + ".buyer-name", order.buyerName());
            storage.yaml().set(path + ".item", order.template());
            storage.yaml().set(path + ".amount", order.amount());
            storage.yaml().set(path + ".fulfilled", order.amountFulfilled());
            storage.yaml().set(path + ".price", order.pricePerItem());
            storage.yaml().set(path + ".total", order.total());
            storage.yaml().set(path + ".created", order.createdAt());
            storage.yaml().set(path + ".expires", order.expiresAt());
            storage.yaml().set(path + ".status", order.status().name());
        });
        storage.yaml().set("stashes", null);
        stashes.forEach((id, items) -> storage.yaml().set("stashes." + id, items));
        storage.save();
    }

    /* ------------------------------------------------------------------ */
    /* limits (OrderLimitManager)                                         */
    /* ------------------------------------------------------------------ */

    private int limitFor(Player player) {
        int limit = cfgInt("orders.default-limit", 1);
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            if (!info.getValue()) {
                continue;
            }
            String node = info.getPermission().toLowerCase(Locale.ROOT);
            String prefix = "samplese.orders.limit.";
            if (node.startsWith(prefix)) {
                try {
                    limit = Math.max(limit, Integer.parseInt(node.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                    // malformed node
                }
            }
        }
        return limit;
    }

    private List<Order> buyerOrders(UUID buyer) {
        return orders.values().stream()
                .filter(order -> order.buyer().equals(buyer))
                .sorted(Comparator.comparingLong(Order::createdAt))
                .toList();
    }

    private List<Order> activeOrders() {
        return orders.values().stream()
                .filter(order -> order.status() == Status.ACTIVE)
                .sorted(Comparator.comparingLong(Order::createdAt))
                .toList();
    }

    private Order byIndex(Player player, String raw) {
        try {
            return orders.get(UUID.fromString(raw));
        } catch (IllegalArgumentException e) {
            // the original GUI used positional ids too; accept 1-based indexes of the browsable list
            try {
                int index = Integer.parseInt(raw);
                List<Order> active = activeOrders();
                return index >= 1 && index <= active.size() ? active.get(index - 1) : null;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private boolean allowed(ItemStack template) {
        if (template == null) {
            return false;
        }
        List<String> allowed = cfgStringList("orders.allowed-items");
        if (allowed.isEmpty()) {
            return true;
        }
        for (String raw : allowed) {
            if (raw.equalsIgnoreCase(template.getType().name())) {
                return true;
            }
        }
        return false;
    }

    private double taxFactor() {
        return cfgDouble("orders.tax-percent", 0.0) / 100.0;
    }

    /* ------------------------------------------------------------------ */
    /* core operations (OrderManager)                                     */
    /* ------------------------------------------------------------------ */

    private void createOrder(Player buyer, ItemStack template, int amount, double pricePerItem) {
        if (!allowed(template)) {
            buyer.sendMessage(msgComponent("item-not-allowed"));
            return;
        }
        long activeCount = buyerOrders(buyer.getUniqueId()).stream()
                .filter(order -> order.status() == Status.ACTIVE).count();
        int max = limitFor(buyer);
        if (activeCount >= max) {
            buyer.sendMessage(msgComponent("max-orders-reached", "limit", String.valueOf(max)));
            return;
        }
        Economy economy = economy();
        if (economy == null) {
            buyer.sendMessage(msgComponent("no-economy"));
            return;
        }
        double total = pricePerItem * amount;
        double taxedTotal = total * (1.0 + taxFactor());
        if (!economy.has(buyer, taxedTotal)) {
            buyer.sendMessage(msgComponent("insufficient-funds",
                    "cost", formatPrice(taxedTotal), "balance", formatPrice(economy.getBalance(buyer))));
            return;
        }
        economy.withdrawPlayer(buyer, taxedTotal);
        long now = System.currentTimeMillis();
        long expiresAt = now + cfgInt("orders.default-expiry-days", 7) * 86_400_000L;
        Order order = new Order(UUID.randomUUID(), buyer.getUniqueId(), buyer.getName(),
                template.clone(), amount, 0, pricePerItem, total, now, expiresAt, Status.ACTIVE);
        orders.put(order.id(), order);
        save();
        buyer.sendMessage(msgComponent("order-created",
                "amount", String.valueOf(amount),
                "item", itemName(template),
                "price", formatPrice(pricePerItem),
                "total", formatPrice(total),
                "id", shortId(order.id())));
        broadcastOrderCreated(buyer, order);
    }

    private void broadcastOrderCreated(Player buyer, Order order) {
        if (!cfgBool("orders.broadcast-on-create", true)) {
            return;
        }
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.getUniqueId().equals(buyer.getUniqueId())) {
                continue;
            }
            online.sendMessage(msgComponent("order-broadcast",
                    "buyer", buyer.getName(),
                    "amount", String.valueOf(order.amount()),
                    "item", itemName(order.template()),
                    "price", formatPrice(order.pricePerItem()),
                    "id", shortId(order.id())));
        }
    }

    private void deliver(Player seller, Order order, ItemStack[] held) {
        if (order == null) {
            seller.sendMessage(msgComponent("order-not-found"));
            return;
        }
        if (order.status() != Status.ACTIVE) {
            seller.sendMessage(msgComponent("order-no-longer-active"));
            return;
        }
        if (order.buyer().equals(seller.getUniqueId())) {
            seller.sendMessage(msgComponent("delivery-own-order"));
            return;
        }
        ItemStack template = order.template();
        int valid = Math.min(countMatching(held, template), order.remaining());
        if (valid <= 0) {
            seller.sendMessage(msgComponent("delivery-no-items"));
            return;
        }
        // remove the matching items from the player's inventory
        int removed = removeMatching(seller, template, valid);
        if (removed <= 0) {
            seller.sendMessage(msgComponent("delivery-no-items"));
            return;
        }
        double payout = order.pricePerItem() * removed * (1.0 - taxFactor());
        Economy economy = economy();
        if (economy != null) {
            economy.depositPlayer(seller, payout);
        }
        int newFulfilled = order.amountFulfilled() + removed;
        boolean finished = newFulfilled >= order.amount();
        Order updated = order.withFulfilled(newFulfilled, finished ? Status.COMPLETED : Status.ACTIVE);
        orders.put(order.id(), updated);
        save();

        seller.sendMessage(msgComponent("delivery-success",
                "amount", String.valueOf(removed),
                "item", itemName(template),
                "payout", formatPrice(payout)));
        Player buyerOnline = plugin.getServer().getPlayer(order.buyer());
        if (buyerOnline != null) {
            buyerOnline.sendMessage(msgComponent("delivery-to-buyer",
                    "seller", seller.getName(),
                    "amount", String.valueOf(removed),
                    "item", itemName(template)));
        }
        if (finished) {
            // everything delivered: the bought items go to the buyer directly or to the stash
            List<ItemStack> collected = new ArrayList<>();
            int left = order.amount();
            while (left > 0) {
                ItemStack part = template.clone();
                part.setAmount(Math.min(left, part.getMaxStackSize()));
                collected.add(part);
                left -= part.getAmount();
            }
            if (buyerOnline != null) {
                Map<Integer, ItemStack> leftover = buyerOnline.getInventory().addItem(collected.toArray(new ItemStack[0]));
                if (!leftover.isEmpty()) {
                    stashes.computeIfAbsent(order.id(), k -> new ArrayList<>()).addAll(leftover.values());
                    buyerOnline.sendMessage(msgComponent("stash-notice"));
                } else {
                    buyerOnline.sendMessage(msgComponent("order-completed-buyer", "item", itemName(template)));
                }
            } else {
                stashes.computeIfAbsent(order.id(), k -> new ArrayList<>()).addAll(collected);
            }
            save();
        }
    }

    private void cancel(Player buyer, Order order) {
        if (order == null || !order.buyer().equals(buyer.getUniqueId())) {
            buyer.sendMessage(msgComponent("order-not-found"));
            return;
        }
        if (order.status() != Status.ACTIVE) {
            buyer.sendMessage(msgComponent("order-no-longer-active"));
            return;
        }
        double refund = order.pricePerItem() * order.remaining();
        Economy economy = economy();
        if (economy != null && refund > 0) {
            economy.depositPlayer(buyer, refund);
        }
        orders.put(order.id(), order.withFulfilled(order.amountFulfilled(), Status.CANCELLED));
        save();
        buyer.sendMessage(msgComponent("order-cancelled", "refund", formatPrice(refund)));
    }

    private void collectStash(Player buyer, Order order) {
        if (order == null || !order.buyer().equals(buyer.getUniqueId())) {
            buyer.sendMessage(msgComponent("order-not-found"));
            return;
        }
        List<ItemStack> stash = stashes.remove(order.id());
        if (stash == null || stash.isEmpty()) {
            buyer.sendMessage(msgComponent("stash-empty"));
            return;
        }
        Map<Integer, ItemStack> leftover = buyer.getInventory().addItem(stash.toArray(new ItemStack[0]));
        if (!leftover.isEmpty()) {
            stashes.put(order.id(), new ArrayList<>(leftover.values()));
        }
        save();
        buyer.sendMessage(msgComponent("stash-collected", "amount", String.valueOf(stash.size() - leftover.size())));
    }

    private void expireOrders() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Order order : new ArrayList<>(orders.values())) {
            if (order.status() == Status.ACTIVE && order.expiresAt() <= now) {
                orders.put(order.id(), order.withFulfilled(order.amountFulfilled(), Status.EXPIRED));
                double refund = order.pricePerItem() * order.remaining();
                Economy economy = economy();
                if (economy != null && refund > 0) {
                    org.bukkit.OfflinePlayer offline = plugin.getServer().getOfflinePlayer(order.buyer());
                    economy.depositPlayer(offline, refund);
                }
                Player online = plugin.getServer().getPlayer(order.buyer());
                if (online != null) {
                    online.sendMessage(msgComponent("order-expired",
                            "amount", String.valueOf(order.remaining()),
                            "item", itemName(order.template()),
                            "refund", formatPrice(refund)));
                }
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    /* ------------------------------------------------------------------ */
    /* item helpers (DeliveryItemUtils)                                   */
    /* ------------------------------------------------------------------ */

    private int countMatching(ItemStack[] items, ItemStack template) {
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && matches(item, template)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private int removeMatching(Player player, ItemStack template, int wanted) {
        int removed = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && removed < wanted; i++) {
            ItemStack item = contents[i];
            if (item == null || !matches(item, template)) {
                continue;
            }
            int take = Math.min(item.getAmount(), wanted - removed);
            removed += take;
            if (take >= item.getAmount()) {
                player.getInventory().setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - take);
                player.getInventory().setItem(i, item);
            }
        }
        return removed;
    }

    /** Item must have the same material and (for damageable gear) the same meta. */
    private boolean matches(ItemStack item, ItemStack template) {
        if (item.getType() != template.getType()) {
            return false;
        }
        if (template.getItemMeta() == null) {
            return true;
        }
        return template.isSimilar(item);
    }

    private String itemName(ItemStack stack) {
        if (stack.getItemMeta() != null && stack.getItemMeta().hasDisplayName()) {
            return Text.plain(stack.getItemMeta().displayName());
        }
        return stack.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private String formatPrice(double value) {
        return new DecimalFormat(cfgString("format.price", "#,##0.##")).format(value);
    }

    private Economy economy() {
        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    /* ------------------------------------------------------------------ */
    /* /order + /orders                                                   */
    /* ------------------------------------------------------------------ */

    private final class OrdersCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.orders")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                listOrders(player, false);
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "history" -> listOrders(player, true);
                case "browse" -> {
                    listOrders(player, false);
                }
                case "new", "create" -> {
                    if (args.length >= 4) {
                        try {
                            int amount = Integer.parseInt(args[2]);
                            double price = Double.parseDouble(args[3]);
                            createOrder(player, player.getInventory().getItemInMainHand(), amount, price);
                        } catch (NumberFormatException e) {
                            player.sendMessage(msgComponent("create-usage"));
                        }
                    } else {
                        player.sendMessage(msgComponent("create-usage"));
                    }
                }
                case "deliver" -> {
                    if (args.length < 2) {
                        player.sendMessage(msgComponent("deliver-usage"));
                        return true;
                    }
                    deliver(player, byIndex(player, args[1]), player.getInventory().getContents());
                }
                case "cancel" -> {
                    if (args.length < 2) {
                        player.sendMessage(msgComponent("cancel-usage"));
                        return true;
                    }
                    cancel(player, byIndex(player, args[1]));
                }
                case "collect" -> {
                    if (args.length < 2) {
                        player.sendMessage(msgComponent("collect-usage"));
                        return true;
                    }
                    collectStash(player, byIndex(player, args[1]));
                }
                default -> player.sendMessage(msgComponent("usage"));
            }
            return true;
        }

        private void listOrders(Player player, boolean history) {
            List<Order> list = history
                    ? buyerOrders(player.getUniqueId())
                    : activeOrders();
            if (list.isEmpty()) {
                player.sendMessage(msgComponent(history ? "history-empty" : "orders-empty"));
                return;
            }
            player.sendMessage(msgComponent(history ? "history-header" : "orders-header",
                    "count", String.valueOf(list.size())));
            int index = 1;
            for (Order order : list) {
                player.sendMessage(msgComponent("order-line",
                        "index", String.valueOf(index++),
                        "id", shortId(order.id()),
                        "amount", String.valueOf(order.remaining()),
                        "item", itemName(order.template()),
                        "price", formatPrice(order.pricePerItem()),
                        "buyer", order.buyerName(),
                        "status", order.status().name().toLowerCase(Locale.ROOT),
                        "time", formatDuration(Math.max(0, order.expiresAt() - System.currentTimeMillis()) / 1000L)));
            }
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("new", "browse", "history", "deliver", "cancel", "collect").stream()
                        .filter(option -> option.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            if (args.length == 2 && List.of("deliver", "cancel", "collect").contains(args[0].toLowerCase(Locale.ROOT))) {
                return activeOrders().stream().map(order -> shortId(order.id())).toList();
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("new")) {
                return List.of("1", "8", "16", "32", "64");
            }
            if (args.length == 4 && args[0].equalsIgnoreCase("new")) {
                return List.of("1", "10", "100", "1000");
            }
            return List.of();
        }
    }

    /** /orderadmin list|purge|refund (OrderAdminCommand). */
    private final class OrderAdminCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.orders.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String sub = args.length == 0 ? "list" : args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "list" -> {
                    sender.sendMessage(msgComponent("admin-list-header",
                            "active", String.valueOf(activeOrders().size()),
                            "total", String.valueOf(orders.size()),
                            "stashes", String.valueOf(stashes.size())));
                    for (Order order : activeOrders()) {
                        sender.sendMessage(Text.parse("<gray>" + shortId(order.id()) + " <white>"
                                + order.remaining() + "x " + itemName(order.template())
                                + " <gray>for <white>" + formatPrice(order.pricePerItem()) + " <gray>each by <white>"
                                + order.buyerName()));
                    }
                }
                case "purge" -> {
                    int removed = 0;
                    for (Order order : new ArrayList<>(orders.values())) {
                        if (order.status() != Status.ACTIVE) {
                            orders.remove(order.id());
                            removed++;
                        }
                    }
                    save();
                    sender.sendMessage(msgComponent("admin-purged", "count", String.valueOf(removed)));
                }
                case "refund" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("admin-refund-usage"));
                        return true;
                    }
                    Order order = byIndex(sender instanceof Player player ? player : null, args[1]);
                    if (order == null) {
                        sender.sendMessage(msgComponent("order-not-found"));
                        return true;
                    }
                    Economy economy = economy();
                    double refund = order.pricePerItem() * order.remaining();
                    if (economy != null && refund > 0) {
                        economy.depositPlayer(plugin.getServer().getOfflinePlayer(order.buyer()), refund);
                    }
                    orders.put(order.id(), order.withFulfilled(order.amountFulfilled(), Status.CANCELLED));
                    save();
                    sender.sendMessage(msgComponent("admin-refunded",
                            "id", shortId(order.id()), "amount", formatPrice(refund)));
                }
                default -> sender.sendMessage(msgComponent("admin-usage"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("list", "purge", "refund");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("refund")) {
                return activeOrders().stream().map(order -> shortId(order.id())).toList();
            }
            return List.of();
        }
    }

    private static String formatDuration(long seconds) {
        if (seconds <= 0) {
            return "expired";
        }
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
