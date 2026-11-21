package com.earth2me.essentials.commands;

import com.earth2me.essentials.AsyncTeleport;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.TPARequestEvent;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpa extends EssentialsCommand {
   public Commandtpa() {
      super("tpa");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User player = this.getPlayer(server, user, args, 0);
         if (user.getName().equalsIgnoreCase(player.getName())) {
            throw new NotEnoughArgumentsException();
         } else if (!player.isAuthorized("essentials.tpaccept")) {
            throw new TranslatableException("teleportNoAcceptPermission", player.getDisplayName());
         } else if (!player.isTeleportEnabled()) {
            throw new TranslatableException("teleportDisabled", player.getDisplayName());
         } else if (user.getWorld() != player.getWorld()
            && this.ess.getSettings().isWorldTeleportPermissions()
            && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
            throw new TranslatableException("noPerm", "essentials.worlds." + player.getWorld().getName());
         } else if (player.hasOutstandingTpaRequest(user.getName(), false)) {
            throw new TranslatableException("requestSentAlready", player.getDisplayName());
         } else if (player.isAutoTeleportEnabled() && !player.isIgnoredPlayer(user)) {
            Trade charge = new Trade(this.getName(), this.ess);
            AsyncTeleport teleport = user.getAsyncTeleport();
            teleport.setTpType(AsyncTeleport.TeleportType.TPA);
            CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
            teleport.teleport(player.getBase(), charge, TeleportCause.COMMAND, future);
            future.thenAccept(success -> {
               if (success) {
                  player.sendTl("requestAcceptedAuto", user.getDisplayName());
                  user.sendTl("requestAcceptedFromAuto", player.getDisplayName());
               }
            });
            throw new NoChargeException();
         } else {
            if (!player.isIgnoredPlayer(user)) {
               TPARequestEvent tpaEvent = new TPARequestEvent(user.getSource(), player, false);
               this.ess.getServer().getPluginManager().callEvent(tpaEvent);
               if (tpaEvent.isCancelled()) {
                  throw new TranslatableException("teleportRequestCancelled", player.getDisplayName());
               }

               player.requestTeleport(user, false);
               player.sendTl("teleportRequest", user.getDisplayName());
               player.sendTl("typeTpaccept");
               player.sendTl("typeTpdeny");
               if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
                  player.sendTl("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation());
               }
            }

            user.sendTl("requestSent", player.getDisplayName());
            if (user.isAuthorized("essentials.tpacancel")) {
               user.sendTl("typeTpacancel");
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
