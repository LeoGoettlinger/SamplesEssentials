package ac.grim.grimac.predictionengine;

import ac.grim.grimac.checks.impl.combat.Reach;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngine;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.collisions.CollisionData;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.VelocityData;
import ac.grim.grimac.utils.data.tags.SyncedTags;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.BlockProperties;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.FluidTypeFlowing;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import ac.grim.grimac.utils.nmsutil.Materials;
import java.util.OptionalInt;
import java.util.Set;
import lombok.Generated;

public class PointThreeEstimator {
   private final GrimPlayer player;
   public boolean isNearFluid = false;
   private boolean headHitter = false;
   private boolean isNearClimbable = false;
   private boolean isGliding = false;
   private boolean gravityChanged = false;
   private boolean isNearHorizontalFlowingLiquid = false;
   private boolean isNearVerticalFlowingLiquid = false;
   private boolean isNearBubbleColumn = false;
   public boolean isNearNetherPortal = false;
   private int maxPositiveLevitation = Integer.MIN_VALUE;
   private int minNegativeLevitation = Integer.MAX_VALUE;
   private boolean isPushing = false;
   private boolean wasAlwaysCertain = true;

   public PointThreeEstimator(GrimPlayer player) {
      this.player = player;
   }

   public void handleChangeBlock(int x, int y, int z, WrappedBlockState state) {
      StateType stateType = state.getType();
      CollisionBox data = CollisionData.getData(stateType).getMovementCollisionBox(this.player, this.player.getClientVersion(), state, x, y, z);
      SimpleCollisionBox normalBox = GetBoundingBox.getBoundingBoxFromPosAndSize(this.player, this.player.x, this.player.y, this.player.z, 0.6F, 1.8F);
      double movementThreshold = this.player.getMovementThreshold();
      SimpleCollisionBox slightlyExpanded = normalBox.copy().expand(movementThreshold, 0.0, movementThreshold);
      if (!slightlyExpanded.isIntersected(data) && slightlyExpanded.offset(0.0, movementThreshold, 0.0).isIntersected(data)) {
         this.headHitter = true;
      }

      float collisionBoxThreshold = (float)(movementThreshold * 2.0);
      SimpleCollisionBox pointThreeBox = GetBoundingBox.getBoundingBoxFromPosAndSize(
         this.player, this.player.x, this.player.y - movementThreshold, this.player.z, 0.6F + collisionBoxThreshold, 1.8F + collisionBoxThreshold
      );
      if ((Materials.isWater(this.player.getClientVersion(), state) || stateType == StateTypes.LAVA)
         && pointThreeBox.isIntersected(new SimpleCollisionBox(x, y, z))) {
         if (stateType == StateTypes.BUBBLE_COLUMN && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            this.isNearBubbleColumn = true;
         }

         Vector3dm fluidVector = FluidTypeFlowing.getFlow(this.player, x, y, z);
         if (fluidVector.getX() != 0.0 || fluidVector.getZ() != 0.0) {
            this.isNearHorizontalFlowingLiquid = true;
         }

         if (fluidVector.getY() != 0.0) {
            this.isNearVerticalFlowingLiquid = true;
         }

         this.isNearFluid = true;
      }

      this.player.checkManager.getPacketCheck(Reach.class).handleBlockChange(new Vector3i(x, y, z), state);
      if (pointThreeBox.isIntersected(new SimpleCollisionBox(x, y, z))) {
         int controllingEntityId = this.player.inVehicle() ? this.player.getRidingVehicleId() : this.player.entityID;
         VelocityData oldFirstBreadKB = this.player.firstBreadKB;
         VelocityData oldLikelyKB = this.player.likelyKB;
         this.player.firstBreadKB = this.player
            .checkManager
            .getKnockbackHandler()
            .calculateFirstBreadKnockback(controllingEntityId, this.player.lastTransactionReceived.get());
         this.player.likelyKB = this.player
            .checkManager
            .getKnockbackHandler()
            .calculateRequiredKB(controllingEntityId, this.player.lastTransactionReceived.get(), true);
         VelocityData oldFirstBreadEx = this.player.firstBreadExplosion;
         VelocityData oldLikelyEx = this.player.likelyExplosions;
         this.player.firstBreadExplosion = this.player
            .checkManager
            .getExplosionHandler()
            .getFirstBreadAddedExplosion(this.player.lastTransactionReceived.get());
         this.player.likelyExplosions = this.player.checkManager.getExplosionHandler().getPossibleExplosions(this.player.lastTransactionReceived.get(), true);
         this.player.updateVelocityMovementSkipping();
         if (this.player.couldSkipTick) {
            this.player.uncertaintyHandler.lastPointThree.reset();
         } else {
            this.player.firstBreadKB = oldFirstBreadKB;
            this.player.likelyKB = oldLikelyKB;
            this.player.firstBreadExplosion = oldFirstBreadEx;
            this.player.likelyExplosions = oldLikelyEx;
         }
      }

      if (!this.player.inVehicle()
         && (
            stateType == StateTypes.POWDER_SNOW && this.player.inventory.getBoots().getType() == ItemTypes.LEATHER_BOOTS
               || this.player.tagManager.block(SyncedTags.CLIMBABLE).contains(stateType)
         )
         && pointThreeBox.isIntersected(new SimpleCollisionBox(x, y, z))) {
         this.isNearClimbable = true;
      }
   }

