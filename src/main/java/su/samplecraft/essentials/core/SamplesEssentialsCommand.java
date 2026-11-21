package su.samplecraft.essentials.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;

/** Root admin command: /samplese [status|modules|reload|toggle|version]. */
public final class SamplesEssentialsCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        return SamplesEssentials.getInstance().getModules().handleCommand(sender, command, label, args);
    }

    @Override
    public java.util.List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, String @NotNull [] args) {
        SamplesEssentials plugin = SamplesEssentials.getInstance();
        if (args.length == 1) {
            return filter(java.util.List.of("status", "modules", "reload", "toggle", "version"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return filter(new java.util.ArrayList<>(plugin.getModules().all().keySet()), args[1]);
        }
        return java.util.List.of();
    }

    private java.util.List<String> filter(java.util.List<String> values, String prefix) {
        String p = prefix.toLowerCase(java.util.Locale.ROOT);
        java.util.List<String> out = new java.util.ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(java.util.Locale.ROOT).startsWith(p)) {
                out.add(value);
            }
        }
        return out;
    }
}
