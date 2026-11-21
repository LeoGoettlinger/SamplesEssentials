package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.debug.HitboxDebugHandler;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttackRange;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.InteractionHand;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import ac.grim.grimac.shaded.fastutil.ints.Int2ObjectMap;
import ac.grim.grimac.shaded.fastutil.ints.Int2ObjectOpenHashMap;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.utils.collisions.datatypes.CollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.NoCollisionBox;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.BlockHitData;
import ac.grim.grimac.utils.data.EntityHitData;
import ac.grim.grimac.utils.data.HitData;
import ac.grim.grimac.utils.data.Pair;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySizeable;
import ac.grim.grimac.utils.data.packetentity.dragon.PacketEntityEnderDragonPart;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.ReachUtils;
import ac.grim.grimac.utils.nmsutil.WorldRayTrace;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import com.viaversion.viaversion.api.Via;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CheckData(
   name = "Reach",
   stableKey = "grim.combat.reach",
   description = "Attacked an entity from too far away",
   setback = 10.0
)
public class Reach extends Check implements PacketCheck {
   private static final Verbose V = Verbose.of("{f64:%.5f} blocks, type={entity}");
   private static final List<EntityType> blacklisted = Arrays.asList(EntityTypes.BOAT, EntityTypes.CHEST_BOAT, EntityTypes.SHULKER);
   private static final Reach.CheckResult NONE = new Reach.CheckResult(Reach.ResultType.NONE, 0.0, 0.0, false);
   private final Int2ObjectMap<Reach.InteractionData> playerAttackQueue = new Int2ObjectOpenHashMap<>();
   private final Set<Vector3i> blocksChangedThisTick = new HashSet<>();
   public static final double extraSearchDistance = 3.0;
   private boolean ignoreNonPlayerTargets;
   private boolean cancelImpossibleHits;
   public double threshold;
   private double cancelBuffer;
   private static final boolean ATTACK_RANGE_COMPONENT_EXISTS = PacketEvents.getAPI()
      .getServerManager()
      .getVersion()
      .isNewerThanOrEquals(ServerVersion.V_1_21_11);
   private static final boolean USE_1_8_HITBOX_MARGIN = PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8);

   public Reach(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (!this.player.disableGrim && event.getPacketType() == PacketType.Play.Client.ATTACK) {
         WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
         this.onInteract(event, packet.getEntityId(), InteractionHand.MAIN_HAND);
      }

      if (!this.player.disableGrim && event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
         this.onInteract(
            event,
            packet.getEntityId(),
            packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK ? InteractionHand.MAIN_HAND : packet.getHand()
         );
      }

      boolean isFlying = WrapperPlayClientPlayerFlying.isFlying(event.getPacketType());
      if (this.isUpdate(event.getPacketType())) {
         this.tickBetterReachCheckWithAngle(isFlying);
      }
   }

   private void onInteract(PacketReceiveEvent event, int entityId, InteractionHand hand) {
      if (this.player.getSetbackTeleportUtil().shouldBlockMovement()) {
         event.setCancelled(true);
         this.player.onPacketCancel();
      } else {
         PacketEntity entity = this.player.compensatedEntities.entityMap.get(entityId);
         if (entity != null && !(entity instanceof PacketEntityEnderDragonPart)) {
            if (!this.ignoreNonPlayerTargets || entity.getType().equals(EntityTypes.PLAYER)) {
               if (!entity.isDead) {
                  if (entity.getType() != EntityTypes.ARMOR_STAND || !this.player.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
                     if (entity.getType() != EntityTypes.HAPPY_GHAST || !this.player.getClientVersion().isOlderThan(ClientVersion.V_1_21_6)) {
                        if (this.player.gamemode != GameMode.CREATIVE && this.player.gamemode != GameMode.SPECTATOR) {
                           if (!this.player.inVehicle()) {
                              if (entity.riding == null) {
                                 ItemStack currentStack = this.player.inventory.getItemInHand(hand);
                                 ItemStack startStack = this.player.inventory.getStartOfTickStack();
                                 boolean hasRange = false;
                                 float maxReach = 0.0F;
                                 float hitboxMargin = 0.0F;
                                 Vector3dm attackRangeMovement = null;
                                 boolean clientAttackRangeExists = this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_11);
                                 boolean clientAndServerAgrees = clientAttackRangeExists && ATTACK_RANGE_COMPONENT_EXISTS;
                                 boolean viaVersionAvailable = false;
                                 if (USE_1_8_HITBOX_MARGIN && ViaVersionUtil.isAvailable) {
                                    viaVersionAvailable = Via.getConfig().getValues().containsKey("use-1_8-hitbox-margin")
                                       && Via.getConfig().use1_8HitboxMargin();
                                 }

                                 boolean clientAndViaVersion = clientAttackRangeExists && viaVersionAvailable;
                                 if (clientAndServerAgrees || clientAndViaVersion) {
                                    ItemAttackRange startRange = startStack.getComponentOr(ComponentTypes.ATTACK_RANGE, null);
                                    ItemAttackRange currentRange = currentStack.getComponentOr(ComponentTypes.ATTACK_RANGE, null);
                                    if (clientAndViaVersion) {
                                       if (startStack != ItemStack.EMPTY) {
                                          startRange = new ItemAttackRange(0.0F, 3.0F, 0.0F, 4.0F, 0.1F, 1.0F);
                                       }

                                       if (currentStack != ItemStack.EMPTY) {
                                          currentRange = new ItemAttackRange(0.0F, 3.0F, 0.0F, 4.0F, 0.1F, 1.0F);
                                       }
                                    }

                                    if (startRange != null) {
                                       hasRange = true;
                                       if (currentRange == null) {
                                          if (!clientAndViaVersion) {
                                             attackRangeMovement = this.player.clientVelocity.clone();
                                          }

                                          maxReach = startRange.getMaxRange();
                                          hitboxMargin = startRange.getHitboxMargin();
                                       } else {
                                          maxReach = Math.min(startRange.getMaxRange(), currentRange.getMaxRange());
                                          hitboxMargin = Math.min(startRange.getHitboxMargin(), currentRange.getHitboxMargin());
                                       }
                                    }
                                 }

                                 boolean tooManyAttacks = this.playerAttackQueue.size() > 10;
                                 if (!tooManyAttacks) {
                                    this.playerAttackQueue
                                       .put(
                                          entityId,
                                          new Reach.InteractionData(
                                             this.player.x, this.player.y, this.player.z, hasRange, maxReach, hitboxMargin, attackRangeMovement
                                          )
                                       );
                                 }

                                 boolean knownInvalid = attackRangeMovement == null && this.isKnownInvalid(entity, hasRange, maxReach, hitboxMargin);
                                 if (this.shouldModifyPackets() && this.cancelImpossibleHits && knownInvalid || tooManyAttacks) {
                                    event.setCancelled(true);
                                    this.player.onPacketCancel();
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            if (this.shouldModifyPackets() && this.player.compensatedEntities.serverPositionsMap.containsKey(entityId)) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         }
      }
   }

   private boolean isKnownInvalid(PacketEntity reachEntity, boolean hasAttackRange, float itemMaxReach, float itemHitboxMargin) {
      if ((blacklisted.contains(reachEntity.getType()) || !reachEntity.isLivingEntity) && reachEntity.getType() != EntityTypes.END_CRYSTAL) {
         return false;
      } else if (this.player.gamemode == GameMode.CREATIVE || this.player.gamemode == GameMode.SPECTATOR) {
         return false;
      } else if (this.player.inVehicle()) {
         return false;
      } else if (this.cancelBuffer != 0.0) {
         Reach.CheckResult result = this.checkReach(
            reachEntity, this.player.x, this.player.y, this.player.z, hasAttackRange, itemMaxReach, itemHitboxMargin, null, true
         );
         return result.isFlag();
      } else {
         SimpleCollisionBox targetBox = this.getTargetBox(reachEntity);
         double maxReach = this.applyReachModifiers(
            targetBox, hasAttackRange, itemMaxReach, itemHitboxMargin, !this.player.packetStateData.didLastMovementIncludePosition
         );
         return ReachUtils.getMinReachToBox(this.player, targetBox) > maxReach;
      }
   }

   private void tickBetterReachCheckWithAngle(boolean isFlying) {
      for (Int2ObjectMap.Entry<Reach.InteractionData> attack : this.playerAttackQueue.int2ObjectEntrySet()) {
         PacketEntity reachEntity = this.player.compensatedEntities.entityMap.get(attack.getIntKey());
         if (reachEntity != null) {
            Reach.InteractionData interactionData = attack.getValue();
            Reach.CheckResult result = this.checkReach(
               reachEntity,
               interactionData.x,
               interactionData.y,
               interactionData.z,
               interactionData.hasAttackRange,
               interactionData.maxReach,
               interactionData.hitboxMargin,
               interactionData.attackRangeMovement,
               false
            );
            switch (result.type()) {
               case REACH:
                  this.flag(V.write(this.verbose()).f64(result.minDistance()).uint(reachEntity.getType().getId(this.player.getClientVersion())), () -> {
                     String addedx = ", type=" + reachEntity.getType().getName().getKey();
                     if (reachEntity instanceof PacketEntitySizeable sizeablex) {
                        addedx = addedx + ", size=" + sizeablex.size;
                     }

                     return result.verbose() + addedx;
                  });
                  break;
               case HITBOX: {
                  String added = "type=" + reachEntity.getType().getName().getKey();
                  if (reachEntity instanceof PacketEntitySizeable sizeable) {
                     added = added + ", size=" + sizeable.size;
                  }

                  this.player.checkManager.getCheck(Hitboxes.class).flag(result.verbose() + added);
                  break;
               }
               case WALL_HIT: {
                  String added = reachEntity.getType() == EntityTypes.PLAYER ? "" : "type=" + reachEntity.getType().getName().getKey();
                  this.player.checkManager.getCheck(WallHit.class).flag(result.verbose() + added);
                  break;
               }
               case ENTITY_PIERCE: {
                  String added = reachEntity.getType() == EntityTypes.PLAYER ? "" : "type=" + reachEntity.getType().getName().getKey();
                  this.player.checkManager.getCheck(EntityPierce.class).flag(result.verbose() + added);
               }
            }
         }
      }

      this.playerAttackQueue.clear();
      if (isFlying) {
         this.blocksChangedThisTick.clear();
      }
   }

   @NotNull
   private Reach.CheckResult checkReach(
      PacketEntity reachEntity,
      double x,
      double y,
      double z,
      boolean hasAttackRange,
      float itemMaxReach,
      float itemHitboxMargin,
      Vector3dm attackRangeMovement,
      boolean isPrediction
   ) {
      SimpleCollisionBox targetBox = this.getTargetBox(reachEntity);
      double movementAllowance = attackRangeMovement == null ? 0.0 : this.getAttackRangeMovementAllowance(attackRangeMovement);
      double maxReach = this.applyReachModifiers(
            targetBox, hasAttackRange, itemMaxReach, itemHitboxMargin, !this.player.packetStateData.didLastLastMovementIncludePosition
         )
         + movementAllowance;
      double minDistance = Double.MAX_VALUE;
      List<Pair<Vector3dm, Double>> lookVecsAndEyeHeights = new ArrayList<>();
      double distance = maxReach + 3.0;
      double[] possibleEyeHeights = this.player.getPossibleEyeHeights();
      Vector3dm[] possibleLookDirs = this.player.getPossibleLookVectors(isPrediction);

      for (Vector3dm lookVec : possibleLookDirs) {
         for (double eye : possibleEyeHeights) {
            Vector3d eyePos = new Vector3d(x, y + eye, z);
            Vector3d endReachPos = eyePos.add(lookVec.getX() * distance, lookVec.getY() * distance, lookVec.getZ() * distance);
            Vector3d intercept = ReachUtils.calculateIntercept(targetBox, eyePos, endReachPos).first();
            if (ReachUtils.isVecInside(targetBox, eyePos)) {
               minDistance = 0.0;
               break;
            }

            if (intercept != null) {
               minDistance = Math.min(eyePos.distance(intercept), minDistance);
               lookVecsAndEyeHeights.add(new Pair<>(lookVec, eye));
            }
         }
      }

      if (this.hitboxDebuggingEnabled()) {
         this.sendHitboxDebugData(reachEntity, x, y, z, lookVecsAndEyeHeights, isPrediction);
      }

      HitData foundHitData = null;
      if (minDistance <= distance - 3.0 && !this.player.compensatedWorld.isNearHardEntity(this.player.boundingBox.copy().expand(4.0))) {
         Pair<Double, HitData> hitResult = WorldRayTrace.didRayTraceHit(this.player, reachEntity, lookVecsAndEyeHeights, x, y, z);
         HitData hitData = hitResult.second();
         if (hitData instanceof EntityHitData
            && this.player.compensatedEntities.getPacketEntityID(((EntityHitData)hitData).getEntity())
               != this.player.compensatedEntities.getPacketEntityID(reachEntity)) {
            minDistance = Double.MIN_VALUE;
            foundHitData = hitData;
         } else if (hitData instanceof BlockHitData && !this.blocksChangedThisTick.contains(((BlockHitData)hitData).position())) {
            minDistance = Double.MIN_VALUE;
            foundHitData = hitData;
         }
      }

      if (!blacklisted.contains(reachEntity.getType()) && reachEntity.isLivingEntity || reachEntity.getType() == EntityTypes.END_CRYSTAL) {
         if (minDistance == Double.MIN_VALUE && foundHitData != null) {
            this.cancelBuffer = 1.0;
            if (foundHitData instanceof BlockHitData) {
               return new Reach.CheckResult(
                  Reach.ResultType.WALL_HIT, 0.0, 0.0, false, "Hit block=" + ((BlockHitData)foundHitData).state().getType().getName() + " "
               );
            }

            return new Reach.CheckResult(
               Reach.ResultType.ENTITY_PIERCE, 0.0, 0.0, false, "Hit entity=" + ((EntityHitData)foundHitData).getEntity().getType().getName() + " "
            );
         }

         if (minDistance == Double.MAX_VALUE) {
            this.cancelBuffer = 1.0;
            return new Reach.CheckResult(Reach.ResultType.HITBOX, 0.0, 0.0, false);
         }

         if (minDistance > maxReach) {
            this.cancelBuffer = 1.0;
            return new Reach.CheckResult(Reach.ResultType.REACH, minDistance, movementAllowance, attackRangeMovement != null);
         }

         this.cancelBuffer = Math.max(0.0, this.cancelBuffer - 0.25);
      }

      return NONE;
   }

   private SimpleCollisionBox getTargetBox(PacketEntity reachEntity) {
      return reachEntity.getType() == EntityTypes.END_CRYSTAL
         ? new SimpleCollisionBox(
            reachEntity.trackedServerPosition.getPos().subtract(1.0, 0.0, 1.0), reachEntity.trackedServerPosition.getPos().add(1.0, 2.0, 1.0)
         )
         : reachEntity.getPossibleCollisionBoxes();
   }

   private double getAttackRangeMovementAllowance(Vector3dm attackRangeMovement) {
      return attackRangeMovement.lengthSquared() == 0.0 ? 0.0 : this.getForwardMovement(attackRangeMovement, this.player.yaw, this.player.pitch);
   }

   private double getForwardMovement(Vector3dm movement, float yaw, float pitch) {
      return Math.max(0.0, movement.dot(ReachUtils.getLook(this.player, yaw, pitch)));
   }

   private double applyReachModifiers(
      SimpleCollisionBox targetBox, boolean hasAttackRange, float itemMaxReach, float itemHitboxMargin, boolean giveMovementThreshold
   ) {
      double hitboxMargin = this.threshold;
      double maxReach;
      if (hasAttackRange) {
         maxReach = itemMaxReach;
         hitboxMargin += itemHitboxMargin;
      } else {
         maxReach = this.player.compensatedEntities.self.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
         if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
            hitboxMargin += 0.1F;
         }
      }

      if (giveMovementThreshold || this.player.canSkipTicks()) {
         hitboxMargin += this.player.getMovementThreshold();
      }

      targetBox.expand(hitboxMargin);
      return maxReach;
   }

   @Override
   public void onReload(ConfigManager config) {
      this.ignoreNonPlayerTargets = config.getBooleanElse("Reach.ignore-non-player-targets", false);
      this.cancelImpossibleHits = config.getBooleanElse("Reach.block-impossible-hits", true);
      this.threshold = config.getDoubleElse("Reach.threshold", 5.0E-4);
   }

   public void handleBlockChange(Vector3i vector3i, WrappedBlockState state) {
      if (this.blocksChangedThisTick.size() < 40) {
         if (!(GrimMath.distanceSquared(vector3i.x, vector3i.y, vector3i.z, this.player.x, this.player.y, this.player.z) > 36.0)) {
            if (!state.equals(this.player.compensatedWorld.getBlock(vector3i))) {
               this.blocksChangedThisTick.add(vector3i);
            }
         }
      }
   }

   private boolean hitboxDebuggingEnabled() {
      return this.player.checkManager.getCheck(HitboxDebugHandler.class).isEnabled();
   }

   private void sendHitboxDebugData(
      PacketEntity reachEntity, double x, double y, double z, List<Pair<Vector3dm, Double>> lookVecsAndEyeHeights, boolean isPrediction
   ) {
      Map<Integer, CollisionBox> hitboxes = new HashMap<>();

      for (Int2ObjectMap.Entry<PacketEntity> entry : this.player.compensatedEntities.entityMap.int2ObjectEntrySet()) {
         PacketEntity entity = entry.getValue();
         if (entity.canHit()) {
            CollisionBox box;
            if (entity.equals(reachEntity)) {
               box = entity.getPossibleCollisionBoxes();
               SimpleCollisionBox sBox = (SimpleCollisionBox)box;
               sBox.expand(this.threshold);
               if (!this.player.packetStateData.didLastLastMovementIncludePosition || this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                  sBox.expand(this.player.getMovementThreshold());
               }
            } else {
               box = entity.getMinimumPossibleCollisionBoxes();
               if (box instanceof NoCollisionBox) {
                  hitboxes.put(entry.getIntKey(), NoCollisionBox.INSTANCE);
                  continue;
               }

               if (box instanceof SimpleCollisionBox sBox) {
                  sBox.expand(-this.threshold);
                  if (!this.player.packetStateData.didLastLastMovementIncludePosition
                     || this.player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                     sBox.expand(-this.player.getMovementThreshold());
                  }
               }
            }

            if (this.player.getClientVersion().isOlderThan(ClientVersion.V_1_9) && box instanceof SimpleCollisionBox) {
               ((SimpleCollisionBox)box).expand(0.1F);
            }

            hitboxes.put(entry.getIntKey(), box);
         }
      }

      this.player
         .checkManager
         .getCheck(HitboxDebugHandler.class)
         .sendHitboxData(
            hitboxes,
            Collections.singleton(this.player.compensatedEntities.getPacketEntityID(reachEntity)),
            lookVecsAndEyeHeights,
            x,
            y,
            z,
            isPrediction,
            this.player.compensatedEntities.self.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)
         );
   }

   private record CheckResult(Reach.ResultType type, double minDistance, double extraMovement, boolean hasExtraMovement, String message) {
      private CheckResult(Reach.ResultType type, double minDistance, double extraMovement, boolean hasExtraMovement) {
         this(type, minDistance, extraMovement, hasExtraMovement, "");
      }

      public boolean isFlag() {
         return this.type != Reach.ResultType.NONE;
      }

      public String verbose() {
         if (!this.message.isEmpty()) {
            return this.message;
         } else if (this.type != Reach.ResultType.REACH) {
            return "";
         } else {
            String verbose = String.format("%.5f", this.minDistance) + " blocks";
            if (this.hasExtraMovement) {
               verbose = verbose + String.format(", extraMovement=%.5f", this.extraMovement);
            }

            return verbose;
         }
      }
   }

   private record InteractionData(double x, double y, double z, boolean hasAttackRange, float maxReach, float hitboxMargin, Vector3dm attackRangeMovement) {
   }

   private static enum ResultType {
      REACH,
      HITBOX,
      WALL_HIT,
      ENTITY_PIERCE,
      NONE;
   }
}
