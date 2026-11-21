package com.sk89q.worldedit.function.factory;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.GuavaUtil;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

public class Deform implements Contextual<Operation> {
   private Extent destination;
   private Region region;
   private String expression;
   private Deform.Mode mode;
   private Vector3 offset = Vector3.ZERO;

   public Deform(String expression) {
      this(new NullExtent(), new NullRegion(), expression);
   }

   public Deform(String expression, Deform.Mode mode) {
      this(new NullExtent(), new NullRegion(), expression, mode);
   }

   public Deform(Extent destination, Region region, String expression) {
      this(destination, region, expression, Deform.Mode.UNIT_CUBE);
   }

   public Deform(Extent destination, Region region, String expression, Deform.Mode mode) {
      Preconditions.checkNotNull(destination, "destination");
      Preconditions.checkNotNull(region, "region");
      Preconditions.checkNotNull(mode, "mode");
      Preconditions.checkNotNull(expression, "expression");
      this.destination = destination;
      this.region = region;
      this.expression = expression;
      this.mode = mode;
   }

   public Extent getDestination() {
      return this.destination;
   }

   public void setDestination(Extent destination) {
      Preconditions.checkNotNull(destination, "destination");
      this.destination = destination;
   }

   public Region getRegion() {
      return this.region;
   }

   public void setRegion(Region region) {
      Preconditions.checkNotNull(region, "region");
      this.region = region;
   }

   public String getExpression() {
      return this.expression;
   }

   public void setExpression(String expression) {
      Preconditions.checkNotNull(expression, "expression");
      this.expression = expression;
   }

   public Deform.Mode getMode() {
      return this.mode;
   }

   public void setMode(Deform.Mode mode) {
      Preconditions.checkNotNull(mode, "mode");
      this.mode = mode;
   }

   public Vector3 getOffset() {
      return this.offset;
   }

   public void setOffset(Vector3 offset) {
      Preconditions.checkNotNull(offset, "offset");
      this.offset = offset;
   }

   @Override
   public String toString() {
      return "deformation of " + this.expression;
   }

   // $VF: Could not properly define all variable types!
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public Operation createFromContext(final EditContext context) {
      Region region = GuavaUtil.firstNonNull(context.getRegion(), this.region);
      Vector3 zero;

      Vector3 unit = switch (this.mode) {
         case RAW_COORD -> {
            zero = Vector3.ZERO;
            yield Vector3.ONE;
         }
         default -> {
            zero = this.offset;
            yield Vector3.ONE;
         }
         case UNIT_CUBE -> {
            Vector3 min = region.getMinimumPoint().toVector3();
            Vector3 max = region.getMaximumPoint().toVector3();
            zero = max.add(min).multiply(0.5);
            yield max.subtract(zero);
            <unknown> unit;
            if (unit.x() == 0.0) {
               yield unit.withX(1.0);
            }

            if (unit.y() == 0.0) {
               yield unit.withY(1.0);
            }

            if (unit.z() == 0.0) {
               yield unit.withZ(1.0);
            }
         }
      };
      LocalSession session = context.getSession();
      return new Deform.DeformOperation(
         context.getDestination(),
         region,
         zero,
         unit,
         this.expression,
         session == null ? WorldEdit.getInstance().getConfiguration().calculationTimeout : session.getTimeout()
      );
   }

   private record DeformOperation(Extent destination, Region region, Vector3 zero, Vector3 unit, String expression, int timeout) implements Operation {
      @Override
      public Operation resume(RunContext run) throws WorldEditException {
         try {
            ((EditSession)this.destination).deformRegion(this.region, this.zero, this.unit, this.expression, this.timeout);
            return null;
         } catch (ExpressionException var3) {
            throw new RuntimeException("Failed to execute expression", var3);
         }
      }

      @Override
      public void cancel() {
      }

      @Override
      public Iterable<Component> getStatusMessages() {
         return ImmutableList.of(Caption.of("worldedit.operation.deform.expression", TextComponent.of(this.expression)));
      }
   }

   public static enum Mode {
      RAW_COORD,
      OFFSET,
      UNIT_CUBE;
   }
}
