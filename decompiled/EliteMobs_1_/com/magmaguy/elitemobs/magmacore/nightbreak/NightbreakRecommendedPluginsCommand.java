package com.magmaguy.elitemobs.magmacore.nightbreak;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakRecommendedPluginsCommand extends AdvancedCommand {
   private final JavaPlugin plugin;
   private final NightbreakPluginSpec spec;

   public NightbreakRecommendedPluginsCommand(JavaPlugin plugin, NightbreakPluginSpec spec) {
      super(List.of("recommendedplugins"));
      this.plugin = plugin;
      this.spec = spec;
      this.setPermission(spec.setupPermission());
      this.setSenderType(SenderType.ANY);
      this.setDescription("Shows recommended companion plugins for " + spec.displayName() + ".");
      this.setUsage("/" + spec.rootCommand() + " recommendedplugins");
   }

   @Override
   public void execute(CommandData commandData) {
      if (commandData.getCommandSender() instanceof Player player) {
         NightbreakCatalogMenu.openRecommendations(this.plugin, player, this.spec);
      } else {
         NightbreakCatalogMenu.sendRecommendations(commandData.getCommandSender(), this.spec);
      }
   }
}
