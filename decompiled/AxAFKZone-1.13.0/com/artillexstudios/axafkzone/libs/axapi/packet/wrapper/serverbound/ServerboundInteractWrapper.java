package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.serverbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.ServerboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3d;
import com.artillexstudios.axafkzone.libs.axapi.utils.Vector3f;
import com.artillexstudios.axafkzone.libs.axapi.utils.Version;
import java.util.function.Function;

public final class ServerboundInteractWrapper extends PacketWrapper {
   private int entityId;
   private ServerboundInteractWrapper.ActionType type;
   private ServerboundInteractWrapper.Action action;
   private boolean usingSecondaryAction;

   public ServerboundInteractWrapper(PacketEvent event) {
      super(event);
   }

   public int entityId() {
      return this.entityId;
   }

   public void entityId(int entityId) {
      this.entityId = entityId;
   }

   public ServerboundInteractWrapper.ActionType type() {
      return this.type;
   }

   public void type(ServerboundInteractWrapper.ActionType type) {
      this.type = type;
   }

   public ServerboundInteractWrapper.Action action() {
      return this.action;
   }

   public void action(ServerboundInteractWrapper.Action action) {
      this.action = action;
   }

   public boolean usingSecondaryAction() {
      return this.usingSecondaryAction;
   }

   public void usingSecondaryAction(boolean usingSecondaryAction) {
      this.usingSecondaryAction = usingSecondaryAction;
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeVarInt(this.entityId);
      if (Version.getServerVersion().isOlderThan(Version.v26_1)) {
         out.writeEnum(this.type);
      }

      this.action.write(out);
      out.writeBoolean(this.usingSecondaryAction);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.entityId = buf.readVarInt();
      if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
         this.action = new ServerboundInteractWrapper.InteractionAtLocationAction(buf);
      } else {
         this.type = buf.readEnum(ServerboundInteractWrapper.ActionType.class);
         this.action = this.type.mapper.apply(buf);
      }

      this.usingSecondaryAction = buf.readBoolean();
   }

   @Override
   public PacketType packetType() {
      return ServerboundPacketTypes.INTERACT;
   }

   public interface Action {
      void write(FriendlyByteBuf buf);
   }

   public static enum ActionType {
      INTERACT(ServerboundInteractWrapper.InteractionAction::new),
      ATTACK(buf -> new ServerboundInteractWrapper.AttackAction()),
      INTERACT_AT(ServerboundInteractWrapper.InteractionAtLocationAction::new);

      private Function<FriendlyByteBuf, ServerboundInteractWrapper.Action> mapper;

      private ActionType(Function<FriendlyByteBuf, ServerboundInteractWrapper.Action> mapper) {
         this.mapper = mapper;
      }
   }

   public static class AttackAction implements ServerboundInteractWrapper.Action {
      @Override
      public void write(FriendlyByteBuf buf) {
      }
   }

   public static class InteractionAction implements ServerboundInteractWrapper.Action {
      private final ServerboundInteractWrapper.InteractionHand hand;

      public InteractionAction(FriendlyByteBuf buf) {
         this.hand = buf.readEnum(ServerboundInteractWrapper.InteractionHand.class);
      }

      public ServerboundInteractWrapper.InteractionHand hand() {
         return this.hand;
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeEnum(this.hand);
      }
   }

   public static class InteractionAtLocationAction implements ServerboundInteractWrapper.Action {
      private final ServerboundInteractWrapper.InteractionHand hand;
      private final Vector3d location;

      public InteractionAtLocationAction(FriendlyByteBuf buf) {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
            this.hand = buf.readEnum(ServerboundInteractWrapper.InteractionHand.class);
            this.location = buf.readLpVec3();
         } else {
            this.location = new Vector3d(buf.readVector3f());
            this.hand = buf.readEnum(ServerboundInteractWrapper.InteractionHand.class);
         }
      }

      public ServerboundInteractWrapper.InteractionHand hand() {
         return this.hand;
      }

      public Vector3d location() {
         return this.location;
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         if (Version.getServerVersion().isNewerThanOrEqualTo(Version.v26_1)) {
            buf.writeEnum(this.hand);
            buf.writeLpVec3(this.location);
         } else {
            buf.writeVector3f(new Vector3f(this.location));
            buf.writeEnum(this.hand);
         }
      }
   }

   public static enum InteractionHand {
      MAIN_HAND,
      OFF_HAND;
   }
}
