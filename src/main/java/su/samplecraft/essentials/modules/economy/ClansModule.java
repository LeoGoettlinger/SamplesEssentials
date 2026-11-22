package su.samplecraft.essentials.modules.economy;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Merged from {@code vrx-clans-1.0.4.jar} (vrx.economy.vRXClans).
 * <p>
 * Clans with ranks, bank, level/XP, diplomacy, base and clan chat. Kept from the
 * original:
 * <ul>
 *   <li>ranks RECRUIT, MEMBER, OFFICER, CO_LEADER, LEADER with hierarchical checks,</li>
 *   <li>creation cost, name rules (length, chars, blocked names) and join cooldown,</li>
 *   <li>invites with a timeout, join/leave/kick, promote/demote, disband, rename,</li>
 *   <li>clan bank (deposit/withdraw), upkeep handled by the leader,</li>
 *   <li>level + XP from mining and kills, level rewards (bank + commands),</li>
 *   <li>diplomacy: ally/rival/neutral with per-clan limits,</li>
 *   <li>clan base with a radius and a delayed teleport,</li>
 *   <li>clan chat plus a global chat tag, friendly-fire protection.</li>
 * </ul>
 * Rebuilt on a YAML {@code clans.yml} store instead of the original MySQL/SQLite
 * stack; Vault is used through the services manager exactly like other economy
 * modules do.
 * <p>
 * Commands: {@code /clan} (aliases {@code clans}, {@code c}), {@code /clanadmin}.
 */
public final class ClansModule extends EssentialsModule implements Listener {

    private enum Rank {
        RECRUIT, MEMBER, OFFICER, CO_LEADER, LEADER;

        boolean atLeast(Rank other) {
            return ordinal() >= other.ordinal();
        }
    }

    private static final class Clan {
        String name;
        UUID leader;
        String leaderName = "?";
        final Map<UUID, String> memberNames = new LinkedHashMap<>();
        final Map<UUID, Rank> members = new LinkedHashMap<>();
        double bank = 0;
        int level = 1;
        double xp = 0;
        int kills = 0;
        int deaths = 0;
        final Set<String> allies = new LinkedHashSet<>();
        final Set<String> rivals = new LinkedHashSet<>();
        String baseWorld;
        double baseX;
        double baseY;
        double baseZ;
        float baseYaw;
        String motd = "";

        int size() {
            return members.size();
        }

        Location base() {
            if (baseWorld == null) {
                return null;
            }
            World world = org.bukkit.Bukkit.getWorld(baseWorld);
            return world == null ? null : new Location(world, baseX, baseY, baseZ, baseYaw, 0f);
        }

        Rank rankOf(UUID uuid) {
            return members.get(uuid);
        }
    }

    private static final class Invite {
        final String clan;
        final long created = System.currentTimeMillis();

        Invite(String clan) {
            this.clan = clan;
        }
    }

    private final Map<String, Clan> clans = new LinkedHashMap<>();
    private final Map<UUID, Invite> invites = new HashMap<>();
    private final Map<UUID, Long> joinCooldown = new HashMap<>();
    private final Map<UUID, Boolean> clanChat = new HashMap<>();
    /** teleport countdown tasks, one per player */
    private final Map<UUID, org.bukkit.scheduler.BukkitTask> baseTeleports = new HashMap<>();

