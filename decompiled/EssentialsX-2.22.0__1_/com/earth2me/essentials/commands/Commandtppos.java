package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FloatUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtppos extends EssentialsCommand {
   public Commandtppos() {
      super("tppos");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 3) {
         throw new NotEnoughArgumentsException();
      } else {
         double x = args[0].startsWith("~")
            ? user.getLocation().getX() + (args[0].length() > 1 ? Double.parseDouble(args[0].substring(1)) : 0.0)
            : Double.parseDouble(args[0]);
         double y = args[1].startsWith("~")
            ? user.getLocation().getY() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0.0)
            : Double.parseDouble(args[1]);
         double z = args[2].startsWith("~")
            ? user.getLocation().getZ() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0.0)
            : Double.parseDouble(args[2]);
         Location loc = new Location(user.getWorld(), x, y, z, user.getLocation().getYaw(), user.getLocation().getPitch());
         if (args.length == 4) {
            World w = this.ess.getWorld(args[3]);
            if (user.getWorld() != w && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + w.getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + w.getName());
            }

            loc.setWorld(w);
         }

         if (args.length > 4) {
            loc.setYaw((FloatUtil.parseFloat(args[3]) + 360.0F) % 360.0F);
            loc.setPitch(FloatUtil.parseFloat(args[4]));
         }

         if (args.length > 5) {
            World w = this.ess.getWorld(args[5]);
            if (user.getWorld() != w && this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + w.getName())) {
               throw new TranslatableException("noPerm", "essentials.worlds." + w.getName());
            }

            loc.setWorld(w);
         }

         if (!(x > 3.0E7) && !(y > 3.0E7) && !(z > 3.0E7) && !(x < -3.0E7) && !(y < -3.0E7) && !(z < -3.0E7)) {
            Trade charge = new Trade(this.getName(), this.ess);
            charge.isAffordableFor(user);
            user.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            user.getAsyncTeleport().teleport(loc, charge, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
            throw new NoChargeException();
         } else {
            throw new NotEnoughArgumentsException(user.playerTl("teleportInvalidLocation"));
         }
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 4) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, args, 0, true, false);
         double x = args[1].startsWith("~")
            ? user.getLocation().getX() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0.0)
            : Double.parseDouble(args[1]);
         double y = args[2].startsWith("~")
            ? user.getLocation().getY() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0.0)
            : Double.parseDouble(args[2]);
         double z = args[3].startsWith("~")
            ? user.getLocation().getZ() + (args[3].length() > 1 ? Double.parseDouble(args[3].substring(1)) : 0.0)
            : Double.parseDouble(args[3]);
         Location loc = new Location(user.getWorld(), x, y, z, user.getLocation().getYaw(), user.getLocation().getPitch());
         if (args.length == 5) {
            loc.setWorld(this.ess.getWorld(args[4]));
         }

         if (args.length > 5) {
            loc.setYaw((FloatUtil.parseFloat(args[4]) + 360.0F) % 360.0F);
            loc.setPitch(FloatUtil.parseFloat(args[5]));
         }

         if (args.length > 6) {
            loc.setWorld(this.ess.getWorld(args[6]));
         }

         if (!(x > 3.0E7) && !(y > 3.0E7) && !(z > 3.0E7) && !(x < -3.0E7) && !(y < -3.0E7) && !(z < -3.0E7)) {
            sender.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            user.sendTl("teleporting", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            user.getAsyncTeleport().teleport(loc, null, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
         } else {
            throw new NotEnoughArgumentsException(sender.tl("teleportInvalidLocation"));
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1 || args.length == 2 || args.length == 3) {
         return Lists.newArrayList(new String[]{"~0"});
      } else if (args.length == 4 || args.length == 5) {
         return Lists.newArrayList(new String[]{"0"});
      } else if (args.length != 6) {
         return Collections.emptyList();
      } else {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            worlds.add(world.getName());
         }

         return worlds;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else if (args.length == 2 || args.length == 3 || args.length == 4) {
         return Lists.newArrayList(new String[]{"~0"});
      } else if (args.length == 5 || args.length == 6) {
         return Lists.newArrayList(new String[]{"0"});
      } else if (args.length != 7) {
         return Collections.emptyList();
      } else {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            worlds.add(world.getName());
         }

         return worlds;
      }
   }
}
