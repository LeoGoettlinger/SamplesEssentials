package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code EliteMobs(1).jar} (com.magmaguy.elitemobs).
 * <p>
 * Elite mobs and custom bosses. The original is a 2,500-class framework
 * (instanced dungeons, procedural item generation, quests, NPC guilds); this port
 * keeps the part the server actually used - bosses defined in
 * {@code custombosses/*.yml} - and the core combat loop around them:
 * <ul>
 *   <li>boss definitions: entity type, level (fixed or dynamic), name with the
 *       {@code $bossLevel} placeholder, health and damage multipliers, boss bar,
 *       silent/AI flags, trails, timeout and powers,</li>
 *   <li>powers are translated to potion effects ({@code attack_push},
 *       {@code movement_speed}, {@code attack_blinding}, {@code attack_gravity},
 *       {@code spirit_walk}, stamina/health variants),</li>
 *   <li>on-hit and on-damaged chat lines,</li>
 *   <li>loot: vanilla drops can be disabled and a configurable elite coin is
 *       dropped instead, scaled with the boss level,</li>
 *   <li>dynamic level scaling based on the killer count of nearby players.</li>
 * </ul>
 * Commands: {@code /elitemobs spawn <boss>}, {@code /elitemobs list},
 * {@code /elitemobs killall}, {@code /elitemobs reload} (alias {@code /em}).
 */
public final class EliteMobsModule extends EssentialsModule implements Listener {

    private static final class Boss {
        String id;
        String name = "&cElite Mob";
        EntityType type = EntityType.ZOMBIE;
        String level = "1";
        double healthMultiplier = 1;
        double damageMultiplier = 1;
        boolean enabled = true;
        boolean silent;
        boolean ai = true;
        String bossType = "NORMAL";
        int timeout = 0;
        List<String> powers = new ArrayList<>();
        List<String> trails = new ArrayList<>();
        List<String> onDamageMessages = new ArrayList<>();
        List<String> onDamagedMessages = new ArrayList<>();
        boolean dropsVanillaLoot = true;
    }

    private final Map<String, Boss> bosses = new LinkedHashMap<>();
    private NamespacedKey bossKey;
    private NamespacedKey levelKey;
    private NamespacedKey spawnTimeKey;

    public EliteMobsModule(SamplesEssentials plugin) {
        super("gameplay", "elitemobs", "EliteMobs(1).jar",
                "Custom bosses with levels, powers, health bars and scaled loot");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.elitemobs", "Fight elite mobs", PermissionDefault.TRUE);
        ensurePermission("samplese.elitemobs.admin", "Spawn and manage elite mobs", PermissionDefault.OP);
        bossKey = new NamespacedKey(plugin, "elite_boss");
        levelKey = new NamespacedKey(plugin, "elite_level");
        spawnTimeKey = new NamespacedKey(plugin, "elite_spawn_time");
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDirectory("custombosses");
        loadBosses();
        command("elitemobs", new EmCommand());
        command("em", new EmCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
        syncRepeat(100L, 100L, this::checkTimeouts);
    }

    private void ensureDirectory(String name) {
        File folder = new File(getModuleFolder(), name);
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }
    }

    private void loadBosses() {
        bosses.clear();
        File folder = new File(getModuleFolder(), "custombosses");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            plugin.getLogger().info("No custom bosses found.");
            return;
        }
        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            Boss boss = new Boss();
            boss.id = file.getName().replace(".yml", "");
            if (!yaml.getBoolean("isEnabled", true)) {
                continue;
            }
            boss.name = yaml.getString("name", boss.name);
            String typeName = yaml.getString("entityType", "ZOMBIE").toUpperCase(Locale.ROOT);
            try {
                boss.type = EntityType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Boss " + boss.id + " has unknown entity type " + typeName);
                continue;
            }
            if (!boss.type.isSpawnable() || !boss.type.isAlive()) {
                continue;
            }
            boss.level = yaml.getString("level", "1");
            boss.healthMultiplier = yaml.getDouble("healthMultiplier", 1);
            boss.damageMultiplier = yaml.getDouble("damageMultiplier", 1);
            boss.silent = yaml.getBoolean("silent", false);
            boss.ai = yaml.getBoolean("ai", true);
            boss.bossType = yaml.getString("bossType", "NORMAL");
            boss.timeout = yaml.getInt("timeout", 0);
            boss.dropsVanillaLoot = yaml.getBoolean("dropsVanillaLoot", true);
            boss.powers.addAll(yaml.getStringList("powers"));
            boss.trails.addAll(yaml.getStringList("trails"));
            boss.onDamageMessages.addAll(yaml.getStringList("onDamageMessages"));
            boss.onDamagedMessages.addAll(yaml.getStringList("onDamagedMessages"));
            bosses.put(boss.id.toLowerCase(Locale.ROOT), boss);
        }
        plugin.getLogger().info("Elite bosses loaded: " + bosses.size());
    }

    /* ------------------------------------------------------------------ */
    /* spawning                                                           */
    /* ------------------------------------------------------------------ */

    private int resolveLevel(Boss boss, Location location) {
        if (!boss.level.equalsIgnoreCase("dynamic")) {
            try {
                return Math.max(1, Integer.parseInt(boss.level));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        double total = 0;
        int count = 0;
        double radius = cfgDouble("dynamic-level-radius", 32);
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) > radius * radius) {
                continue;
            }
            total += Math.max(1, player.getLevel());
            count++;
        }
        int base = count == 0 ? 1 : (int) Math.round(total / count);
        return Math.max(1, base + cfgInt("dynamic-level-offset", 0));
    }

    private LivingEntity spawn(Boss boss, Location location) {
        if (location.getWorld() == null) {
            return null;
        }
        Location spawnAt = location.clone();
        LivingEntity entity;
        try {
            entity = (LivingEntity) location.getWorld().spawnEntity(spawnAt, boss.type);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not spawn boss " + boss.id + ": " + e.getMessage());
            return null;
        }
        int level = resolveLevel(boss, location);
        double health = Math.min(1024,
                Math.max(1, cfgDouble("base-health", 20) * Math.max(0.1, boss.healthMultiplier)
                        + level * cfgDouble("health-per-level", 2)));
        var maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(health);
        }
        entity.setHealth(health);
        entity.customName(Text.parse(boss.name.replace("$bossLevel", levelTag(level))
                .replace("$reinforcementLevel", levelTag(level))));
        entity.setCustomNameVisible(true);
        entity.setSilent(boss.silent);
        if (!boss.ai) {
            entity.setAI(false);
        }
        entity.setPersistent(false);
        entity.getPersistentDataContainer().set(bossKey, PersistentDataType.STRING, boss.id);
        entity.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        entity.getPersistentDataContainer().set(spawnTimeKey, PersistentDataType.LONG,
                System.currentTimeMillis());
        applyPowers(entity, boss);
        if (boss.bossType.equalsIgnoreCase("BIG") || boss.bossType.equalsIgnoreCase("MAJOR")) {
            entity.setGlowing(true);
        }
        location.getWorld().playSound(location, "entity.wither.spawn", 0.4f, 1.6f);
        return entity;
    }

    private String levelTag(int level) {
        return cfgString("level-format", "&6[Lv." + level + "]").replace("$level", String.valueOf(level));
    }

    private void applyPowers(LivingEntity entity, Boss boss) {
        for (String raw : boss.powers) {
            String power = raw.toLowerCase(Locale.ROOT).replace(".yml", "").replace(".lua", "");
            switch (power) {
                case "movement_speed" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                case "attack_push" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));
                case "attack_blinding" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false));
                case "attack_gravity" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 1, false, false));
                case "spirit_walk" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                case "invisibility" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                case "fire" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
                case "taunt", "attack_taunt" -> entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
                case "zombie_necronomicon" -> entity.setSilent(false);
                default -> {
                    // powers without a 1:1 potion equivalent are skipped
                }
            }
        }
    }

    private void checkTimeouts() {
        long now = System.currentTimeMillis();
        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!entity.getPersistentDataContainer().has(bossKey, PersistentDataType.STRING)) {
                    continue;
                }
                Boss boss = bosses.get(entity.getPersistentDataContainer()
                        .get(bossKey, PersistentDataType.STRING).toLowerCase(Locale.ROOT));
                if (boss == null || boss.timeout <= 0) {
                    continue;
                }
                Long spawned = entity.getPersistentDataContainer().get(spawnTimeKey, PersistentDataType.LONG);
                if (spawned != null && now - spawned > boss.timeout * 1000L) {
                    entity.getWorld().spawnParticle(Particle.LARGE_SMOKE, entity.getLocation(),
                            30, 0.6, 1, 0.6, 0.02);
                    entity.remove();
                }
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* combat                                                             */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(EntityDamageByEntityEvent event) {
        String id = event.getEntity().getPersistentDataContainer()
                .get(bossKey, PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        Boss boss = bosses.get(id.toLowerCase(Locale.ROOT));
        if (boss == null || !(event.getDamager() instanceof Player player)) {
            return;
        }
        if (!boss.onDamagedMessages.isEmpty()) {
            String line = boss.onDamagedMessages.get(ThreadLocalRandom.current()
                    .nextInt(boss.onDamagedMessages.size()));
            player.sendMessage(Text.parse(line.replace("$bossLevel", levelTag(levelOf(event.getEntity())))
                    .replace("$name", boss.name)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        String id = attacker.getPersistentDataContainer().get(bossKey, PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        Boss boss = bosses.get(id.toLowerCase(Locale.ROOT));
        if (boss == null) {
            return;
        }
        event.setDamage(event.getDamage() * Math.max(0.1, boss.damageMultiplier));
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!boss.onDamageMessages.isEmpty()) {
            String line = boss.onDamageMessages.get(ThreadLocalRandom.current()
                    .nextInt(boss.onDamageMessages.size()));
            player.sendMessage(Text.parse(line.replace("$bossLevel", levelTag(levelOf(attacker)))
                    .replace("$name", boss.name)));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        // bosses do not get one-shot by environmental sources they are immune to
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String id = living.getPersistentDataContainer().get(bossKey, PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        Boss boss = bosses.get(id.toLowerCase(Locale.ROOT));
        if (boss != null && !boss.ai && event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
        }
    }

    private int levelOf(org.bukkit.entity.Entity entity) {
        Integer level = entity.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
        return level == null ? 1 : level;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent event) {
        String id = event.getEntity().getPersistentDataContainer()
                .get(bossKey, PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        Boss boss = bosses.get(id.toLowerCase(Locale.ROOT));
        if (boss == null) {
            return;
        }
        int level = levelOf(event.getEntity());
        Player killer = event.getEntity().getKiller();
        if (!boss.dropsVanillaLoot) {
            event.getDrops().clear();
        }
        if (cfgBool("drop-elite-coins", true) && killer != null) {
            double amount = Math.max(1, Math.round(level * cfgDouble("coins-per-level", 1.5)));
            ItemStack coin = eliteCoin((int) amount);
            event.getDrops().add(coin);
        }
        event.getEntity().getWorld().spawnParticle(Particle.FIREWORK,
                event.getEntity().getLocation(), 40, 0.8, 1.2, 0.8, 0.1);
        if (cfgBool("announce-kills", true) && killer != null) {
            plugin.getServer().broadcast(Text.parse(msg("boss-killed",
                    "player", killer.getName(),
                    "boss", boss.name.replace("$bossLevel", levelTag(level)))));
        }
    }

    private ItemStack eliteCoin(int amount) {
        ItemStack coin = new ItemStack(Material.SUNFLOWER, Math.min(64, Math.max(1, amount)));
        ItemMeta meta = coin.getItemMeta();
        meta.displayName(Text.parse(msg("coin-name")));
        meta.lore(List.of(Text.parse(msg("coin-lore"))));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "elite_coin"),
                PersistentDataType.INTEGER, amount);
        coin.setItemMeta(meta);
        return coin;
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class EmCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "spawn" -> spawnCmd(sender, args);
                case "list" -> {
                    if (bosses.isEmpty()) {
                        reply(sender, "no-bosses");
                        return true;
                    }
                    reply(sender, "list-header", "count", String.valueOf(bosses.size()));
                    for (Boss boss : bosses.values()) {
                        sender.sendMessage(Text.parse(msg("list-entry", "boss", boss.id,
                                "type", boss.type.name(), "level", boss.level)));
                    }
                }
                case "killall" -> {
                    if (!sender.hasPermission("samplese.elitemobs.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    int count = 0;
                    for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                        for (LivingEntity entity : new ArrayList<>(world.getLivingEntities())) {
                            if (entity.getPersistentDataContainer()
                                    .has(bossKey, PersistentDataType.STRING)) {
                                entity.remove();
                                count++;
                            }
                        }
                    }
                    reply(sender, "killed-all", "count", String.valueOf(count));
                }
                case "reload" -> {
                    if (!sender.hasPermission("samplese.elitemobs.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    reloadConfig();
                    loadBosses();
                    reply(sender, "reloaded", "count", String.valueOf(bosses.size()));
                }
                default -> sendHelp(sender);
            }
            return true;
        }

        private void sendHelp(CommandSender sender) {
            for (String line : cfgStringList("help-lines")) {
                sender.sendMessage(Text.parse(line));
            }
        }

        private void spawnCmd(CommandSender sender, String[] args) {
            if (!sender.hasPermission("samplese.elitemobs.admin")) {
                reply(sender, "no-permission");
                return;
            }
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/elitemobs spawn <boss> [amount]");
                return;
            }
            Boss boss = bosses.get(args[1].toLowerCase(Locale.ROOT));
            if (boss == null) {
                reply(sender, "unknown-boss", "boss", args[1]);
                return;
            }
            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Math.min(20, Math.max(1, Integer.parseInt(args[2])));
                } catch (NumberFormatException ignored) {
                    amount = 1;
                }
            }
            Location base = player.getLocation();
            int spawned = 0;
            for (int i = 0; i < amount; i++) {
                Location target = base.clone().add(
                        ThreadLocalRandom.current().nextDouble(-2, 2), 0,
                        ThreadLocalRandom.current().nextDouble(-2, 2));
                if (spawn(boss, target) != null) {
                    spawned++;
                }
            }
            reply(sender, "spawned", "count", String.valueOf(spawned), "boss", boss.id);
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("spawn", "list", "killall", "reload");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
                return new ArrayList<>(bosses.keySet());
            }
            return List.of();
        }
    }

    /** Keeps spawn reasons untouched; bosses are only placed by command or spawner. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(CreatureSpawnEvent event) {
        if (cfgBool("debug", false)) {
            plugin.getLogger().fine("spawn " + event.getEntityType() + " (" + event.getSpawnReason() + ")");
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
