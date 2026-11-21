package su.samplecraft.essentials.modules.gameplay;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code AxAFKZone-1.13.0.jar} (com.artillexstudios.axafkzone).
 * <p>
 * Cuboid AFK zones defined in {@code zones/<name>.yml}. Every 20 ticks (1s) each
 * zone counts the time its occupants spend inside; every {@code reward-time-seconds}
 * it rolls {@code roll-amount} weighted rewards (chance &gt; 100 is allowed and simply
 * raises the weight) restricted by reward permission and min/max time. Rewards run
 * console commands with {@code %player%} and/or drop items into the player's
 * inventory, exactly like the original. While inside, players see the configured
 * title, subtitle, action bar and boss bar with a live countdown.
 * <p>
 * Commands: {@code /axafkzone wand|create <name>|delete <name>|redefine <name>|tp <name>|reload|help}
 * (aliases {@code afkzone}, {@code axaz}). The wand is a golden axe tagged through
 * the PDC; left click sets corner 1, right click corner 2, and the selection is
 * visualised with particles.
 */
public final class AfkZoneModule extends EssentialsModule implements Listener {

    private record Reward(List<String> commands, List<ItemStack> items, String permission,
                          int minimumTime, int maximumTime, double chance, String display) {
    }

    private static final class Selection {
        private Location first;
        private Location second;
        private final List<Location> border = new ArrayList<>();

        void updateBorder() {
            border.clear();
            if (first == null || second == null || first.getWorld() == null || second.getWorld() == null
                    || !first.getWorld().equals(second.getWorld())) {
                return;
            }
            World world = first.getWorld();
            int minX = Math.min(first.getBlockX(), second.getBlockX());
            int maxX = Math.max(first.getBlockX(), second.getBlockX());
            int minY = Math.min(first.getBlockY(), second.getBlockY());
            int maxY = Math.max(first.getBlockY(), second.getBlockY());
            int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
            int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
            // Only sample reasonable selections to keep the particle budget sane.
            int step = Math.max(1, (maxX - minX + maxY - minY + maxZ - minZ) / 400);
            for (int x = minX; x <= maxX; x += step) {
                for (int y = minY; y <= maxY; y += step) {
                    for (int z = minZ; z <= maxZ; z += step) {
                        int edges = 0;
                        if (x == minX || x == maxX) {
                            edges++;
                        }
                        if (y == minY || y == maxY) {
                            edges++;
                        }
                        if (z == minZ || z == maxZ) {
                            edges++;
                        }
                        if (edges >= 2) {
                            border.add(new Location(world, x + 0.5, y + 0.5, z + 0.5));
                        }
                    }
                }
            }
        }
    }

    /** A single loaded zone file with its own reward list and player timers. */
    private final class Zone {
        private final String name;
        private final File file;
        private YamlConfiguration config;
        private final Map<UUID, Integer> seconds = new LinkedHashMap<>();
        private final Map<UUID, BossBar> bars = new HashMap<>();
        private Location first;
        private Location second;
        private int rewardSeconds = 60;
        private int rollAmount = 1;
        private String permission = "";
        private final List<Reward> rewards = new ArrayList<>();
        private final Map<UUID, Long> ipCooldown = new HashMap<>();

        Zone(String name, File file) {
            this.name = name;
            this.file = file;
            reload();
        }

