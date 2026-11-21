package com.fastasyncworldedit.core.command.tool.sweep;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.command.tool.ResettableTool;
import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import com.sk89q.worldedit.math.interpolation.KochanekBartelsInterpolation;
import com.sk89q.worldedit.math.interpolation.Node;
import com.sk89q.worldedit.math.interpolation.ReparametrisingInterpolation;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SweepBrush implements Brush, ResettableTool {
   private final List<BlockVector3> positions = new ArrayList<>();
   private BlockVector3 position;
   private final int copies;
   private static final double tension = 0.0;
   private static final double bias = 0.0;
   private static final double continuity = 0.0;

   public SweepBrush(int copies) {
      this.copies = copies > 0 ? copies : -1;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      boolean newPos = !position.equals(this.position);
      this.position = position;
      Actor actor = editSession.getActor();
      if (!(actor instanceof Player)) {
         throw FaweCache.ACTOR_REQUIRED;
      } else if (newPos) {
         actor.print(Caption.of("fawe.worldedit.brush.spline.primary.2"));
         this.positions.add(position);
      } else if (this.positions.size() < 2) {
         actor.print(Caption.of("fawe.worldedit.brush.brush.spline.secondary.error"));
      } else {
         Interpolation interpol = new ReparametrisingInterpolation(new KochanekBartelsInterpolation());
         List<Node> nodes = this.positions.stream().map(v -> {
            Node n = new Node(v.toVector3());
            n.setTension(0.0);
            n.setBias(0.0);
            n.setContinuity(0.0);
            return n;
         }).collect(Collectors.toList());
         interpol.setNodes(nodes);
         LocalSession session = actor.getSession();
         ClipboardHolder holder = session.getExistingClipboard();
         if (holder == null) {
            throw new RuntimeException(new EmptyClipboardException());
         } else {
            Clipboard clipboard = holder.getClipboard();
            BlockVector3 dimensions = clipboard.getDimensions();
            double quality = Math.max(dimensions.x(), dimensions.z());
            AffineTransform transform = new AffineTransform();
            ClipboardSpline spline = new ClipboardSpline(editSession, holder, interpol, transform, nodes.size());
            if (dimensions.x() > dimensions.z()) {
               spline.setDirection(Vector2.at(0.0, 1.0));
            }

            switch (this.copies) {
               case -1:
                  double length = interpol.arcLength(0.0, 1.0);
                  double step = 1.0 / (length * quality);

                  for (double pos = 0.0; pos <= 1.0; pos += step) {
                     spline.pastePosition(pos);
                  }
                  break;
               case 1:
                  spline.pastePosition(0.0);
                  break;
               default:
                  for (double pos = 0.0; pos <= 1.0; pos += 1.0 / (this.copies - 1)) {
                     spline.pastePosition(pos);
                  }
            }

            actor.print(Caption.of("fawe.worldedit.brush.spline.secondary"));
            this.reset();
         }
      }
   }

   @Override
   public boolean reset() {
      this.positions.clear();
      this.position = null;
      return true;
   }
}
