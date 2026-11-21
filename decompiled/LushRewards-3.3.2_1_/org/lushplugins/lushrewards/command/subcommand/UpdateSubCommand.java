package org.lushplugins.lushrewards.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.utils.Updater;

public class UpdateSubCommand extends SubCommand {
   public UpdateSubCommand() {
      super("update");
      this.addRequiredPermission("lushrewards.update");
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      Updater updater = LushRewards.getInstance().getUpdater();
      if (!updater.isAlreadyDownloaded() && updater.isUpdateAvailable()) {
         updater.downloadUpdate().thenAccept(success -> {
            if (success) {
               ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully updated LushRewards, restart the server to apply changes!");
            } else {
               ChatColorHandler.sendMessage(sender, "&#ff6969Failed to update LushRewards!");
            }
         });
         return true;
      } else {
         ChatColorHandler.sendMessage(sender, "&#ff6969It looks like there is no new update available!");
         return true;
      }
   }
}
