package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.Style;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

record BlockNBTComponentImpl(
   List<Component> children, Style style, String nbtPath, boolean interpret, @Nullable Component separator, BlockNBTComponent.Pos pos, boolean plain
) implements BlockNBTComponent {
   static BlockNBTComponent create(
      final List<? extends ComponentLike> children,
      final Style style,
      final String nbtPath,
      final boolean interpret,
      @Nullable final ComponentLike separator,
      final BlockNBTComponent.Pos pos,
      final boolean plain
   ) {
      return new BlockNBTComponentImpl(
         ComponentLike.asComponents(children, IS_NOT_EMPTY),
         Objects.requireNonNull(style, "style"),
         Objects.requireNonNull(nbtPath, "nbtPath"),
         interpret,
         ComponentLike.unbox(separator),
         Objects.requireNonNull(pos, "pos"),
         plain
      );
   }

   public BlockNBTComponent nbtPath(final String nbtPath) {
      return (BlockNBTComponent)(Objects.equals(this.nbtPath, nbtPath)
         ? this
         : create(this.children, this.style, nbtPath, this.interpret, this.separator, this.pos, this.plain));
   }

   public BlockNBTComponent interpret(final boolean interpret) {
      if (this.interpret == interpret) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(interpret, this.plain);
         return create(this.children, this.style, this.nbtPath, interpret, this.separator, this.pos, this.plain);
      }
   }

   public BlockNBTComponent plain(final boolean plain) {
      if (this.plain == plain) {
         return this;
      } else {
         AbstractNBTComponentBuilder.checkInterpretPlainState(this.interpret, plain);
         return create(this.children, this.style, this.nbtPath, this.interpret, this.separator, this.pos, plain);
      }
   }

   public BlockNBTComponent separator(@Nullable final ComponentLike separator) {
      return create(this.children, this.style, this.nbtPath, this.interpret, separator, this.pos, this.plain);
   }

   @Override
   public BlockNBTComponent pos(final BlockNBTComponent.Pos pos) {
      return create(this.children, this.style, this.nbtPath, this.interpret, this.separator, pos, this.plain);
   }

   public BlockNBTComponent children(final List<? extends ComponentLike> children) {
      return create(children, this.style, this.nbtPath, this.interpret, this.separator, this.pos, this.plain);
   }

   public BlockNBTComponent style(final Style style) {
      return create(this.children, style, this.nbtPath, this.interpret, this.separator, this.pos, this.plain);
   }

   @Override
   public BlockNBTComponent.Builder toBuilder() {
      return new BlockNBTComponentImpl.BuilderImpl(this);
   }

   static final class BuilderImpl extends AbstractNBTComponentBuilder<BlockNBTComponent, BlockNBTComponent.Builder> implements BlockNBTComponent.Builder {
      @Nullable
      private BlockNBTComponent.Pos pos;

      BuilderImpl() {
      }

      BuilderImpl(final BlockNBTComponent component) {
         super(component);
         this.pos = component.pos();
      }

      @Override
      public BlockNBTComponent.Builder pos(final BlockNBTComponent.Pos pos) {
         this.pos = Objects.requireNonNull(pos, "pos");
         return this;
      }

      public BlockNBTComponent build() {
         if (this.nbtPath == null) {
            throw new IllegalStateException("nbt path must be set");
         } else if (this.pos == null) {
            throw new IllegalStateException("pos must be set");
         } else {
            return BlockNBTComponentImpl.create(this.children, this.buildStyle(), this.nbtPath, this.interpret, this.separator, this.pos, this.plain);
         }
      }
   }

   record LocalPosImpl(double left, double up, double forwards) implements BlockNBTComponent.LocalPos {
      @Override
      public String toString() {
         return String.format("^%f ^%f ^%f", this.left, this.up, this.forwards);
      }

      @Override
      public String asString() {
         return BlockNBTComponentImpl.Tokens.serializeLocal(this.left)
            + " "
            + BlockNBTComponentImpl.Tokens.serializeLocal(this.up)
            + " "
            + BlockNBTComponentImpl.Tokens.serializeLocal(this.forwards);
      }
   }

   static final class Tokens {
      static final Pattern LOCAL_PATTERN = Pattern.compile("^\\^(-?\\d+(\\.\\d+)?) \\^(-?\\d+(\\.\\d+)?) \\^(-?\\d+(\\.\\d+)?)$");
      static final Pattern WORLD_PATTERN = Pattern.compile("^(~?)(-?\\d+) (~?)(-?\\d+) (~?)(-?\\d+)$");
      static final String LOCAL_SYMBOL = "^";
      static final String RELATIVE_SYMBOL = "~";
      static final String ABSOLUTE_SYMBOL = "";

      private Tokens() {
      }

      static BlockNBTComponent.WorldPos.Coordinate deserializeCoordinate(final String prefix, final String value) {
         int i = Integer.parseInt(value);
         if (prefix.equals("")) {
            return BlockNBTComponent.WorldPos.Coordinate.absolute(i);
         } else if (prefix.equals("~")) {
            return BlockNBTComponent.WorldPos.Coordinate.relative(i);
         } else {
            throw new AssertionError();
         }
      }

      static String serializeLocal(final double value) {
         return "^" + value;
      }

      static String serializeCoordinate(final BlockNBTComponent.WorldPos.Coordinate coordinate) {
         return (coordinate.type() == BlockNBTComponent.WorldPos.Coordinate.Type.RELATIVE ? "~" : "") + coordinate.value();
      }
   }

   record WorldPosImpl(BlockNBTComponent.WorldPos.Coordinate x, BlockNBTComponent.WorldPos.Coordinate y, BlockNBTComponent.WorldPos.Coordinate z)
      implements BlockNBTComponent.WorldPos {
      WorldPosImpl(final BlockNBTComponent.WorldPos.Coordinate x, final BlockNBTComponent.WorldPos.Coordinate y, final BlockNBTComponent.WorldPos.Coordinate z) {
         this.x = Objects.requireNonNull(x, "x");
         this.y = Objects.requireNonNull(y, "y");
         this.z = Objects.requireNonNull(z, "z");
      }

      @Override
      public String toString() {
         return this.x.toString() + " " + this.y.toString() + " " + this.z.toString();
      }

      @Override
      public String asString() {
         return BlockNBTComponentImpl.Tokens.serializeCoordinate(this.x())
            + " "
            + BlockNBTComponentImpl.Tokens.serializeCoordinate(this.y())
            + " "
            + BlockNBTComponentImpl.Tokens.serializeCoordinate(this.z());
      }

      record CoordinateImpl(int value, BlockNBTComponent.WorldPos.Coordinate.Type type) implements BlockNBTComponent.WorldPos.Coordinate {
         CoordinateImpl(final int value, final BlockNBTComponent.WorldPos.Coordinate.Type type) {
            this.value = value;
            this.type = Objects.requireNonNull(type, "type");
         }

         @Override
         public String toString() {
            return (this.type == BlockNBTComponent.WorldPos.Coordinate.Type.RELATIVE ? "~" : "") + this.value;
         }
      }
   }
}
