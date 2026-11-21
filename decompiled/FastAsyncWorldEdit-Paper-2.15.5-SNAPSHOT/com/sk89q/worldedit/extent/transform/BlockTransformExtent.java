package com.sk89q.worldedit.extent.transform;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.ResettableExtent;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.fastasyncworldedit.core.registry.state.PropertyKeySet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.DirectionalProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinTag;

public class BlockTransformExtent extends ResettableExtent {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final Set<PropertyKey> directional = PropertyKeySet.of(
      PropertyKey.HALF,
      PropertyKey.ROTATION,
      PropertyKey.AXIS,
      PropertyKey.FACING,
      PropertyKey.FACE,
      PropertyKey.SHAPE,
      PropertyKey.NORTH,
      PropertyKey.EAST,
      PropertyKey.SOUTH,
      PropertyKey.WEST,
      PropertyKey.ORIENTATION
   );
   private static final Map<Direction, PropertyKey> directionMap = ImmutableMap.of(
      Direction.NORTH, PropertyKey.NORTH, Direction.EAST, PropertyKey.EAST, Direction.SOUTH, PropertyKey.SOUTH, Direction.WEST, PropertyKey.WEST
   );
   private final int[] ALL = new int[0];
   private Transform transform;
   private Transform transformInverse;
   private int[] BLOCK_ROTATION_BITMASK;
   private int[][] BLOCK_TRANSFORM;
   private int[][] BLOCK_TRANSFORM_INVERSE;

   public BlockTransformExtent(Extent parent) {
      this(parent, new AffineTransform());
   }

   public BlockTransformExtent(Extent extent, Transform transform) {
      super(extent);
      Preconditions.checkNotNull(transform);
      this.transform = transform;
      this.transformInverse = this.transform.inverse();
      this.cache();
   }

   private static long combine(Direction... directions) {
      long mask = 0L;

      for (Direction dir : directions) {
         mask |= 1L << dir.ordinal();
      }

      return mask;
   }

   private static long[] adapt(Direction... dirs) {
      long[] arr = new long[dirs.length];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = 1L << dirs[i].ordinal();
      }

