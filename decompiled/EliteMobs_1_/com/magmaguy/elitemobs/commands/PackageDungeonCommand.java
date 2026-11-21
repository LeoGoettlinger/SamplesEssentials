package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class PackageDungeonCommand extends AdvancedCommand {
   public PackageDungeonCommand() {
      super(List.of("package"));
      this.addArgument("dungeonName", new ListStringCommandArgument("<dungeonName>"));
      this.addArgument("version", new IntegerCommandArgument("<version>"));
      this.setUsage("/em package <dungeonName> <version>");
      this.setPermission("elitemobs.package");
      this.setDescription("Packages an EliteMobs dungeon for distribution.");
   }

   @Override
   public void execute(CommandData commandData) {
      new PackageCommand(commandData.getCommandSender(), commandData.getStringArgument("dungeonName"), commandData.getStringArgument("version"));
   }
}
