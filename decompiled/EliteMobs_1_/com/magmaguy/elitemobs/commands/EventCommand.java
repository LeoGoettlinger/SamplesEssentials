package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.events.TimedEvent;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;

public class EventCommand extends AdvancedCommand {
   public EventCommand() {
      super(List.of("event"));
      this.addArgument(
         "filename",
         new DynamicListStringCommandArgument(
            () -> TimedEvent.getBlueprintEvents().stream().map(timedEvent -> timedEvent.getCustomEventsConfigFields().getFilename()).toList(), "filename"
         )
      );
      this.setUsage("/em event <event filename>");
      this.setPermission("elitemobs.event.start");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Triggers a timed event to start at the next available time and place.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.admin.EventCommand.trigger(commandData.getCommandSender(), commandData.getStringArgument("filename"));
   }
}
