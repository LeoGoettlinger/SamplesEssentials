package su.samplecraft.essentials.modules.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Merged from {@code announcement-1.4.jar}
 * (me.usainsrht.announcement.Announcement).
 * <p>
 * Rotating broadcasts with the original template/announcements layout. The
 * old config was MiniMessage based (including {@code <centered>} and
 * clickable vote links), so messages are parsed with MiniMessage - the
 * {@code <centered>} tag is emulated by padding the line to chat width.
 */
public final class AnnouncementModule extends EssentialsModule {

    private int index;
    private String templateHeader = "";
    private String templateFooter = "";

    public AnnouncementModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("chat", "announcements", "announcement-1.4.jar", "Rotating MiniMessage announcements with template");
    }

    @Override
    protected void onEnable() {
        buildTemplate();
        long delaySeconds = Math.max(5, cfgInt("delay", 300));
        long period = delaySeconds * 20L;
        syncRepeat(period, period, this::broadcastNext);
        command("announcements", this::onCommand);
        onDisableHook(this::unbindCommands);
    }

    private void buildTemplate() {
        List<String> template = cfgStringList("template");
        int first = -1;
        int last = -1;
        for (int i = 0; i < template.size(); i++) {
            if (template.get(i).contains("<announcement>")) {
                if (first < 0) {
                    first = i;
                }
                last = i;
            }
        }
        if (first < 0) {
            return;
        }
        StringBuilder top = new StringBuilder();
        for (int i = 0; i < first; i++) {
            if (i > 0) {
                top.append("<newline>");
            }
            top.append(template.get(i));
        }
        StringBuilder footer = new StringBuilder();
        for (int i = first; i <= last; i++) {
            if (i > first) {
                footer.append("<newline>");
            }
            footer.append(template.get(i).replace("<announcement>", ""));
        }
        for (int i = last + 1; i < template.size(); i++) {
            footer.append("<newline>").append(template.get(i));
        }
        templateHeader = top.toString();
        templateFooter = footer.toString();
    }

    private void broadcastNext() {
        List<String> announcements = cfgStringList("announcements");
        if (announcements.isEmpty()) {
            return;
        }
        String messageRaw;
        if (cfgBool("ordered", true)) {
            messageRaw = announcements.get(Math.floorMod(index, announcements.size()));
            index++;
        } else {
            messageRaw = announcements.get(ThreadLocalRandom.current().nextInt(announcements.size()));
        }
        String body = center(messageRaw, cfgInt("center-width", 52));
        String full = templateHeader + body + templateFooter;
        net.kyori.adventure.text.Component component = Text.parse(full);
        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(component);
        }
        if (cfgBool("broadcast", true)) {
            plugin.getServer().getConsoleSender().sendMessage(component);
        }
    }

    /**
     * Adventure 5 has no <centered> tag; the old announcements used it, so the
     * line is padded to the configured chat width instead (colours stripped
     * for measuring, the original string is sent untouched).
     */
    private String center(String raw, int width) {
        if (!cfgBool("center-lines", true)) {
            return raw;
        }
        String plain = Text.stripColors(raw);
        int pad = Math.max(0, (width - plain.length()) / 2);
        return " ".repeat(pad) + raw;
    }

    private boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("samplese.chat.announce")) {
            sender.sendMessage(plugin.getLang().component("no-permission"));
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("send")) {
            List<String> list = cfgStringList("announcements");
            int i = 0;
            if (args.length > 1) {
                try {
                    i = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    i = -1;
                }
            }
            if (i >= 0 && i < list.size()) {
                net.kyori.adventure.text.Component c = Text.parse(templateHeader + list.get(i) + templateFooter);
                for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(c);
                }
                sender.sendMessage(msgComponent("sent", "index", String.valueOf(i)));
            } else {
                sender.sendMessage(plugin.getLang().component("usage"));
            }
            return true;
        }
        sender.sendMessage(msgComponent("header", "count", String.valueOf(cfgStringList("announcements").size())));
        List<String> list = cfgStringList("announcements");
        for (int i = 0; i < list.size(); i++) {
            sender.sendMessage(Text.MINI.deserialize("<gray>" + i + "<dark_gray>: <white>" + list.get(i)));
        }
        return true;
    }
}