        boolean reload() {
            try {
                this.config = YamlConfiguration.loadConfiguration(file);
            } catch (Exception e) {
                plugin.getLogger().warning("AFK zone " + name + ": " + e.getMessage());
                return false;
            }
            this.first = location(config.getString("zone.location1"));
            this.second = location(config.getString("zone.location2"));
            this.rewardSeconds = Math.max(1, config.getInt("reward-time-seconds", 60));
            this.rollAmount = Math.max(1, config.getInt("roll-amount", 1));
            this.permission = config.getString("permission", "");
            this.rewards.clear();
            for (Map<?, ?> raw : config.getMapList("rewards")) {
                List<String> commands = new ArrayList<>();
                if (raw.get("commands") instanceof List<?> list) {
                    for (Object entry : list) {
                        commands.add(String.valueOf(entry));
                    }
                }
                List<ItemStack> items = new ArrayList<>();
                if (raw.get("items") instanceof List<?> list) {
                    for (Object entry : list) {
                        ItemStack item = item(entry);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                }
                String rewardPermission = raw.get("permission") == null ? null : String.valueOf(raw.get("permission"));
                int min = raw.get("minimum-time") instanceof Number number ? number.intValue() : 0;
                int max = raw.get("maximum-time") instanceof Number number ? number.intValue() : Integer.MAX_VALUE;
                double chance = raw.get("chance") instanceof Number number ? number.doubleValue() : 10.0D;
                String display = raw.get("display") == null ? null : String.valueOf(raw.get("display"));
                rewards.add(new Reward(commands, items, rewardPermission, min, max, chance, display));
            }
            return true;
        }

        private Location location(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String[] parts = raw.split(",");
            if (parts.length < 4) {
                return null;
            }
            try {
                World world = plugin.getServer().getWorld(parts[0].trim());
                if (world == null) {
                    return null;
                }
                return new Location(world, Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim()), Double.parseDouble(parts[3].trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private ItemStack item(Object raw) {
            if (raw instanceof ItemStack stack) {
                return stack;
            }
            if (!(raw instanceof Map<?, ?> map)) {
                return null;
            }
            Object material = map.get("material");
            Material type = material == null ? null : Material.matchMaterial(String.valueOf(material));
            if (type == null) {
                return null;
            }
            int amount = map.get("amount") instanceof Number number ? number.intValue() : 1;
            ItemStack stack = new ItemStack(type, Math.min(Math.max(amount, 1), type.getMaxStackSize()));
            ItemMeta meta = stack.getItemMeta();
            if (map.get("name") != null) {
                meta.displayName(Text.parse(String.valueOf(map.get("name"))));
            }
            if (map.get("lore") instanceof List<?> lore) {
                List<Component> lines = new ArrayList<>();
                for (Object line : lore) {
                    lines.add(Text.parse(String.valueOf(line)));
                }
                meta.lore(lines);
            }
            stack.setItemMeta(meta);
            return stack;
        }

        boolean contains(Player player) {
            if (first == null || second == null) {
                return false;
            }
            Location location = player.getLocation();
            if (location.getWorld() == null || !location.getWorld().equals(first.getWorld())) {
                return false;
            }
            int minX = Math.min(first.getBlockX(), second.getBlockX());
            int maxX = Math.max(first.getBlockX(), second.getBlockX());
            int minY = Math.min(first.getBlockY(), second.getBlockY());
            int maxY = Math.max(first.getBlockY(), second.getBlockY());
            int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
            int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        boolean allowed(Player player) {
            return permission == null || permission.isBlank() || player.hasPermission(permission);
        }

        void tick() {
            Set<Player> inside = new HashSet<>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.isDead() && contains(player) && allowed(player)) {
                    inside.add(player);
                }
            }
            // leave: offline or no longer inside
            Iterator<Map.Entry<UUID, Integer>> iterator = seconds.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null && player.isOnline() && inside.contains(player)) {
                    continue;
                }
                if (player != null && player.isOnline()) {
                    sendLang(player, "messages.left", "%time%", fancyTime(entry.getValue() * 1000L));
                }
                removeBar(entry.getKey());
                iterator.remove();
                if (player != null && player.isOnline()) {
                    player.sendActionBar(Component.empty());
                    player.clearTitle();
                }
            }
            // enter + timers
            for (Player player : inside) {
                Integer current = seconds.get(player.getUniqueId());
                if (current == null) {
                    enter(player);
                    current = 0;
                }
                int next = current + 1;
                seconds.put(player.getUniqueId(), next);
                if (next % rewardSeconds == 0) {
                    giveRewards(player, next);
                    if (cfgBool("reset-after-reward", false)) {
                        seconds.put(player.getUniqueId(), 0);
                    }
                }
                showHud(player, next);
            }
        }

        private void enter(Player player) {
            removeBar(player.getUniqueId());
            sendLang(player, "messages.entered", "%time%", fancyTime(rewardSeconds * 1000L));
            int ipLimit = cfgInt("zone-per-ip-limit", -1);
            if (ipLimit > 0 && !player.hasPermission("samplese.afkzone.bypass.iplimit")
                    && countSameIp(player) >= ipLimit) {
                sendLang(player, "zone.ip-limit");
                seconds.put(player.getUniqueId(), 0);
                player.sendActionBar(Component.empty());
                return;
            }
            seconds.put(player.getUniqueId(), 0);
            ConfigurationSection bar = config.getConfigurationSection("in-zone.bossbar");
            if (bar != null) {
                BossBar bossBar = BossBar.bossBar(
                        Text.parse(applyTime(bar.getString("name", ""), 0)),
                        1.0F,
                        barColor(bar.getString("color", "RED")),
                        barOverlay(bar.getString("style", "NOTCHED_20")));
                player.showBossBar(bossBar);
                bars.put(player.getUniqueId(), bossBar);
            }
            showHud(player, 0);
        }

        private int countSameIp(Player player) {
            if (player.getAddress() == null || player.getAddress().getAddress() == null) {
                return 0;
            }
            int count = 0;
            for (UUID uuid : seconds.keySet()) {
                Player other = plugin.getServer().getPlayer(uuid);
                if (other == null || other.equals(player) || other.getAddress() == null
                        || other.getAddress().getAddress() == null
                        || other.hasPermission("samplese.afkzone.bypass.iplimit")) {
                    continue;
                }
                if (other.getAddress().getAddress().equals(player.getAddress().getAddress())) {
                    count++;
                }
            }
            return count;
        }

        private void showHud(Player player, int time) {
            long remaining = timeUntilNext(time);
            String title = config.getString("in-zone.title", "");
            String subtitle = config.getString("in-zone.subtitle", "");
            if ((title != null && !title.isBlank()) || (subtitle != null && !subtitle.isBlank())) {
                player.sendTitle(
                        Text.parse(applyTime(title, remaining)),
                        Text.parse(applyTime(subtitle, remaining)),
                        0, 30, 10);
            }
            String actionbar = config.getString("in-zone.actionbar", "");
            if (actionbar != null && !actionbar.isBlank()) {
                player.sendActionBar(Text.parse(applyTime(actionbar, remaining)));
            }
            BossBar bar = bars.get(player.getUniqueId());
            if (bar != null) {
                ConfigurationSection section = config.getConfigurationSection("in-zone.bossbar");
                if (section != null) {
                    bar.name(Text.parse(applyTime(section.getString("name", ""), remaining)));
                    float progress = (float) (time % rewardSeconds) / (rewardSeconds - 1);
                    boolean increasing = cfgInt("bossbar-direction", 0) == 1;
                    bar.progress(Math.max(0.0F, Math.min(1.0F, increasing ? progress : 1.0F - progress)));
                }
            }
        }

        private String applyTime(String raw, long millis) {
            return raw == null ? "" : raw.replace("%time%", fancyTime(millis));
        }

        long timeUntilNext(int time) {
            return rewardSeconds * 1000L - (time % rewardSeconds) * 1000L;
        }

        private void giveRewards(Player player, int time) {
            List<Reward> rolled = roll(player, time);
            if (rolled.isEmpty()) {
                return;
            }
            List<String> messages = config.getStringList("messages.reward");
            if (messages.isEmpty()) {
                return;
            }
            boolean first = true;
            for (String line : messages) {
                String prefixed = first ? msg("prefix") + line : line;
                first = false;
                if (prefixed.contains("%reward%")) {
                    for (Reward reward : rolled) {
                        player.sendMessage(Text.parse(prefixed
                                .replace("%reward%", reward.display() == null ? "---" : reward.display())
                                .replace("%time%", fancyTime(time * 1000L))));
                    }
                } else {
                    player.sendMessage(Text.parse(prefixed.replace("%time%", fancyTime(time * 1000L))));
                }
            }
        }

        private List<Reward> roll(Player player, int time) {
            List<Reward> result = new ArrayList<>();
            if (rewards.isEmpty()) {
                return result;
            }
            Map<Reward, Double> chances = new LinkedHashMap<>();
            for (Reward reward : rewards) {
                String needed = reward.permission();
                if ((needed == null || needed.isBlank() || player.hasPermission(needed))
                        && time >= reward.minimumTime() && time <= reward.maximumTime()
                        && reward.chance() > 0) {
                    chances.put(reward, reward.chance());
                }
            }
            if (chances.isEmpty()) {
                return result;
            }
            for (int i = 0; i < rollAmount; i++) {
                Reward selected = pick(chances);
                result.add(selected);
                run(selected, player);
            }
            return result;
        }

        private Reward pick(Map<Reward, Double> chances) {
            double total = 0;
            for (double weight : chances.values()) {
                total += weight;
            }
            double roll = ThreadLocalRandom.current().nextDouble(total);
            double cursor = 0;
            for (Map.Entry<Reward, Double> entry : chances.entrySet()) {
                cursor += entry.getValue();
                if (roll < cursor) {
                    return entry.getKey();
                }
            }
            return chances.keySet().iterator().next();
        }

        private void run(Reward reward, Player player) {
            for (String command : reward.commands()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        command.replace("%player%", player.getName()));
            }
            for (ItemStack stack : reward.items()) {
                for (ItemStack leftover : player.getInventory().addItem(stack.clone()).values()) {
                    player.getWorld().dropItem(player.getLocation(), leftover);
                }
            }
        }

