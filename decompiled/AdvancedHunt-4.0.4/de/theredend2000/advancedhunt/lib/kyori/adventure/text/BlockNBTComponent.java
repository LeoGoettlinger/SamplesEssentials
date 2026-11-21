package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import java.util.regex.Matcher;
import org.jetbrains.annotations.Contract;

public sealed interface BlockNBTComponent extends NBTComponent<BlockNBTComponent>, ScopedComponent<BlockNBTComponent> permits BlockNBTComponentImpl {
   BlockNBTComponent.Pos pos();

   @Contract(
      pure = true
   )
   BlockNBTComponent pos(final BlockNBTComponent.Pos pos);

   @Contract(
      pure = true
   )
   default BlockNBTComponent localPos(final double left, final double up, final double forwards) {
      return this.pos(BlockNBTComponent.LocalPos.localPos(left, up, forwards));
   }

   @Contract(
      pure = true
   )
   default BlockNBTComponent worldPos(
      final BlockNBTComponent.WorldPos.Coordinate x, final BlockNBTComponent.WorldPos.Coordinate y, final BlockNBTComponent.WorldPos.Coordinate z
   ) {
      return this.pos(BlockNBTComponent.WorldPos.worldPos(x, y, z));
   }

   @Contract(
      pure = true
   )
   default BlockNBTComponent absoluteWorldPos(final int x, final int y, final int z) {
      return this.worldPos(
         BlockNBTComponent.WorldPos.Coordinate.absolute(x),
         BlockNBTComponent.WorldPos.Coordinate.absolute(y),
         BlockNBTComponent.WorldPos.Coordinate.absolute(z)
      );
   }

   @Contract(
      pure = true
   )
   default BlockNBTComponent relativeWorldPos(final int x, final int y, final int z) {
      return this.worldPos(
         BlockNBTComponent.WorldPos.Coordinate.relative(x),
         BlockNBTComponent.WorldPos.Coordinate.relative(y),
         BlockNBTComponent.WorldPos.Coordinate.relative(z)
      );
   }

   BlockNBTComponent.Builder toBuilder();

   public sealed interface Builder extends NBTComponentBuilder<BlockNBTComponent, BlockNBTComponent.Builder> permits BlockNBTComponentImpl.BuilderImpl {
      @Contract("_ -> this")
      BlockNBTComponent.Builder pos(final BlockNBTComponent.Pos pos);

      @Contract("_, _, _ -> this")
      default BlockNBTComponent.Builder localPos(final double left, final double up, final double forwards) {
         return this.pos(BlockNBTComponent.LocalPos.localPos(left, up, forwards));
      }

      @Contract("_, _, _ -> this")
      default BlockNBTComponent.Builder worldPos(
         final BlockNBTComponent.WorldPos.Coordinate x, final BlockNBTComponent.WorldPos.Coordinate y, final BlockNBTComponent.WorldPos.Coordinate z
      ) {
         return this.pos(BlockNBTComponent.WorldPos.worldPos(x, y, z));
      }

      @Contract("_, _, _ -> this")
      default BlockNBTComponent.Builder absoluteWorldPos(final int x, final int y, final int z) {
         return this.worldPos(
            BlockNBTComponent.WorldPos.Coordinate.absolute(x),
            BlockNBTComponent.WorldPos.Coordinate.absolute(y),
            BlockNBTComponent.WorldPos.Coordinate.absolute(z)
         );
      }

      @Contract("_, _, _ -> this")
      default BlockNBTComponent.Builder relativeWorldPos(final int x, final int y, final int z) {
         return this.worldPos(
            BlockNBTComponent.WorldPos.Coordinate.relative(x),
            BlockNBTComponent.WorldPos.Coordinate.relative(y),
            BlockNBTComponent.WorldPos.Coordinate.relative(z)
         );
      }
   }

   public interface LocalPos extends BlockNBTComponent.Pos {
      static BlockNBTComponent.LocalPos localPos(final double left, final double up, final double forwards) {
         return new BlockNBTComponentImpl.LocalPosImpl(left, up, forwards);
      }

      double left();

      double up();

      double forwards();
   }

   public interface Pos {
      static BlockNBTComponent.Pos fromString(final String input) throws IllegalArgumentException {
         Matcher localMatch = BlockNBTComponentImpl.Tokens.LOCAL_PATTERN.matcher(input);
         if (localMatch.matches()) {
            return BlockNBTComponent.LocalPos.localPos(
               Double.parseDouble(localMatch.group(1)), Double.parseDouble(localMatch.group(3)), Double.parseDouble(localMatch.group(5))
            );
         } else {
            Matcher worldMatch = BlockNBTComponentImpl.Tokens.WORLD_PATTERN.matcher(input);
            if (worldMatch.matches()) {
               return BlockNBTComponent.WorldPos.worldPos(
                  BlockNBTComponentImpl.Tokens.deserializeCoordinate(worldMatch.group(1), worldMatch.group(2)),
                  BlockNBTComponentImpl.Tokens.deserializeCoordinate(worldMatch.group(3), worldMatch.group(4)),
                  BlockNBTComponentImpl.Tokens.deserializeCoordinate(worldMatch.group(5), worldMatch.group(6))
               );
            } else {
               throw new IllegalArgumentException("Cannot convert position specification '" + input + "' into a position");
            }
         }
      }

      String asString();
   }

   public interface WorldPos extends BlockNBTComponent.Pos {
      static BlockNBTComponent.WorldPos worldPos(
         final BlockNBTComponent.WorldPos.Coordinate x, final BlockNBTComponent.WorldPos.Coordinate y, final BlockNBTComponent.WorldPos.Coordinate z
      ) {
         return new BlockNBTComponentImpl.WorldPosImpl(x, y, z);
      }

      BlockNBTComponent.WorldPos.Coordinate x();

      BlockNBTComponent.WorldPos.Coordinate y();

      BlockNBTComponent.WorldPos.Coordinate z();

      public interface Coordinate {
         static BlockNBTComponent.WorldPos.Coordinate absolute(final int value) {
            return coordinate(value, BlockNBTComponent.WorldPos.Coordinate.Type.ABSOLUTE);
         }

         static BlockNBTComponent.WorldPos.Coordinate relative(final int value) {
            return coordinate(value, BlockNBTComponent.WorldPos.Coordinate.Type.RELATIVE);
         }

         static BlockNBTComponent.WorldPos.Coordinate coordinate(final int value, final BlockNBTComponent.WorldPos.Coordinate.Type type) {
            return new BlockNBTComponentImpl.WorldPosImpl.CoordinateImpl(value, type);
         }

         int value();

         BlockNBTComponent.WorldPos.Coordinate.Type type();

         public static enum Type {
            ABSOLUTE,
            RELATIVE;
         }
      }
   }
}
