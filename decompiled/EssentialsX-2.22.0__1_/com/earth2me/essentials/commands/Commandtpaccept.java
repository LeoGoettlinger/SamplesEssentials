package com.earth2me.essentials.commands;

import com.earth2me.essentials.AsyncTeleport;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.TeleportRequestResponseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpaccept extends EssentialsCommand {
   public Commandtpaccept() {
      super("tpaccept");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      boolean acceptAll;
      if (args.length > 0) {
         acceptAll = args[0].equals("*") || args[0].equalsIgnoreCase("all");
      } else {
         acceptAll = false;
      }

      if (!user.hasPendingTpaRequests(true, acceptAll)) {
         throw new TranslatableException("noPendingRequest");
      } else {
         if (args.length > 0) {
            if (acceptAll) {
               this.acceptAllRequests(user, commandLabel);
               throw new NoChargeException();
            }

            user.sendTl("requestAccepted");
            this.handleTeleport(user, user.getOutstandingTpaRequest(this.getPlayer(server, user, args, 0).getName(), true), commandLabel);
         } else {
            user.sendTl("requestAccepted");
            this.handleTeleport(user, user.getNextTpaRequest(true, false, false), commandLabel);
         }

         throw new NoChargeException();
      }
   }

   private void acceptAllRequests(final User user, final String commandLabel) throws Exception {
      int count = 0;

      IUser.TpaRequest request;
      while ((request = user.getNextTpaRequest(true, true, true)) != null) {
         try {
            this.handleTeleport(user, request, commandLabel);
            count++;
         } catch (Exception var9) {
            this.ess.showError(user.getSource(), var9, commandLabel);
         } finally {
            user.removeTpaRequest(request.getName());
         }
      }

      user.sendTl("requestAcceptedAll", count);
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

   private void handleTeleport(final User user, final IUser.TpaRequest request, String commandLabel) throws Exception {
      if (request == null) {
         throw new TranslatableException("noPendingRequest");
      } else {
         User requester = this.ess.getUser(request.getRequesterUuid());
         if (!requester.getBase().isOnline()) {
            user.removeTpaRequest(request.getName());
            throw new TranslatableException("noPendingRequest");
         } else if (!request.isHere()
            || (requester.isAuthorized("essentials.tpahere") || requester.isAuthorized("essentials.tpaall"))
               && (
                  user.getWorld() == requester.getWorld()
                     || !this.ess.getSettings().isWorldTeleportPermissions()
                     || user.isAuthorized("essentials.worlds." + user.getWorld().getName())
               )) {
            if (request.isHere()
               || requester.isAuthorized("essentials.tpa")
                  && (
                     user.getWorld() == requester.getWorld()
                        || !this.ess.getSettings().isWorldTeleportPermissions()
                        || user.isAuthorized("essentials.worlds." + requester.getWorld().getName())
                  )) {
               TeleportRequestResponseEvent event = new TeleportRequestResponseEvent(user, requester, request, true);
               Bukkit.getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  if (this.ess.getSettings().isDebug()) {
                     this.ess.getLogger().info("TPA accept cancelled by API for " + user.getName() + " (requested by " + requester.getName() + ")");
                  }
               } else {
                  Trade charge = new Trade(this.getName(), this.ess);
                  requester.sendTl("requestAcceptedFrom", user.getDisplayName());
                  CompletableFuture<Boolean> future = this.getNewExceptionFuture(requester.getSource(), commandLabel);
                  future.exceptionally(e -> {
                     user.sendTl("pendingTeleportCancelled");
                     return false;
                  });
                  if (request.isHere()) {
                     Location loc = request.getLocation();
                     AsyncTeleport teleport = requester.getAsyncTeleport();
                     teleport.setTpType(AsyncTeleport.TeleportType.TPA);
                     future.thenAccept(success -> {
                        if (success) {
                           requester.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        }
                     });
                     teleport.teleportPlayer(user, loc, charge, TeleportCause.COMMAND, future);
                  } else {
                     AsyncTeleport teleport = requester.getAsyncTeleport();
                     teleport.setTpType(AsyncTeleport.TeleportType.TPA);
                     teleport.teleport(user.getBase(), charge, TeleportCause.COMMAND, future);
                  }

                  user.removeTpaRequest(request.getName());
               }
            } else {
               throw new TranslatableException("noPendingRequest");
            }
         } else {
            throw new TranslatableException("noPendingRequest");
         }
      }
   }
}
