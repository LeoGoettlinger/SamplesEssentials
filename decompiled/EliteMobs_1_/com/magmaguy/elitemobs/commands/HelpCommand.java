package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.CommandManager;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.entity.Player;

public class HelpCommand extends AdvancedCommand {
   public HelpCommand() {
      super(List.of("help"));
      this.setUsage("/em help");
      this.setPermission("elitemobs.help");
      this.setDescription("Lists all commands.");
   }

   @Override
   public void execute(CommandData commandData) {
      Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getHelpHeaderMessage());

      for (CommandManager commandManager : CommandManager.getCommandManagers()) {
         commandManager.commands.forEach(command -> {
            if (commandData.getCommandSender() instanceof Player player) {
               player.spigot().sendMessage(Logger.hoverMessage(command.getUsage(), command.getDescription()));
            } else {
               Logger.sendSimpleMessage(commandData.getCommandSender(), command.getUsage());
               Logger.sendSimpleMessage(commandData.getCommandSender(), command.getDescription());
            }
         });
      }
   }
}
