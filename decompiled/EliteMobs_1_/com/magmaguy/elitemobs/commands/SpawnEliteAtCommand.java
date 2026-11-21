package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.WorldCommandArgument;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import java.util.List;
import java.util.Optional;
import org.bukkit.util.Vector;

public class SpawnEliteAtCommand extends AdvancedCommand {
   public SpawnEliteAtCommand() {
      super(List.of("spawn"));
      this.addLiteral("eliteAt");
      this.addArgument("world", new WorldCommandArgument("<world>"));
      this.addArgument("x", new DoubleCommandArgument("<x>"));
      this.addArgument("y", new DoubleCommandArgument("<y>"));
      this.addArgument("z", new DoubleCommandArgument("<z>"));
      this.addArgument("entityType", new EliteEntityTypeCommandArgument());
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addVarargsArgument("powers", new ListStringCommandArgument(ElitePower.getElitePowers().keySet().stream().toList(), "<powers>"));
      this.setUsage("/em spawn eliteAt <world> <x> <y> <z> <entityType> <level> <power1> <power2> <power3> <...>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Spawns an elite of the specified type and level, optionally with specified powers, at the chosen location.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedX = commandData.getDoubleArgument("x");
      Double parsedY = commandData.getDoubleArgument("y");
      Double parsedZ = commandData.getDoubleArgument("z");
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedX != null && parsedY != null && parsedZ != null && parsedLevel != null) {
         String powersArg = commandData.getStringSequenceArgument("powers");
         Optional<String> powers = powersArg != null && !powersArg.isBlank() ? Optional.of(powersArg.trim()) : Optional.empty();
         SpawnCommand.spawnEliteEntityTypeCommand(
            commandData.getPlayerSender(),
            EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("entityType")),
            commandData.getStringArgument("world"),
            new Vector(parsedX, parsedY, parsedZ),
            parsedLevel,
            powers
         );
      }
   }
}
