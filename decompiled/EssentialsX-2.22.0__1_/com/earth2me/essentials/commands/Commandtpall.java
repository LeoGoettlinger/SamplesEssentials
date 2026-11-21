package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpall extends EssentialsCommand {
   public Commandtpall() {
      super("tpall");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         if (sender.isPlayer()) {
            this.teleportAllPlayers(server, sender, this.ess.getUser(sender.getPlayer()), commandLabel);
         } else {
            throw new NotEnoughArgumentsException();
         }
      } else {
         User target = this.getPlayer(server, sender, args, 0);
         this.teleportAllPlayers(server, sender, target, commandLabel);
      }
   }

   private void teleportAllPlayers(final Server server, final CommandSource sender, final User target, final String label) {
      sender.sendTl("teleportAll");
      Location loc = target.getLocation();

      for (User player : this.ess.getOnlineUsers()) {
         if (target != player
            && (
               !sender.getSender().equals(target.getBase())
                  || target.getWorld() == player.getWorld()
                  || !this.ess.getSettings().isWorldTeleportPermissions()
                  || target.isAuthorized("essentials.worlds." + target.getWorld().getName())
            )) {
            player.getAsyncTeleport().now(loc, false, TeleportCause.COMMAND, this.getNewExceptionFuture(sender, label));
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
