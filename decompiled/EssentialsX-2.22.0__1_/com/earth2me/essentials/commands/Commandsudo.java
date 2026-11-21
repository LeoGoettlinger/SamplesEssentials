package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Locale;
import org.bukkit.Server;

public class Commandsudo extends EssentialsLoopCommand {
   public Commandsudo() {
      super("sudo");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         String command = getFinalArg(args, 1);
         boolean multiple = !sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.sudo.multiple");
         sender.sendTl("sudoRun", args[0], command, "");
         this.loopOnlinePlayers(server, sender, false, multiple, args[0], new String[]{command});
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) {
      if (!user.getName().equals(sender.getSender().getName())) {
         if (user.isAuthorized("essentials.sudo.exempt") && sender.isPlayer()) {
            sender.sendTl("sudoExempt", user.getName());
         } else if (args[0].toLowerCase(Locale.ENGLISH).startsWith("c:")) {
            user.getBase().chat(getFinalArg(args, 0).substring(2));
         } else {
            final String command = getFinalArg(args, 0);
            if (command.length() > 0) {
               class SudoCommandTask implements Runnable {
                  @Override
                  public void run() {
                     try {
                        user.getBase().chat("/" + command);
                     } catch (Exception var2) {
                        sender.sendTl("errorCallingCommand", command);
                     }
                  }
               }

               this.ess.scheduleSyncDelayedTask(new SudoCommandTask());
            }
         }
      }
   }
}
