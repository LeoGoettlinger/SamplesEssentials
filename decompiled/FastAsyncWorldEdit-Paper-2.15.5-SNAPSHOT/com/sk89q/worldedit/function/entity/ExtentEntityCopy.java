package com.sk89q.worldedit.function.entity;

import com.fastasyncworldedit.core.util.TaskManager;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.entity.EntityTypes;
import java.util.UUID;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

public class ExtentEntityCopy implements EntityFunction {
   private final Extent source;
   private final Extent destination;
   private final Vector3 from;
   private final Vector3 to;
   private final Transform transform;
   private boolean removing;

   @Deprecated
   public ExtentEntityCopy(Vector3 from, Extent destination, Vector3 to, Transform transform) {
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(destination);
      Preconditions.checkNotNull(to);
      Preconditions.checkNotNull(transform);
      this.source = null;
      this.destination = destination;
      this.from = from;
      this.to = to;
      this.transform = transform;
   }

   public ExtentEntityCopy(Extent source, Vector3 from, Extent destination, Vector3 to, Transform transform) {
      Preconditions.checkNotNull(source);
      Preconditions.checkNotNull(from);
      Preconditions.checkNotNull(destination);
      Preconditions.checkNotNull(to);
      Preconditions.checkNotNull(transform);
      this.source = source;
      this.destination = destination;
      this.from = from;
      this.to = to;
      this.transform = transform;
   }

   public boolean isRemoving() {
      return this.removing;
   }

   public void setRemoving(boolean removing) {
      this.removing = removing;
   }

   @Override
   public boolean apply(Entity entity) throws WorldEditException {
      BaseEntity state = entity.getState();
      if (state != null && state.getType() != EntityTypes.PLAYER) {
         Location location = entity.getLocation();
         LinCompoundTag tag = state.getNbt();
         boolean hasTilePosition = false;
         if (tag != null) {
            if (tag.value().get("block_pos") instanceof LinIntArrayTag blockPos && blockPos.value().length == 3) {
               location = location.setPosition(Vector3.at(blockPos.value()[0], blockPos.value()[1], blockPos.value()[2]).add(0.5, 0.5, 0.5));
               hasTilePosition = true;
            } else if (tag.value().get("TileX") instanceof LinNumberTag<?> tagX
               && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
               && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ) {
               location = location.setPosition(Vector3.at(tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue()).add(0.5, 0.5, 0.5));
               hasTilePosition = true;
            }
         }

         Vector3 pivot = this.from.round().add(0.5, 0.5, 0.5);
         Vector3 newPosition = this.transform.apply(location.toVector().subtract(pivot));
         if (hasTilePosition) {
            newPosition = newPosition.subtract(0.5, 0.5, 0.5);
         }

         Vector3 newDirection = this.transform.isIdentity()
            ? entity.getLocation().getDirection()
            : this.transform.apply(location.getDirection()).subtract(this.transform.apply(Vector3.ZERO)).normalize();
         Location newLocation = new Location(this.destination, newPosition.add(this.to.round().add(0.5, 0.5, 0.5)), newDirection);
         state = this.transformNbtData(state);
         boolean success = this.destination.createEntity(newLocation, state) != null;
         if (this.isRemoving() && success) {
            UUID uuid = entity.getState().getUUID();
            if (uuid != null) {
               if (this.source != null) {
                  this.source.removeEntity(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ(), uuid);
               } else {
                  TaskManager.taskManager().sync(entity::remove);
               }
            }
         }

         return success;
      } else {
         return false;
      }
   }

