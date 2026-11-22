package su.samplecraft.essentials.modules.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Merged from {@code AxRankMenu-1.17.0.jar} (com.artillexstudios.axrankmenu).
 * <p>
 * Rank shop GUI. Kept from the original:
 * <ul>
 *   <li>ranks defined in {@code ranks.yml} with price, currency, slot, icon and lore,</li>
 *   <li>{@code %price%}, {@code %name%}, {@code %rank%} placeholders inside item lore,</li>
 *   <li>buy-actions ({@code [MESSAGE]}, {@code [CONSOLE]}, {@code [PLAYER]}, {@code [CLOSE]}),</li>
 *   <li>{@code prevent-downgrading} through weighted ranks and {@code discount-ranks}
 *       (price difference to the owned rank),</li>
 *   <li>Vault currency (the original's other currency hooks are not part of this
 *       server stack; rank permission changes still go through LuckPerms commands
 *       configured as buy-actions, exactly like the original).</li>
 * </ul>
 * Commands: {@code /rankmenu} (aliases {@code rank}, {@code ranks}, {@code axrankmenu}).
 */
public final class RankMenuModule extends EssentialsModule implements Listener {

    private static final class Rank {
        String key;
        String name;
        String rank;
        double price;
        int slot;
        int weight;
        String currency = "Vault";
        List<String> lore = new ArrayList<>();
        List<String> buyActions = new ArrayList<>();
        Material material = Material.PAPER;
        String displayName;
    }

    /** Holder used to recognise our GUI clicks. */
    private static final class RankHolder implements InventoryHolder {
        Inventory inventory;

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final RankHolder holder = new RankHolder();

    public RankMenuModule(SamplesEssentials plugin) {
        super("economy", "rankmenu", "AxRankMenu-1.17.0.jar",
                "Rank shop GUI with weighted ranks, discounts and configurable buy actions");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rankmenu", "Open the rank menu", PermissionDefault.TRUE);
        ensurePermission("samplese.rankmenu.admin", "Reload the rank menu", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDefaultFile("ranks.yml");
        loadRanks();
        command("rankmenu", new RankCommand());
        command("rank", new RankCommand());
        command("ranks", new RankCommand());
        command("axrankmenu", new RankCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    private void loadRanks() {
        ranks.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                new File(getModuleFolder(), "ranks.yml"));
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null || key.equals("title") || key.equals("rows") || key.equals("type")) {
                continue;
            }
            Rank rank = new Rank();
            rank.key = key;
            rank.rank = section.getString("rank", key);
            rank.name = section.getString("display-name", rank.rank);
            rank.price = section.getDouble("price", 0);
            rank.currency = section.getString("currency", "Vault");
            rank.slot = section.getInt("slot", 0);
            rank.weight = section.getInt("weight", 0);
            ConfigurationSection item = section.getConfigurationSection("item");
            if (item != null) {
                Material material = Material.matchMaterial(item.getString("type", "PAPER"));
                rank.material = material == null ? Material.PAPER : material;
                rank.displayName = item.getString("name", rank.name);
                rank.lore = item.getStringList("lore");
            }
            rank.buyActions = section.getStringList("buy-actions");
            ranks.put(key.toLowerCase(Locale.ROOT), rank);
        }
        plugin.getLogger().info("Rank menu ranks loaded: " + ranks.size());
    }

    private double price(Rank rank, Player player) {
        boolean discount = cfgBool("discount-ranks", false);
        if (!discount) {
            return rank.price;
        }
        int ownedWeight = ownedWeight(player);
        if (ownedWeight <= 0) {
            return rank.price;
        }
        Rank best = null;
        for (Rank candidate : ranks.values()) {
            if (candidate.weight == ownedWeight
                    && player.hasPermission("group." + candidate.rank)) {
                best = candidate;
            }
        }
        double base = best == null ? 0 : best.price;
        return Math.max(0, rank.price - base);
    }

