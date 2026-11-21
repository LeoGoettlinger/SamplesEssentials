package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.List;
import java.util.Optional;

public class SpawnElitePowers extends AdvancedCommand {
   public SpawnElitePowers() {
      super(List.of("spawn"));
      this.addLiteral("elite");
      this.addArgument("entityType", new EliteEntityTypeCommandArgument());
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addVarargsArgument("powers", new ListStringCommandArgument(ElitePower.getElitePowers().keySet().stream().toList(), "<powers>"));
      this.setUsage("/em spawn elite <entityType> <level> <power1> <power2> <power3> <...>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Spawns an elite of the specified type and level with the specified powers.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         String powersArg = commandData.getStringSequenceArgument("powers");
         Optional<String> powers = powersArg != null && !powersArg.isBlank() ? Optional.of(powersArg.trim()) : Optional.empty();
         SpawnCommand.spawnEliteEntityTypeCommand(
            commandData.getPlayerSender(), EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("entityType")), parsedLevel, powers
         );
      }
   }
}
