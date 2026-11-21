package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.command.tool.ResettableTool;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.function.mask.IdMask;
import com.fastasyncworldedit.core.function.visitor.DFSRecursiveVisitor;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.interpolation.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SplineBrush implements Brush, ResettableTool {
   public static int MAX_POINTS = 15;
   private final ArrayList<ArrayList<BlockVector3>> positionSets;
   private int numSplines;
   private final Player player;
   private BlockVector3 position;

   public SplineBrush(Player player) {
      this.player = player;
      this.positionSets = new ArrayList<>();
   }

   @Override
   public boolean reset() {
      this.numSplines = 0;
      this.positionSets.clear();
      this.position = null;
      return true;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws WorldEditException {
      Mask mask = editSession.getMask();
      if (mask == null) {
         mask = new IdMask(editSession);
      } else {
         mask = new MaskIntersection(mask, new IdMask(editSession));
      }

      boolean newPos = !position.equals(this.position);
      this.position = position;
      if (newPos) {
         if (this.positionSets.size() >= MAX_POINTS) {
            throw FaweCache.MAX_CHECKS;
         } else {
            ArrayList<BlockVector3> points = new ArrayList<>();
            if (size > 0.0) {
               DFSRecursiveVisitor visitor = new DFSRecursiveVisitor(mask, p -> {
                  points.add(p.toImmutable());
                  return true;
               }, (int)size, 1);
               List<BlockVector3> directions = visitor.getDirections();

               for (int x = -1; x <= 1; x++) {
                  for (int y = -1; y <= 1; y++) {
                     for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                           BlockVector3 pos = BlockVector3.at(x, y, z);
                           if (!directions.contains(pos)) {
                              directions.add(pos);
                           }
                        }
                     }
                  }
               }

               directions.sort((o1, o2) -> (int)Math.signum((float)(o1.lengthSq() - o2.lengthSq())));
               visitor.visit(position);
               Operations.completeBlindly(visitor);
               if (points.size() > this.numSplines) {
                  this.numSplines = points.size();
               }
            } else {
               points.add(position);
            }

            this.positionSets.add(points);
            this.player.print(Caption.of("fawe.worldedit.brush.spline.primary.2"));
         }
      } else if (this.positionSets.size() < 2) {
         this.player.print(Caption.of("fawe.worldedit.brush.brush.spline.secondary.error"));
      } else {
         List<Vector3> centroids = new ArrayList<>();

         for (List<BlockVector3> points : this.positionSets) {
            centroids.add(this.getCentroid(points));
         }

         double tension = 0.0;
         double bias = 0.0;
         double continuity = 0.0;
         List<Node> nodes = new ArrayList<>(centroids.size());

         for (Vector3 nodevector : centroids) {
            Node n = new Node(nodevector);
            n.setTension(tension);
            n.setBias(bias);
            n.setContinuity(continuity);
            nodes.add(n);
         }

         for (int i = 0; i < this.numSplines; i++) {
            List<BlockVector3> currentSpline = new ArrayList<>();

            for (ArrayList<BlockVector3> points : this.positionSets) {
               int listSize = points.size();
               int index = (int)((double)(i * listSize) / this.numSplines);
               currentSpline.add(points.get(index));
            }

            editSession.drawSpline(pattern, currentSpline, 0.0, 0.0, 0.0, 10.0, 0.0, true);
         }

         this.player.print(Caption.of("fawe.worldedit.brush.spline.secondary"));
         this.positionSets.clear();
         this.numSplines = 0;
      }
   }

   private Vector3 getCentroid(Collection<BlockVector3> points) {
      MutableVector3 sum = new MutableVector3();

      for (BlockVector3 p : points) {
         sum.mutX(sum.x() + p.x());
         sum.mutY(sum.y() + p.y());
         sum.mutZ(sum.z() + p.z());
      }

      return sum.multiply(1.0 / points.size());
   }
}
