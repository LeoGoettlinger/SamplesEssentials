package ac.grim.grimac.predictionengine.movementtick;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.PlayerBaseTick;
import ac.grim.grimac.predictionengine.blockeffects.PotentSulfurGeyser;
import ac.grim.grimac.predictionengine.predictions.PredictionEngine;
import ac.grim.grimac.predictionengine.predictions.PredictionEngineElytra;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityStrider;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.BlockProperties;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.EntityTypeTags;
import ac.grim.grimac.utils.nmsutil.FluidFallingAdjustedMovement;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import ac.grim.grimac.utils.nmsutil.MainSupportingBlockPosFinder;
import ac.grim.grimac.utils.team.EntityPredicates;
import ac.grim.grimac.utils.team.EntityTeam;
import ac.grim.grimac.utils.team.TeamHandler;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import com.viaversion.viaversion.api.Via;
import lombok.Generated;

public class MovementTicker {
   public final GrimPlayer player;

   public static void handleEntityCollisions(GrimPlayer player) {
      boolean serverSupported = PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9);
      boolean hasEntityPushing = !player.getClientVersion().isOlderThan(ClientVersion.V_1_9)
         && (serverSupported || ViaVersionUtil.isAvailable && !Via.getConfig().isPreventCollision());
      if (hasEntityPushing) {
         int possibleCollidingEntities = 0;
         int possibleRiptideEntities = 0;
         if (!player.inVehicle() && player.gamemode != GameMode.SPECTATOR) {
            SimpleCollisionBox playerBox = GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.lastX, player.lastY, player.lastZ, 0.6F, 1.8F);
            playerBox.encompass(
               GetBoundingBox.getBoundingBoxFromPosAndSize(player, player.x, player.y, player.z, 0.6F, 1.8F).expand(player.getMovementThreshold())
            );
            playerBox.expand(0.2);
            TeamHandler teamHandler = player.checkManager.getPacketCheck(TeamHandler.class);
            EntityTeam playerTeam = teamHandler != null ? teamHandler.getPlayerTeam() : null;

            for (PacketEntity entity : player.compensatedEntities.entityMap.values()) {
               SimpleCollisionBox entityBox = entity.getPossibleCollisionBoxes();
               if (playerBox.isCollided(entityBox)) {
                  possibleRiptideEntities++;
                  if (entity.isPushable()) {
                     if (serverSupported) {
                        EntityTeam entityTeam = teamHandler != null ? teamHandler.getEntityTeam(entity) : null;
                        if (!EntityPredicates.canBePushedBy(entityTeam, playerTeam)) {
                           continue;
                        }
                     }

                     possibleCollidingEntities++;
                  }
               }
            }
         }

         if (player.isGliding && possibleCollidingEntities > 0) {
            player.uncertaintyHandler.yNegativeUncertainty -= 0.05;
            player.uncertaintyHandler.yPositiveUncertainty += 0.05;
         }

