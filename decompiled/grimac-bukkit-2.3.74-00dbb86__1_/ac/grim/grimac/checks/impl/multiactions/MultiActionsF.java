package ac.grim.grimac.checks.impl.multiactions;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayList;
import java.util.List;

@CheckData(
   name = "MultiActionsF",
   stableKey = "grim.multiactions.block_and_entity_interact",
   description = "Interacting with a block and an entity in the same tick",
   experimental = true
)
public class MultiActionsF extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("action=place").or("action=entity").or("action=dig");
   private static final int ACTION_PLACE = 0;
   private static final int ACTION_ENTITY = 1;
   private static final int ACTION_DIG = 2;
   private final List<MultiActionsF.FlagData> flags = new ArrayList<>();
   private boolean entity;
   private boolean block;

   public MultiActionsF(GrimPlayer player) {
      super(player);
   }

   private Verbose.Writer writeAction(int action) {
      return V.write(this.verbose(), action);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      this.block = true;
      if (this.entity) {
         if (!this.player.canSkipTicks()) {
            if (this.flag(this.writeAction(0)) && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         } else {
            this.flags.add(new MultiActionsF.FlagData(0));
         }
      }
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
         || event.getPacketType() == PacketType.Play.Client.ATTACK
         || event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
         this.entity = true;
         if (this.block) {
            if (!this.player.canSkipTicks()) {
               if (this.flag(this.writeAction(1)) && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.flags.add(new MultiActionsF.FlagData(1));
            }
         }
      }

      if (this.isTickPacket(event.getPacketType())) {
         this.block = this.entity = false;
      }
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (blockBreak.action == DiggingAction.START_DIGGING || blockBreak.action == DiggingAction.FINISHED_DIGGING) {
         this.block = true;
         if (this.entity) {
            if (!this.player.canSkipTicks()) {
               if (this.flag(this.writeAction(2)) && this.shouldModifyPackets()) {
                  blockBreak.cancel();
               }
            } else {
               this.flags.add(new MultiActionsF.FlagData(2));
            }
         }
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (MultiActionsF.FlagData data : this.flags) {
               this.flag(this.writeAction(data.action()));
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int action) {
   }
}
