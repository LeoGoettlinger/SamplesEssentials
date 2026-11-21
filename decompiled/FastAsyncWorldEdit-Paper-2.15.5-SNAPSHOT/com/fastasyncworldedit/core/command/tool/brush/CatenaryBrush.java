package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.command.tool.ResettableTool;
import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import java.util.Arrays;
import java.util.List;

public class CatenaryBrush implements Brush, ResettableTool {
   private final boolean shell;
   private final boolean select;
   private final boolean direction;
   private final double slack;
   private BlockVector3 pos1;
   private BlockVector3 pos2;
   private BlockVector3 vertex;

   public CatenaryBrush(boolean shell, boolean select, boolean direction, double lengthFactor) {
      this.shell = shell;
      this.select = select;
      this.direction = direction;
      this.slack = lengthFactor;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 pos2, final Pattern pattern, double size) throws MaxChangedBlocksException {
      Actor actor = editSession.getActor();
      if (actor instanceof Player player) {
         if (this.pos1 != null && !pos2.equals(this.pos1)) {
            if (this.vertex == null) {
               this.vertex = getVertex(this.pos1.toVector3(), pos2.toVector3(), this.slack);
               if (this.direction) {
                  actor.print(Caption.of("fawe.worldedit.brush.brush.catenary.direction", 2));
                  return;
               }
            } else if (this.direction) {
               Location loc = player.getLocation();
               Vector3 facing = loc.getDirection().normalize();
               BlockVector3 midpoint = this.pos1.add(pos2).divide(2);
               BlockVector3 offset = midpoint.subtract(this.vertex);
               this.vertex = midpoint.add(facing.multiply(offset.length()).toBlockPoint());
            }

            List<BlockVector3> nodes = Arrays.asList(this.pos1, this.vertex, pos2);
            this.vertex = null;
            editSession.drawSpline(pattern, nodes, 0.0, 0.0, 0.0, 10.0, size, !this.shell);
            player.print(Caption.of("fawe.worldedit.brush.brush.line.secondary"));
            if (!this.select) {
               this.pos1 = null;
            } else {
               this.pos1 = pos2;
            }
         } else {
            this.pos1 = pos2;
            actor.print(Caption.of("fawe.worldedit.brush.brush.line.primary", pos2));
         }
      } else {
         throw FaweCache.PLAYER_ONLY;
      }
   }

   @Override
   public boolean reset() {
      this.pos1 = null;
      return true;
   }

   public static BlockVector3 getVertex(Vector3 pos1, Vector3 pos2, double lenPercent) {
      if (lenPercent <= 1.0) {
         return pos1.add(pos2).divide(2.0).toBlockPoint();
      } else {
         double curveLen = pos1.distance(pos2) * lenPercent;
         double dy = pos2.y() - pos1.y();
         double dx = pos2.x() - pos1.x();
         double dz = pos2.z() - pos1.z();
         double dh = Math.sqrt(dx * dx + dz * dz);
         double g = Math.sqrt(curveLen * curveLen - dy * dy) / 2.0;
         double a = 1.0E-5;

         while (g < a * Math.sinh(dh / (2.0 * a))) {
            a *= 1.00001;
         }

         double vertX = (dh - a * Math.log((curveLen + dy) / (curveLen - dy))) / 2.0;
         double z = dh / 2.0 / a;
         double oY = (dy - curveLen * (Math.cosh(z) / Math.sinh(z))) / 2.0;
         double vertY = a * 1.0 + oY;
         return pos1.add(pos2.subtract(pos1).multiply(vertX / dh).add(0.0, vertY, 0.0)).round().toBlockPoint();
      }
   }
}
