package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakDownloadPluginUpdateCommand extends AdvancedCommand {
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec pluginSpec;

   public NightbreakDownloadPluginUpdateCommand(JavaPlugin plugin, NightbreakPluginSpec pluginSpec) {
      super(List.of("downloadpluginupdate"));
      this.plugin = plugin;
      this.pluginSpec = pluginSpec;
      this.setPermission(pluginSpec.adminPermission());
      this.setSenderType(SenderType.ANY);
      this.setDescription("Downloads a " + pluginSpec.displayName() + " plugin update.");
      this.setUsage("/" + pluginSpec.rootCommand() + " downloadpluginupdate");
   }

   @Override
   public void execute(CommandData commandData) {
      NightbreakPluginUpdater.downloadPluginUpdateAsync(this.plugin, this.pluginSpec, commandData.getCommandSender(), null);
   }
}
