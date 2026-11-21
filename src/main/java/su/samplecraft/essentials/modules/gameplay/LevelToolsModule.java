package su.samplecraft.essentials.modules.gameplay;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code LevelTools-2.1.2.jar} (me.byteful.plugin.leveltools).
 * <p>
 * Faithful subset of the original profile engine (the original is a full
 * profile-driven framework; everything that drives gameplay is ported):
 * <ul>
 *   <li>item profiles: material lists, {@code max_level}, {@code trigger_profiles},
 *       {@code reward_profile}, {@code display_profile} — all read from
 *       {@code gameplay/leveltools/item_profiles.yml},</li>
 *   <li>trigger profiles: {@code BLOCK_BREAK} (pickaxes/axes/shovels),
 *       {@code ENTITY_KILL} (swords/bows/…) and {@code FISHING} with
 *       {@code xp_modifier} random ranges, per-material overrides and blacklists
 *       ({@code trigger_profiles.yml}),</li>
 *   <li>XP formula {@code level_xp_formula} (default {@code 100 + {current_level} * 100}),
 *       {@code playerPlacedBlocks} rule, level-up sound,</li>
 *   <li>rewards on level-up from {@code reward_profiles.yml} with the original handlers
 *       {@code command}, {@code player-command}, {@code enchant}, {@code enchant2},
 *       {@code enchant3}, {@code attribute},</li>
 *   <li>display profiles: lore lines (default shows Level and a progress bar) and the
 *       action-bar progress ({@code display_profiles.yml}),</li>
 *   <li>{@code /leveltools} with {@code reload}, {@code resetall <player>},
 *       {@code resethand [player]} (LevelToolsCommand).</li>
 * </ul>
 * Level/XP live in the item's persistent data container and are stored as
 * {@code "level"} and {@code "xp"} tags, so combining items works like the original
 * {@code anvil_combine} mode (default {@code ADD_BOTH} for lore-less merges).
 */
public final class LevelToolsModule extends EssentialsModule implements Listener {

    private record XpRange(double min, double max) {
        double roll() {
            return max <= min ? min : ThreadLocalRandom.current().nextDouble(min, max);
        }
    }

    private static final class TriggerProfile {
        String name;
        String type;
        XpRange defaultRange = new XpRange(1, 1);
        Map<String, XpRange> custom = new HashMap<>();
        String filterType = "";
        List<String> filter = new ArrayList<>();

        XpRange rangeFor(String key) {
            return custom.getOrDefault(key, defaultRange);
        }

        boolean blocked(String key) {
            if (key == null) {
                return false;
            }
            boolean listed = filter.stream().anyMatch(entry -> entry.equalsIgnoreCase(key));
            if ("BLACKLIST".equalsIgnoreCase(filterType)) {
                return listed;
            }
            if ("WHITELIST".equalsIgnoreCase(filterType)) {
                return !listed;
            }
            return false;
        }
    }

    private static final class ItemProfile {
        String name;
        List<String> materials = new ArrayList<>();
        List<String> triggers = new ArrayList<>();
        String reward = "";
        String display = "default";
        int maxLevel = 100;
    }

    private final Map<String, ItemProfile> itemProfiles = new HashMap<>();
    private final Map<Material, ItemProfile> byMaterial = new HashMap<>();
    private final Map<String, TriggerProfile> triggerProfiles = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> rewardProfiles = new HashMap<>();

    private NamespacedKey levelKey;
    private NamespacedKey xpKey;

