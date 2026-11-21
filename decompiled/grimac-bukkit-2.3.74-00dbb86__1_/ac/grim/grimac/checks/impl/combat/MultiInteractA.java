package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectateEntity;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import java.util.ArrayList;

@CheckData(
   name = "MultiInteractA",
   stableKey = "grim.multiinteract.multiple_targets",
   description = "Interacted with multiple entities in the same tick",
   experimental = true
)
public class MultiInteractA extends Check implements PostPredictionCheck {
   private static final Verbose V = Verbose.of("lastEntity={sint}, entity={sint}, lastSneaking={bool}, sneaking={bool}");
   private final ArrayList<MultiInteractA.FlagData> flags = new ArrayList<>();
   private int lastEntity;
   private boolean lastSneaking;
   private boolean hasInteracted = false;

   public MultiInteractA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
         int entity = packet.getEntityId();
         boolean sneaking = packet.isSneaking().orElse(false);
         this.onInteract(event, entity, sneaking);
      }

      if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
         WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
         this.onInteract(event, packet.getEntityId(), this.lastSneaking);
      }

      if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
         WrapperPlayClientSpectateEntity packet = new WrapperPlayClientSpectateEntity(event);
         this.onInteract(event, packet.getEntityId(), this.lastSneaking);
      }

      if (!this.player.cameraEntity.isSelf() || this.isTickPacket(event.getPacketType())) {
         this.hasInteracted = false;
      }
   }

   private void onInteract(PacketReceiveEvent event, int entity, boolean sneaking) {
      if (this.hasInteracted && (entity != this.lastEntity || sneaking != this.lastSneaking)) {
         if (!this.player.canSkipTicks()) {
            if (this.flag(V.write(this.verbose()).sint(this.lastEntity).sint(entity).bool(this.lastSneaking).bool(sneaking)) && this.shouldModifyPackets()) {
               event.setCancelled(true);
               this.player.onPacketCancel();
            }
         } else {
            this.flags.add(new MultiInteractA.FlagData(this.lastEntity, entity, this.lastSneaking, sneaking));
         }
      }

      this.lastEntity = entity;
      this.lastSneaking = sneaking;
      this.hasInteracted = true;
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      if (this.player.canSkipTicks()) {
         if (this.player.isTickingReliablyFor(3)) {
            for (MultiInteractA.FlagData data : this.flags) {
               this.flag(V.write(this.verbose()).sint(data.lastEntity()).sint(data.entity()).bool(data.lastSneaking()).bool(data.sneaking()));
            }
         }

         this.flags.clear();
      }
   }

   private record FlagData(int lastEntity, int entity, boolean lastSneaking, boolean sneaking) {
   }
}
