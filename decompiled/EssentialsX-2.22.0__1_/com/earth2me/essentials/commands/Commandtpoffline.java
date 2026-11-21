package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtpoffline extends EssentialsCommand {
   public Commandtpoffline() {
      super("tpoffline");
   }

   @Override
   public void run(final Server server, final User user, final String label, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, true);
         Location logout = target.getLogoutLocation();
         if (logout == null) {
            user.sendTl("teleportOfflineUnknown", target.getDisplayName());
            throw new NoChargeException();
         } else if (user.getWorld() != logout.getWorld()
            && this.ess.getSettings().isWorldTeleportPermissions()
            && !user.isAuthorized("essentials.worlds." + logout.getWorld().getName())) {
            throw new TranslatableException("noPerm", "essentials.worlds." + logout.getWorld().getName());
         } else {
            user.sendTl("teleporting", logout.getWorld().getName(), logout.getBlockX(), logout.getBlockY(), logout.getBlockZ());
            user.getAsyncTeleport().now(logout, false, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), label));
         }
      }
   }
}
