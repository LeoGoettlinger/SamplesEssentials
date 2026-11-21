package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class VersionCommand extends AdvancedCommand {
   public VersionCommand() {
      super(List.of("version"));
      this.setUsage("/em version");
      this.setPermission("elitemobs.version");
      this.setDescription("Checks the server's EliteMobs plugin version.");
   }

   @Override
   public void execute(CommandData commandData) {
      Logger.sendMessage(
         commandData.getCommandSender(),
         ChatColor.WHITE + " version " + ChatColor.GREEN + Bukkit.getPluginManager().getPlugin("EliteMobs").getDescription().getVersion()
      );
   }
}
