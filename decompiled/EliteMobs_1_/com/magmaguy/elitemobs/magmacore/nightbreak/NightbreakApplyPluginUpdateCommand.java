package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakApplyPluginUpdateCommand extends AdvancedCommand {
   private final JavaPlugin plugin;

   public NightbreakApplyPluginUpdateCommand(JavaPlugin plugin, NightbreakPluginSpec pluginSpec) {
      super(List.of("applypluginupdate"));
      this.plugin = plugin;
      this.setPermission(pluginSpec.adminPermission());
      this.setSenderType(SenderType.ANY);
      this.setDescription("Applies a downloaded " + pluginSpec.displayName() + " plugin update without a restart.");
      this.setUsage("/" + pluginSpec.rootCommand() + " applypluginupdate");
   }

   @Override
   public void execute(CommandData commandData) {
      NightbreakPluginHotSwap.applyStagedUpdate(this.plugin, commandData.getCommandSender());
   }
}
