package org.lushplugins.lushrewards.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public class VersionSubCommand extends SubCommand {
   public VersionSubCommand() {
      super("version");
      this.addRequiredPermission("lushrewards.version");
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      ChatColorHandler.sendMessage(
         sender, "&#a8e1ffYou are currently running &#58b1e0LushRewards &#a8e1ffversion &#58b1e0" + LushRewards.getInstance().getDescription().getVersion()
      );
      return true;
   }
}
