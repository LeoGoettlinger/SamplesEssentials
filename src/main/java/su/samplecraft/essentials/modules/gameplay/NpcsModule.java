package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.persistence.PersistentDataType;
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
import java.util.UUID;

/**
 * Merged from {@code FancyNpcs-2.12.1.jar} (net.fancyinnovations.fancynpcs).
 * <p>
 * NPCs with click actions. The original renders packet-based player NPCs, which
 * needs packetevents - that dependency stays standalone on this server, so this
 * port uses real, AI-less entities instead: same data model, same action system,
 * same commands, different renderer.
 * <p>
 * Kept from the original:
 * <ul>
 *   <li>NPC records with name, display name, entity type, location, glowing,
 *       collidable, turn-to-player distance, interaction cooldown, visibility
 *       distance, scale and per-trigger actions,</li>
 *   <li>triggers ANY_CLICK / RIGHT_CLICK / LEFT_CLICK,</li>
 *   <li>action types player_command, console_command, message, sound,</li>
 *   <li>the old {@code npcs.yml} format is migrated on first start, so existing
 *       NPCs keep their position and actions (PLAYER becomes a villager, because
 *       player-type fake entities are not available without packets).</li>
 * </ul>
 * Commands: {@code /npc ...} (aliases {@code fancynpcs}, {@code npcs}).
 */
public final class NpcsModule extends EssentialsModule implements Listener {

    private static final class Action {
        String trigger = "ANY_CLICK";
        String type = "player_command";
        String value = "";

        Action(String trigger, String type, String value) {
            this.trigger = trigger;
            this.type = type;
            this.value = value;
        }
    }

    private static final class Npc {
        String id;
        String name;
        String displayName;
        EntityType type = EntityType.VILLAGER;
        String world;
        double x;
        double y;
        double z;
        float yaw;
        float pitch;
        boolean glowing;
        boolean collidable = true;
        double turnToPlayerDistance = 5;
        double interactionCooldown = 0;
        double scale = 1;
        double visibilityDistance = -1;
        final List<Action> actions = new ArrayList<>();
        UUID entityId;

        Location location() {
            World bukkit = org.bukkit.Bukkit.getWorld(world);
            return bukkit == null ? null : new Location(bukkit, x, y, z, yaw, pitch);
        }
    }

    private final Map<String, Npc> npcs = new LinkedHashMap<>();
    private final Map<UUID, Long> lastInteraction = new LinkedHashMap<>();

    public NpcsModule(SamplesEssentials plugin) {
        super("gameplay", "npcs", "FancyNpcs-2.12.1.jar",
                "NPCs with click actions, glowing, interaction cooldowns and distance visibility");
    }

    private org.bukkit.NamespacedKey npcKey;

