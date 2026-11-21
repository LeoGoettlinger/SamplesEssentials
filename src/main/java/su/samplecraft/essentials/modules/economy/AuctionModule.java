package su.samplecraft.essentials.modules.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code CrazyAuctions-1.7.0.jar} (com.badbones69.crazyauctions).
 * <p>
 * Both market types of the original are ported:
 * <ul>
 *   <li>{@code SELL} listings — {@code /ca sell <price>} while holding an item stores it
 *       for {@code Sell-Time} (default 2d), sellers can cancel ({@code /ca listed}) and
 *       buyers purchase with one click ({@code /ca view}),</li>
 *   <li>{@code BID} listings — {@code /ca bid <start-price>} starts a timed auction for
 *       {@code Bid-Time} (default 2m30s); {@code /ca view} shows the current bid,
 *       clicking bids the next increment; the highest bidder wins when the bid time ends
 *       and claims the item within {@code Bid-Winner-Time},</li>
 *   <li>expired / cancelled listings ({@code /ca expired|collect}), the original
 *       {@code force_end_all} admin command, minimum/maximum prices and the
 *       damaged-item rule,</li>
 *   <li>listing lifetime {@code Full-Expire-Time} (10d) and per-player permission
 *       {@code samplese.auction.sell/<n>} used by the original Permissions enum,</li>
 *   <li>GUI lore templates from the original {@code GUISettings}.</li>
 * </ul>
 * Data lives in {@code economy/auction/data/auctions.yml} (Base64 item stacks, like
 * the original data.yml {@code Items.<id>.Item} layout).
 */
public final class AuctionModule extends EssentialsModule implements Listener {

    private enum Type { SELL, BID }

    private static final class Listing {
        UUID id;
        Type type;
        UUID seller;
        String sellerName;
        ItemStack item;
        double price;
        double highestBid;
        UUID highestBidder;
        String highestBidderName;
        long listed;
        long expires;
        long claimDeadline;

        Listing copy() {
            Listing copy = new Listing();
            copy.id = id;
            copy.type = type;
            copy.seller = seller;
            copy.sellerName = sellerName;
            copy.item = item == null ? null : item.clone();
            copy.price = price;
            copy.highestBid = highestBid;
            copy.highestBidder = highestBidder;
            copy.highestBidderName = highestBidderName;
            copy.listed = listed;
            copy.expires = expires;
            copy.claimDeadline = claimDeadline;
            return copy;
        }
    }

    private final Map<UUID, Listing> listings = new ConcurrentHashMap<>();
    /** expired/cancelled listings waiting for collection: owner -> listings */
    private final Map<UUID, List<Listing>> expired = new ConcurrentHashMap<>();
    /** players with an open auction inventory -> listing ids in slot order */
    private final Map<UUID, List<UUID>> openMenus = new ConcurrentHashMap<>();
    private final Map<UUID, String> openModes = new ConcurrentHashMap<>();

    private ModuleStorage storage;

    public AuctionModule(SamplesEssentials plugin) {
        super("economy", "auction", "CrazyAuctions-1.7.0.jar", "Auction house with selling, bidding, expired items and categories");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.auction", "Use the auction house", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.auction.admin", "Administer the auction house", org.bukkit.permissions.PermissionDefault.OP);
        ensurePermission("samplese.auction.selling", "Create sell listings", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.auction.bidding", "Create bid listings and bid", org.bukkit.permissions.PermissionDefault.TRUE);
        storage = new ModuleStorage(plugin, getDataFolder(), "auctions.yml");
        load();
        command("ca", new AuctionCommand());
        command("auction", new AuctionCommand());
        command("crazyauctions", new AuctionCommand());
        onDisableHook(() -> {
            unbindCommands();
            save();
        });
        listen(this);
        syncRepeat(1200L, 1200L, this::expireListings);
    }

    /* ------------------------------------------------------------------ */
    /* storage                                                            */
    /* ------------------------------------------------------------------ */