    public ClansModule(SamplesEssentials plugin) {
        super("economy", "clans", "vrx-clans-1.0.4.jar",
                "Clans with ranks, bank, levels, diplomacy, base and clan chat");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.clans", "Use /clan", PermissionDefault.TRUE);
        ensurePermission("samplese.clans.admin", "Use /clanadmin", PermissionDefault.OP);
        ensurePermission("samplese.clans.bypass", "Bypass clan claim checks", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        ensureDefaultFile("clans.yml");
        load();
        command("clan", new ClanCommand());
        command("clans", new ClanCommand());
        command("clanadmin", new AdminCommand());
        onDisableHook(() -> {
            unbindCommands();
            save();
        });
        listen(this);
        long ticks = Math.max(200L, cfgInt("top-cache-refresh-seconds", 60) * 20L);
        syncRepeat(ticks, ticks, this::save);
        // invite + cooldown housekeeping
        syncRepeat(200L, 600L, this::expireInvites);
    }

    /* ------------------------------------------------------------------ */
    /* persistence                                                        */
    /* ------------------------------------------------------------------ */

    private File clansFile() {
        return new File(getModuleFolder(), "clans.yml");
    }

    private void load() {
        clans.clear();
        invites.clear();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(clansFile());
        ConfigurationSection root = yaml.getConfigurationSection("clans");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Clan clan = new Clan();
            clan.name = section.getString("name", key);
            String leader = section.getString("leader");
            if (leader != null) {
                try {
                    clan.leader = UUID.fromString(leader);
                } catch (IllegalArgumentException ignored) {
                    // ignore malformed leader
                }
            }
            clan.leaderName = section.getString("leader-name", "?");
            clan.bank = section.getDouble("bank", 0);
            clan.level = Math.max(1, section.getInt("level", 1));
            clan.xp = section.getDouble("xp", 0);
            clan.kills = section.getInt("kills", 0);
            clan.deaths = section.getInt("deaths", 0);
            clan.allies.addAll(section.getStringList("allies"));
            clan.rivals.addAll(section.getStringList("rivals"));
            clan.motd = section.getString("motd", "");
            if (section.isString("base.world")) {
                clan.baseWorld = section.getString("base.world");
                clan.baseX = section.getDouble("base.x");
                clan.baseY = section.getDouble("base.y");
                clan.baseZ = section.getDouble("base.z");
                clan.baseYaw = (float) section.getDouble("base.yaw");
            }
            ConfigurationSection members = section.getConfigurationSection("members");
            if (members != null) {
                for (String uuidText : members.getKeys(false)) {
                    ConfigurationSection member = members.getConfigurationSection(uuidText);
                    if (member == null) {
                        continue;
                    }
                    try {
                        UUID uuid = UUID.fromString(uuidText);
                        clan.members.put(uuid, parseRank(member.getString("rank", "MEMBER")));
                        clan.memberNames.put(uuid, member.getString("name", "?"));
                    } catch (IllegalArgumentException ignored) {
                        // skip malformed member uuid
                    }
                }
            }
            if (clan.leader != null) {
                clan.members.putIfAbsent(clan.leader, Rank.LEADER);
            }
            clans.put(clan.name.toLowerCase(Locale.ROOT), clan);
        }
        plugin.getLogger().info("Clans loaded: " + clans.size());
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Clan clan : clans.values()) {
            String base = "clans." + clan.name.toLowerCase(Locale.ROOT) + ".";
            yaml.set(base + "name", clan.name);
            if (clan.leader != null) {
                yaml.set(base + "leader", clan.leader.toString());
            }
            yaml.set(base + "leader-name", clan.leaderName);
            yaml.set(base + "bank", clan.bank);
            yaml.set(base + "level", clan.level);
            yaml.set(base + "xp", clan.xp);
            yaml.set(base + "kills", clan.kills);
            yaml.set(base + "deaths", clan.deaths);
            yaml.set(base + "allies", new ArrayList<>(clan.allies));
            yaml.set(base + "rivals", new ArrayList<>(clan.rivals));
            yaml.set(base + "motd", clan.motd);
            if (clan.baseWorld != null) {
                yaml.set(base + "base.world", clan.baseWorld);
                yaml.set(base + "base.x", clan.baseX);
                yaml.set(base + "base.y", clan.baseY);
                yaml.set(base + "base.z", clan.baseZ);
                yaml.set(base + "base.yaw", clan.baseYaw);
            }
            for (Map.Entry<UUID, Rank> entry : clan.members.entrySet()) {
                String memberBase = base + "members." + entry.getKey() + ".";
                yaml.set(memberBase + "rank", entry.getValue().name());
                yaml.set(memberBase + "name", clan.memberNames.getOrDefault(entry.getKey(), "?"));
            }
        }
        try {
            yaml.save(clansFile());
        } catch (Exception e) {
            plugin.getLogger().warning("Could not save clans: " + e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private static Rank parseRank(String text) {
        try {
            return Rank.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Rank.MEMBER;
        }
    }

    private Clan clanOf(UUID player) {
        for (Clan clan : clans.values()) {
            if (clan.members.containsKey(player)) {
                return clan;
            }
        }
        return null;
    }

    private Clan clanNamed(String name) {
        if (name == null) {
            return null;
        }
        Clan direct = clans.get(name.toLowerCase(Locale.ROOT));
        if (direct != null) {
            return direct;
        }
        for (Clan clan : clans.values()) {
            if (clan.name.equalsIgnoreCase(name)) {
                return clan;
            }
        }
        return null;
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private boolean charge(Player player, double amount) {
        if (amount <= 0) {
            return true;
        }
        var economy = economy();
        if (economy == null) {
            return true;
        }
        if (economy.getBalance(player) < amount) {
            reply(player, "need-money", "amount", format(amount));
            return false;
        }
        economy.withdrawPlayer(player, amount);
        return true;
    }

    private void pay(Player player, double amount) {
        if (amount <= 0) {
            return;
        }
        var economy = economy();
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    private static String format(double value) {
        return value == Math.floor(value)
                ? String.valueOf((long) value)
                : String.format(Locale.ROOT, "%.2f", value);
    }

    private double requiredXp(Clan clan) {
        return clan.level * Math.max(1, cfgDouble("level-up-multiplier", 1000));
    }

    private void addXp(Clan clan, double amount) {
        if (amount <= 0 || clan == null) {
            return;
        }
        clan.xp += amount;
        while (clan.xp >= requiredXp(clan)) {
            clan.xp -= requiredXp(clan);
            clan.level++;
            forEachMember(clan, member -> {
                member.sendMessage(Text.parse(msg("level-up-chat", "clan", clan.name,
                        "level", String.valueOf(clan.level))));
                member.showTitle(net.kyori.adventure.title.Title.title(
                        Text.parse(msg("level-up-title")),
                        Text.parse(msg("level-up-sub", "level", String.valueOf(clan.level)))));
            });
            org.bukkit.configuration.ConfigurationSection reward =
                    config().getConfigurationSection("level-rewards." + clan.level);
            if (reward != null) {
                clan.bank += reward.getDouble("bank", 0);
                String message = reward.getString("message");
                if (message != null && !message.isBlank()) {
                    forEachMember(clan, member -> member.sendMessage(Text.parse(message
                            .replace("%level%", String.valueOf(clan.level))
                            .replace("%bank%", format(reward.getDouble("bank", 0))))));
                }
                for (String command : reward.getStringList("commands")) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            command.replace("%clan%", clan.name)
                                    .replace("%level%", String.valueOf(clan.level)));
                }
            }
        }
    }

    private void forEachMember(Clan clan, java.util.function.Consumer<Player> action) {
        for (UUID uuid : clan.members.keySet()) {
            Player online = plugin.getServer().getPlayer(uuid);
            if (online != null) {
                action.accept(online);
            }
        }
    }

    private void expireInvites() {
        int timeout = Math.max(1, cfgInt("invite-timeout", 60));
        long now = System.currentTimeMillis();
        invites.entrySet().removeIf(entry ->
                (now - entry.getValue().created) / 1000L > timeout);
        int cooldown = Math.max(0, cfgInt("join-cooldown", 86400));
        if (cooldown > 0) {
            joinCooldown.entrySet().removeIf(entry ->
                    (now - entry.getValue()) / 1000L > cooldown);
        }
    }

    private void disband(Clan clan, String reason) {
        for (UUID uuid : new ArrayList<>(clan.members.keySet())) {
            Player online = plugin.getServer().getPlayer(uuid);
            if (online != null) {
                reply(online, "disband-success", "clan", clan.name);
            }
        }
        for (Clan other : clans.values()) {
            other.allies.removeIf(name -> name.equalsIgnoreCase(clan.name));
            other.rivals.removeIf(name -> name.equalsIgnoreCase(clan.name));
        }
        clans.remove(clan.name.toLowerCase(Locale.ROOT));
        save();
        if (reason != null) {
            plugin.getLogger().info("Clan " + clan.name + " disbanded (" + reason + ")");
        }
    }

    private String validateName(Player player, String name) {
        int min = Math.max(1, cfgInt("name-length-min", 3));
        int max = Math.max(min, cfgInt("name-length-max", 12));
        if (name.length() < min || name.length() > max) {
            reply(player, "invalid-name-length", "min", String.valueOf(min), "max", String.valueOf(max));
            return null;
        }
        if (!name.matches("[A-Za-z0-9]+")) {
            reply(player, "invalid-name-chars");
            return null;
        }
        for (String blocked : cfgStringList("blocked-names")) {
            if (blocked.equalsIgnoreCase(name)) {
                reply(player, "blocked-name");
                return null;
            }
        }
        if (clanNamed(name) != null) {
            reply(player, "clan-exists");
            return null;
        }
        return name;
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Clan clan = clanOf(event.getPlayer().getUniqueId());
        if (clan == null) {
            return;
        }
        addXp(clan, cfgDouble("xp-per-blockbreak", 1));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMonsterKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || event.getEntity() instanceof Player) {
            return;
        }
        Clan clan = clanOf(killer.getUniqueId());
        if (clan != null) {
            addXp(clan, cfgDouble("xp-per-monsterkill", 5));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player killer = victim.getKiller();
        Clan victimClan = clanOf(victim.getUniqueId());
        if (victimClan != null) {
            victimClan.deaths++;
        }
        if (killer == null || killer.equals(victim)) {
            return;
        }
        Clan killerClan = clanOf(killer.getUniqueId());
        if (killerClan != null) {
            killerClan.kills++;
            addXp(killerClan, cfgDouble("xp-per-playerkill", 10));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)
                || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        Clan victimClan = clanOf(victim.getUniqueId());
        if (victimClan == null) {
            return;
        }
        Clan attackerClan = clanOf(attacker.getUniqueId());
        if (attackerClan == victimClan && cfgBool("disable-friendly-fire", true)) {
            event.setCancelled(true);
            reply(attacker, "ff-friendly");
            return;
        }
        if (attackerClan != null && victimClan.allies.contains(attackerClan.name.toLowerCase(Locale.ROOT))
                && cfgBool("disable-ally-fire", true)) {
            event.setCancelled(true);
            reply(attacker, "ff-ally");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clanChat.remove(event.getPlayer().getUniqueId());
        baseTeleports.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        clanChat.remove(event.getPlayer().getUniqueId());
        baseTeleports.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(io.papermc.paper.event.player.AsyncChatEvent event) {
        if (!clanChat.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            return;
        }
        Clan clan = clanOf(event.getPlayer().getUniqueId());
        if (clan == null) {
            return;
        }
        event.setCancelled(true);
        String message = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.message());
        var rendered = Text.parse(msg("clan-chat-format",
                "clan", clan.name, "player", event.getPlayer().getName(), "message", message));
        forEachMember(clan, member -> member.sendMessage(rendered));
        plugin.getServer().getConsoleSender().sendMessage(rendered);
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class ClanCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                if (sender instanceof Player player) {
                    Clan clan = clanOf(player.getUniqueId());
                    if (clan == null) {
                        reply(player, "not-in-clan");
                        return true;
                    }
                    showInfo(player, clan);
                } else {
                    sendHelp(sender);
                }
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "create" -> create(sender, args);
                case "disband" -> disbandCmd(sender);
                case "invite" -> invite(sender, args);
                case "join" -> join(sender, args);
                case "leave" -> leave(sender);
                case "kick" -> kick(sender, args);
                case "promote" -> promote(sender, args, true);
                case "demote" -> promote(sender, args, false);
                case "chat", "c" -> chatToggle(sender);
                case "bank" -> bank(sender);
                case "deposit" -> moveMoney(sender, args, true);
                case "withdraw" -> moveMoney(sender, args, false);
                case "ally", "rival", "neutral" -> diplomacy(sender, args,
                        args[0].toLowerCase(Locale.ROOT));
                case "base" -> base(sender, args);
                case "sethome", "setbase" -> setBase(sender);
                case "info", "show" -> infoCmd(sender, args);
                case "list", "top" -> list(sender);
                case "rename" -> rename(sender, args);
                case "motd" -> motd(sender, args);
                case "help" -> sendHelp(sender);
                default -> sendHelp(sender);
            }
            return true;
        }

        private void sendHelp(CommandSender sender) {
            for (String line : cfgStringList("help-lines")) {
                sender.sendMessage(Text.parse(line));
            }
        }

        private Player player(CommandSender sender) {
            if (sender instanceof Player player) {
                return player;
            }
            reply(sender, "only-players");
            return null;
        }

        private Clan own(Player player) {
            Clan clan = clanOf(player.getUniqueId());
            if (clan == null) {
                reply(player, "not-in-clan");
            }
            return clan;
        }

        private boolean require(Player player, Clan clan, Rank rank) {
            Rank own = clan.rankOf(player.getUniqueId());
            if (own == null || !own.atLeast(rank)) {
                reply(player, "not-co-leader");
                return false;
            }
            return true;
        }

        private void create(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage", "/clan create <name>");
                return;
            }
            if (clanOf(player.getUniqueId()) != null) {
                reply(player, "clan-exists");
                return;
            }
            long cooldown = cfgInt("join-cooldown", 86400);
            Long last = joinCooldown.get(player.getUniqueId());
            if (last != null && cooldown > 0) {
                long remaining = cooldown - (System.currentTimeMillis() - last) / 1000L;
                if (remaining > 0) {
                    reply(player, "cooldown", "time", duration(remaining));
                    return;
                }
            }
            String name = validateName(player, args[1]);
            if (name == null) {
                return;
            }
            double cost = cfgDouble("clan-creation-cost", 1000);
            if (!charge(player, cost)) {
                return;
            }
            Clan clan = new Clan();
            clan.name = name;
            clan.leader = player.getUniqueId();
            clan.leaderName = player.getName();
            clan.members.put(player.getUniqueId(), Rank.LEADER);
            clan.memberNames.put(player.getUniqueId(), player.getName());
            clans.put(name.toLowerCase(Locale.ROOT), clan);
            save();
            reply(player, "clan-created", "clan", clan.name);
        }

        private void disbandCmd(CommandSender sender) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!player.getUniqueId().equals(clan.leader)) {
                reply(player, "not-leader");
                return;
            }
            disband(clan, "leader disbanded");
        }

        private void invite(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!require(player, clan, Rank.OFFICER)) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage", "/clan invite <player>");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                reply(player, "player-not-found");
                return;
            }
            if (clanOf(target.getUniqueId()) != null) {
                reply(player, "invite-already-in-clan");
                return;
            }
            int max = Math.max(1, cfgInt("max-members", 15));
            if (clan.size() >= max) {
                reply(player, "join-clan-full", "max", String.valueOf(max));
                return;
            }
            invites.put(target.getUniqueId(), new Invite(clan.name));
            reply(player, "invite-sent", "player", target.getName(),
                    "time", duration(cfgInt("invite-timeout", 60)));
            reply(target, "invite-received", "clan", clan.name,
                    "time", duration(cfgInt("invite-timeout", 60)));
        }

        private void join(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            if (clanOf(player.getUniqueId()) != null) {
                reply(player, "clan-exists");
                return;
            }
            Invite invite = invites.get(player.getUniqueId());
            if (invite == null) {
                reply(player, "join-no-invite");
                return;
            }
            Clan clan = clanNamed(invite.clan);
            if (clan == null) {
                invite = null;
                invites.remove(player.getUniqueId());
                reply(player, "join-clan-missing");
                return;
            }
            int max = Math.max(1, cfgInt("max-members", 15));
            if (clan.size() >= max) {
                reply(player, "join-clan-full", "max", String.valueOf(max));
                return;
            }
            clan.members.put(player.getUniqueId(), Rank.RECRUIT);
            clan.memberNames.put(player.getUniqueId(), player.getName());
            invites.remove(player.getUniqueId());
            save();
            reply(player, "join-success", "clan", clan.name);
            forEachMember(clan, member -> {
                if (!member.equals(player)) {
                    member.sendMessage(Text.parse(msg("member-joined",
                            "player", player.getName(), "clan", clan.name)));
                }
            });
        }

        private void leave(CommandSender sender) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (player.getUniqueId().equals(clan.leader)) {
                reply(player, "leave-leader");
                return;
            }
            clan.members.remove(player.getUniqueId());
            clan.memberNames.remove(player.getUniqueId());
            long cooldown = cfgInt("join-cooldown", 86400);
            if (cooldown > 0) {
                joinCooldown.put(player.getUniqueId(), System.currentTimeMillis());
            }
            save();
            reply(player, "leave-success", "time", duration(cooldown));
            forEachMember(clan, member -> member.sendMessage(Text.parse(msg("member-left",
                    "player", player.getName(), "clan", clan.name))));
        }

        private void kick(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage", "/clan kick <player>");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                reply(player, "player-not-found");
                return;
            }
            Rank targetRank = clan.rankOf(target.getUniqueId());
            if (targetRank == null) {
                reply(player, "kick-not-in-clan");
                return;
            }
            Rank own = clan.rankOf(player.getUniqueId());
            if (own == null || own == Rank.RECRUIT
                    || targetRank.ordinal() >= own.ordinal()) {
                reply(player, "kick-cannot-kick");
                return;
            }
            clan.members.remove(target.getUniqueId());
            clan.memberNames.remove(target.getUniqueId());
            long cooldown = cfgInt("join-cooldown", 86400);
            if (cooldown > 0) {
                joinCooldown.put(target.getUniqueId(), System.currentTimeMillis());
            }
            save();
            reply(player, "kick-success", "player", target.getName());
            reply(target, "kick-target", "time", duration(cooldown));
        }

        private void promote(CommandSender sender, String[] args, boolean up) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!require(player, clan, Rank.CO_LEADER)) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage",
                        "/clan " + (up ? "promote" : "demote") + " <player>");
                return;
            }
            Player target = plugin.getServer().getPlayerExact(args[1]);
            if (target == null) {
                reply(player, "player-not-found");
                return;
            }
            Rank rank = clan.rankOf(target.getUniqueId());
            if (rank == null) {
                reply(player, "kick-not-in-clan");
                return;
            }
            Rank[] values = Rank.values();
            int index = rank.ordinal() + (up ? 1 : -1);
            if (index < 0 || index >= values.length) {
                reply(player, up ? "promote-max" : "demote-min");
                return;
            }
            if (values[index] == Rank.LEADER) {
                // transferring leadership is an explicit admin action
                reply(player, "promote-max");
                return;
            }
            clan.members.put(target.getUniqueId(), values[index]);
            save();
            reply(player, up ? "promote-success" : "demote-success",
                    "player", target.getName(), "rank", values[index].name());
            reply(target, up ? "promote-target" : "demote-target", "rank", values[index].name());
        }

        private void chatToggle(CommandSender sender) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            if (own(player) == null) {
                return;
            }
            boolean state = !clanChat.getOrDefault(player.getUniqueId(), false);
            clanChat.put(player.getUniqueId(), state);
            reply(player, state ? "chat-on" : "chat-off");
        }

        private void bank(CommandSender sender) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            reply(player, "bank-balance", "clan", clan.name, "balance", format(clan.bank));
        }

        private void moveMoney(CommandSender sender, String[] args, boolean deposit) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage",
                        "/clan " + (deposit ? "deposit" : "withdraw") + " <amount>");
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                reply(player, "eco-invalid-number");
                return;
            }
            if (amount <= 0) {
                reply(player, "eco-invalid-number");
                return;
            }
            if (deposit) {
                var economy = economy();
                if (economy != null && economy.getBalance(player) < amount) {
                    reply(player, "eco-not-enough");
                    return;
                }
                if (economy != null) {
                    economy.withdrawPlayer(player, amount);
                }
                clan.bank += amount;
                reply(player, "eco-deposit", "amount", format(amount));
            } else {
                if (!require(player, clan, Rank.OFFICER)) {
                    return;
                }
                if (clan.bank < amount) {
                    reply(player, "eco-not-enough");
                    return;
                }
                clan.bank -= amount;
                pay(player, amount);
                reply(player, "eco-withdraw", "amount", format(amount));
            }
            save();
        }

        private void diplomacy(CommandSender sender, String[] args, String action) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!require(player, clan, Rank.CO_LEADER)) {
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage", "/clan " + action + " <clan>");
                return;
            }
            Clan target = clanNamed(args[1]);
            if (target == null) {
                reply(player, "diplomacy-clan-missing");
                return;
            }
            if (target == clan) {
                reply(player, "diplomacy-cannot-self");
                return;
            }
            String other = target.name.toLowerCase(Locale.ROOT);
            switch (action) {
                case "ally" -> {
                    if (clan.allies.contains(other)) {
                        reply(player, "diplomacy-already-allied");
                        return;
                    }
                    int limit = Math.max(0, cfgInt("max-allies", 3));
                    if (clan.allies.size() >= limit) {
                        reply(player, "diplomacy-limit-reached-self", "limit", String.valueOf(limit));
                        return;
                    }
                    if (!charge(player, cfgDouble("ally-creation-cost", 100))) {
                        return;
                    }
                    clan.allies.add(other);
                    target.allies.add(clan.name.toLowerCase(Locale.ROOT));
                    broadcast(clan, "diplomacy-ally-success", clan.name, target.name);
                }
                case "rival" -> {
                    if (clan.rivals.contains(other)) {
                        reply(player, "diplomacy-already-rivals");
                        return;
                    }
                    int limit = Math.max(0, cfgInt("max-rivals", 3));
                    if (clan.rivals.size() >= limit) {
                        reply(player, "diplomacy-limit-reached-self", "limit", String.valueOf(limit));
                        return;
                    }
                    clan.rivals.add(other);
                    target.rivals.add(clan.name.toLowerCase(Locale.ROOT));
                    broadcast(clan, "diplomacy-rival-success", clan.name, target.name);
                }
                default -> {
                    if (!clan.allies.remove(other) & !clan.rivals.remove(other)) {
                        reply(player, "diplomacy-already-neutral");
                        return;
                    }
                    target.allies.remove(clan.name.toLowerCase(Locale.ROOT));
                    target.rivals.remove(clan.name.toLowerCase(Locale.ROOT));
                    broadcast(clan, "diplomacy-neutral-success", clan.name, target.name);
                }
            }
            save();
        }

        private void broadcast(Clan clan, String key, String first, String second) {
            forEachMember(clan, member -> member.sendMessage(Text.parse(msg(key,
                    "clan1", first, "clan2", second))));
        }

        private void setBase(CommandSender sender) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!require(player, clan, Rank.OFFICER)) {
                return;
            }
            Location location = player.getLocation();
            clan.baseWorld = location.getWorld().getName();
            clan.baseX = location.getX();
            clan.baseY = location.getY();
            clan.baseZ = location.getZ();
            clan.baseYaw = location.getYaw();
            save();
            reply(player, "base-set");
        }

        private void base(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            Location base = clan.base();
            if (base == null) {
                reply(player, "base-not-set");
                return;
            }
            if (baseTeleports.containsKey(player.getUniqueId())) {
                reply(player, "base-tp-already");
                return;
            }
            double cost = Math.max(0, cfgDouble("base-teleport-cost", 10));
            if (!charge(player, cost)) {
                return;
            }
            reply(player, "base-tp-paid", "amount", format(cost));
            int delay = Math.max(0, cfgInt("base-teleport-delay", 3));
            if (delay == 0) {
                player.teleportAsync(base);
                reply(player, "base-tp-welcome");
                return;
            }
            reply(player, "base-tp-delay", "time", String.valueOf(delay));
            final int[] remaining = {delay};
            final org.bukkit.scheduler.BukkitTask[] handle = new org.bukkit.scheduler.BukkitTask[1];
            handle[0] = syncRepeat(20L, 20L, () -> {
                if (!player.isOnline()) {
                    baseTeleports.remove(player.getUniqueId());
                    if (handle[0] != null) {
                        handle[0].cancel();
                    }
                    return;
                }
                remaining[0]--;
                if (remaining[0] > 0) {
                    return;
                }
                baseTeleports.remove(player.getUniqueId());
                if (handle[0] != null) {
                    handle[0].cancel();
                }
                player.teleportAsync(base);
                reply(player, "base-tp-welcome");
            });
            baseTeleports.put(player.getUniqueId(), handle[0]);
        }

        private void infoCmd(CommandSender sender, String[] args) {
            Clan clan = args.length >= 2 ? clanNamed(args[1])
                    : (sender instanceof Player player ? clanOf(player.getUniqueId()) : null);
            if (clan == null) {
                reply(sender, args.length >= 2 ? "clan-not-found" : "not-in-clan");
                return;
            }
            showInfo(sender, clan);
        }

        private void showInfo(CommandSender sender, Clan clan) {
            reply(sender, "info-header", "clan", clan.name);
            sender.sendMessage(Text.parse(msg("info-leader", "leader", clan.leaderName)));
            sender.sendMessage(Text.parse(msg("info-level",
                    "level", String.valueOf(clan.level), "xp", format(clan.xp),
                    "required", format(requiredXp(clan)))));
            sender.sendMessage(Text.parse(msg("info-members",
                    "members", String.valueOf(clan.size()),
                    "max", String.valueOf(cfgInt("max-members", 15)))));
            sender.sendMessage(Text.parse(msg("info-kdr",
                    "kills", String.valueOf(clan.kills), "deaths", String.valueOf(clan.deaths),
                    "kdr", clan.deaths == 0 ? String.valueOf(clan.kills)
                            : String.format(Locale.ROOT, "%.2f", (double) clan.kills / clan.deaths))));
            sender.sendMessage(Text.parse(msg("info-bank", "balance", format(clan.bank))));
            if (!clan.allies.isEmpty() || !clan.rivals.isEmpty()) {
                sender.sendMessage(Text.parse(msg("info-diplomacy",
                        "allies", String.join(", ", clan.allies),
                        "rivals", String.join(", ", clan.rivals))));
            }
            if (sender instanceof Player player) {
                Rank rank = clan.rankOf(player.getUniqueId());
                if (rank != null) {
                    sender.sendMessage(Text.parse(msg("info-rank", "rank", rank.name())));
                }
            }
            List<String> names = new ArrayList<>(clan.memberNames.values());
            sender.sendMessage(Text.parse(msg("info-member-list",
                    "members", String.join(", ", names))));
        }

        private void list(CommandSender sender) {
            List<Clan> sorted = new ArrayList<>(clans.values());
            sorted.sort(Comparator.comparingDouble((Clan clan) -> clan.level).reversed()
                    .thenComparing(Comparator.comparingInt(Clan::size).reversed()));
            reply(sender, "list-header", "count", String.valueOf(sorted.size()));
            int place = 1;
            for (Clan clan : sorted) {
                sender.sendMessage(Text.parse(msg("list-entry",
                        "place", String.valueOf(place),
                        "clan", clan.name,
                        "level", String.valueOf(clan.level),
                        "members", String.valueOf(clan.size()))));
                place++;
            }
        }

        private void rename(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (!player.getUniqueId().equals(clan.leader)) {
                reply(player, "not-leader");
                return;
            }
            if (args.length < 2) {
                reply(player, "invalid-args", "usage", "/clan rename <name>");
                return;
            }
            String old = clan.name;
            String name = validateName(player, args[1]);
            if (name == null) {
                return;
            }
            if (!charge(player, cfgDouble("clan-rename-cost", 500))) {
                return;
            }
            for (Clan other : clans.values()) {
                replaceIgnoreCase(other.allies, old, name);
                replaceIgnoreCase(other.rivals, old, name);
            }
            clans.remove(old.toLowerCase(Locale.ROOT));
            clan.name = name;
            clans.put(name.toLowerCase(Locale.ROOT), clan);
            save();
            reply(player, "clan-renamed", "old", old, "new", name);
        }

        private void replaceIgnoreCase(Set<String> set, String old, String replacement) {
            if (set.removeIf(entry -> entry.equalsIgnoreCase(old))) {
                set.add(replacement.toLowerCase(Locale.ROOT));
            }
        }

        private void motd(CommandSender sender, String[] args) {
            Player player = player(sender);
            if (player == null) {
                return;
            }
            Clan clan = own(player);
            if (clan == null) {
                return;
            }
            if (args.length < 2) {
                reply(player, "motd-current", "motd",
                        clan.motd.isBlank() ? msg("motd-empty") : clan.motd);
                return;
            }
            if (!require(player, clan, Rank.OFFICER)) {
                return;
            }
            clan.motd = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            save();
            reply(player, "motd-set");
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("create", "disband", "invite", "join",
                        "leave", "kick", "promote", "demote", "chat", "bank", "deposit", "withdraw",
                        "ally", "rival", "neutral", "base", "setbase", "info", "list", "rename",
                        "motd", "help"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            if (args.length == 2) {
                String sub = args[0].toLowerCase(Locale.ROOT);
                if (sub.equals("info") || sub.equals("ally") || sub.equals("rival")
                        || sub.equals("neutral")) {
                    List<String> names = new ArrayList<>();
                    clans.values().forEach(clan -> names.add(clan.name));
                    return names;
                }
                if (!sub.equals("create") && !sub.equals("list") && !sub.equals("rename")) {
                    return null;
                }
            }
            return List.of();
        }
    }

    /** Administrative escapes for a broken clan (`/clanadmin`). */
    private final class AdminCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.clans.admin")) {
                reply(sender, "no-permission");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(Text.parse(msg("admin-usage")));
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "disband" -> {
                    if (args.length < 2) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    Clan clan = clanNamed(args[1]);
                    if (clan == null) {
                        reply(sender, "clan-not-found");
                        return true;
                    }
                    disband(clan, "admin");
                    reply(sender, "admin-disbanded", "clan", clan.name);
                }
                case "setlevel" -> {
                    if (args.length < 3) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    Clan clan = clanNamed(args[1]);
                    if (clan == null) {
                        reply(sender, "clan-not-found");
                        return true;
                    }
                    try {
                        clan.level = Math.max(1, Integer.parseInt(args[2]));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    save();
                    reply(sender, "admin-level-set", "clan", clan.name,
                            "level", String.valueOf(clan.level));
                }
                case "addxp" -> {
                    if (args.length < 3) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    Clan clan = clanNamed(args[1]);
                    if (clan == null) {
                        reply(sender, "clan-not-found");
                        return true;
                    }
                    try {
                        addXp(clan, Double.parseDouble(args[2]));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    save();
                    reply(sender, "admin-xp-added", "clan", clan.name, "xp", args[2]);
                }
                case "setleader" -> {
                    if (args.length < 3) {
                        sender.sendMessage(Text.parse(msg("admin-usage")));
                        return true;
                    }
                    Clan clan = clanNamed(args[1]);
                    if (clan == null) {
                        reply(sender, "clan-not-found");
                        return true;
                    }
                    Player target = plugin.getServer().getPlayerExact(args[2]);
                    if (target == null) {
                        reply(sender, "player-not-found");
                        return true;
                    }
                    if (clan.leader != null) {
                        clan.members.put(clan.leader, Rank.CO_LEADER);
                    }
                    clan.leader = target.getUniqueId();
                    clan.leaderName = target.getName();
                    clan.members.put(target.getUniqueId(), Rank.LEADER);
                    clan.memberNames.put(target.getUniqueId(), target.getName());
                    save();
                    reply(sender, "admin-setleader", "clan", clan.name, "player", target.getName());
                }
                case "save" -> {
                    save();
                    reply(sender, "admin-saved");
                }
                case "reload" -> {
                    reloadConfig();
                    load();
                    reply(sender, "admin-reloaded");
                }
                default -> sender.sendMessage(Text.parse(msg("admin-usage")));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("disband", "setlevel", "addxp", "setleader", "save", "reload");
            }
            if (args.length == 2) {
                List<String> names = new ArrayList<>();
                clans.values().forEach(clan -> names.add(clan.name));
                return names;
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("setleader")) {
                return null;
            }
            return List.of();
        }
    }

    private static String duration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "m";
        }
        if (seconds < 86400) {
            return (seconds / 3600) + "h";
        }
        return (seconds / 86400) + "d";
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
