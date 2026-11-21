package ac.grim.grimac.checks.impl.packetorder;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayDeque;

@CheckData(
   name = "PacketOrderE",
   stableKey = "grim.packetorder.slot_order",
   description = "Changed held item slot during another conflicting action",
   experimental = true
)
public class PacketOrderE extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of(
      "attacking={bool}, rightClicking={bool}, openingInventory={bool}, releasing={bool}, sneaking={bool}, sprinting={bool}, bed={bool}, gliding={bool}, mountJumping={bool}"
   );
   private static final int ATTACKING = 1;
   private static final int RIGHT_CLICKING = 2;
   private static final int OPENING_INVENTORY = 4;
   private static final int RELEASING = 8;
   private static final int SNEAKING = 16;
   private static final int SPRINTING = 32;
   private static final int LEAVING_BED = 64;
   private static final int GLIDING = 128;
   private static final int MOUNT_JUMPING = 256;
   private final ArrayDeque<Integer> flags = new ArrayDeque<>();
   private boolean setback;

   public PacketOrderE(GrimPlayer player) {
      super(player);
   }

   private Verbose.Writer write(int flags) {
      return V.write(this.verbose())
         .bool(has(flags, 1))
         .bool(has(flags, 2))
         .bool(has(flags, 4))
         .bool(has(flags, 8))
         .bool(has(flags, 16))
         .bool(has(flags, 32))
         .bool(has(flags, 64))
         .bool(has(flags, 128))
         .bool(has(flags, 256));
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
         int currentFlags = this.currentFlags();
         if (currentFlags != 0
            && (this.player.canSkipTicks() && this.flags.add(currentFlags) || this.flag(this.write(currentFlags)))
            && this.player.packetOrderProcessor.isUsing()) {
            this.setback = true;
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (!this.player.canSkipTicks()) {
         if (this.setback) {
            this.setback = false;
            this.setbackIfAboveSetbackVL();
         }
      } else {
         if (this.player.isTickingReliablyFor(3)) {
            for (int currentFlags : this.flags) {
               if (this.flag(this.write(currentFlags)) && this.setback) {
                  this.setback = false;
                  this.setbackIfAboveSetbackVL();
               }
            }
         }

         this.setback = false;
         this.flags.clear();
      }
   }

   private int currentFlags() {
      int flags = 0;
      if (this.player.packetOrderProcessor.isAttackingOrStabbing()) {
         flags |= 1;
      }

      if (this.player.packetOrderProcessor.isRightClicking()) {
         flags |= 2;
      }

      if (this.player.packetOrderProcessor.isOpeningInventory()) {
         flags |= 4;
      }

      if (this.player.packetOrderProcessor.isReleasing()) {
         flags |= 8;
      }

      if (this.player.packetOrderProcessor.isSneaking()) {
         flags |= 16;
      }

      if (this.player.packetOrderProcessor.isSprinting()) {
         flags |= 32;
      }

      if (this.player.packetOrderProcessor.isLeavingBed()) {
         flags |= 64;
      }

      if (this.player.packetOrderProcessor.isStartingToGlide()) {
         flags |= 128;
      }

      if (this.player.packetOrderProcessor.isJumpingWithMount()) {
         flags |= 256;
      }

      return flags;
   }

   private static boolean has(int flags, int flag) {
      return (flags & flag) != 0;
   }
}
