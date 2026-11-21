package ac.grim.grimac.utils.nmsutil;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.utils.data.MainSupportingBlockData;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHorse;
import ac.grim.grimac.utils.data.packetentity.PacketEntityNautilus;
import ac.grim.grimac.utils.data.packetentity.PacketEntityStrider;
import ac.grim.grimac.utils.math.GrimMath;
import lombok.Generated;

public final class BlockProperties {
   public static float getFrictionInfluencedSpeed(float blockFriction, GrimPlayer player) {
      float movementSpeed = (float)player.speed;
      if (player.lastOnGround) {
         blockFriction = getModifiedFriction(blockFriction, player);
         if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14)) {
            float friction = blockFriction * 0.91F;
            float acceleration = player.getClientVersion().isOlderThan(ClientVersion.V_1_13) ? 0.16277136F : 0.16277137F;
            return movementSpeed * (acceleration / (friction * friction * friction));
         } else {
            return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) && blockFriction <= 0.6F
               ? movementSpeed
               : movementSpeed * (0.21600002F / (blockFriction * blockFriction * blockFriction));
         }
      } else {
         if (player.inVehicle()) {
            PacketEntity riding = player.compensatedEntities.self.getRiding();
            if (riding.getType() == EntityTypes.PIG || riding instanceof PacketEntityNautilus || riding instanceof PacketEntityHorse) {
               return movementSpeed * 0.1F;
            }

            if (riding instanceof PacketEntityStrider strider) {
               if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_20)) {
                  return movementSpeed * 0.1F;
               }

               return (float)strider.getAttributeValue(Attributes.MOVEMENT_SPEED) * (strider.isShaking ? 0.66F : 1.0F) * 0.1F;
            }
         }

         if (player.isFlying) {
            return player.flySpeed * 20.0F * (player.isSprinting ? 0.1F : 0.05F);
         } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_19_4)) {
            return player.isSprinting ? 0.025999999F : 0.02F;
         } else {
            return player.lastSprintingForSpeed ? (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_18_2) ? 0.026F : 0.025999999F) : 0.02F;
         }
      }
   }

   public static StateType getOnPos(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
      if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
         return getOnBlock(player, playerPos.getX(), playerPos.getY(), playerPos.getZ());
      } else {
         Vector3i pos = getOnPos(player, playerPos, mainSupportingBlockData, 0.2F);
         return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
      }
   }

   public static float getFriction(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
      if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
         double searchBelowAmount = 0.5000001;
         if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) {
            searchBelowAmount = 1.0;
         }

         StateType type = player.compensatedWorld.getBlockType(playerPos.getX(), playerPos.getY() - searchBelowAmount, playerPos.getZ());
         return getMaterialFriction(player, type);
      } else {
         StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
         return getMaterialFriction(player, underPlayer);
      }
   }

   public static float getBlockSpeedFactor(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
      if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) {
         return 1.0F;
      } else if (!player.isGliding && !player.isFlying) {
         if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)) {
            return getBlockSpeedFactorLegacy(player, playerPos);
         } else {
            WrappedBlockState inBlock = player.compensatedWorld.getBlock(playerPos.getX(), playerPos.getY(), playerPos.getZ());
            float inBlockSpeedFactor = getBlockSpeedFactor(player, inBlock.getType());
            if (inBlockSpeedFactor == 1.0F && inBlock.getType() != StateTypes.WATER && inBlock.getType() != StateTypes.BUBBLE_COLUMN) {
               StateType underPlayer = getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos);
               return getModernVelocityMultiplier(player, getBlockSpeedFactor(player, underPlayer));
            } else {
               return getModernVelocityMultiplier(player, inBlockSpeedFactor);
            }
         }
      } else {
         return 1.0F;
      }
   }

   public static boolean onHoneyBlock(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
      if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) {
         return false;
      } else {
         StateType inBlock = player.compensatedWorld.getBlockType(playerPos.getX(), playerPos.getY(), playerPos.getZ());
         return inBlock == StateTypes.HONEY_BLOCK
            || getBlockPosBelowThatAffectsMyMovement(player, mainSupportingBlockData, playerPos) == StateTypes.HONEY_BLOCK;
      }
   }

   private static StateType getBlockPosBelowThatAffectsMyMovement(GrimPlayer player, MainSupportingBlockData mainSupportingBlockData, Vector3d playerPos) {
      Vector3i pos = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_19_4)
         ? new Vector3i(GrimMath.floor(playerPos.getX()), GrimMath.floor(playerPos.getY() - 0.5000001), GrimMath.floor(playerPos.getZ()))
         : getOnPos(player, playerPos, mainSupportingBlockData, 0.500001F);
      return player.compensatedWorld.getBlockType(pos.x, pos.y, pos.z);
   }

   private static Vector3i getOnPos(GrimPlayer player, Vector3d playerPos, MainSupportingBlockData mainSupportingBlockData, float searchBelowPlayer) {
      Vector3i mainBlockPos = mainSupportingBlockData.blockPos();
      if (mainBlockPos == null) {
         return new Vector3i(GrimMath.floor(playerPos.getX()), GrimMath.floor(playerPos.getY() - searchBelowPlayer), GrimMath.floor(playerPos.getZ()));
      } else {
         StateType blockstate = player.compensatedWorld.getBlockType(mainBlockPos.x, mainBlockPos.y, mainBlockPos.z);
         boolean shouldReturn = (!(searchBelowPlayer <= 0.5) || !BlockTags.FENCES.contains(blockstate))
            && !BlockTags.WALLS.contains(blockstate)
            && !BlockTags.FENCE_GATES.contains(blockstate);
         return shouldReturn ? mainBlockPos.withY(GrimMath.floor(playerPos.getY() - searchBelowPlayer)) : mainBlockPos;
      }
   }

   public static float getMaterialFriction(GrimPlayer player, StateType material) {
      float friction = 0.6F;
      if (material == StateTypes.ICE) {
         friction = 0.98F;
      }

      if (material == StateTypes.SLIME_BLOCK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
         friction = 0.8F;
      }

      if (material == StateTypes.HONEY_BLOCK && player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) {
         friction = 0.8F;
      }

      if (material == StateTypes.PACKED_ICE) {
         friction = 0.98F;
      }

      if (material == StateTypes.FROSTED_ICE) {
         friction = 0.98F;
      }

      if (material == StateTypes.BLUE_ICE) {
         friction = 0.98F;
         if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            friction = 0.989F;
         }
      }

      return friction;
   }

   private static StateType getOnBlock(GrimPlayer player, double x, double y, double z) {
      StateType block1 = player.compensatedWorld.getBlockType(GrimMath.floor(x), GrimMath.floor(y - 0.2F), GrimMath.floor(z));
      if (block1.isAir()) {
         StateType block2 = player.compensatedWorld.getBlockType(GrimMath.floor(x), GrimMath.floor(y - 1.2F), GrimMath.floor(z));
         if (Materials.isFence(block2) || Materials.isWall(block2) || Materials.isGate(block2)) {
            return block2;
         }
      }

      return block1;
   }

   private static float getBlockSpeedFactorLegacy(GrimPlayer player, Vector3d pos) {
      StateType block = player.compensatedWorld.getBlockType(pos.getX(), pos.getY(), pos.getZ());
      if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16) && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_1)) {
         StateType onBlock = getOnBlock(player, pos.getX(), pos.getY(), pos.getZ());
         if (onBlock == StateTypes.SOUL_SAND && player.inventory.getBoots().getEnchantmentLevel(EnchantmentTypes.SOUL_SPEED) > 0) {
            return 1.0F;
         }
      }

      float speed = getBlockSpeedFactor(player, block);
      if (speed == 1.0F && block != StateTypes.SOUL_SAND && block != StateTypes.WATER && block != StateTypes.BUBBLE_COLUMN) {
         StateType block2 = player.compensatedWorld.getBlockType(pos.getX(), pos.getY() - 0.5000001, pos.getZ());
         return getBlockSpeedFactor(player, block2);
      } else {
         return speed;
      }
   }

   private static float getBlockSpeedFactor(GrimPlayer player, StateType type) {
      if (type == StateTypes.HONEY_BLOCK) {
         return 0.4F;
      } else if (type == StateTypes.SOUL_SAND) {
         return player.getClientVersion().isOlderThan(ClientVersion.V_1_21)
               && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16_2)
               && player.inventory.getBoots().getEnchantmentLevel(EnchantmentTypes.SOUL_SPEED) > 0
            ? 1.0F
            : 0.4F;
      } else {
         return 1.0F;
      }
   }

   private static float getModernVelocityMultiplier(GrimPlayer player, float blockSpeedFactor) {
      return player.getClientVersion().isOlderThan(ClientVersion.V_1_21)
         ? blockSpeedFactor
         : (float)GrimMath.lerp((float)player.compensatedEntities.self.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), blockSpeedFactor, 1.0);
   }

   public static float getModifiedFriction(float friction, GrimPlayer player) {
      if (player.getClientVersion().isOlderThan(ClientVersion.V_26_2)) {
         return friction;
      } else {
         PacketEntity entity = (PacketEntity)(player.inVehicle() ? player.compensatedEntities.self.getRiding() : player.compensatedEntities.self);
         return GrimMath.clamp(1.0F - (1.0F - friction) * (float)entity.getAttributeValue(Attributes.FRICTION_MODIFIER), 0.0F, 1.0F);
      }
   }

   public static float getModifiedAirDrag(float drag, GrimPlayer player) {
      if (player.getClientVersion().isOlderThan(ClientVersion.V_26_2)) {
         return drag;
      } else {
         PacketEntity entity = (PacketEntity)(player.inVehicle() ? player.compensatedEntities.self.getRiding() : player.compensatedEntities.self);
         return GrimMath.clamp(1.0F - (1.0F - drag) * (float)entity.getAttributeValue(Attributes.AIR_DRAG_MODIFIER), 0.0F, 1.0F);
      }
   }

   public static float getEntityBounciness(GrimPlayer player) {
      if (player.getClientVersion().isOlderThan(ClientVersion.V_26_2)) {
         return 0.0F;
      } else {
         PacketEntity entity = (PacketEntity)(player.inVehicle() ? player.compensatedEntities.self.getRiding() : player.compensatedEntities.self);
         if (entity == player.compensatedEntities.self && player.isSneaking) {
            return 0.0F;
         } else {
            return !entity.isLivingEntity ? 0.0F : (float)entity.getAttributeValue(Attributes.BOUNCINESS);
         }
      }
   }

   public static double getVelocityAfterHorizontalCollision(GrimPlayer player, double velocity) {
      return player.getClientVersion().isOlderThan(ClientVersion.V_26_2) ? 0.0 : -velocity * getEntityBounciness(player);
   }

   public static double getVelocityAfterVerticalCollision(GrimPlayer player, double velocity, double movementY) {
      return getVelocityAfterVerticalCollision(player, velocity, movementY, getEntityBounciness(player));
   }

   public static double getVelocityAfterVerticalCollision(GrimPlayer player, double velocity, double movementY, double restitution) {
      double gravity = getEffectiveGravity(player, velocity);
      if (!player.getClientVersion().isOlderThan(ClientVersion.V_26_2) && (!(velocity < 0.0) || !(-velocity < gravity))) {
         if (restitution <= 0.0) {
            return 0.0;
         } else {
            double portionWithMovement = velocity == 0.0 ? 0.0 : movementY / velocity;
            double gravityCompensation = portionWithMovement * gravity;
            double effectiveDrag = GrimMath.lerp(portionWithMovement, 1.0, getModifiedAirDrag(0.98F, player));
            return (gravityCompensation - velocity) * effectiveDrag * restitution;
         }
      } else {
         return 0.0;
      }
   }

   public static double getEffectiveGravity(GrimPlayer player, double deltaMovementY) {
      PacketEntity entity = (PacketEntity)(player.inVehicle() ? player.compensatedEntities.self.getRiding() : player.compensatedEntities.self);
      double gravity = entity.getAttributeValue(Attributes.GRAVITY);
      if (deltaMovementY <= 0.0 && player.compensatedEntities.getSlowFallingAmplifier().isPresent()) {
         return player.getClientVersion().isOlderThan(ClientVersion.V_1_20_5) ? 0.01 : Math.min(gravity, 0.01);
      } else {
         return gravity;
      }
   }

   public static float getBlockBounceRestitution(StateType type, GrimPlayer player) {
      if (type == StateTypes.SLIME_BLOCK && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
         return 1.0F;
      } else if (BlockTags.BEDS.contains(type) && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_12)) {
         return player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2) ? 0.75F : 0.66F;
      } else {
         return type == StateTypes.HONEY_BLOCK
               && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14_4)
               && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)
            ? 1.0F
            : 0.0F;
      }
   }

   @Generated
   private BlockProperties() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