   private BaseEntity transformNbtData(BaseEntity state) {
      LinCompoundTag tag = state.getNbt();
      if (tag != null) {
         LinCompoundTag leashCompound = tag.findTag("Leash", LinTagType.compoundTag());
         if (leashCompound != null
            && tag.value().get("X") instanceof LinNumberTag<?> tagX
            && tag.value().get("Y") instanceof LinNumberTag<?> tagY
            && tag.value().get("Z") instanceof LinNumberTag<?> tagZ) {
            Vector3 tilePosition = Vector3.at(tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue());
            BlockVector3 newLeash = this.transform.apply(tilePosition.subtract(this.from)).add(this.to).toBlockPoint();
            return new BaseEntity(
               state.getType(),
               LazyReference.computed(
                  tag.toBuilder()
                     .put("Leash", leashCompound.toBuilder().putInt("X", newLeash.x()).putInt("Y", newLeash.y()).putInt("Z", newLeash.z()).build())
                     .build()
               )
            );
         }

         if (tag.value().containsKey("TileX") && tag.value().containsKey("TileY") && tag.value().containsKey("TileZ")) {
            boolean var58 = true;
         } else {
            boolean var10000 = false;
         }

         LinTag<?> rotation = tag.value().get("Rotation");
         Vector3 tilePosition = null;
         if (tag.value().get("block_pos") instanceof LinIntArrayTag blockPos) {
            tilePosition = Vector3.at(blockPos.value()[0], blockPos.value()[1], blockPos.value()[2]);
         }

         if (tag.value().get("TileX") instanceof LinNumberTag<?> tagX
            && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
            && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ) {
            tilePosition = Vector3.at(tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue());
         }

         if (tilePosition != null) {
            BlockVector3 newTilePosition = this.transform.apply(tilePosition.subtract(this.from)).add(this.to).toBlockPoint();
            LinCompoundTag.Builder builder = tag.toBuilder();
            if (WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion() < 4325) {
               builder.putInt("TileX", newTilePosition.x()).putInt("TileY", newTilePosition.y()).putInt("TileZ", newTilePosition.z());
            } else {
               builder.putIntArray("block_pos", new int[]{newTilePosition.x(), newTilePosition.y(), newTilePosition.z()});
            }

            ExtentEntityCopy.FacingTagData newDirection = tryGetFacingData(tag);
            if (newDirection instanceof ExtentEntityCopy.FacingTagData(String facingVector, LinNumberTag var62)) {
               LinNumberTag var52 = var62;
               if (state.getType() == EntityTypes.PAINTING) {
                  Direction direction = MCDirections.fromHorizontalHanging(((Number)var52.value()).intValue());
                  Vector3 vector = this.transform.apply(direction.toVector()).subtract(this.transform.apply(Vector3.ZERO)).normalize();
                  Direction newDirectionx = Direction.findClosest(vector, Direction.Flag.CARDINAL);
                  byte facingValue = (byte)MCDirections.toHorizontalHanging(newDirectionx);
                  builder.putByte(facingVector, facingValue);
               } else {
                  Direction facingDirection = MCDirections.fromHanging(((Number)var52.value()).intValue());
                  Vector3 facingVectorx = this.transform.apply(facingDirection.toVector()).subtract(this.transform.apply(Vector3.ZERO)).normalize();
                  Direction newFacingDirection = Direction.findClosest(facingVectorx, Direction.Flag.CARDINAL | Direction.Flag.UPRIGHT);
                  byte facingValue = (byte)MCDirections.toHanging(newFacingDirection);
                  builder.putByte(facingVector, facingValue);
                  String itemRotationKey = "ItemRotation";
                  if (!this.transform.isIdentity() && tag.value().get(itemRotationKey) instanceof LinByteTag tagItemRotation) {
                     String itemId = this.getItemInItemFrame(tag);
                     int availableRotations = itemId != null && itemId.equals("minecraft:filled_map") ? 4 : 8;
                     Direction rotationBaseDirection = facingDirection != Direction.UP && facingDirection != Direction.DOWN ? Direction.UP : Direction.NORTH;
                     int itemRotation = tagItemRotation.value().intValue();
                     Vector3 rotationVector = this.getItemRotationVector(rotationBaseDirection, facingDirection.toVector(), itemRotation, availableRotations);
                     Vector3 newRotationVector = this.transform.apply(rotationVector);
                     Direction newRotationBaseDirection = newFacingDirection != Direction.UP && newFacingDirection != Direction.DOWN
                        ? Direction.UP
                        : Direction.NORTH;
                     byte newItemRotation = (byte)this.getItemRotationSteps(newRotationBaseDirection, newFacingDirection, newRotationVector, availableRotations);
                     builder.putByte(itemRotationKey, newItemRotation);
                  }
               }
            }

            if (rotation instanceof LinListTag<?> rotationTag) {
               LinListTag<LinFloatTag> orgrot = rotationTag.asTypeChecked(LinTagType.floatTag());
               Vector3 orgDirection = new Location(this.destination, 0.0, 0.0, 0.0, orgrot.get(0).valueAsFloat(), orgrot.get(1).valueAsFloat()).getDirection();
               Vector3 newDirectionx = this.transform.apply(orgDirection).subtract(this.transform.apply(Vector3.ZERO)).normalize();
               builder.put(
                  "Rotation",
                  LinListTag.builder(LinTagType.floatTag())
                     .add(LinFloatTag.of((float)newDirectionx.toYaw()))
                     .add(LinFloatTag.of((float)newDirectionx.toPitch()))
                     .build()
               );
            }

            return new BaseEntity(state.getType(), LazyReference.computed(builder.build()));
         }

         if (rotation instanceof LinListTag<?> rotationTag) {
            LinCompoundTag.Builder builderx = tag.toBuilder();
            LinListTag<LinFloatTag> orgrot = rotationTag.asTypeChecked(LinTagType.floatTag());
            Vector3 orgDirection = new Location(this.destination, 0.0, 0.0, 0.0, orgrot.get(0).valueAsFloat(), orgrot.get(1).valueAsFloat()).getDirection();
            Vector3 newDirectionx = this.transform.apply(orgDirection).subtract(this.transform.apply(Vector3.ZERO)).normalize();
            builderx.put(
               "Rotation",
               LinListTag.builder(LinTagType.floatTag())
                  .add(LinFloatTag.of((float)newDirectionx.toYaw()))
                  .add(LinFloatTag.of((float)newDirectionx.toPitch()))
                  .build()
            );
            return new BaseEntity(state.getType(), LazyReference.computed(builderx.build()));
         }
      }

      return state;
   }

