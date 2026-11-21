package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public enum SelectionWand implements DoubleActionBlockTool {
   INSTANCE;

   private static final Logger LOGGER = LogManagerCompat.getLogger();

   @Override
   public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      RegionSelector selector = session.getRegionSelector(player.getWorld());
      BlockVector3 blockPoint = clicked.toVector().toBlockPoint();
      if (selector.selectPrimary(blockPoint, ActorSelectorLimits.forActor(player))) {
         if (Settings.settings().EXPERIMENTAL.OTHER) {
            LOGGER.info("actSecondary Hit and about to explain with explainPrimarySelection");
         }

         selector.explainPrimarySelection(player, session, blockPoint);
      }

      return true;
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      RegionSelector selector = session.getRegionSelector(player.getWorld());
      BlockVector3 blockPoint = clicked.toVector().toBlockPoint();
      if (selector.selectSecondary(blockPoint, ActorSelectorLimits.forActor(player))) {
         if (Settings.settings().EXPERIMENTAL.OTHER) {
            LOGGER.info("actPrimary Hit and about to explain with explainSecondarySelection");
         }

         selector.explainSecondarySelection(player, session, blockPoint);
      }

      return true;
   }

   @Override
   public boolean canUse(Actor actor) {
      return actor.hasPermission("worldedit.selection.pos");
   }
}
