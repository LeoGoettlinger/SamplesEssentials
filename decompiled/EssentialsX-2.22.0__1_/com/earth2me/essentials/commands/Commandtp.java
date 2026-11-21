package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtp extends EssentialsCommand {
   public Commandtp() {
      super("tp");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
      switch (args.length) {
         case 0:
            throw new NotEnoughArgumentsException();
         case 1:
            User player = this.getPlayer(server, user, args, 0, false, true);
            if (!player.isTeleportEnabled()) {
               throw new TranslatableException("teleportDisabled", player.getDisplayName());
            }

            if (!player.getBase().isOnline()) {
               if (user.isAuthorized("essentials.tpoffline")) {
                  throw new TranslatableException("teleportOffline", player.getDisplayName());
               }

               throw new PlayerNotFoundException();
            }

            if (user.getWorld() != player.getWorld()
               && this.ess.getSettings().isWorldTeleportPermissions()
               && !user.isAuthorized("essentials.worlds." + player.getWorld().getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + player.getWorld().getName());
            }

            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(user);
            user.getAsyncTeleport().teleport(player.getBase(), charge, TeleportCause.COMMAND, future);
            throw new NoChargeException();
         case 2:
         default:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new TranslatableException("noPerm", "essentials.tp.others");
            }

            User target = this.getPlayer(server, user, args, 0);
            User toPlayer = this.getPlayer(server, user, args, 1);
            if (!target.isTeleportEnabled()) {
               throw new TranslatableException("teleportDisabled", target.getDisplayName());
            }

            if (!toPlayer.isTeleportEnabled()) {
               throw new TranslatableException("teleportDisabled", toPlayer.getDisplayName());
            }

            if (target.getWorld() != toPlayer.getWorld()
               && this.ess.getSettings().isWorldTeleportPermissions()
               && !user.isAuthorized("essentials.worlds." + toPlayer.getWorld().getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + toPlayer.getWorld().getName());
            }

            target.sendTl("teleportAtoB", user.getDisplayName(), toPlayer.getDisplayName());
            target.getAsyncTeleport().now(toPlayer.getBase(), false, TeleportCause.COMMAND, future);
            break;
         case 3:
            if (!user.isAuthorized("essentials.tp.position")) {
               throw new TranslatableException("noPerm", "essentials.tp.position");
            }

            double x2 = args[0].startsWith("~")
               ? user.getLocation().getX() + (args[0].length() > 1 ? Double.parseDouble(args[0].substring(1)) : 0.0)
               : Double.parseDouble(args[0]);
            double y2 = args[1].startsWith("~")
               ? user.getLocation().getY() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0.0)
               : Double.parseDouble(args[1]);
            double z2 = args[2].startsWith("~")
               ? user.getLocation().getZ() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0.0)
               : Double.parseDouble(args[2]);
            if (x2 > 3.0E7 || y2 > 3.0E7 || z2 > 3.0E7 || x2 < -3.0E7 || y2 < -3.0E7 || z2 < -3.0E7) {
               throw new NotEnoughArgumentsException(user.playerTl("teleportInvalidLocation"));
            }

            Location locpos = new Location(user.getWorld(), x2, y2, z2, user.getLocation().getYaw(), user.getLocation().getPitch());
            user.getAsyncTeleport().now(locpos, false, TeleportCause.COMMAND, future);
            future.thenAccept(success -> {
               if (success) {
                  user.sendTl("teleporting", locpos.getWorld().getName(), locpos.getBlockX(), locpos.getBlockY(), locpos.getBlockZ());
               }
            });
            break;
         case 4:
            if (!user.isAuthorized("essentials.tp.others")) {
               throw new TranslatableException("noPerm", "essentials.tp.others");
            }

            if (!user.isAuthorized("essentials.tp.position")) {
               throw new TranslatableException("noPerm", "essentials.tp.position");
            }

            User target2 = this.getPlayer(server, user, args, 0);
            double x = args[1].startsWith("~")
               ? target2.getLocation().getX() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0.0)
               : Double.parseDouble(args[1]);
            double y = args[2].startsWith("~")
               ? target2.getLocation().getY() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0.0)
               : Double.parseDouble(args[2]);
            double z = args[3].startsWith("~")
               ? target2.getLocation().getZ() + (args[3].length() > 1 ? Double.parseDouble(args[3].substring(1)) : 0.0)
               : Double.parseDouble(args[3]);
            if (x > 3.0E7 || y > 3.0E7 || z > 3.0E7 || x < -3.0E7 || y < -3.0E7 || z < -3.0E7) {
               throw new NotEnoughArgumentsException(user.playerTl("teleportInvalidLocation"));
            }

            Location locposother = new Location(target2.getWorld(), x, y, z, target2.getLocation().getYaw(), target2.getLocation().getPitch());
            if (!target2.isTeleportEnabled()) {
               throw new TranslatableException("teleportDisabled", target2.getDisplayName());
            }

            user.sendTl("teleporting", locposother.getWorld().getName(), locposother.getBlockX(), locposother.getBlockY(), locposother.getBlockZ());
            target2.getAsyncTeleport().now(locposother, false, TeleportCause.COMMAND, future);
            future.thenAccept(success -> {
               if (success) {
                  target2.sendTl("teleporting", locposother.getWorld().getName(), locposother.getBlockX(), locposother.getBlockY(), locposother.getBlockZ());
               }
            });
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, false);
         if (args.length == 2) {
            User toPlayer = this.getPlayer(server, args, 1, true, false);
            target.sendTl("teleportAtoB", Console.displayName(), toPlayer.getDisplayName());
            target.getAsyncTeleport().now(toPlayer.getBase(), false, TeleportCause.COMMAND, this.getNewExceptionFuture(sender, commandLabel));
         } else {
            if (args.length <= 3) {
               throw new NotEnoughArgumentsException();
            }

            double x = args[1].startsWith("~")
               ? target.getLocation().getX() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0.0)
               : Double.parseDouble(args[1]);
            double y = args[2].startsWith("~")
               ? target.getLocation().getY() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0.0)
               : Double.parseDouble(args[2]);
            double z = args[3].startsWith("~")
               ? target.getLocation().getZ() + (args[3].length() > 1 ? Double.parseDouble(args[3].substring(1)) : 0.0)
               : Double.parseDouble(args[3]);
            if (x > 3.0E7 || y > 3.0E7 || z > 3.0E7 || x < -3.0E7 || y < -3.0E7 || z < -3.0E7) {
               throw new NotEnoughArgumentsException(sender.tl("teleportInvalidLocation"));
            }

            Location loc = new Location(target.getWorld(), x, y, z, target.getLocation().getYaw(), target.getLocation().getPitch());
            sender.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            CompletableFuture<Boolean> future = this.getNewExceptionFuture(sender, commandLabel);
            target.getAsyncTeleport().now(loc, false, TeleportCause.COMMAND, future);
            future.thenAccept(success -> {
               if (success) {
                  target.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
               }
            });
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return args.length != 1 && (args.length != 2 || !user.isAuthorized("essentials.tp.others")) ? Collections.emptyList() : this.getPlayers(user);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length != 1 && args.length != 2 ? Collections.emptyList() : this.getPlayers(sender);
   }
}