        private void removeBar(UUID uuid) {
            BossBar bar = bars.remove(uuid);
            Player player = plugin.getServer().getPlayer(uuid);
            if (bar != null && player != null) {
                player.hideBossBar(bar);
            }
        }

        void disable() {
            for (UUID uuid : new ArrayList<>(bars.keySet())) {
                removeBar(uuid);
            }
            seconds.clear();
        }

        Location center() {
            if (first == null || second == null) {
                return null;
            }
            return new Location(first.getWorld(),
                    (first.getBlockX() + second.getBlockX()) / 2.0 + 0.5,
                    (first.getBlockY() + second.getBlockY()) / 2.0 + 0.5,
                    (first.getBlockZ() + second.getBlockZ()) / 2.0 + 0.5);
        }
    }

    private final Map<String, Zone> zones = new LinkedHashMap<>();
    private final Map<UUID, Selection> selections = new HashMap<>();

    public AfkZoneModule(SamplesEssentials plugin) {
        super("gameplay", "afkzone", "AxAFKZone-1.13.0.jar",
                "Cuboid AFK zones with timed, chance-based rewards and scoreboard-style HUD");
    }

    @Override
    protected void onEnable() {
        ensureDefaultFile("config.yml");
        ensureDefaultFile("zones/example-zone.yml");
        reloadConfig();
        loadZones();
        command("axafkzone", new AfkCommand());
        onDisableHook(this::unbindCommands);
        onDisableHook(this::disableAll);
        listen(this);
        syncRepeat(20L, 20L, this::tick);
    }

