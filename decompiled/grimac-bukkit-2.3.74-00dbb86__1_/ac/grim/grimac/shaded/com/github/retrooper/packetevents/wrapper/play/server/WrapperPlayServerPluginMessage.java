package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.resources.ResourceLocation;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.common.server.WrapperCommonServerPluginMessage;

public class WrapperPlayServerPluginMessage extends WrapperCommonServerPluginMessage<WrapperPlayServerPluginMessage> {
   public WrapperPlayServerPluginMessage(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerPluginMessage(ResourceLocation channelName, byte[] data) {
      this(channelName.toString(), data);
   }

   public WrapperPlayServerPluginMessage(String channelName, byte[] data) {
      super(PacketType.Play.Server.PLUGIN_MESSAGE, channelName, data);
   }

   @Override
   public void read() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
         this.channelName = this.readString();
      } else {
         this.channelName = this.readString(20);
      }

      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         short var1 = this.readShort();
      }

      this.data = this.readRemainingBytes();
   }

   @Override
   public void write() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
         this.writeString(this.channelName);
      } else {
         this.writeString(this.channelName, 20);
      }

      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         this.writeShort(this.data.length);
      }

      this.writeBytes(this.data);
   }
}
