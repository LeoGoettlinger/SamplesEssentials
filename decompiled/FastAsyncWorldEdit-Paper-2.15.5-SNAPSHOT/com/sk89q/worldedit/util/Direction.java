package com.sk89q.worldedit.util;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;

public enum Direction {
   NORTH(Vector3.at(0.0, 0.0, -1.0), Direction.Flag.CARDINAL, 3, 1),
   EAST(Vector3.at(1.0, 0.0, 0.0), Direction.Flag.CARDINAL, 0, 2),
   SOUTH(Vector3.at(0.0, 0.0, 1.0), Direction.Flag.CARDINAL, 1, 3),
   WEST(Vector3.at(-1.0, 0.0, 0.0), Direction.Flag.CARDINAL, 2, 0),
   UP(Vector3.at(0.0, 1.0, 0.0), Direction.Flag.UPRIGHT, -1, -1),
   DOWN(Vector3.at(0.0, -1.0, 0.0), Direction.Flag.UPRIGHT, -1, -1),
   NORTHEAST(Vector3.at(1.0, 0.0, -1.0), Direction.Flag.ORDINAL, 7, 8),
   NORTHWEST(Vector3.at(-1.0, 0.0, -1.0), Direction.Flag.ORDINAL, 9, 6),
   SOUTHEAST(Vector3.at(1.0, 0.0, 1.0), Direction.Flag.ORDINAL, 6, 9),
   SOUTHWEST(Vector3.at(-1.0, 0.0, 1.0), Direction.Flag.ORDINAL, 8, 7),
   WEST_NORTHWEST(Vector3.at(-Math.cos(Math.PI / 8), 0.0, -Math.sin(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 18, 14),
   WEST_SOUTHWEST(Vector3.at(-Math.cos(Math.PI / 8), 0.0, Math.sin(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 17, 13),
   NORTH_NORTHWEST(Vector3.at(-Math.sin(Math.PI / 8), 0.0, -Math.cos(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 12, 15),
   NORTH_NORTHEAST(Vector3.at(Math.sin(Math.PI / 8), 0.0, -Math.cos(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 11, 16),
   EAST_NORTHEAST(Vector3.at(Math.cos(Math.PI / 8), 0.0, -Math.sin(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 13, 17),
   EAST_SOUTHEAST(Vector3.at(Math.cos(Math.PI / 8), 0.0, Math.sin(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 14, 18),
   SOUTH_SOUTHEAST(Vector3.at(Math.sin(Math.PI / 8), 0.0, Math.cos(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 15, 12),
   SOUTH_SOUTHWEST(Vector3.at(-Math.sin(Math.PI / 8), 0.0, Math.cos(Math.PI / 8)), Direction.Flag.SECONDARY_ORDINAL, 16, 11),
   ASCENDING_NORTH(Vector3.at(0.0, 1.0, -1.0), Direction.Flag.ASCENDING_CARDINAL, 21, 19),
   ASCENDING_EAST(Vector3.at(1.0, 1.0, 0.0), Direction.Flag.ASCENDING_CARDINAL, 18, 20),
   ASCENDING_SOUTH(Vector3.at(0.0, 1.0, 1.0), Direction.Flag.ASCENDING_CARDINAL, 19, 21),
   ASCENDING_WEST(Vector3.at(-1.0, 1.0, 0.0), Direction.Flag.ASCENDING_CARDINAL, 20, 18),
   DESCENDING_NORTH(Vector3.at(0.0, -1.0, -1.0), Direction.Flag.ASCENDING_CARDINAL, 25, 23),
   DESCENDING_EAST(Vector3.at(1.0, -1.0, 0.0), Direction.Flag.ASCENDING_CARDINAL, 22, 24),
   DESCENDING_SOUTH(Vector3.at(0.0, -1.0, 1.0), Direction.Flag.ASCENDING_CARDINAL, 23, 25),
   DESCENDING_WEST(Vector3.at(-1.0, -1.0, 0.0), Direction.Flag.ASCENDING_CARDINAL, 24, 22);

   private final Vector3 direction;
   private final int flags;
   private final int left;
   private final int right;
   private final BlockVector3 blockPoint;
   private static final HashMap<String, Direction> map = new HashMap<>();

   private Direction(Vector3 vector, int flags, int left, int right) {
      this.blockPoint = BlockVector3.at(Math.signum(vector.x()), Math.signum(vector.y()), Math.signum(vector.z()));
      this.direction = vector.normalize();
      this.flags = flags;
      this.left = left;
      this.right = right;
   }

   public static Direction get(CharSequence sequence) {
      return map.get(sequence);
   }

   public Direction getLeft() {
      return this.left != -1 ? values()[this.left] : this;
   }

   public Direction getRight() {
      return this.right != -1 ? values()[this.right] : this;
   }

   public double getX() {
      return this.direction.x();
   }

   public double getY() {
      return this.direction.y();
   }

   public double getZ() {
      return this.direction.z();
   }

   public int getBlockX() {
      return this.blockPoint.x();
   }

   public int getBlockY() {
      return this.blockPoint.y();
   }

   public int getBlockZ() {
      return this.blockPoint.z();
   }

   public boolean isCardinal() {
      return (this.flags & Direction.Flag.CARDINAL) > 0;
   }

   public boolean isOrdinal() {
      return (this.flags & Direction.Flag.ORDINAL) > 0;
   }

   public boolean isSecondaryOrdinal() {
      return (this.flags & Direction.Flag.SECONDARY_ORDINAL) > 0;
   }

   public boolean isUpright() {
      return (this.flags & Direction.Flag.UPRIGHT) > 0;
   }

   public Vector3 toVector() {
      return this.direction;
   }

   public BlockVector3 toBlockVector() {
      return this.blockPoint;
   }

   @Nullable
   public static Direction findClosest(Vector3 vector, int flags) {
      if ((flags & Direction.Flag.UPRIGHT) == 0) {
         vector = vector.withY(0.0);
      }

      vector = vector.normalize();
      Direction closest = null;
      double closestDot = -2.0;

      for (Direction direction : values()) {
         if ((~flags & direction.flags) <= 0) {
            double dot = direction.toVector().dot(vector);
            if (dot >= closestDot) {
               closest = direction;
               closestDot = dot;
            }
         }
      }

      return closest;
   }

   public static List<Direction> valuesOf(int flags) {
      List<Direction> directions = new ArrayList<>();

      for (Direction direction : values()) {
         if ((~flags & direction.flags) == 0) {
            directions.add(direction);
         }
      }

      return directions;
   }

   public static Optional<Direction> fromRotationIndex(int rotation) {
      switch (rotation) {
         case 0:
            return Optional.of(SOUTH);
         case 1:
            return Optional.of(SOUTH_SOUTHWEST);
         case 2:
            return Optional.of(SOUTHWEST);
         case 3:
            return Optional.of(WEST_SOUTHWEST);
         case 4:
            return Optional.of(WEST);
         case 5:
            return Optional.of(WEST_NORTHWEST);
         case 6:
            return Optional.of(NORTHWEST);
         case 7:
            return Optional.of(NORTH_NORTHWEST);
         case 8:
            return Optional.of(NORTH);
         case 9:
            return Optional.of(NORTH_NORTHEAST);
         case 10:
            return Optional.of(NORTHEAST);
         case 11:
            return Optional.of(EAST_NORTHEAST);
         case 12:
            return Optional.of(EAST);
         case 13:
            return Optional.of(EAST_SOUTHEAST);
         case 14:
            return Optional.of(SOUTHEAST);
         case 15:
            return Optional.of(SOUTH_SOUTHEAST);
         default:
            return Optional.empty();
      }
   }

   public OptionalInt toRotationIndex() {
      switch (this) {
         case NORTH:
            return OptionalInt.of(8);
         case EAST:
            return OptionalInt.of(12);
         case SOUTH:
            return OptionalInt.of(0);
         case WEST:
            return OptionalInt.of(4);
         case UP:
         case DOWN:
         default:
            return OptionalInt.empty();
         case NORTHEAST:
            return OptionalInt.of(10);
         case NORTHWEST:
            return OptionalInt.of(6);
         case SOUTHEAST:
            return OptionalInt.of(14);
         case SOUTHWEST:
            return OptionalInt.of(2);
         case WEST_NORTHWEST:
            return OptionalInt.of(5);
         case WEST_SOUTHWEST:
            return OptionalInt.of(3);
         case NORTH_NORTHWEST:
            return OptionalInt.of(7);
         case NORTH_NORTHEAST:
            return OptionalInt.of(9);
         case EAST_NORTHEAST:
            return OptionalInt.of(11);
         case EAST_SOUTHEAST:
            return OptionalInt.of(13);
         case SOUTH_SOUTHEAST:
            return OptionalInt.of(15);
         case SOUTH_SOUTHWEST:
            return OptionalInt.of(1);
      }
   }

   public static EnumSet<Direction> getDirections(BlockState state) {
      EnumSet<Direction> directions = EnumSet.noneOf(Direction.class);

      for (Property<?> property : state.getBlockType().getProperties()) {
         if (property instanceof DirectionalProperty dirProp) {
            directions.add(state.getState(dirProp));
         } else {
            Object value = state.getState(property);
            if (value instanceof String str) {
               String i = str.toLowerCase(Locale.ROOT);
               switch (i) {
                  case "upper":
                  case "ceiling":
                  case "up":
                  case "top":
                     directions.add(UP);
                     break;
                  case "lower":
                  case "floor":
                  case "down":
                  case "bottom":
                  case "y":
                     directions.add(DOWN);
                  case "double":
                  case "wall":
                  case "hinge":
                  case "shape":
                  default:
                     break;
                  case "south":
                     directions.add(SOUTH);
                     break;
                  case "x":
                  case "east":
                     directions.add(EAST);
                     break;
                  case "z":
                  case "north":
                     directions.add(NORTH);
                     break;
                  case "west":
                     directions.add(WEST);
               }
            } else if (value instanceof Integer i) {
               fromRotationIndex(i).ifPresent(directions::add);
            } else if (value instanceof Boolean b && b) {
               try {
                  directions.add(valueOf(property.getName().toUpperCase(Locale.ROOT)));
               } catch (IllegalArgumentException var9) {
               }
            }
         }
      }

      return directions;
   }

   static {
      for (Direction dir : values()) {
         map.put(dir.name(), dir);
         map.put(dir.name().toLowerCase(Locale.ROOT), dir);
      }
   }

   public static final class Flag {
      public static int CARDINAL = 1;
      public static int ORDINAL = 2;
      public static int SECONDARY_ORDINAL = 4;
      public static int UPRIGHT = 8;
      public static int ASCENDING_CARDINAL = 15;
      public static int ALL = CARDINAL | ORDINAL | SECONDARY_ORDINAL | UPRIGHT | ASCENDING_CARDINAL;

      private Flag() {
      }
   }
}
