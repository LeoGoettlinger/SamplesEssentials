package su.samplecraft.essentials.modules.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.List;

/**
 * Merged from {@code ChatClear.jar} (and the /lpc clear command).
 * Clears the chat for everyone with an optional reason.
 */
public final class ChatClearModule extends EssentialsModule {

    public ChatClearModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("chat", "chatclear", "ChatClear.jar", "Clears the server chat with a reason");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.chat.clear", "Clear the server chat", org.bukkit.permissions.PermissionDefault.OP);
        command("clearchat", new Handler());
        command("chatclear", new Handler());
        onDisableHook(this::unbindCommands);
    }

    private final class Handler implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!sender.hasPermission("samplese.chat.clear")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String reason = args.length > 0 ? String.join(" ", args) : cfgString("default-reason", "");
            Player clearer = sender instanceof Player p ? p : null;

            int blankLines = Math.max(1, cfgInt("blank-lines", 100));
            for (int i = 0; i < blankLines; i++) {
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    online.sendMessage(net.kyori.adventure.text.Component.empty());
                }
            }

            net.kyori.adventure.text.Component notice = msgComponent("cleared",
                    "player", sender.getName(),
                    "reason", reason.isEmpty() ? "" : reason);
            for (Player online : plugin.getServer().getOnlinePlayers()) {
                online.sendMessage(notice);
            }
            plugin.getServer().getConsoleSender().sendMessage(notice);
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            return List.of();
        }
    }
}
