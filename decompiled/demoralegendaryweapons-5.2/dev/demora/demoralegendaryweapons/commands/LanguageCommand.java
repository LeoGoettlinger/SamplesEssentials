package dev.demora.demoralegendaryweapons.commands;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageCommand implements CommandExecutor {
   private final DemoraLegendaryWeapons plugin;

   public LanguageCommand(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.language-only-players"));
         return true;
      } else if (!player.isOp()) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.language-no-op"));
         return true;
      } else if (args.length == 0) {
         this.plugin.getLanguageManager().showLanguageSelection(player);
         return true;
      } else {
         String language = args[0].toLowerCase();
         switch (language) {
            case "ru":
               this.plugin.getLanguageManager().setLanguage("ru");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            case "en":
               this.plugin.getLanguageManager().setLanguage("en");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            case "vn":
               this.plugin.getLanguageManager().setLanguage("vn");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            default:
               player.sendMessage(this.plugin.getMessageWithPrefix("commands.language-available"));
         }

         return true;
      }
   }
}