    @Override
    protected void onEnable() {
        ensurePermission("samplese.npcs", "Use NPCs", PermissionDefault.TRUE);
        ensurePermission("samplese.npcs.admin", "Create and manage NPCs", PermissionDefault.OP);
        npcKey = new org.bukkit.NamespacedKey(plugin, "npc_id");
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDefaultFile("npcs.yml");
        load();
        command("npc", new NpcCommand());
        command("fancynpcs", new NpcCommand());
        command("npcs", new NpcCommand());
        onDisableHook(() -> {
            unbindCommands();
            despawnAll();
        });
        listen(this);
        long ticks = Math.max(40L, cfgInt("npc_update_visibility_interval", 20));
        syncRepeat(ticks, ticks, this::tick);
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private File npcFile() {
        return new File(getModuleFolder(), "npcs.yml");
    }

    private void load() {
        npcs.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(npcFile());
        ConfigurationSection root = yaml.getConfigurationSection("npcs");
        if (root == null) {
            plugin.getLogger().info("No NPCs configured.");
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Npc npc = new Npc();
            npc.id = key;
            npc.name = section.getString("name", key);
            npc.displayName = section.getString("displayName", npc.name);
            npc.world = section.getString("location.world", section.getString("world"));
            npc.x = section.getDouble("location.x", section.getDouble("x"));
            npc.y = section.getDouble("location.y", section.getDouble("y"));
            npc.z = section.getDouble("location.z", section.getDouble("z"));
            npc.yaw = (float) section.getDouble("location.yaw", section.getDouble("yaw"));
            npc.pitch = (float) section.getDouble("location.pitch", section.getDouble("pitch"));
            npc.glowing = section.getBoolean("glowing", false);
            npc.collidable = section.getBoolean("collidable", true);
            npc.turnToPlayerDistance = section.getDouble("turnToPlayerDistance", 5);
            npc.interactionCooldown = section.getDouble("interactionCooldown", 0);
            npc.scale = section.getDouble("scale", 1);
            npc.visibilityDistance = section.getDouble("visibility_distance", -1);
            String typeName = section.getString("type", "VILLAGER");
            if (typeName.equalsIgnoreCase("PLAYER")) {
                // packet-free renderer: fall back to a humanoid-shaped mob
                npc.type = EntityType.VILLAGER;
            } else {
                EntityType parsed = null;
                try {
                    parsed = EntityType.valueOf(typeName.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    // unknown type, keep default
                }
                npc.type = parsed == null || !parsed.isSpawnable() || !parsed.isAlive()
                        ? EntityType.VILLAGER : parsed;
            }
            ConfigurationSection actions = section.getConfigurationSection("actions");
            if (actions != null) {
                for (String trigger : actions.getKeys(false)) {
                    ConfigurationSection triggerSection = actions.getConfigurationSection(trigger);
                    if (triggerSection == null) {
                        continue;
                    }
                    for (String actionKey : triggerSection.getKeys(false)) {
                        ConfigurationSection action = triggerSection.getConfigurationSection(actionKey);
                        if (action == null) {
                            continue;
                        }
                        npc.actions.add(new Action(trigger.toUpperCase(Locale.ROOT),
                                action.getString("action", "player_command"),
                                action.getString("value", "")));
                    }
                }
            }
            if (npc.world != null) {
                npcs.put(npc.name.toLowerCase(Locale.ROOT), npc);
            }
        }
        plugin.getLogger().info("NPCs loaded: " + npcs.size());
        for (Npc npc : npcs.values()) {
            spawn(npc);
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Npc npc : npcs.values()) {
            String base = "npcs." + npc.id + ".";
            yaml.set(base + "name", npc.name);
            yaml.set(base + "displayName", npc.displayName);
            yaml.set(base + "type", npc.type.name());
            yaml.set(base + "location.world", npc.world);
            yaml.set(base + "location.x", npc.x);
            yaml.set(base + "location.y", npc.y);
            yaml.set(base + "location.z", npc.z);
            yaml.set(base + "location.yaw", npc.yaw);
            yaml.set(base + "location.pitch", npc.pitch);
            yaml.set(base + "glowing", npc.glowing);
            yaml.set(base + "collidable", npc.collidable);
            yaml.set(base + "turnToPlayerDistance", npc.turnToPlayerDistance);
            yaml.set(base + "interactionCooldown", npc.interactionCooldown);
            yaml.set(base + "scale", npc.scale);
            yaml.set(base + "visibility_distance", npc.visibilityDistance);
            int index = 0;
            for (Action action : npc.actions) {
                String actionBase = base + "actions." + action.trigger + "." + (++index) + ".";
                yaml.set(actionBase + "action", action.type);
                yaml.set(actionBase + "value", action.value);
            }
        }
        try {
            yaml.save(npcFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save NPCs: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* entity handling                                                    */
    /* ------------------------------------------------------------------ */

    private void spawn(Npc npc) {
        Location location = npc.location();
        if (location == null) {
            return;
        }
        removeEntity(npc);
        Entity entity;
        try {
            entity = location.getWorld().spawnEntity(location, npc.type);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not spawn NPC " + npc.name + ": " + e.getMessage());
            return;
        }
        entity.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id);
        entity.setPersistent(false);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setCustomNameVisible(true);
        entity.customName(Text.parse(npc.displayName));
        if (entity instanceof LivingEntity living) {
            living.setAI(false);
            living.setCanPickupItems(false);
            living.setRemoveWhenFarAway(false);
            living.setCollidable(npc.collidable);
            living.setGlowing(npc.glowing);
            if (npc.scale > 0) {
                living.getAttribute(org.bukkit.attribute.Attribute.SCALE)
                        .setBaseValue(Math.min(4, Math.max(0.2, npc.scale)));
            }
            EntityEquipment equipment = living.getEquipment();
            if (equipment != null) {
                equipment.setItemInMainHand(new ItemStack(Material.AIR));
            }
        }
        npc.entityId = entity.getUniqueId();
    }

    private void removeEntity(Npc npc) {
        if (npc.entityId == null) {
            return;
        }
        Entity entity = plugin.getServer().getEntity(npc.entityId);
        if (entity != null) {
            entity.remove();
        }
        npc.entityId = null;
    }

    private void despawnAll() {
        for (Npc npc : npcs.values()) {
            removeEntity(npc);
        }
    }

    private void tick() {
        for (Npc npc : npcs.values()) {
            if (npc.entityId == null) {
                spawn(npc);
                continue;
            }
            Entity entity = plugin.getServer().getEntity(npc.entityId);
            if (entity == null) {
                spawn(npc);
                continue;
            }
            // distance based visibility: hide far away players' NPCs
            double maxDistance = npc.visibilityDistance > 0
                    ? npc.visibilityDistance
                    : cfgDouble("visibility_distance", 20);
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.getWorld().equals(entity.getWorld())) {
                    continue;
                }
                double distance = player.getLocation().distanceSquared(entity.getLocation());
                if (maxDistance > 0 && distance > maxDistance * maxDistance) {
                    player.hideEntity(plugin, entity);
                } else {
                    player.showEntity(plugin, entity);
                }
            }
            // turn to the closest player
            double turnDistance = npc.turnToPlayerDistance;
            if (turnDistance > 0) {
                Player closest = null;
                double best = turnDistance * turnDistance;
                for (Player player : entity.getWorld().getPlayers()) {
                    double distance = player.getLocation().distanceSquared(entity.getLocation());
                    if (distance <= best) {
                        best = distance;
                        closest = player;
                    }
                }
                if (closest != null) {
                    Location target = entity.getLocation();
                    var direction = closest.getLocation().toVector()
                            .subtract(target.toVector());
                    target.setDirection(direction);
                    entity.teleport(target);
                }
            }
        }
    }

    private Npc npcOf(Entity entity) {
        if (entity == null) {
            return null;
        }
        String id = entity.getPersistentDataContainer().get(npcKey, PersistentDataType.STRING);
        if (id == null) {
            return null;
        }
        for (Npc npc : npcs.values()) {
            if (npc.id.equals(id)) {
                return npc;
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEntityEvent event) {
        Npc npc = npcOf(event.getRightClicked());
        if (npc == null) {
            return;
        }
        event.setCancelled(true);
        trigger(npc, event.getPlayer(), "RIGHT_CLICK");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent event) {
        Npc npc = npcOf(event.getEntity());
        if (npc == null) {
            return;
        }
        event.setCancelled(true);
        if (event.getDamager() instanceof Player player) {
            trigger(npc, player, "LEFT_CLICK");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastInteraction.remove(event.getPlayer().getUniqueId());
    }

    private void trigger(Npc npc, Player player, String clicked) {
        if (npc.interactionCooldown > 0) {
            long now = System.currentTimeMillis();
            long last = lastInteraction.getOrDefault(player.getUniqueId(), 0L);
            long cooldownMillis = (long) (npc.interactionCooldown * 1000);
            if (now - last < cooldownMillis) {
                if (!cfgBool("disable_interaction_cooldown_message", false)) {
                    reply(player, "cooldown", "time",
                            String.valueOf((cooldownMillis - (now - last)) / 1000 + 1));
                }
                return;
            }
            lastInteraction.put(player.getUniqueId(), now);
        }
        boolean any = false;
        for (Action action : npc.actions) {
            if (action.trigger.equalsIgnoreCase(clicked) || action.trigger.equals("ANY_CLICK")) {
                execute(action, npc, player);
                any = true;
            }
        }
        if (!any && cfgBool("debug", false)) {
            plugin.getLogger().info("NPC " + npc.name + " has no action for " + clicked);
        }
    }

    private void execute(Action action, Npc npc, Player player) {
        String value = action.value
                .replace("%player%", player.getName())
                .replace("%npc%", npc.name);
        switch (action.type.toLowerCase(Locale.ROOT)) {
            case "player_command", "playercommand" -> player.performCommand(value);
            case "console_command", "consolecommand" -> plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), value);
            case "message" -> player.sendMessage(Text.parse(value));
            case "sound" -> {
                try {
                    player.playSound(player.getLocation(), value.toLowerCase(Locale.ROOT), 1f, 1f);
                } catch (Exception e) {
                    plugin.getLogger().warning("Unknown sound for NPC " + npc.name + ": " + value);
                }
            }
            default -> plugin.getLogger().warning("Unsupported NPC action type: " + action.type);
        }
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class NpcCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("list")) {
                reply(sender, "list-header", "count", String.valueOf(npcs.size()));
                for (Npc npc : npcs.values()) {
                    sender.sendMessage(Text.parse(msg("list-entry", "npc", npc.name,
                            "type", npc.type.name(), "world", npc.world)));
                }
                return true;
            }
            if (!sender.hasPermission("samplese.npcs.admin")) {
                reply(sender, "no-permission");
                return true;
            }
            switch (sub) {
                case "create" -> create(sender, args);
                case "remove", "delete" -> remove(sender, args);
                case "teleport", "tp" -> teleport(sender, args);
                case "here", "teleporthere" -> here(sender, args);
                case "displayname", "rename" -> displayName(sender, args);
                case "type" -> type(sender, args);
                case "glowing" -> toggle(sender, args, "glowing");
                case "collidable" -> toggle(sender, args, "collidable");
                case "cooldown", "interactioncooldown" -> cooldown(sender, args);
                case "turntoplayer", "turntoplayerdistance" -> turnToPlayer(sender, args);
                case "visibilitydistance" -> visibilityDistance(sender, args);
                case "scale" -> scale(sender, args);
                case "action" -> action(sender, args);
                case "save" -> {
                    save();
                    reply(sender, "saved");
                }
                case "reload" -> {
                    reloadConfig();
                    despawnAll();
                    load();
                    reply(sender, "reloaded", "count", String.valueOf(npcs.size()));
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

        private Npc find(CommandSender sender, String[] args) {
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/npc " + args[0] + " <name>");
                return null;
            }
            Npc npc = npcs.get(args[1].toLowerCase(Locale.ROOT));
            if (npc == null) {
                reply(sender, "not-found", "npc", args[1]);
            }
            return npc;
        }

        private void create(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/npc create <name> [type]");
                return;
            }
            if (npcs.containsKey(args[1].toLowerCase(Locale.ROOT))) {
                reply(sender, "already-exists", "npc", args[1]);
                return;
            }
            EntityType type = EntityType.VILLAGER;
            if (args.length >= 3) {
                try {
                    type = EntityType.valueOf(args[2].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    reply(sender, "unknown-type", "type", args[2]);
                    return;
                }
            }
            if (!type.isSpawnable() || !type.isAlive()) {
                reply(sender, "unknown-type", "type", type.name());
                return;
            }
            Npc npc = new Npc();
            npc.id = UUID.randomUUID().toString();
            npc.name = args[1];
            npc.displayName = args[1];
            npc.type = type;
            Location location = player.getLocation();
            npc.world = location.getWorld().getName();
            npc.x = location.getX();
            npc.y = location.getY();
            npc.z = location.getZ();
            npc.yaw = location.getYaw();
            npc.pitch = location.getPitch();
            npc.glowing = cfgBool("glowing-by-default", false);
            npcs.put(npc.name.toLowerCase(Locale.ROOT), npc);
            spawn(npc);
            save();
            reply(sender, "created", "npc", npc.name, "type", npc.type.name());
        }

        private void remove(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            removeEntity(npc);
            npcs.remove(npc.name.toLowerCase(Locale.ROOT));
            save();
            reply(sender, "removed", "npc", npc.name);
        }

        private void teleport(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            player.teleportAsync(npc.location());
            reply(sender, "teleported", "npc", npc.name);
        }

        private void here(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (!(sender instanceof Player player)) {
                reply(sender, "player-only");
                return;
            }
            Location location = player.getLocation();
            npc.world = location.getWorld().getName();
            npc.x = location.getX();
            npc.y = location.getY();
            npc.z = location.getZ();
            npc.yaw = location.getYaw();
            npc.pitch = location.getPitch();
            spawn(npc);
            save();
            reply(sender, "moved", "npc", npc.name);
        }

        private void displayName(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc displayName <name> <text>");
                return;
            }
            npc.displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            Entity entity = npc.entityId == null ? null : plugin.getServer().getEntity(npc.entityId);
            if (entity != null) {
                entity.customName(Text.parse(npc.displayName));
                entity.setCustomNameVisible(true);
            }
            save();
            reply(sender, "displayname-set", "npc", npc.name);
        }

        private void type(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc type <name> <entitytype>");
                return;
            }
            EntityType type;
            try {
                type = EntityType.valueOf(args[2].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                reply(sender, "unknown-type", "type", args[2]);
                return;
            }
            if (!type.isSpawnable() || !type.isAlive()) {
                reply(sender, "unknown-type", "type", type.name());
                return;
            }
            npc.type = type;
            spawn(npc);
            save();
            reply(sender, "type-set", "npc", npc.name, "type", type.name());
        }

        private void toggle(CommandSender sender, String[] args, String flag) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            boolean state;
            if (args.length >= 3) {
                state = Boolean.parseBoolean(args[2]);
            } else {
                state = !(flag.equals("glowing") ? npc.glowing : npc.collidable);
            }
            if (flag.equals("glowing")) {
                npc.glowing = state;
            } else {
                npc.collidable = state;
            }
            Entity entity = npc.entityId == null ? null : plugin.getServer().getEntity(npc.entityId);
            if (entity instanceof LivingEntity living) {
                living.setGlowing(npc.glowing);
                living.setCollidable(npc.collidable);
            }
            save();
            reply(sender, "flag-set", "npc", npc.name, "flag", flag,
                    "value", String.valueOf(state));
        }

        private void cooldown(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc cooldown <name> <seconds>");
                return;
            }
            try {
                npc.interactionCooldown = Math.max(0, Double.parseDouble(args[2]));
            } catch (NumberFormatException e) {
                reply(sender, "usage", "usage", "/npc cooldown <name> <seconds>");
                return;
            }
            save();
            reply(sender, "cooldown-set", "npc", npc.name, "value", args[2]);
        }

        private void turnToPlayer(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc turnToPlayer <name> <distance>");
                return;
            }
            try {
                npc.turnToPlayerDistance = Math.max(0, Double.parseDouble(args[2]));
            } catch (NumberFormatException e) {
                reply(sender, "usage", "usage", "/npc turnToPlayer <name> <distance>");
                return;
            }
            save();
            reply(sender, "turn-set", "npc", npc.name, "value", args[2]);
        }

