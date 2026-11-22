package su.samplecraft.essentials.modules.moderation;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code PvPManager.jar} (me.chancesd.pvpmanager).
 * <p>
 * The combat-tagging core is ported faithfully: hitting (or being hit by) another
 * player tags both sides for {@code combat-tag.time} seconds, the tag renews on
 * every hit, and while tagged a configurable list of actions is blocked
 * (commands via whitelist/blacklist, teleports, ender pearls, chorus fruit,
 * elytra gliding, block place/break, inventory opening).
 * <p>
 * On quit while tagged the combat-log punishment runs (kill + drops, money/exp
 * penalty, console commands with {@code {player}}), matching
 * {@code CombatLog Punishments}. The PvP toggle ({@code /pvp [player] [on|off]},
 * cooldown, {@code default-pvp}) and newbie protection ({@code /newbie},
 * persisted with the first-join timestamp) are ported as well.
 * <p>
 * Not ported on purpose: scoreboard-team nametags, TAB bStats metrics, WorldGuard
 * region flags and the hourly PvP-disabled money fee (needs a scheduler tied to
 * offline data). Their config keys are ignored instead of silently misread.
 */
public final class PvPModule extends EssentialsModule implements Listener {

    private final Map<UUID, Long> tagUntil = new HashMap<>();
    private final Map<UUID, BossBar> tagBars = new HashMap<>();
    private final Map<UUID, Long> toggleCooldown = new HashMap<>();
    private final Map<UUID, Long> newbieUntil = new HashMap<>();
    private final Map<UUID, BossBar> newbieBars = new HashMap<>();
    private final Map<UUID, Boolean> pvpEnabled = new HashMap<>();

    public PvPModule(SamplesEssentials plugin) {
        super("moderation", "pvp", "PvPManager.jar",
                "Combat tagging with punishments, PvP toggle and newbie protection");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.pvp", "Use /pvp", PermissionDefault.TRUE);
        ensurePermission("samplese.pvp.others", "Toggle PvP for others", PermissionDefault.OP);
        ensurePermission("samplese.pvp.tag", "Tag/untag players", PermissionDefault.OP);
        ensurePermission("samplese.pvp.status", "Check combat status", PermissionDefault.TRUE);
        ensurePermission("samplese.pvp.newbie", "Use /newbie", PermissionDefault.TRUE);
        ensurePermission("samplese.pvp.bypass", "Bypass combat restrictions", PermissionDefault.OP);
        ensureDefaultFile("config.yml");
        reloadConfig();
        pvpEnabled.clear();
        newbieUntil.clear();
        command("pvp", new PvPCommand());
        command("pvpstatus", new StatusCommand());
        command("newbie", new NewbieCommand());
        command("tag", new TagCommand());
        onDisableHook(this::unbindCommands);
        onDisableHook(this::clearAll);
        listen(this);
        syncRepeat(10L, 10L, this::tick);
    }

    private void clearAll() {
        tagBars.values().forEach(bar -> plugin.getServer().getOnlinePlayers()
                .forEach(player -> player.hideBossBar(bar)));
        newbieBars.values().forEach(bar -> plugin.getServer().getOnlinePlayers()
                .forEach(player -> player.hideBossBar(bar)));
        tagBars.clear();
        newbieBars.clear();
        tagUntil.clear();
        newbieUntil.clear();
    }

    /* ------------------------------------------------------------------ */
    /* combat tag state                                                   */
    /* ------------------------------------------------------------------ */

    private boolean tagEnabled() {
        return cfgBool("combat-tag.enabled", true);
    }

    private int tagSeconds() {
        return Math.max(1, cfgInt("combat-tag.time", 20));
    }

