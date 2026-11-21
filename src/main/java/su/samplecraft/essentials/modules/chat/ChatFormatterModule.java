package su.samplecraft.essentials.modules.chat;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merged from {@code LPC - Chat Formatter [1.7.10 - 1.21.11].jar}
 * (me.wikmor.lpc.LPC).
 * <p>
 * The original read LuckPerms prefix/suffix metadata and rendered
 * {@code {prefix}{name}{suffix} &r&8»&r {message}}. On modern Paper the same
 * result is produced with {@link AsyncChatEvent} + {@link ChatRenderer}, using
 * the player's Bukkit display name (which LuckPerms/TAB keep up to date) as
 * prefix/suffix source. Colour permissions and a per-group format table are
 * supported, exactly like the old config.
 */
public final class ChatFormatterModule extends EssentialsModule implements Listener {

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final Map<UUID, Long> lastMessage = new ConcurrentHashMap<>();

    public ChatFormatterModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("chat", "chatformatter", "LPC - Chat Formatter.jar", "LuckPerms-style chat format with prefix/suffix, groups and colours");
    }

    @Override
    protected void onEnable() {
        command("chatfmt", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.admin")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage(plugin.getLang().component("reloaded"));
            }
            return true;
        });
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String format = resolveFormat(player);
        if (format == null) {
            return; // formatting disabled -> vanilla chat
        }

        Component message = event.message();
        if (!player.hasPermission("samplese.chat.format")) {
            // strip MiniMessage/legacy so normal players cannot inject colours
            message = Component.text(PLAIN.serialize(message));
        }
        Component finalMessage = message;

        String display = cfgBool("use-displayname", true) && player.displayName() != null
                ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(player.displayName())
                : player.getName();

        String rendered = Text.applyPairs(format,
                "prefix", playerPrefix(player),
                "suffix", playerSuffix(player),
                "name", display,
                "displayname", display,
                "username", player.getName(),
                "world", player.getWorld().getName());

        // {message} stays as a live placeholder so the raw text is inserted as a component
        int split = rendered.indexOf("{message}");
        final Component head = split >= 0 ? Text.parse(rendered.substring(0, split)) : Text.parse(rendered);
        final Component tail = split >= 0 ? Text.parse(rendered.substring(split + "{message}".length())) : Component.empty();

        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, msg) ->
                head.append(finalMessage).append(tail)));
    }

    private String resolveFormat(Player player) {
        if (!cfgBool("enabled", true)) {
            return null;
        }
        String group = primaryGroup(player);
        org.bukkit.configuration.ConfigurationSection section = config().getConfigurationSection("group-formats");
        Map<String, Object> groups = section == null ? Map.of() : section.getValues(false);
        for (Map.Entry<String, Object> entry : groups.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(group) && entry.getValue() instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        String base = cfgString("chat-format", "{prefix}{name}{suffix} &r&8»&r {message}");
        return base.isBlank() ? null : base;
    }

    private String playerPrefix(Player player) {
        Component displayName = player.displayName();
        String plain = PLAIN.serialize(displayName);
        String name = player.getName();
        if (cfgBool("derive-prefix-from-displayname", true) && plain.contains(name)) {
            return plain.substring(0, plain.indexOf(name));
        }
        return cfgString("fallbacks.prefix", "");
    }

    private String playerSuffix(Player player) {
        Component displayName = player.displayName();
        String plain = PLAIN.serialize(displayName);
        String name = player.getName();
        if (cfgBool("derive-prefix-from-displayname", true) && plain.contains(name)) {
            return plain.substring(plain.indexOf(name) + name.length());
        }
        return cfgString("fallbacks.suffix", "");
    }

    private String primaryGroup(Player player) {
        // Group names are matched against the permission node "samplese.group.<name>",
        // which admins can grant per rank (works with LuckPerms/TAB untouched).
        for (org.bukkit.permissions.PermissionAttachmentInfo info : player.getEffectivePermissions()) {
            String node = info.getPermission().toLowerCase(java.util.Locale.ROOT);
            if (info.getValue() && node.startsWith("samplese.group.")) {
                return node.substring("samplese.group.".length());
            }
        }
        return "default";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        lastMessage.remove(event.getPlayer().getUniqueId());
    }
}
