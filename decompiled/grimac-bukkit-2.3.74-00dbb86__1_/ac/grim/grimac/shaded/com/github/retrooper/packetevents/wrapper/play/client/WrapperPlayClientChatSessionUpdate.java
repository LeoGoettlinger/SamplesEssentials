package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.chat.RemoteChatSession;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;

public class WrapperPlayClientChatSessionUpdate extends PacketWrapper<WrapperPlayClientChatSessionUpdate> {
   private RemoteChatSession chatSession;

   public WrapperPlayClientChatSessionUpdate(PacketReceiveEvent event) {
      super(event);
   }

   public WrapperPlayClientChatSessionUpdate(RemoteChatSession chatSession) {
      super(PacketType.Play.Client.CHAT_SESSION_UPDATE);
      this.chatSession = chatSession;
   }

   @Override
   public void read() {
      this.chatSession = this.readRemoteChatSession();
   }

   @Override
   public void write() {
      this.writeRemoteChatSession(this.chatSession);
   }

   public void copy(WrapperPlayClientChatSessionUpdate wrapper) {
      this.chatSession = wrapper.chatSession;
   }

   public RemoteChatSession getChatSession() {
      return this.chatSession;
   }

   public void setChatSession(RemoteChatSession chatSession) {
      this.chatSession = chatSession;
   }
}