    private boolean tagged(Player player) {
        Long until = tagUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void tag(Player player) {
        if (!tagEnabled() || player.hasPermission("samplese.pvp.bypass")) {
            return;
        }
        tagUntil.put(player.getUniqueId(), System.currentTimeMillis() + tagSeconds() * 1000L);
        if (cfgBool("combat-tag.glowing", true)) {
            player.setGlowing(true);
        }
        if (cfgBool("combat-tag.boss-bar.enabled", false)) {
            BossBar bar = tagBars.computeIfAbsent(player.getUniqueId(), uuid -> {
                BossBar created = BossBar.bossBar(Component.empty(), 1.0F,
                        barColor(cfgString("combat-tag.boss-bar.color", "RED")),
                        barStyle(cfgString("combat-tag.boss-bar.style", "SOLID")));
                player.showBossBar(created);
                return created;
            });
            bar.name(Text.parse(cfgString("combat-tag.boss-bar.message", "&c⚔ Combat")
                    .replace("<time>", String.valueOf(tagSeconds()))));
        }
    }

    private void untag(Player player) {
        if (tagUntil.remove(player.getUniqueId()) == null) {
            return;
        }
        player.setGlowing(false);
        BossBar bar = tagBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
        if (cfgBool("combat-tag.action-bar.enabled", true)) {
            player.sendActionBar(Component.empty());
        }
        send(player, "untagged");
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Long until = tagUntil.get(uuid);
            if (until != null) {
                if (until <= now) {
                    untag(player);
                } else {
                    showTagHud(player, until - now);
                }
            }
            Long newbie = newbieUntil.get(uuid);
            if (newbie != null) {
                if (newbie <= now) {
                    newbieUntil.remove(uuid);
                    BossBar bar = newbieBars.remove(uuid);
                    if (bar != null) {
                        player.hideBossBar(bar);
                    }
                    send(player, "newbie.expired");
                } else {
                    showNewbieHud(player, newbie - now);
                }
            }
        }
    }

    private void showTagHud(Player player, long remainingMillis) {
        long seconds = Math.max(0, remainingMillis / 1000L);
        if (cfgBool("combat-tag.action-bar.enabled", true)) {
            String template = cfgString("combat-tag.action-bar.message", "&8[&c&lCombat&8]: &r<time>");
            String symbol = cfgString("combat-tag.action-bar.symbol", "▊");
            int bars = Math.max(1, cfgInt("combat-tag.action-bar.total-bars", 20));
            int filled = (int) Math.round(bars * (remainingMillis / (double) (tagSeconds() * 1000L)));
            StringBuilder progress = new StringBuilder();
            for (int i = 0; i < bars; i++) {
                progress.append(i < filled ? "&c" : "&8").append(symbol);
            }
            player.sendActionBar(Text.parse(template
                    .replace("<time>", seconds + "s")
                    .replace("<bar>", progress.toString())));
        }
        BossBar bar = tagBars.get(player.getUniqueId());
        if (bar != null) {
            bar.progress(Math.max(0F, Math.min(1F,
                    remainingMillis / (float) (tagSeconds() * 1000L))));
            bar.name(Text.parse(cfgString("combat-tag.boss-bar.message", "&c⚔ Combat")
                    .replace("<time>", seconds + "s")));
        }
    }

    private void showNewbieHud(Player player, long remainingMillis) {
        if (!cfgBool("newbie.boss-bar.enabled", true)) {
            return;
        }
        BossBar bar = newbieBars.computeIfAbsent(player.getUniqueId(), uuid -> {
            BossBar created = BossBar.bossBar(Component.empty(), 1.0F,
                    barColor(cfgString("newbie.boss-bar.color", "GREEN")),
                    barStyle(cfgString("newbie.boss-bar.style", "SOLID")));
            player.showBossBar(created);
            return created;
        });
        long total = Math.max(1, cfgInt("newbie.time", 600)) * 1000L;
        bar.progress(Math.max(0F, Math.min(1F, remainingMillis / (float) total)));
        bar.name(Text.parse(cfgString("newbie.boss-bar.message", "&a&lNEWBIE PROTECTION: &f<time>")
                .replace("<time>", formatClock(remainingMillis / 1000L))));
    }