   public boolean canPredictNextVerticalMovement() {
      return !this.gravityChanged && this.maxPositiveLevitation == Integer.MIN_VALUE && this.minNegativeLevitation == Integer.MAX_VALUE;
   }

   public double positiveLevitation(double y) {
      return this.maxPositiveLevitation == Integer.MIN_VALUE ? y : 0.05 * (this.maxPositiveLevitation + 1) - y * 0.2;
   }

   public double negativeLevitation(double y) {
      return this.minNegativeLevitation == Integer.MAX_VALUE ? y : 0.05 * (this.minNegativeLevitation + 1) - y * 0.2;
   }

   public boolean controlsVerticalMovement() {
      return this.isNearFluid
         || this.isNearClimbable
         || this.isNearHorizontalFlowingLiquid
         || this.isNearVerticalFlowingLiquid
         || this.isNearBubbleColumn
         || this.isGliding
         || this.player.uncertaintyHandler.influencedByBouncyBlock()
         || this.player.checkManager.getKnockbackHandler().isKnockbackPointThree()
         || this.player.checkManager.getExplosionHandler().isExplosionPointThree();
   }

   public void updatePlayerPotions(PotionType potion, Integer level) {
      if (potion == PotionTypes.LEVITATION) {
         this.maxPositiveLevitation = Math.max(level == null ? Integer.MIN_VALUE : level, this.maxPositiveLevitation);
         this.minNegativeLevitation = Math.min(level == null ? Integer.MAX_VALUE : level, this.minNegativeLevitation);
      }
   }

   public void updatePlayerGliding() {
      this.isGliding = true;
   }

   public void updatePlayerGravity() {
      this.gravityChanged = true;
   }

   public void endOfTickTick() {
      double movementThreshold = this.player.getMovementThreshold();
      float collisionBoxThreshold = (float)(movementThreshold * 2.0);
      SimpleCollisionBox pointThreeBox = GetBoundingBox.getBoundingBoxFromPosAndSize(
         this.player, this.player.x, this.player.y - movementThreshold, this.player.z, 0.6F + collisionBoxThreshold, 1.8F + collisionBoxThreshold
      );
      SimpleCollisionBox oldBB = this.player.boundingBox;
      this.headHitter = false;

      for (float sizes : this.player.skippedTickInActualMovement ? new float[]{0.6F, 1.5F, 1.8F} : new float[]{this.player.pose.height}) {
         this.player.boundingBox = GetBoundingBox.getBoundingBoxFromPosAndSize(
            this.player, this.player.x, this.player.y + (sizes - 0.01F), this.player.z, 0.6F, 0.01F
         );
         this.headHitter = this.headHitter || Collisions.collide(this.player, 0.0, movementThreshold, 0.0).getY() != movementThreshold;
      }

      this.player.boundingBox = oldBB;
      this.checkNearbyBlocks(pointThreeBox);
      this.maxPositiveLevitation = Integer.MIN_VALUE;
      this.minNegativeLevitation = Integer.MAX_VALUE;
      this.isGliding = this.player.isGliding;
      this.gravityChanged = false;
      this.wasAlwaysCertain = true;
      this.isPushing = false;
   }

