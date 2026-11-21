package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandtpacancel extends EssentialsCommand {
   public Commandtpacancel() {
      super("tpacancel");
   }

   public static boolean cancelTeleportRequest(final User user, final User requester) {
      return user.removeTpaRequest(requester.getName()) != null;
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         int cancellations = 0;

         for (User onlineUser : this.ess.getOnlineUsers()) {
            if (onlineUser != user && cancelTeleportRequest(onlineUser, user)) {
               cancellations++;
            }
         }

         if (cancellations <= 0) {
            throw new TranslatableException("noPendingRequest");
         }

         user.sendTl("teleportRequestAllCancelled", cancellations);
      } else {
         User targetPlayer = this.getPlayer(server, user, args, 0);
         if (cancelTeleportRequest(targetPlayer, user)) {
            user.sendTl("teleportRequestSpecificCancelled", targetPlayer.getName());
         }
      }
   }
}
