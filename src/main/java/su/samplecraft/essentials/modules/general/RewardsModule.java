package su.samplecraft.essentials.modules.general;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code LushRewards-3.3.2.jar} (org.lushplugins.lushrewards).
 * <p>
 * Ported modules (the original supports more types, these are the ones configured
 * on the old server):
 * <ul>
 *   <li>{@code daily-rewards} — streak mode: one reward per day, a missed day resets
 *       the streak (or resets to {@code reset-days-at}), {@code allow-reward-stacking},
 *       {@code upcoming-category} display, the {@code default} fallback day and
 *       {@code reward-mode: streak},</li>
 *   <li>{@code playtime-rewards} / {@code playtime-rewards-daily} — goals are granted
 *       when a player reaches {@code play-minutes}; the check runs every
 *       {@code refresh-time} minutes and resets every {@code reset-playtime-at} days,</li>
 *   <li>GUI: one button per day with {@code display-item} material/amount/lore and the
 *       claimed/unclaimed placeholders, click claims all claimable rewards, rewards
 *       of type {@code item} are given directly (+ {@code redeem-sound}),</li>
 *   <li>{@code /rewards} opens the daily GUI, {@code /rewards claim},
 *       {@code /rewards gui [module]}, {@code /rewards reload},
 *       {@code /rewards edit-user <module> reset|set-days|set-streak <player>},
 *       {@code /rewards set-playtime <minutes> <player>} (admin),</li>
 *   <li>reminder broadcast every {@code reminder-period} seconds when rewards are
 *       claimable (config {@code reminder} message, optional sound).</li>
 * </ul>
 */
public final class RewardsModule extends EssentialsModule implements Listener {

    private static final class RewardDefinition {
        int amount = 1;
        Material material = Material.CHEST;
        List<String> lore = new ArrayList<>();
        String sound = "";
        final List<ItemStack> rewards = new ArrayList<>();
    }

    private final Map<Integer, RewardDefinition> dailyRewards = new HashMap<>();
    private final RewardDefinition defaultDaily = new RewardDefinition();
    private final List<TimedGoal> playtimeGoals = new ArrayList<>();
    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();
    private final Map<UUID, String> openGuis = new ConcurrentHashMap<>();

    private record TimedGoal(int minutes, RewardDefinition definition) {
    }

    private static final class PlayerData {
        int day = 1;
        int streak = 0;
        long lastClaim = 0L;
        long playtimeMinutesToday = 0L;
        long playtimeTotalMinutes = 0L;
        long joinTime = 0L;
        long dayStart = 0L;
        final java.util.Set<Integer> claimedGoals = new java.util.HashSet<>();
    }

    private ModuleStorage storage;

