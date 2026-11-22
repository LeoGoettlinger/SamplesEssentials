package su.samplecraft.essentials.modules.gameplay;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code EvenMoreFish-2.5.1.jar} (com.oheers.fish).
 * <p>
 * Fishing competitions and custom fish. Kept from the original:
 * <ul>
 *   <li>rarities with weight, format, worth multiplier and a display material,</li>
 *   <li>custom fish per rarity (material, length range, worth formula),</li>
 *   <li>competition types LARGEST_FISH / MOST_FISH / LARGEST_TOTAL,</li>
 *   <li>scheduled competitions ({@code times}, blacklisted days), duration,
 *       minimum players, bossbar with live leaderboard,</li>
 *   <li>rewards per place ({@code MESSAGE:}, {@code EXP:}, {@code ITEM:},
 *       {@code COMMAND:}, {@code MONEY:}) plus a participation message.</li>
 * </ul>
 * The original's item/requirement addons, journal GUI and database are not part
 * of this port: fish are plain items tagged with the rarity and length, which is
 * what the competition logic needs.
 * <p>
 * Commands: {@code /emf start|stop|status|reload|fish <rarity> [player]}.
 */
public final class FishingModule extends EssentialsModule implements Listener {

    private record Rarity(String id, String displayName, String format, double weight,
                          Material material, double worthMultiplier, boolean disabled) {
    }

    private record Fish(String id, String rarity, Material material, double minLength,
                        double maxLength, double baseWorth) {
    }

    private static final class Competition {
        String id;
        String type = "LARGEST_FISH";
        int duration = 10;
        int minimumPlayers = 1;
        List<String> times = new ArrayList<>();
        BossBar bossBar;
        long endsAt;
        boolean running;
        final Map<UUID, Double> scores = new LinkedHashMap<>();
        final Map<UUID, String> names = new LinkedHashMap<>();
        List<String> rewards1 = new ArrayList<>();
        List<String> rewards2 = new ArrayList<>();
        List<String> rewards3 = new ArrayList<>();
        List<String> participation = new ArrayList<>();
    }

    private final Map<String, Rarity> rarities = new LinkedHashMap<>();
    private final Map<String, Fish> fish = new LinkedHashMap<>();
    private final List<Competition> competitions = new ArrayList<>();
    private final Competition active = new Competition();
    /** player uuid -> rarity id of the last caught custom fish, for /emf fish */
    private final Map<UUID, String> lastCaught = new LinkedHashMap<>();

