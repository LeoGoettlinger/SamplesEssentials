package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandworld extends EssentialsCommand {
   public Commandworld() {
      super("world");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      World world;
      if (args.length < 1) {
         World nether = null;
         List<World> worlds = server.getWorlds();

         for (World world2 : worlds) {
            if (world2.getEnvironment() == Environment.NETHER) {
               nether = world2;
               break;
            }
         }

         if (nether == null) {
            return;
         }

         world = user.getWorld() == nether ? worlds.get(0) : nether;
      } else {
         world = this.ess.getWorld(getFinalArg(args, 0));
         if (world == null) {
            user.sendTl("invalidWorld");
            user.sendTl("possibleWorlds", server.getWorlds().size() - 1);
            user.sendTl("typeWorldName");
            throw new NoChargeException();
         }
      }

      if (this.ess.getSettings().isWorldTeleportPermissions() && !user.isAuthorized("essentials.worlds." + world.getName())) {
         throw new TranslatableException("noPerm", "essentials.worlds." + world.getName());
      } else {
         double factor;
         if (user.getWorld().getEnvironment() == Environment.NETHER && world.getEnvironment() == Environment.NORMAL) {
            factor = 8.0;
         } else if (user.getWorld().getEnvironment() == Environment.NORMAL && world.getEnvironment() == Environment.NETHER) {
            factor = 0.125;
         } else {
            factor = 1.0;
         }

         Location loc = user.getLocation();
         Location target = new Location(world, loc.getBlockX() * factor + 0.5, loc.getBlockY(), loc.getBlockZ() * factor + 0.5);
         Trade charge = new Trade(this.getName(), this.ess);
         charge.isAffordableFor(user);
         user.getAsyncTeleport().teleport(target, charge, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
         throw new NoChargeException();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         return Collections.emptyList();
      } else {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            if (!this.ess.getSettings().isWorldTeleportPermissions() || user.isAuthorized("essentials.worlds." + world.getName())) {
               worlds.add(world.getName());
            }
         }

         return worlds;
      }
   }
}