    public RewardsModule(SamplesEssentials plugin) {
        super("general", "rewards", "LushRewards-3.3.2.jar", "Daily streak rewards and playtime reward goals with GUI");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.rewards", "Use /rewards", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.rewards.admin", "Edit reward data", org.bukkit.permissions.PermissionDefault.OP);
        storage = new ModuleStorage(plugin, getDataFolder(), "users.yml");
        loadDefinitions();
        loadData();
        command("rewards", new RewardsCommand());
        onDisableHook(() -> {
            unbindCommands();
            saveData();
        });
        listen(this);
        int refresh = Math.max(1, cfgInt("refresh-time", 1));
        syncRepeat(refresh * 1200L, refresh * 1200L, this::tickPlaytime);
        int reminder = cfgInt("reminder-period", 1800);
        if (reminder > 0) {
            syncRepeat(reminder * 20L, reminder * 20L, this::sendReminder);
        }
        // players already online (plugin reload) start counting from now
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            data.computeIfAbsent(player.getUniqueId(), k -> new PlayerData()).joinTime = System.currentTimeMillis();
        }
    }

    /* ------------------------------------------------------------------ */
    /* definitions                                                        */
    /* ------------------------------------------------------------------ */

    private void loadDefinitions() {
        dailyRewards.clear();
        playtimeGoals.clear();
        var dailyFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("daily-rewards.yml"));
        var dailySection = dailyFile.getConfigurationSection("daily-rewards");
        if (dailySection != null) {
            for (String key : dailySection.getKeys(false)) {
                RewardDefinition definition = readDefinition(dailySection, key);
                if (key.equalsIgnoreCase("default")) {
                    copyInto(definition, defaultDaily);
                } else {
                    try {
                        dailyRewards.put(Integer.parseInt(key), definition);
                    } catch (NumberFormatException ignored) {
                        // not a day key
                    }
                }
            }
        }
        var playtimeFile = org.bukkit.configuration.file.YamlConfiguration
                .loadConfiguration(ensureDefaultFile("playtime-rewards.yml"));
        List<Map<?, ?>> goals = playtimeFile.getMapList("goals");
        for (Map<?, ?> raw : goals) {
            try {
                Object minutesRaw = raw.get("play-minutes");
                int minutes = minutesRaw instanceof Number number ? number.intValue()
                        : Integer.parseInt(String.valueOf(minutesRaw));
                RewardDefinition definition = new RewardDefinition();
                Map<?, ?> display = raw.get("display-item") instanceof Map<?, ?> map ? map : Map.of();
                definition.material = material(String.valueOf(display.getOrDefault("material", "clock")));
                definition.amount = display.get("amount") instanceof Number n ? n.intValue() : 1;
                if (raw.get("rewards") instanceof List<?> rewardList) {
                    for (Object entry : rewardList) {
                        if (entry instanceof Map<?, ?> reward && "item".equals(String.valueOf(reward.get("type")))) {
                            ItemStack item = new ItemStack(material(String.valueOf(reward.get("material"))),
                                    reward.get("amount") instanceof Number n ? n.intValue() : 1);
                            definition.rewards.add(item);
                        }
                    }
                }
                playtimeGoals.add(new TimedGoal(minutes, definition));
            } catch (Exception ignored) {
                // broken goal
            }
        }
        playtimeGoals.sort(java.util.Comparator.comparingInt(TimedGoal::minutes));
    }

    private RewardDefinition readDefinition(org.bukkit.configuration.ConfigurationSection root, String key) {
        RewardDefinition definition = new RewardDefinition();
        var display = root.getConfigurationSection(key + ".display-item");
        if (display != null) {
            definition.material = material(display.getString("material", "chest"));
            definition.amount = definition.material == Material.PLAYER_HEAD ? 1 : display.getInt("amount", 1);
            definition.lore = display.getStringList("lore");
        }
        definition.sound = root.getString(key + ".redeem-sound", "");
        var rewards = root.getMapList(key + ".rewards");
        for (Map<?, ?> raw : rewards) {
            String type = String.valueOf(raw.get("type"));
            if (type.equalsIgnoreCase("item")) {
                int amount = raw.get("amount") instanceof Number number ? number.intValue() : 1;
                definition.rewards.add(new ItemStack(material(String.valueOf(raw.get("material"))), amount));
            } else if (type.equalsIgnoreCase("random") && raw.get("rewards") instanceof List<?> random) {
                // "random": exactly one of the nested rewards
                if (!random.isEmpty()) {
                    Object chosen = random.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(random.size()));
                    if (chosen instanceof Map<?, ?> entry && "item".equals(String.valueOf(entry.get("type")))) {
                        int amount = entry.get("amount") instanceof Number number ? number.intValue() : 1;
                        definition.rewards.add(new ItemStack(material(String.valueOf(entry.get("material"))), amount));
                    }
                }
            }
        }
        return definition;
    }

    private void copyInto(RewardDefinition from, RewardDefinition to) {
        to.material = from.material;
        to.amount = from.amount;
        to.lore = from.lore;
        to.sound = from.sound;
        to.rewards.clear();
        to.rewards.addAll(from.rewards);
    }

    private Material material(String raw) {
        try {
            return Material.valueOf(raw.toUpperCase(Locale.ROOT).replace("MINECRAFT:", ""));
        } catch (IllegalArgumentException e) {
            return Material.CHEST;
        }
    }

    /* ------------------------------------------------------------------ */
    /* data                                                               */
    /* ------------------------------------------------------------------ */

    private void loadData() {
        var section = storage.yaml().getConfigurationSection("users");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                PlayerData playerData = new PlayerData();
                playerData.day = storage.yaml().getInt("users." + key + ".day", 1);
                playerData.streak = storage.yaml().getInt("users." + key + ".streak", 0);
                playerData.lastClaim = storage.yaml().getLong("users." + key + ".last-claim", 0L);
                playerData.playtimeMinutesToday = storage.yaml().getLong("users." + key + ".playtime-today", 0L);
                playerData.playtimeTotalMinutes = storage.yaml().getLong("users." + key + ".playtime-total", 0L);
                playerData.dayStart = storage.yaml().getLong("users." + key + ".day-start", 0L);
                playerData.claimedGoals.addAll(storage.yaml().getIntegerList("users." + key + ".claimed-goals"));
                data.put(id, playerData);
            } catch (Exception ignored) {
                // corrupted entry
            }
        }
    }

    private void saveData() {
        data.forEach((id, playerData) -> {
            String path = "users." + id;
            storage.yaml().set(path + ".day", playerData.day);
            storage.yaml().set(path + ".streak", playerData.streak);
            storage.yaml().set(path + ".last-claim", playerData.lastClaim);
            storage.yaml().set(path + ".playtime-today", playerData.playtimeMinutesToday);
            storage.yaml().set(path + ".playtime-total", playerData.playtimeTotalMinutes);
            storage.yaml().set(path + ".day-start", playerData.dayStart);
            storage.yaml().set(path + ".claimed-goals", new ArrayList<>(playerData.claimedGoals));
        });
        storage.save();
    }

    private PlayerData data(Player player) {
        return data.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    private boolean canClaimDaily(PlayerData playerData) {
        if (playerData.lastClaim == 0) {
            return true;
        }
        return !sameDay(playerData.lastClaim, System.currentTimeMillis());
    }

    private boolean sameDay(long first, long second) {
        java.time.LocalDate a = java.time.Instant.ofEpochMilli(first).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        java.time.LocalDate b = java.time.Instant.ofEpochMilli(second).atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return a.equals(b);
    }

    /* ------------------------------------------------------------------ */
    /* claiming                                                           */
    /* ------------------------------------------------------------------ */

    private RewardDefinition definitionForDay(int day) {
        RewardDefinition definition = dailyRewards.get(day);
        return definition == null ? defaultDaily : definition;
    }

    private void claimDaily(Player player) {
        PlayerData playerData = data(player);
        if (!canClaimDaily(playerData)) {
            player.sendMessage(msgComponent("already-claimed"));
            return;
        }
        long now = System.currentTimeMillis();
        boolean streakContinues = playerData.lastClaim != 0
                && java.time.Instant.ofEpochMilli(playerData.lastClaim).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                .plusDays(1).equals(java.time.Instant.ofEpochMilli(now)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        if (playerData.lastClaim != 0 && !streakContinues) {
            playerData.day = 1;
            playerData.streak = 0;
        }
        RewardDefinition definition = definitionForDay(playerData.day);
        give(player, definition);
        playerData.lastClaim = now;
        playerData.streak++;
        int maxDay = dailyRewards.isEmpty() ? 1 : dailyRewards.keySet().stream().max(Integer::compareTo).orElse(1);
        playerData.day = playerData.day >= maxDay ? maxDay : playerData.day + 1;
        int resetAt = cfgInt("reset-days-at", -1);
        if (resetAt > 0 && playerData.day > resetAt) {
            playerData.day = 1;
        }
        saveData();
        player.sendMessage(msgComponent("daily-given",
                "day", String.valueOf(playerData.day - 1), "streak", String.valueOf(playerData.streak)));
        playSound(player, definition.sound.isBlank()
                ? cfgString("default-redeem-sound", "ENTITY_PLAYER_LEVELUP") : definition.sound);
    }

    private void give(Player player, RewardDefinition definition) {
        for (ItemStack item : definition.rewards) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
            for (ItemStack rest : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), rest);
            }
        }
    }

    private void playSound(Player player, String raw) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(raw.toUpperCase(Locale.ROOT)), 1f, 1f);
        } catch (IllegalArgumentException ignored) {
            // unknown sound
        }
    }

    private void tickPlaytime() {
        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData playerData = data(player);
            if (playerData.joinTime == 0) {
                playerData.joinTime = now;
            }
            long minutes = Math.max(0, (now - playerData.joinTime) / 60_000L);
            playerData.joinTime = now;
            playerData.playtimeMinutesToday += minutes;
            playerData.playtimeTotalMinutes += minutes;
            for (int i = 0; i < playtimeGoals.size(); i++) {
                TimedGoal goal = playtimeGoals.get(i);
                if (playerData.playtimeMinutesToday >= goal.minutes() && !playerData.claimedGoals.contains(i)) {
                    playerData.claimedGoals.add(i);
                    give(player, goal.definition());
                    player.sendMessage(msgComponent("playtime-given", "minutes", String.valueOf(goal.minutes())));
                }
            }
            resetPlaytimeIfNeeded(playerData, now);
        }
        saveData();
    }

    private void resetPlaytimeIfNeeded(PlayerData playerData, long now) {
        int resetDays = Math.max(1, cfgInt("reset-playtime-at", 1));
        if (playerData.dayStart == 0) {
            playerData.dayStart = now;
            return;
        }
        long elapsedDays = (now - playerData.dayStart) / 86_400_000L;
        if (elapsedDays >= resetDays && !sameDay(playerData.dayStart, now)) {
            playerData.playtimeMinutesToday = 0;
            playerData.claimedGoals.clear();
            playerData.dayStart = now;
        }
    }

    private boolean hasClaimable(Player player) {
        PlayerData playerData = data(player);
        if (canClaimDaily(playerData)) {
            return true;
        }
        for (int i = 0; i < playtimeGoals.size(); i++) {
            if (playerData.playtimeMinutesToday >= playtimeGoals.get(i).minutes()
                    && !playerData.claimedGoals.contains(i)) {
                return true;
            }
        }
        return false;
    }

    private void sendReminder() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasClaimable(player)) {
                player.sendMessage(msgComponent("reminder"));
                playSound(player, cfgString("reminder-sound", "BLOCK_NOTE_BLOCK_PLING"));
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* GUI                                                                */
    /* ------------------------------------------------------------------ */

    private void openGui(Player player) {
        PlayerData playerData = data(player);
        var categories = config().getConfigurationSection("categories");
        int size = 54;
        Inventory inventory = Bukkit.createInventory(null, size,
                Text.parse(cfgString("gui-title", "&8Daily Rewards")));
        int slot = 0;
        int maxDay = dailyRewards.isEmpty() ? 1 : dailyRewards.keySet().stream().max(Integer::compareTo).orElse(1);
        for (int day = 1; day <= maxDay && slot < 45; day++, slot++) {
            RewardDefinition definition = definitionForDay(day);
            boolean claimed = day < playerData.day || (!canClaimDaily(playerData) && day == playerData.day - 1);
            boolean current = day == playerData.day;
            ItemStack item = new ItemStack(definition.material, Math.max(1, definition.amount));
            if (item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Text.parse((claimed ? "<green>" : current ? "<gold>" : "<gray>")
                        + "Day " + day + (playerData.streak > 0 ? " (streak " + playerData.streak + ")" : "")));
                List<Component> lore = new ArrayList<>();
                for (String line : definition.lore) {
                    lore.add(Text.parse(line));
                }
                lore.add(Text.parse(claimed ? "<green>Claimed" : current ? "<yellow>Click to claim" : "<dark_gray>Locked"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
        }
        ItemStack info = new ItemStack(Material.BOOK);
        if (info.getItemMeta() != null) {
            ItemMeta meta = info.getItemMeta();
            meta.displayName(Text.parse("<gold>Rewards"));
            meta.lore(List.of(
                    Text.parse("<gray>Day: <white>" + playerData.day),
                    Text.parse("<gray>Streak: <white>" + playerData.streak),
                    Text.parse("<gray>Playtime today: <white>" + playerData.playtimeMinutesToday + " min"),
                    Text.parse("<yellow>Click to claim!")));
            info.setItemMeta(meta);
        }
        inventory.setItem(49, info);
        openGuis.put(player.getUniqueId(), "main");
        player.openInventory(inventory);
        if (categories == null) {
            // categories are only used for the original item templates, nothing to add
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!openGuis.containsKey(player.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        claimDaily(player);
        player.closeInventory();
        openGuis.remove(player.getUniqueId());
    }

    /* ------------------------------------------------------------------ */
    /* events                                                             */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData playerData = data(event.getPlayer());
        playerData.joinTime = System.currentTimeMillis();
        if (playerData.dayStart == 0) {
            playerData.dayStart = System.currentTimeMillis();
        }
        if (hasClaimable(event.getPlayer())) {
            event.getPlayer().sendMessage(msgComponent("reminder"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerData playerData = data.computeIfAbsent(event.getPlayer().getUniqueId(), k -> new PlayerData());
        if (playerData.joinTime > 0) {
            long minutes = Math.max(0, (System.currentTimeMillis() - playerData.joinTime) / 60_000L);
            playerData.playtimeMinutesToday += minutes;
            playerData.playtimeTotalMinutes += minutes;
            playerData.joinTime = 0;
        }
        saveData();
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class RewardsCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull org.bukkit.command.Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            String sub = args.length == 0 ? "gui" : args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "gui", "menu" -> {
                    if (!player.hasPermission("samplese.rewards")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    openGui(player);
                }
                case "claim" -> {
                    if (!player.hasPermission("samplese.rewards")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    claimDaily(player);
                }
                case "reload" -> {
                    if (!player.hasPermission("samplese.rewards.admin")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    reloadConfig();
                    loadDefinitions();
                    player.sendMessage(msgComponent("reloaded"));
                }
                case "edit-user" -> {
                    if (!player.hasPermission("samplese.rewards.admin")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    if (args.length < 4) {
                        player.sendMessage(msgComponent("edit-usage"));
                        return true;
                    }
                    var target = plugin.getServer().getPlayerExact(args[args.length - 1]);
                    if (target == null) {
                        player.sendMessage(msgComponent("unknown-player", "player", args[args.length - 1]));
                        return true;
                    }
                    PlayerData playerData = data(target);
                    switch (args[2].toLowerCase(Locale.ROOT)) {
                        case "reset" -> {
                            playerData.day = 1;
                            playerData.streak = 0;
                            playerData.lastClaim = 0;
                            saveData();
                            player.sendMessage(msgComponent("edit-reset", "player", target.getName()));
                        }
                        case "set-days" -> {
                            if (args.length < 5) {
                                player.sendMessage(msgComponent("edit-usage"));
                                return true;
                            }
                            try {
                                playerData.day = Integer.parseInt(args[3]);
                                saveData();
                                player.sendMessage(msgComponent("edit-set-days",
                                        "player", target.getName(), "day", String.valueOf(playerData.day)));
                            } catch (NumberFormatException e) {
                                player.sendMessage(msgComponent("edit-usage"));
                            }
                        }
                        case "set-streak" -> {
                            if (args.length < 5) {
                                player.sendMessage(msgComponent("edit-usage"));
                                return true;
                            }
                            try {
                                playerData.streak = Integer.parseInt(args[3]);
                                saveData();
                                player.sendMessage(msgComponent("edit-set-streak",
                                        "player", target.getName(), "streak", String.valueOf(playerData.streak)));
                            } catch (NumberFormatException e) {
                                player.sendMessage(msgComponent("edit-usage"));
                            }
                        }
                        default -> player.sendMessage(msgComponent("edit-usage"));
                    }
                }
                case "set-playtime" -> {
                    if (!player.hasPermission("samplese.rewards.admin")) {
                        player.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage(msgComponent("set-playtime-usage"));
                        return true;
                    }
                    var target = plugin.getServer().getPlayerExact(args[2]);
                    if (target == null) {
                        player.sendMessage(msgComponent("unknown-player", "player", args[2]));
                        return true;
                    }
                    try {
                        data(target).playtimeMinutesToday = Long.parseLong(args[1]);
                        saveData();
                        player.sendMessage(msgComponent("playtime-set",
                                "player", target.getName(), "playtime", args[1]));
                    } catch (NumberFormatException e) {
                        player.sendMessage(msgComponent("set-playtime-usage"));
                    }
                }
                case "version" -> player.sendMessage(msgComponent("version",
                        "version", plugin.getPluginMeta().getVersion()));
                default -> {
                    if (args[0].equalsIgnoreCase("help")) {
                        player.sendMessage(msgComponent("help-header"));
                        player.sendMessage(Text.parse("<gray>/rewards <dark_gray>- <white>open the rewards menu"));
                        player.sendMessage(Text.parse("<gray>/rewards claim <dark_gray>- <white>claim today's reward"));
                        return true;
                    }
                    openGui(player);
                }
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                          @NotNull org.bukkit.command.Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("gui", "claim", "reload", "edit-user", "set-playtime", "version", "help")
                        .stream().filter(option -> option.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("edit-user")) {
                return List.of("reset", "set-days", "set-streak");
            }
            if (args.length >= 3 && (args[0].equalsIgnoreCase("edit-user") || args[0].equalsIgnoreCase("set-playtime"))) {
                return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return List.of();
        }
    }
}
