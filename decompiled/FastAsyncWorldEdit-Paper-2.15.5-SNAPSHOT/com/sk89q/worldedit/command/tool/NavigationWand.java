package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Location;

public enum NavigationWand implements DoubleActionTraceTool {
   INSTANCE;

   private static final String PRIMARY_PERMISSION = "worldedit.navigation.thru.tool";
   private static final String SECONDARY_PERMISSION = "worldedit.navigation.jumpto.tool";

   @Override
   public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      if (!player.hasPermission("worldedit.navigation.jumpto.tool")) {
         return false;
      } else {
         int maxDist = config.navigationWandMaxDistance;
         if (maxDist <= 0) {
            return false;
         } else {
            Location pos = player.getSolidBlockTrace(maxDist);
            if (pos != null) {
               player.findFreePosition(pos);
            } else {
               player.print(Caption.of("worldedit.jumpto.none"));
            }

            return true;
         }
      }
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      if (!player.hasPermission("worldedit.navigation.thru.tool")) {
         return false;
      } else {
         int maxDist = config.navigationWandMaxDistance;
         if (maxDist <= 0) {
            return false;
         } else {
            if (!player.passThroughForwardWall(Math.max(1, maxDist - 10))) {
               player.print(Caption.of("worldedit.thru.obstructed"));
            }

            return true;
         }
      }
   }

   @Override
   public boolean canUse(Actor actor) {
      return actor.hasPermission("worldedit.navigation.thru.tool") || actor.hasPermission("worldedit.navigation.jumpto.tool");
   }
}
