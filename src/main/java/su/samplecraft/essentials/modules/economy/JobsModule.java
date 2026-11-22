package su.samplecraft.essentials.modules.economy;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code UJobs-1.0.7.jar} (me.usainsrht.ujobs).
 * <p>
 * Jobs grant job XP and money for configured actions. Job definitions live in
 * {@code jobs.yml} and follow the original layout:
 * <pre>
 * jobs:
 *   hunter:
 *     name: "&cHunter"
 *     icon: NETHERITE_SWORD
 *     level_equation: "&lt;next_level&gt;*&lt;next_level&gt;*5"
 *     levelup_commands: ["eco give &lt;player&gt; &lt;level&gt;*1000"]
 *     levelup_message: { chat: "...", sound: "entity.player.levelup" }
 *     actions:
 *       kill:      { zombie: { exp: 2, money: 0.1 }, ... }
 *       break:     { stone: { exp: 1, money: 0.05 } }
 *       place:     { stone: { exp: 0.5, money: 0 } }
 *       breed:     { cow: { exp: 5 } }
 *       tame:      { wolf: { exp: 5 } }
 *       fish:      { cod: { exp: 3, money: 0.2 } }
 *       enchant:   { "*": { exp: 10 } }
 *       harvest:   { wheat: { exp: 1 } }
 *       smelt:     { iron_ore: { exp: 1 } }
 * </pre>
 * Player progress is stored per UUID in {@code data/jobs.yml}
 * ({@code <uuid>.<job>.level/exp}).
 * <p>
 * Commands: {@code /jobs [list|join <job>|leave <job>|stats [player]|top <job>|reload]}
 * plus admin {@code /jobs addexp <player> <job> <amount>} and
 * {@code /jobs setlevel <player> <job> <level>}.
 * Money is paid through Vault when present; otherwise only XP is granted.
 */
public final class JobsModule extends EssentialsModule implements Listener {

    private record Reward(double exp, double money) {
    }

    /** One configured job: display data plus per-action lookup tables. */
    private static final class Job {
        String id;
        String name;
        Material icon = Material.PAPER;
        String levelEquation = "<next_level>*<next_level>*5";
        List<String> levelUpCommands = new ArrayList<>();
        String levelUpChat;
        String levelUpSound;
        String bossBarTitle;
        String bossBarOverlay;
        String bossBarColor;
        final Map<String, Map<String, Reward>> actions = new LinkedHashMap<>();

        Reward reward(String action, String key) {
            Map<String, Reward> table = actions.get(action);
            if (table == null) {
                return null;
            }
            Reward exact = table.get(key.toLowerCase(Locale.ROOT));
            if (exact != null) {
                return exact;
            }
            return table.get("*");
        }
    }

    private final Map<String, Job> jobs = new LinkedHashMap<>();
    private final Map<UUID, Map<String, double[]>> progress = new LinkedHashMap<>();
    private final Map<UUID, BossBar> bossBars = new LinkedHashMap<>();

