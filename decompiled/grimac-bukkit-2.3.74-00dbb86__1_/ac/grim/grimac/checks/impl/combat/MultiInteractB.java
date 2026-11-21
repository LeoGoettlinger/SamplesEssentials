package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayList;

@CheckData(
   name = "MultiInteractB",
   stableKey = "grim.multiinteract.interact_at_position_changed",
   description = "Sent multiple entity interaction packets with different hit positions in one tick",
   experimental = true
)
public class MultiInteractB extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("pos={f64}, {f64}, {f64}, lastPos={f64}, {f64}, {f64}");
   private final ArrayList<MultiInteractB.FlagData> flags = new ArrayList<>();
   private Vector3d lastPos;
   private boolean hasInteracted;

   public MultiInteractB(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
         if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT) {
            return;
         }

         Vector3d pos = packet.getLocation();
         if (pos == null) {
            return;
         }

         if (this.hasInteracted && !pos.equals(this.lastPos)) {
            if (!this.player.canSkipTicks()) {
               if (this.flag(V.write(this.verbose()).f64(pos.x).f64(pos.y).f64(pos.z).f64(this.lastPos.x).f64(this.lastPos.y).f64(this.lastPos.z))
                  && this.shouldModifyPackets()) {
                  event.setCancelled(true);
                  this.player.onPacketCancel();
               }
            } else {
               this.flags.add(new MultiInteractB.FlagData(pos.x, pos.y, pos.z, this.lastPos.x, this.lastPos.y, this.lastPos.z));
            }
         }

         this.lastPos = pos;
         this.hasInteracted = true;
      }

      if (!this.player.cameraEntity.isSelf() || this.isTickPacket(event.getPacketType())) {
         this.hasInteracted = false;
      }
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (MultiInteractB.FlagData data : this.flags) {
               this.flag(
                  V.write(this.verbose()).f64(data.posX()).f64(data.posY()).f64(data.posZ()).f64(data.lastPosX()).f64(data.lastPosY()).f64(data.lastPosZ())
               );
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(double posX, double posY, double posZ, double lastPosX, double lastPosY, double lastPosZ) {
   }
}