    private void load() {
        var section = storage.yaml().getConfigurationSection("items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                Listing listing = new Listing();
                listing.id = UUID.fromString(key);
                String path = "items." + key;
                listing.type = Type.valueOf(storage.yaml().getString(path + ".type", "SELL"));
                listing.seller = UUID.fromString(storage.yaml().getString(path + ".seller", key));
                listing.sellerName = storage.yaml().getString(path + ".seller-name", "?");
                String encoded = storage.yaml().getString(path + ".item");
                if (encoded == null) {
                    continue;
                }
                listing.item = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
                listing.price = storage.yaml().getDouble(path + ".price");
                String bidder = storage.yaml().getString(path + ".highest-bidder");
                listing.highestBidder = bidder == null ? null : UUID.fromString(bidder);
                listing.highestBidderName = storage.yaml().getString(path + ".highest-bidder-name");
                listing.highestBid = storage.yaml().getDouble(path + ".highest-bid");
                listing.listed = storage.yaml().getLong(path + ".listed");
                listing.expires = storage.yaml().getLong(path + ".expires");
                listing.claimDeadline = storage.yaml().getLong(path + ".claim-deadline");
                listings.put(listing.id, listing);
            } catch (Exception ignored) {
                // corrupted entry
            }
        }
        var expiredSection = storage.yaml().getConfigurationSection("expired");
        if (expiredSection != null) {
            for (String ownerKey : expiredSection.getKeys(false)) {
                try {
                    UUID owner = UUID.fromString(ownerKey);
                    List<Listing> list = new ArrayList<>();
                    for (String encoded : storage.yaml().getStringList("expired." + ownerKey)) {
                        Listing listing = new Listing();
                        listing.id = UUID.randomUUID();
                        listing.seller = owner;
                        listing.item = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
                        listing.type = Type.SELL;
                        list.add(listing);
                    }
                    expired.put(owner, list);
                } catch (Exception ignored) {
                    // corrupted entry
                }
            }
        }
    }

    private void save() {
        storage.yaml().set("items", null);
        listings.forEach((id, listing) -> {
            String path = "items." + id;
            storage.yaml().set(path + ".type", listing.type.name());
            storage.yaml().set(path + ".seller", listing.seller.toString());
            storage.yaml().set(path + ".seller-name", listing.sellerName);
            storage.yaml().set(path + ".item", Base64.getEncoder().encodeToString(listing.item.serializeAsBytes()));
            storage.yaml().set(path + ".price", listing.price);
            storage.yaml().set(path + ".highest-bidder", listing.highestBidder == null ? null : listing.highestBidder.toString());
            storage.yaml().set(path + ".highest-bidder-name", listing.highestBidderName);
            storage.yaml().set(path + ".highest-bid", listing.highestBid);
            storage.yaml().set(path + ".listed", listing.listed);
            storage.yaml().set(path + ".expires", listing.expires);
            storage.yaml().set(path + ".claim-deadline", listing.claimDeadline);
        });
        storage.yaml().set("expired", null);
        expired.forEach((owner, list) -> {
            List<String> encoded = new ArrayList<>();
            for (Listing listing : list) {
                encoded.add(Base64.getEncoder().encodeToString(listing.item.serializeAsBytes()));
            }
            storage.yaml().set("expired." + owner, encoded);
        });
        storage.save();
    }

    /* ------------------------------------------------------------------ */
    /* help                                                               */
    /* ------------------------------------------------------------------ */

    private long duration(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        long total = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+)\\s*([dhms])", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(raw.replace(" ", ""));
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            total += switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
                case "d" -> value * 86_400_000L;
                case "h" -> value * 3_600_000L;
                case "m" -> value * 60_000L;
                default -> value * 1000L;
            };
        }
        return total <= 0 ? fallback : total;
    }

    private Economy economy() {
        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private List<Listing> activeOfType(Type type) {
        return listings.values().stream()
                .filter(listing -> listing.type == type)
                .sorted(Comparator.comparingLong(listing -> listing.listed))
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /* listing / buying (CrazyManager)                                    */
    /* ------------------------------------------------------------------ */

    private void createListing(Player player, Type type, double price) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(msgComponent("no-item"));
            return;
        }
        if (cfgBool("settings.disallow-damaged-items", true) && item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable
                && damageable.hasDamage()) {
            player.sendMessage(msgComponent("damaged-item"));
            return;
        }
        double min = cfgDouble(type == Type.SELL ? "settings.minimum-sell-price" : "settings.minimum-bid-price", 10);
        double max = cfgDouble(type == Type.SELL ? "settings.max-sell-price" : "settings.max-bid-price", 1_000_000);
        if (price < min) {
            player.sendMessage(msgComponent("price-too-low", "min", String.valueOf(min)));
            return;
        }
        if (price > max) {
            player.sendMessage(msgComponent("price-too-high", "max", String.valueOf(max)));
            return;
        }
        if (type == Type.SELL && !cfgBool("settings.feature-toggle.selling", true)) {
            player.sendMessage(msgComponent("selling-disabled"));
            return;
        }
        if (type == Type.BID && !cfgBool("settings.feature-toggle.bidding", true)) {
            player.sendMessage(msgComponent("bidding-disabled"));
            return;
        }
        Listing listing = new Listing();
        listing.id = UUID.randomUUID();
        listing.type = type;
        listing.seller = player.getUniqueId();
        listing.sellerName = player.getName();
        listing.item = item.clone();
        listing.price = price;
        listing.listed = System.currentTimeMillis();
        long duration = duration(cfgString(type == Type.SELL ? "settings.sell-time" : "settings.bid-time",
                type == Type.SELL ? "2d" : "2m30s"), type == Type.SELL ? 172_800_000L : 150_000L);
        listing.expires = listing.listed + duration;
        player.getInventory().setItemInMainHand(null);
        listings.put(listing.id, listing);
        save();
        player.sendMessage(msgComponent(type == Type.SELL ? "listed-sell" : "listed-bid",
                "item", itemName(listing.item), "price", String.valueOf(price), "time", formatDuration(duration)));
        if (cfgBool("settings.broadcast-listing", true)) {
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                if (!online.getUniqueId().equals(player.getUniqueId())) {
                    online.sendMessage(msgComponent("broadcast-listing",
                            "player", player.getName(), "item", itemName(listing.item), "price", String.valueOf(price)));
                }
            }
        }
    }

    private void buy(Player buyer, Listing listing) {
        if (listing == null) {
            buyer.sendMessage(msgComponent("listing-gone"));
            return;
        }
        if (listing.seller.equals(buyer.getUniqueId())) {
            buyer.sendMessage(msgComponent("own-listing"));
            return;
        }
        if (listing.type != Type.SELL) {
            bid(buyer, listing, listing.highestBid > 0
                    ? listing.highestBid + cfgDouble("settings.bid-increment", 1)
                    : listing.price);
            return;
        }
        Economy economy = economy();
        if (economy == null) {
            buyer.sendMessage(msgComponent("no-economy"));
            return;
        }
        if (!economy.has(buyer, listing.price)) {
            buyer.sendMessage(msgComponent("not-enough-money", "price", String.valueOf(listing.price)));
            return;
        }
        economy.withdrawPlayer(buyer, listing.price);
        economy.depositPlayer(plugin.getServer().getOfflinePlayer(listing.seller), listing.price);
        listings.remove(listing.id);
        save();
        giveItem(buyer, listing.item);
        buyer.sendMessage(msgComponent("bought-item",
                "item", itemName(listing.item), "price", String.valueOf(listing.price)));
        Player sellerOnline = plugin.getServer().getPlayer(listing.seller);
        if (sellerOnline != null) {
            sellerOnline.sendMessage(msgComponent("item-sold",
                    "item", itemName(listing.item), "price", String.valueOf(listing.price), "buyer", buyer.getName()));
        }
        playSound(buyer, cfgString("settings.sold-item-sound", "UI_BUTTON_CLICK"));
    }

    private void bid(Player bidder, Listing listing, double amount) {
        if (listing == null || listing.type != Type.BID) {
            bidder.sendMessage(msgComponent("listing-gone"));
            return;
        }
        if (listing.seller.equals(bidder.getUniqueId())) {
            bidder.sendMessage(msgComponent("own-listing"));
            return;
        }
        Economy economy = economy();
        if (economy == null) {
            bidder.sendMessage(msgComponent("no-economy"));
            return;
        }
        if (!economy.has(bidder, amount)) {
            bidder.sendMessage(msgComponent("not-enough-money", "price", String.valueOf(amount)));
            return;
        }
        economy.withdrawPlayer(bidder, amount);
        if (listing.highestBidder != null) {
            economy.depositPlayer(plugin.getServer().getOfflinePlayer(listing.highestBidder), listing.highestBid);
            Player previous = plugin.getServer().getPlayer(listing.highestBidder);
            if (previous != null) {
                previous.sendMessage(msgComponent("outbid",
                        "item", itemName(listing.item), "price", String.valueOf(amount)));
            }
        }
        listing.highestBid = amount;
        listing.highestBidder = bidder.getUniqueId();
        listing.highestBidderName = bidder.getName();
        save();
        bidder.sendMessage(msgComponent("bid-placed",
                "item", itemName(listing.item), "price", String.valueOf(amount)));
        playSound(bidder, cfgString("settings.bid-sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
    }

    private void expireListings() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Listing listing : new ArrayList<>(listings.values())) {
            if (listing.expires > now) {
                continue;
            }
            changed = true;
            listings.remove(listing.id);
            if (listing.type == Type.BID && listing.highestBidder != null) {
                // winner claims the item, the seller gets the money (already held)
                Economy economy = economy();
                if (economy != null) {
                    economy.depositPlayer(plugin.getServer().getOfflinePlayer(listing.seller), listing.highestBid);
                }
                listing.claimDeadline = now + duration(cfgString("settings.bid-winner-time", "20d"), 1_728_000_000L);
                expired.computeIfAbsent(listing.highestBidder, k -> new ArrayList<>()).add(listing);
                Player winner = plugin.getServer().getPlayer(listing.highestBidder);
                if (winner != null) {
                    winner.sendMessage(msgComponent("bid-won",
                            "item", itemName(listing.item), "price", String.valueOf(listing.highestBid)));
                }
                Player seller = plugin.getServer().getPlayer(listing.seller);
                if (seller != null) {
                    seller.sendMessage(msgComponent("bid-sold",
                            "item", itemName(listing.item), "price", String.valueOf(listing.highestBid)));
                }
            } else {
                expired.computeIfAbsent(listing.seller, k -> new ArrayList<>()).add(listing);
                Player seller = plugin.getServer().getPlayer(listing.seller);
                if (seller != null) {
                    seller.sendMessage(msgComponent("listing-expired",
                            "item", itemName(listing.item), "price", String.valueOf(listing.price)));
                }
            }
        }
        if (changed) {
            save();
        }
    }

    private void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
        for (ItemStack rest : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), rest);
        }
    }

    private void playSound(Player player, String raw) {
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(raw.toUpperCase(Locale.ROOT)), 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // unknown sound
        }
    }

    private String itemName(ItemStack item) {
        if (item == null) {
            return "?";
        }
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            return Text.plain(item.getItemMeta().displayName());
        }
        return item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        StringBuilder out = new StringBuilder();
        if (days > 0) {
            out.append(days).append("d ");
        }
        if (hours > 0) {
            out.append(hours).append("h ");
        }
        if (minutes > 0) {
            out.append(minutes).append("m ");
        }
        if (secs > 0 || out.isEmpty()) {
            out.append(secs).append("s");
        }
        return out.toString().trim();
    }

    /* ------------------------------------------------------------------ */
    /* GUI                                                                */
    /* ------------------------------------------------------------------ */

    private void openBrowse(Player player, Type type, int page) {
        List<Listing> list = activeOfType(type);
        int perPage = 45;
        int pages = Math.max(1, (list.size() + perPage - 1) / perPage);
        int current = Math.max(0, Math.min(page, pages - 1));
        String title = cfgString("settings.gui-name", "&4Auction House&8")
                + " - " + (type == Type.SELL ? "Sell" : "Bid") + " " + (current + 1) + "/" + pages;
        Inventory inventory = Bukkit.createInventory(null, 54, Text.parse(title));
        List<UUID> ids = new ArrayList<>();
        int index = 0;
        for (int i = current * perPage; i < Math.min(list.size(), (current + 1) * perPage); i++) {
            Listing listing = list.get(i);
            ItemStack display = listing.item.clone();
            if (display.getItemMeta() != null) {
                ItemMeta meta = display.getItemMeta();
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                List<String> template = cfgStringList(type == Type.SELL
                        ? "settings.gui-settings.selling-item-lore"
                        : "settings.gui-settings.bidding-item-lore");
                if (template.isEmpty()) {
                    template = List.of("&7-------------------------",
                            type == Type.SELL ? "&aClick here to purchase." : "&aClick here to bid.",
                            "", "&9Price: &e$%price%", "&9Seller: &e%seller%",
                            "&9Time Left: &e%time%", "&7-------------------------");
                }
                for (String line : template) {
                    lore.add(Text.parse(line
                            .replace("%price%", String.valueOf(listing.highestBid > 0 ? listing.highestBid : listing.price))
                            .replace("%seller%", listing.sellerName)
                            .replace("%time%", formatDuration(Math.max(0, listing.expires - System.currentTimeMillis())))));
                }
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(index++, display);
            ids.add(listing.id);
        }
        // navigation
        if (current > 0) {
            inventory.setItem(45, button(Material.ARROW, "<gold>Previous page"));
        }
        if (current + 1 < pages) {
            inventory.setItem(53, button(Material.ARROW, "<gold>Next page"));
        }
        inventory.setItem(49, button(Material.CHEST, "<gold>" + (type == Type.SELL ? "Sell listings" : "Bid listings")));
        openMenus.put(player.getUniqueId(), ids);
        openModes.put(player.getUniqueId(), "browse-" + type.name() + "-" + current);
        player.openInventory(inventory);
    }

    private ItemStack button(Material material, String name) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Text.parse(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void openOwnListings(Player player) {
        List<Listing> own = listings.values().stream()
                .filter(listing -> listing.seller.equals(player.getUniqueId()))
                .sorted(Comparator.comparingLong(listing -> listing.listed))
                .toList();
        Inventory inventory = Bukkit.createInventory(null, 54,
                Text.parse(cfgString("settings.players-current-items", "&8Your Current Listings")));
        List<UUID> ids = new ArrayList<>();
        int index = 0;
        for (Listing listing : own) {
            if (index >= 45) {
                break;
            }
            ItemStack display = listing.item.clone();
            if (display.getItemMeta() != null) {
                ItemMeta meta = display.getItemMeta();
                List<String> template = cfgStringList("settings.gui-settings.current-lore");
                if (template.isEmpty()) {
                    template = List.of("&7-------------------------", "&aClick here to cancel.",
                            "", "&9Price: &e$%price%", "&7-------------------------");
                }
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                for (String line : template) {
                    lore.add(Text.parse(line
                            .replace("%price%", String.valueOf(listing.highestBid > 0 ? listing.highestBid : listing.price))
                            .replace("%time%", formatDuration(Math.max(0, listing.expires - System.currentTimeMillis())))));
                }
                meta.lore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(index++, display);
            ids.add(listing.id);
        }
        openMenus.put(player.getUniqueId(), ids);
        openModes.put(player.getUniqueId(), "own");
        player.openInventory(inventory);
    }

    private void openExpired(Player player) {
        List<Listing> own = expired.getOrDefault(player.getUniqueId(), List.of());
        Inventory inventory = Bukkit.createInventory(null, 54,
                Text.parse(cfgString("settings.cancelled-expired-items", "&8Canceled/Expired Listings")));
        List<UUID> ids = new ArrayList<>();
        int index = 0;
        for (Listing listing : own) {
            if (index >= 54) {
                break;
            }
            inventory.setItem(index++, listing.item.clone());
            ids.add(listing.id);
        }
        openMenus.put(player.getUniqueId(), ids);
        openModes.put(player.getUniqueId(), "expired");
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String mode = openModes.get(player.getUniqueId());
        if (mode == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        List<UUID> ids = openMenus.getOrDefault(player.getUniqueId(), List.of());
        int slot = event.getSlot();
        if (mode.startsWith("browse-")) {
            Type type = mode.contains("BID") ? Type.BID : Type.SELL;
            if (slot == 45 || slot == 53) {
                int page = Integer.parseInt(mode.substring(mode.lastIndexOf('-') + 1));
                openBrowse(player, type, slot == 45 ? page - 1 : page + 1);
                return;
            }
            if (slot < ids.size()) {
                Listing listing = listings.get(ids.get(slot));
                if (listing == null) {
                    player.sendMessage(msgComponent("listing-gone"));
                    player.closeInventory();
                    return;
                }
                if (listing.type == Type.SELL) {
                    buy(player, listing);
                } else {
                    double next = listing.highestBid > 0
                            ? listing.highestBid + cfgDouble("settings.bid-increment", 1)
                            : listing.price;
                    bid(player, listing, next);
                }
                player.closeInventory();
            }
            return;
        }
        if (mode.equals("own")) {
            if (slot < ids.size()) {
                Listing listing = listings.remove(ids.get(slot));
                if (listing != null) {
                    List<Listing> ownExpired = expired.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
                    ownExpired.add(listing);
                    if (listing.type == Type.BID && listing.highestBidder != null) {
                        Economy economy = economy();
                        if (economy != null) {
                            economy.depositPlayer(plugin.getServer().getOfflinePlayer(listing.highestBidder), listing.highestBid);
                        }
                    }
                    save();
                    player.sendMessage(msgComponent("listing-cancelled", "item", itemName(listing.item)));
                    player.closeInventory();
                }
            }
            return;
        }
        if (mode.equals("expired")) {
            List<Listing> ownExpired = expired.get(player.getUniqueId());
            if (ownExpired == null) {
                return;
            }
            if (slot < ownExpired.size()) {
                Listing listing = ownExpired.remove(slot);
                giveItem(player, listing.item);
                if (ownExpired.isEmpty()) {
                    expired.remove(player.getUniqueId());
                }
                save();
                player.sendMessage(msgComponent("collected", "item", itemName(listing.item)));
                player.closeInventory();
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* command (AuctionCommand)                                            */
    /* ------------------------------------------------------------------ */

    private final class AuctionCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull org.bukkit.command.Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length == 0) {
                openBrowse(player, Type.SELL, 0);
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "sell", "bid" -> {
                    Type type = args[0].equalsIgnoreCase("sell") ? Type.SELL : Type.BID;
                    if (args.length < 2) {
                        player.sendMessage(msgComponent("usage"));
                        return true;
                    }
                    try {
                        createListing(player, type, Double.parseDouble(args[1]));
                    } catch (NumberFormatException e) {
                        player.sendMessage(msgComponent("invalid-price"));
                    }
                }
                case "view", "browse" -> openBrowse(player, args.length > 1 && args[1].equalsIgnoreCase("bid")
                        ? Type.BID : Type.SELL, 0);
                case "listed" -> openOwnListings(player);
                case "expired", "collect" -> openExpired(player);
                case "help" -> {
                    player.sendMessage(msgComponent("help-header"));
                    for (String line : cfgStringList("messages.help")) {
                        player.sendMessage(Text.parse(line));
                    }
                }
                case "reload" -> {
                    if (!player.hasPermission("samplese.auction.admin")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    reloadConfig();
                    player.sendMessage(msgComponent("reloaded"));
                }
                case "force_end_all" -> {
                    if (!player.hasPermission("samplese.auction.admin")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    int count = 0;
                    for (Listing listing : new ArrayList<>(listings.values())) {
                        listings.remove(listing.id);
                        expired.computeIfAbsent(listing.seller, k -> new ArrayList<>()).add(listing);
                        count++;
                    }
                    save();
                    player.sendMessage(msgComponent("force-ended", "count", String.valueOf(count)));
                }
                default -> player.sendMessage(msgComponent("usage"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                          @NotNull org.bukkit.command.Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("sell", "bid", "view", "listed", "expired", "help", "reload", "force_end_all")
                        .stream().filter(option -> option.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            if (args.length == 2 && (args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("bid"))) {
                return List.of("10", "100", "1000", "10000");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
                return List.of("sell", "bid");
            }
            return List.of();
        }
    }
}
