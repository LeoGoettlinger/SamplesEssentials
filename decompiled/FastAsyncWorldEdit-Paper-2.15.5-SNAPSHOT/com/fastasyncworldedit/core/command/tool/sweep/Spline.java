package com.fastasyncworldedit.core.command.tool.sweep;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import java.util.ArrayList;
import java.util.List;

public abstract class Spline {
   private Vector2 direction = Vector2.at(1.0, 0.0);
   private final int nodeCount;
   protected EditSession editSession;
   private final Interpolation interpolation;
   private List<Spline.Section> sections;
   private final double splineLength;

   protected Spline(EditSession editSession, Interpolation interpolation) {
      this(editSession, interpolation, -1);
   }

   protected Spline(EditSession editSession, Interpolation interpolation, int nodeCount) {
      this.editSession = editSession;
      this.interpolation = interpolation;
      this.nodeCount = nodeCount;
      this.splineLength = interpolation.arcLength(0.0, 1.0);
      if (nodeCount > 2) {
         this.initSections();
      }
   }

   public void setDirection(Vector2 direction) {
      this.direction = direction.normalize();
   }

   public Vector2 getDirection() {
      return this.direction;
   }

   public int pastePosition(double position) throws MaxChangedBlocksException {
      Preconditions.checkArgument(position >= 0.0);
      Preconditions.checkArgument(position <= 1.0);
      return this.nodeCount > 2 ? this.pastePositionDirect(this.translatePosition(position)) : this.pastePositionDirect(position);
   }

   private double cross2D(Vector2 v1, Vector2 v2) {
      return v1.x() * v2.z() - v2.x() * v1.z();
   }

   public int pastePositionDirect(double position) throws MaxChangedBlocksException {
      Preconditions.checkArgument(position >= 0.0);
      Preconditions.checkArgument(position <= 1.0);
      Vector3 target = this.interpolation.getPosition(position);
      BlockVector3 blockTarget = target.toBlockPoint();
      Vector3 offset = target.subtract(target.floor());
      Vector3 deriv = this.interpolation.get1stDerivative(position);
      Vector2 deriv2D = Vector2.at(deriv.x(), deriv.z()).normalize();
      double angle = Math.toDegrees(-Math.atan2(this.cross2D(this.direction, deriv2D), this.direction.dot(deriv2D)));
      angle = (angle % 360.0 + 360.0) % 360.0;
      return this.pasteBlocks(blockTarget, offset, angle);
   }

   protected abstract int pasteBlocks(BlockVector3 target, Vector3 offset, double angle) throws MaxChangedBlocksException;

   private void initSections() {
      int sectionCount = this.nodeCount - 1;
      this.sections = new ArrayList<>(sectionCount);
      double sectionLength = 1.0 / sectionCount;
      double position = 0.0;

      for (int i = 0; i < sectionCount; i++) {
         double length;
         if (i == sectionCount - 1) {
            length = this.interpolation.arcLength(i * sectionLength, 1.0) / this.splineLength;
         } else {
            length = this.interpolation.arcLength(i * sectionLength, (i + 1) * sectionLength) / this.splineLength;
         }

         this.sections.add(new Spline.Section(i * sectionLength, sectionLength, position, length));
         position += length;
      }
   }

   private double translatePosition(double flexPosition) {
      Spline.Section previousSection = this.sections.get(0);

      for (int i = 1; i < this.sections.size(); i++) {
         Spline.Section section = this.sections.get(i);
         if (flexPosition < section.flexStart) {
            break;
         }

         previousSection = section;
      }

      double flexOffset = flexPosition - previousSection.flexStart;
      double uniOffset = flexOffset / previousSection.flexLength * previousSection.uniLength;
      double finalPosition = previousSection.uniStart + uniOffset;
      return finalPosition > 1.0 ? 1.0 : finalPosition;
   }

   private record Section(double uniStart, double uniLength, double flexStart, double flexLength) {
   }
}
