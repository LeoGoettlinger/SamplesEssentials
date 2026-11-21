package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakCatalogMenu;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakCommand extends AdvancedCommand {
   private final JavaPlugin plugin;

   public NightbreakCommand(JavaPlugin plugin) {
      super(new ArrayList<>());
      this.plugin = plugin;
      this.setUsage("/nightbreak plugins");
      this.setDescription("Shows the available plugin catalog.");
      this.setSenderType(SenderType.ANY);
      this.setPermission("nightbreak.plugins");
   }

   @Override
   public void execute(CommandData commandData) {
      String[] args = commandData.getArgs();
      if (args.length != 0 && "plugins".equalsIgnoreCase(args[0])) {
         if (commandData.getCommandSender() instanceof Player player) {
            NightbreakCatalogMenu.openGlobalCatalog(this.plugin, player);
         } else {
            NightbreakCatalogMenu.sendGlobalCatalog(commandData.getCommandSender());
         }
      } else {
         Logger.sendMessage(commandData.getCommandSender(), "&eUsage: /nightbreak plugins");
      }
   }

   @Override
   public List<String> onTabComplete(String[] args) {
      return args.length == 1 && "plugins".startsWith(args[0].toLowerCase()) ? List.of("plugins") : List.of();
   }
}
