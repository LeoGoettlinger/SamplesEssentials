package com.github.retrooper.packetevents.wrapper.play.client;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Internal;

public class WrapperPlayClientClientStatus extends PacketWrapper<WrapperPlayClientClientStatus> {
   private WrapperPlayClientClientStatus.Action action;

   public WrapperPlayClientClientStatus(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientClientStatus(WrapperPlayClientClientStatus.Action action) {
      super(PacketType.Play.Client.CLIENT_STATUS);
      this.action = action;
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
         this.action = this.readEnum(WrapperPlayClientClientStatus.Action.VALUES);
      } else {
         this.action = WrapperPlayClientClientStatus.Action.VALUES[this.readByte()];
      }
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8)) {
         if (this.action == WrapperPlayClientClientStatus.Action.THIRD_ENTRY
            && this.serverVersion.isOlderThan(ServerVersion.V_26_1)
            && this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
            throw new IllegalStateException("Third entry not supported");
         }

         this.writeEnum(this.action);
      } else {
         this.writeByte(this.action.ordinal());
      }
   }

   public void copy(WrapperPlayClientClientStatus wrapper) {
      this.action = wrapper.action;
   }

   public WrapperPlayClientClientStatus.Action getAction() {
      return this.action;
   }

   public void setAction(WrapperPlayClientClientStatus.Action action) {
      this.action = action;
   }

   public static enum Action {
      PERFORM_RESPAWN,
      REQUEST_STATS,
      @Internal
      THIRD_ENTRY;

      public static final WrapperPlayClientClientStatus.Action OPEN_INVENTORY_ACHIEVEMENT = THIRD_ENTRY;
      public static final WrapperPlayClientClientStatus.Action REQUEST_GAMERULE_VALUES = THIRD_ENTRY;
      private static final WrapperPlayClientClientStatus.Action[] VALUES = values();

      public static WrapperPlayClientClientStatus.Action getById(int index) {
         return VALUES[index];
      }
   }
}