    private void loadZones() {
        disableAll();
        zones.clear();
        File folder = new File(getModuleFolder(), "zones");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            plugin.getLogger().warning("AFK zones: could not create " + folder);
            return;
        }
        File[] files = folder.listFiles((dir, fileName) -> fileName.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - 4);
            Zone zone = new Zone(name, file);
            zones.put(name, zone);
        }
        plugin.getLogger().info("AFK zones loaded: " + zones.size());
    }

    private void disableAll() {
        zones.values().forEach(Zone::disable);
    }

    private void tick() {
        for (Zone zone : zones.values()) {
            try {
                zone.tick();
            } catch (Exception e) {
                plugin.getLogger().warning("AFK zone " + zone.name + " tick failed: " + e);
            }
        }
        for (Map.Entry<UUID, Selection> entry : selections.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            for (Location point : entry.getValue().border) {
                player.spawnParticle(Particle.WAX_ON, point, 1, 0, 0, 0, 0);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* event handlers                                                     */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        selections.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getItem() == null
                || !isWand(event.getItem())) {
            return;
        }
        event.setCancelled(true);
        Selection selection = selections.computeIfAbsent(event.getPlayer().getUniqueId(),
                uuid -> new Selection());
        Location clicked = event.getClickedBlock().getLocation();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.first = clicked;
            selection.updateBorder();
            sendLang(event.getPlayer(), "selection.pos1", "%location%", formatLocation(clicked));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.second = clicked;
            selection.updateBorder();
            sendLang(event.getPlayer(), "selection.pos2", "%location%", formatLocation(clicked));
        }
        if (selection.first != null && selection.second != null
                && selection.first.getBlockY() == selection.second.getBlockY()) {
            sendLang(event.getPlayer(), "selection.small-selection");
        }
    }

    private String formatLocation(Location location) {
        return String.format(Locale.ROOT, "%s, %d, %d, %d",
                location.getWorld() == null ? "?" : location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private void sendLang(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }

    /* ------------------------------------------------------------------ */
    /* wand helpers                                                       */
    /* ------------------------------------------------------------------ */

    private boolean isWand(ItemStack item) {
        if (item.getType() != Material.GOLDEN_AXE || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .has(new org.bukkit.NamespacedKey(plugin, "afkzone_wand"), PersistentDataType.BOOLEAN);
    }

    private ItemStack wand() {
        ItemStack item = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(msg("selection-wand.name")));
        List<Component> lore = new ArrayList<>();
        for (String line : cfgStringList("selection-wand.lore")) {
            lore.add(Text.parse(line));
        }
        meta.lore(lore);
        meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "afkzone_wand"),
                PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    /* ------------------------------------------------------------------ */
    /* time formatting                                                    */
    /* ------------------------------------------------------------------ */

    private String fancyTime(long millis) {
        if (millis < 0) {
            return "---";
        }
        long total = millis / 1000L;
        long days = total / 86400L;
        long hours = (total % 86400L) / 3600L;
        long minutes = (total % 3600L) / 60L;
        long secs = total % 60L;
        int format = cfgInt("timer-format", 1);
        if (format == 1) {
            if (days > 0) {
                return String.format(Locale.ROOT, "%02d:%02d:%02d:%02d", days, hours, minutes, secs);
            }
            if (hours > 0) {
                return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs);
            }
            return String.format(Locale.ROOT, "%02d:%02d", minutes, secs);
        }
        String daySuffix = msg("time.day");
        String hourSuffix = msg("time.hour");
        String minuteSuffix = msg("time.minute");
        String secondSuffix = msg("time.second");
        if (format == 2) {
            if (days > 0) {
                return days + daySuffix;
            }
            if (hours > 0) {
                return hours + hourSuffix;
            }
            if (minutes > 0) {
                return minutes + minuteSuffix;
            }
            return secs + secondSuffix;
        }
        if (days > 0) {
            return String.format(Locale.ROOT, "%02d%s %02d%s %02d%s %02d%s",
                    days, daySuffix, hours, hourSuffix, minutes, minuteSuffix, secs, secondSuffix);
        }
        if (hours > 0) {
            return String.format(Locale.ROOT, "%02d%s %02d%s %02d%s",
                    hours, hourSuffix, minutes, minuteSuffix, secs, secondSuffix);
        }
        return String.format(Locale.ROOT, "%02d%s %02d%s", minutes, minuteSuffix, secs, secondSuffix);
    }

    private BossBar.Color barColor(String raw) {
        try {
            return BossBar.Color.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.RED;
        }
    }

    private BossBar.Overlay barOverlay(String raw) {
        try {
            return BossBar.Overlay.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.NOTCHED_20;
        }
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class AfkCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                for (String line : cfgStringList("help")) {
                    sender.sendMessage(Text.parse(line));
                }
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (!sender.hasPermission("samplese.afkzone.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            switch (sub) {
                case "reload" -> {
                    reloadConfig();
                    loadZones();
                    sendLang(sender, "reload.success");
                }
                case "wand" -> {
                    if (!(sender instanceof Player player)) {
                        sendLang(sender, "commands.player-only");
                        return true;
                    }
                    player.getInventory().addItem(wand());
                }
                case "create" -> {
                    if (!(sender instanceof Player player)) {
                        sendLang(sender, "commands.player-only");
                        return true;
                    }
                    if (args.length < 2) {
                        sendLang(sender, "commands.missing-argument", "%value%", "zone");
                        return true;
                    }
                    create(player, args[1]);
                }
                case "delete" -> {
                    if (args.length < 2) {
                        sendLang(sender, "commands.missing-argument", "%value%", "zone");
                        return true;
                    }
                    delete(sender, args[1]);
                }
                case "redefine" -> {
                    if (!(sender instanceof Player player)) {
                        sendLang(sender, "commands.player-only");
                        return true;
                    }
                    if (args.length < 2) {
                        sendLang(sender, "commands.missing-argument", "%value%", "zone");
                        return true;
                    }
                    redefine(player, args[1]);
                }
                case "tp", "teleport" -> {
                    if (!(sender instanceof Player player)) {
                        sendLang(sender, "commands.player-only");
                        return true;
                    }
                    if (args.length < 2) {
                        sendLang(sender, "commands.missing-argument", "%value%", "zone");
                        return true;
                    }
                    teleport(player, args[1]);
                }
                default -> sendLang(sender, "commands.invalid-command");
            }
            return true;
        }

        private Zone zoneOrWarn(CommandSender sender, String name) {
            Zone zone = zones.get(name);
            if (zone == null) {
                sendLang(sender, "zone.not-found", "%name%", name);
            }
            return zone;
        }

        private void create(Player player, String name) {
            if (zones.containsKey(name)) {
                sendLang(player, "zone.already-exists", "%name%", name);
                return;
            }
            Selection selection = selections.get(player.getUniqueId());
            if (selection == null || selection.first == null || selection.second == null
                    || !selection.first.getWorld().equals(selection.second.getWorld())) {
                sendLang(player, "selection.no-selection");
                return;
            }
            File folder = new File(getModuleFolder(), "zones");
            if (!folder.isDirectory()) {
                folder.mkdirs();
            }
            File file = new File(folder, name + ".yml");
            try {
                java.nio.file.Files.copy(
                        new File(getModuleFolder(), "zones/example-zone.yml").toPath(),
                        file.toPath());
            } catch (Exception e) {
                try {
                    if (!file.createNewFile()) {
                        throw e;
                    }
                } catch (Exception ignored) {
                    plugin.getLogger().warning("AFK zone: could not create " + file + ": " + e.getMessage());
                    return;
                }
            }
            YamlConfiguration zoneConfig = YamlConfiguration.loadConfiguration(file);
            zoneConfig.set("zone.location1", serialize(selection.first));
            zoneConfig.set("zone.location2", serialize(selection.second));
            try {
                zoneConfig.save(file);
            } catch (Exception e) {
                plugin.getLogger().warning("AFK zone: could not save " + file + ": " + e.getMessage());
            }
            Zone zone = new Zone(name, file);
            zones.put(name, zone);
            selections.remove(player.getUniqueId());
            sendLang(player, "zone.created", "%name%", name);
        }

        private void delete(CommandSender sender, String name) {
            Zone zone = zoneOrWarn(sender, name);
            if (zone == null) {
                return;
            }
            if (!zone.file.delete()) {
                plugin.getLogger().warning("AFK zone: could not delete " + zone.file);
            }
            zone.disable();
            zones.remove(name);
            sendLang(sender, "zone.deleted", "%name%", name);
        }

        private void redefine(Player player, String name) {
            Zone zone = zoneOrWarn(player, name);
            if (zone == null) {
                return;
            }
            Selection selection = selections.get(player.getUniqueId());
            if (selection == null || selection.first == null || selection.second == null) {
                sendLang(player, "selection.no-selection");
                return;
            }
            zone.config.set("zone.location1", serialize(selection.first));
            zone.config.set("zone.location2", serialize(selection.second));
            try {
                zone.config.save(zone.file);
            } catch (Exception e) {
                plugin.getLogger().warning("AFK zone: could not save " + zone.file + ": " + e.getMessage());
            }
            zone.reload();
            selections.remove(player.getUniqueId());
            sendLang(player, "zone.redefined", "%name%", name);
        }

        private void teleport(Player player, String name) {
            Zone zone = zoneOrWarn(player, name);
            if (zone == null) {
                return;
            }
            Location center = zone.center();
            if (center == null) {
                sendLang(player, "teleport.fail", "%name%", name);
                return;
            }
            player.teleportAsync(center).thenAccept(done -> {
                if (Boolean.TRUE.equals(done)) {
                    sendLang(player, "teleport.success", "%name%", name);
                }
            });
        }

        private String serialize(Location location) {
            return String.format(Locale.ROOT, "%s,%f,%f,%f",
                    location.getWorld() == null ? "" : location.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ());
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("help", "wand", "create", "delete",
                        "redefine", "tp", "reload"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2 && List.of("delete", "redefine", "tp").contains(args[0].toLowerCase(Locale.ROOT))) {
                List<String> options = new ArrayList<>(zones.keySet());
                String prefix = args[1].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(prefix));
                return options;
            }
            return List.of();
        }
    }
}