   private Vector3 getItemRotationVector(Direction baseDirection, Vector3 facingVector, int itemRotation, int rotations) {
      Vector3 baseVec = baseDirection.toVector().normalize();
      double angle = Math.toRadians(itemRotation * (360.0F / rotations));
      Vector3 rotated = this.rotateAroundAxis(baseVec, facingVector, -angle);
      return rotated.normalize();
   }

   private Vector3 rotateAroundAxis(Vector3 vec, Vector3 axis, double angle) {
      axis = axis.normalize();
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      return vec.multiply(cos).add(axis.cross(vec).multiply(sin)).add(axis.multiply(axis.dot(vec) * (1.0 - cos)));
   }

   private int getItemRotationSteps(Direction baseDirection, Direction facingDirection, Vector3 targetVector, int rotations) {
      Vector3 baseVec = baseDirection.toVector();
      double det = facingDirection.toVector().dot(baseVec.cross(targetVector));
      double dot = baseVec.dot(targetVector);
      double signedAngle = Math.atan2(det, dot);
      double stepsDouble = -signedAngle / (Math.PI / (rotations / 2.0F));
      int steps = (int)Math.round(stepsDouble) % rotations;
      if (steps < 0) {
         steps += rotations;
      }

      if (facingDirection == Direction.DOWN) {
         steps = (steps + rotations / 2) % rotations;
      }

      return steps;
   }

   private String getItemInItemFrame(LinCompoundTag tag) {
      return tag.value().get("Item") instanceof LinCompoundTag tagItem && tagItem.value().get("id") instanceof LinStringTag tagId ? tagId.value() : null;
   }

   private static ExtentEntityCopy.FacingTagData tryGetFacingData(LinCompoundTag tag) {
      if (tag.value().get("Facing") instanceof LinNumberTag<?> tagFacingCapital) {
         return new ExtentEntityCopy.FacingTagData("Facing", tagFacingCapital);
      } else {
         return tag.value().get("facing") instanceof LinNumberTag<?> tagFacingLower ? new ExtentEntityCopy.FacingTagData("facing", tagFacingLower) : null;
      }
   }

   private record FacingTagData(String facingKey, LinNumberTag<?> tagFacing) {
   }
}
