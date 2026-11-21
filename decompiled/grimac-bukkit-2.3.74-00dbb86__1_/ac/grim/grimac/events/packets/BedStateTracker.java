package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3d;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUseBed;

public class BedStateTracker extends PacketListenerAbstract {
   public BedStateTracker() {
      super(PacketListenerPriority.HIGH);
   }

   @Override
   public boolean isPreVia() {
      return true;
   }

   @Override
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPacketType() == PacketType.Play.Server.USE_BED) {
         WrapperPlayServerUseBed bed = new WrapperPlayServerUseBed(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player != null && player.entityID == bed.getEntityId()) {
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
               player.isInBed = true;
               player.bedPosition = new Vector3d(bed.getPosition().getX() + 0.5, bed.getPosition().getY(), bed.getPosition().getZ() + 0.5);
            });
         }
      }

      if (event.getPacketType() == PacketType.Play.Server.ENTITY_ANIMATION) {
         WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player != null
            && player.entityID == animation.getEntityId()
            && animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) {
            player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get() + 1, () -> player.isInBed = false);
            event.getTasksAfterSend().add(player::sendTransaction);
         }
      }
   }
}
