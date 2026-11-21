package com.magmaguy.elitemobs.magmacore.command;

import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.elitemobs.magmacore.nightbreak.NightbreakCatalogMenu;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;

public class NightbreakLoginCommand extends AdvancedCommand {
   private final JavaPlugin plugin;

   public NightbreakLoginCommand(JavaPlugin plugin) {
      super(new ArrayList<>());
      this.setUsage("/nightbreaklogin <token>");
      this.setDescription("Register your account token");
      this.setSenderType(SenderType.ANY);
      this.setPermission("nightbreak.login");
      this.plugin = plugin;
      this.addArgument("token", new ListStringCommandArgument("<token>"));
   }

   @Override
   public void execute(CommandData commandData) {
      if (!commandData.getCommandSender().hasPermission("nightbreak.login")) {
         Logger.sendMessage(commandData.getCommandSender(), "&cYou don't have permission to use this command.");
      } else {
         String[] args = commandData.getArgs();
         if (args.length >= 1 && args[0] != null && !args[0].isEmpty()) {
            String token = args[0].trim();
            if (!token.startsWith("nbk_")) {
               Logger.sendMessage(commandData.getCommandSender(), "&eWarning: Token doesn't appear to be an account token (should start with 'nbk_').");
               Logger.sendMessage(commandData.getCommandSender(), "&eProceeding anyway...");
            }

            NightbreakAccount account = NightbreakAccount.registerToken(this.plugin, token);
            if (account != null) {
               NightbreakCatalogMenu.sendLoginSuccess(commandData.getCommandSender(), this.plugin);
            } else {
               Logger.sendMessage(commandData.getCommandSender(), "&cFailed to save account token. Check console for errors.");
            }
         } else {
            Logger.sendMessage(commandData.getCommandSender(), "&cUsage: /nightbreaklogin <token>");
            Logger.sendMessage(commandData.getCommandSender(), "&7Get your token at: &9https://nightbreak.io/account");
         }
      }
   }
}
