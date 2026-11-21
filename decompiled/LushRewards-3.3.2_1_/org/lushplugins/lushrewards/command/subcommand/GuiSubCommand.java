package org.lushplugins.lushrewards.command.subcommand;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.LushRewards;
import org.lushplugins.lushrewards.gui.GuiDisplayer;
import org.lushplugins.lushrewards.libraries.lushlib.command.SubCommand;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushrewards.libraries.lushlib.module.Module;

public class GuiSubCommand extends SubCommand {
   public GuiSubCommand() {
      super("gui");
      this.addRequiredPermission("lushrewards.use");
   }

   @Override
   public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
      if (sender instanceof Player player) {
         if (args.length < 1) {
            ChatColorHandler.sendMessage(
               sender,
               LushRewards.getInstance()
                  .getConfigManager()
                  .getMessage("incorrect-usage", "&#ff6969Incorrect usage try &#d13636%command-usage%")
                  .replace("%command-usage%", "/rewards gui <module>")
            );
         } else {
            String moduleId = args[0];
            if (!player.hasPermission("lushrewards.use." + moduleId)) {
               ChatColorHandler.sendMessage(
                  sender, LushRewards.getInstance().getConfigManager().getMessage("no-permissions", "&#ff6969Insufficient permissions")
               );
               return true;
            }

            LushRewards.getInstance().getModule(moduleId).ifPresent(module -> {
               if (module instanceof GuiDisplayer guiDisplayer) {
                  guiDisplayer.displayGui(player);
               }
            });
         }

         return true;
      } else {
         sender.sendMessage("This command must be ran by a player");
         return true;
      }
   }

   @Nullable
   @Override
   public List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs
   ) {
      return LushRewards.getInstance().getEnabledRewardModules().stream().map(Module::getId).toList();
   }
}
