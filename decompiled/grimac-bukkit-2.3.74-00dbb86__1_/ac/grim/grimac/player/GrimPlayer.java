package ac.grim.grimac.player;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.PacketWorld;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.handler.ResyncHandler;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.checks.impl.misc.ClientBrand;
import ac.grim.grimac.checks.impl.misc.TransactionOrder;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderProcessor;
import ac.grim.grimac.events.packets.CheckManagerListener;
import ac.grim.grimac.events.packets.PacketEntityReplication;
import ac.grim.grimac.manager.AttackCooldownHandler;
import ac.grim.grimac.manager.CheckManager;
import ac.grim.grimac.manager.LastInstanceManager;
import ac.grim.grimac.manager.PunishmentManager;
import ac.grim.grimac.manager.SetbackTeleportUtil;
import ac.grim.grimac.manager.player.features.FeatureManagerImpl;
import ac.grim.grimac.manager.player.handlers.DefaultResyncHandler;
import ac.grim.grimac.manager.player.handlers.NoOpResyncHandler;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.predictionengine.EntityFluidInteraction;
import ac.grim.grimac.predictionengine.MovementCheckRunner;
import ac.grim.grimac.predictionengine.PointThreeEstimator;
import ac.grim.grimac.predictionengine.UncertaintyHandler;
import ac.grim.grimac.predictionengine.blockeffects.CompensatedGeysers;
import ac.grim.grimac.predictionengine.blockeffects.PotentSulfurGeyser;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.ConnectionState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEquippable;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.User;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.BlockFace;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.dimension.DimensionType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisconnect;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import ac.grim.grimac.shaded.fastutil.longs.LongOpenHashSet;
import ac.grim.grimac.shaded.fastutil.longs.LongSet;
import ac.grim.grimac.shaded.fastutil.objects.Object2DoubleArrayMap;
import ac.grim.grimac.shaded.fastutil.objects.Object2DoubleMap;
import ac.grim.grimac.shaded.fastutil.objects.ObjectArrayList;
import ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.TranslatableComponent;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.change.PlayerBlockHistory;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.BlockPlaceSnapshot;
import ac.grim.grimac.utils.data.MainSupportingBlockData;
import ac.grim.grimac.utils.data.PacketStateData;
import ac.grim.grimac.utils.data.RotationData;
import ac.grim.grimac.utils.data.ShortToLongPair;
import ac.grim.grimac.utils.data.TrackerData;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.VehicleData;
import ac.grim.grimac.utils.data.VelocityData;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHappyGhast;
import ac.grim.grimac.utils.data.packetentity.PacketEntityNautilus;
import ac.grim.grimac.utils.data.packetentity.PacketEntitySelf;
import ac.grim.grimac.utils.data.tags.SyncedTags;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.enums.Pose;
import ac.grim.grimac.utils.inventory.InventoryDesyncStatus;
import ac.grim.grimac.utils.latency.CompensatedCameraEntity;
import ac.grim.grimac.utils.latency.CompensatedDashableEntities;
import ac.grim.grimac.utils.latency.CompensatedEntities;
import ac.grim.grimac.utils.latency.CompensatedFireworks;
import ac.grim.grimac.utils.latency.CompensatedInventory;
import ac.grim.grimac.utils.latency.CompensatedWorld;
import ac.grim.grimac.utils.latency.ILatencyUtils;
import ac.grim.grimac.utils.latency.LatencyUtils;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Location;
import ac.grim.grimac.utils.math.TrigHandler;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.BlockProperties;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import ac.grim.grimac.utils.nmsutil.Materials;
import ac.grim.grimac.utils.nmsutil.ReachUtils;
import ac.grim.grimac.utils.viaversion.ViaVersionUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.ProtocolPathEntry;
import com.viaversion.viaversion.api.protocol.packet.PacketTracker;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.Channel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;

