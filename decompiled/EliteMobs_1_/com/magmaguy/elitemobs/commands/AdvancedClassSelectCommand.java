package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.classes.ClassFormDefinition;
import com.magmaguy.elitemobs.advancedcombat.progression.SelectionResult;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;
import org.bukkit.entity.Player;

public final class AdvancedClassSelectCommand extends AdvancedCommand {
   public AdvancedClassSelectCommand() {
      super(List.of("class"));
      this.addLiteral("select");
      this.addArgument(
         "class",
         new DynamicListStringCommandArgument(() -> AdvancedClassCommandSupport.catalog().forms().stream().map(ClassFormDefinition::id).toList(), "<class>")
      );
      this.setUsage("/em class select <class>");
      this.setDescription("Selects an unlocked [Alpha] Advanced Combat System class form.");
      this.setPermission("elitemobs.command");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      Player player = commandData.getPlayerSender();
      if (AdvancedClassCommandSupport.requireModule(player)) {
         String formId = commandData.getStringArgument("class");
         SelectionResult result = AdvancedCombatModule.get().selectForm(player, formId);
         AdvancedClassCommandSupport.reportSelection(player, formId, result);
      }
   }
}