        private void visibilityDistance(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc visibilityDistance <name> <blocks>");
                return;
            }
            try {
                npc.visibilityDistance = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                reply(sender, "usage", "usage", "/npc visibilityDistance <name> <blocks>");
                return;
            }
            save();
            reply(sender, "visibility-set", "npc", npc.name, "value", args[2]);
        }

        private void scale(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage", "/npc scale <name> <scale>");
                return;
            }
            try {
                npc.scale = Math.min(4, Math.max(0.2, Double.parseDouble(args[2])));
            } catch (NumberFormatException e) {
                reply(sender, "usage", "usage", "/npc scale <name> <scale>");
                return;
            }
            spawn(npc);
            save();
            reply(sender, "scale-set", "npc", npc.name, "value", String.valueOf(npc.scale));
        }

        private void action(CommandSender sender, String[] args) {
            Npc npc = find(sender, args);
            if (npc == null) {
                return;
            }
            if (args.length < 3) {
                reply(sender, "usage", "usage",
                        "/npc action <name> <add|remove|list> [trigger] [type] [value]");
                return;
            }
            String mode = args[2].toLowerCase(Locale.ROOT);
            if (mode.equals("list")) {
                if (npc.actions.isEmpty()) {
                    reply(sender, "action-empty", "npc", npc.name);
                    return;
                }
                int index = 0;
                for (Action action : npc.actions) {
                    sender.sendMessage(Text.parse(msg("action-entry",
                            "index", String.valueOf(++index), "trigger", action.trigger,
                            "type", action.type, "value", action.value)));
                }
                return;
            }
            if (mode.equals("remove")) {
                if (args.length < 4) {
                    reply(sender, "usage", "usage", "/npc action <name> remove <index>");
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[3]) - 1;
                } catch (NumberFormatException e) {
                    reply(sender, "usage", "usage", "/npc action <name> remove <index>");
                    return;
                }
                if (index < 0 || index >= npc.actions.size()) {
                    reply(sender, "action-invalid-index");
                    return;
                }
                npc.actions.remove(index);
                save();
                reply(sender, "action-removed", "npc", npc.name);
                return;
            }
            if (!mode.equals("add")) {
                reply(sender, "usage", "usage",
                        "/npc action <name> <add|remove|list> [trigger] [type] [value]");
                return;
            }
            String trigger = args.length >= 4 ? args[3].toUpperCase(Locale.ROOT) : "ANY_CLICK";
            if (!List.of("ANY_CLICK", "RIGHT_CLICK", "LEFT_CLICK").contains(trigger)) {
                reply(sender, "unknown-trigger", "trigger", args[3]);
                return;
            }
            String type = args.length >= 5 ? args[4].toLowerCase(Locale.ROOT) : "player_command";
            if (!List.of("player_command", "console_command", "message", "sound").contains(type)) {
                reply(sender, "unknown-action", "type", args[4]);
                return;
            }
            if (args.length < 6) {
                reply(sender, "usage", "usage",
                        "/npc action <name> add <trigger> <type> <value>");
                return;
            }
            String value = String.join(" ", java.util.Arrays.copyOfRange(args, 5, args.length));
            npc.actions.add(new Action(trigger, type, value));
            save();
            reply(sender, "action-added", "npc", npc.name);
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("create", "remove", "list", "tp",
                        "here", "displayName", "type", "glowing", "collidable", "cooldown",
                        "turnToPlayer", "visibilityDistance", "scale", "action", "save", "reload"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(prefix));
                return options;
            }
            if (args.length == 2) {
                List<String> names = new ArrayList<>();
                npcs.values().forEach(npc -> names.add(npc.name));
                return names;
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("action")) {
                return List.of("add", "remove", "list");
            }
            if (args.length == 4 && args[0].equalsIgnoreCase("action")) {
                return List.of("ANY_CLICK", "RIGHT_CLICK", "LEFT_CLICK");
            }
            if (args.length == 5 && args[0].equalsIgnoreCase("action")) {
                return List.of("player_command", "console_command", "message", "sound");
            }
            if (args.length == 3 && (args[0].equalsIgnoreCase("glowing")
                    || args[0].equalsIgnoreCase("collidable"))) {
                return List.of("true", "false");
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
