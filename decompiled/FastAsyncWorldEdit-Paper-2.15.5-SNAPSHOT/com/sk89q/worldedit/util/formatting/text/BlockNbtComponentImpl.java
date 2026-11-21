package com.sk89q.worldedit.util.formatting.text;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BlockNbtComponentImpl extends NbtComponentImpl<BlockNbtComponent, BlockNbtComponent.Builder> implements BlockNbtComponent {
   private final BlockNbtComponent.Pos pos;

   BlockNbtComponentImpl(
      @NonNull final List<Component> children,
      @NonNull final Style style,
      final String nbtPath,
      final boolean interpret,
      @NonNull final BlockNbtComponent.Pos pos
   ) {
      super(children, style, nbtPath, interpret);
      this.pos = pos;
   }

   @NonNull
   public BlockNbtComponent nbtPath(@NonNull final String nbtPath) {
      return Objects.equals(this.nbtPath, nbtPath) ? this : new BlockNbtComponentImpl(this.children, this.style, nbtPath, this.interpret, this.pos);
   }

   @NonNull
   public BlockNbtComponent interpret(final boolean interpret) {
      return this.interpret == interpret ? this : new BlockNbtComponentImpl(this.children, this.style, this.nbtPath, interpret, this.pos);
   }

   @NonNull
   @Override
   public BlockNbtComponent.Pos pos() {
      return this.pos;
   }

   @NonNull
   @Override
   public BlockNbtComponent pos(@NonNull final BlockNbtComponent.Pos pos) {
      return new BlockNbtComponentImpl(this.children, this.style, this.nbtPath, this.interpret, pos);
   }

   @NonNull
   public BlockNbtComponent children(@NonNull final List<Component> children) {
      return new BlockNbtComponentImpl(children, this.style, this.nbtPath, this.interpret, this.pos);
   }

   @NonNull
   public BlockNbtComponent style(@NonNull final Style style) {
      return Objects.equals(this.style, style) ? this : new BlockNbtComponentImpl(this.children, style, this.nbtPath, this.interpret, this.pos);
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof BlockNbtComponent)) {
         return false;
      } else if (!super.equals(other)) {
         return false;
      } else {
         BlockNbtComponent that = (BlockNbtComponent)other;
         return Objects.equals(this.pos, that.pos());
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + this.pos.hashCode();
   }

   @Override
   protected void populateToString(@NonNull final Map<String, Object> builder) {
      super.populateToString(builder);
      builder.put("pos", this.pos);
   }

   @NonNull
   public BlockNbtComponent.Builder toBuilder() {
      return new BlockNbtComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends NbtComponentImpl.BuilderImpl<BlockNbtComponent, BlockNbtComponent.Builder> implements BlockNbtComponent.Builder {
      @Nullable
      private BlockNbtComponent.Pos pos;

      BuilderImpl() {
      }

      BuilderImpl(@NonNull final BlockNbtComponent component) {
         super(component);
         this.pos = component.pos();
      }

      @NonNull
      @Override
      public BlockNbtComponent.Builder pos(@NonNull final BlockNbtComponent.Pos pos) {
         this.pos = pos;
         return this;
      }

      @NonNull
      public BlockNbtComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.pos == null) {
            throw new IllegalStateException("pos must be set");
         } else {
            return new BlockNbtComponentImpl(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.pos);
         }
      }
   }

   static final class LocalPosImpl implements BlockNbtComponent.LocalPos {
      private final double left;
      private final double up;
      private final double forwards;

      LocalPosImpl(final double left, final double up, final double forwards) {
         this.left = left;
         this.up = up;
         this.forwards = forwards;
      }

      @Override
      public double left() {
         return this.left;
      }

      @Override
      public double up() {
         return this.up;
      }

      @Override
      public double forwards() {
         return this.forwards;
      }

      @Override
      public boolean equals(@Nullable final Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof BlockNbtComponent.LocalPos)) {
            return false;
         } else {
            BlockNbtComponent.LocalPos that = (BlockNbtComponent.LocalPos)other;
            return equals(that.left(), this.left()) && equals(that.up(), this.up()) && equals(that.forwards(), this.forwards());
         }
      }

      private static boolean equals(final double a, final double b) {
         return Double.doubleToLongBits(a) == Double.doubleToLongBits(b);
      }

      @Override
      public int hashCode() {
         int result = Double.hashCode(this.left);
         result = 31 * result + Double.hashCode(this.up);
         return 31 * result + Double.hashCode(this.forwards);
      }

      @Override
      public String toString() {
         return String.format("^%f ^%f ^%f", this.left, this.up, this.forwards);
      }
   }

   static final class WorldPosImpl implements BlockNbtComponent.WorldPos {
      private final BlockNbtComponent.WorldPos.Coordinate x;
      private final BlockNbtComponent.WorldPos.Coordinate y;
      private final BlockNbtComponent.WorldPos.Coordinate z;

      WorldPosImpl(final BlockNbtComponent.WorldPos.Coordinate x, final BlockNbtComponent.WorldPos.Coordinate y, final BlockNbtComponent.WorldPos.Coordinate z) {
         this.x = Objects.requireNonNull(x, "x");
         this.y = Objects.requireNonNull(y, "y");
         this.z = Objects.requireNonNull(z, "z");
      }

      @NonNull
      @Override
      public BlockNbtComponent.WorldPos.Coordinate x() {
         return this.x;
      }

      @NonNull
      @Override
      public BlockNbtComponent.WorldPos.Coordinate y() {
         return this.y;
      }

      @NonNull
      @Override
      public BlockNbtComponent.WorldPos.Coordinate z() {
         return this.z;
      }

      @Override
      public boolean equals(@Nullable final Object other) {
         if (this == other) {
            return true;
         } else if (!(other instanceof BlockNbtComponent.WorldPos)) {
            return false;
         } else {
            BlockNbtComponent.WorldPos that = (BlockNbtComponent.WorldPos)other;
            return this.x.equals(that.x()) && this.y.equals(that.y()) && this.z.equals(that.z());
         }
      }

      @Override
      public int hashCode() {
         int result = this.x.hashCode();
         result = 31 * result + this.y.hashCode();
         return 31 * result + this.z.hashCode();
      }

      @Override
      public String toString() {
         return this.x.toString() + ' ' + this.y.toString() + ' ' + this.z.toString();
      }

      static final class CoordinateImpl implements BlockNbtComponent.WorldPos.Coordinate {
         private final int value;
         private final BlockNbtComponent.WorldPos.Coordinate.Type type;

         CoordinateImpl(final int value, @NonNull final BlockNbtComponent.WorldPos.Coordinate.Type type) {
            this.value = value;
            this.type = Objects.requireNonNull(type, "type");
         }

         @Override
         public int value() {
            return this.value;
         }

         @NonNull
         @Override
         public BlockNbtComponent.WorldPos.Coordinate.Type type() {
            return this.type;
         }

         @Override
         public boolean equals(@Nullable final Object other) {
            if (this == other) {
               return true;
            } else if (!(other instanceof BlockNbtComponent.WorldPos.Coordinate)) {
               return false;
            } else {
               BlockNbtComponent.WorldPos.Coordinate that = (BlockNbtComponent.WorldPos.Coordinate)other;
               return this.value() == that.value() && this.type() == that.type();
            }
         }

         @Override
         public int hashCode() {
            int result = this.value;
            return 31 * result + this.type.hashCode();
         }

         @Override
         public String toString() {
            return (this.type == BlockNbtComponent.WorldPos.Coordinate.Type.RELATIVE ? "~" : "") + this.value;
         }
      }
   }
}
