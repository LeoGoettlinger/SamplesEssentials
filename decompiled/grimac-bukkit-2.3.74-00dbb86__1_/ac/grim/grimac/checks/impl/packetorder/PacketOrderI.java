package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.nmsutil.BlockBreakSpeed;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderI",
   stableKey = "grim.packetorder.input_tick_order",
   description = "Sent combat, use, release, or digging packets in an invalid tick order",
   experimental = true
)
public class PacketOrderI extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("type={str}[, attacking={bool}][, rightClicking={bool}][, picking={bool}][, releasing={bool}], digging={bool}");
   static final int TYPE_INTERACT = 0;
   static final int TYPE_PLACE_USE = 1;
   static final int TYPE_RELEASE = 2;
   static final int TYPE_ATTACK = 3;
   private boolean exemptPlacingWhileDigging;
   private boolean setback;
   private boolean digging;
   private final ArrayDeque<PacketOrderI.FlagData> flags = new ArrayDeque<>();

   public PacketOrderI(GrimPlayer player) {
      super(player);
   }

   static String typeName(int type) {
      return switch (type) {
         case 0 -> "interact";
         case 1 -> "place/use";
         case 2 -> "release";
         case 3 -> "attack";
         default -> "unknown";
      };
   }

   private Verbose.Writer write(int type, boolean attacking, boolean rightClicking, boolean picking, boolean releasing, boolean digging) {
      boolean release = type == 2;
      boolean attack = type == 3;
      return V.write(this.verbose())
         .str(typeName(type))
         .bool(release)
         .bool(attacking)
         .bool(release || attack)
         .bool(rightClicking)
         .bool(release || attack)
         .bool(picking)
         .bool(!release)
         .bool(releasing)
         .bool(digging);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         if (new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            this.onAttack(event);
         } else if (this.player.packetOrderProcessor.isReleasing() || this.player.packetOrderProcessor.isDigging()) {
            boolean releasing = this.player.packetOrderProcessor.isReleasing();
            boolean digging = this.player.packetOrderProcessor.isDigging();
            if (!this.player.canSkipTicks()) {
               if (this.flag(this.write(0, false, false, false, releasing, digging)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.flags.add(new PacketOrderI.FlagData(0, false, false, false, releasing, digging));
            }
         }
      }

      if ((event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM)
         && (this.player.packetOrderProcessor.isReleasing() || this.digging)) {
         boolean releasing = this.player.packetOrderProcessor.isReleasing();
         if (!this.player.canSkipTicks()) {
            if (this.flag(this.write(1, false, false, false, releasing, this.digging)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(new PacketOrderI.FlagData(1, false, false, false, releasing, this.digging));
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.ATTACK || event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
         this.onAttack(event);
      }

      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
         WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
         switch (packet.getAction()) {
            case STAB:
               this.onAttack(event);
               break;
            case RELEASE_USE_ITEM:
               if (this.player.packetOrderProcessor.isAttackingOrStabbing()
                  || this.player.packetOrderProcessor.isRightClicking()
                  || this.player.packetOrderProcessor.isPicking()
                  || this.player.packetOrderProcessor.isDigging()) {
                  boolean attacking = this.player.packetOrderProcessor.isAttackingOrStabbing();
                  boolean rightClicking = this.player.packetOrderProcessor.isRightClicking();
                  boolean picking = this.player.packetOrderProcessor.isPicking();
                  boolean digging = this.player.packetOrderProcessor.isDigging();
                  if (!this.player.canSkipTicks()) {
                     if (this.flag(this.write(2, attacking, rightClicking, picking, false, digging))) {
                        this.setback = true;
                     }
                  } else {
                     this.flags.add(new PacketOrderI.FlagData(2, attacking, rightClicking, picking, false, digging));
                     this.setback = true;
                  }
               }
               break;
            case START_DIGGING:
               double damage = BlockBreakSpeed.getBlockDamage(this.player, this.player.compensatedWorld.getBlock(packet.getBlockPosition()));
               if (damage >= 1.0 || damage <= 0.0 && this.player.gamemode == GameMode.CREATIVE) {
                  return;
               }
            case CANCELLED_DIGGING:
            case FINISHED_DIGGING:
               if (this.exemptPlacingWhileDigging || this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_7_10)) {
                  return;
               }

               this.digging = true;
         }
      }

      if (!this.player.cameraEntity.isSelf() || this.isTickPacket(event.getPacketType())) {
         this.digging = false;
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (!this.player.canSkipTicks()) {
         if (this.setback) {
            this.setbackIfAboveSetbackVL();
            this.setback = false;
         }
      } else {
         if (this.player.isTickingReliablyFor(3)) {
            for (PacketOrderI.FlagData data : this.flags) {
               if (this.flag(this.write(data.type(), data.attacking(), data.rightClicking(), data.picking(), data.releasing(), data.digging())) && this.setback
                  )
                {
                  this.setbackIfAboveSetbackVL();
                  this.setback = false;
               }
            }
         }

         this.flags.clear();
         this.setback = false;
      }
   }

   private void onAttack(PacketReceiveEvent event) {
      if (this.player.packetOrderProcessor.isRightClicking()
         || this.player.packetOrderProcessor.isPicking()
         || this.player.packetOrderProcessor.isReleasing()
         || this.player.packetOrderProcessor.isDigging()) {
         boolean rightClicking = this.player.packetOrderProcessor.isRightClicking();
         boolean picking = this.player.packetOrderProcessor.isPicking();
         boolean releasing = this.player.packetOrderProcessor.isReleasing();
         boolean digging = this.player.packetOrderProcessor.isDigging();
         if (!this.player.canSkipTicks()) {
            if (this.flag(this.write(3, false, rightClicking, picking, releasing, digging)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(new PacketOrderI.FlagData(3, false, rightClicking, picking, releasing, digging));
         }
      }
   }

   @Override
   public void onReload(ConfigManager config) {
      this.exemptPlacingWhileDigging = config.getBooleanElse(this.getConfigName() + ".exempt-placing-while-digging", false);
   }

   private record FlagData(int type, boolean attacking, boolean rightClicking, boolean picking, boolean releasing, boolean digging) {
   }
}