    private int ownedWeight(Player player) {
        int highest = 0;
        for (Rank rank : ranks.values()) {
            if (player.hasPermission("group." + rank.rank)) {
                highest = Math.max(highest, rank.weight);
            }
        }
        return highest;
    }

    private ItemStack icon(Rank rank, Player player) {
        ItemStack item = new ItemStack(rank.material);
        ItemMeta meta = item.getItemMeta();
        double cost = price(rank, player);
        String display = rank.displayName == null ? rank.name : rank.displayName;
        meta.displayName(Text.parse(display
                .replace("%name%", rank.name)
                .replace("%rank%", rank.rank)));
        List<Component> lore = new ArrayList<>();
        for (String line : rank.lore) {
            lore.add(Text.parse(line
                    .replace("%price%", format(cost))
                    .replace("%name%", rank.name)
                    .replace("%rank%", rank.rank)));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Inventory build(Player player) {
        String title = cfgString("title", "&0&lʀᴀɴᴋs");
        int rows = Math.min(6, Math.max(1, cfgInt("rows", 3)));
        Inventory inventory = plugin.getServer().createInventory(holder, rows * 9, Text.parse(title));
        holder.inventory = inventory;
        for (Rank rank : ranks.values()) {
            if (rank.slot < 0 || rank.slot >= inventory.getSize()) {
                continue;
            }
            inventory.setItem(rank.slot, icon(rank, player));
        }
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RankHolder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof RankHolder)) {
            return;
        }
        int slot = event.getSlot();
        for (Rank rank : ranks.values()) {
            if (rank.slot == slot) {
                buy(player, rank);
                return;
            }
        }
    }

    private void buy(Player player, Rank rank) {
        if (!rank.currency.equalsIgnoreCase("Vault")) {
            reply(player, "unsupported-currency", "currency", rank.currency);
            return;
        }
        int ownedWeight = ownedWeight(player);
        if (cfgBool("prevent-downgrading", true) && rank.weight > 0
                && ownedWeight > rank.weight) {
            reply(player, "downgrade-disabled");
            return;
        }
        if (player.hasPermission("group." + rank.rank)) {
            reply(player, "already-owned", "rank", rank.name);
            return;
        }
        double cost = price(rank, player);
        var economy = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economy != null && economy.getProvider().getBalance(player) < cost) {
            reply(player, "no-currency", "price", format(cost));
            return;
        }
        if (economy != null) {
            economy.getProvider().withdrawPlayer(player, cost);
        }
        if (rank.buyActions.isEmpty()) {
            reply(player, "purchased", "rank", rank.name);
            return;
        }
        for (String action : rank.buyActions) {
            String parsed = action
                    .replace("%player%", player.getName())
                    .replace("%name%", rank.name)
                    .replace("%rank%", rank.rank)
                    .replace("%price%", format(cost));
            if (parsed.startsWith("[MESSAGE]")) {
                player.sendMessage(Text.parse(parsed.substring("[MESSAGE]".length()).trim()));
            } else if (parsed.startsWith("[PLAYER]")) {
                player.performCommand(parsed.substring("[PLAYER]".length()).trim());
            } else if (parsed.startsWith("[CONSOLE]")) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        parsed.substring("[CONSOLE]".length()).trim());
            } else if (parsed.startsWith("[CLOSE]")) {
                player.closeInventory();
            } else {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
            }
        }
        reply(player, "purchased", "rank", rank.name);
    }

    private static String format(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.2f", value);
    }

    private final class RankCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("samplese.rankmenu.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    reloadConfig();
                    loadRanks();
                    reply(sender, "reloaded");
                    return true;
                }
                reply(sender, "player-only");
                return true;
            }
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("samplese.rankmenu.admin")) {
                    reply(player, "no-permission");
                    return true;
                }
                reloadConfig();
                loadRanks();
                reply(player, "reloaded");
                return true;
            }
            if (ranks.isEmpty()) {
                reply(player, "no-ranks");
                return true;
            }
            player.openInventory(build(player));
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1 && sender.hasPermission("samplese.rankmenu.admin")) {
                return List.of("reload");
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
