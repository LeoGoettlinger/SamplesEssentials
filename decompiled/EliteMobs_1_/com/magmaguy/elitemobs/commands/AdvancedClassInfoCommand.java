package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;

public final class AdvancedClassInfoCommand extends AdvancedCommand {
   public AdvancedClassInfoCommand() {
      super(List.of("class"));
      this.addLiteral("info");
      this.addArgument(
         "class",
         new DynamicListStringCommandArgument(() -> AdvancedClassCommandSupport.catalog().forms().stream().map(ClassFormDefinition::id).toList(), "<class>")
      );
      this.setUsage("/em class info <class>");
      this.setDescription("Explains one [Alpha] Advanced Combat System class form.");
      this.setPermission("elitemobs.command");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      AdvancedClassCommandSupport.showForm(commandData.getPlayerSender(), commandData.getStringArgument("class"));
   }
}