      return arr;
   }

   private static long[] adapt(Long... dirs) {
      long[] arr = new long[dirs.length];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = dirs[i];
      }

      return arr;
   }

   private static long[] getDirections(AbstractProperty<?> property) {
      if (property instanceof DirectionalProperty dir) {
         return adapt(dir.getValues().toArray(new Direction[0]));
      } else {
         List<?> values = property.getValues();
         PropertyKey key = property.getKey();
         String var4 = key.getName().toLowerCase();
         switch (var4) {
            case "half":
               return adapt(Direction.UP, Direction.DOWN);
            case "type":
               return adapt(combine(Direction.UP), combine(Direction.DOWN), 0L);
            case "rotation":
               List<Direction> directions = new ArrayList<>();

               for (Object value : values) {
                  directions.add(Direction.fromRotationIndex((Integer)value).orElseThrow());
               }

               return adapt(directions.toArray(new Direction[0]));
            case "axis":
               return switch (property.getValues().size()) {
                  case 2 -> adapt(combine(Direction.EAST, Direction.WEST), combine(Direction.SOUTH, Direction.NORTH));
                  case 3 -> adapt(combine(Direction.EAST, Direction.WEST), combine(Direction.UP, Direction.DOWN), combine(Direction.SOUTH, Direction.NORTH));
                  default -> {
                     LOGGER.error("Invalid {} {}", property.getName(), property.getValues());
                     yield null;
                  }
               };
            case "facing":
               List<Direction> directions = new ArrayList<>();

               for (Object value : values) {
                  directions.add(Direction.valueOf(value.toString().toUpperCase(Locale.ROOT)));
               }

               return adapt(directions.toArray(new Direction[0]));
            case "face":
               if (values.size() == 3) {
                  return adapt(combine(Direction.UP), combine(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST), combine(Direction.DOWN));
               }

               return null;
            case "hinge":
               return adapt(
                  combine(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST),
                  combine(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHEAST, Direction.SOUTHWEST)
               );
            case "shape":
               if (values.contains("left")) {
                  return adapt(combine(Direction.EAST, Direction.WEST), combine(Direction.NORTH, Direction.SOUTH));
               } else {
                  if (values.contains("straight")) {
                     ArrayList<Long> result = new ArrayList<>();

                     for (Object value : values) {
                        String var24 = value.toString();
                        switch (var24) {
                           case "straight":
                              result.add(combine(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
                              break;
                           case "inner_left":
                           case "inner_right":
                              result.add(
                                 orIndex(
                                    combine(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST),
                                    property.getIndexFor("outer_right"),
                                    property.getIndexFor("outer_left")
                                 )
                              );
                              break;
                           case "outer_left":
                           case "outer_right":
                              result.add(
                                 orIndex(
                                    combine(Direction.NORTHEAST, Direction.NORTHWEST, Direction.SOUTHWEST, Direction.SOUTHEAST),
                                    property.getIndexFor("inner_left"),
                                    property.getIndexFor("inner_right")
                                 )
                              );
                              break;
                           default:
                              LOGGER.warn("Unknown direction {}", value);
                              result.add(0L);
                        }
                     }

                     return adapt(result.toArray(new Long[0]));
                  }

                  List<Long> directions = new ArrayList<>();

                  for (Object value : values) {
                     String var23 = value.toString();
                     switch (var23) {
                        case "north_south":
                           directions.add(combine(Direction.NORTH, Direction.SOUTH));
                           break;
                        case "east_west":
                           directions.add(combine(Direction.EAST, Direction.WEST));
                           break;
                        case "ascending_east":
                           directions.add(combine(Direction.ASCENDING_EAST));
                           break;
                        case "ascending_west":
                           directions.add(combine(Direction.ASCENDING_WEST));
                           break;
                        case "ascending_north":
                           directions.add(combine(Direction.ASCENDING_NORTH));
                           break;
                        case "ascending_south":
                           directions.add(combine(Direction.ASCENDING_SOUTH));
                           break;
                        case "south_east":
                           directions.add(combine(Direction.SOUTHEAST));
                           break;
                        case "south_west":
                           directions.add(combine(Direction.SOUTHWEST));
                           break;
                        case "north_west":
                           directions.add(combine(Direction.NORTHWEST));
                           break;
                        case "north_east":
                           directions.add(combine(Direction.NORTHEAST));
                           break;
                        default:
                           LOGGER.warn("Unknown direction {}", value);
                           directions.add(0L);
                     }
                  }

                  return adapt(directions.toArray(new Long[0]));
               }
            case "orientiation":
               List<Long> directions = new ArrayList<>();

               for (Object value : values) {
                  String var9 = value.toString();
                  switch (var9) {
                     case "ascending_east":
                        directions.add(combine(Direction.ASCENDING_EAST));
                        break;
                     case "ascending_west":
                        directions.add(combine(Direction.ASCENDING_WEST));
                        break;
                     case "ascending_north":
                        directions.add(combine(Direction.ASCENDING_NORTH));
                        break;
                     case "ascending_south":
                        directions.add(combine(Direction.ASCENDING_SOUTH));
                        break;
                     case "descending_east":
                        directions.add(combine(Direction.DESCENDING_EAST));
                        break;
                     case "descending_west":
                        directions.add(combine(Direction.DESCENDING_WEST));
                        break;
                     case "descending_north":
                        directions.add(combine(Direction.DESCENDING_NORTH));
                        break;
                     case "descending_south":
                        directions.add(combine(Direction.DESCENDING_SOUTH));
                  }
               }

               return adapt(directions.toArray(new Long[0]));
            default:
               return null;
         }
      }
   }

   private static boolean hasDirection(long mask, Direction dir) {
      return (mask & 1L << dir.ordinal()) != 0L;
   }

   private static long orIndex(long mask, int... indexes) {
      for (int index : indexes) {
         mask |= 1L << index + Direction.values().length;
      }

      return mask;
   }

   private static boolean hasIndex(long mask, int index) {
      return (mask >> Direction.values().length & 1L << index) == 0L;
   }

   @Nullable
   private static Integer getNewStateIndex(Transform transform, long[] directions, int oldIndex) {
      long oldDirMask = directions[oldIndex];
      if (oldDirMask == 0L) {
         return null;
      } else {
         Integer newIndex = null;

         for (Direction oldDirection : Direction.values()) {
            if (hasDirection(oldDirMask, oldDirection)) {
               Vector3 oldVector = oldDirection.toVector();
               Vector3 newVector = transform.apply(oldVector).subtract(transform.apply(Vector3.ZERO)).normalize();
               boolean flip = false;
               if (transform instanceof AffineTransform) {
                  flip = ((AffineTransform)transform).isScaled(oldVector);
               }

               if (flip || !oldVector.equalsFuzzy(newVector)) {
                  double closest = oldVector.normalize().dot(newVector);

                  for (int i = 0; i < directions.length; i++) {
                     int j = (oldIndex + i) % directions.length;
                     long newDirMask = directions[j];
                     if (hasIndex(oldDirMask, j)) {
                        for (Direction v : Direction.values()) {
                           if (hasDirection(newDirMask, v)) {
                              double dot = v.toVector().normalize().dot(newVector);
                              if (dot > closest || flip && dot >= closest) {
                                 closest = dot;
                                 newIndex = j;
                              }
                           }
                        }
                     }
                  }

                  if (newIndex != null) {
                     return newIndex;
                  }
               }
            }
         }

         return newIndex;
      }
   }

   private static boolean isDirectional(Property<?> property) {
      if (property instanceof DirectionalProperty) {
         return true;
      } else if (directional.contains(property.getKey())) {
         return true;
      } else {
         List<?> values = property.getValues();
         return values.contains("top") || values.contains("left");
      }
   }

   private static BaseBlock transformBaseBlockNBT(BlockState transformed, @Nonnull LinCompoundTag tag, Transform transform) {
      Map<String, LinTag<?>> value = tag.value();
      if (value.get("Rot") instanceof LinNumberTag<?> rotTag) {
         int rot = rotTag.value().intValue();
         Direction direction = MCDirections.fromRotation(rot);
         if (direction != null) {
            Vector3 applyAbsolute = transform.apply(direction.toVector());
            Vector3 applyOrigin = transform.apply(Vector3.ZERO);
            applyAbsolute.mutX(applyAbsolute.x() - applyOrigin.x());
            applyAbsolute.mutY(applyAbsolute.y() - applyOrigin.y());
            applyAbsolute.mutZ(applyAbsolute.z() - applyOrigin.z());
            Direction newDirection = Direction.findClosest(applyAbsolute, Direction.Flag.CARDINAL | Direction.Flag.ORDINAL | Direction.Flag.SECONDARY_ORDINAL);
            if (newDirection != null) {
               LinCompoundTag.Builder builder = tag.toBuilder();
               builder.putByte("Rot", (byte)MCDirections.toRotation(newDirection));
               return transformed.toBaseBlock(builder.build());
            }
         }
      }

      return transformed.toBaseBlock(tag);
   }

   private static int transformState(BlockState state, Transform transform) {
      int newMaskedId = state.getInternalId();
      BlockType type = state.getBlockType();
      if (type.hasProperty(PropertyKey.NORTH)
         && type.hasProperty(PropertyKey.EAST)
         && type.hasProperty(PropertyKey.SOUTH)
         && type.hasProperty(PropertyKey.WEST)) {
         BlockState tmp = state;

         for (Entry<Direction, PropertyKey> entry : directionMap.entrySet()) {
            Direction newDir = Direction.findClosest(transform.apply(entry.getKey().toVector()), Direction.Flag.CARDINAL);
            if (newDir != null) {
               Object dirState = state.getState(entry.getValue());
               tmp = tmp.with(directionMap.get(newDir), dirState);
            }
         }

         newMaskedId = tmp.getInternalId();
      }

      for (AbstractProperty<?> property : type.getProperties()) {
         if (isDirectional(property)) {
            long[] directions = getDirections(property);
            if (directions != null) {
               int oldIndex = property.getIndex(state.getInternalId());
               if (oldIndex >= directions.length) {
                  if (Settings.settings().ENABLED_COMPONENTS.DEBUG) {
                     LOGGER.warn(
                        String.format("Index outside direction array length found for block:{%s} property:{%s}", state.getBlockType().id(), property.getName())
                     );
                  }
               } else {
                  Integer newIndex = getNewStateIndex(transform, directions, oldIndex);
                  if (newIndex != null) {
                     newMaskedId = property.modifyIndex(newMaskedId, newIndex);
                  }
               }
            }
         }
      }

      return newMaskedId;
   }

   private void cache() {
      this.BLOCK_ROTATION_BITMASK = new int[BlockTypes.size()];
      this.BLOCK_TRANSFORM = new int[BlockTypes.size()][];
      this.BLOCK_TRANSFORM_INVERSE = new int[BlockTypes.size()][];

      for (int i = 0; i < this.BLOCK_TRANSFORM.length; i++) {
         this.BLOCK_TRANSFORM[i] = this.ALL;
         this.BLOCK_TRANSFORM_INVERSE[i] = this.ALL;
         BlockType type = BlockTypes.get(i);
         int bitMask = 0;

         for (AbstractProperty<?> property : type.getProperties()) {
            if (isDirectional(property)) {
               this.BLOCK_TRANSFORM[i] = null;
               this.BLOCK_TRANSFORM_INVERSE[i] = null;
               bitMask |= property.getBitMask();
            }
         }

         if (bitMask != 0) {
            this.BLOCK_ROTATION_BITMASK[i] = bitMask;
         }
      }
   }

   public Transform getTransform() {
      return this.transform;
   }

   private <T extends BlockStateHolder<T>> T transformBlock(T block, boolean reverse) {
      return transform(block, reverse ? this.transform.inverse() : this.transform);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.transformBlock(super.getBlock(position), false).toImmutableState();
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.transformBlock(super.getBlock(x, y, z), false).toImmutableState();
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.transformBlock(super.getFullBlock(position), false).toBaseBlock();
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.transformBlock(super.getFullBlock(x, y, z), false).toBaseBlock();
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      return super.setBlock(location, this.transformInverse(block));
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return super.setBlock(x, y, z, (T)this.transformInverse(block));
   }

   public void setTransform(Transform affine) {
      this.transform = affine;
      this.transformInverse = this.transform.inverse();
      this.cache();
   }

   public static <B extends BlockStateHolder<B>> B transform(@Nonnull B block, @Nonnull Transform transform) {
      BlockState state = block.toImmutableState();
      int transformedId = transformState(state, transform);
      BlockState transformed = BlockState.getFromInternalId(transformedId);
      boolean baseBlock = block instanceof BaseBlock;
      if (baseBlock && block.getNbt() != null) {
         return (B)transformBaseBlockNBT(transformed, block.getNbt(), transform);
      } else {
         return (B)(baseBlock ? transformed.toBaseBlock() : transformed);
      }
   }

   private BlockState transform(BlockState state, int[][] transformArray, Transform transform) {
      int typeId = state.getInternalBlockTypeId();
      int[] arr = transformArray[typeId];
      if (arr == this.ALL) {
         return state;
      } else {
         if (arr == null) {
            arr = transformArray[typeId] = new int[state.getBlockType().getMaxStateId() + 1];
            Arrays.fill(arr, -1);
         }

         int mask = this.BLOCK_ROTATION_BITMASK[typeId];
         int internalId = state.getInternalId();
         int maskedId = internalId & mask;
         int newMaskedId = arr[maskedId >> BlockTypesCache.BIT_OFFSET];
         if (newMaskedId != -1) {
            return BlockState.getFromInternalId(newMaskedId | internalId & ~mask);
         } else {
            newMaskedId = transformState(state, transform);
            arr[maskedId >> BlockTypesCache.BIT_OFFSET] = newMaskedId & mask;
            return BlockState.getFromInternalId(newMaskedId);
         }
      }
   }

   public final BaseBlock transform(BlockStateHolder<BaseBlock> block) {
      BlockState transformed = this.transform(block.toImmutableState());
      return block.getNbt() != null ? transformBaseBlockNBT(transformed, block.getNbt(), this.transform) : transformed.toBaseBlock();
   }

   protected final BlockStateHolder transformInverse(BlockStateHolder block) {
      BlockState transformed = this.transformInverse(block.toImmutableState());
      return (BlockStateHolder)(block.getNbt() != null ? transformBaseBlockNBT(transformed, block.getNbt(), this.transformInverse) : transformed);
   }

   public final BlockState transform(BlockState block) {
      return this.transform(block, this.BLOCK_TRANSFORM, this.transform);
   }

   private BlockState transformInverse(BlockState block) {
      return this.transform(block, this.BLOCK_TRANSFORM_INVERSE, this.transformInverse);
   }
}
