package org.lushplugins.lushrewards.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.command.subcommand.AboutSubCommand;
import org.lushplugins.lushrewards.command.subcommand.ClaimSubCommand;
import org.lushplugins.lushrewards.command.subcommand.EditUserSubCommand;
import org.lushplugins.lushrewards.command.subcommand.GuiSubCommand;
import org.lushplugins.lushrewards.command.subcommand.ImportSubCommand;
import org.lushplugins.lushrewards.command.subcommand.MessagesSubCommand;
import org.lushplugins.lushrewards.command.subcommand.ReloadSubCommand;
import org.lushplugins.lushrewards.command.subcommand.SetPlaytimeSubCommand;
import org.lushplugins.lushrewards.command.subcommand.UpdateSubCommand;
import org.lushplugins.lushrewards.command.subcommand.VersionSubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.command.Command;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.module.dailyrewards.DailyRewardsGui;
import org.lushplugins.lushrewards.module.dailyrewards.DailyRewardsModule;

public class RewardsCommand extends Command {
   public RewardsCommand() {
      super("rewards");
      this.addSubCommand(new AboutSubCommand());
      this.addSubCommand(new ClaimSubCommand());
      this.addSubCommand(new GuiSubCommand());
      this.addSubCommand(new EditUserSubCommand());
      this.addSubCommand(new ImportSubCommand());
      this.addSubCommand(new MessagesSubCommand());
      this.addSubCommand(new ReloadSubCommand());
      this.addSubCommand(new SetPlaytimeSubCommand());
      this.addSubCommand(new UpdateSubCommand());
      this.addSubCommand(new VersionSubCommand());
   }

   @Override
   public boolean execute(
      @NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs
   ) {
      LushRewards.getInstance().getConfigManager().checkRefresh();
      if (sender instanceof Player player) {
         if (!player.hasPermission("lushrewards.use")) {
            ChatColorHandler.sendMessage(sender, LushRewards.getInstance().getConfigManager().getMessage("no-permissions"));
            return true;
         } else {
            LushRewards.getInstance()
               .getModules()
               .stream()
               .filter(module -> module instanceof DailyRewardsModule && player.hasPermission("lushrewards.use." + module.getId()))
               .findFirst()
               .ifPresentOrElse(
                  module -> new DailyRewardsGui((DailyRewardsModule)module, player).open(),
                  () -> ChatColorHandler.sendMessage(player, "&#ff6969The daily rewards module is disabled or you don't have permission")
               );
            return true;
         }
      } else {
         ChatColorHandler.sendMessage(sender, "Console cannot run this command!");
         return true;
      }
   }
}
