package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.api.events.TPARequestEvent;
import org.bukkit.Server;

public class Commandtpaall extends EssentialsCommand {
   public Commandtpaall() {
      super("tpaall");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         if (sender.isPlayer()) {
            this.tpaAll(sender, this.ess.getUser(sender.getPlayer()));
         } else {
            throw new NotEnoughArgumentsException();
         }
      } else {
         User target = this.getPlayer(server, sender, args, 0);
         this.tpaAll(sender, target);
      }
   }

   private void tpaAll(final CommandSource sender, final User target) {
      sender.sendTl("teleportAAll");

      for (User player : this.ess.getOnlineUsers()) {
         if (target != player
            && player.isTeleportEnabled()
            && (
               !sender.getSender().equals(target.getBase())
                  || target.getWorld() == player.getWorld()
                  || !this.ess.getSettings().isWorldTeleportPermissions()
                  || target.isAuthorized("essentials.worlds." + target.getWorld().getName())
            )) {
            try {
               TPARequestEvent tpaEvent = new TPARequestEvent(sender, player, true);
               this.ess.getServer().getPluginManager().callEvent(tpaEvent);
               if (tpaEvent.isCancelled()) {
                  sender.sendTl("teleportRequestCancelled", player.getDisplayName());
               } else {
                  player.requestTeleport(target, true);
                  player.sendTl("teleportHereRequest", target.getDisplayName());
                  player.sendTl("typeTpaccept");
                  if (this.ess.getSettings().getTpaAcceptCancellation() != 0L) {
                     player.sendTl("teleportRequestTimeoutInfo", this.ess.getSettings().getTpaAcceptCancellation());
                  }
               }
            } catch (Exception var6) {
               this.ess.showError(sender, var6, this.getName());
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
