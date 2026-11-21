package net.ess3.provider;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.FormattedCommandAlias;

public interface FormattedCommandAliasProvider extends Provider {
   List<String> createCommands(FormattedCommandAlias command, CommandSender sender, String[] args);

   String[] getFormatStrings(FormattedCommandAlias command);

   String buildCommand(FormattedCommandAlias command, CommandSender sender, String formatString, String[] args);
}
