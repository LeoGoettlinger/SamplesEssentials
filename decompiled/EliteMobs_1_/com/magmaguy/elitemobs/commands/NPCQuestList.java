package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.quests.QuestInteractionHandler;
import java.util.List;
import java.util.UUID;

public class NPCQuestList extends AdvancedCommand {
   public NPCQuestList() {
      super(List.of("npc"));
      this.addLiteral("questList");
      this.addArgument("npcUUID", new ListStringCommandArgument("npcUUID"));
      this.setSenderType(SenderType.PLAYER);
      this.setUsage("Not to be run manually!");
      this.setDescription("Internal command.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestInteractionHandler.processNPCQuests(
         commandData.getPlayerSender(), EntityTracker.getNpcEntities().get(UUID.fromString(commandData.getStringArgument("npcUUID")))
      );
   }
}