   private void checkNearbyBlocks(SimpleCollisionBox pointThreeBox) {
      this.isNearHorizontalFlowingLiquid = false;
      this.isNearVerticalFlowingLiquid = false;
      this.isNearClimbable = false;
      this.isNearBubbleColumn = false;
      this.isNearFluid = false;
      this.isNearNetherPortal = false;
      Collisions.hasMaterial(
         this.player,
         pointThreeBox,
         (block, x, y, z) -> {
            StateType stateType = block.getType();
            if (this.player.tagManager.block(SyncedTags.CLIMBABLE).contains(stateType)
               || stateType == StateTypes.POWDER_SNOW && !this.player.inVehicle() && this.player.inventory.getBoots().getType() == ItemTypes.LEATHER_BOOTS) {
               this.isNearClimbable = true;
            }

            if (BlockTags.TRAPDOORS.contains(stateType)) {
               this.isNearClimbable = this.isNearClimbable || Collisions.trapdoorUsableAsLadder(this.player, x, y, z, block);
            }

            if (stateType == StateTypes.BUBBLE_COLUMN && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
               this.isNearBubbleColumn = true;
            }

            if (Materials.isWater(this.player.getClientVersion(), block) || block.getType() == StateTypes.LAVA) {
               this.isNearFluid = true;
            }

            if (stateType == StateTypes.NETHER_PORTAL) {
               this.isNearNetherPortal = true;
            }

            Vector3dm fluidVector = FluidTypeFlowing.getFlow(this.player, x, y, z);
            if (fluidVector.getX() != 0.0 || fluidVector.getZ() != 0.0) {
               this.isNearHorizontalFlowingLiquid = true;
            }

            if (fluidVector.getY() != 0.0) {
               this.isNearVerticalFlowingLiquid = true;
            }

            return false;
         }
      );
   }

   public boolean closeEnoughToGroundToStepWithPointThree(VectorData data, double originalY) {
      if (this.player.inVehicle()) {
         return false;
      } else if (!this.player.isPointThree()) {
         return false;
      } else {
         return this.player.clientControlledVerticalCollision && data != null && data.isZeroPointZeroThree() ? this.checkForGround(originalY) : false;
      }
   }

   private boolean checkForGround(double y) {
      SimpleCollisionBox playerBox = this.player.boundingBox;
      double threshold = this.player.getMovementThreshold();
      this.player.boundingBox = this.player.boundingBox.copy().expand(threshold, 0.0, threshold).offset(0.0, threshold, 0.0);
      double searchDistance = -0.2 + Math.min(0.0, y);
      Vector3dm collisionResult = Collisions.collide(this.player, 0.0, searchDistance, 0.0);
      this.player.boundingBox = playerBox;
      return collisionResult.getY() != searchDistance;
   }

