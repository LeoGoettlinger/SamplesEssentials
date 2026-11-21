package com.sk89q.worldedit.util.formatting.text;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface BlockNbtComponent extends NbtComponent<BlockNbtComponent, BlockNbtComponent.Builder>, ScopedComponent<BlockNbtComponent> {
   @NonNull
   static BlockNbtComponent.Builder builder() {
      return new BlockNbtComponentImpl.BuilderImpl();
   }

   @NonNull
   static BlockNbtComponent of(@NonNull final String nbtPath, @NonNull final BlockNbtComponent.Pos pos) {
      return builder().nbtPath(nbtPath).pos(pos).build();
   }

   @NonNull
   static BlockNbtComponent make(@NonNull final Consumer<? super BlockNbtComponent.Builder> consumer) {
      BlockNbtComponent.Builder builder = builder();
      return AbstractComponentBuilder.configureAndBuild(builder, consumer);
   }

   @NonNull
   BlockNbtComponent.Pos pos();

   @NonNull
   BlockNbtComponent pos(@NonNull final BlockNbtComponent.Pos pos);

   @NonNull
   default BlockNbtComponent localPos(final double left, final double up, final double forwards) {
      return this.pos(BlockNbtComponent.LocalPos.of(left, up, forwards));
   }

   @NonNull
   default BlockNbtComponent worldPos(
      @NonNull final BlockNbtComponent.WorldPos.Coordinate x,
      @NonNull final BlockNbtComponent.WorldPos.Coordinate y,
      @NonNull final BlockNbtComponent.WorldPos.Coordinate z
   ) {
      return this.pos(BlockNbtComponent.WorldPos.of(x, y, z));
   }

   @NonNull
   default BlockNbtComponent absoluteWorldPos(final int x, final int y, final int z) {
      return this.worldPos(
         BlockNbtComponent.WorldPos.Coordinate.absolute(x),
         BlockNbtComponent.WorldPos.Coordinate.absolute(y),
         BlockNbtComponent.WorldPos.Coordinate.absolute(z)
      );
   }

   @NonNull
   default BlockNbtComponent relativeWorldPos(final int x, final int y, final int z) {
      return this.worldPos(
         BlockNbtComponent.WorldPos.Coordinate.relative(x),
         BlockNbtComponent.WorldPos.Coordinate.relative(y),
         BlockNbtComponent.WorldPos.Coordinate.relative(z)
      );
   }

   public interface Builder extends NbtComponentBuilder<BlockNbtComponent, BlockNbtComponent.Builder> {
      @NonNull
      BlockNbtComponent.Builder pos(@NonNull final BlockNbtComponent.Pos pos);

      @NonNull
      default BlockNbtComponent.Builder localPos(final double left, final double up, final double forwards) {
         return this.pos(BlockNbtComponent.LocalPos.of(left, up, forwards));
      }

      @NonNull
      default BlockNbtComponent.Builder worldPos(
         @NonNull final BlockNbtComponent.WorldPos.Coordinate x,
         @NonNull final BlockNbtComponent.WorldPos.Coordinate y,
         @NonNull final BlockNbtComponent.WorldPos.Coordinate z
      ) {
         return this.pos(BlockNbtComponent.WorldPos.of(x, y, z));
      }

      @NonNull
      default BlockNbtComponent.Builder absoluteWorldPos(final int x, final int y, final int z) {
         return this.worldPos(
            BlockNbtComponent.WorldPos.Coordinate.absolute(x),
            BlockNbtComponent.WorldPos.Coordinate.absolute(y),
            BlockNbtComponent.WorldPos.Coordinate.absolute(z)
         );
      }

      @NonNull
      default BlockNbtComponent.Builder relativeWorldPos(final int x, final int y, final int z) {
         return this.worldPos(
            BlockNbtComponent.WorldPos.Coordinate.relative(x),
            BlockNbtComponent.WorldPos.Coordinate.relative(y),
            BlockNbtComponent.WorldPos.Coordinate.relative(z)
         );
      }
   }

   public interface LocalPos extends BlockNbtComponent.Pos {
      @NonNull
      static BlockNbtComponent.LocalPos of(final double left, final double up, final double forwards) {
         return new BlockNbtComponentImpl.LocalPosImpl(left, up, forwards);
      }

      double left();

      double up();

      double forwards();
   }

   public interface Pos {
   }

   public interface WorldPos extends BlockNbtComponent.Pos {
      @NonNull
      static BlockNbtComponent.WorldPos of(
         @NonNull final BlockNbtComponent.WorldPos.Coordinate x,
         @NonNull final BlockNbtComponent.WorldPos.Coordinate y,
         @NonNull final BlockNbtComponent.WorldPos.Coordinate z
      ) {
         return new BlockNbtComponentImpl.WorldPosImpl(x, y, z);
      }

      @NonNull
      BlockNbtComponent.WorldPos.Coordinate x();

      @NonNull
      BlockNbtComponent.WorldPos.Coordinate y();

      @NonNull
      BlockNbtComponent.WorldPos.Coordinate z();

      public interface Coordinate {
         @NonNull
         static BlockNbtComponent.WorldPos.Coordinate absolute(final int value) {
            return of(value, BlockNbtComponent.WorldPos.Coordinate.Type.ABSOLUTE);
         }

         @NonNull
         static BlockNbtComponent.WorldPos.Coordinate relative(final int value) {
            return of(value, BlockNbtComponent.WorldPos.Coordinate.Type.RELATIVE);
         }

         @NonNull
         static BlockNbtComponent.WorldPos.Coordinate of(final int value, @NonNull final BlockNbtComponent.WorldPos.Coordinate.Type type) {
            return new BlockNbtComponentImpl.WorldPosImpl.CoordinateImpl(value, type);
         }

         int value();

         @NonNull
         BlockNbtComponent.WorldPos.Coordinate.Type type();

         public static enum Type {
            ABSOLUTE,
            RELATIVE;
         }
      }
   }
}
