package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends AdvancedCommand {
   public ReloadCommand() {
      super(List.of("reload"));
      this.setUsage("/em reload");
      this.setPermission("elitemobs.reload");
      this.setDescription("Reloads EliteMobs.");
   }

   public static void reload(CommandSender commandSender) {
      Logger.sendMessage(commandSender, CommandMessagesConfig.getReloadStartMessage());
      MetadataHandler.pendingReloadSender = commandSender;
      AdvancedCombatRuntime.prepareForSoftReload();
      MetadataHandler.PLUGIN.onDisable();
      MetadataHandler.PLUGIN.onLoad();
      MetadataHandler.PLUGIN.onEnable();
   }

   @Override
   public void execute(CommandData commandData) {
      reload(commandData.getCommandSender());
   }
}