   public boolean determineCanSkipTick(float speed, Set<VectorData> init) {
      if (!this.player.canSkipTicks() && this.player.packetStateData.didLastMovementIncludePosition && !this.player.uncertaintyHandler.isSteppingOnSlime) {
         return false;
      } else {
         double minimum = Double.MAX_VALUE;
         if ((this.player.isGliding || this.player.wasGliding) && !this.player.packetStateData.didLastMovementIncludePosition) {
            return true;
         } else if (this.player.inVehicle()) {
            return false;
         } else if (!this.isNearClimbable()
            && !this.isPushing
            && !this.player.uncertaintyHandler.wasAffectedByStuckSpeed()
            && this.player.fireworks.getMaxFireworksAppliedPossible() <= 0) {
            boolean couldStep = this.player.isPointThree() && this.checkForGround(this.player.clientVelocity.getY());

            for (VectorData data : init) {
               Vector3dm toZeroVec = new PredictionEngine().handleStartingVelocityUncertainty(this.player, data, new Vector3dm());
               Vector3dm collisionResult = Collisions.collide(this.player, toZeroVec.getX(), toZeroVec.getY(), toZeroVec.getZ(), -2.1474836E9F, null);
               boolean likelyStepSkip = this.player.isPointThree() && data.vector.getY() > -0.08 && data.vector.getY() < 0.06 && couldStep;
               double minHorizLength = Math.max(0.0, Math.hypot(collisionResult.getX(), collisionResult.getZ()) - speed);
               boolean forcedNo003 = data.isExplosion() || data.isKnockback();
               double length = Math.hypot(
                  (forcedNo003 || !this.player.lastOnGround) && !likelyStepSkip && !this.controlsVerticalMovement() ? Math.abs(collisionResult.getY()) : 0.0,
                  minHorizLength
               );
               minimum = Math.min(minimum, length);
               if (minimum < this.player.getMovementThreshold()) {
                  break;
               }
            }

            return minimum < this.player.getMovementThreshold();
         } else {
            return true;
         }
      }
   }

   public double getHorizontalFluidPushingUncertainty(VectorData vector) {
      return this.isNearHorizontalFlowingLiquid && vector.isZeroPointZeroThree() ? 0.028 : 0.0;
   }

   public double getVerticalFluidPushingUncertainty(VectorData vector) {
      return (this.isNearBubbleColumn || this.isNearVerticalFlowingLiquid) && vector.isZeroPointZeroThree() ? 0.028 : 0.0;
   }

   public double getVerticalBubbleUncertainty(VectorData vectorData) {
      return this.isNearBubbleColumn && vectorData.isZeroPointZeroThree() ? 0.35 : 0.0;
   }

   public double getAdditionalVerticalUncertainty(VectorData vector) {
      double fluidAddition = vector.isZeroPointZeroThree() ? 0.014 : 0.0;
      if (this.player.inVehicle()) {
         return 0.0;
      } else if (this.headHitter) {
         this.wasAlwaysCertain = false;
         return -Math.max(0.0, vector.vector.getY()) - 0.1 - fluidAddition;
      } else if (this.player.uncertaintyHandler.wasAffectedByStuckSpeed()) {
         this.wasAlwaysCertain = false;
         return -0.1 - fluidAddition;
      } else if (!vector.isZeroPointZeroThree()) {
         return 0.0;
      } else {
         double minMovement = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? 0.003 : 0.005;
         double yVel = vector.vector.getY();
         double maxYTraveled = 0.0;
         boolean first = true;

         do {
            if (Math.abs(yVel) < minMovement) {
               yVel = 0.0;
            }

            if (!first) {
               maxYTraveled += yVel;
            }

            if (!first && yVel == 0.0) {
               break;
            }

            first = false;
            yVel = this.iterateGravity(this.player, yVel);
         } while (yVel != 0.0 && Math.abs(maxYTraveled + vector.vector.getY()) < this.player.getMovementThreshold());

         if (maxYTraveled != 0.0) {
            this.wasAlwaysCertain = false;
         }

         return maxYTraveled;
      }
   }

   private double iterateGravity(GrimPlayer player, double y) {
      OptionalInt levitation = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.LEVITATION);
      if (levitation.isPresent()) {
         y += (0.05 * (levitation.getAsInt() + 1) - y) * 0.2;
      } else if (player.hasGravity) {
         y -= player.gravity;
      }

      return y * BlockProperties.getModifiedAirDrag(0.98F, player);
   }

   @Generated
   public boolean isNearClimbable() {
      return this.isNearClimbable;
   }

   @Generated
   public void setPushing(boolean isPushing) {
      this.isPushing = isPushing;
   }

   @Generated
   public boolean isPushing() {
      return this.isPushing;
   }

   @Generated
   public boolean isWasAlwaysCertain() {
      return this.wasAlwaysCertain;
   }
}
