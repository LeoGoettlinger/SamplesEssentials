package ac.grim.grimac.manager;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.debug.HitboxDebugHandler;
import ac.grim.grimac.checks.impl.aim.AimDuplicateLook;
import ac.grim.grimac.checks.impl.aim.AimModulo360;
import ac.grim.grimac.checks.impl.aim.processor.AimProcessor;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsA;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsB;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsC;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsD;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsE;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsF;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsG;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsH;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsI;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsJ;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsK;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsL;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsM;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsN;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsO;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsP;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsQ;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsR;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsS;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsT;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsU;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsV;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsW;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsX;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsY;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsZ;
import ac.grim.grimac.checks.impl.breaking.AirLiquidBreak;
import ac.grim.grimac.checks.impl.breaking.FarBreak;
import ac.grim.grimac.checks.impl.breaking.FastBreak;
import ac.grim.grimac.checks.impl.breaking.InvalidBreak;
import ac.grim.grimac.checks.impl.breaking.MultiBreak;
import ac.grim.grimac.checks.impl.breaking.NoSwingBreak;
import ac.grim.grimac.checks.impl.breaking.PositionBreakA;
import ac.grim.grimac.checks.impl.breaking.PositionBreakB;
import ac.grim.grimac.checks.impl.breaking.RotationBreak;
import ac.grim.grimac.checks.impl.breaking.WrongBreak;
import ac.grim.grimac.checks.impl.chat.ChatA;
import ac.grim.grimac.checks.impl.chat.ChatB;
import ac.grim.grimac.checks.impl.chat.ChatC;
import ac.grim.grimac.checks.impl.chat.ChatD;
import ac.grim.grimac.checks.impl.combat.EntityPierce;
import ac.grim.grimac.checks.impl.combat.Hitboxes;
import ac.grim.grimac.checks.impl.combat.MultiInteractA;
import ac.grim.grimac.checks.impl.combat.MultiInteractB;
import ac.grim.grimac.checks.impl.combat.Reach;
import ac.grim.grimac.checks.impl.combat.SelfInteract;
import ac.grim.grimac.checks.impl.combat.WallHit;
import ac.grim.grimac.checks.impl.crash.CrashA;
import ac.grim.grimac.checks.impl.crash.CrashB;
import ac.grim.grimac.checks.impl.crash.CrashC;
import ac.grim.grimac.checks.impl.crash.CrashD;
import ac.grim.grimac.checks.impl.crash.CrashE;
import ac.grim.grimac.checks.impl.crash.CrashF;
import ac.grim.grimac.checks.impl.crash.CrashG;
import ac.grim.grimac.checks.impl.crash.CrashH;
import ac.grim.grimac.checks.impl.crash.CrashI;
import ac.grim.grimac.checks.impl.elytra.ElytraA;
import ac.grim.grimac.checks.impl.elytra.ElytraB;
import ac.grim.grimac.checks.impl.elytra.ElytraC;
import ac.grim.grimac.checks.impl.elytra.ElytraD;
import ac.grim.grimac.checks.impl.elytra.ElytraE;
import ac.grim.grimac.checks.impl.elytra.ElytraF;
import ac.grim.grimac.checks.impl.elytra.ElytraG;
import ac.grim.grimac.checks.impl.elytra.ElytraH;
import ac.grim.grimac.checks.impl.elytra.ElytraI;
import ac.grim.grimac.checks.impl.exploit.ExploitA;
import ac.grim.grimac.checks.impl.exploit.ExploitB;
import ac.grim.grimac.checks.impl.groundspoof.NoFall;
import ac.grim.grimac.checks.impl.inventory.InventoryA;
import ac.grim.grimac.checks.impl.inventory.InventoryB;
import ac.grim.grimac.checks.impl.inventory.InventoryC;
import ac.grim.grimac.checks.impl.inventory.InventoryD;
import ac.grim.grimac.checks.impl.inventory.InventoryE;
import ac.grim.grimac.checks.impl.inventory.InventoryF;
import ac.grim.grimac.checks.impl.inventory.InventoryG;
import ac.grim.grimac.checks.impl.misc.ClientBrand;
import ac.grim.grimac.checks.impl.misc.GhostBlockMitigation;
import ac.grim.grimac.checks.impl.misc.Post;
import ac.grim.grimac.checks.impl.misc.TransactionOrder;
import ac.grim.grimac.checks.impl.movement.NoSlow;
import ac.grim.grimac.checks.impl.movement.PredictionRunner;
import ac.grim.grimac.checks.impl.movement.SetbackBlocker;
import ac.grim.grimac.checks.impl.movement.VehiclePredictionRunner;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsA;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsB;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsC;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsD;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsE;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsF;
import ac.grim.grimac.checks.impl.multiactions.MultiActionsG;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderA;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderB;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderC;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderD;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderE;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderF;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderG;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderH;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderI;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderJ;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderK;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderL;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderM;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderN;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderO;
import ac.grim.grimac.checks.impl.packetorder.PacketOrderProcessor;
import ac.grim.grimac.checks.impl.prediction.DebugHandler;
import ac.grim.grimac.checks.impl.prediction.GroundSpoof;
import ac.grim.grimac.checks.impl.prediction.OffsetHandler;
import ac.grim.grimac.checks.impl.prediction.Phase;
import ac.grim.grimac.checks.impl.scaffolding.AirLiquidPlace;
import ac.grim.grimac.checks.impl.scaffolding.DuplicateRotPlace;
import ac.grim.grimac.checks.impl.scaffolding.FabricatedPlace;
import ac.grim.grimac.checks.impl.scaffolding.FarPlace;
import ac.grim.grimac.checks.impl.scaffolding.InvalidPlaceA;
import ac.grim.grimac.checks.impl.scaffolding.InvalidPlaceB;
import ac.grim.grimac.checks.impl.scaffolding.MultiPlace;
import ac.grim.grimac.checks.impl.scaffolding.PositionPlace;
import ac.grim.grimac.checks.impl.scaffolding.RotationPlace;
import ac.grim.grimac.checks.impl.sprint.SprintA;
import ac.grim.grimac.checks.impl.sprint.SprintB;
import ac.grim.grimac.checks.impl.sprint.SprintC;
import ac.grim.grimac.checks.impl.sprint.SprintD;
import ac.grim.grimac.checks.impl.sprint.SprintE;
import ac.grim.grimac.checks.impl.sprint.SprintF;
import ac.grim.grimac.checks.impl.sprint.SprintG;
import ac.grim.grimac.checks.impl.timer.NegativeTimer;
import ac.grim.grimac.checks.impl.timer.TickTimer;
import ac.grim.grimac.checks.impl.timer.Timer;
import ac.grim.grimac.checks.impl.timer.TimerLimit;
import ac.grim.grimac.checks.impl.timer.VehicleTimer;
import ac.grim.grimac.checks.impl.vehicle.VehicleA;
import ac.grim.grimac.checks.impl.vehicle.VehicleB;
import ac.grim.grimac.checks.impl.vehicle.VehicleC;
import ac.grim.grimac.checks.impl.vehicle.VehicleD;
import ac.grim.grimac.checks.impl.vehicle.VehicleE;
import ac.grim.grimac.checks.impl.vehicle.VehicleF;
import ac.grim.grimac.checks.impl.velocity.ExplosionHandler;
import ac.grim.grimac.checks.impl.velocity.KnockbackHandler;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.checks.type.PositionCheck;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.checks.type.VehicleCheck;
import ac.grim.grimac.events.packets.PacketChangeGameState;
import ac.grim.grimac.events.packets.PacketEntityReplication;
import ac.grim.grimac.events.packets.PacketPlayerAbilities;
import ac.grim.grimac.events.packets.PacketWorldBorder;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.grim.grimac.manager.init.start.SuperDebug;
import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.GhostBlockDetector;
import ac.grim.grimac.predictionengine.SneakingEstimator;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.anticheat.update.PositionUpdate;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;
import ac.grim.grimac.utils.anticheat.update.VehiclePositionUpdate;
import ac.grim.grimac.utils.latency.CompensatedCameraEntity;
import ac.grim.grimac.utils.latency.CompensatedCooldown;
import ac.grim.grimac.utils.latency.CompensatedFireworks;
import ac.grim.grimac.utils.latency.CompensatedInventory;
import ac.grim.grimac.utils.team.TeamHandler;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap.Builder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckManager {
   private static final AtomicBoolean initedAtomic = new AtomicBoolean(false);
   private static boolean inited;
   public final ClassToInstanceMap<AbstractCheck> allChecks;
   private final ClassToInstanceMap<PacketCheck> preViaPacketChecks;
   private final ClassToInstanceMap<PacketCheck> packetChecks;
   private final ClassToInstanceMap<PositionCheck> positionChecks;
   private final ClassToInstanceMap<RotationCheck> rotationChecks;
   private final ClassToInstanceMap<VehicleCheck> vehicleChecks;
   private final ClassToInstanceMap<PacketCheck> prePredictionChecks;
   private final ClassToInstanceMap<BlockBreakCheck> blockBreakChecks;
   private final ClassToInstanceMap<BlockPlaceCheck> blockPlaceChecks;
   private final ClassToInstanceMap<PostPredictionCheck> postPredictionChecks;
   private final List<PacketCheck> preViaPacketChecksValues;
   private final List<PacketCheck> packetChecksValues;
   private final List<PositionCheck> positionChecksValues;
   private final List<RotationCheck> rotationChecksValues;
   private final List<VehicleCheck> vehicleChecksValues;
   private final List<PacketCheck> prePredictionChecksValues;
   private final List<BlockBreakCheck> blockBreakChecksValues;
   private final List<BlockPlaceCheck> blockPlaceChecksValues;
   private final List<PostPredictionCheck> postPredictionChecksValues;

   public CheckManager(GrimPlayer player) {
      this.preViaPacketChecks = new Builder()
         .put(CompensatedCameraEntity.class, player.cameraEntity)
         .put(ChatA.class, new ChatA(player))
         .put(ChatB.class, new ChatB(player))
         .put(ChatC.class, new ChatC(player))
         .put(ChatD.class, new ChatD(player))
         .put(BadPacketsA.class, new BadPacketsA(player))
         .put(BadPacketsB.class, new BadPacketsB(player))
         .put(BadPacketsC.class, new BadPacketsC(player))
         .put(BadPacketsF.class, new BadPacketsF(player))
         .put(BadPacketsG.class, new BadPacketsG(player))
         .put(BadPacketsI.class, new BadPacketsI(player))
         .put(BadPacketsK.class, new BadPacketsK(player))
         .put(BadPacketsM.class, new BadPacketsM(player))
         .put(BadPacketsY.class, new BadPacketsY(player))
         .put(BadPacketsZ.class, new BadPacketsZ(player))
         .put(PacketOrderB.class, new PacketOrderB(player))
         .put(PacketOrderC.class, new PacketOrderC(player))
         .put(PacketOrderD.class, new PacketOrderD(player))
         .put(SelfInteract.class, new SelfInteract(player))
         .put(MultiActionsA.class, new MultiActionsA(player))
         .put(MultiActionsE.class, new MultiActionsE(player))
         .put(VehicleA.class, new VehicleA(player))
         .put(VehicleB.class, new VehicleB(player))
         .build();
      this.packetChecks = new Builder()
         .put(PacketOrderProcessor.class, player.packetOrderProcessor)
         .put(Reach.class, new Reach(player))
         .put(PacketEntityReplication.class, player.packetEntityReplication)
         .put(PacketChangeGameState.class, new PacketChangeGameState(player))
         .put(CompensatedInventory.class, player.inventory)
         .put(PacketPlayerAbilities.class, new PacketPlayerAbilities(player))
         .put(PacketWorldBorder.class, new PacketWorldBorder(player))
         .put(AttackCooldownHandler.class, player.attackCooldown)
         .put(TeamHandler.class, new TeamHandler(player))
         .put(ClientBrand.class, new ClientBrand(player))
         .put(NoFall.class, new NoFall(player))
         .put(ExploitA.class, new ExploitA(player))
         .put(ExploitB.class, new ExploitB(player))
         .put(BadPacketsD.class, new BadPacketsD(player))
         .put(BadPacketsE.class, new BadPacketsE(player))
         .put(BadPacketsJ.class, new BadPacketsJ(player))
         .put(BadPacketsL.class, new BadPacketsL(player))
         .put(BadPacketsO.class, new BadPacketsO(player))
         .put(BadPacketsP.class, new BadPacketsP(player))
         .put(BadPacketsQ.class, new BadPacketsQ(player))
         .put(BadPacketsR.class, new BadPacketsR(player))
         .put(BadPacketsS.class, new BadPacketsS(player))
         .put(BadPacketsT.class, new BadPacketsT(player))
         .put(BadPacketsU.class, new BadPacketsU(player))
         .put(BadPacketsV.class, new BadPacketsV(player))
         .put(InventoryA.class, new InventoryA(player))
         .put(InventoryB.class, new InventoryB(player))
         .put(InventoryE.class, new InventoryE(player))
         .put(InventoryF.class, new InventoryF(player))
         .put(InventoryG.class, new InventoryG(player))
         .put(MultiActionsC.class, new MultiActionsC(player))
         .put(MultiActionsD.class, new MultiActionsD(player))
         .put(PacketOrderO.class, new PacketOrderO(player))
         .put(VehicleD.class, new VehicleD(player))
         .put(VehicleE.class, new VehicleE(player))
         .put(VehicleF.class, new VehicleF(player))
         .put(CrashB.class, new CrashB(player))
         .put(CrashD.class, new CrashD(player))
         .put(CrashE.class, new CrashE(player))
         .put(CrashF.class, new CrashF(player))
         .put(CrashH.class, new CrashH(player))
         .put(CrashI.class, new CrashI(player))
         .put(SetbackBlocker.class, new SetbackBlocker(player))
         .build();
      this.positionChecks = new Builder()
         .put(PredictionRunner.class, new PredictionRunner(player))
         .put(CompensatedCooldown.class, new CompensatedCooldown(player))
         .build();
      this.rotationChecks = new Builder()
         .put(AimProcessor.class, new AimProcessor(player))
         .put(AimModulo360.class, new AimModulo360(player))
         .put(AimDuplicateLook.class, new AimDuplicateLook(player))
         .build();
      this.vehicleChecks = new Builder().put(VehiclePredictionRunner.class, new VehiclePredictionRunner(player)).build();
      this.postPredictionChecks = new Builder()
         .put(NegativeTimer.class, new NegativeTimer(player))
         .put(ExplosionHandler.class, new ExplosionHandler(player))
         .put(KnockbackHandler.class, new KnockbackHandler(player))
         .put(GhostBlockDetector.class, new GhostBlockDetector(player))
         .put(InventoryD.class, new InventoryD(player))
         .put(Phase.class, new Phase(player))
         .put(Post.class, new Post(player))
         .put(PacketOrderA.class, new PacketOrderA(player))
         .put(PacketOrderE.class, new PacketOrderE(player))
         .put(PacketOrderF.class, new PacketOrderF(player))
         .put(PacketOrderG.class, new PacketOrderG(player))
         .put(PacketOrderH.class, new PacketOrderH(player))
         .put(PacketOrderI.class, new PacketOrderI(player))
         .put(PacketOrderJ.class, new PacketOrderJ(player))
         .put(PacketOrderK.class, new PacketOrderK(player))
         .put(PacketOrderL.class, new PacketOrderL(player))
         .put(PacketOrderM.class, new PacketOrderM(player))
         .put(GroundSpoof.class, new GroundSpoof(player))
         .put(OffsetHandler.class, new OffsetHandler(player))
         .put(SuperDebug.class, new SuperDebug(player))
         .put(DebugHandler.class, new DebugHandler(player))
         .put(BadPacketsX.class, new BadPacketsX(player))
         .put(NoSlow.class, new NoSlow(player))
         .put(SprintA.class, new SprintA(player))
         .put(SprintB.class, new SprintB(player))
         .put(SprintC.class, new SprintC(player))
         .put(SprintD.class, new SprintD(player))
         .put(SprintE.class, new SprintE(player))
         .put(SprintF.class, new SprintF(player))
         .put(SprintG.class, new SprintG(player))
         .put(MultiInteractA.class, new MultiInteractA(player))
         .put(MultiInteractB.class, new MultiInteractB(player))
         .put(ElytraA.class, new ElytraA(player))
         .put(ElytraB.class, new ElytraB(player))
         .put(ElytraC.class, new ElytraC(player))
         .put(ElytraD.class, new ElytraD(player))
         .put(ElytraE.class, new ElytraE(player))
         .put(ElytraF.class, new ElytraF(player))
         .put(ElytraG.class, new ElytraG(player))
         .put(ElytraH.class, new ElytraH(player))
         .put(ElytraI.class, new ElytraI(player))
         .put(SetbackTeleportUtil.class, new SetbackTeleportUtil(player))
         .put(CompensatedFireworks.class, player.fireworks)
         .put(SneakingEstimator.class, new SneakingEstimator(player))
         .put(LastInstanceManager.class, player.lastInstanceManager)
         .build();
      this.blockPlaceChecks = new Builder()
         .put(InventoryC.class, new InventoryC(player))
         .put(InvalidPlaceA.class, new InvalidPlaceA(player))
         .put(InvalidPlaceB.class, new InvalidPlaceB(player))
         .put(AirLiquidPlace.class, new AirLiquidPlace(player))
         .put(MultiPlace.class, new MultiPlace(player))
         .put(MultiActionsF.class, new MultiActionsF(player))
         .put(MultiActionsG.class, new MultiActionsG(player))
         .put(BadPacketsH.class, new BadPacketsH(player))
         .put(CrashG.class, new CrashG(player))
         .put(FarPlace.class, new FarPlace(player))
         .put(FabricatedPlace.class, new FabricatedPlace(player))
         .put(PositionPlace.class, new PositionPlace(player))
         .put(RotationPlace.class, new RotationPlace(player))
         .put(PacketOrderN.class, new PacketOrderN(player))
         .put(DuplicateRotPlace.class, new DuplicateRotPlace(player))
         .put(GhostBlockMitigation.class, new GhostBlockMitigation(player))
         .build();
      this.prePredictionChecks = new Builder()
         .put(Timer.class, new Timer(player))
         .put(TickTimer.class, new TickTimer(player))
         .put(TimerLimit.class, new TimerLimit(player))
         .put(CrashA.class, new CrashA(player))
         .put(CrashC.class, new CrashC(player))
         .put(VehicleTimer.class, new VehicleTimer(player))
         .build();
      this.blockBreakChecks = new Builder()
         .put(AirLiquidBreak.class, new AirLiquidBreak(player))
         .put(WrongBreak.class, new WrongBreak(player))
         .put(RotationBreak.class, new RotationBreak(player))
         .put(FastBreak.class, new FastBreak(player))
         .put(MultiBreak.class, new MultiBreak(player))
         .put(NoSwingBreak.class, new NoSwingBreak(player))
         .put(FarBreak.class, new FarBreak(player))
         .put(InvalidBreak.class, new InvalidBreak(player))
         .put(PositionBreakA.class, new PositionBreakA(player))
         .put(PositionBreakB.class, new PositionBreakB(player))
         .put(MultiActionsB.class, new MultiActionsB(player))
         .build();
      ClassToInstanceMap<AbstractCheck> noneModules = new Builder()
         .put(BadPacketsN.class, new BadPacketsN(player))
         .put(BadPacketsW.class, new BadPacketsW(player))
         .put(TransactionOrder.class, new TransactionOrder(player))
         .put(VehicleC.class, new VehicleC(player))
         .put(Hitboxes.class, new Hitboxes(player))
         .put(WallHit.class, new WallHit(player))
         .put(EntityPierce.class, new EntityPierce(player))
         .put(HitboxDebugHandler.class, new HitboxDebugHandler(player))
         .build();
      this.allChecks = new Builder()
         .putAll(this.preViaPacketChecks)
         .putAll(this.packetChecks)
         .putAll(this.positionChecks)
         .putAll(this.rotationChecks)
         .putAll(this.vehicleChecks)
         .putAll(this.postPredictionChecks)
         .putAll(this.blockPlaceChecks)
         .putAll(this.prePredictionChecks)
         .putAll(this.blockBreakChecks)
         .putAll(noneModules)
         .build();
      this.preViaPacketChecksValues = new ArrayList<>(this.preViaPacketChecks.values());
      this.packetChecksValues = new ArrayList<>(this.packetChecks.values());
      this.positionChecksValues = new ArrayList<>(this.positionChecks.values());
      this.rotationChecksValues = new ArrayList<>(this.rotationChecks.values());
      this.vehicleChecksValues = new ArrayList<>(this.vehicleChecks.values());
      this.prePredictionChecksValues = new ArrayList<>(this.prePredictionChecks.values());
      this.blockBreakChecksValues = new ArrayList<>(this.blockBreakChecks.values());
      this.blockPlaceChecksValues = new ArrayList<>(this.blockPlaceChecks.values());
      this.postPredictionChecksValues = new ArrayList<>(this.postPredictionChecks.values());
      this.registerBuiltInVerboseTemplates();
      this.init();
   }

   private void registerBuiltInVerboseTemplates() {
      VerboseRegistry registry = GrimAPI.INSTANCE.getDataStoreLifecycle().verboseRegistry();
      if (registry != null) {
         registry.registerTemplates(() -> {
            for (AbstractCheck check : this.allChecks.values()) {
               if (check instanceof Check grimCheck) {
                  grimCheck.registerVerboseTemplates(registry);
               }
            }
         });
      }
   }

   public <T extends AbstractCheck> T getCheck(Class<T> check) {
      return (T)this.allChecks.getInstance(check);
   }

   public <T extends PositionCheck> T getPositionCheck(Class<T> check) {
      return (T)this.positionChecks.getInstance(check);
   }

   public <T extends RotationCheck> T getRotationCheck(Class<T> check) {
      return (T)this.rotationChecks.getInstance(check);
   }

   public <T extends BlockPlaceCheck> T getBlockPlaceCheck(Class<T> check) {
      return (T)this.blockPlaceChecks.getInstance(check);
   }

   public <T extends PacketCheck> T getPacketCheck(Class<T> check) {
      return (T)this.packetChecks.getInstance(check);
   }

   public <T extends PacketCheck> T getPreViaPacketCheck(Class<T> check) {
      return (T)this.preViaPacketChecks.getInstance(check);
   }

   public <T extends PacketCheck> T getPrePredictionCheck(Class<T> check) {
      return (T)this.prePredictionChecks.getInstance(check);
   }

   public <T extends PostPredictionCheck> T getPostPredictionCheck(Class<T> check) {
      return (T)this.postPredictionChecks.getInstance(check);
   }

   public void onPrePredictionReceivePacket(PacketReceiveEvent packet) {
      for (PacketCheck check : this.prePredictionChecksValues) {
         check.onPacketReceive(packet);
      }
   }

   public void onPacketReceive(PacketReceiveEvent packet) {
      for (PacketCheck check : this.packetChecksValues) {
         check.onPacketReceive(packet);
      }

      for (PostPredictionCheck check : this.postPredictionChecksValues) {
         check.onPacketReceive(packet);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onPacketReceive(packet);
      }
   }

   public void onPreViaPacketReceive(PacketReceiveEvent packet) {
      for (PacketCheck check : this.preViaPacketChecksValues) {
         check.onPacketReceive(packet);
      }

      for (BlockBreakCheck check : this.blockBreakChecksValues) {
         check.onPacketReceive(packet);
      }
   }

   public void onPacketSend(PacketSendEvent packet) {
      for (PacketCheck check : this.prePredictionChecksValues) {
         check.onPacketSend(packet);
      }

      for (PacketCheck check : this.packetChecksValues) {
         check.onPacketSend(packet);
      }

      for (PostPredictionCheck check : this.postPredictionChecksValues) {
         check.onPacketSend(packet);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onPacketSend(packet);
      }
   }

   public void onPreViaPacketSend(PacketSendEvent packet) {
      for (PacketCheck check : this.preViaPacketChecksValues) {
         check.onPacketSend(packet);
      }

      for (BlockBreakCheck check : this.blockBreakChecksValues) {
         check.onPacketSend(packet);
      }
   }

   public void onPositionUpdate(PositionUpdate position) {
      for (PositionCheck check : this.positionChecksValues) {
         check.onPositionUpdate(position);
      }
   }

   public void onRotationUpdate(RotationUpdate rotation) {
      for (RotationCheck check : this.rotationChecksValues) {
         check.process(rotation);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.process(rotation);
      }
   }

   public void onVehiclePositionUpdate(VehiclePositionUpdate update) {
      for (VehicleCheck check : this.vehicleChecksValues) {
         check.process(update);
      }
   }

   public void onPredictionFinish(PredictionComplete complete) {
      for (PostPredictionCheck check : this.postPredictionChecksValues) {
         check.onPredictionComplete(complete);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onPredictionComplete(complete);
      }

      for (BlockBreakCheck check : this.blockBreakChecksValues) {
         check.onPredictionComplete(complete);
      }
   }

   public void onBlockPlace(BlockPlace place) {
      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onBlockPlace(place);
      }
   }

   public void onPostFlyingBlockPlace(BlockPlace place) {
      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onPostFlyingBlockPlace(place);
      }
   }

   public void onBlockBreak(BlockBreak blockBreak) {
      for (BlockBreakCheck check : this.blockBreakChecksValues) {
         check.onBlockBreak(blockBreak);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onBlockBreak(blockBreak);
      }
   }

   public void onPostFlyingBlockBreak(BlockBreak blockBreak) {
      for (BlockBreakCheck check : this.blockBreakChecksValues) {
         check.onPostFlyingBlockBreak(blockBreak);
      }

      for (BlockPlaceCheck check : this.blockPlaceChecksValues) {
         check.onPostFlyingBlockBreak(blockBreak);
      }
   }

   public ExplosionHandler getExplosionHandler() {
      return this.getPostPredictionCheck(ExplosionHandler.class);
   }

   public NoFall getNoFall() {
      return this.getPacketCheck(NoFall.class);
   }

   public KnockbackHandler getKnockbackHandler() {
      return this.getPostPredictionCheck(KnockbackHandler.class);
   }

   public CompensatedCooldown getCompensatedCooldown() {
      return this.getPositionCheck(CompensatedCooldown.class);
   }

   public NoSlow getNoSlow() {
      return this.getPostPredictionCheck(NoSlow.class);
   }

   public SetbackTeleportUtil getSetbackUtil() {
      return this.getPostPredictionCheck(SetbackTeleportUtil.class);
   }

   public DebugHandler getDebugHandler() {
      return this.getPostPredictionCheck(DebugHandler.class);
   }

   private void init() {
      if (!inited && !initedAtomic.getAndSet(true)) {
         inited = true;
         String[] permissions = new String[]{"grim.exempt.", "grim.nosetback.", "grim.nomodifypacket."};

         for (AbstractCheck check : this.allChecks.values()) {
            if (check.getConfigName() != null) {
               String id = check.getConfigName().toLowerCase();

               for (String permissionName : permissions) {
                  GrimAPI.INSTANCE.getPermissionManager().registerPermission(permissionName + id, PermissionDefaultValue.FALSE);
               }
            }
         }
      }
   }
}
