package ac.grim.grimac.utils.data;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;

public class ReachInterpolationData {
   private final SimpleCollisionBox targetLocation;
   private final GrimPlayer player;
   private final PacketEntity entity;
   public SimpleCollisionBox startingLocation;
   private int interpolationStepsLowBound = 0;
   private int interpolationStepsHighBound = 0;
   private int interpolationSteps = 1;
   private boolean expandNonRelative = false;

   public ReachInterpolationData(GrimPlayer player, SimpleCollisionBox startingLocation, TrackedPosition position, PacketEntity entity) {
      boolean unreliableTicking = !player.inVehicle() && player.canSkipTicks();
      this.startingLocation = startingLocation;
      Vector3d pos = position.getPos();
      this.targetLocation = new SimpleCollisionBox(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z, false);
      this.player = player;
      this.entity = entity;
      if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)
         && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)) {
         this.targetLocation.expand(0.03125);
      }

      if (entity.isBoat) {
         this.interpolationSteps = 10;
      } else if (entity.isMinecart) {
         this.interpolationSteps = 5;
      } else if (entity.getType() == EntityTypes.SHULKER) {
         this.interpolationSteps = 1;
      } else if (entity.isLivingEntity) {
         this.interpolationSteps = 3;
      } else {
         this.interpolationSteps = 1;
      }

      if (unreliableTicking) {
         this.interpolationStepsHighBound = this.getInterpolationSteps();
      }
   }

   public ReachInterpolationData(GrimPlayer player, SimpleCollisionBox finishedLoc, PacketEntity entity) {
      this.startingLocation = finishedLoc;
      this.targetLocation = finishedLoc;
      this.entity = entity;
      this.player = player;
   }

   public static SimpleCollisionBox combineCollisionBox(SimpleCollisionBox one, SimpleCollisionBox two) {
      double minX = Math.min(one.minX, two.minX);
      double maxX = Math.max(one.maxX, two.maxX);
      double minY = Math.min(one.minY, two.minY);
      double maxY = Math.max(one.maxY, two.maxY);
      double minZ = Math.min(one.minZ, two.minZ);
      double maxZ = Math.max(one.maxZ, two.maxZ);
      return new SimpleCollisionBox(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public static CollisionBox getOverlapHitbox(CollisionBox b1, CollisionBox b2) {
      if (b1 == NoCollisionBox.INSTANCE || b2 == NoCollisionBox.INSTANCE) {
         return NoCollisionBox.INSTANCE;
      } else if (b1 instanceof SimpleCollisionBox && b2 instanceof SimpleCollisionBox) {
         SimpleCollisionBox box1 = (SimpleCollisionBox)b1;
         SimpleCollisionBox box2 = (SimpleCollisionBox)b2;
         double overlapMinX = Math.max(box1.minX, box2.minX);
         double overlapMaxX = Math.min(box1.maxX, box2.maxX);
         double overlapMinY = Math.max(box1.minY, box2.minY);
         double overlapMaxY = Math.min(box1.maxY, box2.maxY);
         double overlapMinZ = Math.max(box1.minZ, box2.minZ);
         double overlapMaxZ = Math.min(box1.maxZ, box2.maxZ);
         return (CollisionBox)(!(overlapMinX > overlapMaxX) && !(overlapMinY > overlapMaxY) && !(overlapMinZ > overlapMaxZ)
            ? new SimpleCollisionBox(overlapMinX, overlapMinY, overlapMinZ, overlapMaxX, overlapMaxY, overlapMaxZ)
            : NoCollisionBox.INSTANCE);
      } else {
         throw new IllegalArgumentException("Both b1 and b2 must be SimpleCollisionBox instances");
      }
   }

   private int getInterpolationSteps() {
      return this.interpolationSteps;
   }

   public SimpleCollisionBox getPossibleLocationCombined() {
      int interpSteps = this.getInterpolationSteps();
      double stepMinX = (this.targetLocation.minX - this.startingLocation.minX) / interpSteps;
      double stepMaxX = (this.targetLocation.maxX - this.startingLocation.maxX) / interpSteps;
      double stepMinY = (this.targetLocation.minY - this.startingLocation.minY) / interpSteps;
      double stepMaxY = (this.targetLocation.maxY - this.startingLocation.maxY) / interpSteps;
      double stepMinZ = (this.targetLocation.minZ - this.startingLocation.minZ) / interpSteps;
      double stepMaxZ = (this.targetLocation.maxZ - this.startingLocation.maxZ) / interpSteps;
      double loMinX = this.startingLocation.minX + this.interpolationStepsLowBound * stepMinX;
      double loMinY = this.startingLocation.minY + this.interpolationStepsLowBound * stepMinY;
      double loMinZ = this.startingLocation.minZ + this.interpolationStepsLowBound * stepMinZ;
      double loMaxX = this.startingLocation.maxX + this.interpolationStepsLowBound * stepMaxX;
      double loMaxY = this.startingLocation.maxY + this.interpolationStepsLowBound * stepMaxY;
      double loMaxZ = this.startingLocation.maxZ + this.interpolationStepsLowBound * stepMaxZ;
      double hiMinX = this.startingLocation.minX + this.interpolationStepsHighBound * stepMinX;
      double hiMinY = this.startingLocation.minY + this.interpolationStepsHighBound * stepMinY;
      double hiMinZ = this.startingLocation.minZ + this.interpolationStepsHighBound * stepMinZ;
      double hiMaxX = this.startingLocation.maxX + this.interpolationStepsHighBound * stepMaxX;
      double hiMaxY = this.startingLocation.maxY + this.interpolationStepsHighBound * stepMaxY;
      double hiMaxZ = this.startingLocation.maxZ + this.interpolationStepsHighBound * stepMaxZ;
      return new SimpleCollisionBox(
         Math.min(loMinX, hiMinX),
         Math.min(loMinY, hiMinY),
         Math.min(loMinZ, hiMinZ),
         Math.max(loMaxX, hiMaxX),
         Math.max(loMaxY, hiMaxY),
         Math.max(loMaxZ, hiMaxZ)
      );
   }

   public SimpleCollisionBox getPossibleHitboxCombined() {
      SimpleCollisionBox minimumInterpLocation = this.getPossibleLocationCombined();
      if (this.expandNonRelative) {
         minimumInterpLocation.expand(0.03125, 0.015625, 0.03125);
      }

      GetBoundingBox.expandBoundingBoxByEntityDimensions(minimumInterpLocation, this.player, this.entity);
      return minimumInterpLocation;
   }

   public CollisionBox getOverlapHitboxCombined() {
      int interpSteps = this.getInterpolationSteps();
      double stepMinX = (this.targetLocation.minX - this.startingLocation.minX) / interpSteps;
      double stepMaxX = (this.targetLocation.maxX - this.startingLocation.maxX) / interpSteps;
      double stepMinY = (this.targetLocation.minY - this.startingLocation.minY) / interpSteps;
      double stepMaxY = (this.targetLocation.maxY - this.startingLocation.maxY) / interpSteps;
      double stepMinZ = (this.targetLocation.minZ - this.startingLocation.minZ) / interpSteps;
      double stepMaxZ = (this.targetLocation.maxZ - this.startingLocation.maxZ) / interpSteps;
      double overallMinX = Double.NEGATIVE_INFINITY;
      double overallMaxX = Double.POSITIVE_INFINITY;
      double overallMinY = Double.NEGATIVE_INFINITY;
      double overallMaxY = Double.POSITIVE_INFINITY;
      double overallMinZ = Double.NEGATIVE_INFINITY;
      double overallMaxZ = Double.POSITIVE_INFINITY;
      boolean isFirstStep = true;

      for (int step = this.interpolationStepsLowBound; step <= this.interpolationStepsHighBound; step++) {
         double currentMinX = this.startingLocation.minX + step * stepMinX;
         double currentMaxX = this.startingLocation.maxX + step * stepMaxX;
         double currentMinY = this.startingLocation.minY + step * stepMinY;
         double currentMaxY = this.startingLocation.maxY + step * stepMaxY;
         double currentMinZ = this.startingLocation.minZ + step * stepMinZ;
         double currentMaxZ = this.startingLocation.maxZ + step * stepMaxZ;
         SimpleCollisionBox[] cornerBoxes = new SimpleCollisionBox[]{
            new SimpleCollisionBox(currentMinX, currentMinY, currentMinZ, currentMinX, currentMinY, currentMinZ),
            new SimpleCollisionBox(currentMaxX, currentMinY, currentMinZ, currentMaxX, currentMinY, currentMinZ),
            new SimpleCollisionBox(currentMinX, currentMinY, currentMaxZ, currentMinX, currentMinY, currentMaxZ),
            new SimpleCollisionBox(currentMaxX, currentMinY, currentMaxZ, currentMaxX, currentMinY, currentMaxZ)
         };

         for (SimpleCollisionBox cornerBox : cornerBoxes) {
            GetBoundingBox.expandBoundingBoxByEntityDimensions(cornerBox, this.player, this.entity);
         }

         CollisionBox stepOverlap = this.getOverlapOfBoxes(cornerBoxes);
         if (stepOverlap == NoCollisionBox.INSTANCE) {
            return NoCollisionBox.INSTANCE;
         }

         SimpleCollisionBox stepBox = (SimpleCollisionBox)stepOverlap;
         if (isFirstStep) {
            overallMinX = stepBox.minX;
            overallMaxX = stepBox.maxX;
            overallMinY = stepBox.minY;
            overallMaxY = stepBox.maxY;
            overallMinZ = stepBox.minZ;
            overallMaxZ = stepBox.maxZ;
            isFirstStep = false;
         } else {
            overallMinX = Math.max(overallMinX, stepBox.minX);
            overallMaxX = Math.min(overallMaxX, stepBox.maxX);
            overallMinY = Math.max(overallMinY, stepBox.minY);
            overallMaxY = Math.min(overallMaxY, stepBox.maxY);
            overallMinZ = Math.max(overallMinZ, stepBox.minZ);
            overallMaxZ = Math.min(overallMaxZ, stepBox.maxZ);
         }

         if (overallMinX > overallMaxX || overallMinY > overallMaxY || overallMinZ > overallMaxZ) {
            return NoCollisionBox.INSTANCE;
         }
      }

      return (CollisionBox)(!(overallMinX > overallMaxX) && !(overallMinY > overallMaxY) && !(overallMinZ > overallMaxZ)
         ? new SimpleCollisionBox(overallMinX, overallMinY, overallMinZ, overallMaxX, overallMaxY, overallMaxZ)
         : NoCollisionBox.INSTANCE);
   }

   private CollisionBox getOverlapOfBoxes(SimpleCollisionBox[] boxes) {
      double minX = Double.NEGATIVE_INFINITY;
      double maxX = Double.POSITIVE_INFINITY;
      double minY = Double.NEGATIVE_INFINITY;
      double maxY = Double.POSITIVE_INFINITY;
      double minZ = Double.NEGATIVE_INFINITY;
      double maxZ = Double.POSITIVE_INFINITY;
      boolean first = true;

      for (SimpleCollisionBox box : boxes) {
         if (first) {
            minX = box.minX;
            maxX = box.maxX;
            minY = box.minY;
            maxY = box.maxY;
            minZ = box.minZ;
            maxZ = box.maxZ;
            first = false;
         } else {
            minX = Math.max(minX, box.minX);
            maxX = Math.min(maxX, box.maxX);
            minY = Math.max(minY, box.minY);
            maxY = Math.min(maxY, box.maxY);
            minZ = Math.max(minZ, box.minZ);
            maxZ = Math.min(maxZ, box.maxZ);
         }

         if (minX > maxX || minY > maxY || minZ > maxZ) {
            return NoCollisionBox.INSTANCE;
         }
      }

      return new SimpleCollisionBox(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public void updatePossibleStartingLocation(SimpleCollisionBox possibleLocationCombined) {
      this.startingLocation = combineCollisionBox(this.startingLocation, possibleLocationCombined);
   }

   public void tickMovement(boolean incrementLowBound, boolean tickingReliably) {
      if (!tickingReliably) {
         this.interpolationStepsHighBound = this.getInterpolationSteps();
      }

      if (incrementLowBound) {
         this.interpolationStepsLowBound = Math.min(this.interpolationStepsLowBound + 1, this.getInterpolationSteps());
      }

      this.interpolationStepsHighBound = Math.min(this.interpolationStepsHighBound + 1, this.getInterpolationSteps());
   }

   @Override
   public String toString() {
      return "ReachInterpolationData{targetLocation="
         + this.targetLocation
         + ", startingLocation="
         + this.startingLocation
         + ", interpolationStepsLowBound="
         + this.interpolationStepsLowBound
         + ", interpolationStepsHighBound="
         + this.interpolationStepsHighBound
         + "}";
   }

   public void expandNonRelative() {
      this.expandNonRelative = true;
   }
}
