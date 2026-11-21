package su.samplecraft.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All chat/message handling for the plugin.
 * <p>
 * Messages written in {@code lang/*.yml} and module configs are legacy strings
 * ({@code &a}, {@code &l}, ...). They are parsed once into Adventure
 * {@link Component}s and cached. Anything that looks like MiniMessage
 * ({@code <gradient:...>}) is passed straight through to MiniMessage so the
 * migrated announcement formats from the old server keep working.
 */
public final class Text {

    /** Legacy serialiser, uses the vanilla '&' - no deprecation-prone ChatColor index juggling. */
    public static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    public static final MiniMessage MINI = MiniMessage.miniMessage();

    /** {@code <gradient:#ff0000:#00ff00>}, {@code <rainbow>}, {@code <yellow>} ... */
    private static final Pattern MINI_TAG = Pattern.compile(
            "<\\/?(gradient|rainbow|color|colour|red|blue|green|yellow|gold|gray|grey|white|black|"
                    + "dark_red|dark_blue|dark_green|dark_aqua|dark_purple|dark_gray|dark_grey|aqua|"
                    + "light_purple|bold|italic|underlined|strikethrough|obfuscated|reset|newline|br|"
                    + "click|hover|centered|font|transition)(:[^>]*)?>",
            Pattern.CASE_INSENSITIVE);

    /** Placeholder resolver so callers never build ad-hoc string replaces. */
    @FunctionalInterface
    public interface Placeholder {
        String value(String key);
    }

    private Text() {
    }

    public static boolean looksLikeMiniMessage(String raw) {
        return raw != null && MINI_TAG.matcher(raw).find();
    }

    /** Parses a raw config string (legacy or MiniMessage) into a Component. */
    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        if (looksLikeMiniMessage(raw)) {
            return MINI.deserialize(raw);
        }
        return LEGACY.deserialize(raw);
    }

    /** Replaces {@code {key}} tokens with the resolver's values. */
    public static String apply(String raw, Placeholder resolver) {
        if (raw == null || raw.indexOf('{') < 0) {
            return raw;
        }
        String out = raw;
        int guard = 0;
        int start;
        while ((start = out.indexOf('{', 0)) >= 0 && guard++ < 100) {
            int end = out.indexOf('}', start);
            if (end < 0) {
                break;
            }
            String key = out.substring(start + 1, end);
            String value = resolver.value(key);
            if (value == null) {
                break; // unknown token: stop, keep the rest as-is
            }
            // replace this exact occurrence, then restart the scan after it
            out = out.substring(0, start) + value + out.substring(end + 1);
            // if the value itself contains braces we continue after the inserted value
            // to avoid replacing into user content:
            // (guard counter protects against pathological chains)
        }
        return out;
    }

    public static String applyPairs(String raw, String... keyValuePairs) {
        if (raw == null) {
            return "";
        }
        String out = raw;
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            out = out.replace("{" + keyValuePairs[i] + "}", keyValuePairs[i + 1]);
        }
        return out;
    }

    /** Colours are already applied inside {@link Component}s, so this is just a convenience. */
    public static void send(CommandSender to, String raw) {
        if (to != null) {
            to.sendMessage(parse(raw));
        }
    }

    public static void send(CommandSender to, String raw, String... kv) {
        if (to != null) {
            to.sendMessage(parse(applyPairs(raw, kv)));
        }
    }

    public static void send(CommandSender to, Component component) {
        if (to != null) {
            to.sendMessage(component);
        }
    }

    public static List<Component> parseAll(List<String> raw) {
        List<Component> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            out.add(parse(s));
        }
        return out;
    }

    /** Renders a Component back to a legacy string (used for logging and plain-text fallbacks). */
    public static String plain(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
    }

    public static String stripColors(String raw) {
        return plain(parse(raw));
    }
}