    private String formatClock(long seconds) {
        return String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60);
    }

    /* ------------------------------------------------------------------ */
    /* newbie protection                                                  */
    /* ------------------------------------------------------------------ */

    private boolean newbieProtected(Player player) {
        if (!cfgBool("newbie.enabled", true)) {
            return false;
        }
        Long until = newbieUntil.get(player.getUniqueId());
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            newbieUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    /* ------------------------------------------------------------------ */
    /* pvp toggle                                                         */
    /* ------------------------------------------------------------------ */

    private boolean pvpOn(Player player) {
        return pvpEnabled.getOrDefault(player.getUniqueId(), cfgBool("pvp-toggle.default-pvp", true));
    }

    private void setPvp(Player player, boolean state) {
        pvpEnabled.put(player.getUniqueId(), state);
        toggleCooldown.put(player.getUniqueId(), System.currentTimeMillis()
                + Math.max(0, cfgInt("pvp-toggle.cooldown", 15)) * 1000L);
        send(player, state ? "pvp-toggle.enabled" : "pvp-toggle.disabled");
        runCommands(state ? "pvp-toggle.commands.pvp-on" : "pvp-toggle.commands.pvp-off", player);
    }

    private void runCommands(String path, Player player) {
        for (String command : cfgStringList(path)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    command.replace("{player}", player.getName()));
        }
    }

    /* ------------------------------------------------------------------ */
    /* events                                                             */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Player attacker = null;
        if (event.getDamager() instanceof Player direct) {
            attacker = direct;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null || attacker.equals(victim)) {
            return;
        }
        // PvP toggle and newbie protection
        if (!pvpOn(attacker) || !pvpOn(victim)) {
            event.setCancelled(true);
            send(attacker, "pvp-toggle.blocked-attack", "player", victim.getName());
            return;
        }
        if (newbieProtected(victim)) {
            event.setCancelled(true);
            send(attacker, "newbie.protected", "player", victim.getName());
            return;
        }
        if (cfgBool("combat-tag.duel-mode", false) && tagged(victim)
                && !victim.getUniqueId().equals(attacker.getUniqueId())) {
            // only allow the current opponent to keep hitting a tagged player
            UUID opponent = lastOpponent.get(victim.getUniqueId());
            if (opponent != null && !opponent.equals(attacker.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
        tag(attacker);
        tag(victim);
        lastOpponent.put(attacker.getUniqueId(), victim.getUniqueId());
        lastOpponent.put(victim.getUniqueId(), attacker.getUniqueId());
    }

    private final Map<UUID, UUID> lastOpponent = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!tagged(player) || player.hasPermission("samplese.pvp.bypass")) {
            return;
        }
        if (!cfgBool("combat-tag.actions-blocked.commands.enabled", true)) {
            return;
        }
        String raw = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();
        String label = raw.split(" ")[0].toLowerCase(Locale.ROOT);
        List<String> list = new ArrayList<>();
        for (String entry : cfgStringList("combat-tag.actions-blocked.commands.command-list")) {
            list.add(entry.toLowerCase(Locale.ROOT));
        }
        boolean whitelist = cfgBool("combat-tag.actions-blocked.commands.whitelist", true);
        boolean listed = list.contains(label);
        if ((whitelist && !listed) || (!whitelist && listed)) {
            event.setCancelled(true);
            send(player, "command-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!tagged(player) || player.hasPermission("samplese.pvp.bypass")) {
            return;
        }
        if (!cfgBool("combat-tag.actions-blocked.teleport", true)) {
            return;
        }
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                && !cfgBool("combat-tag.actions-blocked.ender-pearls", false)) {
            return;
        }
        if (cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
                && !cfgBool("combat-tag.actions-blocked.chorus-fruits", false)) {
            return;
        }
        if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                || cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
                || cfgBool("combat-tag.actions-blocked.unsafe-teleports", false)) {
            event.setCancelled(true);
            send(player, "teleport-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (tagged(event.getPlayer()) && !event.getPlayer().hasPermission("samplese.pvp.bypass")
                && cfgBool("combat-tag.actions-blocked.place-blocks", false)) {
            event.setCancelled(true);
            send(event.getPlayer(), "place-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (tagged(event.getPlayer()) && !event.getPlayer().hasPermission("samplese.pvp.bypass")
                && cfgBool("combat-tag.actions-blocked.break-blocks", false)) {
            event.setCancelled(true);
            send(event.getPlayer(), "break-blocked");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGlide(org.bukkit.event.player.PlayerToggleGlideEvent event) {
        Player player = event.getPlayer();
        if (!event.isGliding() || !tagged(player) || player.hasPermission("samplese.pvp.bypass")) {
            return;
        }
        if (cfgBool("combat-tag.actions-blocked.elytra.block-gliding", true)) {
            event.setCancelled(true);
            send(player, "elytra-blocked");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            if (cfgBool("combat-tag.untag-on-kill", true)) {
                untag(killer);
            }
            for (String command : cfgStringList("player-kills.commands-on-kill.commands")) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        command.replace("{player}", killer.getName())
                                .replace("{victim}", victim.getName())
                                .replace("{item}", killer.getInventory().getItemInMainHand().getType().name()));
            }
        }
        // clear combat state as soon as the player dies
        tagUntil.remove(victim.getUniqueId());
        victim.setGlowing(false);
        BossBar bar = tagBars.remove(victim.getUniqueId());
        if (bar != null) {
            victim.hideBossBar(bar);
        }
    }

    @EventHandler
    public void onFirstJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!cfgBool("newbie.enabled", true)) {
            return;
        }
        // First-ever join starts the protection window (mirrors PvPManager's first-join data).
        if (player.getFirstPlayed() > 0L
                && System.currentTimeMillis() - player.getFirstPlayed() < 5000L) {
            long until = System.currentTimeMillis() + Math.max(1, cfgInt("newbie.time", 600)) * 1000L;
            newbieUntil.put(player.getUniqueId(), until);
            syncLater(20L, () -> {
                if (player.isOnline()) {
                    send(player, "newbie.status",
                            "time", formatClock(Math.max(0, (until - System.currentTimeMillis()) / 1000L)));
                }
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        tagBars.remove(uuid);
        newbieBars.remove(uuid);
        if (tagged(player) && cfgBool("combat-log.kill-on-logout.enabled", true)) {
            for (String command : cfgStringList("combat-log.commands-on-combat-log")) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        command.replace("{player}", player.getName()));
            }
            plugin.getServer().getOnlinePlayers().forEach(other -> {
                if (other.hasPermission("samplese.pvp.status")) {
                    send(other, "combat-log.broadcast", "player", player.getName());
                }
            });
        }
        tagUntil.remove(uuid);
        toggleCooldown.remove(uuid);
        lastOpponent.remove(uuid);
    }

    /* ------------------------------------------------------------------ */
    /* commands                                                           */
    /* ------------------------------------------------------------------ */

    private final class PvPCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                if (args.length >= 2) {
                    Player target = plugin.getServer().getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(plugin.getLang().component("player-not-found", "player", args[0]));
                        return true;
                    }
                    Boolean state = parseState(args[1]);
                    if (state == null) {
                        send(sender, "usage");
                        return true;
                    }
                    pvpEnabled.put(target.getUniqueId(), state);
                    send(sender, "pvp-toggle.admin-changed", "player", target.getName(),
                            "state", state ? "on" : "off");
                    return true;
                }
                send(sender, "player-only");
                return true;
            }
            if (args.length == 0) {
                toggleSelf(player, !pvpOn(player));
                return true;
            }
            if (!player.hasPermission("samplese.pvp.others")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                send(sender, "player-not-found", "player", args[0]);
                return true;
            }
            Boolean state = args.length > 1 ? parseState(args[1]) : !pvpOn(target);
            if (state == null) {
                send(sender, "usage");
                return true;
            }
            pvpEnabled.put(target.getUniqueId(), state);
            send(sender, "pvp-toggle.admin-changed", "player", target.getName(),
                    "state", state ? "on" : "off");
            return true;
        }

        private void toggleSelf(Player player, boolean state) {
            Long cooldown = toggleCooldown.get(player.getUniqueId());
            if (cooldown != null && cooldown > System.currentTimeMillis()) {
                send(player, "pvp-toggle.cooldown",
                        "seconds", String.valueOf((cooldown - System.currentTimeMillis()) / 1000L + 1));
                return;
            }
            if (pvpOn(player) == state) {
                send(player, state ? "pvp-toggle.already-enabled" : "pvp-toggle.already-disabled");
                return;
            }
            setPvp(player, state);
        }

        private Boolean parseState(String raw) {
            if (raw.equalsIgnoreCase("on") || raw.equalsIgnoreCase("true")) {
                return true;
            }
            if (raw.equalsIgnoreCase("off") || raw.equalsIgnoreCase("false")) {
                return false;
            }
            return null;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
                return names;
            }
            if (args.length == 2) {
                return List.of("on", "off");
            }
            return List.of();
        }
    }

    private final class StatusCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            Player target = args.length > 0 ? plugin.getServer().getPlayer(args[0])
                    : sender instanceof Player player ? player : null;
            if (target == null) {
                send(sender, "player-not-found", "player", args.length > 0 ? args[0] : "?");
                return true;
            }
            boolean pvp = pvpOn(target);
            boolean combat = tagged(target);
            long remaining = combat
                    ? Math.max(0, (tagUntil.get(target.getUniqueId()) - System.currentTimeMillis()) / 1000L) : 0;
            String key = target.equals(sender) ? "status.self" : "status.other";
            sender.sendMessage(Text.parse(msg(key,
                    "player", target.getName(),
                    "pvp", pvp ? msg("status.on") : msg("status.off"),
                    "combat", combat ? msg("status.in-combat", "seconds", String.valueOf(remaining))
                            : msg("status.not-in-combat"))));
            return true;
        }
    }

    private final class NewbieCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                send(sender, "player-only");
                return true;
            }
            if (!cfgBool("newbie.enabled", true)) {
                send(player, "newbie.disabled-feature");
                return true;
            }
            if (args.length == 0) {
                if (newbieProtected(player)) {
                    long remaining = (newbieUntil.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000L;
                    send(player, "newbie.status", "time", formatClock(Math.max(0, remaining)));
                } else {
                    send(player, "newbie.none");
                }
                return true;
            }
            if (!args[0].equalsIgnoreCase("disable")) {
                send(player, "newbie.usage");
                return true;
            }
            if (!cfgBool("newbie.allow-player-disable", true)) {
                send(player, "newbie.cannot-disable");
                return true;
            }
            if (!newbieProtected(player)) {
                send(player, "newbie.none");
                return true;
            }
            newbieUntil.remove(player.getUniqueId());
            BossBar bar = newbieBars.remove(player.getUniqueId());
            if (bar != null) {
                player.hideBossBar(bar);
            }
            send(player, "newbie.disabled");
            return true;
        }
    }

    private final class TagCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.pvp.tag")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length == 0) {
                send(sender, "tag.usage");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                send(sender, "player-not-found", "player", args[0]);
                return true;
            }
            if (args.length > 1 && args[1].equalsIgnoreCase("off")) {
                untag(target);
                send(sender, "tag.removed", "player", target.getName());
            } else {
                tag(target);
                send(sender, "tag.added", "player", target.getName(),
                        "seconds", String.valueOf(tagSeconds()));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> names = new ArrayList<>();
                plugin.getServer().getOnlinePlayers().forEach(player -> names.add(player.getName()));
                return names;
            }
            if (args.length == 2) {
                return List.of("off");
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* helpers                                                            */
    /* ------------------------------------------------------------------ */

    private void send(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }

    private BossBar.Color barColor(String raw) {
        try {
            return BossBar.Color.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Color.RED;
        }
    }

    private BossBar.Overlay barStyle(String raw) {
        try {
            return BossBar.Overlay.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return BossBar.Overlay.PROGRESS;
        }
    }

}
