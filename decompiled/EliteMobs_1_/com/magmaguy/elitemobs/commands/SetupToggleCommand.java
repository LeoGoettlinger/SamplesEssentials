package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;

public class SetupToggleCommand extends AdvancedCommand {
   public SetupToggleCommand() {
      super(List.of("setup"));
      this.addLiteral("toggle");
      this.addArgument(
         "empackages",
         new DynamicListStringCommandArgument(
            () -> EMPackage.getEmPackages()
               .values()
               .stream()
               .map(emPackage -> emPackage.getContentPackagesConfigFields().getFilename())
               .collect(Collectors.toUnmodifiableList()),
            "empackages"
         )
      );
      this.setUsage("/em setup toggle <dungeonConfig>");
      this.setPermission("elitemobs.setup");
      this.setDescription("Allows you to toggle the installation of specified EliteMobs content.");
   }

   @Override
   public void execute(CommandData commandData) {
      String dungeon = commandData.getStringArgument("empackages");
      if (!dungeon.isEmpty() && EMPackage.getEmPackages().get(dungeon) != null) {
         EMPackage emPackage = EMPackage.getEmPackages().get(dungeon);
         Player player = commandData.getCommandSender() instanceof Player playerSender ? playerSender : null;
         emPackage.setupMenuToggle(player);
         if (emPackage.isInstalled()) {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupInstalledMessage());
         } else {
            Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupUninstalledMessage());
         }
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupNotValidPackageMessage());
      }
   }
}
