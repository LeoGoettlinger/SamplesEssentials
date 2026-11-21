package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import java.util.List;

public class KillTypeCommand extends AdvancedCommand {
   public KillTypeCommand() {
      super(List.of("kill"));
      this.addLiteral("type");
      this.addArgument("type", new EliteEntityTypeCommandArgument());
      this.setUsage("/em kill type <entityType>");
      this.setPermission("elitemobs.kill.command");
      this.setDescription("Kills all elites of the specified type.");
   }

   @Override
   public void execute(CommandData commandData) {
      KillHandler.killEntityType(commandData.getCommandSender(), EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("type")));
   }
}
