package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.impl.elytra.ElytraA;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import ac.grim.grimac.utils.data.IntToObjectPair;
import ac.grim.grimac.utils.data.SprintingState;
import ac.grim.grimac.utils.data.packetentity.JumpableEntity;

public class PacketEntityAction extends PacketListenerAbstract {
   public PacketEntityAction() {
      super(PacketListenerPriority.LOW);
   }

   @Override
   public boolean isPreVia() {
      return true;
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
         WrapperPlayClientEntityAction action = new WrapperPlayClientEntityAction(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         switch (action.getAction()) {
            case START_SPRINTING:
               player.isSprinting = true;
               player.vehicleData.camelSprintingState = SprintingState.STARTED;
               break;
            case STOP_SPRINTING:
               player.isSprinting = false;
               player.vehicleData.camelSprintingState = SprintingState.STOPPED;
               break;
            case START_SNEAKING:
               player.isSneaking = true;
               break;
            case STOP_SNEAKING:
               player.isSneaking = false;
               break;
            case START_FLYING_WITH_ELYTRA:
               if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_9)) {
                  return;
               }

               if (!player.onGround && !player.lastOnGround) {
                  player.checkManager.getPostPredictionCheck(ElytraA.class).onStartGliding(event);
                  if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) {
                     return;
                  }

                  if (player.canGlide()) {
                     player.isGliding = true;
                     player.pointThreeEstimator.updatePlayerGliding();
                  } else {
                     player.getSetbackTeleportUtil().executeNonSimulatingForceResync();
                     if (player.platformPlayer != null) {
                        player.platformPlayer.setSneaking(!player.platformPlayer.isSneaking());
                     }

                     event.setCancelled(true);
                     player.onPacketCancel();
                  }
               } else {
                  player.getSetbackTeleportUtil().executeNonSimulatingForceResync();
                  if (player.platformPlayer != null) {
                     player.platformPlayer.setSneaking(!player.platformPlayer.isSneaking());
                  }

                  event.setCancelled(true);
                  player.onPacketCancel();
               }
               break;
            case START_JUMPING_WITH_HORSE:
               if (player.compensatedEntities.self.getRiding() instanceof JumpableEntity jumpable) {
                  if (player.vehicleData.pendingJumps.size() >= 20) {
                     return;
                  }

                  player.vehicleData.pendingJumps.add(new IntToObjectPair<>(action.getJumpBoost(), jumpable));
               }
         }
      }
   }
}