    public JobsModule(SamplesEssentials plugin) {
        super("economy", "jobs", "UJobs-1.0.7.jar",
                "Jobs with per-action XP/money rewards, levels and leaderboards");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.jobs", "Use jobs", PermissionDefault.TRUE);
        ensurePermission("samplese.jobs.admin", "Administer jobs", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        ensureDefaultFile("jobs.yml");
        reloadConfig();
        loadJobs();
        loadProgress();
        JobsCommand executor = new JobsCommand();
        command("jobs", executor);
        command("ujobs", executor);
        command("job", executor);
        onDisableHook(this::unbindCommands);
        onDisableHook(this::saveProgress);
        onDisableHook(this::hideBars);
        listen(this);
    }

    /* ------------------------------------------------------------------ */
    /* data                                                               */
    /* ------------------------------------------------------------------ */

    private void loadJobs() {
        jobs.clear();
        File file = new File(getModuleFolder(), "jobs.yml");
        if (!file.isFile()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("jobs");
        if (root == null) {
            plugin.getLogger().warning("jobs.yml has no 'jobs' section");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Job job = new Job();
            job.id = id.toLowerCase(Locale.ROOT);
            job.name = section.getString("name", id);
            Material icon = Material.matchMaterial(section.getString("icon", "PAPER"));
            job.icon = icon == null ? Material.PAPER : icon;
            job.levelEquation = section.getString("level_equation", job.levelEquation);
            job.levelUpCommands = section.getStringList("levelup_commands");
            job.levelUpChat = section.getString("levelup_message.chat");
            job.levelUpSound = section.getString("levelup_message.sound");
            job.bossBarTitle = section.getString("bossbar.title");
            job.bossBarOverlay = section.getString("bossbar.overlay");
            job.bossBarColor = section.getString("bossbar.color");
            ConfigurationSection actions = section.getConfigurationSection("actions");
            if (actions != null) {
                for (String action : actions.getKeys(false)) {
                    ConfigurationSection table = actions.getConfigurationSection(action);
                    if (table == null) {
                        continue;
                    }
                    Map<String, Reward> rewards = new LinkedHashMap<>();
                    for (String key : table.getKeys(false)) {
                        ConfigurationSection entry = table.getConfigurationSection(key);
                        if (entry == null) {
                            continue;
                        }
                        rewards.put(key.toLowerCase(Locale.ROOT), new Reward(
                                entry.getDouble("exp", 0),
                                entry.getDouble("money", 0)));
                    }
                    job.actions.put(action.toLowerCase(Locale.ROOT), rewards);
                }
            }
            jobs.put(job.id, job);
        }
    }

    private File progressFile() {
        File folder = new File(getModuleFolder(), "data");
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
        return new File(folder, "jobs.yml");
    }

    private void loadProgress() {
        progress.clear();
        File file = progressFile();
        if (!file.isFile()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String uuid : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(uuid);
            if (section == null) {
                continue;
            }
            Map<String, double[]> data = new LinkedHashMap<>();
            for (String job : section.getKeys(false)) {
                data.put(job, new double[]{
                        section.getDouble(job + ".level", 1),
                        section.getDouble(job + ".exp", 0)});
            }
            try {
                progress.put(UUID.fromString(uuid), data);
            } catch (IllegalArgumentException ignored) {
                // skip malformed keys
            }
        }
    }

    private void saveProgress() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, double[]>> entry : progress.entrySet()) {
            for (Map.Entry<String, double[]> job : entry.getValue().entrySet()) {
                yaml.set(entry.getKey() + "." + job.getKey() + ".level", job.getValue()[0]);
                yaml.set(entry.getKey() + "." + job.getKey() + ".exp", job.getValue()[1]);
            }
        }
        try {
            yaml.save(progressFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save jobs data: " + e.getMessage());
        }
    }

    private void hideBars() {
        for (BossBar bar : bossBars.values()) {
            plugin.getServer().getOnlinePlayers().forEach(player -> player.hideBossBar(bar));
        }
        bossBars.clear();
    }

    /* ------------------------------------------------------------------ */
    /* leveling                                                           */
    /* ------------------------------------------------------------------ */

    /** Evaluates the {@code level_equation} with {@code <next_level>}/{@code <level>}. */
    private double expNeeded(Job job, int nextLevel) {
        String raw = job.levelEquation
                .replace("<next_level>", String.valueOf(nextLevel))
                .replace("<nextlevel>", String.valueOf(nextLevel))
                .replace("<level>", String.valueOf(nextLevel - 1));
        return Math.max(1, evaluate(raw, nextLevel * nextLevel * 5));
    }

    /** Tiny arithmetic evaluator (+ - * / and parentheses) - no scripting engine. */
    private double evaluate(String expression, double fallback) {
        try {
            return new Object() {
                int pos = -1;
                int ch;

                void next() {
                    ch = ++pos < expression.length() ? expression.charAt(pos) : -1;
                }

                void spaces() {
                    while (Character.isWhitespace(ch)) {
                        next();
                    }
                }

                double parse() {
                    next();
                    double value = parseExpression();
                    spaces();
                    if (pos < expression.length()) {
                        throw new IllegalArgumentException("trailing: " + expression.substring(pos));
                    }
                    return value;
                }

                double parseExpression() {
                    double value = parseTerm();
                    for (; ; ) {
                        spaces();
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
                    for (; ; ) {
                        spaces();
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
                    spaces();
                    if (ch == '+' || ch == '-') {
                        int sign = ch == '-' ? -1 : 1;
                        next();
                        return sign * parseFactor();
                    }
                    if (ch == '(') {
                        next();
                        double value = parseExpression();
                        spaces();
                        if (ch != ')') {
                            throw new IllegalArgumentException("expected )");
                        }
                        next();
                        return value;
                    }
                    int start = pos;
                    while (Character.isDigit(ch) || ch == '.') {
                        next();
                    }
                    if (start == pos) {
                        throw new IllegalArgumentException("number expected");
                    }
                    return Double.parseDouble(expression.substring(start, pos));
                }
            }.parse();
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private void grant(Player player, Job job, double exp, double money) {
        if (exp <= 0 && money <= 0) {
            return;
        }
        Map<String, double[]> data = progress.computeIfAbsent(player.getUniqueId(),
                uuid -> new LinkedHashMap<>());
        double[] state = data.computeIfAbsent(job.id, id -> new double[]{1, 0});
        if (money > 0) {
            var economy = economy();
            if (economy != null) {
                economy.depositPlayer(player, money);
            }
        }
        if (exp <= 0) {
            return;
        }
        state[1] += exp;
        int levels = 0;
        while (state[1] >= expNeeded(job, (int) state[0] + 1)) {
            state[1] -= expNeeded(job, (int) state[0] + 1);
            state[0] += 1;
            levels++;
        }
        showBossBar(player, job, state, exp);
        for (int i = 0; i < levels; i++) {
            levelUp(player, job, (int) state[0]);
        }
    }

    private void levelUp(Player player, Job job, int level) {
        if (job.levelUpChat != null && !job.levelUpChat.isBlank()) {
            player.sendMessage(Text.parse(job.levelUpChat
                    .replace("<level>", String.valueOf(level))
                    .replace("<job>", job.name)
                    .replace("<player>", player.getName())));
        }
        if (job.levelUpSound != null && !job.levelUpSound.isBlank()) {
            playSound(player, job.levelUpSound);
        }
        for (String command : job.levelUpCommands) {
            String resolved = command
                    .replace("<player>", player.getName())
                    .replace("<level>", String.valueOf(level));
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), resolved);
        }
    }

    private void playSound(Player player, String key) {
        String normalized = key.contains(":") ? key : "minecraft:" + key.toLowerCase(Locale.ROOT);
        var sound = org.bukkit.Registry.SOUND_EVENT.get(
                org.bukkit.NamespacedKey.fromString(normalized));
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        }
    }

    private void showBossBar(Player player, Job job, double[] state, double gained) {
        if (!cfgBool("progress-bossbars", true) || job.bossBarTitle == null) {
            return;
        }
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), uuid -> {
            BossBar created = BossBar.bossBar(net.kyori.adventure.text.Component.empty(),
                    1.0F, bossColor(job.bossBarColor), bossOverlay(job.bossBarOverlay));
            player.showBossBar(created);
            return created;
        });
        double required = expNeeded(job, (int) state[0] + 1);
        bar.progress(Math.max(0F, Math.min(1F, (float) (state[1] / required))));
        bar.name(Text.parse(job.bossBarTitle
                .replace("<level>", String.valueOf((int) state[0]))
                .replace("<job>", job.name)
                .replace("<gained_exp>", formatNumber(gained))
                .replace("<exp>", formatNumber(state[1]))
                .replace("<next_exp>", formatNumber(required))
                .replace("<symbol_exp>", "✦")));
        // hide it again shortly after the gain
        syncLater(60L, () -> {
            BossBar current = bossBars.remove(player.getUniqueId());
            if (current != null && player.isOnline()) {
                player.hideBossBar(current);
            }
        });
    }