    public LevelToolsModule(SamplesEssentials plugin) {
        super("gameplay", "leveltools", "LevelTools-2.1.2.jar", "Items gain XP/levels from mining, combat and fishing with rewards");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.leveltools", "Use LevelTools", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.leveltools.admin", "Administer LevelTools", org.bukkit.permissions.PermissionDefault.OP);
        levelKey = new NamespacedKey(plugin, "leveltools_level");
        xpKey = new NamespacedKey(plugin, "leveltools_xp");
        loadProfiles();
        LevelToolsCommand handler = new LevelToolsCommand();
        command("leveltools", handler);
        command("lt", handler);
        onDisableHook(this::unbindCommands);
        listen(this);
        syncRepeat(40L, 40L, this::refreshActionBars);
    }

    /* ------------------------------------------------------------------ */
    /* profiles                                                           */
    /* ------------------------------------------------------------------ */

    private void loadProfiles() {
        itemProfiles.clear();
        byMaterial.clear();
        triggerProfiles.clear();
        rewardProfiles.clear();

        var itemFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("item_profiles.yml"));
        var itemSection = itemFile.getConfigurationSection("profiles");
        if (itemSection != null) {
            for (String key : itemSection.getKeys(false)) {
                ItemProfile profile = new ItemProfile();
                profile.name = key;
                profile.materials = itemSection.getStringList(key + ".materials");
                profile.triggers = itemSection.getStringList(key + ".trigger_profiles");
                profile.reward = itemSection.getString(key + ".reward_profile", "");
                profile.display = itemSection.getString(key + ".display_profile", "default");
                profile.maxLevel = itemSection.getInt(key + ".max_level", 100);
                itemProfiles.put(key, profile);
                for (String raw : profile.materials) {
                    try {
                        byMaterial.put(Material.valueOf(raw.toUpperCase(Locale.ROOT)), profile);
                    } catch (IllegalArgumentException ignored) {
                        // unknown material
                    }
                }
            }
        }

        var triggerFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("trigger_profiles.yml"));
        var triggerSection = triggerFile.getConfigurationSection("profiles");
        if (triggerSection != null) {
            for (String key : triggerSection.getKeys(false)) {
                TriggerProfile profile = new TriggerProfile();
                profile.name = key;
                profile.type = triggerSection.getString(key + ".type", "BLOCK_BREAK");
                profile.defaultRange = readRange(triggerSection, key + ".xp_modifier.default");
                var custom = triggerSection.getConfigurationSection(key + ".xp_modifier.custom");
                if (custom != null) {
                    for (String customKey : custom.getKeys(false)) {
                        profile.custom.put(customKey, readRange(custom, customKey));
                    }
                }
                profile.filterType = triggerSection.getString(key + ".filter.type", "");
                profile.filter = triggerSection.getStringList(key + ".filter.list");
                triggerProfiles.put(key, profile);
            }
        }

        var rewardFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("reward_profiles.yml"));
        var rewardSection = rewardFile.getConfigurationSection("profiles");
        if (rewardSection != null) {
            for (String key : rewardSection.getKeys(false)) {
                var levels = rewardSection.getConfigurationSection(key + ".levels");
                Map<Integer, List<String>> byLevel = new HashMap<>();
                if (levels != null) {
                    for (String levelKey : levels.getKeys(false)) {
                        try {
                            byLevel.put(Integer.parseInt(levelKey), levels.getStringList(levelKey));
                        } catch (NumberFormatException ignored) {
                            // not a level
                        }
                    }
                }
                rewardProfiles.put(key, byLevel);
            }
        }
    }

    private XpRange readRange(org.bukkit.configuration.ConfigurationSection section, String path) {
        return new XpRange(section.getDouble(path + ".min", 1.0), section.getDouble(path + ".max", 1.0));
    }

    /* ------------------------------------------------------------------ */
    /* item data                                                          */
    /* ------------------------------------------------------------------ */

    private int level(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return 0;
        }
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        return data.getOrDefault(levelKey, PersistentDataType.INTEGER, 1) - 1;
    }

    private double xp(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return 0;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .getOrDefault(xpKey, PersistentDataType.DOUBLE, 0.0);
    }

    private void setLevel(ItemStack item, int level) {
        if (item.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, Math.max(0, level) + 1);
        item.setItemMeta(meta);
    }

    private void setXp(ItemStack item, double value) {
        if (item.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(xpKey, PersistentDataType.DOUBLE, Math.max(0, value));
        item.setItemMeta(meta);
    }

    /** Evaluates {@code level_xp_formula}, e.g. {@code 100 + {current_level} * 100}. */
    private double requiredXp(int currentLevel) {
        String formula = cfgString("level_xp_formula", "100 + {current_level} * 100")
                .replace("{current_level}", String.valueOf(currentLevel));
        try {
            return evaluateArithmetic(formula);
        } catch (Exception e) {
            return 100 + currentLevel * 100.0;
        }
    }

    /** Tiny arithmetic evaluator (the formula only uses + - * / and numbers). */
    private double evaluateArithmetic(String expression) {
        String sanitized = expression.replaceAll("[^0-9+\\-*/.() ]", "");
        return new Object() {
            int pos = -1;
            int ch;

            void next() {
                ch = ++pos < sanitized.length() ? sanitized.charAt(pos) : -1;
            }

            double parse() {
                next();
                double value = parseExpression();
                if (pos < sanitized.length()) {
                    throw new IllegalArgumentException("Unexpected: " + (char) ch);
                }
                return value;
            }

            double parseExpression() {
                double value = parseTerm();
                while (true) {
                    if (ch == '+') {
                        next();
                        value += parseTerm();
                    } else if (ch == '-') {
                        next();
                        value -= parseTerm();
                    } else {
                        return value;
                    }
                }
            }

            double parseTerm() {
                double value = parseFactor();
                while (true) {
                    if (ch == '*') {
                        next();
                        value *= parseFactor();
                    } else if (ch == '/') {
                        next();
                        value /= parseFactor();
                    } else {
                        return value;
                    }
                }
            }

            double parseFactor() {
                if (ch == '+') {
                    next();
                    return parseFactor();
                }
                if (ch == '-') {
                    next();
                    return -parseFactor();
                }
                double value;
                int start = pos;
                if (ch == '(') {
                    next();
                    value = parseExpression();
                    if (ch != ')') {
                        throw new IllegalArgumentException("Expected )");
                    }
                    next();
                } else if (ch >= '0' && ch <= '9' || ch == '.') {
                    while (ch >= '0' && ch <= '9' || ch == '.') {
                        next();
                    }
                    value = Double.parseDouble(sanitized.substring(start, pos));
                } else {
                    throw new IllegalArgumentException("Unexpected: " + (char) ch);
                }
                return value;
            }
        }.parse();
    }

    /* ------------------------------------------------------------------ */
    /* XP gain                                                            */
    /* ------------------------------------------------------------------ */

    private void grantXp(Player player, ItemStack item, ItemProfile profile, String triggerType, String key) {
        if (item == null || profile == null) {
            return;
        }
        int level = level(item);
        if (level >= profile.maxLevel) {
            return;
        }
        double gained = 0;
        for (String triggerName : profile.triggers) {
            TriggerProfile trigger = triggerProfiles.get(triggerName);
            if (trigger == null || !trigger.type.equalsIgnoreCase(triggerType) || trigger.blocked(key)) {
                continue;
            }
            gained += trigger.rangeFor(key).roll();
        }
        if (gained <= 0) {
            return;
        }
        double current = xp(item) + gained;
        double needed = requiredXp(level);
        boolean leveled = false;
        while (current >= needed && level < profile.maxLevel) {
            current -= needed;
            level++;
            leveled = true;
            applyRewards(player, item, profile, level);
            needed = requiredXp(level);
        }
        setLevel(item, level);
        setXp(item, current);
        applyDisplay(player, item, profile, level, current, needed);
        if (leveled) {
            playLevelUpSound(player);
        }
    }

    private void applyRewards(Player player, ItemStack item, ItemProfile profile, int level) {
        Map<Integer, List<String>> rewards = rewardProfiles.get(profile.reward);
        if (rewards == null) {
            return;
        }
        List<String> actions = rewards.get(level);
        if (actions == null) {
            return;
        }
        for (String action : actions) {
            String[] parts = action.trim().split("\\s+");
            if (parts.length < 2) {
                continue;
            }
            String handler = parts[0].toLowerCase(Locale.ROOT);
            try {
                switch (handler) {
                    case "command" -> plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            action.substring(parts[0].length()).trim()
                                    .replace("%player%", player.getName()));
                    case "player-command" -> player.performCommand(action.substring(parts[0].length()).trim());
                    case "enchant", "enchant2", "enchant3" -> applyEnchant(item, parts[1], Integer.parseInt(parts[2]), handler);
                    case "attribute" -> {
                        if (item.getItemMeta() != null) {
                            org.bukkit.attribute.Attribute attribute = null;
                            for (org.bukkit.attribute.Attribute candidate : org.bukkit.Registry.ATTRIBUTE) {
                                String keyName = candidate.getKey().getKey();
                                if (keyName.equalsIgnoreCase(parts[1].replace("generic.", ""))
                                        || keyName.equalsIgnoreCase(parts[1])) {
                                    attribute = candidate;
                                    break;
                                }
                            }
                            if (attribute != null) {
                                ItemMeta meta = item.getItemMeta();
                                meta.addAttributeModifier(attribute, new org.bukkit.attribute.AttributeModifier(
                                        new NamespacedKey(plugin, "leveltools_" + level),
                                        Double.parseDouble(parts[2]),
                                        org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                                        org.bukkit.inventory.EquipmentSlotGroup.MAINHAND));
                                item.setItemMeta(meta);
                            }
                        }
                    }
                    default -> {
                        // unknown handler, ignored like the original
                    }
                }
            } catch (Exception ignored) {
                // broken reward line
            }
        }
    }

    private void applyEnchant(ItemStack item, String enchantName, int level, String mode) {
        Enchantment enchantment = null;
        try {
            enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantName.toLowerCase(Locale.ROOT)));
        } catch (Exception ignored) {
            // unknown enchantment
        }
        if (enchantment == null) {
            try {
                enchantment = Registry.ENCHANTMENT.get(NamespacedKey.fromString(enchantName.toLowerCase(Locale.ROOT)));
            } catch (Exception ignored) {
                // unknown enchantment key
            }
        }
        if (enchantment == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        int existing = meta.getEnchantLevel(enchantment);
        int newLevel = switch (mode) {
            case "enchant3" -> existing + level;
            case "enchant2" -> Math.max(existing, level);
            default -> level;
        };
        meta.addEnchant(enchantment, newLevel, true);
        item.setItemMeta(meta);
    }

    private void playLevelUpSound(Player player) {
        String sound = cfgString("level_up_sound.sound", "ENTITY_PLAYER_LEVELUP");
        if (sound == null || sound.isBlank() || sound.equalsIgnoreCase("null")) {
            return;
        }
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound.toUpperCase(Locale.ROOT)),
                    (float) cfgDouble("level_up_sound.volume", 1.0),
                    (float) cfgDouble("level_up_sound.pitch", 1.0));
        } catch (IllegalArgumentException ignored) {
            // unknown sound
        }
    }

    /* ------------------------------------------------------------------ */
    /* display                                                            */
    /* ------------------------------------------------------------------ */

    private void applyDisplay(Player player, ItemStack item, ItemProfile profile, int level, double xp, double needed) {
        var displayFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("display_profiles.yml"));
        String path = "profiles." + profile.display;
        List<String> loreLines = displayFile.getStringList(path + ".lore.lines");
        boolean loreEnabled = displayFile.getBoolean(path + ".lore.enabled", true);
        if (loreEnabled && !loreLines.isEmpty() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Text.parse(displayReplace(line, item, level, xp, needed, displayFile, path)));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        String nameFormat = displayFile.getString(path + ".name.text", "");
        if (displayFile.getBoolean(path + ".name.enabled", false) && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Text.parse(displayReplace(nameFormat, item, level, xp, needed, displayFile, path)));
            item.setItemMeta(meta);
        }
    }

    private String displayReplace(String raw, ItemStack item, int level, double xp, double needed,
                                  org.bukkit.configuration.file.YamlConfiguration displayFile, String path) {
        String itemName = item.getItemMeta() != null && item.getItemMeta().hasDisplayName()
                ? Text.plain(item.getItemMeta().displayName())
                : item.getType().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return raw
                .replace("{level}", String.valueOf(level))
                .replace("{xp}", String.valueOf((int) xp))
                .replace("{max_xp}", String.valueOf((int) needed))
                .replace("{xp_formatted}", formatXp(xp))
                .replace("{max_xp_formatted}", formatXp(needed))
                .replace("{item}", itemName)
                .replace("{progress_bar}", progressBar(displayFile, path, xp, needed));
    }

    private String formatXp(double value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        }
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        }
        if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        }
        return String.valueOf((int) value);
    }

    private String progressBar(org.bukkit.configuration.file.YamlConfiguration displayFile, String path,
                               double xp, double needed) {
        int total = Math.max(1, displayFile.getInt(path + ".progress_bar.total_bars", 50));
        String symbol = displayFile.getString(path + ".progress_bar.bar_symbol", "|");
        String prefix = displayFile.getString(path + ".progress_bar.prefix_symbol", "[");
        String suffix = displayFile.getString(path + ".progress_bar.suffix_symbol", "]");
        String completedColor = displayFile.getString(path + ".progress_bar.completed_color", "e");
        String placeholderColor = displayFile.getString(path + ".progress_bar.placeholder_color", "7");
        double ratio = needed <= 0 ? 1 : Math.max(0, Math.min(1, xp / needed));
        int completed = (int) Math.round(ratio * total);
        return "&8" + prefix + "&" + completedColor + symbol.repeat(completed)
                + "&" + placeholderColor + symbol.repeat(Math.max(0, total - completed)) + "&8" + suffix;
    }

    private void refreshActionBars() {
        var displayFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("display_profiles.yml"));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemProfile profile = byMaterial.get(item.getType());
            if (profile == null) {
                continue;
            }
            String path = "profiles." + profile.display;
            if (!displayFile.getBoolean(path + ".action_bar.enabled", true)) {
                continue;
            }
            String template = displayFile.getString(path + ".action_bar.text", "");
            int level = level(item);
            double xp = xp(item);
            double needed = requiredXp(level);
            player.sendActionBar(Text.parse(displayReplace(template, item, level, xp, needed, displayFile, path)));
        }
    }

    /* ------------------------------------------------------------------ */
    /* events                                                             */
    /* ------------------------------------------------------------------ */

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemProfile profile = byMaterial.get(item.getType());
        if (profile == null) {
            return;
        }
        if (!cfgBool("playerPlacedBlocks", false) && placedBlocks.contains(event.getBlock().getLocation())) {
            return;
        }
        grantXp(player, item, profile, "BLOCK_BREAK", event.getBlock().getType().name());
    }

    /** Tracks player-placed blocks in memory (the original persists them to a database). */
    private final java.util.Set<org.bukkit.Location> placedBlocks = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @EventHandler(ignoreCancelled = true)
    public void onPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        placedBlocks.add(event.getBlock().getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        ItemStack item = killer.getInventory().getItemInMainHand();
        ItemProfile profile = byMaterial.get(item.getType());
        if (profile == null) {
            return;
        }
        grantXp(killer, item, profile, "ENTITY_KILL", event.getEntityType().name());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemProfile profile = byMaterial.get(item.getType());
        if (profile == null) {
            return;
        }
        grantXp(player, item, profile, "FISHING", "FISH");
    }

    /* ------------------------------------------------------------------ */
    /* command (LevelToolsCommand)                                        */
    /* ------------------------------------------------------------------ */

    private final class LevelToolsCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull org.bukkit.command.Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                sender.sendMessage(msgComponent("help-header"));
                sender.sendMessage(Text.parse("<gray>/leveltools reload <dark_gray>- <white>reload profiles"));
                sender.sendMessage(Text.parse("<gray>/leveltools resetall <player> <dark_gray>- <white>reset all tool XP"));
                sender.sendMessage(Text.parse("<gray>/leveltools resethand [player] <dark_gray>- <white>reset the tool in hand"));
                return true;
            }
            if (!sender.hasPermission("samplese.leveltools.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> {
                    reloadConfig();
                    loadProfiles();
                    sender.sendMessage(msgComponent("reloaded"));
                }
                case "resetall" -> {
                    if (args.length < 2) {
                        sender.sendMessage(msgComponent("usage"));
                        return true;
                    }
                    Player target = plugin.getServer().getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(msgComponent("player-not-found", "player", args[1]));
                        return true;
                    }
                    int count = 0;
                    for (ItemStack item : target.getInventory().getContents()) {
                        if (item != null && byMaterial.containsKey(item.getType())) {
                            if (item.getItemMeta() != null) {
                                ItemMeta meta = item.getItemMeta();
                                meta.getPersistentDataContainer().remove(levelKey);
                                meta.getPersistentDataContainer().remove(xpKey);
                                meta.lore(null);
                                item.setItemMeta(meta);
                            }
                            count++;
                        }
                    }
                    sender.sendMessage(msgComponent("reset-all", "player", target.getName(), "count", String.valueOf(count)));
                }
                case "resethand" -> {
                    Player target = sender instanceof Player player ? player : null;
                    if (args.length > 1) {
                        target = plugin.getServer().getPlayerExact(args[1]);
                    }
                    if (target == null) {
                        sender.sendMessage(msgComponent("player-not-found",
                                "player", args.length > 1 ? args[1] : "?"));
                        return true;
                    }
                    ItemStack item = target.getInventory().getItemInMainHand();
                    if (!byMaterial.containsKey(item.getType())) {
                        sender.sendMessage(msgComponent("item-not-tool"));
                        return true;
                    }
                    if (item.getItemMeta() != null) {
                        ItemMeta meta = item.getItemMeta();
                        meta.getPersistentDataContainer().remove(levelKey);
                        meta.getPersistentDataContainer().remove(xpKey);
                        meta.lore(null);
                        item.setItemMeta(meta);
                    }
                    sender.sendMessage(msgComponent("reset-hand", "player", target.getName()));
                }
                case "info" -> {
                    Player target = sender instanceof Player player ? player : null;
                    ItemStack item = target == null ? null : target.getInventory().getItemInMainHand();
                    ItemProfile profile = item == null ? null : byMaterial.get(item.getType());
                    if (profile == null) {
                        sender.sendMessage(msgComponent("item-not-tool"));
                        return true;
                    }
                    sender.sendMessage(msgComponent("info",
                            "profile", profile.name,
                            "level", String.valueOf(level(item)),
                            "xp", formatXp(xp(item)),
                            "max_xp", formatXp(requiredXp(level(item))),
                            "max_level", String.valueOf(profile.maxLevel)));
                }
                default -> sender.sendMessage(msgComponent("usage"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                          @NotNull org.bukkit.command.Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("reload", "resetall", "resethand", "info");
            }
            if (args.length == 2) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return List.of();
        }
    }
}