         player.uncertaintyHandler.riptideEntities.add(possibleRiptideEntities);
         player.uncertaintyHandler.collidingEntities.add(possibleCollidingEntities);
      }
   }

   private boolean isHorizontalCollisionSoft(Vector3dm collide) {
      double horizontalLengthSquared = collide.getX() * collide.getX() + collide.getZ() * collide.getZ();
      if (horizontalLengthSquared < 1.0E-5F) {
         return false;
      } else {
         float xxa = (float)this.player.predictedVelocity.input.getX();
         float zza = (float)this.player.predictedVelocity.input.getZ();
         float yawInRadians = this.player.yaw * (float) (Math.PI / 180.0);
         double sin = this.player.trigHandler.sin(yawInRadians);
         double cos = this.player.trigHandler.cos(yawInRadians);
         double g = xxa * cos - zza * sin;
         double h = zza * cos + xxa * sin;
         double i = g * g + h * h;
         return i >= 1.0E-5F && Math.acos((g * collide.getX() + h * collide.getZ()) / Math.sqrt(i * horizontalLengthSquared)) < 0.13962634F;
      }
   }

   public void move(Vector3dm inputVel, Vector3dm collide) {
      if (this.player.stuckSpeedMultiplier.getX() < 0.99) {
         this.player.clientVelocity = new Vector3dm();
      }

      double deltaX = this.player.clientVelocity.getX();
      double deltaY = this.player.clientVelocity.getY();
      double deltaZ = this.player.clientVelocity.getZ();
      if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2)) {
         boolean xAxis = !GrimMath.equal(inputVel.getX(), collide.getX());
         boolean zAxis = !GrimMath.equal(inputVel.getZ(), collide.getZ());
         if (xAxis) {
            this.player.clientVelocity.setX(BlockProperties.getVelocityAfterHorizontalCollision(this.player, this.player.clientVelocity.getX()));
         }

         if (zAxis) {
            this.player.clientVelocity.setZ(BlockProperties.getVelocityAfterHorizontalCollision(this.player, this.player.clientVelocity.getZ()));
         }

         this.player.horizontalCollision = xAxis || zAxis;
         this.player.softHorizontalCollision = this.player.horizontalCollision && this.isHorizontalCollisionSoft(collide);
      } else {
         if (inputVel.getX() != collide.getX()) {
            this.player.clientVelocity.setX(BlockProperties.getVelocityAfterHorizontalCollision(this.player, this.player.clientVelocity.getX()));
         }

         if (inputVel.getZ() != collide.getZ()) {
            this.player.clientVelocity.setZ(BlockProperties.getVelocityAfterHorizontalCollision(this.player, this.player.clientVelocity.getZ()));
         }

         this.player.horizontalCollision = inputVel.getX() != collide.getX() || inputVel.getZ() != collide.getZ();
      }

      this.player.verticalCollision = inputVel.getY() != collide.getY();
      boolean calculatedOnGround = this.player.verticalCollision && inputVel.getY() < 0.0;
      if (inputVel.getY() == -1.0E-7 && collide.getY() > -1.0E-7 && collide.getY() <= 0.0 && !this.player.inVehicle()) {
         calculatedOnGround = this.player.onGround;
      }

      this.player.clientClaimsLastOnGround = this.player.onGround;
      if (this.player.inVehicle()
         && this.player.clientControlledVerticalCollision
         && this.player.uncertaintyHandler.isStepMovement
         && (inputVel.getY() <= 0.0 || this.player.predictedVelocity.isSwimHop())) {
         calculatedOnGround = true;
      }

      if (this.player.inVehicle() || !this.player.exemptOnGround()) {
         this.player.onGround = calculatedOnGround;
      }

      this.player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(this.player, this.player.x, this.player.y, this.player.z);
      PacketEntity riding = this.player.compensatedEntities.self.getRiding();
      if (this.player.getClientVersion() != ClientVersion.V_1_21_4
         && !this.player.wasTouchingWater
         && (riding == null || !riding.isBoat && !riding.isHappyGhast)) {
         if (this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_21_11)) {
            PlayerBaseTick.updateInWaterStateAndDoWaterCurrentPushing(this.player);
         } else {
            PlayerBaseTick.updateFluidInteraction(this.player);
         }
      }

      if (this.player.onGround) {
         this.player.fallDistance = 0.0;
      } else if (collide.getY() < 0.0) {
         this.player.fallDistance = this.player.fallDistance - collide.getY();
         this.player.vehicleData.lastYd = collide.getY();
      }

      if (riding instanceof PacketEntityStrider) {
         Collisions.handleInsideBlocks(this.player);
      }

      this.player.mainSupportingBlockData = MainSupportingBlockPosFinder.findMainSupportingBlockPos(
         this.player,
         this.player.mainSupportingBlockData,
         new Vector3d(collide.getX(), collide.getY(), collide.getZ()),
         this.player.boundingBox,
         this.player.onGround
      );
      StateType onBlock = BlockProperties.getOnPos(this.player, this.player.mainSupportingBlockData, new Vector3d(this.player.x, this.player.y, this.player.z));
      if (inputVel.getY() != collide.getY()) {
         float blockRestitution = BlockProperties.getBlockBounceRestitution(onBlock, this.player);
         boolean suppressesBounce = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && BlockTags.SUPPRESSES_BOUNCE.contains(onBlock);
         if (!(blockRestitution > 0.0F) && (suppressesBounce || !(BlockProperties.getEntityBounciness(this.player) > 0.0F))) {
            this.player.clientVelocity.setY(0);
         } else {
            this.applyBlockBounce(blockRestitution, collide, riding);
         }
      }

      collide = PredictionEngine.clampMovementToHardBorder(this.player, collide);
      if (!(collide.lengthSquared() <= 1.0E-7)
         || !this.player.getClientVersion().isOlderThan(ClientVersion.V_1_21_2) && !(inputVel.lengthSquared() - collide.lengthSquared() >= 1.0E-7)) {
         if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)) {
            Vector3d from = new Vector3d(this.player.lastX, this.player.lastY, this.player.lastZ);
            Vector3d to = new Vector3d(this.player.x, this.player.y, this.player.z);
            this.player.addMovementThisTick(new GrimPlayer.Movement(from, to, new Vector3d(deltaX, deltaY, deltaZ)));
         }
      } else {
         collide = new Vector3dm();
      }

      this.player.predictedVelocity = new VectorData(collide.clone(), this.player.predictedVelocity.lastVector, this.player.predictedVelocity.vectorType);
      float f = BlockProperties.getBlockSpeedFactor(this.player, this.player.mainSupportingBlockData, new Vector3d(this.player.x, this.player.y, this.player.z));
      this.player.clientVelocity.multiply(f, 1.0, f);
      if (!this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
         if (this.player.stuckSpeedMultiplier.getX() < 0.99) {
            this.player.uncertaintyHandler.lastStuckSpeedMultiplier.reset();
         }

         this.player.stuckSpeedMultiplier = GrimPlayer.DEFAULT_STUCK_SPEED;
         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_16)) {
            this.player.wasTouchingLava = false;
         }

         Collisions.handleInsideBlocks(this.player);
         if (this.player.stuckSpeedMultiplier.getX() < 0.9) {
            this.player.fallDistance = 0.0;
         }

         if (this.player.isFlying) {
            this.player.stuckSpeedMultiplier = GrimPlayer.DEFAULT_STUCK_SPEED;
         }
      }
   }

   public void livingEntityAIStep() {
      handleEntityCollisions(this.player);
      SimpleCollisionBox oldBB = this.player.boundingBox.copy();
      if (!this.player.inVehicle()) {
         this.playerEntityTravel();
      } else {
         this.livingEntityTravel();
      }

      this.player.uncertaintyHandler.xNegativeUncertainty = 0.0;
      this.player.uncertaintyHandler.xPositiveUncertainty = 0.0;
      this.player.uncertaintyHandler.yNegativeUncertainty = 0.0;
      this.player.uncertaintyHandler.yPositiveUncertainty = 0.0;
      this.player.uncertaintyHandler.zNegativeUncertainty = 0.0;
      this.player.uncertaintyHandler.zPositiveUncertainty = 0.0;
      if (this.player.uncertaintyHandler.lastTeleportTicks.hasOccurredSince(0)) {
         this.player.uncertaintyHandler.yNegativeUncertainty -= 0.02;
      }

      if (this.player.isFlying) {
         SimpleCollisionBox playerBox = GetBoundingBox.getCollisionBoxForPlayer(this.player, this.player.lastX, this.player.lastY, this.player.lastZ);
         if (!Collisions.isEmpty(this.player, playerBox.copy().offset(0.0, 0.1, 0.0))) {
            this.player.uncertaintyHandler.yPositiveUncertainty = this.player.flySpeed * 5.0F;
         }

         if (!Collisions.isEmpty(this.player, playerBox.copy().offset(0.0, -0.1, 0.0))) {
            this.player.uncertaintyHandler.yNegativeUncertainty = this.player.flySpeed * -5.0F;
         }
      }

      if (!this.player.getClientVersion().isOlderThan(ClientVersion.V_1_14) && !this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2)) {
         oldBB.expand(-1.0E-7);
         double posX = Math.max(0.0, this.player.predictedVelocity.vector.getX()) + 1.0E-7;
         double negX = Math.min(0.0, this.player.predictedVelocity.vector.getX()) - 1.0E-7;
         double posZ = Math.max(0.0, this.player.predictedVelocity.vector.getZ()) + 1.0E-7;
         double negZ = Math.min(0.0, this.player.predictedVelocity.vector.getZ()) - 1.0E-7;
         boolean xAxisCollision = !Collisions.isEmpty(this.player, oldBB.expandMin(negX, 0.0, 0.0).expandMax(posX, 0.0, 0.0));
         boolean zAxisCollision = !Collisions.isEmpty(this.player, oldBB.expandMin(0.0, 0.0, negZ).expandMax(0.0, 0.0, posZ));
         zAxisCollision = zAxisCollision || this.player.actualMovement.getZ() == 0.0;
         if (zAxisCollision && xAxisCollision) {
            double playerSpeed = this.player.speed;
            if (this.player.wasTouchingWater) {
               float swimSpeed = 0.02F;
               if (this.player.depthStriderLevel > 0.0F) {
                  swimSpeed += ((float)this.player.speed - swimSpeed) * this.player.depthStriderLevel / 3.0F;
               }

               playerSpeed = swimSpeed;
            } else if (this.player.wasTouchingLava) {
               playerSpeed = 0.02F;
            } else if (this.player.isGliding) {
               playerSpeed = 0.4;
               this.player.uncertaintyHandler.yNegativeUncertainty -= 0.05;
               this.player.uncertaintyHandler.yPositiveUncertainty += 0.05;
            }

            this.player.uncertaintyHandler.xNegativeUncertainty -= playerSpeed * 3.0;
            this.player.uncertaintyHandler.xPositiveUncertainty += playerSpeed * 3.0;
         }
      }
   }

   public void playerEntityTravel() {
      if (this.player.isFlying && !this.player.inVehicle()) {
         double oldY = this.player.clientVelocity.getY();
         double oldYJumping = oldY + this.player.flySpeed * 3.0F;
         this.livingEntityTravel();
         if (!this.player.predictedVelocity.isKnockback()
            && !this.player.predictedVelocity.isTrident()
            && this.player.uncertaintyHandler.yPositiveUncertainty == 0.0
            && this.player.uncertaintyHandler.yNegativeUncertainty == 0.0
            && !this.player.isGliding) {
            if (Math.abs(oldY - this.player.actualMovement.getY()) < oldYJumping - this.player.actualMovement.getY()) {
               this.player.clientVelocity.setY(oldY * 0.6);
            } else {
               this.player.clientVelocity.setY(oldYJumping * 0.6);
            }
         } else {
            this.player.clientVelocity.setY(this.player.actualMovement.getY() * 0.6);
         }
      } else {
         this.livingEntityTravel();
      }
   }

   public void doWaterMove(float swimSpeed, boolean isFalling, float swimFriction) {
   }

   public void doLavaMove() {
   }

   public void doNormalMove(float blockFriction) {
   }

   private void applyBlockBounce(float blockRestitution, Vector3dm movement, PacketEntity riding) {
      if (this.player.isSneaking) {
         this.player.clientVelocity.setY(0);
      } else {
         double currentY = this.player.clientVelocity.getY();
         if (!(currentY >= 0.0)) {
            double restitution = blockRestitution;
            if (riding != null && !riding.isLivingEntity) {
               restitution *= 0.8F;
            }

            if (this.player.getClientVersion().isOlderThan(ClientVersion.V_26_2)) {
               this.player.clientVelocity.setY(-currentY * restitution);
            } else {
               restitution = Math.max(restitution, (double)BlockProperties.getEntityBounciness(this.player));
               this.player.clientVelocity.setY(BlockProperties.getVelocityAfterVerticalCollision(this.player, currentY, movement.getY(), restitution));
            }
         }
      }
   }

   public void livingEntityTravel() {
      double playerGravity = !this.player.inVehicle()
         ? this.player.compensatedEntities.self.getAttributeValue(Attributes.GRAVITY)
         : this.player.compensatedEntities.self.getRiding().getAttributeValue(Attributes.GRAVITY);
      boolean isFalling = this.player.actualMovement.getY() <= 0.0;
      if (isFalling && this.player.compensatedEntities.getSlowFallingAmplifier().isPresent()) {
         playerGravity = this.player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5) ? 0.01 : Math.min(playerGravity, 0.01);
         this.player.fallDistance = 0.0;
      }

      this.player.gravity = playerGravity;
      double lavaLevel = 0.0;
      if (this.canStandOnLava()) {
         lavaLevel = this.player
            .compensatedWorld
            .getLavaFluidLevelAt(GrimMath.floor(this.player.lastX), GrimMath.floor(this.player.lastY), GrimMath.floor(this.player.lastZ));
      }

      if (this.player.wasTouchingWater && !this.player.isFlying) {
         boolean isSkeletonHorse = this.player.inVehicle()
            && this.player.compensatedEntities.self.getRiding().getType() == EntityTypes.SKELETON_HORSE
            && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13);
         float swimFriction = this.player.isSprinting && this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)
            ? 0.9F
            : (isSkeletonHorse ? 0.96F : 0.8F);
         float swimSpeed = 0.02F;
         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_21) && this.player.depthStriderLevel > 3.0F) {
            this.player.depthStriderLevel = 3.0F;
         }

         if (!this.player.lastOnGround) {
            this.player.depthStriderLevel *= 0.5F;
         }

         if (this.player.depthStriderLevel > 0.0F) {
            float divisor = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21) ? 1.0F : 3.0F;
            swimFriction += (0.54600006F - swimFriction) * this.player.depthStriderLevel / divisor;
            swimSpeed += ((float)this.player.speed - swimSpeed) * this.player.depthStriderLevel / divisor;
         }

         if (this.player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.DOLPHINS_GRACE).isPresent()) {
            swimFriction = 0.96F;
         }

         this.player.friction = swimFriction;
         this.doWaterMove(swimSpeed, isFalling, swimFriction);
         this.player.isClimbing = Collisions.onClimbable(this.player, this.player.x, this.player.y, this.player.z);
         if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) && this.player.isClimbing) {
            this.player.lastWasClimbing = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(
                  this.player, playerGravity, isFalling, this.player.clientVelocity.clone().setY(0.1600000023841858)
               )
               .getY();
         }

         this.floatInWaterWhileRidden();
      } else {
         this.player.canFloatWhileRidden = false;
         if (!this.player.wasTouchingLava || this.player.isFlying || lavaLevel > 0.0 && this.canStandOnLava()) {
            if (this.player.isGliding) {
               if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5)
                  && Collisions.onClimbable(this.player, this.player.lastX, this.player.lastY, this.player.lastZ)) {
                  float blockFriction = BlockProperties.getFriction(
                     this.player, this.player.mainSupportingBlockData, new Vector3d(this.player.lastX, this.player.lastY, this.player.lastZ)
                  );
                  float airDrag = BlockProperties.getModifiedAirDrag(0.91F, this.player);
                  this.player.friction = this.player.lastOnGround ? BlockProperties.getModifiedFriction(blockFriction, this.player) * airDrag : airDrag;
                  this.doNormalMove(blockFriction);
                  this.player.isGliding = false;
                  this.player.pointThreeEstimator.updatePlayerGliding();
               } else {
                  this.player.friction = 0.99F;
                  if (this.player.clientVelocity.getY() > -0.5) {
                     this.player.fallDistance = 1.0;
                  }

                  new PredictionEngineElytra().guessBestMovement(0.0F, this.player);
               }
            } else {
               float blockFriction = BlockProperties.getFriction(
                  this.player, this.player.mainSupportingBlockData, new Vector3d(this.player.lastX, this.player.lastY, this.player.lastZ)
               );
               float airDrag = BlockProperties.getModifiedAirDrag(0.91F, this.player);
               this.player.friction = this.player.lastOnGround ? BlockProperties.getModifiedFriction(blockFriction, this.player) * airDrag : airDrag;
               this.doNormalMove(blockFriction);
            }
         } else {
            this.player.friction = 0.5F;
            this.doLavaMove();
            if (this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16) && this.player.getFluidHeight(FluidTag.LAVA) <= 0.4) {
               this.player.clientVelocity = this.player.clientVelocity.multiply(0.5, 0.8F, 0.5);
               this.player.clientVelocity = FluidFallingAdjustedMovement.getFluidFallingAdjustedMovement(
                  this.player, playerGravity, isFalling, this.player.clientVelocity
               );
            } else {
               this.player.clientVelocity.multiply(0.5);
            }

            if (this.player.hasGravity) {
               this.player.clientVelocity.add(0.0, -playerGravity / 4.0, 0.0);
            }
         }
      }

      Collisions.applyEffectsFromBlocks(this.player);
      PotentSulfurGeyser.launchEntityTicker(this.player, this.player.clientVelocity, true);
   }

   private boolean canFloatWhileRidden() {
      if (!this.player.getClientVersion().isOlderThan(ClientVersion.V_1_21_11) && this.player.inVehicle()) {
         PacketEntity vehicle = this.player.getVehicle();
         double fluidHeight = this.player.getFluidHeight(FluidTag.WATER);
         return EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN.anyOf(vehicle.getType()) && fluidHeight > 0.4;
      } else {
         return false;
      }
   }

   private void floatInWaterWhileRidden() {
      this.player.canFloatWhileRidden = this.canFloatWhileRidden();
      if (this.player.canFloatWhileRidden) {
         this.player.clientVelocity.add(0.0, 0.04F, 0.0);
      }
   }

   public boolean canStandOnLava() {
      return false;
   }

   @Generated
   public MovementTicker(GrimPlayer player) {
      this.player = player;
   }
}
