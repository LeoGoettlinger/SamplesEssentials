package com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.clientbound;

import com.artillexstudios.axafkzone.libs.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axafkzone.libs.axapi.packet.FriendlyByteBuf;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketEvent;
import com.artillexstudios.axafkzone.libs.axapi.packet.PacketType;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.PacketWrapper;
import com.artillexstudios.axafkzone.libs.axapi.utils.BossBar;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.kyori.adventure.text.Component;

public final class ClientboundBossEventWrapper extends PacketWrapper {
   public static final ClientboundBossEventWrapper.Action REMOVE_ACTION = new ClientboundBossEventWrapper.Action() {
      @Override
      public void write(FriendlyByteBuf buf) {
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.REMOVE;
      }
   };
   private UUID uuid;
   private ClientboundBossEventWrapper.Action action;

   public ClientboundBossEventWrapper(UUID uuid, ClientboundBossEventWrapper.Action action) {
      this.uuid = uuid;
      this.action = action;
   }

   public ClientboundBossEventWrapper(PacketEvent event) {
      super(event);
   }

   @Override
   public void write(FriendlyByteBuf out) {
      out.writeUUID(this.uuid);
      out.writeEnum(this.action.type());
      this.action.write(out);
   }

   @Override
   public void read(FriendlyByteBuf buf) {
      this.uuid = buf.readUUID();
      ClientboundBossEventWrapper.ActionType actionType = buf.readEnum(ClientboundBossEventWrapper.ActionType.class);
      this.action = actionType.mapper.apply(buf);
   }

   @Override
   public PacketType packetType() {
      return ClientboundPacketTypes.BOSS_EVENT;
   }

   private static int encodeProperties(boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
      int i = 0;
      if (darkenSky) {
         i |= 1;
      }

      if (dragonMusic) {
         i |= 2;
      }

      if (thickenFog) {
         i |= 4;
      }

      return i;
   }

   public interface Action {
      void write(FriendlyByteBuf buf);

      ClientboundBossEventWrapper.ActionType type();
   }

   public static enum ActionType {
      ADD(ClientboundBossEventWrapper.AddAction::new),
      REMOVE(buf -> ClientboundBossEventWrapper.REMOVE_ACTION),
      UPDATE_PROGRESS(ClientboundBossEventWrapper.UpdateProgressAction::new),
      UPDATE_NAME(ClientboundBossEventWrapper.UpdateNameAction::new),
      UPDATE_STYLE(ClientboundBossEventWrapper.UpdateStyleAction::new),
      UPDATE_PROPERTIES(ClientboundBossEventWrapper.UpdateFlagsAction::new);

      private Function<FriendlyByteBuf, ClientboundBossEventWrapper.Action> mapper;

      private ActionType(Function<FriendlyByteBuf, ClientboundBossEventWrapper.Action> mapper) {
         this.mapper = mapper;
      }
   }

   public static class AddAction implements ClientboundBossEventWrapper.Action {
      private final Component component;
      private final float progress;
      private final BossBar.Color color;
      private final BossBar.Style style;
      private final Set<BossBar.Flag> flags;

      public AddAction(Component component, float progress, BossBar.Color color, BossBar.Style style, Set<BossBar.Flag> flags) {
         this.component = component;
         this.progress = progress;
         this.color = color;
         this.style = style;
         this.flags = flags;
      }

      public AddAction(FriendlyByteBuf buf) {
         this.component = buf.readComponent();
         this.progress = buf.readFloat();
         this.color = buf.readEnum(BossBar.Color.class);
         this.style = buf.readEnum(BossBar.Style.class);
         int flags = buf.readUnsignedByte();
         this.flags = EnumSet.noneOf(BossBar.Flag.class);
         if ((flags & 1) > 0) {
            this.flags.add(BossBar.Flag.DARKEN_SCREEN);
         }

         if ((flags & 2) > 0) {
            this.flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
         }

         if ((flags & 4) > 0) {
            this.flags.add(BossBar.Flag.CREATE_WORLD_FOG);
         }
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeComponent(this.component);
         buf.writeFloat(this.progress);
         buf.writeEnum(this.color);
         buf.writeEnum(this.style);
         buf.writeByte(
            ClientboundBossEventWrapper.encodeProperties(
               this.flags.contains(BossBar.Flag.DARKEN_SCREEN),
               this.flags.contains(BossBar.Flag.PLAY_BOSS_MUSIC),
               this.flags.contains(BossBar.Flag.CREATE_WORLD_FOG)
            )
         );
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.ADD;
      }
   }

   public static class UpdateFlagsAction implements ClientboundBossEventWrapper.Action {
      private final Set<BossBar.Flag> flags;

      public UpdateFlagsAction(Set<BossBar.Flag> flags) {
         this.flags = flags;
      }

      public UpdateFlagsAction(FriendlyByteBuf buf) {
         int flags = buf.readUnsignedByte();
         this.flags = EnumSet.noneOf(BossBar.Flag.class);
         if ((flags & 1) > 0) {
            this.flags.add(BossBar.Flag.DARKEN_SCREEN);
         }

         if ((flags & 2) > 0) {
            this.flags.add(BossBar.Flag.PLAY_BOSS_MUSIC);
         }

         if ((flags & 4) > 0) {
            this.flags.add(BossBar.Flag.CREATE_WORLD_FOG);
         }
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeByte(
            ClientboundBossEventWrapper.encodeProperties(
               this.flags.contains(BossBar.Flag.DARKEN_SCREEN),
               this.flags.contains(BossBar.Flag.PLAY_BOSS_MUSIC),
               this.flags.contains(BossBar.Flag.CREATE_WORLD_FOG)
            )
         );
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.UPDATE_PROPERTIES;
      }
   }

   public static class UpdateNameAction implements ClientboundBossEventWrapper.Action {
      private final Component name;

      public UpdateNameAction(Component name) {
         this.name = name;
      }

      public UpdateNameAction(FriendlyByteBuf buf) {
         this.name = buf.readComponent();
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeComponent(this.name);
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.UPDATE_NAME;
      }
   }

   public static class UpdateProgressAction implements ClientboundBossEventWrapper.Action {
      private final float progress;

      public UpdateProgressAction(float progress) {
         this.progress = progress;
      }

      public UpdateProgressAction(FriendlyByteBuf buf) {
         this.progress = buf.readFloat();
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeFloat(this.progress);
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.UPDATE_PROGRESS;
      }
   }

   public static class UpdateStyleAction implements ClientboundBossEventWrapper.Action {
      private final BossBar.Color color;
      private final BossBar.Style style;

      public UpdateStyleAction(BossBar.Color color, BossBar.Style style) {
         this.color = color;
         this.style = style;
      }

      public UpdateStyleAction(FriendlyByteBuf buf) {
         this.color = buf.readEnum(BossBar.Color.class);
         this.style = buf.readEnum(BossBar.Style.class);
      }

      @Override
      public void write(FriendlyByteBuf buf) {
         buf.writeEnum(this.color);
         buf.writeEnum(this.style);
      }

      @Override
      public ClientboundBossEventWrapper.ActionType type() {
         return ClientboundBossEventWrapper.ActionType.UPDATE_STYLE;
      }
   }
}