    private BossBar.Color bossColor(String raw) {
        try {
            return BossBar.Color.valueOf((raw == null ? "GREEN" : raw).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.GREEN;
        }
    }

    private BossBar.Overlay bossOverlay(String raw) {
        try {
            return BossBar.Overlay.valueOf((raw == null ? "PROGRESS" : raw).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    /* ------------------------------------------------------------------ */
    /* action listeners                                                   */
    /* ------------------------------------------------------------------ */

    private void handle(Player player, String action, String key) {
        for (Job job : jobs.values()) {
            Reward reward = job.reward(action, key);
            if (reward != null) {
                grant(player, job, reward.exp(), reward.money());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || event.getEntity() instanceof Player) {
            return;
        }
        EntityType type = event.getEntityType();
        handle(killer, "kill", type.name().toLowerCase(Locale.ROOT));
        if (event.getEntity() instanceof org.bukkit.entity.Ageable ageable && !ageable.isAdult()) {
            handle(killer, "kill", "baby_" + type.name().toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        handle(event.getPlayer(), "break", event.getBlock().getType().name().toLowerCase(Locale.ROOT));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        handle(event.getPlayer(), "place", event.getBlock().getType().name().toLowerCase(Locale.ROOT));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreed(EntityBreedEvent event) {
        if (event.getBreeder() instanceof Player player) {
            handle(player, "breed", event.getEntityType().name().toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        if (event.getOwner() instanceof Player player) {
            handle(player, "tame", event.getEntityType().name().toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                || !(event.getCaught() instanceof org.bukkit.entity.Item caught)) {
            return;
        }
        if (caught.getItemStack().getType() == Material.AIR) {
            return;
        }
        handle(event.getPlayer(), "fish", caught.getItemStack().getType().name().toLowerCase(Locale.ROOT));
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        handle(event.getEnchanter(), "enchant", "*");
    }

    @EventHandler(ignoreCancelled = true)
    public void onHarvest(PlayerHarvestBlockEvent event) {
        handle(event.getPlayer(), "harvest", event.getHarvestedBlock().getType().name().toLowerCase(Locale.ROOT));
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmelt(FurnaceExtractEvent event) {
        handle(event.getPlayer(), "smelt", event.getItemType().name().toLowerCase(Locale.ROOT));
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class JobsCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                if (!(sender instanceof Player player)) {
                    send(sender, "player-only");
                    return true;
                }
                list(player);
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            switch (sub) {
                case "reload" -> {
                    if (!sender.hasPermission("samplese.jobs.admin")) {
                        sender.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    loadJobs();
                    send(sender, "reloaded", "jobs", String.valueOf(jobs.size()));
                }
                case "list" -> {
                    if (sender instanceof Player player) {
                        list(player);
                    } else {
                        send(sender, "list-console", "jobs", String.join(", ", jobs.keySet()));
                    }
                }
                case "join" -> {
                    if (!(sender instanceof Player player)) {
                        send(sender, "player-only");
                        return true;
                    }
                    if (args.length < 2) {
                        send(player, "usage-join");
                        return true;
                    }
                    join(player, args[1]);
                }
                case "leave", "quit" -> {
                    if (!(sender instanceof Player player)) {
                        send(sender, "player-only");
                        return true;
                    }
                    if (args.length < 2) {
                        send(player, "usage-leave");
                        return true;
                    }
                    leave(player, args[1]);
                }
                case "stats", "info" -> {
                    Player target = args.length > 1 ? plugin.getServer().getPlayer(args[1])
                            : sender instanceof Player player ? player : null;
                    if (target == null) {
                        if (args.length > 1) {
                            sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[1]));
                        } else {
                            send(sender, "player-only");
                        }
                        return true;
                    }
                    stats(sender, target);
                }
                case "top", "leaderboard" -> {
                    if (args.length < 2) {
                        send(sender, "usage-top");
                        return true;
                    }
                    top(sender, args[1]);
                }
                case "addexp" -> {
                    if (!sender.hasPermission("samplese.jobs.admin")) {
                        sender.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    if (args.length < 4) {
                        send(sender, "usage-addexp");
                        return true;
                    }
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[1]));
                        return true;
                    }
                    Job job = jobs.get(args[2].toLowerCase(Locale.ROOT));
                    if (job == null) {
                        send(sender, "unknown-job", "job", args[2]);
                        return true;
                    }
                    Double amount = parseDouble(args[3]);
                    if (amount == null) {
                        send(sender, "usage-addexp");
                        return true;
                    }
                    grant(target, job, amount, 0);
                    saveProgress();
                    send(sender, "exp-added", "player", target.getName(),
                            "job", job.name, "amount", formatNumber(amount));
                }
                case "setlevel" -> {
                    if (!sender.hasPermission("samplese.jobs.admin")) {
                        sender.sendMessage(plugin.getLang().component("no-permission"));
                        return true;
                    }
                    if (args.length < 4) {
                        send(sender, "usage-setlevel");
                        return true;
                    }
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[1]));
                        return true;
                    }
                    Job job = jobs.get(args[2].toLowerCase(Locale.ROOT));
                    if (job == null) {
                        send(sender, "unknown-job", "job", args[2]);
                        return true;
                    }
                    Double level = parseDouble(args[3]);
                    if (level == null) {
                        send(sender, "usage-setlevel");
                        return true;
                    }
                    Map<String, double[]> data = progress.computeIfAbsent(target.getUniqueId(),
                            uuid -> new LinkedHashMap<>());
                    data.put(job.id, new double[]{Math.max(1, level), 0});
                    saveProgress();
                    send(sender, "level-set", "player", target.getName(),
                            "job", job.name, "level", formatNumber(level));
                }
                default -> send(sender, "usage");
            }
            return true;
        }

        private void list(Player player) {
            send(player, "list-header", "jobs", String.valueOf(jobs.size()));
            Map<String, double[]> data = progress.getOrDefault(player.getUniqueId(), Map.of());
            for (Job job : jobs.values()) {
                double[] state = data.get(job.id);
                double level = state == null ? 1 : state[0];
                player.sendMessage(Text.parse(msg("list-entry",
                        "job", job.name,
                        "level", formatNumber(level),
                        "exp", formatNumber(state == null ? 0 : state[1]),
                        "next", formatNumber(expNeeded(job, (int) level + 1)))));
            }
        }

        private void join(Player player, String rawJob) {
            Job job = jobs.get(rawJob.toLowerCase(Locale.ROOT));
            if (job == null) {
                send(player, "unknown-job", "job", rawJob);
                return;
            }
            Map<String, double[]> data = progress.computeIfAbsent(player.getUniqueId(),
                    uuid -> new LinkedHashMap<>());
            data.computeIfAbsent(job.id, id -> new double[]{1, 0});
            saveProgress();
            send(player, "joined", "job", job.name);
        }

        private void leave(Player player, String rawJob) {
            Job job = jobs.get(rawJob.toLowerCase(Locale.ROOT));
            if (job == null) {
                send(player, "unknown-job", "job", rawJob);
                return;
            }
            Map<String, double[]> data = progress.get(player.getUniqueId());
            if (data == null || data.remove(job.id) == null) {
                send(player, "not-in-job", "job", job.name);
                return;
            }
            saveProgress();
            send(player, "left", "job", job.name);
        }

        private void stats(CommandSender sender, Player target) {
            Map<String, double[]> data = progress.getOrDefault(target.getUniqueId(), Map.of());
            if (data.isEmpty()) {
                send(sender, "stats-empty", "player", target.getName());
                return;
            }
            send(sender, "stats-header", "player", target.getName());
            for (Job job : jobs.values()) {
                double[] state = data.get(job.id);
                if (state == null) {
                    continue;
                }
                sender.sendMessage(Text.parse(msg("stats-line",
                        "job", job.name,
                        "level", formatNumber(state[0]),
                        "exp", formatNumber(state[1]),
                        "next", formatNumber(expNeeded(job, (int) state[0] + 1)))));
            }
        }

        private void top(CommandSender sender, String rawJob) {
            Job job = jobs.get(rawJob.toLowerCase(Locale.ROOT));
            if (job == null) {
                send(sender, "unknown-job", "job", rawJob);
                return;
            }
            record Entry(String name, double level, double exp) {
            }
            List<Entry> entries = new ArrayList<>();
            for (Map.Entry<UUID, Map<String, double[]>> entry : progress.entrySet()) {
                double[] state = entry.getValue().get(job.id);
                if (state == null) {
                    continue;
                }
                String name = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                entries.add(new Entry(name == null ? entry.getKey().toString() : name,
                        state[0], state[1]));
            }
            entries.sort(Comparator.comparingDouble(Entry::level).reversed()
                    .thenComparing(Comparator.comparingDouble(Entry::exp).reversed()));
            send(sender, "top-header", "job", job.name);
            int rank = 1;
            for (Entry entry : entries) {
                if (rank > 10) {
                    break;
                }
                sender.sendMessage(Text.parse(msg("top-line",
                        "rank", String.valueOf(rank),
                        "player", entry.name(),
                        "level", formatNumber(entry.level()),
                        "exp", formatNumber(entry.exp()))));
                rank++;
            }
            if (entries.isEmpty()) {
                send(sender, "top-empty");
            }
        }

        private Double parseDouble(String raw) {
            try {
                return Double.parseDouble(raw);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("list", "join", "leave", "stats", "top"));
                if (sender.hasPermission("samplese.jobs.admin")) {
                    options.addAll(List.of("addexp", "setlevel", "reload"));
                }
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2) {
                String sub = args[0].toLowerCase(Locale.ROOT);
                if (sub.equals("join") || sub.equals("leave") || sub.equals("top")) {
                    return new ArrayList<>(jobs.keySet());
                }
            }
            if (args.length == 4 && args[0].equalsIgnoreCase("setlevel")) {
                return new ArrayList<>(jobs.keySet());
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

    private void send(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