public class GrimPlayer implements GrimUser {
   public static final Vector3d DEFAULT_STUCK_SPEED = new Vector3d(1.0, 1.0, 1.0);
   public final UUID uuid;
   public final User user;
   public int entityID;
   @Nullable
   public PlatformPlayer platformPlayer;
   public final Queue<ShortToLongPair> transactionsSent = new ConcurrentLinkedQueue<>();
   public final Set<Short> didWeSendThatTrans = ConcurrentHashMap.newKeySet();
   private final AtomicInteger transactionIDCounter = new AtomicInteger(0);
   public final AtomicInteger lastTransactionSent = new AtomicInteger(0);
   public final AtomicInteger lastTransactionReceived = new AtomicInteger(0);
   public final CheckManager checkManager;
   public final AttackCooldownHandler attackCooldown;
   public final PunishmentManager punishmentManager;
   public final MovementCheckRunner movementCheckRunner;
   public final SyncedTags tagManager;
   public Vector3dm clientVelocity = new Vector3dm();
   private PacketTracker viaPacketTracker;
   public final PacketOrderProcessor packetOrderProcessor = new PacketOrderProcessor(this);
   private long transactionPing = 0L;
   public long lastTransSent = 0L;
   public long lastTransReceived = 0L;
   private long playerClockAtLeast = System.nanoTime();
   public double lastWasClimbing = 0.0;
   public boolean canSwimHop = false;
   public int riptideSpinAttackTicks = 0;
   public int powderSnowFrozenTicks = 0;
   public boolean hasGravity = true;
   public final long joinTime = System.currentTimeMillis();
   public boolean playerEntityHasGravity = true;
   public VectorData predictedVelocity = new VectorData(new Vector3dm(), VectorData.VectorType.Normal);
   public Vector3dm actualMovement = new Vector3dm();
   public Vector3d stuckSpeedMultiplier = DEFAULT_STUCK_SPEED;
   public final UncertaintyHandler uncertaintyHandler;
   public double gravity;
   public float friction;
   public double speed;
   public Vector3d filterMojangStupidityOnMojangStupidity = new Vector3d();
   public double x;
   public double y;
   public double z;
   public double lastX;
   public double lastY;
   public double lastZ;
   public float yaw;
   public float pitch;
   public float lastYaw;
   public float lastPitch;
   public boolean onGround;
   public boolean lastOnGround;
   public boolean isSneaking;
   public boolean wasSneaking;
   public boolean isSprinting;
   public boolean lastSprinting;
   public boolean lastSprintingForSpeed;
   public boolean isFlying;
   public boolean canFly;
   public boolean wasFlying;
   public boolean isSwimming;
   public boolean wasSwimming;
   public boolean isClimbing;
   public boolean isGliding;
   public boolean wasGliding;
   public boolean isRiptidePose = false;
   public double fallDistance;
   public SimpleCollisionBox boundingBox;
   public Pose pose;
   public Pose lastPose;
   public boolean isSlowMovement;
   public boolean isInBed;
   public boolean lastInBed;
   public int food;
   public float depthStriderLevel;
   public float sneakingSpeedMultiplier;
   public float flySpeed;
   public final VehicleData vehicleData;
   public boolean clientClaimsLastOnGround;
   public boolean wasTouchingWater;
   public boolean wasWasTouchingWater;
   public boolean wasTouchingLava;
   public boolean wasEyeInWater;
   public FluidTag fluidOnEyes;
   public boolean softHorizontalCollision;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean clientControlledVerticalCollision;
   public boolean couldSkipTick;
   public boolean skippedTickInActualMovement;
   public final LastInstanceManager lastInstanceManager;
   public final CompensatedFireworks fireworks;
   public final CompensatedWorld compensatedWorld;
   public final CompensatedGeysers compensatedGeysers;
   public final CompensatedEntities compensatedEntities;
   public final CompensatedDashableEntities dashableEntities;
   public final CompensatedInventory inventory;
   public final PacketEntityReplication packetEntityReplication;
   public final ILatencyUtils latencyUtils;
   public final PointThreeEstimator pointThreeEstimator;
   public final TrigHandler trigHandler;
   public final PacketStateData packetStateData;
   public Vector3dm baseTickAddition;
   public Vector3dm baseTickWaterPushing;
   public Vector3dm startTickClientVel;
   public int movementPackets;
   public VelocityData firstBreadKB;
   public VelocityData likelyKB;
   public VelocityData firstBreadExplosion;
   public VelocityData likelyExplosions;
   public int minAttackSlow;
   public int maxAttackSlow;
   public GameMode gamemode;
   public DimensionType dimensionType;
   @Nullable
   public String worldName;
   public Vector3d bedPosition;
   public long lastBlockPlaceUseItem;
   public long lastBlockBreak;
   public final AtomicInteger cancelledPackets;
   public MainSupportingBlockData mainSupportingBlockData;
   public final Object2DoubleMap<FluidTag> fluidHeight;
   public final double[][] possibleEyeHeights;
   public boolean hasInventoryOpen;
   public long lastInventoryOpen;
   public InventoryDesyncStatus inventoryDesyncStatus;
   public final Queue<BlockPlaceSnapshot> placeUseItemPackets;
   public final Queue<BlockBreak> queuedBreaks;
   public final PlayerBlockHistory blockHistory;
   public final ArrayDeque<RotationData> pendingRotations;
   public final CompensatedCameraEntity cameraEntity;
   private ResyncHandler resyncHandler;
   private final FeatureManagerImpl featureManager;
   public boolean serverOpenedInventoryThisTick;
   public boolean isInNetherPortal;
   private boolean debugPacketCancel;
   private int spamThreshold;
   private int maxTransactionTime;
   private boolean ignoreDuplicatePacketRotation;
   private boolean experimentalChecks;
   private boolean cancelDuplicatePacket;
   private boolean exemptElytra;
   private boolean forceStuckSpeed;
   private boolean forceSlowMovement;
   private boolean resetItemUsageOnAttack;
   private boolean resetItemUsageOnItemUpdate;
   private boolean resetItemUsageOnSlotChange;
   private boolean resetItemUsageOnItemUse;
   public boolean noModifyPacketPermission;
   public boolean noSetbackPermission;
   public boolean disableGrim;
   public final ArrayDeque<GrimPlayer.Movement> movementThisTick;
   public final List<GrimPlayer.Movement> finalMovementsThisTick;
   public final LongSet visitedBlocks;
   @Nullable
   private UserConnection viaUserConnection;
   public boolean wasLastPredictionCompleteChecked;
   public boolean isJumping;
   public boolean lastJumping;
   public EntityFluidInteraction fluidInteraction;
   public boolean canFloatWhileRidden;
   private final AtomicBoolean hasDisconnected;

