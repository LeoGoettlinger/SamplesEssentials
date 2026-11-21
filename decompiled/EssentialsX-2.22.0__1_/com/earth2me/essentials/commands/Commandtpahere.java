package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.TPARequestEvent;
import org.bukkit.Server;

public class Commandtpahere extends EssentialsCommand {
   public Commandtpahere() {
      super("tpahere");
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
            && !user.isAuthorized("essentials.worlds." + user.getWorld().getName())) {
            throw new TranslatableException("noPerm", "essentials.worlds." + user.getWorld().getName());
         } else if (player.hasOutstandingTpaRequest(user.getName(), true)) {
            throw new TranslatableException("requestSentAlready", player.getDisplayName());
         } else {
            if (!player.isIgnoredPlayer(user)) {
               TPARequestEvent tpaEvent = new TPARequestEvent(user.getSource(), player, true);
               this.ess.getServer().getPluginManager().callEvent(tpaEvent);
               if (tpaEvent.isCancelled()) {
                  throw new TranslatableException("teleportRequestCancelled", player.getDisplayName());
               }

               player.requestTeleport(user, true);
               player.sendTl("teleportHereRequest", user.getDisplayName());
               player.sendTl("typeTpaccept");
               player.sendTl("typeTpdeny");
               if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
                  player.sendTl("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation());
               }
            }

            user.sendTl("requestSent", player.getDisplayName());
            user.sendTl("typeTpacancel");
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(user) : Collections.emptyList();
   }
}
