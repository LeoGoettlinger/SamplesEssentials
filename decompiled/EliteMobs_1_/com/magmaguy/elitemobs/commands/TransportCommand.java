package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.transport.TransportModule;
import java.util.List;

public final class TransportCommand extends AdvancedCommand {
   private final String action;
   private final boolean argument;

   public TransportCommand(String action) {
      super(List.of("transport"));
      this.action = action;
      this.addLiteral(action);
      this.argument = List.of("create", "edit", "start", "move").contains(action);
      if (this.argument) {
         this.addArgument("value", new ListStringCommandArgument(action.equals("move") ? "<waypoint number>" : "<route id>") {
            @Override
            public boolean matchesInput(String input) {
               return input != null && !input.isBlank();
            }
         });
      }

      this.setPermission("elitemobs.transport.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setUsage("/em transport " + action + (this.argument ? " <value>" : ""));
      this.setDescription("Authors, previews and tests curved transport routes.");
   }

   @Override
   public void execute(CommandData data) {
      TransportModule module = TransportModule.get();
      if (module == null) {
         data.getPlayerSender().sendMessage("Transport is unavailable.");
      } else {
         module.editor().execute(data.getPlayerSender(), this.action, this.argument ? data.getStringArgument("value") : null);
      }
   }
}
