package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerChatPreview extends PacketWrapper<WrapperPlayServerChatPreview> {
   private int queryId;
   @Nullable
   private Component message;

   public WrapperPlayServerChatPreview(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerChatPreview(int queryId, @Nullable Component message) {
      super(PacketType.Play.Server.CHAT_PREVIEW_PACKET);
      this.queryId = queryId;
      this.message = message;
   }

   @Override
   public void read() {
      this.queryId = this.readInt();
      this.message = this.readOptional(PacketWrapper::readComponent);
   }

   @Override
   public void write() {
      this.writeInt(this.queryId);
      this.writeOptional(this.message, PacketWrapper::writeComponent);
   }

   public void copy(WrapperPlayServerChatPreview wrapper) {
      this.queryId = wrapper.queryId;
      this.message = wrapper.message;
   }

   public int getQueryId() {
      return this.queryId;
   }

   public void setQueryId(int queryId) {
      this.queryId = queryId;
   }

   public Optional<Component> getMessage() {
      return Optional.ofNullable(this.message);
   }

   public void setMessage(@Nullable Component message) {
      this.message = message;
   }
}
