package su.samplecraft.essentials.modules.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code FancyTags-unspecified.jar} (dev.vatten.baserad).
 * <p>
 * The original renders "tags" out of player-head (and atlas sprite) symbols
 * inside chat messages. Modern Paper sends those as {@code object} components
 * ({@link ObjectContents#playerHead()}), which is exactly what the original
 * plugin uses. Ported behaviour:
 * <ul>
 *   <li>tag definitions live in {@code chat/fancytags/tags.yml} with the original
 *       {@code tags.<name>.symbols} layout ({@code playerHead: {value, signature}},
 *       {@code atlas: {atlas, sprite}});</li>
 *   <li>chat can be prefixed with the tags the player owns. The original hooks the
 *       chat renderer, so the tag appears inline before the player name. Here the
 *       format is configurable per group, e.g. {@code {tags} {name} » {message}} or
 *       {@code {leader}<{name}> {message}};</li>
 *   <li>{@code /tags list} prints every tag (rendered!), {@code /tags reload},
 *       {@code /tags test} reproduces the original demo output;</li>
 *   <li>the same strings are exposed as placeholders for the legacy
 *       {@code %tags_<name>%} syntax inside other modules' messages;</li>
 *   <li>cache settings of the original: tags are pushed to clients on join and on a
 *       timer via an off-screen title (MiniMessage), so player-head objects render
 *       without the skin-loading flicker.</li>
 * </ul>
 */
public final class FancyTagsModule extends EssentialsModule implements Listener {

    private record Symbol(String type, String value, String signature, String atlas, String sprite) {
    }

    private final Map<String, List<Symbol>> tags = new LinkedHashMap<>();
    private final Map<UUID, Component> tagCache = new ConcurrentHashMap<>();
    private YamlConfiguration tagsFile = new YamlConfiguration();

    public FancyTagsModule(SamplesEssentials plugin) {
        super("chat", "fancytags", "FancyTags-unspecified.jar", "Player-head chat tags rendered through object components");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.tags", "Use /tags", org.bukkit.permissions.PermissionDefault.TRUE);
        ensurePermission("samplese.tags.admin", "Reload tags", org.bukkit.permissions.PermissionDefault.OP);
        ensureTagPermissions();
        loadTags();
        command("tags", new TagsCommand());
        command("fancytags", new TagsCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
        if (cfgBool("cache-settings.enabled", true) && cfgBool("cache-settings.on-timer", true)) {
            long intervalTicks = Math.max(20L, (long) cfgDouble("cache-settings.interval-duration", 300) * 20L);
            syncRepeat(intervalTicks, intervalTicks, () -> plugin.getServer().getOnlinePlayers().forEach(this::pushCacheUpdate));
        }
    }

    /* ------------------------------------------------------------------ */
    /* tag storage (configs/TagsConfig + TagStore)                        */
    /* ------------------------------------------------------------------ */

    private void loadTags() {
        tags.clear();
        tagsFile = YamlConfiguration.loadConfiguration(ensureDefaultFile("tags.yml"));
        ConfigurationSection section = tagsFile.getConfigurationSection("tags");
        if (section == null) {
            return;
        }
        for (String name : section.getKeys(false)) {
            List<Symbol> symbols = new ArrayList<>();
            List<Map<?, ?>> raw = tagsFile.getMapList("tags." + name + ".symbols");
            for (Map<?, ?> entry : raw) {
                Object type = entry.get("type");
                if ("atlas".equals(type)) {
                    symbols.add(new Symbol("atlas", null, null,
                            String.valueOf(entry.get("atlas")), String.valueOf(entry.get("sprite"))));
                } else if (entry.get("value") != null) {
                    symbols.add(new Symbol("playerHead", String.valueOf(entry.get("value")),
                            entry.get("signature") == null ? null : String.valueOf(entry.get("signature")), null, null));
                }
            }
            // The original YAML is a polymorphic list; plain strings are accepted as a fallback
            if (raw.isEmpty()) {
                for (String line : tagsFile.getStringList("tags." + name + ".symbols")) {
                    symbols.add(new Symbol("text", line, null, null, null));
                }
            }
            tags.put(name.toLowerCase(Locale.ROOT), symbols);
        }
        tagCache.clear();
    }

    private void ensureTagPermissions() {
        for (String name : List.of("owner", "admin", "dev", "mod", "builder", "helper", "youtube", "twitch",
                "team", "player", "fancytags_logo", "simple", "stylish")) {
            ensurePermission("samplese.tags.tag." + name, "Show the " + name + " tag", org.bukkit.permissions.PermissionDefault.FALSE);
        }
    }

    /** Tag rendered as an adventure component, exactly like Tag#serialize. */
    public Component renderTag(String name) {
        List<Symbol> symbols = tags.get(name.toLowerCase(Locale.ROOT));
        if (symbols == null || symbols.isEmpty()) {
            return Component.empty();
        }
        Component result = Component.empty();
        for (Symbol symbol : symbols) {
            result = result.append(switch (symbol.type()) {
                case "atlas" -> Component.object(ObjectContents.sprite(
                        Key.key(symbol.atlas() == null ? "minecraft:blocks" : symbol.atlas()),
                        Key.key(symbol.sprite() == null ? "stone" : symbol.sprite())));
                case "playerHead" -> Component.object(ObjectContents.playerHead()
                        .profileProperty(new com.destroystokyo.paper.profile.ProfileProperty(
                                "textures", symbol.value(), symbol.signature()))
                        .build());
                default -> Text.parse(symbol.value() == null ? "" : symbol.value());
            });
        }
        return result;
    }

    /** All tags a player owns, in tag order (permission samplese.tags.tag.<name>). */
    public Component ownedTags(Player player) {
        return tagCache.computeIfAbsent(player.getUniqueId(), id -> {
            Component result = Component.empty();
            for (String name : tags.keySet()) {
                if (player.hasPermission("samplese.tags.tag." + name)) {
                    result = result.append(renderTag(name));
                }
            }
            return result;
        });
    }

    public void invalidate(UUID id) {
        tagCache.remove(id);
    }

    public List<String> tagNames() {
        return new ArrayList<>(tags.keySet());
    }

    /** MiniMessage string for a tag (used by placeholders / cache pushes). */
    public String tagMiniMessage(String name) {
        Component component = renderTag(name);
        return Text.MINI.serialize(component);
    }

    /* ------------------------------------------------------------------ */
    /* chat prefixing                                                     */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(io.papermc.paper.event.player.AsyncChatEvent event) {
        if (!cfgBool("chat.enabled", true)) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission("samplese.chat.format.bypass")) {
            return;
        }
        String format = resolveChatFormat(player);
        if (format == null) {
            return;
        }
        Component tagsComponent = ownedTags(player);
        Component name = Text.parse(resolveNamePart(player));
        Component message = event.message();

        // Render the format while inserting live components for {tags}/{name}/{message}
        Component built = Component.empty();
        String[] parts = format.split("\\{tags\\}|\\{name\\}|\\{message\\}", -1);
        String[] tokens = tokenize(format);
        for (int i = 0; i < parts.length; i++) {
            built = built.append(Text.parse(parts[i]));
            if (i < tokens.length) {
                built = built.append(switch (tokens[i]) {
                    case "tags" -> tagsComponent;
                    case "name" -> name;
                    default -> message;
                });
            }
        }
        Component finalRendered = built;
        event.renderer(io.papermc.paper.chat.ChatRenderer.viewerUnaware((source, displayName, msg) -> finalRendered));
    }

    private String[] tokenize(String format) {
        List<String> tokens = new ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{(tags|name|message)\\}").matcher(format);
        while (matcher.find()) {
            tokens.add(matcher.group(1));
        }
        return tokens.toArray(new String[0]);
    }

    private String resolveChatFormat(Player player) {
        ConfigurationSection groups = config().getConfigurationSection("chat.formats");
        String group = group(player);
        if (groups != null) {
            String format = groups.getString(group);
            if (format != null && !format.isBlank()) {
                return format;
            }
        }
        String base = cfgString("chat.format", "{tags} {name} &8» &f{message}");
        return base.isBlank() ? null : base;
    }

    private String resolveNamePart(Player player) {
        String style = cfgString("chat.name-format", "{name}");
        return style.replace("{name}", player.getName());
    }

    private String group(Player player) {
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String node = info.getPermission().toLowerCase(Locale.ROOT);
            if (info.getValue() && node.startsWith("samplese.group.")) {
                return node.substring("samplese.group.".length());
            }
        }
        return "default";
    }

    /* ------------------------------------------------------------------ */
    /* cache pushes (PluginConfig.CacheSettings)                          */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        invalidate(event.getPlayer().getUniqueId());
        if (cfgBool("cache-settings.enabled", true) && cfgBool("cache-settings.on-join", true)) {
            syncLater(20L, () -> pushCacheUpdate(event.getPlayer()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        invalidate(event.getPlayer().getUniqueId());
    }

    /**
     * The original pushes every tag off-screen via a title so the client caches the
     * player-head textures. {@code spacing} = two spaces per head, {@code offScreenRatio}
     * from the original config.
     */
    private void pushCacheUpdate(Player player) {
        if (!cfgBool("cache-settings.enabled", true)) {
            return;
        }
        List<String> names = tagNames();
        if (names.isEmpty()) {
            return;
        }
        double ratio = cfgDouble("cache-settings.off-screen-ratio", 2.5);
        StringBuilder spacer = new StringBuilder();
        int spacing = Math.max(1, (int) Math.ceil(ratio * names.size()));
        spacer.append("  ".repeat(spacing));
        Component title = Component.empty().append(Text.parse(spacer.toString()));
        for (String name : names) {
            title = title.append(renderTag(name)).append(Text.parse(spacer.toString()));
        }
        player.showTitle(net.kyori.adventure.title.Title.title(
                Component.empty(), title,
                net.kyori.adventure.title.Title.Times.times(java.time.Duration.ZERO,
                        java.time.Duration.ofMillis(500), java.time.Duration.ZERO)));
    }

    /* ------------------------------------------------------------------ */
    /* /tags (TagsCommand)                                                */
    /* ------------------------------------------------------------------ */

    private final class TagsCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
        @Override
        public boolean onCommand(@NotNull org.bukkit.command.CommandSender sender,
                                 @NotNull org.bukkit.command.Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (args.length >= 1) {
                switch (args[0].toLowerCase(Locale.ROOT)) {
                    case "reload" -> {
                        if (!player.hasPermission("samplese.tags.admin")) {
                            player.sendMessage(plugin.getLang().component("no-permission"));
                            return true;
                        }
                        reloadConfig();
                        loadTags();
                        player.sendMessage(msgComponent("reloaded"));
                        return true;
                    }
                    case "list" -> {
                        player.sendMessage(msgComponent("list-header"));
                        Component line = Component.empty();
                        int perLine = Math.max(1, cfgInt("list.per-line", 8));
                        int index = 0;
                        for (String name : tagNames()) {
                            line = line.append(renderTag(name)).append(Text.parse(" <gray>" + name + "</gray> "));
                            if (++index % perLine == 0) {
                                player.sendMessage(line);
                                line = Component.empty();
                            }
                        }
                        if (index % perLine != 0) {
                            player.sendMessage(line);
                        }
                        player.sendMessage(msgComponent("list-footer"));
                        return true;
                    }
                    case "test" -> {
                        player.sendMessage(Component.empty());
                        for (String name : tagNames()) {
                            if (player.hasPermission("samplese.tags.tag." + name) || player.hasPermission("samplese.tags.admin")) {
                                player.sendMessage(renderTag(name).append(Text.parse(" <white>" + player.getName() + ": this is the " + name + " tag")));
                            }
                        }
                        player.sendMessage(Component.empty());
                        return true;
                    }
                    default -> {
                        String name = args[0].toLowerCase(Locale.ROOT);
                        if (!tags.containsKey(name)) {
                            player.sendMessage(msgComponent("unknown-tag", "tag", name));
                            return true;
                        }
                        player.sendMessage(renderTag(name).append(Text.parse(" <gray>" + name + "</gray>")));
                        return true;
                    }
                }
            }
            player.sendMessage(msgComponent("help-header"));
            player.sendMessage(Text.parse(cfgString("help.list", "<gray>/tags list <dark_gray>- <white>list all tags")));
            player.sendMessage(Text.parse(cfgString("help.test", "<gray>/tags test <dark_gray>- <white>preview your tags")));
            player.sendMessage(Text.parse(cfgString("help.reload", "<gray>/tags reload <dark_gray>- <white>reload tags.yml")));
            player.sendMessage(Text.parse("<gray>Prefix with <white>%tags_&lt;name&gt;%</white> to use a tag elsewhere."));
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull org.bukkit.command.CommandSender sender,
                                          @NotNull org.bukkit.command.Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("list", "test", "reload"));
                options.addAll(tagNames());
                String prefix = args[0].toLowerCase(Locale.ROOT);
                return options.stream().filter(option -> option.startsWith(prefix)).map(String::toLowerCase).toList();
            }
            return List.of();
        }
    }

    /* ------------------------------------------------------------------ */
    /* placeholders used by other modules                                 */
    /* ------------------------------------------------------------------ */

    /** Resolves {@code %tags_<name>%} against the registered tags. */
    public String resolvePlaceholders(String raw) {
        if (raw == null || !raw.contains("%tags_")) {
            return raw;
        }
        String result = raw;
        for (String name : tagNames()) {
            result = result.replace("%tags_" + name + "%", tagMiniMessage(name));
        }
        return result;
    }

    /** Renders a placeholder list entry such as the PAPI expansion of the original. */
    public static Component itemPreview(ItemStack stack) {
        if (stack == null || stack.getType() != Material.PLAYER_HEAD) {
            return Component.empty();
        }
        if (stack.getItemMeta() instanceof SkullMeta meta && meta.getOwningPlayer() != null) {
            OfflinePlayer owner = meta.getOwningPlayer();
            return Component.object(ObjectContents.playerHead()
                    .name(Component.text(owner.getName() == null ? "?" : owner.getName()))
                    .build());
        }
        return Component.empty();
    }

    /** Utility kept for parity with the original Base64 tag format. */
    public static String decodeProfileName(String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));
            int index = json.indexOf("\"profileName\"");
            if (index < 0) {
                return null;
            }
            int start = json.indexOf('"', json.indexOf(':', index)) + 1;
            int end = json.indexOf('"', start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
