package com.earth2me.essentials.commands;

import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class Commandtpdeny extends EssentialsCommand {
   public Commandtpdeny() {
      super("tpdeny");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      boolean denyAll;
      if (args.length > 0) {
         denyAll = args[0].equals("*") || args[0].equalsIgnoreCase("all");
      } else {
         denyAll = false;
      }

      if (!user.hasPendingTpaRequests(false, false)) {
         throw new TranslatableException("noPendingRequest");
      } else {
         IUser.TpaRequest denyRequest;
         if (args.length > 0) {
            if (denyAll) {
               this.denyAllRequests(user);
               return;
            }

            denyRequest = user.getOutstandingTpaRequest(this.getPlayer(server, user, args, 0).getName(), false);
         } else {
            denyRequest = user.getNextTpaRequest(false, true, false);
         }

         if (denyRequest == null) {
            throw new TranslatableException("noPendingRequest");
         } else {
            User player = this.ess.getUser(denyRequest.getRequesterUuid());
            if (player == null || !player.getBase().isOnline()) {
               throw new TranslatableException("noPendingRequest");
            } else if (!this.sendEvent(user, player, denyRequest)) {
               user.sendTl("requestDenied");
               player.sendTl("requestDeniedFrom", user.getDisplayName());
               user.removeTpaRequest(denyRequest.getName());
            }
         }
      }
   }

   private void denyAllRequests(User user) {
      int count = 0;

      IUser.TpaRequest request;
      while ((request = user.getNextTpaRequest(false, true, false)) != null) {
         User player = this.ess.getUser(request.getRequesterUuid());
         if (!this.sendEvent(user, player, request)) {
            if (player != null && player.getBase().isOnline()) {
               player.sendTl("requestDeniedFrom", user.getDisplayName());
            }

            user.removeTpaRequest(request.getName());
            count++;
         }
      }

      user.sendTl("requestDeniedAll", count);
   }

   private boolean sendEvent(User user, User player, IUser.TpaRequest request) {
      TeleportRequestResponseEvent event = new TeleportRequestResponseEvent(user, player, request, false);
      Bukkit.getPluginManager().callEvent(event);
      boolean cancelled = event.isCancelled();
      if (cancelled && this.ess.getSettings().isDebug()) {
         this.ess.getLogger().info("TPA deny cancelled by API for " + user.getName() + " (requested by " + player.getName() + ")");
      }

      return event.isCancelled();
   }

   @Override
   protected List<String> getTabCompleteOptions(Server server, User user, String commandLabel, String[] args) {
      if (args.length == 1) {
         List<String> options = new ArrayList<>(user.getPendingTpaKeys());
         options.add("*");
         return options;
      } else {
         return Collections.emptyList();
      }
   }
}