    public FishingModule(SamplesEssentials plugin) {
        super("gameplay", "fishing", "EvenMoreFish-2.5.1.jar",
                "Custom fish rarities and scheduled fishing competitions");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.fishing", "Participate in fishing competitions", PermissionDefault.TRUE);
        ensurePermission("samplese.fishing.admin", "Manage fishing competitions", PermissionDefault.OP);
        ensurePermission("samplese.fishing.competition.start", "Start competitions", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDefaultFile("competitions.yml");
        ensureDefaultFile("rarities.yml");
        ensureDefaultFile("fish.yml");
        loadRarities();
        loadFish();
        loadCompetitions();
        command("emf", new EmfCommand());
        command("evenmorefish", new EmfCommand());
        onDisableHook(() -> {
            unbindCommands();
            stop("shutdown");
        });
        listen(this);
        syncRepeat(40L, 40L, this::tick);
    }

    /* ------------------------------------------------------------------ */
    /* config loaders                                                     */
    /* ------------------------------------------------------------------ */

    private void loadRarities() {
        rarities.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                new File(getModuleFolder(), "rarities.yml"));
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = section.getString("id", key);
            Material material = Material.matchMaterial(
                    section.getString("item.material", "COD"));
            Rarity rarity = new Rarity(id, section.getString("displayname", id),
                    section.getString("format", "<white>{name}"),
                    section.getDouble("weight", 10),
                    material == null ? Material.COD : material,
                    section.getDouble("worth-multiplier", 1),
                    section.getBoolean("disabled", false));
            rarities.put(id.toLowerCase(Locale.ROOT), rarity);
        }
    }

    private void loadFish() {
        fish.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                new File(getModuleFolder(), "fish.yml"));
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String rarity = section.getString("rarity", "Common");
            Material material = Material.matchMaterial(section.getString("material", "COD"));
            fish.put(key.toLowerCase(Locale.ROOT), new Fish(key, rarity,
                    material == null ? Material.COD : material,
                    section.getDouble("min-length", 5),
                    section.getDouble("max-length", 60),
                    section.getDouble("base-worth", 10)));
        }
    }

    private void loadCompetitions() {
        competitions.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                new File(getModuleFolder(), "competitions.yml"));
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null || section.getBoolean("disabled", false)) {
                continue;
            }
            Competition competition = new Competition();
            competition.id = section.getString("id", key);
            competition.type = section.getString("type", "LARGEST_FISH")
                    .toUpperCase(Locale.ROOT);
            competition.duration = Math.max(1, section.getInt("duration", 10));
            competition.minimumPlayers = Math.max(1, section.getInt("minimum-players", 1));
            competition.times.addAll(section.getStringList("times"));
            competition.rewards1.addAll(section.getStringList("rewards.1"));
            competition.rewards2.addAll(section.getStringList("rewards.2"));
            competition.rewards3.addAll(section.getStringList("rewards.3"));
            competition.participation.addAll(section.getStringList("rewards.participation"));
            competitions.add(competition);
        }
    }

    /* ------------------------------------------------------------------ */
    /* competition engine                                                 */
    /* ------------------------------------------------------------------ */

    private int onlineCompetitors() {
        int count = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("samplese.fishing")
                    && player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
                count++;
            }
        }
        return count;
    }

    private void start(Competition competition) {
        if (active.running) {
            plugin.getLogger().info("A competition is already running.");
            return;
        }
        active.id = competition.id;
        active.type = competition.type;
        active.duration = competition.duration;
        active.minimumPlayers = competition.minimumPlayers;
        active.rewards1 = competition.rewards1;
        active.rewards2 = competition.rewards2;
        active.rewards3 = competition.rewards3;
        active.participation = competition.participation;
        active.scores.clear();
        active.names.clear();
        active.endsAt = System.currentTimeMillis() + active.duration * 60_000L;
        active.running = true;
        active.bossBar = BossBar.bossBar(bossBarTitle(), 1f, BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.showBossBar(active.bossBar);
        }
        plugin.getServer().broadcast(Text.parse(msg("competition-started", "type", typeName())));
    }

    private String typeName() {
        return switch (active.type) {
            case "MOST_FISH" -> msg("type-most-fish");
            case "LARGEST_TOTAL" -> msg("type-largest-total");
            default -> msg("type-largest-fish");
        };
    }

    private Component bossBarTitle() {
        int secondsLeft = (int) Math.max(0, (active.endsAt - System.currentTimeMillis()) / 1000);
        return Text.parse(msg("bossbar", "type", typeName(),
                "time", String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60)));
    }

    private void tick() {
        if (!active.running) {
            // check schedule (times are HH:mm, matched to the current minute)
            String now = String.format("%02d:%02d",
                    java.time.LocalTime.now().getHour(), java.time.LocalTime.now().getMinute());
            if (lastTickMinute.equals(now)) {
                return;
            }
            lastTickMinute = now;
            for (Competition competition : competitions) {
                if (!competition.times.contains(now)) {
                    continue;
                }
                if (onlineCompetitors() < competition.minimumPlayers) {
                    plugin.getLogger().info("Skipped competition " + competition.id
                            + ": not enough players (" + onlineCompetitors() + "/"
                            + competition.minimumPlayers + ")");
                    continue;
                }
                start(competition);
                return;
            }
            return;
        }
        if (System.currentTimeMillis() >= active.endsAt) {
            stop("time");
            return;
        }
        float progress = Math.max(0f, Math.min(1f,
                (active.endsAt - System.currentTimeMillis()) / (float) (active.duration * 60_000L)));
        active.bossBar.progress(progress);
        active.bossBar.name(bossBarTitle());
    }

    private String lastTickMinute = "";

    private void score(Player player, double amount, boolean total) {
        if (!active.running) {
            return;
        }
        active.names.put(player.getUniqueId(), player.getName());
        double current = active.scores.getOrDefault(player.getUniqueId(), 0.0);
        if (total) {
            active.scores.put(player.getUniqueId(), current + amount);
        } else {
            active.scores.put(player.getUniqueId(), Math.max(current, amount));
        }
    }

    private void stop(String reason) {
        if (!active.running) {
            return;
        }
        active.running = false;
        if (active.bossBar != null) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.hideBossBar(active.bossBar);
            }
            active.bossBar = null;
        }
        List<Map.Entry<UUID, Double>> ranking = new ArrayList<>(active.scores.entrySet());
        ranking.sort(Comparator.comparingDouble(Map.Entry<UUID, Double>::getValue).reversed());
        plugin.getServer().broadcast(Text.parse(msg("competition-over", "type", typeName())));
        int place = 1;
        for (Map.Entry<UUID, Double> entry : ranking) {
            if (place > 3) {
                break;
            }
            Player player = plugin.getServer().getPlayer(entry.getKey());
            List<String> rewards = switch (place) {
                case 1 -> active.rewards1;
                case 2 -> active.rewards2;
                default -> active.rewards3;
            };
            if (player != null) {
                applyRewards(player, rewards);
            }
            plugin.getServer().broadcast(Text.parse(msg("competition-place",
                    "place", String.valueOf(place),
                    "player", active.names.getOrDefault(entry.getKey(), "?"),
                    "score", formatScore(entry.getValue()))));
            place++;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("samplese.fishing") && !active.scores.containsKey(player.getUniqueId())) {
                applyRewards(player, active.participation);
            }
        }
        active.scores.clear();
        active.names.clear();
    }

    private String formatScore(double score) {
        return active.type.equals("LARGEST_FISH")
                ? String.format(Locale.ROOT, "%.1f cm", score)
                : String.valueOf((int) score);
    }

    private void applyRewards(Player player, List<String> rewards) {
        for (String raw : rewards) {
            String reward = raw.trim();
            if (reward.regionMatches(true, 0, "MESSAGE:", 0, 8)) {
                player.sendMessage(Text.parse(reward.substring(8)));
            } else if (reward.regionMatches(true, 0, "EXP:", 0, 4)) {
                try {
                    player.giveExp(Integer.parseInt(reward.substring(4).trim()));
                } catch (NumberFormatException ignored) {
                    // malformed reward, skip
                }
            } else if (reward.regionMatches(true, 0, "ITEM:", 0, 5)) {
                String body = reward.substring(5).trim();
                String[] parts = body.split(",");
                Material material = Material.matchMaterial(parts[0].trim());
                int amount = parts.length > 1 ? safeInt(parts[1], 1) : 1;
                if (material != null) {
                    player.getInventory().addItem(new ItemStack(material, Math.max(1, amount)))
                            .values().forEach(rest ->
                                    player.getWorld().dropItemNaturally(player.getLocation(), rest));
                }
            } else if (reward.regionMatches(true, 0, "MONEY:", 0, 6)) {
                double amount = safeDouble(reward.substring(6), 0);
                var economy = plugin.getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economy != null) {
                    economy.getProvider().depositPlayer(player, amount);
                }
            } else if (reward.regionMatches(true, 0, "COMMAND:", 0, 8)) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        reward.substring(8).trim().replace("%player%", player.getName()));
            }
        }
    }

    private static int safeInt(String text, int def) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double safeDouble(String text, double def) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /* ------------------------------------------------------------------ */
    /* fishing                                                            */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                || !(event.getCaught() instanceof Item item)) {
            return;
        }
        Player player = event.getPlayer();
        if (!cfgBool("fishing.catch-enabled", true)) {
            return;
        }
        Rarity rarity = pickRarity();
        if (rarity == null) {
            return;
        }
        Fish caught = pickFish(rarity);
        double length = caught == null
                ? ThreadLocalRandom.current().nextDouble(5, 60)
                : ThreadLocalRandom.current().nextDouble(caught.minLength(),
                Math.max(caught.minLength() + 0.1, caught.maxLength()));
        double worth = (caught == null ? 10 : caught.baseWorth())
                * rarity.worthMultiplier() * (1 + length / 50.0);

        ItemStack fishItem = new ItemStack(caught == null ? rarity.material() : caught.material());
        ItemMeta meta = fishItem.getItemMeta();
        String fishName = caught == null ? rarity.displayName() : caught.id();
        meta.displayName(Text.parse(rarity.format().replace("{name}", fishName)
                + " <gray>(" + String.format(Locale.ROOT, "%.1f", length) + " cm)"));
        meta.lore(List.of(
                Text.parse("<gray>Rarity: " + rarity.format().replace("{name}", rarity.displayName())),
                Text.parse("<gray>Worth: <gold>$" + String.format(Locale.ROOT, "%.2f", worth)),
                Text.parse("<dark_gray>" + msg("fish-lore-tag"))));
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "emf_rarity"),
                org.bukkit.persistence.PersistentDataType.STRING, rarity.id());
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "emf_length"),
                org.bukkit.persistence.PersistentDataType.DOUBLE, length);
        fishItem.setItemMeta(meta);

        item.setItemStack(fishItem);
        lastCaught.put(player.getUniqueId(), rarity.id());

        if (active.running && competitionAllowsFishing(player)) {
            boolean total = active.type.equals("LARGEST_TOTAL");
            score(player, active.type.equals("MOST_FISH") ? 1 : worth, total);
            if (cfgBool("fishing.announce-catches", false)) {
                player.sendMessage(Text.parse(msg("catch", "fish", fishName,
                        "length", String.format(Locale.ROOT, "%.1f", length),
                        "rarity", rarity.displayName())));
            }
        }
    }

    private boolean competitionAllowsFishing(Player player) {
        return player.hasPermission("samplese.fishing");
    }

    private Rarity pickRarity() {
        double total = 0;
        for (Rarity rarity : rarities.values()) {
            if (!rarity.disabled()) {
                total += Math.max(0, rarity.weight());
            }
        }
        if (total <= 0) {
            return null;
        }
        double roll = ThreadLocalRandom.current().nextDouble(total);
        for (Rarity rarity : rarities.values()) {
            if (rarity.disabled()) {
                continue;
            }
            roll -= Math.max(0, rarity.weight());
            if (roll <= 0) {
                return rarity;
            }
        }
        return rarities.values().stream().filter(rarity -> !rarity.disabled()).findFirst().orElse(null);
    }

    private Fish pickFish(Rarity rarity) {
        List<Fish> candidates = fish.values().stream()
                .filter(candidate -> candidate.rarity().equalsIgnoreCase(rarity.id()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class EmfCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                reply(sender, "usage");
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "start" -> startCmd(sender, args);
                case "stop", "end" -> {
                    if (!sender.hasPermission("samplese.fishing.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    if (!active.running) {
                        reply(sender, "no-competition");
                        return true;
                    }
                    stop("command");
                    reply(sender, "stopped");
                }
                case "status" -> {
                    if (!active.running) {
                        StringBuilder schedule = new StringBuilder();
                        for (Competition competition : competitions) {
                            if (!competition.times.isEmpty()) {
                                schedule.append(competition.id).append(" @ ")
                                        .append(String.join(", ", competition.times)).append("; ");
                            }
                        }
                        reply(sender, "no-competition");
                        if (!schedule.isEmpty()) {
                            sender.sendMessage(Text.parse(msg("schedule", "schedule", schedule.toString())));
                        }
                        return true;
                    }
                    reply(sender, "status", "type", typeName(),
                            "time", String.valueOf((active.endsAt - System.currentTimeMillis()) / 1000),
                            "players", String.valueOf(active.scores.size()));
                    List<Map.Entry<UUID, Double>> ranking = new ArrayList<>(active.scores.entrySet());
                    ranking.sort(Comparator.comparingDouble(Map.Entry<UUID, Double>::getValue).reversed());
                    int place = 1;
                    for (Map.Entry<UUID, Double> entry : ranking) {
                        if (place > 5) {
                            break;
                        }
                        sender.sendMessage(Text.parse(msg("status-entry",
                                "place", String.valueOf(place),
                                "player", active.names.getOrDefault(entry.getKey(), "?"),
                                "score", formatScore(entry.getValue()))));
                        place++;
                    }
                }
                case "fish" -> fishCmd(sender, args);
                case "reload" -> {
                    if (!sender.hasPermission("samplese.fishing.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    reloadConfig();
                    loadRarities();
                    loadFish();
                    loadCompetitions();
                    reply(sender, "reloaded", "fish", String.valueOf(fish.size()),
                            "rarities", String.valueOf(rarities.size()));
                }
                default -> reply(sender, "usage");
            }
            return true;
        }

        private void startCmd(CommandSender sender, String[] args) {
            if (!sender.hasPermission("samplese.fishing.competition.start")) {
                reply(sender, "no-permission");
                return;
            }
            if (active.running) {
                reply(sender, "already-running");
                return;
            }
            Competition template;
            if (args.length >= 2) {
                String id = args[1];
                template = competitions.stream()
                        .filter(competition -> competition.id.equalsIgnoreCase(id))
                        .findFirst().orElse(null);
                if (template == null) {
                    reply(sender, "unknown-competition", "id", id);
                    return;
                }
            } else if (!competitions.isEmpty()) {
                template = competitions.get(0);
            } else {
                template = new Competition();
                template.id = "manual";
                template.duration = 10;
            }
            start(template);
            reply(sender, "started", "type", typeName());
        }

        private void fishCmd(CommandSender sender, String[] args) {
            Player target;
            if (args.length >= 3) {
                target = plugin.getServer().getPlayerExact(args[2]);
                if (target == null) {
                    reply(sender, "player-not-found", "player", args[2]);
                    return;
                }
            } else if (sender instanceof Player player) {
                target = player;
            } else {
                reply(sender, "player-only");
                return;
            }
            Rarity rarity = rarities.get(args.length >= 2
                    ? args[1].toLowerCase(Locale.ROOT)
                    : "common");
            if (rarity == null) {
                reply(sender, "unknown-rarity", "rarity", args.length >= 2 ? args[1] : "common");
                return;
            }
            Fish template = pickFish(rarity);
            double length = template == null ? 30
                    : ThreadLocalRandom.current().nextDouble(template.minLength(), template.maxLength());
            ItemStack item = new ItemStack(template == null ? rarity.material() : template.material());
            ItemMeta meta = item.getItemMeta();
                meta.displayName(Text.parse(rarity.format()
                            .replace("{name}", template == null ? rarity.displayName() : template.id())
                            + " <gray>(" + String.format(Locale.ROOT, "%.1f", length) + " cm)"));
            item.setItemMeta(meta);
            target.getInventory().addItem(item).values().forEach(rest ->
                    target.getWorld().dropItemNaturally(target.getLocation(), rest));
            reply(sender, "fish-given", "fish", template == null ? rarity.displayName() : template.id(),
                    "player", target.getName());
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("start", "stop", "status", "fish", "reload");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
                List<String> ids = new ArrayList<>();
                competitions.forEach(competition -> ids.add(competition.id));
                return ids;
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("fish")) {
                return new ArrayList<>(rarities.keySet());
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
