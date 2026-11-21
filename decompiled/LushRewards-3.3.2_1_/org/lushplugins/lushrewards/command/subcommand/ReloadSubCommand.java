package org.lushplugins.lushrewards.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;

public class ReloadSubCommand extends SubCommand {
   public ReloadSubCommand() {
      super("reload");
      this.addRequiredPermission("lushrewards.reload");
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      LushRewards.getInstance().getConfigManager().reloadConfig();
      ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("reload"));
      return true;
   }
}
