package su.samplecraft.essentials.modules.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.modules.social.PlayerDirectory;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code AsBounty-1.0.0.jar} (com.itzazeem.asbounty.AsBounty).
 * <p>
 * Ported 1:1: {@code /bounty} opens the paged bounty GUI (sorting: newest,
 * oldest, highest_price, lowest_price, search), {@code /bounty add <player>
 * <amount>} with the {@code k/m/b/t} abbreviations, the configurable min/max
 * limits, Vault payout to the killer and the bounty-added notifications
 * (chat, actionbar, sound). Data lives in {@code economy/bounty/data/bounties.yml}.
 */
public final class BountyModule extends EssentialsModule implements Listener {

    private ModuleStorage storage;
    private PlayerDirectory directory;

    /** viewer -> page */
    private final Map<UUID, Integer> pages = new HashMap<>();
    /** viewer -> current sort key */
    private final Map<UUID, String> sorts = new HashMap<>();

    public BountyModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("economy", "bounty", "AsBounty-1.0.0.jar", "Bounty GUI with sorting, search and Vault payouts");
    }

    @Override
    protected void onEnable() {
        storage = new ModuleStorage(plugin, getDataFolder(), "bounties.yml");
        directory = new PlayerDirectory(plugin);
        ensurePermission("samplese.bounty", "Use bounties", org.bukkit.permissions.PermissionDefault.TRUE);
        command("bounty", new BountyCommand());
        onDisableHook(() -> {
            unbindCommands();
            storage.save();
        });
        listen(this);
    }

    private Economy economy() {
        RegisteredServiceProvider<Economy> registration =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    /* ------------------------------------------------------------------ */
    /* data                                                                */
    /* ------------------------------------------------------------------ */

    private double bounty(UUID target) {
        return storage.yaml().getDouble("targets." + target + ".amount", 0.0);
    }

    private long timestamp(UUID target) {
        return storage.yaml().getLong("targets." + target + ".timestamp", 0L);
    }

    private String targetName(UUID target) {
        String stored = storage.yaml().getString("targets." + target + ".name");
        if (stored != null) {
            return stored;
        }
        String known = directory.nameOf(target);
        return known == null ? target.toString().substring(0, 8) : known;
    }

    private void addBounty(UUID target, String name, double amount) {
        String path = "targets." + target;
        storage.yaml().set(path + ".amount", bounty(target) + amount);
        storage.yaml().set(path + ".name", name);
        storage.yaml().set(path + ".timestamp", System.currentTimeMillis());
        storage.saveLater();
    }

    private double claim(UUID target) {
        double total = bounty(target);
        if (total > 0) {
            storage.yaml().set("targets." + target, null);
            storage.saveLater();
        }
        return total;
    }

    private List<Map.Entry<UUID, Double>> sorted(String sortBy) {
        ConfigurationSection section = storage.yaml().getConfigurationSection("targets");
        List<Map.Entry<UUID, Double>> entries = new ArrayList<>();
        if (section == null) {
            return entries;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                double amount = section.getDouble(key + ".amount", 0.0);
                if (amount > 0) {
                    entries.add(Map.entry(id, amount));
                }
            } catch (IllegalArgumentException ignored) {
                // stale key
            }
        }
        Comparator<Map.Entry<UUID, Double>> comparator = switch (sortBy == null ? "highest_price" : sortBy) {
            case "oldest" -> Comparator.comparing(e -> timestamp(e.getKey()));
            case "newest" -> Comparator.comparing((Map.Entry<UUID, Double> e) -> timestamp(e.getKey())).reversed();
            case "lowest_price" -> Map.Entry.comparingByValue();
            default -> Map.Entry.<UUID, Double>comparingByValue().reversed();
        };
        entries.sort(comparator);
        return entries;
    }

    /* ------------------------------------------------------------------ */
    /* amount parsing with k/m/b/t (original parseAmountWithAbbreviation)   */
    /* ------------------------------------------------------------------ */

    private double parseAmount(String input) {
        String upper = input.toUpperCase(Locale.ROOT);
        double multiplier = 1.0;
        String numberPart = input;
        for (String format : List.of("K", "M", "B", "T")) {
            if (upper.endsWith(format)) {
                multiplier = switch (format) {
                    case "K" -> 1_000.0;
                    case "M" -> 1_000_000.0;
                    case "B" -> 1_000_000_000.0;
                    default -> 1_000_000_000_000.0;
                };
                numberPart = input.substring(0, input.length() - 1);
                break;
            }
        }
        return Double.parseDouble(numberPart.trim()) * multiplier;
    }

    /* ------------------------------------------------------------------ */
    /* /bounty                                                             */
    /* ------------------------------------------------------------------ */

    private final class BountyCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("samplese.bounty")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    openGui(player, pages.getOrDefault(player.getUniqueId(), 0),
                            sorts.getOrDefault(player.getUniqueId(), "highest_price"), null);
                } else {
                    sender.sendMessage(plugin.getLang().component("players-only"));
                }
                return true;
            }
            if (!args[0].equalsIgnoreCase("add")) {
                if (sender instanceof Player player) {
                    openGui(player, 0, sorts.getOrDefault(player.getUniqueId(), "highest_price"), null);
                } else {
                    sender.sendMessage(plugin.getLang().component("players-only"));
                }
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.bounty")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(msgComponent("usage", "label", label));
                return true;
            }
            double amount;
            try {
                amount = parseAmount(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(msgComponent("invalid-amount"));
                return true;
            }
            double min = cfgDouble("limits.min-amount", 10.0);
            double max = cfgDouble("limits.max-amount", 1_000_000_000.0);
            if (amount < min) {
                player.sendMessage(msgComponent("below-min", "min", formatAmount(min)));
                return true;
            }
            if (amount > max) {
                player.sendMessage(msgComponent("above-max", "max", formatAmount(max)));
                return true;
            }
            PlayerDirectory.Entry target = directory.find(args[1]);
            if (target == null) {
                OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (offline == null || !offline.hasPlayedBefore()) {
                    player.sendMessage(msgComponent("player-not-found"));
                    return true;
                }
                target = new PlayerDirectory.Entry(offline.getUniqueId(),
                        offline.getName() == null ? args[1] : offline.getName());
            }
            if (target.id().equals(player.getUniqueId())) {
                player.sendMessage(msgComponent("cannot-self"));
                return true;
            }
            Economy economy = economy();
            if (economy == null) {
                player.sendMessage(msgComponent("no-economy"));
                return true;
            }
            if (!economy.has(player, amount)) {
                player.sendMessage(msgComponent("not-enough-balance"));
                return true;
            }
            economy.withdrawPlayer(player, amount);
            addBounty(target.id(), target.name(), amount);

            player.sendMessage(msgComponent("bounty-added",
                    "amount", formatAmount(amount), "target", target.name()));
            Player onlineTarget = plugin.getServer().getPlayer(target.id());
            if (onlineTarget != null) {
                onlineTarget.sendMessage(msgComponent("bounty-added-on-you",
                        "user", player.getName(), "amount", formatAmount(amount)));
                onlineTarget.sendActionBar(msgComponent("bounty-added-on-you",
                        "user", player.getName(), "amount", formatAmount(amount)));
            }
            playSound(player, cfgString("sounds.bounty-added.sound", "ENTITY_PLAYER_LEVELUP"));
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("add");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
                return List.of("10", "100", "1k", "10k", "100k", "1m");
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* GUI                                                                 */
    /* ------------------------------------------------------------------ */

    private void openGui(Player player, int page, String sortBy, String search) {
        List<Map.Entry<UUID, Double>> entries = sorted(sortBy).stream()
                .filter(e -> search == null || targetName(e.getKey()).toLowerCase(Locale.ROOT)
                        .contains(search.toLowerCase(Locale.ROOT)))
                .toList();
        int perPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) entries.size() / perPage));
        int current = Math.max(0, Math.min(page, totalPages - 1));
        String title = cfgString("gui.title", "&8Bounties (Page %page%/%max%)")
                .replace("%page%", String.valueOf(current + 1))
                .replace("%max%", String.valueOf(totalPages));
        Inventory inventory = Bukkit.createInventory(null, 54, Text.parse(title));
        int start = current * perPage;
        for (int i = start; i < Math.min(start + perPage, entries.size()); i++) {
            Map.Entry<UUID, Double> entry = entries.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            if (head.getItemMeta() instanceof SkullMeta meta) {
                meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(entry.getKey()));
                head.setItemMeta(meta);
            }
            if (head.getItemMeta() != null) {
                ItemMeta meta = head.getItemMeta();
                meta.displayName(Text.parse(cfgString("gui.head-name", "&#00fc88%name%")
                        .replace("%name%", targetName(entry.getKey()))));
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                for (String line : cfgStringList("gui.head-lore")) {
                    lore.add(Text.parse(line
                            .replace("%bounty%", formatAmount(entry.getValue()))
                            .replace("%name%", targetName(entry.getKey()))));
                }
                meta.lore(lore);
                head.setItemMeta(meta);
            }
            inventory.setItem(i - start, head);
        }
        inventory.setItem(45, icon(Material.ARROW, "&#00fc88PREVIOUS"));
        inventory.setItem(53, icon(Material.ARROW, "&#00fc88NEXT"));
        inventory.setItem(48, icon(Material.HOPPER, "&#00fc88SORT: " + sortBy.toUpperCase(Locale.ROOT)));
        inventory.setItem(49, icon(Material.SKELETON_SKULL, "&#00fc88AsBounty"));
        inventory.setItem(50, icon(Material.OAK_SIGN, "&#00fc88SEARCH"));
        pages.put(player.getUniqueId(), current);
        sorts.put(player.getUniqueId(), sortBy);
        player.openInventory(inventory);
    }

    private ItemStack icon(Material material, String name) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Text.parse(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !pages.containsKey(player.getUniqueId())) {
            return;
        }
        if (event.getView().getTopInventory() != event.getClickedInventory()) {
            return;
        }
        event.setCancelled(true);
        String sort = sorts.getOrDefault(player.getUniqueId(), "highest_price");
        int page = pages.getOrDefault(player.getUniqueId(), 0);
        switch (event.getSlot()) {
            case 45 -> openGui(player, Math.max(0, page - 1), sort, null);
            case 53 -> openGui(player, page + 1, sort, null);
            case 48 -> {
                List<String> keys = List.of("newest", "oldest", "highest_price", "lowest_price");
                String next = keys.get((keys.indexOf(sort) + 1) % keys.size());
                openGui(player, 0, next, null);
            }
            case 49 -> openGui(player, page, sort, null);
            case 50 -> {
                player.closeInventory();
                player.sendMessage(msgComponent("search-prompt"));
                pendingSearch.add(player.getUniqueId());
            }
            default -> {
                // head click: no action, kept for parity with the original GUI
            }
        }
        playSound(player, cfgString("sounds.gui-click.sound", "UI_BUTTON_CLICK"));
    }

    private final java.util.Set<UUID> pendingSearch = new java.util.HashSet<>();

    @EventHandler(ignoreCancelled = true)
    public void onSearchChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingSearch.contains(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        pendingSearch.remove(player.getUniqueId());
        String query = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () -> openGui(player, 0,
                sorts.getOrDefault(player.getUniqueId(), "highest_price"),
                query.equalsIgnoreCase("cancel") ? null : query));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        double bounty = bounty(victim.getUniqueId());
        if (bounty <= 0) {
            return;
        }
        if (killer.equals(victim) && !cfgBool("if-kill-myself-get-reward", false)) {
            return;
        }
        double claimed = claim(victim.getUniqueId());
        if (claimed <= 0) {
            return;
        }
        Economy economy = economy();
        if (economy != null) {
            economy.depositPlayer(killer, claimed);
        }
        killer.sendMessage(msgComponent("claimed", "victim", victim.getName(), "amount", formatAmount(claimed)));
        killer.sendActionBar(msgComponent("claimed", "victim", victim.getName(), "amount", formatAmount(claimed)));
        playSound(killer, cfgString("sounds.bounty-added.sound", "ENTITY_PLAYER_LEVELUP"));
    }

    private void playSound(Player player, String name) {
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(name.toUpperCase(Locale.ROOT)), 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // unknown sound in config
        }
    }

    private String formatAmount(double amount) {
        String format = cfgString("economy.currency-format", "#,##0.##");
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat(format);
        return decimalFormat.format(amount);
    }
}