   public GrimPlayer(@NotNull User user) {
      this.boundingBox = GetBoundingBox.getBoundingBoxFromPosAndSizeRaw(this.x, this.y, this.z, 0.6F, 1.8F);
      this.pose = Pose.STANDING;
      this.lastPose = Pose.STANDING;
      this.isSlowMovement = false;
      this.isInBed = false;
      this.lastInBed = false;
      this.food = 20;
      this.sneakingSpeedMultiplier = 0.3F;
      this.vehicleData = new VehicleData();
      this.wasTouchingWater = false;
      this.wasWasTouchingWater = false;
      this.wasTouchingLava = false;
      this.wasEyeInWater = false;
      this.couldSkipTick = false;
      this.skippedTickInActualMovement = false;
      this.packetEntityReplication = new PacketEntityReplication(this);
      this.latencyUtils = new LatencyUtils(this);
      this.trigHandler = new TrigHandler(this);
      this.packetStateData = new PacketStateData();
      this.baseTickAddition = new Vector3dm();
      this.baseTickWaterPushing = new Vector3dm();
      this.startTickClientVel = new Vector3dm();
      this.movementPackets = 0;
      this.firstBreadKB = null;
      this.likelyKB = null;
      this.firstBreadExplosion = null;
      this.likelyExplosions = null;
      this.minAttackSlow = 0;
      this.maxAttackSlow = 0;
      this.lastBlockPlaceUseItem = 0L;
      this.lastBlockBreak = 0L;
      this.cancelledPackets = new AtomicInteger(0);
      this.mainSupportingBlockData = new MainSupportingBlockData(null, false);
      this.fluidHeight = new Object2DoubleArrayMap<>(2);
      this.possibleEyeHeights = new double[3][];
      this.placeUseItemPackets = new LinkedBlockingQueue<>();
      this.queuedBreaks = new LinkedBlockingQueue<>();
      this.blockHistory = new PlayerBlockHistory();
      this.pendingRotations = new ArrayDeque<>();
      this.resyncHandler = (ResyncHandler)(GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("disable-default-resync-handler", false)
         ? NoOpResyncHandler.INSTANCE
         : new DefaultResyncHandler(this));
      this.featureManager = new FeatureManagerImpl(this);
      this.debugPacketCancel = false;
      this.spamThreshold = 100;
      this.maxTransactionTime = 60;
      this.ignoreDuplicatePacketRotation = false;
      this.experimentalChecks = false;
      this.cancelDuplicatePacket = true;
      this.exemptElytra = false;
      this.forceStuckSpeed = true;
      this.forceSlowMovement = true;
      this.noModifyPacketPermission = false;
      this.noSetbackPermission = false;
      this.disableGrim = false;
      this.movementThisTick = new ArrayDeque<>(8);
      this.finalMovementsThisTick = new ObjectArrayList<>();
      this.visitedBlocks = new LongOpenHashSet();
      this.fluidInteraction = new EntityFluidInteraction(FluidTag.WATER, FluidTag.LAVA);
      this.canFloatWhileRidden = false;
      this.hasDisconnected = new AtomicBoolean(false);
      this.user = user;
      this.uuid = user.getUUID();
      this.fireworks = new CompensatedFireworks(this);
      this.inventory = new CompensatedInventory(this);
      this.compensatedWorld = new CompensatedWorld(this);
      this.compensatedGeysers = new CompensatedGeysers();
      this.compensatedEntities = new CompensatedEntities(this);
      this.dashableEntities = new CompensatedDashableEntities();
      this.cameraEntity = new CompensatedCameraEntity(this);
      this.lastInstanceManager = new LastInstanceManager(this);
      this.attackCooldown = new AttackCooldownHandler(this);
      this.checkManager = new CheckManager(this);
      this.punishmentManager = new PunishmentManager(this);
      this.tagManager = new SyncedTags(this);
      this.movementCheckRunner = new MovementCheckRunner(this);
      this.uncertaintyHandler = new UncertaintyHandler(this);
      this.pointThreeEstimator = new PointThreeEstimator(this);
      if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
         float scale = (float)this.compensatedEntities.self.getAttributeValue(Attributes.SCALE);
         this.possibleEyeHeights[2] = new double[]{0.4 * scale, 1.62 * scale, 1.27 * scale};
         this.possibleEyeHeights[1] = new double[]{1.27 * scale, 1.62 * scale, 0.4 * scale};
         this.possibleEyeHeights[0] = new double[]{1.62 * scale, 1.27 * scale, 0.4 * scale};
      } else if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
         this.possibleEyeHeights[2] = new double[]{0.4, 1.62, 1.54};
         this.possibleEyeHeights[1] = new double[]{1.54, 1.62, 0.4};
         this.possibleEyeHeights[0] = new double[]{1.62, 1.54, 0.4};
      } else {
         this.possibleEyeHeights[1] = new double[]{1.54F, 1.62F};
         this.possibleEyeHeights[0] = new double[]{1.62F, 1.54F};
      }

      this.reload();
   }

   public void onPacketCancel() {
      if (this.spamThreshold != -1 && this.cancelledPackets.incrementAndGet() > this.spamThreshold) {
         LogUtil.info("Disconnecting " + this.getName() + " for spamming invalid packets, packets cancelled within a second " + this.cancelledPackets);
         this.disconnect(MessageUtil.miniMessage(MessageUtil.replacePlaceholders(this, GrimAPI.INSTANCE.getConfigManager().getDisconnectClosed())));
         this.cancelledPackets.set(0);
         if (this.debugPacketCancel) {
            try {
               throw new Exception();
            } catch (Exception var2) {
               LogUtil.error("Stacktrace for onPacketCancel (debug-packet-cancel=true)", var2);
            }
         }
      }
   }

   public Set<VectorData> getPossibleVelocities() {
      Set<VectorData> set = new HashSet<>();
      if (this.firstBreadKB != null) {
         set.add(new VectorData(this.firstBreadKB.vector.clone(), VectorData.VectorType.Knockback).returnNewModified(VectorData.VectorType.FirstBreadKnockback));
      }

      if (this.likelyKB != null) {
         set.add(new VectorData(this.likelyKB.vector.clone(), VectorData.VectorType.Knockback));
      }

      set.addAll(this.getPossibleVelocitiesMinusKnockback());
      return set;
   }

   public Set<VectorData> getPossibleVelocitiesMinusKnockback() {
      Set<VectorData> possibleMovements = new HashSet<>();
      possibleMovements.add(new VectorData(this.clientVelocity, VectorData.VectorType.Normal));
      if (this.canSwimHop && !this.onGround) {
         Vector3dm vector = this.clientVelocity.clone().setY(0.3F + (this.canFloatWhileRidden ? 0.04F : 0.0));
         if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)) {
            Collisions.resolveBlockEffects(this, vector, true, this.finalMovementsThisTick);
         }

         if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_26_2)) {
            PotentSulfurGeyser.launchEntityTicker(this, vector, false);
         }

         possibleMovements.add(new VectorData(vector, VectorData.VectorType.Swimhop));
      }

      if (this.riptideSpinAttackTicks >= 0 && Collections.max(this.uncertaintyHandler.riptideEntities) > 0) {
         possibleMovements.add(new VectorData(this.clientVelocity.clone().multiply(-0.2), VectorData.VectorType.Trident));
      }

      if (this.lastWasClimbing != 0.0) {
         possibleMovements.add(
            new VectorData(this.clientVelocity.clone().setY(this.lastWasClimbing + this.baseTickAddition.getY()), VectorData.VectorType.Climbable)
         );
      }

      for (VectorData data : new HashSet<>(possibleMovements)) {
         for (BlockFace direction : this.uncertaintyHandler.slimePistonBounces) {
            if (direction.getModX() != 0) {
               possibleMovements.add(data.returnNewModified(data.vector.clone().setX(direction.getModX()), VectorData.VectorType.SlimePistonBounce));
            } else if (direction.getModY() != 0) {
               possibleMovements.add(data.returnNewModified(data.vector.clone().setY(direction.getModY()), VectorData.VectorType.SlimePistonBounce));
            } else if (direction.getModZ() != 0) {
               possibleMovements.add(data.returnNewModified(data.vector.clone().setZ(direction.getModZ()), VectorData.VectorType.SlimePistonBounce));
            }
         }
      }

      return possibleMovements;
   }

   public boolean addTransactionResponse(short id) {
      ShortToLongPair data = null;
      boolean hasID = false;
      int skipped = 0;

      for (ShortToLongPair iterator : this.transactionsSent) {
         if (iterator.first() == id) {
            hasID = true;
            break;
         }

         skipped++;
      }

      if (hasID) {
         if (this.viaPacketTracker != null) {
            this.viaPacketTracker.setIntervalPackets(this.viaPacketTracker.getIntervalPackets() - 1L);
         }

         if (skipped > 0 && System.currentTimeMillis() - this.joinTime > 5000L) {
            this.checkManager.getCheck(TransactionOrder.class).flag("skipped=" + skipped);
         }

         do {
            data = this.transactionsSent.poll();
            if (data == null) {
               break;
            }

            this.lastTransactionReceived.incrementAndGet();
            this.lastTransReceived = System.currentTimeMillis();
            this.transactionPing = System.nanoTime() - data.second();
            this.playerClockAtLeast = data.second();
         } while (data.first() != id);

         CheckManagerListener.handleQueuedPlaces(this, false, 0.0F, 0.0F, System.currentTimeMillis());
         CheckManagerListener.handleQueuedBreaks(this, false, 0.0F, 0.0F, System.currentTimeMillis());
         this.latencyUtils.handleNettySyncTransaction(this.lastTransactionReceived.get());
      }

      return data != null;
   }

   public void baseTickAddWaterPushing(Vector3dm vector) {
      this.baseTickWaterPushing.add(vector);
   }

   public void baseTickAddVector(Vector3dm vector) {
      this.clientVelocity.add(vector);
   }

   public void trackBaseTickAddition(Vector3dm vector) {
      this.baseTickAddition.add(vector);
   }

   public float getMaxUpStep() {
      PacketEntitySelf self = this.compensatedEntities.self;
      PacketEntity riding = self.getRiding();
      if (riding == null) {
         return (float)self.getAttributeValue(Attributes.STEP_HEIGHT);
      } else if (riding.isBoat) {
         return 0.0F;
      } else {
         float value = (float)riding.getAttributeValue(Attributes.STEP_HEIGHT);
         if (riding.isHappyGhast) {
            return ((PacketEntityHappyGhast)riding).isControllingPassenger() ? Math.max(value, 1.0F) : value;
         } else {
            return value;
         }
      }
   }

   public void sendTransaction() {
      this.sendTransaction(false);
   }

   public void sendTransaction(boolean async) {
      if (this.user.getEncoderState() == ConnectionState.PLAY) {
         if (!this.disableGrim || !(System.nanoTime() - this.getPlayerClockAtLeast() > 1.5E10)) {
            this.lastTransSent = System.currentTimeMillis();
            short transactionID = (short)(-1 * (this.transactionIDCounter.getAndIncrement() & 32767));

            try {
               PacketWrapper<?> packet;
               if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
                  packet = new WrapperPlayServerPing(transactionID);
               } else {
                  packet = new WrapperPlayServerWindowConfirmation(0, transactionID, false);
               }

               if (async) {
                  this.runSafely(() -> {
                     this.addTransactionSend(transactionID);
                     this.user.writePacket(packet);
                  });
               } else {
                  this.addTransactionSend(transactionID);
                  this.user.writePacket(packet);
               }
            } catch (Exception var4) {
            }
         }
      }
   }

   public void addTransactionSend(short id) {
      this.didWeSendThatTrans.add(id);
   }

   public double getEyeHeight() {
      return this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? this.pose.eyeHeight : (this.isSneaking ? 1.54F : 1.62F);
   }

   public void timedOut() {
      this.disconnect(MessageUtil.miniMessage(MessageUtil.replacePlaceholders(this, GrimAPI.INSTANCE.getConfigManager().getDisconnectTimeout())));
   }

   public void disconnect(Component reason) {
      if (this.hasDisconnected.compareAndSet(false, true)) {
         String textReason;
         if (reason instanceof TranslatableComponent translatableComponent) {
            textReason = translatableComponent.key();
         } else {
            textReason = LegacyComponentSerializer.legacySection().serialize(reason);
         }

         LogUtil.info("Disconnecting " + this.user.getProfile().getName() + " for " + MessageUtil.stripColor(textReason));

         try {
            this.user.sendPacket((PacketWrapper<?>)(new WrapperPlayServerDisconnect(reason)));
         } catch (Exception var4) {
            LogUtil.warn("Failed to send disconnect packet to disconnect " + this.user.getProfile().getName() + "! Disconnecting anyways.");
         }

         this.user.closeConnection();
         if (this.platformPlayer != null) {
            GrimAPI.INSTANCE
               .getScheduler()
               .getEntityScheduler()
               .execute(this.platformPlayer, GrimAPI.INSTANCE.getGrimPlugin(), () -> this.platformPlayer.kickPlayer(textReason), null, 1L);
         }
      }
   }

   public void pollData() {
      if (this.lastTransSent != 0L && this.lastTransSent + 80L < System.currentTimeMillis()) {
         this.sendTransaction(true);
      }

      if (System.nanoTime() - this.getPlayerClockAtLeast() > this.maxTransactionTime * 1.0E9) {
         this.timedOut();
      }

      if (!GrimAPI.INSTANCE.getPlayerDataManager().shouldCheck(this.user)) {
         GrimAPI.INSTANCE.getPlayerDataManager().remove(this.user);
      }

      if (this.viaPacketTracker == null && ViaVersionUtil.isAvailable && this.uuid != null) {
         UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(this.uuid);
         this.viaPacketTracker = connection != null ? connection.getPacketTracker() : null;
         this.viaUserConnection = connection;
      }

      if (this.uuid != null && this.platformPlayer == null) {
         this.platformPlayer = GrimAPI.INSTANCE.getPlatformPlayerFactory().getFromUUID(this.uuid);
         this.updatePermissions();
      }

      if (this.uuid != null) {
         GrimAPI.INSTANCE.getDataStoreLifecycle().sessionTracker().pollHeartbeat(this.uuid, System.currentTimeMillis());
      }
   }

   public void updateVelocityMovementSkipping() {
      if (!this.couldSkipTick) {
         this.couldSkipTick = this.pointThreeEstimator
            .determineCanSkipTick(
               BlockProperties.getFrictionInfluencedSpeed((float)(this.speed * (this.isSprinting ? 1.3 : 1.0)), this),
               this.getPossibleVelocitiesMinusKnockback()
            );
      }

      Set<VectorData> knockback = new HashSet<>();
      if (this.firstBreadKB != null) {
         knockback.add(new VectorData(this.firstBreadKB.vector, VectorData.VectorType.Knockback));
      }

      if (this.likelyKB != null) {
         knockback.add(new VectorData(this.likelyKB.vector, VectorData.VectorType.Knockback));
      }

      boolean kbPointThree = this.pointThreeEstimator
         .determineCanSkipTick(BlockProperties.getFrictionInfluencedSpeed((float)(this.speed * (this.isSprinting ? 1.3 : 1.0)), this), knockback);
      this.checkManager.getKnockbackHandler().setPointThree(kbPointThree);
      Set<VectorData> explosion = new HashSet<>();
      if (this.firstBreadExplosion != null) {
         explosion.add(new VectorData(this.firstBreadExplosion.vector, VectorData.VectorType.Explosion));
      }

      if (this.likelyExplosions != null) {
         explosion.add(new VectorData(this.likelyExplosions.vector, VectorData.VectorType.Explosion));
      }

      boolean explosionPointThree = this.pointThreeEstimator
         .determineCanSkipTick(BlockProperties.getFrictionInfluencedSpeed((float)(this.speed * (this.isSprinting ? 1.3 : 1.0)), this), explosion);
      this.checkManager.getExplosionHandler().setPointThree(explosionPointThree);
      if (kbPointThree || explosionPointThree) {
         this.uncertaintyHandler.lastPointThree.reset();
      }
   }

   @Override
   public void updatePermissions() {
      this.runSafely(() -> {
         try {
            boolean noModifyPacketPermission = this.hasPermission("grim.nomodifypacket");
            boolean noSetbackPermission = this.hasPermission("grim.nosetback");
            boolean disabledPermission = this.hasPermission("grim.disabled");
            boolean exemptPermission = this.hasPermission("grim.exempt");

            for (AbstractCheck check : this.checkManager.allChecks.values()) {
               if (check instanceof Check c) {
                  c.updatePermissions();
               }
            }

            this.noModifyPacketPermission = noModifyPacketPermission;
            this.noSetbackPermission = noSetbackPermission;
            this.disableGrim = disabledPermission;
            if (exemptPermission) {
               GrimAPI.INSTANCE.getPlayerDataManager().exemptUser(this.user);
            }
         } catch (Exception var8) {
            LogUtil.error("Failed to update permissions for " + this.getName() + "!", var8);
         }
      });
   }

   public boolean isPointThree() {
      return this.getClientVersion().isOlderThan(ClientVersion.V_1_18_2);
   }

   public double getMovementThreshold() {
      return this.isPointThree() ? 0.03 : 2.0E-4;
   }

   public ClientVersion getClientVersion() {
      return Objects.requireNonNullElseGet(
         this.user.getClientVersion(), () -> ClientVersion.getById(PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion())
      );
   }

   public boolean isTickingReliablyFor(int ticks) {
      return !this.cameraEntity.isSelf()
         ? false
         : !this.canSkipTicks()
            || (this.inVehicle() || !this.uncertaintyHandler.lastPointThree.hasOccurredSince(ticks))
               && !this.uncertaintyHandler.lastVehicleSwitch.hasOccurredSince(1);
   }

   public boolean inVehicle() {
      return this.compensatedEntities.self.inVehicle();
   }

   public PacketEntity getVehicle() {
      return this.compensatedEntities.self.riding;
   }

   public EntityType getVehicleType() {
      return this.inVehicle() ? this.getVehicle().getType() : null;
   }

   public double[] getPossibleEyeHeights() {
      if (this.getClientVersion().isOlderThan(ClientVersion.V_1_9)) {
         return this.isSneaking ? this.possibleEyeHeights[1] : this.possibleEyeHeights[0];
      } else {
         return switch (this.pose) {
            case FALL_FLYING, SPIN_ATTACK, SWIMMING -> this.possibleEyeHeights[2];
            case NINE_CROUCHING, CROUCHING -> this.possibleEyeHeights[1];
            default -> this.possibleEyeHeights[0];
         };
      }
   }

   public Vector3dm[] getPossibleLookVectors(boolean isPrediction) {
      List<Vector3dm> possibleLookDirs = new ArrayList<>(Collections.singletonList(ReachUtils.getLook(this, this.yaw, this.pitch)));
      if (!isPrediction) {
         possibleLookDirs.add(ReachUtils.getLook(this, this.lastYaw, this.pitch));
         if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
            possibleLookDirs.add(ReachUtils.getLook(this, this.lastYaw, this.lastPitch));
         }

         if (this.getClientVersion().isOlderThan(ClientVersion.V_1_8)) {
            possibleLookDirs = Collections.singletonList(ReachUtils.getLook(this, this.yaw, this.pitch));
         }
      }

      return possibleLookDirs.toArray(new Vector3dm[0]);
   }

   @Override
   public PacketWorld getPacketWorld() {
      return this.compensatedWorld;
   }

   @Override
   public int getTransactionPing() {
      return GrimMath.floor(this.transactionPing / 1000000.0);
   }

   @Override
   public int getKeepAlivePing() {
      return this.platformPlayer == null ? -1 : PacketEvents.getAPI().getPlayerManager().getPing(this.platformPlayer.getNative());
   }

   public SetbackTeleportUtil getSetbackTeleportUtil() {
      return this.checkManager.getSetbackUtil();
   }

   public boolean wouldCollisionResultFlagGroundSpoof(double inputY, double collisionY) {
      boolean verticalCollision = inputY != collisionY;
      boolean calculatedOnGround = verticalCollision && inputY < 0.0;
      if (this.exemptOnGround()) {
         return false;
      } else {
         return inputY == -1.0E-7 && collisionY > -1.0E-7 && collisionY <= 0.0 ? false : calculatedOnGround != this.onGround;
      }
   }

   public boolean exemptOnGround() {
      return this.inVehicle()
         || Collections.max(this.uncertaintyHandler.pistonX) != 0.0
         || Collections.max(this.uncertaintyHandler.pistonY) != 0.0
         || Collections.max(this.uncertaintyHandler.pistonZ) != 0.0
         || this.uncertaintyHandler.isStepMovement
         || this.isFlying
         || this.compensatedEntities.self.isDead
         || this.isInBed
         || this.lastInBed
         || this.uncertaintyHandler.lastFlyingStatusChange.hasOccurredSince(30)
         || this.uncertaintyHandler.lastHardCollidingLerpingEntity.hasOccurredSince(3)
         || this.uncertaintyHandler.isOrWasNearGlitchyBlock;
   }

   public void handleMountVehicle(int vehicleID) {
      this.compensatedEntities.serverPlayerVehicle = vehicleID;
      TrackerData data = this.compensatedEntities.getTrackedEntity(vehicleID);
      if (data != null
         && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
         && this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)
         && (
            EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.BOAT)
               || EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.ABSTRACT_HORSE)
               || data.getEntityType() == EntityTypes.PIG
               || data.getEntityType() == EntityTypes.STRIDER
               || EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.CAMEL)
               || data.getEntityType() == EntityTypes.HAPPY_GHAST
               || EntityTypes.isTypeInstanceOf(data.getEntityType(), EntityTypes.ABSTRACT_NAUTILUS)
         )) {
         this.user.writePacket((PacketWrapper<?>)(new WrapperPlayServerEntityVelocity(vehicleID, new Vector3d())));
      }

      this.sendTransaction();
      this.latencyUtils.addRealTimeTask(this.lastTransactionSent.get(), () -> this.vehicleData.wasVehicleSwitch = true);
   }

   public int getRidingVehicleId() {
      return this.compensatedEntities.getPacketEntityID(this.compensatedEntities.self.getRiding());
   }

   public void handleDismountVehicle(PacketSendEvent event) {
      EntityType entityType = this.getVehicleType();
      this.sendTransaction();
      this.compensatedEntities.serverPlayerVehicle = null;
      event.getTasksAfterSend()
         .add(
            () -> {
               if (this.inVehicle()) {
                  int ridingId = this.getRidingVehicleId();
                  TrackerData data = this.compensatedEntities.serverPositionsMap.get(ridingId);
                  if (data != null) {
                     this.user
                        .writePacket(
                           (PacketWrapper<?>)(new WrapperPlayServerEntityTeleport(
                              ridingId, new Vector3d(data.getX(), data.getY(), data.getZ()), data.getXRot(), data.getYRot(), false
                           ))
                        );
                  }
               }
            }
         );
      this.latencyUtils
         .addRealTimeTask(
            this.lastTransactionSent.get(),
            () -> {
               this.vehicleData.wasVehicleSwitch = true;
               if (this.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14)
                  || this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_5) && EntityTypes.MINECART == entityType) {
                  this.compensatedEntities.hasSprintingAttributeEnabled = false;
               }
            }
         );
   }

   public boolean canGlide() {
      ItemStack chestPlate = this.inventory.getChestplate();
      if (chestPlate.getType() == ItemTypes.ELYTRA && chestPlate.getDamageValue() < chestPlate.getMaxDamage() - 1) {
         return true;
      } else {
         return !this.getClientVersion().isOlderThan(ClientVersion.V_1_21_2)
               && !PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_2)
            ? isGlider(this.inventory.getHelmet(), EquipmentSlot.CHEST_PLATE)
               || isGlider(this.inventory.getChestplate(), EquipmentSlot.LEGGINGS)
               || isGlider(this.inventory.getLeggings(), EquipmentSlot.BOOTS)
               || isGlider(this.inventory.getBoots(), EquipmentSlot.OFF_HAND)
               || isGlider(this.inventory.getOffHand(), EquipmentSlot.HELMET)
            : false;
      }
   }

   private static boolean isGlider(ItemStack stack, EquipmentSlot slot) {
      if (stack.hasComponent(ComponentTypes.GLIDER) && (!stack.canBeDepleted() || stack.getDamageValue() < stack.getMaxDamage() - 1)) {
         Optional<ItemEquippable> equippable = stack.getComponent(ComponentTypes.EQUIPPABLE);
         return equippable.isPresent() && equippable.get().getSlot() == slot;
      } else {
         return false;
      }
   }

   public void resyncPose() {
      if (this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) && this.platformPlayer != null) {
         this.platformPlayer.setSneaking(!this.platformPlayer.isSneaking());
      }
   }

   public boolean canPlaceGameMasterBlocks() {
      return this.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_10) || this.canUseGameMasterBlocks();
   }

   public boolean canUseGameMasterBlocks() {
      return this.gamemode == GameMode.CREATIVE && this.compensatedEntities.self.opLevel >= 2;
   }

   public boolean isInWaterOrRain() {
      return this.compensatedWorld.isRaining
         || Collisions.hasMaterial(this, this.boundingBox.copy().expand(0.1F), (block, x, y, z) -> Materials.isWater(CompensatedWorld.blockVersion, block));
   }

   public void updateNetherPortalState() {
      SimpleCollisionBox movementThisTick = GetBoundingBox.getCollisionBoxForPlayer(this, this.x, this.y, this.z)
         .expandToCoordinate(this.lastX - this.x, this.lastY - this.y, this.lastZ - this.z);
      this.isInNetherPortal = this.compensatedWorld.containsNetherPortal(movementThisTick);
   }

   @Contract(
      pure = true
   )
   public boolean supportsEndTickPreVia() {
      return this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2);
   }

   @Contract(
      pure = true
   )
   public boolean supportsEndTick() {
      return this.supportsEndTickPreVia() && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);
   }

   @Contract(
      pure = true
   )
   public boolean canSkipTicksPreVia() {
      return this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && !this.supportsEndTickPreVia();
   }

   @Contract(
      pure = true
   )
   public boolean canSkipTicks() {
      return this.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && !this.supportsEndTick();
   }

   @Override
   public void runSafely(Runnable runnable) {
      ChannelHelper.runInEventLoop(this.user.getChannel(), runnable);
   }

   @Override
   public int getLastTransactionReceived() {
      return this.lastTransactionReceived.get();
   }

   @Override
   public int getLastTransactionSent() {
      return this.lastTransactionSent.get();
   }

   @Override
   public void addRealTimeTask(int transaction, Runnable runnable) {
      this.latencyUtils.addRealTimeTask(transaction, runnable);
   }

   @Override
   public String getName() {
      return this.user.getName();
   }

   @Override
   public UUID getUniqueId() {
      return this.user.getProfile().getUUID();
   }

   @Override
   public String getBrand() {
      return this.checkManager.getPacketCheck(ClientBrand.class).getBrand();
   }

   @Nullable
   @Override
   public String getWorldName() {
      return this.platformPlayer != null ? this.platformPlayer.getWorld().getName() : null;
   }

   @Nullable
   @Override
   public UUID getWorldUID() {
      return this.platformPlayer != null ? this.platformPlayer.getWorld().getUID() : null;
   }

   @Override
   public String getVersionName() {
      return this.getClientVersion().getReleaseName();
   }

   @Override
   public double getHorizontalSensitivity() {
      return this.checkManager.getRotationCheck(AimProcessor.class).sensitivityX;
   }

   @Override
   public double getVerticalSensitivity() {
      return this.checkManager.getRotationCheck(AimProcessor.class).sensitivityY;
   }

   @Override
   public boolean isVanillaMath() {
      return this.trigHandler.isVanillaMath();
   }

   @Override
   public Collection<? extends AbstractCheck> getChecks() {
      return this.checkManager.allChecks.values();
   }

   public void runNettyTaskInMs(Runnable runnable, int ms) {
      ((Channel)this.user.getChannel()).eventLoop().schedule(runnable, ms, TimeUnit.MILLISECONDS);
   }

   public final void reload(ConfigManager config) {
      this.updatePermissions();
      this.featureManager.onReload(config);
      this.debugPacketCancel = config.getBooleanElse("debug-packet-cancel", false);
      this.spamThreshold = config.getIntElse("packet-spam-threshold", 100);
      this.maxTransactionTime = GrimMath.clamp(config.getIntElse("max-transaction-time", 60), 1, 180);
      this.ignoreDuplicatePacketRotation = config.getBooleanElse("ignore-duplicate-packet-rotation", false);
      this.cancelDuplicatePacket = config.getBooleanElse("cancel-duplicate-packet", true);
      boolean shouldDisableResync = config.getBooleanElse("disable-default-resync-handler", false);
      Class<?> currentHandlerClass = this.resyncHandler.getClass();
      boolean isInternalHandler = currentHandlerClass == DefaultResyncHandler.class || currentHandlerClass == NoOpResyncHandler.class;
      if (isInternalHandler) {
         if (shouldDisableResync) {
            if (currentHandlerClass != NoOpResyncHandler.class) {
               this.resyncHandler = NoOpResyncHandler.INSTANCE;
            }
         } else if (currentHandlerClass != DefaultResyncHandler.class) {
            this.resyncHandler = new DefaultResyncHandler(this);
         }
      }

      this.resetItemUsageOnAttack = config.getBooleanElse("reset-item-usage-on-attack", true);
      this.resetItemUsageOnItemUpdate = config.getBooleanElse("reset-item-usage-on-item-update", true);
      this.resetItemUsageOnSlotChange = config.getBooleanElse("reset-item-usage-on-slot-change", true);
      this.resetItemUsageOnItemUse = config.getBooleanElse("reset-item-usage-on-item-use", true);

      for (AbstractCheck value : this.checkManager.allChecks.values()) {
         value.reload();
      }

      this.punishmentManager.reload(config);
   }

   @Override
   public void reload() {
      this.reload(GrimAPI.INSTANCE.getConfigManager().getConfig());
   }

   @Override
   public void sendMessage(String message) {
      if (this.platformPlayer != null) {
         this.platformPlayer.sendMessage(message);
      }
   }

   @Override
   public boolean hasPermission(String s) {
      return this.platformPlayer != null && this.platformPlayer.hasPermission(s);
   }

   public boolean hasPermission(String s, boolean defaultIfUnset) {
      return this.platformPlayer != null && this.platformPlayer.hasPermission(s, defaultIfUnset);
   }

   public void sendMessage(Component message) {
      if (this.platformPlayer != null) {
         this.platformPlayer.sendMessage(message);
      }
   }

   public void resyncPosition(Vector3i pos) {
      this.resyncHandler.resync(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
   }

   public void resyncPosition(Vector3i pos, int sequenceID) {
      this.resyncHandler.resyncPosition(pos.x, pos.y, pos.z, sequenceID);
   }

   public void resyncPositions(SimpleCollisionBox box) {
      this.resyncHandler
         .resync(
            GrimMath.floor(box.minX),
            GrimMath.floor(box.minY),
            GrimMath.floor(box.minZ),
            GrimMath.ceil(box.maxX),
            GrimMath.ceil(box.maxY),
            GrimMath.ceil(box.maxZ)
         );
   }

   public void addMovementThisTick(GrimPlayer.Movement movement) {
      if (this.movementThisTick.size() >= 100) {
         GrimPlayer.Movement movement1 = this.movementThisTick.removeFirst();
         GrimPlayer.Movement movement2 = this.movementThisTick.removeFirst();
         GrimPlayer.Movement movement3 = new GrimPlayer.Movement(movement1.from(), movement2.to());
         this.movementThisTick.addFirst(movement3);
      }

      this.movementThisTick.add(movement);
   }

   public Location getLocation() {
      return new Location(this.platformPlayer.getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
   }

   public int getViaTranslatedClientBlockID(int blockStateId) {
      if (this.viaUserConnection == null) {
         return blockStateId;
      } else {
         ProtocolVersion clientVersion = this.viaUserConnection.getProtocolInfo().protocolVersion();
         ProtocolVersion serverVersion = this.viaUserConnection.getProtocolInfo().serverProtocolVersion();
         List<ProtocolPathEntry> protocolPath = Via.getManager().getProtocolManager().getProtocolPath(clientVersion, serverVersion);
         if (protocolPath == null) {
            return blockStateId;
         } else {
            for (int i = protocolPath.size() - 1; i >= 0; i--) {
               Protocol<?, ?, ?, ?> protocol = protocolPath.get(i).protocol();
               if (protocol.getMappingData() != null && protocol.getMappingData().getBlockStateMappings() != null) {
                  blockStateId = protocol.getMappingData().getNewBlockStateId(blockStateId);
               }
            }

            return blockStateId;
         }
      }
   }

   public double getFluidHeight(FluidTag fluidTag) {
      return this.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_21_11)
         ? this.fluidHeight.getDouble(fluidTag)
         : this.fluidInteraction.getFluidHeight(fluidTag);
   }

   public boolean isEyeInFluid(FluidTag fluidTag) {
      return this.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_21_11) ? this.fluidOnEyes == fluidTag : this.fluidInteraction.isEyeInFluid(fluidTag);
   }

   public boolean isPushedByFluid() {
      if (!this.inVehicle()) {
         return !this.isFlying;
      } else {
         PacketEntity vehicle = this.getVehicle();
         return !(vehicle instanceof PacketEntityNautilus);
      }
   }

   @Generated
   public long getPlayerClockAtLeast() {
      return this.playerClockAtLeast;
   }

   @Generated
   @Override
   public ResyncHandler getResyncHandler() {
      return this.resyncHandler;
   }

   @Generated
   @Override
   public void setResyncHandler(ResyncHandler resyncHandler) {
      this.resyncHandler = resyncHandler;
   }

   @Generated
   public FeatureManagerImpl getFeatureManager() {
      return this.featureManager;
   }

   @Generated
   public boolean isIgnoreDuplicatePacketRotation() {
      return this.ignoreDuplicatePacketRotation;
   }

   @Generated
   public boolean isExperimentalChecks() {
      return this.experimentalChecks;
   }

   @Generated
   public void setExperimentalChecks(boolean experimentalChecks) {
      this.experimentalChecks = experimentalChecks;
   }

   @Generated
   public boolean isCancelDuplicatePacket() {
      return this.cancelDuplicatePacket;
   }

   @Generated
   public boolean isExemptElytra() {
      return this.exemptElytra;
   }

   @Generated
   public void setExemptElytra(boolean exemptElytra) {
      this.exemptElytra = exemptElytra;
   }

   @Generated
   public boolean isForceStuckSpeed() {
      return this.forceStuckSpeed;
   }

   @Generated
   public void setForceStuckSpeed(boolean forceStuckSpeed) {
      this.forceStuckSpeed = forceStuckSpeed;
   }

   @Generated
   public boolean isForceSlowMovement() {
      return this.forceSlowMovement;
   }

   @Generated
   public void setForceSlowMovement(boolean forceSlowMovement) {
      this.forceSlowMovement = forceSlowMovement;
   }

   @Generated
   public boolean isResetItemUsageOnAttack() {
      return this.resetItemUsageOnAttack;
   }

   @Generated
   public boolean isResetItemUsageOnItemUpdate() {
      return this.resetItemUsageOnItemUpdate;
   }

   @Generated
   public boolean isResetItemUsageOnSlotChange() {
      return this.resetItemUsageOnSlotChange;
   }

   @Generated
   public boolean isResetItemUsageOnItemUse() {
      return this.resetItemUsageOnItemUse;
   }

   public record Movement(Vector3d from, Vector3d to, Vector3d axisDependentOriginalMovement) {
      public Movement(Vector3d from, Vector3d to) {
         this(from, to, null);
      }

      public boolean axisIndependant() {
         return this.axisDependentOriginalMovement != null;
      }
   }
}
