package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolEditor;
import java.util.List;
import java.util.Locale;

public final class PatrolCommand extends AdvancedCommand {
   private final String action;

   public PatrolCommand(String action) {
      super(List.of("patrol"));
      this.action = action.toLowerCase(Locale.ROOT);
      this.addLiteral(this.action);
      if (this.action.equals("mode")) {
         this.addArgument("mode", new ListStringCommandArgument(List.of("LOOP", "REVERSE"), "<LOOP|REVERSE>"));
      }

      this.setUsage("/em patrol " + this.action + (this.action.equals("mode") ? " <LOOP|REVERSE>" : ""));
      this.setPermission("elitemobs.patrol.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Authors and inspects native mob patrol routes.");
   }

   @Override
   public void execute(CommandData commandData) {
      String var2 = this.action;
      switch (var2) {
         case "edit":
            PatrolEditor.edit(commandData.getPlayerSender());
            break;
         case "add":
            PatrolEditor.add(commandData.getPlayerSender());
            break;
         case "remove":
            PatrolEditor.remove(commandData.getPlayerSender());
            break;
         case "undo":
            PatrolEditor.undo(commandData.getPlayerSender());
            break;
         case "mode":
            PatrolEditor.mode(commandData.getPlayerSender(), commandData.getStringArgument("mode"));
            break;
         case "save":
            PatrolEditor.save(commandData.getPlayerSender());
            break;
         case "cancel":
            PatrolEditor.cancel(commandData.getPlayerSender());
            break;
         case "status":
            PatrolEditor.status(commandData.getPlayerSender());
            break;
         default:
            throw new IllegalStateException("Unknown patrol action " + this.action);
      }
   }
}
